import { BadgeCheck, Ban, FileClock, ShieldCheck, Users, WalletCards } from 'lucide-react';
import { useEffect, useState } from 'react';
import { api, apiError } from '../api/client.js';
import DataState from '../components/DataState.jsx';
import MetricTile from '../components/MetricTile.jsx';
import Pagination from '../components/Pagination.jsx';
import { formatCurrency, formatDate, typeLabel } from '../utils/format.js';

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [accounts, setAccounts] = useState(null);
  const [customers, setCustomers] = useState(null);
  const [transactions, setTransactions] = useState(null);
  const [logs, setLogs] = useState(null);
  const [pages, setPages] = useState({ accounts: 0, customers: 0, transactions: 0, logs: 0 });
  const [createAccount, setCreateAccount] = useState({ userId: '', accountType: 'SAVINGS', openingBalance: '0.00' });
  const [status, setStatus] = useState({ loading: true, error: '', message: '' });

  useEffect(() => {
    loadAll();
  }, [pages]);

  async function loadAll(nextMessage = '') {
    setStatus((current) => ({ ...current, loading: true, error: '' }));
    try {
      const [statsResponse, customersResponse, accountsResponse, transactionsResponse, logsResponse] = await Promise.all([
        api.get('/admin/stats'),
        api.get('/admin/customers', { params: { page: pages.customers, size: 6 } }),
        api.get('/admin/accounts', { params: { page: pages.accounts, size: 6 } }),
        api.get('/admin/transactions', { params: { page: pages.transactions, size: 6 } }),
        api.get('/admin/audit-logs', { params: { page: pages.logs, size: 6 } }),
      ]);
      setStats(statsResponse.data);
      setCustomers(customersResponse.data);
      setAccounts(accountsResponse.data);
      setTransactions(transactionsResponse.data);
      setLogs(logsResponse.data);
      setCreateAccount((current) => ({
        ...current,
        userId: current.userId || customersResponse.data.content?.[0]?.id || '',
      }));
      setStatus({ loading: false, error: '', message: nextMessage });
    } catch (error) {
      setStatus({ loading: false, error: apiError(error), message: '' });
    }
  }

  async function changeStatus(accountId, nextStatus) {
    setStatus((current) => ({ ...current, message: '' }));
    try {
      await api.patch(`/admin/accounts/${accountId}/status`, { status: nextStatus });
      await loadAll(`Account ${nextStatus.toLowerCase()}.`);
    } catch (error) {
      setStatus((current) => ({ ...current, error: apiError(error) }));
    }
  }

  function updateCreateAccount(event) {
    setCreateAccount((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  async function submitCreateAccount(event) {
    event.preventDefault();
    setStatus((current) => ({ ...current, message: '', error: '' }));
    try {
      await api.post('/admin/accounts', {
        userId: Number(createAccount.userId),
        accountType: createAccount.accountType,
        openingBalance: Number(createAccount.openingBalance),
      });
      await loadAll('Demo account created.');
    } catch (error) {
      setStatus((current) => ({ ...current, error: apiError(error) }));
    }
  }

  return (
    <DataState loading={status.loading && !stats} error={status.error && !stats ? status.error : ''}>
      <section className="page-heading">
        <div>
          <span className="eyebrow">Administrative console</span>
          <h2>Admin Dashboard</h2>
        </div>
      </section>
      {status.message ? <div className="alert success">{status.message}</div> : null}
      {status.error && stats ? <div className="alert error">{status.error}</div> : null}
      <section className="metric-grid">
        <MetricTile icon={Users} label="Customers" value={stats?.totalCustomers || 0} />
        <MetricTile icon={WalletCards} label="Accounts" value={stats?.totalAccounts || 0} tone="teal" />
        <MetricTile icon={BadgeCheck} label="Active" value={stats?.activeAccounts || 0} tone="green" />
        <MetricTile icon={Ban} label="Suspended" value={stats?.suspendedAccounts || 0} tone="amber" />
        <MetricTile icon={FileClock} label="Transactions" value={stats?.totalTransactions || 0} />
        <MetricTile icon={ShieldCheck} label="Volume" value={formatCurrency(stats?.simulatedTransactionVolume)} tone="teal" />
      </section>

      <AdminSection title="Registered Customers" page={customers} setPage={(page) => setPages((current) => ({ ...current, customers: page }))}>
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Phone</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {customers?.content?.map((customer) => (
              <tr key={customer.id}>
                <td>{customer.firstName} {customer.lastName}</td>
                <td>{customer.email}</td>
                <td>{customer.phone || 'N/A'}</td>
                <td>{customer.enabled ? 'Enabled' : 'Disabled'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </AdminSection>

      <AdminSection title="Demo Accounts" page={accounts} setPage={(page) => setPages((current) => ({ ...current, accounts: page }))}>
        <form className="admin-create-form" onSubmit={submitCreateAccount}>
          <label>
            Customer
            <select name="userId" value={createAccount.userId} onChange={updateCreateAccount} required>
              {customers?.content?.map((customer) => (
                <option key={customer.id} value={customer.id}>
                  {customer.firstName} {customer.lastName} - {customer.email}
                </option>
              ))}
            </select>
          </label>
          <label>
            Type
            <select name="accountType" value={createAccount.accountType} onChange={updateCreateAccount}>
              <option value="SAVINGS">Savings</option>
              <option value="CHECKING">Checking</option>
            </select>
          </label>
          <label>
            Opening balance
            <input name="openingBalance" type="number" min="0" step="0.01" value={createAccount.openingBalance} onChange={updateCreateAccount} required />
          </label>
          <button className="primary-button">Create account</button>
        </form>
        <table>
          <thead>
            <tr>
              <th>Account</th>
              <th>Type</th>
              <th>Balance</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {accounts?.content?.map((account) => (
              <tr key={account.id}>
                <td>{account.accountNumber}</td>
                <td>{account.accountType}</td>
                <td>{formatCurrency(account.balance)}</td>
                <td>{account.status}</td>
                <td>
                  <button className="secondary-button small-button" onClick={() => changeStatus(account.id, account.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE')}>
                    {account.status === 'ACTIVE' ? 'Suspend' : 'Activate'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </AdminSection>

      <AdminSection title="Transaction History" page={transactions} setPage={(page) => setPages((current) => ({ ...current, transactions: page }))}>
        <table>
          <thead>
            <tr>
              <th>Date</th>
              <th>Reference</th>
              <th>Type</th>
              <th>Amount</th>
              <th>Account</th>
            </tr>
          </thead>
          <tbody>
            {transactions?.content?.map((transaction) => (
              <tr key={transaction.id}>
                <td>{formatDate(transaction.occurredAt)}</td>
                <td>{transaction.transactionReference}</td>
                <td>{typeLabel(transaction.type)}</td>
                <td>{formatCurrency(transaction.amount)}</td>
                <td>{transaction.accountNumber}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </AdminSection>

      <AdminSection title="Audit Logs" page={logs} setPage={(page) => setPages((current) => ({ ...current, logs: page }))}>
        <table>
          <thead>
            <tr>
              <th>Date</th>
              <th>Actor</th>
              <th>Action</th>
              <th>Entity</th>
              <th>Details</th>
            </tr>
          </thead>
          <tbody>
            {logs?.content?.map((log) => (
              <tr key={log.id}>
                <td>{formatDate(log.createdAt)}</td>
                <td>{log.actorEmail}</td>
                <td>{log.action}</td>
                <td>{log.entityType} {log.entityId}</td>
                <td>{log.details}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </AdminSection>
    </DataState>
  );
}

function AdminSection({ title, page, setPage, children }) {
  return (
    <section className="section-panel admin-section">
      <div className="section-title">
        <h3>{title}</h3>
      </div>
      <DataState empty={!page?.content?.length} title={`No ${title.toLowerCase()} found`}>
        <div className="table-wrap">{children}</div>
        <Pagination page={page} onPageChange={setPage} />
      </DataState>
    </section>
  );
}
