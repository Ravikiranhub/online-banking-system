import { Send } from 'lucide-react';
import { useEffect, useState } from 'react';
import { api, apiError } from '../api/client.js';
import DataState from '../components/DataState.jsx';
import { formatCurrency, newIdempotencyKey } from '../utils/format.js';

export default function TransferPage() {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState({ type: '', text: '' });
  const [form, setForm] = useState({
    fromAccountId: '',
    recipientAccountNumber: '',
    amount: '',
    description: '',
  });

  useEffect(() => {
    api
      .get('/accounts')
      .then(({ data }) => {
        setAccounts(data);
        setForm((current) => ({ ...current, fromAccountId: data[0]?.id || '' }));
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
      const { data } = await api.post('/transfers', {
        ...form,
        fromAccountId: Number(form.fromAccountId),
        amount: Number(form.amount),
        idempotencyKey: newIdempotencyKey(),
      });
      setMessage({ type: 'success', text: `Transfer ${data.transferReference} completed.` });
      setForm((current) => ({ ...current, recipientAccountNumber: '', amount: '', description: '' }));
    } catch (error) {
      setMessage({ type: 'error', text: apiError(error) });
    }
  }

  return (
    <DataState loading={loading}>
      <section className="page-heading">
        <div>
          <span className="eyebrow">Atomic demo transfer</span>
          <h2>Transfer Money</h2>
        </div>
      </section>
      <section className="form-panel">
        {message.text ? <div className={`alert ${message.type}`}>{message.text}</div> : null}
        <form onSubmit={submit} className="stack-form">
          <label>
            From account
            <select name="fromAccountId" value={form.fromAccountId} onChange={update} required>
              {accounts.map((account) => (
                <option key={account.id} value={account.id} disabled={account.status !== 'ACTIVE'}>
                  {account.accountNumber} - {formatCurrency(account.balance)} - {account.status}
                </option>
              ))}
            </select>
          </label>
          <label>
            Recipient account number
            <input name="recipientAccountNumber" value={form.recipientAccountNumber} onChange={update} required maxLength={20} />
          </label>
          <label>
            Amount
            <input name="amount" type="number" step="0.01" min="0.01" value={form.amount} onChange={update} required />
          </label>
          <label>
            Description
            <input name="description" value={form.description} onChange={update} maxLength={260} />
          </label>
          <button className="primary-button">
            <Send size={18} />
            Confirm transfer
          </button>
        </form>
      </section>
    </DataState>
  );
}
