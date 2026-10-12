
import { useState } from 'react'
import './Auth.css'

const AUTH_URL = '/api/auth'

export default function Auth({ onLogin }) {
  const [mode, setMode] = useState('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setMessage('')

    const normalizedEmail = email.trim().toLowerCase()

    if (!normalizedEmail || !password) {
      setError('Please enter your email and password.')
      return
    }

    if (mode === 'register') {
      if (password.length < 8 || password.length > 72) {
        setError('Password must contain between 8 and 72 characters.')
        return
      }

      if (password !== confirmPassword) {
        setError('The passwords do not match.')
        return
      }
    }

    setLoading(true)

    try {
      const response = await fetch(
        `${AUTH_URL}/${mode === 'login' ? 'login' : 'register'}`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            email: normalizedEmail,
            password,
          }),
        },
      )

      const text = await response.text()
      let data = {}

      try {
        data = text ? JSON.parse(text) : {}
      } catch {
        data = { message: text }
      }

      if (!response.ok) {
        throw new Error(
          data.message ||
            data.error ||
            (mode === 'login'
              ? 'Login failed. Check your credentials.'
              : 'Registration failed. Please try again.'),
        )
      }

      if (mode === 'register') {
        setMessage('Account created successfully. You can now log in.')
        setMode('login')
        setPassword('')
        setConfirmPassword('')
        return
      }

      if (!data.token) {
        throw new Error('The server did not return an authentication token.')
      }

      onLogin(data.token, normalizedEmail)
    } catch (err) {
      setError(
        err.message ||
          'Unable to connect to the server. Please try again.',
      )
    } finally {
      setLoading(false)
    }
  }

  function switchMode(nextMode) {
    setMode(nextMode)
    setError('')
    setMessage('')
    setPassword('')
    setConfirmPassword('')
  }

  return (
    <main className="auth-page">
      <section className="auth-card">
        <div className="auth-brand">
          <div className="auth-logo">T</div>
          <div>
            <h1>
              TraceNest <span>AI</span>
            </h1>
            <p>DECISION INTELLIGENCE</p>
          </div>
        </div>

        <div className="auth-heading">
          <h2>
            {mode === 'login' ? 'Welcome back' : 'Create your account'}
          </h2>
          <p>
            {mode === 'login'
              ? 'Log in to manage your decisions and track progress.'
              : 'Sign up to start organizing your decisions.'}
          </p>
        </div>

        <div className="auth-tabs">
          <button
            type="button"
            className={mode === 'login' ? 'active' : ''}
            onClick={() => switchMode('login')}
          >
            Log in
          </button>
          <button
            type="button"
            className={mode === 'register' ? 'active' : ''}
            onClick={() => switchMode('register')}
          >
            Register
          </button>
        </div>

        {error && (
          <div className="auth-error" role="alert">
            {error}
          </div>
        )}

        {message && (
          <div className="auth-success" role="status">
            {message}
          </div>
        )}

        <form className="auth-form" onSubmit={handleSubmit}>
          <label htmlFor="auth-email">Email address</label>
          <input
            id="auth-email"
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="you@example.com"
            autoComplete="email"
            maxLength={254}
            required
            disabled={loading}
          />

          <label htmlFor="auth-password">Password</label>
          <input
            id="auth-password"
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            placeholder="Enter your password"
            autoComplete={
              mode === 'login' ? 'current-password' : 'new-password'
            }
            minLength={8}
            maxLength={72}
            required
            disabled={loading}
          />

          {mode === 'register' && (
            <>
              <label htmlFor="auth-confirm-password">
                Confirm password
              </label>
              <input
                id="auth-confirm-password"
                type="password"
                value={confirmPassword}
                onChange={(event) =>
                  setConfirmPassword(event.target.value)
                }
                placeholder="Re-enter your password"
                autoComplete="new-password"
                minLength={8}
                maxLength={72}
                required
                disabled={loading}
              />
            </>
          )}

          <button
            className="auth-submit"
            type="submit"
            disabled={loading}
          >
            {loading
              ? 'Please wait…'
              : mode === 'login'
                ? 'Log in'
                : 'Create account'}
          </button>
        </form>

        <p className="auth-switch">
          {mode === 'login'
            ? "Don't have an account?"
            : 'Already have an account?'}{' '}
          <button
            type="button"
            onClick={() =>
              switchMode(mode === 'login' ? 'register' : 'login')
            }
            disabled={loading}
          >
            {mode === 'login' ? 'Register' : 'Log in'}
          </button>
        </p>

        <p className="auth-footer">
          Your decisions, organized in one workspace.
        </p>
      </section>
    </main>
  )
}
