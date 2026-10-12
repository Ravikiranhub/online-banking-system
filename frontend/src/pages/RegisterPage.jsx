import { UserPlus } from 'lucide-react';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';

export default function RegisterPage() {
  const navigate = useNavigate();
  const { register } = useAuth();
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    phone: '',
    address: '',
  });
  const [status, setStatus] = useState({ loading: false, error: '' });

  function update(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  async function submit(event) {
    event.preventDefault();
    setStatus({ loading: true, error: '' });
    const result = await register(form);
    if (!result.ok) {
      setStatus({ loading: false, error: result.message });
      return;
    }
    navigate('/dashboard');
  }

  return (
    <div className="auth-page">
      <Link to="/" className="brand">
        <span className="brand-mark">BS</span>
        <span>BankSphere</span>
      </Link>
      <section className="auth-card wide-auth-card">
        <span className="eyebrow">Open a demo account</span>
        <h1>Create your profile</h1>
        <p>Registration creates a simulated savings account. It never connects to real banks.</p>
        <form className="auth-form two-column-form" onSubmit={submit}>
          {status.error ? <div className="alert error form-span">{status.error}</div> : null}
          <label>
            First name
            <input name="firstName" value={form.firstName} onChange={update} required maxLength={80} />
          </label>
          <label>
            Last name
            <input name="lastName" value={form.lastName} onChange={update} required maxLength={80} />
          </label>
          <label className="form-span">
            Email
            <input name="email" type="email" value={form.email} onChange={update} required maxLength={160} />
          </label>
          <label>
            Password
            <input name="password" type="password" minLength={8} value={form.password} onChange={update} required />
          </label>
          <label>
            Phone
            <input name="phone" value={form.phone} onChange={update} placeholder="+1-555-0101" />
          </label>
          <label className="form-span">
            Address
            <input name="address" value={form.address} onChange={update} maxLength={260} />
          </label>
          <button className="primary-button full-width form-span" disabled={status.loading}>
            <UserPlus size={18} />
            {status.loading ? 'Creating account...' : 'Register'}
          </button>
          <p className="form-note form-span">
            Already registered? <Link to="/login">Login</Link>
          </p>
        </form>
      </section>
    </div>
  );
}
