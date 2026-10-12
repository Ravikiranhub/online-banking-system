import { ArrowRight, BadgeDollarSign, LockKeyhole, ShieldCheck, Smartphone } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function HomePage() {
  return (
    <div className="public-page">
      <header className="public-nav">
        <Link to="/" className="brand">
          <span className="brand-mark">BS</span>
          <span>BankSphere</span>
        </Link>
        <div className="public-actions">
          <Link to="/login" className="secondary-button">
            Login
          </Link>
          <Link to="/register" className="primary-button">
            Register
          </Link>
        </div>
      </header>

      <section className="hero-section">
        <div className="hero-copy">
          <span className="eyebrow">Demo money only</span>
          <h1>BankSphere</h1>
          <p>
            A modern full stack online banking simulation with secure sign-in, account management,
            transfers, statements, and an admin console.
          </p>
          <div className="hero-actions">
            <Link to="/register" className="primary-button">
              Open demo account <ArrowRight size={18} />
            </Link>
            <Link to="/login" className="secondary-button">
              Use demo login
            </Link>
          </div>
        </div>
        <div className="hero-panel" aria-label="BankSphere balance preview">
          <div className="bank-card-preview">
            <span>BankSphere</span>
            <strong>Demo Savings</strong>
            <small>7810 •••• •••• 0001</small>
          </div>
          <div className="preview-row">
            <span>Total balance</span>
            <strong>$8,450.75</strong>
          </div>
          <div className="preview-row">
            <span>Recent transfer</span>
            <strong>$125.00</strong>
          </div>
        </div>
      </section>

      <section className="feature-band">
        <Feature icon={LockKeyhole} title="JWT security" text="Role-based customer and admin access." />
        <Feature icon={BadgeDollarSign} title="Atomic transfers" text="Server-side debit and credit logic." />
        <Feature icon={Smartphone} title="Responsive UI" text="Works across desktop, tablet, and mobile." />
        <Feature icon={ShieldCheck} title="Admin oversight" text="Manage accounts, customers, and audit logs." />
      </section>
    </div>
  );
}

function Feature({ icon: Icon, title, text }) {
  return (
    <article className="feature-card">
      <Icon size={24} />
      <h2>{title}</h2>
      <p>{text}</p>
    </article>
  );
}
