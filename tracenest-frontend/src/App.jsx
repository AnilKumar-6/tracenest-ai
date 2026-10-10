
import { useCallback, useEffect, useMemo, useState } from 'react'
import './App.css'

const API_URL = '/api/decisions'

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
        <button className="button button-primary" onClick={onCreate}>
          + Create decision
        </button>
      )}
    </div>
  )
}

function LoadingRows() {
  return Array.from({ length: 4 }, (_, index) => (
    <tr key={index}>
      <td><div className="skeleton skeleton-short" /></td>
      <td>
        <div className="skeleton skeleton-title" />
        <div className="skeleton skeleton-reason" />
      </td>
      <td><div className="skeleton skeleton-status" /></td>
      <td><div className="skeleton skeleton-date" /></td>
      <td><div className="skeleton skeleton-actions" /></td>
    </tr>
  ))
}

export default function App() {
  const [decisions, setDecisions] = useState([])
  const [dashboard, setDashboard] = useState({})
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [saving, setSaving] = useState(false)

  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [sortOrder, setSortOrder] = useState('desc')

  const [editingId, setEditingId] = useState(null)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({
    title: '',
    reason: '',
    status: 'PENDING',
  })

  // History panel state
  const [historyItems, setHistoryItems] = useState([])
  const [historyDecision, setHistoryDecision] = useState(null)
  const [historyLoading, setHistoryLoading] = useState(false)
  const [historyError, setHistoryError] = useState('')

  const refreshAll = useCallback(async (quiet = false) => {
    if (quiet) {
      setRefreshing(true)
    } else {
      setLoading(true)
    }

    setError('')

    try {
      const [decisionsResponse, dashboardResponse] = await Promise.all([
        fetch(API_URL),
        fetch(`${API_URL}/dashboard`),
      ])

      const decisionsData = await readResponse(decisionsResponse)
      const dashboardData = await readResponse(dashboardResponse)

      if (!decisionsResponse.ok) {
        throw new Error(
          getErrorMessage(decisionsData, 'Unable to load decisions.'),
        )
      }

      if (!dashboardResponse.ok) {
        throw new Error(
          getErrorMessage(dashboardData, 'Unable to load dashboard statistics.'),
        )
      }

      setDecisions(Array.isArray(decisionsData) ? decisionsData : [])

      setDashboard(
        dashboardData && typeof dashboardData === 'object'
          ? dashboardData
          : {},
      )
    } catch (err) {
      setError(
        `${err.message || 'Unable to connect to the backend.'} Check that the Spring Boot server is running on port 8081.`,
      )
    } finally {
      setLoading(false)
      setRefreshing(false)
    }
  }, [])

  useEffect(() => {
    refreshAll()
  }, [refreshAll])

  const openCreateForm = () => {
    setEditingId(null)
    setForm({
      title: '',
      reason: '',
      status: 'PENDING',
    })
    setShowForm(true)
    setNotice('')
  }

  const handleEdit = (decision) => {
    setEditingId(decision.id)

    setForm({
      title: decision.title ?? '',
      reason: decision.reason ?? '',
      status: normalizeStatus(decision.status) === 'UNSET'
        ? 'PENDING'
        : normalizeStatus(decision.status),
    })

    setShowForm(true)
    setNotice('')
    setError('')

    window.scrollTo({ top: 0, behavior: 'smooth' })
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

  const handleSubmit = async (event) => {
    event.preventDefault()

    if (!form.title.trim() || !form.reason.trim()) {
      setError('Please enter both a decision title and reason.')
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
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(payload),
        },
      )

      const data = await readResponse(response)

      if (!response.ok) {
        throw new Error(
          getErrorMessage(data, 'Unable to save the decision.'),
        )
      }

      setNotice(editingId ? 'Decision updated successfully.' : 'Decision created successfully.')
      closeForm()
      await refreshAll(true)
    } catch (err) {
      setError(err.message || 'Unable to save the decision.')
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (decision) => {
    const confirmed = window.confirm(
      `Delete decision "${decision.title}" (ID ${decision.id})? This cannot be undone.`,
    )

    if (!confirmed) return

    setError('')
    setNotice('')

    try {
      const response = await fetch(`${API_URL}/${decision.id}`, {
        method: 'DELETE',
      })

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

      await refreshAll(true)
    } catch (err) {
      setError(err.message || 'Unable to delete the decision.')
    }
  }

  const handleQuickStatusChange = async (decision, status) => {
    setError('')
    setNotice('')

    try {
      const response = await fetch(`${API_URL}/${decision.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          title: decision.title,
          reason: decision.reason,
          status,
        }),
      })

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
    setHistoryDecision(decision)
    setHistoryItems([])
    setHistoryError('')
    setHistoryLoading(true)

    try {
      const response = await fetch(`${API_URL}/${decision.id}/history`)
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

  const closeHistory = () => {
    setHistoryDecision(null)
    setHistoryItems([])
    setHistoryError('')
    setHistoryLoading(false)
  }

  const clearFilters = () => {
    setSearch('')
    setStatusFilter('ALL')
  }

  const visibleDecisions = useMemo(() => {
    const term = search.trim().toLowerCase()

    return [...decisions]
      .filter((decision) => {
        const matchesSearch =
          !term ||
          String(decision.title ?? '').toLowerCase().includes(term) ||
          String(decision.reason ?? '').toLowerCase().includes(term) ||
          String(decision.id ?? '').includes(term)

        const matchesStatus =
          statusFilter === 'ALL' ||
          normalizeStatus(decision.status) === statusFilter

        return matchesSearch && matchesStatus
      })
      .sort((a, b) => {
        const first = Number(a.id) || 0
        const second = Number(b.id) || 0

        return sortOrder === 'asc' ? first - second : second - first
      })
  }, [decisions, search, statusFilter, sortOrder])

  const totalCount = Number(
    dashboard.totalDecisions ??
    dashboard.total ??
    dashboard.totalCount ??
    decisions.length,
  )

  const pendingCount = Number(
    dashboard.pendingDecisions ??
    dashboard.pending ??
    dashboard.pendingCount ??
    decisions.filter((item) => normalizeStatus(item.status) === 'PENDING').length,
  )

  const progressCount = Number(
    dashboard.inProgressDecisions ??
    dashboard.inProgress ??
    dashboard.inProgressCount ??
    decisions.filter((item) => normalizeStatus(item.status) === 'IN_PROGRESS').length,
  )

  const completedCount = Number(
    dashboard.completedDecisions ??
    dashboard.completed ??
    dashboard.completedCount ??
    decisions.filter((item) => normalizeStatus(item.status) === 'COMPLETED').length,
  )

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
            <span className="nav-count">{decisions.length}</span>
          </a>
        </nav>

        <div className="sidebar-label sidebar-label-spaced">BY STATUS</div>

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
          <p>Record the why, track the progress, and keep the history.</p>

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
          Local workspace
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
            <div className="avatar">AK</div>
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

          <section className="stats-grid" aria-label="Decision statistics">
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
            <section className="form-card" aria-label="Decision form">
              <div className="section-heading">
                <div>
                  <div className="eyebrow">
                    {editingId ? 'UPDATE RECORD' : 'NEW RECORD'}
                  </div>
                  <h2>{editingId ? 'Edit decision' : 'Create a decision'}</h2>
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
                      setForm({ ...form, title: event.target.value })
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
                      setForm({ ...form, reason: event.target.value })
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
                      setForm({ ...form, status: event.target.value })
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
                <p>Review and manage the decisions in your workspace.</p>
              </div>
              <span className="record-count">
                {visibleDecisions.length} records
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
                  onChange={(event) => setStatusFilter(event.target.value)}
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
                  setSortOrder((current) => current === 'asc' ? 'desc' : 'asc')
                }
                title="Change decision ID sort order"
              >
                ID {sortOrder === 'asc' ? '↑' : '↓'}
              </button>

              {(search || statusFilter !== 'ALL') && (
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
                  ) : visibleDecisions.length === 0 ? (
                    <tr>
                      <td colSpan={5}>
                        <EmptyState
                          search={Boolean(search || statusFilter !== 'ALL')}
                          onCreate={openCreateForm}
                        />
                      </td>
                    </tr>
                  ) : (
                    visibleDecisions.map((decision) => (
                      <tr key={decision.id}>
                        <td className="id-cell">#{decision.id}</td>

                        <td className="decision-cell">
                          <strong>{decision.title || 'Untitled decision'}</strong>
                          <p>{decision.reason || 'No reason provided.'}</p>
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
                              handleQuickStatusChange(decision, event.target.value)
                            }
                            aria-label={`Change status for decision ${decision.id}`}
                          >
                            <option value="PENDING">Set pending</option>
                            <option value="IN_PROGRESS">Set in progress</option>
                            <option value="COMPLETED">Set completed</option>
                          </select>
                        </td>

                        <td className="date-cell">
                          {formatDate(
                            decision.createdAt ??
                            decision.createdDate ??
                            decision.dateCreated,
                          )}
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
                Showing {loading ? 0 : visibleDecisions.length} of {decisions.length} decisions
              </span>
              <span className="footer-note">
                <span className="online-dot" />
                Your workspace data is stored locally
              </span>
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
                  #{historyDecision.id} · {historyDecision.title || 'Untitled decision'}
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
              <div className="history-message">Loading decision history…</div>
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

            {!historyLoading && !historyError && historyItems.length === 0 && (
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
                          {String(item.action || 'UPDATED').replaceAll('_', ' ')}
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
