import { ArrowDownLeft, ArrowUpRight, Landmark, WalletCards } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, apiError } from '../api/client.js';
import DataState from '../components/DataState.jsx';
import MetricTile from '../components/MetricTile.jsx';
import { formatCurrency, formatDate, typeLabel } from '../utils/format.js';

export default function DashboardPage() {
  const [state, setState] = useState({ loading: true, error: '', data: null });

  useEffect(() => {
    let mounted = true;
    api
      .get('/customer/dashboard')
      .then(({ data }) => mounted && setState({ loading: false, error: '', data }))
      .catch((error) => mounted && setState({ loading: false, error: apiError(error), data: null }));
    return () => {
      mounted = false;
    };
  }, []);

  const data = state.data;

  return (
    <DataState loading={state.loading} error={state.error}>
      <section className="page-heading">
        <div>
          <span className="eyebrow">Live backend data</span>
          <h2>Dashboard</h2>
        </div>
        <Link to="/transfer" className="primary-button">
          Transfer money
        </Link>
      </section>

      <section className="metric-grid">
        <MetricTile icon={WalletCards} label="Total balance" value={formatCurrency(data?.totalBalance)} tone="navy" />
        <MetricTile icon={Landmark} label="Primary account" value={data?.primaryAccountNumber || 'No account'} tone="teal" />
        <MetricTile icon={ArrowDownLeft} label="Income" value={formatCurrency(data?.income)} tone="green" />
        <MetricTile icon={ArrowUpRight} label="Expenses" value={formatCurrency(data?.expenses)} tone="amber" />
      </section>

      <section className="section-panel">
        <div className="section-title">
          <h3>Recent transactions</h3>
          <Link to="/transactions" className="text-button">
            View all
          </Link>
        </div>
        <DataState empty={!data?.recentTransactions?.length} title="No transactions yet">
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Reference</th>
                  <th>Type</th>
                  <th>Amount</th>
                  <th>Date</th>
                </tr>
              </thead>
              <tbody>
                {data?.recentTransactions?.map((transaction) => (
                  <tr key={transaction.id || transaction.transactionReference}>
                    <td>{transaction.transactionReference}</td>
                    <td><span className={`status-chip ${transaction.type?.includes('IN') || transaction.type === 'DEPOSIT' ? 'status-active' : 'status-muted'}`}>{typeLabel(transaction.type)}</span></td>
                    <td>{formatCurrency(transaction.amount)}</td>
                    <td>{formatDate(transaction.occurredAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </DataState>
      </section>
    </DataState>
  );
}
