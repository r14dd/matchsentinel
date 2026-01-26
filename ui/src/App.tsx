import { useEffect, useMemo, useState } from 'react'
import './App.css'

type Page<T> = {
  content: T[]
  totalElements: number
  totalPages: number
}

type Flag = {
  id: string
  transactionId: string
  accountId: string
  amount: number
  currency: string
  country: string
  merchant: string
  occurredAt: string
  createdAt: string
  riskScore: number
  reasons: string[]
}

type CaseItem = {
  id: string
  transactionId: string
  accountId: string
  status: string
  assignedAnalystId?: string | null
  riskScore: number
  reasons: string[]
  createdAt: string
  updatedAt: string
}

type NotificationItem = {
  id: string
  caseId: string
  eventType: string
  channel: string
  status: string
  recipient: string
  payload: string
  createdAt: string
  updatedAt: string
}

type DailyStat = {
  id: string
  statDate: string
  totalTransactions: number
  flaggedTransactions: number
  casesCreated: number
  notificationsSent: number
  createdAt: string
  updatedAt: string
}

type AiDecision = {
  id: string
  transactionId: string
  accountId: string
  amount: number
  currency: string
  country: string
  merchant: string
  occurredAt: string
  riskScore: number
  reasons: string[]
  modelVersion: string
  createdAt: string
}

function App() {
  const [flags, setFlags] = useState<Page<Flag> | null>(null)
  const [cases, setCases] = useState<Page<CaseItem> | null>(null)
  const [notifications, setNotifications] = useState<Page<NotificationItem> | null>(null)
  const [dailyStat, setDailyStat] = useState<DailyStat | null>(null)
  const [aiLookupId, setAiLookupId] = useState('')
  const [aiDecision, setAiDecision] = useState<AiDecision | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const endpoints = useMemo(
    () => ({
      ruleEngine: import.meta.env.VITE_RULE_ENGINE_URL ?? 'http://localhost:8083',
      cases: import.meta.env.VITE_CASE_SERVICE_URL ?? 'http://localhost:8084',
      notifications: import.meta.env.VITE_NOTIFICATION_SERVICE_URL ?? 'http://localhost:8085',
      reporting: import.meta.env.VITE_REPORTING_SERVICE_URL ?? 'http://localhost:8086',
      ai: import.meta.env.VITE_AI_SERVICE_URL ?? 'http://localhost:8087',
    }),
    []
  )

  const today = useMemo(() => new Date().toISOString().slice(0, 10), [])

  const fetchJson = async <T,>(url: string): Promise<T | null> => {
    try {
      const res = await fetch(url)
      if (!res.ok) return null
      return (await res.json()) as T
    } catch {
      return null
    }
  }

  const refreshAll = async () => {
    setLoading(true)
    setError(null)
    const [flagData, caseData, notificationData, reportData] = await Promise.all([
      fetchJson<Page<Flag>>(`${endpoints.ruleEngine}/api/flags`),
      fetchJson<Page<CaseItem>>(`${endpoints.cases}/api/cases`),
      fetchJson<Page<NotificationItem>>(`${endpoints.notifications}/api/notifications`),
      fetchJson<Page<DailyStat>>(`${endpoints.reporting}/api/reports/daily?date=${today}`),
    ])

    setFlags(flagData)
    setCases(caseData)
    setNotifications(notificationData)
    setDailyStat(reportData?.content?.[0] ?? null)
    if (!flagData || !caseData || !notificationData) {
      setError('Some services did not respond. Check CORS or container health.')
    }
    setLoading(false)
  }

  useEffect(() => {
    refreshAll()
  }, [])

  const lookupAi = async () => {
    if (!aiLookupId.trim()) {
      setAiDecision(null)
      return
    }
    const decision = await fetchJson<AiDecision>(
      `${endpoints.ai}/api/ai/decisions?transactionId=${aiLookupId.trim()}`
    )
    setAiDecision(decision)
  }

  return (
    <div className="app">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">MS</span>
          <div>
            <p className="brand-title">MatchSentinel</p>
            <p className="brand-sub">Analyst Console</p>
          </div>
        </div>
        <nav className="nav">
          <a href="#dashboard">Dashboard</a>
          <a href="#flags">Flags</a>
          <a href="#cases">Cases</a>
          <a href="#notifications">Notifications</a>
          <a href="#ai">AI Decisions</a>
        </nav>
        <div className="sidebar-foot">
          <p>Services: Auth · Transaction · Rule Engine · Case · Notification · Reporting · AI</p>
        </div>
      </aside>

      <main>
        <header className="topbar">
          <div>
            <h1>Risk Operations Overview</h1>
            <p>Live signals from your microservices pipeline.</p>
          </div>
          <div className="topbar-actions">
            <button className="ghost" onClick={refreshAll} disabled={loading}>
              {loading ? 'Refreshing…' : 'Refresh'}
            </button>
          </div>
        </header>

        {error && <div className="alert">{error}</div>}

        <section id="dashboard" className="section">
          <div className="section-head">
            <h2>Dashboard</h2>
            <span className="pill">Date: {today}</span>
          </div>
          <div className="stats-grid">
            <StatCard label="Total Transactions" value={dailyStat?.totalTransactions ?? '—'} />
            <StatCard label="Flagged Transactions" value={dailyStat?.flaggedTransactions ?? '—'} accent />
            <StatCard label="Cases Created" value={dailyStat?.casesCreated ?? '—'} />
            <StatCard label="Notifications Sent" value={dailyStat?.notificationsSent ?? '—'} />
          </div>
        </section>

        <section id="flags" className="section">
          <div className="section-head">
            <h2>Flags</h2>
            <span className="pill">{flags?.totalElements ?? 0} total</span>
          </div>
          <DataTable
            rows={flags?.content ?? []}
            columns={[
              { key: 'transactionId', label: 'Transaction' },
              { key: 'accountId', label: 'Account' },
              { key: 'amount', label: 'Amount' },
              { key: 'riskScore', label: 'Risk' },
              { key: 'reasons', label: 'Reasons' },
            ]}
          />
        </section>

        <section id="cases" className="section">
          <div className="section-head">
            <h2>Cases</h2>
            <span className="pill">{cases?.totalElements ?? 0} total</span>
          </div>
          <DataTable
            rows={cases?.content ?? []}
            columns={[
              { key: 'id', label: 'Case' },
              { key: 'status', label: 'Status' },
              { key: 'accountId', label: 'Account' },
              { key: 'riskScore', label: 'Risk' },
              { key: 'reasons', label: 'Reasons' },
            ]}
          />
        </section>

        <section id="notifications" className="section">
          <div className="section-head">
            <h2>Notifications</h2>
            <span className="pill">{notifications?.totalElements ?? 0} total</span>
          </div>
          <DataTable
            rows={notifications?.content ?? []}
            columns={[
              { key: 'eventType', label: 'Event' },
              { key: 'channel', label: 'Channel' },
              { key: 'status', label: 'Status' },
              { key: 'recipient', label: 'Recipient' },
            ]}
          />
        </section>

        <section id="ai" className="section">
          <div className="section-head">
            <h2>AI Decisions</h2>
            <span className="pill">Lookup by transaction</span>
          </div>
          <div className="ai-lookup">
            <input
              type="text"
              placeholder="Transaction ID"
              value={aiLookupId}
              onChange={(event) => setAiLookupId(event.target.value)}
            />
            <button onClick={lookupAi}>Lookup</button>
          </div>
          {aiDecision ? (
            <div className="ai-card">
              <div>
                <p className="muted">Model</p>
                <p className="value">{aiDecision.modelVersion}</p>
              </div>
              <div>
                <p className="muted">Risk Score</p>
                <p className="value">{aiDecision.riskScore}</p>
              </div>
              <div>
                <p className="muted">Reasons</p>
                <p className="value">{aiDecision.reasons.join(', ')}</p>
              </div>
            </div>
          ) : (
            <p className="muted">Enter a transaction ID to see an AI decision.</p>
          )}
        </section>
      </main>
    </div>
  )
}

