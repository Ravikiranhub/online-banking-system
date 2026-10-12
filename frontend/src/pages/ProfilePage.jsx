import { Save } from 'lucide-react';
import { useEffect, useState } from 'react';
import { api, apiError } from '../api/client.js';
import DataState from '../components/DataState.jsx';

export default function ProfilePage() {
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState({ type: '', text: '' });
  const [form, setForm] = useState({ firstName: '', lastName: '', phone: '', address: '' });

  useEffect(() => {
    api
      .get('/customer/profile')
      .then(({ data }) =>
        setForm({
          firstName: data.firstName || '',
          lastName: data.lastName || '',
          phone: data.phone || '',
          address: data.address || '',
        }),
      )
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
      await api.put('/customer/profile', form);
      setMessage({ type: 'success', text: 'Profile updated.' });
    } catch (error) {
      setMessage({ type: 'error', text: apiError(error) });
    }
  }

  return (
    <DataState loading={loading}>
      <section className="page-heading">
        <div>
          <span className="eyebrow">Customer information</span>
          <h2>Profile</h2>
        </div>
      </section>
      <section className="form-panel">
        {message.text ? <div className={`alert ${message.type}`}>{message.text}</div> : null}
        <form className="stack-form" onSubmit={submit}>
          <label>
            First name
            <input name="firstName" value={form.firstName} onChange={update} required maxLength={80} />
          </label>
          <label>
            Last name
            <input name="lastName" value={form.lastName} onChange={update} required maxLength={80} />
          </label>
          <label>
            Phone
            <input name="phone" value={form.phone} onChange={update} />
          </label>
          <label>
            Address
            <input name="address" value={form.address} onChange={update} maxLength={260} />
          </label>
          <button className="primary-button">
            <Save size={18} />
            Save profile
          </button>
        </form>
      </section>
    </DataState>
  );
}
