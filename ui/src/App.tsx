import { useEffect, useMemo, useState } from 'react'
import './App.css'

type Page<T> = {
  content: T[]
  totalElements: number
  totalPages: number
}

type Transaction = {
  id: string
  accountId: string
  amount: number
  currency: string
  country: string
  merchant: string
  occurredAt: string
  createdAt: string
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
  const [scenarioTransaction, setScenarioTransaction] = useState<Transaction | null>(null)
  const [scenarioFlags, setScenarioFlags] = useState<Flag[]>([])
  const [scenarioCase, setScenarioCase] = useState<CaseItem | null>(null)
  const [scenarioNotifications, setScenarioNotifications] = useState<NotificationItem[]>([])
  const [scenarioAi, setScenarioAi] = useState<AiDecision | null>(null)
  const [scenarioStatus, setScenarioStatus] = useState<'idle' | 'submitting' | 'polling' | 'complete' | 'error'>(
    'idle'
  )
  const [scenarioError, setScenarioError] = useState<string | null>(null)
  const [aiLookupId, setAiLookupId] = useState('')
  const [aiDecision, setAiDecision] = useState<AiDecision | null>(null)
  const [formState, setFormState] = useState({
    accountId: '11111111-1111-1111-1111-111111111111',
    amount: '15000',
    currency: 'USD',
    country: 'IR',
    merchant: 'Test Merchant',
    occurredAt: new Date().toISOString().slice(0, 16),
  })
  const [autoRefresh, setAutoRefresh] = useState(false)
  const [refreshEvery, setRefreshEvery] = useState(15)
  const [flagQuery, setFlagQuery] = useState('')
  const [flagMinRisk, setFlagMinRisk] = useState(0)
  const [caseQuery, setCaseQuery] = useState('')
  const [caseStatusFilter, setCaseStatusFilter] = useState('ALL')
  const [notificationQuery, setNotificationQuery] = useState('')
  const [notificationStatusFilter, setNotificationStatusFilter] = useState('ALL')
  const [notificationChannelFilter, setNotificationChannelFilter] = useState('ALL')
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [drawerLoading, setDrawerLoading] = useState(false)
  const [drawerError, setDrawerError] = useState<string | null>(null)
  const [drawerTransactionId, setDrawerTransactionId] = useState<string | null>(null)
  const [drawerCase, setDrawerCase] = useState<CaseItem | null>(null)
  const [drawerFlags, setDrawerFlags] = useState<Flag[]>([])
  const [drawerNotifications, setDrawerNotifications] = useState<NotificationItem[]>([])
  const [drawerAi, setDrawerAi] = useState<AiDecision | null>(null)
  const [caseStatusDraft, setCaseStatusDraft] = useState<string>('OPEN')
  const [analystDraft, setAnalystDraft] = useState('')
  const [caseNotes, setCaseNotes] = useState<Record<string, string>>(() => {
    try {
      const stored = localStorage.getItem('ms_case_notes')
      return stored ? (JSON.parse(stored) as Record<string, string>) : {}
    } catch {
      return {}
    }
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const endpoints = useMemo(
    () => ({
      transaction: import.meta.env.VITE_TRANSACTION_SERVICE_URL ?? 'http://localhost:8082',
      ruleEngine: import.meta.env.VITE_RULE_ENGINE_URL ?? 'http://localhost:8083',
      cases: import.meta.env.VITE_CASE_SERVICE_URL ?? 'http://localhost:8084',
      notifications: import.meta.env.VITE_NOTIFICATION_SERVICE_URL ?? 'http://localhost:8085',
      reporting: import.meta.env.VITE_REPORTING_SERVICE_URL ?? 'http://localhost:8086',
      ai: import.meta.env.VITE_AI_SERVICE_URL ?? 'http://localhost:8087',
    }),
    []
  )

  const today = useMemo(() => new Date().toISOString().slice(0, 10), [])

  const filteredFlags = useMemo(() => {
    const data = flags?.content ?? []
    const query = flagQuery.trim().toLowerCase()
    return data.filter((flag) => {
      if (flagMinRisk && flag.riskScore < flagMinRisk) return false
      if (!query) return true
      return [
        flag.transactionId,
        flag.accountId,
        flag.country,
        flag.merchant,
        flag.reasons.join(' '),
        String(flag.amount),
      ]
        .join(' ')
        .toLowerCase()
        .includes(query)
    })
  }, [flags, flagQuery, flagMinRisk])

  const filteredCases = useMemo(() => {
    const data = cases?.content ?? []
    const query = caseQuery.trim().toLowerCase()
    return data.filter((caseItem) => {
      if (caseStatusFilter !== 'ALL' && caseItem.status !== caseStatusFilter) return false
      if (!query) return true
      return [
        caseItem.id,
        caseItem.transactionId,
        caseItem.accountId,
        caseItem.status,
        caseItem.reasons.join(' '),
      ]
        .join(' ')
        .toLowerCase()
        .includes(query)
    })
  }, [cases, caseQuery, caseStatusFilter])

  const filteredNotifications = useMemo(() => {
    const data = notifications?.content ?? []
    const query = notificationQuery.trim().toLowerCase()
    return data.filter((note) => {
      if (notificationStatusFilter !== 'ALL' && note.status !== notificationStatusFilter) return false
      if (notificationChannelFilter !== 'ALL' && note.channel !== notificationChannelFilter) return false
      if (!query) return true
      return [note.id, note.caseId, note.eventType, note.channel, note.status, note.recipient, note.payload]
        .join(' ')
        .toLowerCase()
        .includes(query)
    })
  }, [notifications, notificationQuery, notificationStatusFilter, notificationChannelFilter])

  const fetchJson = async <T,>(url: string): Promise<T | null> => {
    try {
      const res = await fetch(url)
      if (!res.ok) return null
      return (await res.json()) as T
    } catch {
      return null
    }
  }

  const postJson = async <T,>(url: string, body: unknown): Promise<T> => {
    const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })
    if (!res.ok) {
      const text = await res.text().catch(() => '')
      const suffix = text ? `: ${text}` : ''
      throw new Error(`HTTP ${res.status} ${res.statusText}${suffix}`)
    }
    return (await res.json()) as T
  }

  const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

  const poll = async <T,>(
    fn: () => Promise<T | null>,
    isReady: (value: T | null) => boolean,
    attempts = 8,
    waitMs = 1200
  ): Promise<T | null> => {
    for (let i = 0; i < attempts; i += 1) {
      const value = await fn()
      if (isReady(value)) {
        return value
      }
      await delay(waitMs)
    }
    return null
  }

  const patchJson = async <T,>(url: string, body: unknown): Promise<T> => {
    const res = await fetch(url, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })
    if (!res.ok) {
      const text = await res.text().catch(() => '')
      const suffix = text ? `: ${text}` : ''
      throw new Error(`HTTP ${res.status} ${res.statusText}${suffix}`)
    }
    return (await res.json()) as T
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

  useEffect(() => {
    if (!autoRefresh) return
    const handle = window.setInterval(() => {
      refreshAll()
    }, Math.max(5, refreshEvery) * 1000)
    return () => window.clearInterval(handle)
  }, [autoRefresh, refreshEvery])

  useEffect(() => {
    if (!drawerCase) return
    setCaseStatusDraft(drawerCase.status)
    setAnalystDraft(drawerCase.assignedAnalystId ?? '')
  }, [drawerCase])

  useEffect(() => {
    localStorage.setItem('ms_case_notes', JSON.stringify(caseNotes))
  }, [caseNotes])

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

  const hydrateDrawer = async (transactionId: string, preferredCaseId?: string | null) => {
    setDrawerLoading(true)
    setDrawerError(null)
    setDrawerTransactionId(transactionId)
    try {
      const [aiResult, flagsPage] = await Promise.all([
        fetchJson<AiDecision>(`${endpoints.ai}/api/ai/decisions?transactionId=${transactionId}`),
        fetchJson<Page<Flag>>(`${endpoints.ruleEngine}/api/flags`),
      ])

      const matchedFlags = flagsPage?.content.filter((flag) => flag.transactionId === transactionId) ?? []

      let caseItem: CaseItem | null = null
      if (preferredCaseId) {
        caseItem = await fetchJson<CaseItem>(`${endpoints.cases}/api/cases/${preferredCaseId}`)
      } else {
        const casePage = await fetchJson<Page<CaseItem>>(
          `${endpoints.cases}/api/cases?transactionId=${transactionId}`
        )
        caseItem = casePage?.content?.[0] ?? null
      }

      let notifications: NotificationItem[] = []
      if (caseItem) {
        const notificationPage = await fetchJson<Page<NotificationItem>>(`${endpoints.notifications}/api/notifications`)
        notifications = notificationPage?.content.filter((note) => note.caseId === caseItem.id) ?? []
      }

      setDrawerAi(aiResult)
      setDrawerFlags(matchedFlags)
      setDrawerCase(caseItem)
      setDrawerNotifications(notifications)
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to load detail view.'
      setDrawerError(message)
    } finally {
      setDrawerLoading(false)
    }
  }

  const openFromFlag = (flag: Flag) => {
    setDrawerOpen(true)
    setDrawerFlags([flag])
    setDrawerCase(null)
    setDrawerNotifications([])
    setDrawerAi(null)
    hydrateDrawer(flag.transactionId)
  }

  const openFromCase = (caseItem: CaseItem) => {
    setDrawerOpen(true)
    setDrawerCase(caseItem)
    setDrawerFlags([])
    setDrawerNotifications([])
    setDrawerAi(null)
    hydrateDrawer(caseItem.transactionId, caseItem.id)
  }

  const openFromNotification = async (note: NotificationItem) => {
    setDrawerOpen(true)
    setDrawerError(null)
    setDrawerCase(null)
    setDrawerFlags([])
    setDrawerNotifications([note])
    setDrawerAi(null)
    const caseItem = await fetchJson<CaseItem>(`${endpoints.cases}/api/cases/${note.caseId}`)
    if (!caseItem) {
      setDrawerError('Case not found for this notification.')
      return
    }
    hydrateDrawer(caseItem.transactionId, caseItem.id)
  }

  const closeDrawer = () => {
    setDrawerOpen(false)
    setDrawerError(null)
  }

  const runScenario = async () => {
    setScenarioStatus('submitting')
    setScenarioError(null)
    setScenarioTransaction(null)
    setScenarioFlags([])
    setScenarioCase(null)
    setScenarioNotifications([])
    setScenarioAi(null)

    const occurredAtIso = formState.occurredAt
      ? new Date(formState.occurredAt).toISOString()
      : new Date().toISOString()
    const payload = {
      accountId: formState.accountId.trim(),
      amount: Number(formState.amount),
      currency: formState.currency.trim(),
      country: formState.country.trim(),
      merchant: formState.merchant.trim(),
      occurredAt: occurredAtIso,
    }

    let created: Transaction
    try {
      created = await postJson<Transaction>(`${endpoints.transaction}/api/transactions`, payload)
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unknown error'
      setScenarioStatus('error')
      setScenarioError(
        `Transaction service failed (${endpoints.transaction}/api/transactions). ${message}`
      )
      return
    }

    setScenarioTransaction(created)
    setScenarioStatus('polling')

    const aiResult = await poll(
      () => fetchJson<AiDecision>(`${endpoints.ai}/api/ai/decisions?transactionId=${created.id}`),
      (value) => Boolean(value),
      10
    )
    setScenarioAi(aiResult)

    const matchedFlags =
      (await poll(
        async () => {
          const data = await fetchJson<Page<Flag>>(`${endpoints.ruleEngine}/api/flags`)
          const matches = data?.content.filter((flag) => flag.transactionId === created.id) ?? []
          return matches.length ? matches : null
        },
        (value) => Array.isArray(value) && value.length > 0,
        10
      )) ?? []
    setScenarioFlags(matchedFlags)

    const caseItem = await poll(
      async () => {
        const data = await fetchJson<Page<CaseItem>>(
          `${endpoints.cases}/api/cases?transactionId=${created.id}`
        )
        return data?.content?.[0] ?? null
      },
      (value) => Boolean(value),
      10
    )
    setScenarioCase(caseItem)

    if (caseItem) {
      const notificationMatches =
        (await poll(
          async () => {
            const data = await fetchJson<Page<NotificationItem>>(`${endpoints.notifications}/api/notifications`)
            const matches = data?.content.filter((note) => note.caseId === caseItem.id) ?? []
            return matches.length ? matches : null
          },
          (value) => Array.isArray(value) && value.length > 0,
          10
        )) ?? []
      setScenarioNotifications(notificationMatches)
    }

    await refreshAll()
    setScenarioStatus('complete')
  }

  const updateCaseStatus = async () => {
    if (!drawerCase) return
    try {
      const updated = await patchJson<CaseItem>(
        `${endpoints.cases}/api/cases/${drawerCase.id}/status`,
        { status: caseStatusDraft }
      )
      setDrawerCase(updated)
      await refreshAll()
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to update case status.'
      setDrawerError(message)
    }
  }

  const assignCase = async () => {
    if (!drawerCase) return
    try {
      const updated = await patchJson<CaseItem>(
        `${endpoints.cases}/api/cases/${drawerCase.id}/assign`,
        { analystId: analystDraft || null }
      )
      setDrawerCase(updated)
      await refreshAll()
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to assign analyst.'
      setDrawerError(message)
    }
  }

  const drawerSnapshot = drawerFlags[0] ?? drawerAi ?? null
  const drawerAccountId = drawerCase?.accountId ?? drawerSnapshot?.accountId ?? '—'
  const drawerAmount = drawerSnapshot?.amount
  const drawerCountry = drawerSnapshot?.country
  const drawerMerchant = drawerSnapshot?.merchant
  const drawerOccurredAt = drawerSnapshot?.occurredAt

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
          <a href="#workflow">Workflow</a>
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
            <label className="toggle">
              <input
                type="checkbox"
                checked={autoRefresh}
                onChange={(event) => setAutoRefresh(event.target.checked)}
              />
              <span>Auto refresh</span>
            </label>
            <select
              className="select"
              value={refreshEvery}
              onChange={(event) => setRefreshEvery(Number(event.target.value))}
              disabled={!autoRefresh}
            >
              <option value={5}>Every 5s</option>
              <option value={10}>Every 10s</option>
              <option value={15}>Every 15s</option>
              <option value={30}>Every 30s</option>
            </select>
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

        <section id="workflow" className="section">
          <div className="section-head">
            <h2>Pipeline Simulator</h2>
            <span className="pill">Create · Score · Flag · Case · Notify</span>
          </div>
          <div className="workflow-grid">
            <div className="workflow-panel">
              <h3>Submit a transaction</h3>
              <div className="workflow-form">
                <label>
                  Account ID
                  <input
                    value={formState.accountId}
                    onChange={(event) => setFormState({ ...formState, accountId: event.target.value })}
                  />
                </label>
                <label>
                  Amount
                  <input
                    type="number"
                    value={formState.amount}
                    onChange={(event) => setFormState({ ...formState, amount: event.target.value })}
                  />
                </label>
                <label>
                  Currency
                  <input
                    value={formState.currency}
                    onChange={(event) => setFormState({ ...formState, currency: event.target.value })}
                  />
                </label>
                <label>
                  Country
                  <input
                    value={formState.country}
                    onChange={(event) => setFormState({ ...formState, country: event.target.value })}
                  />
                </label>
                <label>
                  Merchant
                  <input
                    value={formState.merchant}
                    onChange={(event) => setFormState({ ...formState, merchant: event.target.value })}
                  />
                </label>
                <label>
                  Occurred At
                  <input
                    type="datetime-local"
                    value={formState.occurredAt}
                    onChange={(event) => setFormState({ ...formState, occurredAt: event.target.value })}
                  />
                </label>
              </div>
              <div className="workflow-actions">
                <button onClick={runScenario} disabled={scenarioStatus === 'submitting' || scenarioStatus === 'polling'}>
                  {scenarioStatus === 'submitting'
                    ? 'Submitting…'
                    : scenarioStatus === 'polling'
                      ? 'Tracing…'
                      : 'Run Pipeline'}
                </button>
                <button
                  className="ghost"
                  onClick={() => {
                    setScenarioStatus('idle')
                    setScenarioError(null)
                    setScenarioTransaction(null)
                    setScenarioFlags([])
                    setScenarioCase(null)
                    setScenarioNotifications([])
                    setScenarioAi(null)
                  }}
                >
                  Reset
                </button>
              </div>
              {scenarioError && <div className="alert">{scenarioError}</div>}
            </div>
            <div className="workflow-panel">
              <h3>Live trace</h3>
              <ul className="timeline">
                <li className={scenarioTransaction ? 'done' : scenarioStatus !== 'idle' ? 'pending' : ''}>
                  <span className="dot" />
                  <div>
                    <p>Transaction created</p>
                    <p className="muted">{scenarioTransaction?.id ?? 'Waiting for submission'}</p>
                  </div>
                </li>
                <li className={scenarioAi ? 'done' : scenarioStatus === 'polling' ? 'pending' : ''}>
                  <span className="dot" />
                  <div>
                    <p>AI scored transaction</p>
                    <p className="muted">
                      {scenarioAi ? `${scenarioAi.riskScore} · ${scenarioAi.modelVersion}` : 'Waiting for AI'}
                    </p>
                  </div>
                </li>
                <li className={scenarioFlags.length ? 'done' : scenarioStatus === 'polling' ? 'pending' : ''}>
                  <span className="dot" />
                  <div>
                    <p>Rule engine flagged</p>
                    <p className="muted">
                      {scenarioFlags.length ? `${scenarioFlags.length} flags` : 'Waiting for flags'}
                    </p>
                  </div>
                </li>
                <li className={scenarioCase ? 'done' : scenarioStatus === 'polling' ? 'pending' : ''}>
                  <span className="dot" />
                  <div>
                    <p>Case opened</p>
                    <p className="muted">{scenarioCase ? scenarioCase.status : 'Waiting for case'}</p>
                  </div>
                </li>
                <li className={scenarioNotifications.length ? 'done' : scenarioStatus === 'polling' ? 'pending' : ''}>
                  <span className="dot" />
                  <div>
                    <p>Notification sent</p>
                    <p className="muted">
                      {scenarioNotifications.length ? `${scenarioNotifications.length} sent` : 'Waiting for notify'}
                    </p>
                  </div>
                </li>
              </ul>
              <div className="trace-meta">
                <div>
                  <p className="muted">Flag reasons</p>
                  <p className="value">
                    {scenarioFlags.length ? scenarioFlags[0].reasons.join(', ') : '—'}
                  </p>
                </div>
                <div>
                  <p className="muted">Notification recipient</p>
                  <p className="value">
                    {scenarioNotifications.length ? scenarioNotifications[0].recipient : '—'}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section id="flags" className="section">
          <div className="section-head">
            <h2>Flags</h2>
            <span className="pill">
              {filteredFlags.length} shown · {flags?.totalElements ?? 0} total
            </span>
          </div>
          <div className="filters">
            <input
              placeholder="Search flags..."
              value={flagQuery}
              onChange={(event) => setFlagQuery(event.target.value)}
            />
            <label className="range">
              Min risk
              <input
                type="range"
                min={0}
                max={1}
                step={0.05}
                value={flagMinRisk}
                onChange={(event) => setFlagMinRisk(Number(event.target.value))}
              />
              <span>{flagMinRisk.toFixed(2)}</span>
            </label>
          </div>
          <DataTable
            rows={filteredFlags}
            columns={[
              { key: 'transactionId', label: 'Transaction' },
              { key: 'accountId', label: 'Account' },
              { key: 'amount', label: 'Amount' },
              { key: 'riskScore', label: 'Risk' },
              { key: 'reasons', label: 'Reasons' },
            ]}
            onRowClick={openFromFlag}
          />
        </section>

        <section id="cases" className="section">
          <div className="section-head">
            <h2>Cases</h2>
            <span className="pill">
              {filteredCases.length} shown · {cases?.totalElements ?? 0} total
            </span>
          </div>
          <div className="filters">
            <input
              placeholder="Search cases..."
              value={caseQuery}
              onChange={(event) => setCaseQuery(event.target.value)}
            />
            <select
              className="select"
              value={caseStatusFilter}
              onChange={(event) => setCaseStatusFilter(event.target.value)}
            >
              <option value="ALL">All statuses</option>
              <option value="OPEN">OPEN</option>
              <option value="UNDER_REVIEW">UNDER_REVIEW</option>
              <option value="APPROVED">APPROVED</option>
              <option value="REJECTED">REJECTED</option>
            </select>
          </div>
          <DataTable
            rows={filteredCases}
            columns={[
              { key: 'id', label: 'Case' },
              { key: 'status', label: 'Status' },
              { key: 'accountId', label: 'Account' },
              { key: 'riskScore', label: 'Risk' },
              { key: 'reasons', label: 'Reasons' },
            ]}
            onRowClick={openFromCase}
          />
        </section>

        <section id="notifications" className="section">
          <div className="section-head">
            <h2>Notifications</h2>
            <span className="pill">
              {filteredNotifications.length} shown · {notifications?.totalElements ?? 0} total
            </span>
          </div>
          <div className="filters">
            <input
              placeholder="Search notifications..."
              value={notificationQuery}
              onChange={(event) => setNotificationQuery(event.target.value)}
            />
            <select
              className="select"
              value={notificationStatusFilter}
              onChange={(event) => setNotificationStatusFilter(event.target.value)}
            >
              <option value="ALL">All status</option>
              <option value="PENDING">PENDING</option>
              <option value="SENT">SENT</option>
              <option value="FAILED">FAILED</option>
            </select>
            <select
              className="select"
              value={notificationChannelFilter}
              onChange={(event) => setNotificationChannelFilter(event.target.value)}
            >
              <option value="ALL">All channels</option>
              <option value="EMAIL">EMAIL</option>
              <option value="SMS">SMS</option>
            </select>
          </div>
          <DataTable
            rows={filteredNotifications}
            columns={[
              { key: 'eventType', label: 'Event' },
              { key: 'channel', label: 'Channel' },
              { key: 'status', label: 'Status' },
              { key: 'recipient', label: 'Recipient' },
            ]}
            onRowClick={openFromNotification}
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

      {drawerOpen && (
        <div className="drawer-overlay" onClick={closeDrawer}>
          <aside className="drawer" onClick={(event) => event.stopPropagation()}>
            <div className="drawer-head">
              <div>
                <h2>Case & Transaction Detail</h2>
                <p className="muted">{drawerTransactionId ?? '—'}</p>
              </div>
              <button className="ghost" onClick={closeDrawer}>
                Close
              </button>
            </div>

            {drawerLoading ? (
              <p className="muted">Loading detail view…</p>
            ) : drawerError ? (
              <div className="alert">{drawerError}</div>
            ) : (
              <div className="drawer-grid">
                <div className="drawer-card">
                  <h3>Transaction snapshot</h3>
                  <div className="drawer-kv">
                    <span>Account</span>
                    <span>{drawerAccountId}</span>
                  </div>
                  <div className="drawer-kv">
                    <span>Amount</span>
                    <span>{drawerAmount ? drawerAmount.toLocaleString() : '—'}</span>
                  </div>
                  <div className="drawer-kv">
                    <span>Country</span>
                    <span>{drawerCountry ?? '—'}</span>
                  </div>
                  <div className="drawer-kv">
                    <span>Merchant</span>
                    <span>{drawerMerchant ?? '—'}</span>
                  </div>
                  <div className="drawer-kv">
                    <span>Occurred</span>
                    <span>{drawerOccurredAt ?? '—'}</span>
                  </div>
                </div>

                <div className="drawer-card">
                  <h3>AI decision</h3>
                  {drawerAi ? (
                    <>
                      <div className="drawer-kv">
                        <span>Risk score</span>
                        <span>{drawerAi.riskScore}</span>
                      </div>
                      <div className="drawer-kv">
                        <span>Model</span>
                        <span>{drawerAi.modelVersion}</span>
                      </div>
                      <p className="muted">{drawerAi.reasons.join(', ')}</p>
                    </>
                  ) : (
                    <p className="muted">No AI decision found.</p>
                  )}
                </div>

                <div className="drawer-card">
                  <h3>Rule flags</h3>
                  {drawerFlags.length ? (
                    <ul className="chip-list">
                      {drawerFlags.map((flag) => (
                        <li key={flag.id}>{flag.reasons.join(', ')}</li>
                      ))}
                    </ul>
                  ) : (
                    <p className="muted">No flags found.</p>
                  )}
                </div>

                <div className="drawer-card">
                  <h3>Case actions</h3>
                  {drawerCase ? (
                    <>
                      <div className="drawer-kv">
                        <span>Status</span>
                        <span>{drawerCase.status}</span>
                      </div>
                      <div className="drawer-kv">
                        <span>Assigned analyst</span>
                        <span>{drawerCase.assignedAnalystId ?? 'Unassigned'}</span>
                      </div>
                      <div className="drawer-actions">
                        <select
                          className="select"
                          value={caseStatusDraft}
                          onChange={(event) => setCaseStatusDraft(event.target.value)}
                        >
                          <option value="OPEN">OPEN</option>
                          <option value="UNDER_REVIEW">UNDER_REVIEW</option>
                          <option value="APPROVED">APPROVED</option>
                          <option value="REJECTED">REJECTED</option>
                        </select>
                        <button onClick={updateCaseStatus}>Update status</button>
                      </div>
                      <div className="drawer-actions">
                        <input
                          placeholder="Analyst UUID"
                          value={analystDraft}
                          onChange={(event) => setAnalystDraft(event.target.value)}
                        />
                        <button onClick={assignCase}>Assign</button>
                      </div>
                      <label className="notes">
                        Notes (local only)
                        <textarea
                          value={caseNotes[drawerCase.id] ?? ''}
                          onChange={(event) =>
                            setCaseNotes({ ...caseNotes, [drawerCase.id]: event.target.value })
                          }
                        />
                      </label>
                    </>
                  ) : (
                    <p className="muted">No case linked yet.</p>
                  )}
                </div>

                <div className="drawer-card">
                  <h3>Notifications</h3>
                  {drawerNotifications.length ? (
                    <ul className="notification-list">
                      {drawerNotifications.map((note) => (
                        <li key={note.id}>
                          <strong>{note.channel}</strong> · {note.status}
                          <span>{note.recipient}</span>
                        </li>
                      ))}
                    </ul>
                  ) : (
                    <p className="muted">No notifications found.</p>
                  )}
                </div>
              </div>
            )}
          </aside>
        </div>
      )}
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
  onRowClick?: (row: T) => void
}

function DataTable<T extends Record<string, unknown>>({ rows, columns, onRowClick }: DataTableProps<T>) {
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
            className={`table-row ${onRowClick ? 'clickable' : ''}`}
            key={(row as { id?: string }).id ?? index}
            style={{ gridTemplateColumns: `repeat(${columns.length}, minmax(0, 1fr))` }}
            onClick={() => onRowClick?.(row)}
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
