
import { useCallback, useEffect, useState } from 'react'
import './App.css'
import Auth from './Auth.jsx'

const API_URL = 'https://tracenest-ai.onrender.com/api/decisions'

function authHeaders(token, extraHeaders = {}) {
  return {
    ...extraHeaders,
    Authorization: `Bearer ${token}`,
  }
}

function normalizeStatus(status) {
  if (!status) return 'UNSET'

  const value = String(status).toUpperCase().replace(/[\s-]+/g, '_')

  if (value === 'INPROGRESS') return 'IN_PROGRESS'
  if (['PENDING', 'IN_PROGRESS', 'COMPLETED'].includes(value)) return value

  return 'UNSET'
}

function formatStatus(status) {
  return normalizeStatus(status)
    .replaceAll('_', ' ')
    .toLowerCase()
    .replace(/\b\w/g, (letter) => letter.toUpperCase())
}

function formatDate(value) {
  if (!value) return '—'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)

  return date.toLocaleDateString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  })
}

function formatDateTime(value) {
  if (!value) return 'Date unavailable'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)

  return date.toLocaleString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

async function readResponse(response) {
  const text = await response.text()
  if (!text) return null

  try {
    return JSON.parse(text)
  } catch {
    return text
  }
}

function getErrorMessage(data, fallback) {
  if (typeof data === 'string' && data.trim()) return data
  if (data?.message) return data.message
  if (data?.error) return data.error
  return fallback
}

function StatusBadge({ status }) {
  const normalized = normalizeStatus(status)

  return (
    <span className={`status-badge ${normalized}`}>
      <span className="status-dot" />
      {formatStatus(normalized)}
    </span>
  )
}

function StatCard({ icon, tone, label, value, description }) {
  return (
    <article className="stat-card">
      <div className={`stat-icon ${tone}`}>{icon}</div>
      <div className="stat-content">
        <p>{label}</p>
        <h2>{value}</h2>
        <span>{description}</span>
      </div>
    </article>
  )
}

function EmptyState({ search, onCreate }) {
  return (
    <div className="empty-state">
      <div className="empty-icon">⌕</div>
      <h3>{search ? 'No matching decisions' : 'No decisions yet'}</h3>
      <p>
        {search
          ? 'Try a different search term or clear your filters.'
          : 'Create your first decision to start tracking progress and history.'}
      </p>

      {!search && (
        <button
          className="button button-primary"
          onClick={onCreate}
          type="button"
        >
          + Create decision
        </button>
      )}
    </div>
  )
}

function LoadingRows() {
  return Array.from({ length: 4 }, (_, index) => (
    <tr key={index}>
      <td>
        <div className="skeleton skeleton-short" />
      </td>
      <td>
        <div className="skeleton skeleton-title" />
        <div className="skeleton skeleton-reason" />
      </td>
      <td>
        <div className="skeleton skeleton-status" />
      </td>
      <td>
        <div className="skeleton skeleton-date" />
      </td>
      <td>
        <div className="skeleton skeleton-actions" />
      </td>
    </tr>
  ))
}

