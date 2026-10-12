import { LogIn } from 'lucide-react';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';

export default function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [form, setForm] = useState({ email: 'demo.customer@banksphere.test', password: 'Customer@12345' });
  const [status, setStatus] = useState({ loading: false, error: '' });

  function update(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  async function submit(event) {
    event.preventDefault();
    setStatus({ loading: true, error: '' });
    const result = await login(form);
    if (!result.ok) {
      setStatus({ loading: false, error: result.message });
      return;
    }
    navigate(result.user?.roles?.includes('ROLE_ADMIN') ? '/admin' : '/dashboard');
  }

  return (
    <AuthPage title="Welcome back" subtitle="Sign in to your BankSphere demo workspace.">
      <form className="auth-form" onSubmit={submit}>
        {status.error ? <div className="alert error">{status.error}</div> : null}
        <label>
          Email
          <input name="email" type="email" value={form.email} onChange={update} required />
        </label>
        <label>
          Password
          <input name="password" type="password" value={form.password} onChange={update} required />
        </label>
        <button className="primary-button full-width" disabled={status.loading}>
          <LogIn size={18} />
          {status.loading ? 'Signing in...' : 'Login'}
        </button>
        <p className="form-note">
          Admin demo: <button type="button" className="text-button" onClick={() => setForm({ email: 'admin@banksphere.test', password: 'Admin@12345' })}>admin@banksphere.test</button>
        </p>
        <p className="form-note">
          New here? <Link to="/register">Create a demo account</Link>
        </p>
      </form>
    </AuthPage>
  );
}

function AuthPage({ title, subtitle, children }) {
  return (
    <div className="auth-page">
      <Link to="/" className="brand">
        <span className="brand-mark">BS</span>
        <span>BankSphere</span>
      </Link>
      <section className="auth-card">
        <span className="eyebrow">Educational simulation</span>
        <h1>{title}</h1>
        <p>{subtitle}</p>
        {children}
      </section>
    </div>
  );
}
