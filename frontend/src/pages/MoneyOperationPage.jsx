import { ArrowDownLeft, ArrowUpRight } from 'lucide-react';
import { useEffect, useState } from 'react';
import { api, apiError } from '../api/client.js';
import DataState from '../components/DataState.jsx';
import { formatCurrency } from '../utils/format.js';

export default function MoneyOperationPage({ mode }) {
  const isDeposit = mode === 'deposit';
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState({ type: '', text: '' });
  const [form, setForm] = useState({ accountId: '', amount: '', description: '' });

  useEffect(() => {
    api
      .get('/accounts')
      .then(({ data }) => {
        setAccounts(data);
        setForm((current) => ({ ...current, accountId: data[0]?.id || '' }));
      })
      .catch((error) => setMessage({ type: 'error', text: apiError(error) }))
      .finally(() => setLoading(false));
  }, []);

  function update(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  async function submit(event) {
    event.preventDefault();
    setMessage({ type: '', text: '' });
    try {
      const { data } = await api.post(`/transactions/${isDeposit ? 'deposit' : 'withdraw'}`, {
        ...form,
        accountId: Number(form.accountId),
        amount: Number(form.amount),
      });
      setMessage({
        type: 'success',
        text: `${isDeposit ? 'Deposit' : 'Withdrawal'} posted. New balance: ${formatCurrency(data.balanceAfter)}.`,
      });
      setForm((current) => ({ ...current, amount: '', description: '' }));
    } catch (error) {
      setMessage({ type: 'error', text: apiError(error) });
    }
  }

  return (
    <DataState loading={loading}>
      <section className="page-heading">
        <div>
          <span className="eyebrow">Simulated transaction</span>
          <h2>{isDeposit ? 'Deposit Money' : 'Withdraw Money'}</h2>
        </div>
      </section>
      <section className="form-panel">
        {message.text ? <div className={`alert ${message.type}`}>{message.text}</div> : null}
        <form className="stack-form" onSubmit={submit}>
          <label>
            Account
            <select name="accountId" value={form.accountId} onChange={update} required>
              {accounts.map((account) => (
                <option key={account.id} value={account.id} disabled={account.status !== 'ACTIVE'}>
                  {account.accountNumber} - {formatCurrency(account.balance)} - {account.status}
                </option>
              ))}
            </select>
          </label>
          <label>
            Amount
            <input name="amount" type="number" min="0.01" step="0.01" value={form.amount} onChange={update} required />
          </label>
          <label>
            Description
            <input name="description" value={form.description} onChange={update} maxLength={260} />
          </label>
          <button className="primary-button">
            {isDeposit ? <ArrowDownLeft size={18} /> : <ArrowUpRight size={18} />}
            {isDeposit ? 'Post deposit' : 'Post withdrawal'}
          </button>
        </form>
      </section>
    </DataState>
  );
}