export default function App() {
  // Authentication
  const [token, setToken] = useState(
    () => sessionStorage.getItem('tracenest_token') || '',
  )
  const [userEmail, setUserEmail] = useState(
    () => sessionStorage.getItem('tracenest_email') || '',
  )

  // Dashboard data
  const [decisions, setDecisions] = useState([])
  const [dashboard, setDashboard] = useState({})
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [saving, setSaving] = useState(false)

  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  // Search, filters and sorting
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [sortOrder, setSortOrder] = useState('desc')

  // Pagination
  const [currentPage, setCurrentPage] = useState(0)
  const [pageSize, setPageSize] = useState(5)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  // Create/edit form
  const [editingId, setEditingId] = useState(null)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({
    title: '',
    reason: '',
    status: 'PENDING',
  })

  // Decision history
  const [historyItems, setHistoryItems] = useState([])
  const [historyDecision, setHistoryDecision] = useState(null)
  const [historyLoading, setHistoryLoading] = useState(false)
  const [historyError, setHistoryError] = useState('')

  const handleLogin = (newToken, email) => {
    sessionStorage.setItem('tracenest_token', newToken)
    sessionStorage.setItem('tracenest_email', email)

    setToken(newToken)
    setUserEmail(email)
    setCurrentPage(0)
    setError('')
    setNotice('')
  }

  const handleLogout = () => {
    sessionStorage.removeItem('tracenest_token')
    sessionStorage.removeItem('tracenest_email')

    setToken('')
    setUserEmail('')
    setDecisions([])
    setDashboard({})
    setTotalPages(0)
    setTotalElements(0)
    setHistoryDecision(null)
    setHistoryItems([])
    setShowForm(false)
    setEditingId(null)
    setError('')
    setNotice('')
    setLoading(false)
    setRefreshing(false)
  }

  const closeHistory = () => {
    setHistoryDecision(null)
    setHistoryItems([])
    setHistoryError('')
    setHistoryLoading(false)
  }

  const closeForm = () => {
    setShowForm(false)
    setEditingId(null)
    setForm({
      title: '',
      reason: '',
      status: 'PENDING',
    })
  }

  // Load the current server-side page and dashboard statistics.
  const refreshAll = useCallback(
    async (quiet = false) => {
      if (!token) {
        setDecisions([])
        setDashboard({})
        setTotalPages(0)
        setTotalElements(0)
        setLoading(false)
        setRefreshing(false)
        return
      }

      if (quiet) {
        setRefreshing(true)
      } else {
        setLoading(true)
      }

      setError('')

      try {
        const params = new URLSearchParams({
          page: String(currentPage),
          size: String(pageSize),
          sortBy: 'id',
          direction: sortOrder,
        })

        if (search.trim()) {
          params.set('search', search.trim())
        }

        if (statusFilter !== 'ALL' && statusFilter !== 'UNSET') {
          params.set('status', statusFilter)
        }

        const [decisionsResponse, dashboardResponse] = await Promise.all([
          fetch(`${API_URL}/page?${params.toString()}`, {
            headers: authHeaders(token),
          }),
          fetch(`${API_URL}/dashboard`, {
            headers: authHeaders(token),
          }),
        ])

        if (
          decisionsResponse.status === 401 ||
          decisionsResponse.status === 403 ||
          dashboardResponse.status === 401 ||
          dashboardResponse.status === 403
        ) {
          handleLogout()
          return
        }

        const decisionsData = await readResponse(decisionsResponse)
        const dashboardData = await readResponse(dashboardResponse)

        if (!decisionsResponse.ok) {
          throw new Error(
            getErrorMessage(decisionsData, 'Unable to load decisions.'),
          )
        }

        if (!dashboardResponse.ok) {
          throw new Error(
            getErrorMessage(
              dashboardData,
              'Unable to load dashboard statistics.',
            ),
          )
        }

        setDecisions(
          Array.isArray(decisionsData?.content)
            ? decisionsData.content
            : [],
        )

        setTotalPages(Number(decisionsData?.totalPages ?? 0))
        setTotalElements(Number(decisionsData?.totalElements ?? 0))

        setDashboard(
          dashboardData && typeof dashboardData === 'object'
            ? dashboardData
            : {},
        )
      } catch (err) {
        setError(
          `${err.message || 'Unable to connect to the backend.'} Please check the backend service.`,
        )
      } finally {
        setLoading(false)
        setRefreshing(false)
      }
    },
    [token, currentPage, pageSize, search, statusFilter, sortOrder],
  )

  useEffect(() => {
    if (token) {
      refreshAll()
    } else {
      setLoading(false)
    }
  }, [token, refreshAll])

  // Reset to the first page when search or filters change.
  useEffect(() => {
    setCurrentPage(0)
  }, [search, statusFilter, pageSize, sortOrder])

  const openCreateForm = () => {
    setEditingId(null)
    setForm({
      title: '',
      reason: '',
      status: 'PENDING',
    })
    setShowForm(true)
    setNotice('')
    setError('')
  }

  const handleEdit = (decision) => {
    setEditingId(decision.id)
    setForm({
      title: decision.title ?? '',
      reason: decision.reason ?? '',
      status:
        normalizeStatus(decision.status) === 'UNSET'
          ? 'PENDING'
          : normalizeStatus(decision.status),
    })
    setShowForm(true)
    setNotice('')
    setError('')
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const handleSubmit = async (event) => {
    event.preventDefault()

    if (!form.title.trim() || !form.reason.trim()) {
      setError('Please enter both a decision title and reason.')
      return
    }

    if (!token) {
      setError('Please log in before saving a decision.')
      return
    }

    setSaving(true)
    setError('')
    setNotice('')

    const payload = {
      title: form.title.trim(),
      reason: form.reason.trim(),
      status: form.status,
    }

    try {
      const response = await fetch(
        editingId ? `${API_URL}/${editingId}` : API_URL,
        {
          method: editingId ? 'PUT' : 'POST',
          headers: authHeaders(token, {
            'Content-Type': 'application/json',
          }),
          body: JSON.stringify(payload),
        },
      )

      if (response.status === 401 || response.status === 403) {
        handleLogout()
        return
      }

      const data = await readResponse(response)

      if (!response.ok) {
        throw new Error(
          getErrorMessage(data, 'Unable to save the decision.'),
        )
      }

      setNotice(
        editingId
          ? 'Decision updated successfully.'
          : 'Decision created successfully.',
      )

      closeForm()
      setCurrentPage(0)
    } catch (err) {
      setError(err.message || 'Unable to save the decision.')
    } finally {
      setSaving(false)
    }
  }

  // Refresh after the page state changes following a successful save.
  useEffect(() => {
    if (token && !showForm && notice) {
      refreshAll(true)
    }
  }, [token, showForm, notice, refreshAll])

  const handleDelete = async (decision) => {
    const confirmed = window.confirm(
      `Delete decision "${decision.title}" (ID ${decision.id})? This cannot be undone.`,
    )

    if (!confirmed) return
    if (!token) return

    setError('')
    setNotice('')

    try {
      const response = await fetch(`${API_URL}/${decision.id}`, {
        method: 'DELETE',
        headers: authHeaders(token),
      })

      if (response.status === 401 || response.status === 403) {
        handleLogout()
        return
      }

      const data = await readResponse(response)

      if (!response.ok) {
        throw new Error(
          getErrorMessage(data, 'Unable to delete the decision.'),
        )
      }

      setNotice(`Decision #${decision.id} deleted successfully.`)

      if (historyDecision?.id === decision.id) {
        closeHistory()
      }

      if (decisions.length === 1 && currentPage > 0) {
        setCurrentPage((page) => page - 1)
      } else {
        await refreshAll(true)
      }
    } catch (err) {
      setError(err.message || 'Unable to delete the decision.')
    }
  }

  const handleQuickStatusChange = async (decision, status) => {
    if (!token) return

    setError('')
    setNotice('')

    try {
      const response = await fetch(`${API_URL}/${decision.id}`, {
        method: 'PUT',
        headers: authHeaders(token, {
          'Content-Type': 'application/json',
        }),
        body: JSON.stringify({
          title: decision.title,
          reason: decision.reason,
          status,
        }),
      })

      if (response.status === 401 || response.status === 403) {
        handleLogout()
        return
      }

      const data = await readResponse(response)

      if (!response.ok) {
        throw new Error(
          getErrorMessage(data, 'Unable to update status.'),
        )
      }

      setNotice(`Status updated for decision #${decision.id}.`)
      await refreshAll(true)
    } catch (err) {
      setError(err.message || 'Unable to update status.')
    }
  }

  const handleViewHistory = async (decision) => {
    if (!token) return

    setHistoryDecision(decision)
    setHistoryItems([])
    setHistoryError('')
    setHistoryLoading(true)

    try {
      const response = await fetch(
        `${API_URL}/${decision.id}/history`,
        {
          headers: authHeaders(token),
        },
      )

      if (response.status === 401 || response.status === 403) {
        handleLogout()
        return
      }

      const data = await readResponse(response)

      if (!response.ok) {
        throw new Error(
          getErrorMessage(data, 'Unable to load decision history.'),
        )
      }

      setHistoryItems(Array.isArray(data) ? data : [])
    } catch (err) {
      setHistoryError(err.message || 'Unable to load decision history.')
    } finally {
      setHistoryLoading(false)
    }
  }

  const clearFilters = () => {
    setSearch('')
    setStatusFilter('ALL')
    setCurrentPage(0)
  }

  const totalCount = Number(
    dashboard.totalDecisions ??
      dashboard.total ??
      dashboard.totalCount ??
      totalElements,
  )

  const pendingCount = Number(
    dashboard.pendingDecisions ??
      dashboard.pending ??
      dashboard.pendingCount ??
      0,
  )

  const progressCount = Number(
    dashboard.inProgressDecisions ??
      dashboard.inProgress ??
      dashboard.inProgressCount ??
      0,
  )

  const completedCount = Number(
    dashboard.completedDecisions ??
      dashboard.completed ??
      dashboard.completedCount ??
      0,
  )

  const firstRecord =
    totalElements === 0 ? 0 : currentPage * pageSize + 1

  const lastRecord = Math.min(
    (currentPage + 1) * pageSize,
    totalElements,
  )

  const hasActiveFilters = Boolean(
    search.trim() || statusFilter !== 'ALL',
  )

  if (!token) {
    return <Auth onLogin={handleLogin} />
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark">T</div>
          <div className="brand-name">
            TraceNest <strong>AI</strong>
            <small>DECISION INTELLIGENCE</small>
          </div>
        </div>

        <div className="sidebar-label">WORKSPACE</div>

        <nav className="sidebar-nav">
          <a className="nav-item active" href="#dashboard">
            <span className="nav-icon">▦</span>
            Dashboard
          </a>
          <a className="nav-item" href="#decisions">
            <span className="nav-icon">☷</span>
            Decisions
            <span className="nav-count">{totalElements}</span>
          </a>
        </nav>

        <div className="sidebar-label sidebar-label-spaced">
          BY STATUS
        </div>

        <div className="sidebar-metric">
          <span className="metric-dot pending-dot" />
          Pending
          <strong>{pendingCount}</strong>
        </div>

        <div className="sidebar-metric">
          <span className="metric-dot progress-dot" />
          In progress
          <strong>{progressCount}</strong>
        </div>

        <div className="sidebar-metric">
          <span className="metric-dot completed-dot" />
          Completed
          <strong>{completedCount}</strong>
        </div>

        <div className="sidebar-bottom">
          <div className="sidebar-help-icon">?</div>
          <strong>Keep decisions clear.</strong>
          <p>
            Record the why, track the progress, and keep the history.
          </p>

          <button
            className="sidebar-create"
            onClick={openCreateForm}
            type="button"
          >
            + New decision
          </button>
        </div>

        <div className="sidebar-footer">
          <span className="online-dot" />
          Connected workspace
        </div>
      </aside>

      <main className="main-content" id="dashboard">
        <header className="topbar">
          <div className="breadcrumb">
            Workspace <span>/</span> <strong>Dashboard</strong>
          </div>

          <div className="topbar-right">
            <div className="connection-status">
              <span className="online-dot" />
              Connected to workspace
            </div>

            <span className="user-email" title={userEmail}>
              {userEmail}
            </span>

            <button
              className="button button-secondary"
              type="button"
              onClick={handleLogout}
            >
              Log out
            </button>

            <div className="avatar">
              {userEmail
                ? userEmail.slice(0, 2).toUpperCase()
                : 'TN'}
            </div>
          </div>
        </header>

        <div className="page-content">
          <section className="welcome-row">
            <div>
              <div className="eyebrow">YOUR WORKSPACE</div>
              <h1>Decision Dashboard</h1>
              <p className="page-subtitle">
                Make decisions visible. Keep progress accountable.
              </p>
            </div>

            <div className="welcome-actions">
              <button
                className="button button-secondary"
                onClick={() => refreshAll(true)}
                disabled={refreshing || loading}
                type="button"
              >
                <span className={refreshing ? 'spin' : ''}>↻</span>
                {refreshing ? 'Refreshing…' : 'Refresh'}
              </button>

              <button
                className="button button-primary"
                onClick={openCreateForm}
                type="button"
              >
                + New decision
              </button>
            </div>
          </section>

          {error && (
            <div className="alert alert-error" role="alert">
              <span className="alert-symbol">!</span>
              <div>
                <strong>Something went wrong</strong>
                <p>{error}</p>
              </div>
              <button
                className="alert-close"
                type="button"
                onClick={() => setError('')}
                aria-label="Dismiss error"
              >
                ×
              </button>
            </div>
          )}

          {notice && (
            <div className="alert alert-success" role="status">
              <span className="alert-symbol">✓</span>
              <p>{notice}</p>
              <button
                className="alert-close"
                type="button"
                onClick={() => setNotice('')}
                aria-label="Dismiss notification"
              >
                ×
              </button>
            </div>
          )}

          <section
            className="stats-grid"
            aria-label="Decision statistics"
          >
            <StatCard
              icon="▦"
              tone="purple"
              label="Total decisions"
              value={loading ? '—' : totalCount}
              description="All recorded decisions"
            />
            <StatCard
              icon="◷"
              tone="amber"
              label="Pending"
              value={loading ? '—' : pendingCount}
              description="Awaiting action"
            />
            <StatCard
              icon="↗"
              tone="blue"
              label="In progress"
              value={loading ? '—' : progressCount}
              description="Currently being worked on"
            />
            <StatCard
              icon="✓"
              tone="green"
              label="Completed"
              value={loading ? '—' : completedCount}
              description="Marked as completed"
            />
          </section>

          {showForm && (
            <section
              className="form-card"
              aria-label="Decision form"
            >
              <div className="section-heading">
                <div>
                  <div className="eyebrow">
                    {editingId ? 'UPDATE RECORD' : 'NEW RECORD'}
                  </div>
                  <h2>
                    {editingId ? 'Edit decision' : 'Create a decision'}
                  </h2>
                  <p>
                    Capture the decision and the reason behind it.
                  </p>
                </div>

                <button
                  className="icon-button"
                  onClick={closeForm}
                  type="button"
                  aria-label="Close form"
                >
                  ×
                </button>
              </div>

              <form className="decision-form" onSubmit={handleSubmit}>
                <label>
                  Decision title <span>*</span>
                  <input
                    autoFocus
                    maxLength={200}
                    required
                    value={form.title}
                    onChange={(event) =>
                      setForm({
                        ...form,
                        title: event.target.value,
                      })
                    }
                    placeholder="e.g. Choose Spring Boot for the backend"
                  />
                  <small>Use a clear, descriptive title.</small>
                </label>

                <label>
                  Reason <span>*</span>
                  <textarea
                    required
                    rows={3}
                    maxLength={2000}
                    value={form.reason}
                    onChange={(event) =>
                      setForm({
                        ...form,
                        reason: event.target.value,
                      })
                    }
                    placeholder="Why was this decision made?"
                  />
                  <small>Explain the context and reasoning.</small>
                </label>

                <label>
                  Status
                  <select
                    value={form.status}
                    onChange={(event) =>
                      setForm({
                        ...form,
                        status: event.target.value,
                      })
                    }
                  >
                    <option value="PENDING">Pending</option>
                    <option value="IN_PROGRESS">In progress</option>
                    <option value="COMPLETED">Completed</option>
                  </select>
                </label>

                <div className="form-actions">
                  <button
                    className="button button-secondary"
                    onClick={closeForm}
                    type="button"
                    disabled={saving}
                  >
                    Cancel
                  </button>
                  <button
                    className="button button-primary"
                    type="submit"
                    disabled={saving}
                  >
                    {saving
                      ? 'Saving…'
                      : editingId
                        ? 'Save changes'
                        : 'Create decision'}
                  </button>
                </div>
              </form>
            </section>
          )}

          <section className="decisions-card" id="decisions">
            <div className="table-heading">
              <div>
                <h2>All decisions</h2>
                <p>
                  Review and manage the decisions in your workspace.
                </p>
              </div>
              <span className="record-count">
                {totalElements} records
              </span>
            </div>

            <div className="toolbar">
              <div className="search-box">
                <span>⌕</span>
                <input
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                  placeholder="Search by title, reason, or ID…"
                  aria-label="Search decisions"
                />
                {search && (
                  <button
                    type="button"
                    onClick={() => setSearch('')}
                    aria-label="Clear search"
                  >
                    ×
                  </button>
                )}
              </div>

              <label className="filter-control">
                Status
                <select
                  value={statusFilter}
                  onChange={(event) =>
                    setStatusFilter(event.target.value)
                  }
                  aria-label="Filter by status"
                >
                  <option value="ALL">All statuses</option>
                  <option value="PENDING">Pending</option>
                  <option value="IN_PROGRESS">In progress</option>
                  <option value="COMPLETED">Completed</option>
                  <option value="UNSET">Unset</option>
                </select>
              </label>

              <button
                className="button button-secondary sort-button"
                type="button"
                onClick={() =>
                  setSortOrder((current) =>
                    current === 'asc' ? 'desc' : 'asc',
                  )
                }
                title="Change decision ID sort order"
              >
                ID {sortOrder === 'asc' ? '↑' : '↓'}
              </button>

              {hasActiveFilters && (
                <button
                  className="button button-secondary sort-button"
                  type="button"
                  onClick={clearFilters}
                >
                  Clear
                </button>
              )}
            </div>

            <div className="table-wrap">
              <table className="decision-table">
                <thead>
                  <tr>
                    <th className="id-column">ID</th>
                    <th>DECISION</th>
                    <th>STATUS</th>
                    <th>CREATED</th>
                    <th className="actions-column">ACTIONS</th>
                  </tr>
                </thead>

                <tbody>
                  {loading ? (
                    <LoadingRows />
                  ) : decisions.length === 0 ? (
                    <tr>
                      <td colSpan={5}>
                        <EmptyState
                          search={hasActiveFilters}
                          onCreate={openCreateForm}
                        />
                      </td>
                    </tr>
                  ) : (
                    decisions.map((decision) => (
                      <tr key={decision.id}>
                        <td className="id-cell">#{decision.id}</td>

                        <td className="decision-cell">
                          <strong>
                            {decision.title || 'Untitled decision'}
                          </strong>
                          <p>
                            {decision.reason || 'No reason provided.'}
                          </p>
                        </td>

                        <td>
                          <StatusBadge status={decision.status} />
                          <select
                            className="quick-status"
                            value={
                              normalizeStatus(decision.status) === 'UNSET'
                                ? 'PENDING'
                                : normalizeStatus(decision.status)
                            }
                            onChange={(event) =>
                              handleQuickStatusChange(
                                decision,
                                event.target.value,
                              )
                            }
                            aria-label={`Change status for decision ${decision.id}`}
                          >
                            <option value="PENDING">Set pending</option>
                            <option value="IN_PROGRESS">
                              Set in progress
                            </option>
                            <option value="COMPLETED">
                              Set completed
                            </option>
                          </select>
                        </td>

                        <td className="date-cell">
                          {formatDate(decision.createdAt)}
                        </td>

                        <td>
                          <div className="row-actions">
                            <button
                              className="action-button edit-action"
                              type="button"
                              onClick={() => handleEdit(decision)}
                              title={`Edit decision ${decision.id}`}
                            >
                              Edit
                            </button>

                            <button
                              className="action-button edit-action"
                              type="button"
                              onClick={() => handleViewHistory(decision)}
                              title={`View history for decision ${decision.id}`}
                            >
                              History
                            </button>

                            <button
                              className="action-button delete-action"
                              type="button"
                              onClick={() => handleDelete(decision)}
                              title={`Delete decision ${decision.id}`}
                            >
                              Delete
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            <div className="table-footer">
              <span>
                Showing {loading ? 0 : firstRecord}–
                {loading ? 0 : lastRecord} of {totalElements} decisions
              </span>

              <div className="pagination-controls">
                <label className="page-size-control">
                  Rows
                  <select
                    value={pageSize}
                    onChange={(event) =>
                      setPageSize(Number(event.target.value))
                    }
                    aria-label="Rows per page"
                  >
                    <option value={5}>5</option>
                    <option value={10}>10</option>
                    <option value={20}>20</option>
                    <option value={50}>50</option>
                  </select>
                </label>

                <button
                  className="button button-secondary sort-button"
                  type="button"
                  disabled={loading || currentPage <= 0}
                  onClick={() =>
                    setCurrentPage((page) => Math.max(0, page - 1))
                  }
                >
                  Previous
                </button>

                <span className="page-indicator">
                  Page {totalPages === 0 ? 0 : currentPage + 1} of{' '}
                  {totalPages}
                </span>

                <button
                  className="button button-secondary sort-button"
                  type="button"
                  disabled={loading || currentPage + 1 >= totalPages}
                  onClick={() =>
                    setCurrentPage((page) =>
                      Math.min(totalPages - 1, page + 1),
                    )
                  }
                >
                  Next
                </button>
              </div>
            </div>
          </section>

          <footer className="page-footer">
            <span>© {new Date().getFullYear()} TraceNest AI</span>
            <span>Built for clearer decisions.</span>
          </footer>
        </div>
      </main>

      {historyDecision && (
        <div
          className="history-overlay"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget) closeHistory()
          }}
        >
          <aside
            className="history-panel"
            role="dialog"
            aria-modal="true"
            aria-labelledby="history-title"
          >
            <div className="history-header">
              <div>
                <div className="eyebrow">AUDIT TRAIL</div>
                <h2 id="history-title">Decision history</h2>
                <p>
                  #{historyDecision.id} ·{' '}
                  {historyDecision.title || 'Untitled decision'}
                </p>
              </div>

              <button
                className="icon-button"
                type="button"
                onClick={closeHistory}
                aria-label="Close history"
              >
                ×
              </button>
            </div>

            {historyLoading && (
              <div className="history-message">
                Loading decision history…
              </div>
            )}

            {historyError && (
              <div className="alert alert-error" role="alert">
                <span className="alert-symbol">!</span>
                <div>
                  <strong>Unable to load history</strong>
                  <p>{historyError}</p>
                </div>
              </div>
            )}

            {!historyLoading &&
              !historyError &&
              historyItems.length === 0 && (
                <div className="history-message">
                  No history entries were returned for this decision. Older
                  decisions may not have history records if they were created
                  before history tracking was added.
                </div>
              )}

            {!historyLoading && historyItems.length > 0 && (
              <div className="history-timeline">
                {historyItems.map((item, index) => (
                  <article
                    className="history-entry"
                    key={item.id ?? `${item.changedAt}-${index}`}
                  >
                    <span className="history-marker" />

                    <div className="history-entry-content">
                      <div className="history-entry-top">
                        <strong>
                          {String(item.action || 'UPDATED').replaceAll(
                            '_',
                            ' ',
                          )}
                        </strong>
                        <time>{formatDateTime(item.changedAt)}</time>
                      </div>

                      <p>{item.details || 'A change was recorded.'}</p>
                    </div>
                  </article>
                ))}
              </div>
            )}
          </aside>
        </div>
      )}
    </div>
  )
}