type StatCardProps = {
  label: string
  value: number | string
  accent?: boolean
}

function StatCard({ label, value, accent }: StatCardProps) {
  return (
    <div className={`stat-card ${accent ? 'accent' : ''}`}>
      <p>{label}</p>
      <h3>{value}</h3>
    </div>
  )
}

type Column<T> = {
  key: keyof T
  label: string
}

type DataTableProps<T> = {
  rows: T[]
  columns: Column<T>[]
}

function DataTable<T extends Record<string, unknown>>({ rows, columns }: DataTableProps<T>) {
  return (
    <div className="table">
      <div className="table-head" style={{ gridTemplateColumns: `repeat(${columns.length}, minmax(0, 1fr))` }}>
        {columns.map((col) => (
          <span key={col.label}>{col.label}</span>
        ))}
      </div>
      {rows.length === 0 ? (
        <div className="table-row empty" style={{ gridTemplateColumns: `repeat(${columns.length}, minmax(0, 1fr))` }}>
          No data yet.
        </div>
      ) : (
        rows.slice(0, 8).map((row, index) => (
          <div
            className="table-row"
            key={index}
            style={{ gridTemplateColumns: `repeat(${columns.length}, minmax(0, 1fr))` }}
          >
            {columns.map((col) => (
              <span key={col.label} title={String(row[col.key] ?? '')}>
                {formatCell(row[col.key])}
              </span>
            ))}
          </div>
        ))
      )}
    </div>
  )
}

function formatCell(value: unknown): string | number {
  if (Array.isArray(value)) {
    return value.join(', ')
  }
  if (typeof value === 'number') {
    return value.toLocaleString()
  }
  if (typeof value === 'string') {
    return value.length > 26 ? `${value.slice(0, 26)}…` : value
  }
  if (typeof value === 'boolean') {
    return value ? 'true' : 'false'
  }
  if (value == null) {
    return '—'
  }
  return String(value)
}

export default App
