import { BadgeCheck, Ban, CreditCard } from 'lucide-react';
import { useEffect, useState } from 'react';
import { api, apiError } from '../api/client.js';
import DataState from '../components/DataState.jsx';
import { formatCurrency, formatDate } from '../utils/format.js';

export default function AccountsPage() {
  const [state, setState] = useState({ loading: true, error: '', accounts: [] });

  useEffect(() => {
    let mounted = true;
    api
      .get('/accounts')
      .then(({ data }) => mounted && setState({ loading: false, error: '', accounts: data }))
      .catch((error) => mounted && setState({ loading: false, error: apiError(error), accounts: [] }));
    return () => {
      mounted = false;
    };
  }, []);

  return (
    <DataState loading={state.loading} error={state.error}>
      <section className="page-heading">
        <div>
          <span className="eyebrow">Your accounts</span>
          <h2>My Accounts</h2>
        </div>
      </section>
      <DataState empty={!state.accounts.length} title="No accounts available">
        <div className="account-grid">
          {state.accounts.map((account) => (
            <article className="account-card" key={account.id}>
              <div className="account-card-head">
                <CreditCard size={24} />
                <span className={`status-chip ${account.status === 'ACTIVE' ? 'status-active' : 'status-danger'}`}>
                  {account.status === 'ACTIVE' ? <BadgeCheck size={15} /> : <Ban size={15} />}
                  {account.status}
                </span>
              </div>
              <h3>{account.accountType}</h3>
              <strong>{formatCurrency(account.balance)}</strong>
              <dl>
                <div>
                  <dt>Account number</dt>
                  <dd>{account.accountNumber}</dd>
                </div>
                <div>
                  <dt>Created</dt>
                  <dd>{formatDate(account.createdAt)}</dd>
                </div>
              </dl>
            </article>
          ))}
        </div>
      </DataState>
    </DataState>
  );
}
