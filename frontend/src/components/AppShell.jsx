import {
  Bell,
  CreditCard,
  FileText,
  History,
  Home,
  Landmark,
  LogOut,
  Menu,
  ShieldCheck,
  User,
  WalletCards,
} from 'lucide-react';
import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';

const links = [
  { to: '/dashboard', label: 'Dashboard', icon: Home },
  { to: '/accounts', label: 'My Accounts', icon: WalletCards },
  { to: '/transfer', label: 'Transfer', icon: Landmark },
  { to: '/deposit', label: 'Deposit', icon: CreditCard },
  { to: '/withdraw', label: 'Withdraw', icon: CreditCard },
  { to: '/transactions', label: 'History', icon: History },
  { to: '/statement', label: 'Statement', icon: FileText },
  { to: '/profile', label: 'Profile', icon: User },
  { to: '/notifications', label: 'Notifications', icon: Bell },
];

export default function AppShell() {
  const { user, logout, hasRole } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const visibleLinks = hasRole('ROLE_ADMIN')
    ? [...links, { to: '/admin', label: 'Admin', icon: ShieldCheck }]
    : links;

  function handleLogout() {
    logout();
    navigate('/');
  }

  return (
    <div className="app-frame">
      <aside className={`sidebar ${open ? 'sidebar-open' : ''}`}>
        <NavLink to="/dashboard" className="brand" onClick={() => setOpen(false)}>
          <span className="brand-mark">BS</span>
          <span>BankSphere</span>
        </NavLink>
        <nav>
          {visibleLinks.map(({ to, label, icon: Icon }) => (
            <NavLink key={to} to={to} className="nav-link" onClick={() => setOpen(false)}>
              <Icon size={19} />
              <span>{label}</span>
            </NavLink>
          ))}
        </nav>
        <button className="nav-link logout-link" onClick={handleLogout}>
          <LogOut size={19} />
          <span>Logout</span>
        </button>
      </aside>

      <div className="workspace">
        <header className="topbar">
          <button className="icon-button mobile-menu" onClick={() => setOpen((value) => !value)} aria-label="Menu">
            <Menu size={22} />
          </button>
          <div>
            <span className="eyebrow">Educational banking simulation</span>
            <h1>Welcome, {user?.firstName || 'Customer'}</h1>
          </div>
          <div className="topbar-actions">
            <NavLink className="icon-button" to="/notifications" aria-label="Notifications">
              <Bell size={21} />
            </NavLink>
            <button className="secondary-button" onClick={handleLogout}>
              Logout
            </button>
          </div>
        </header>
        <main className="content-area">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
