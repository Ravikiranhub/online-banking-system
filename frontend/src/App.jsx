import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './auth/AuthContext.jsx';
import AppShell from './components/AppShell.jsx';
import DataState from './components/DataState.jsx';
import AccountsPage from './pages/AccountsPage.jsx';
import AdminDashboard from './pages/AdminDashboard.jsx';
import DashboardPage from './pages/DashboardPage.jsx';
import HomePage from './pages/HomePage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import MoneyOperationPage from './pages/MoneyOperationPage.jsx';
import NotificationsPage from './pages/NotificationsPage.jsx';
import ProfilePage from './pages/ProfilePage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import StatementPage from './pages/StatementPage.jsx';
import TransactionHistoryPage from './pages/TransactionHistoryPage.jsx';
import TransferPage from './pages/TransferPage.jsx';

function RequireAuth({ children }) {
  const { token, initializing } = useAuth();
  if (initializing) {
    return <DataState loading title="Preparing secure session" />;
  }
  return token ? children : <Navigate to="/login" replace />;
}

function RequireAdmin({ children }) {
  const { hasRole } = useAuth();
  return hasRole('ROLE_ADMIN') ? children : <Navigate to="/dashboard" replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route
        element={
          <RequireAuth>
            <AppShell />
          </RequireAuth>
        }
      >
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/accounts" element={<AccountsPage />} />
        <Route path="/transfer" element={<TransferPage />} />
        <Route path="/deposit" element={<MoneyOperationPage mode="deposit" />} />
        <Route path="/withdraw" element={<MoneyOperationPage mode="withdraw" />} />
        <Route path="/transactions" element={<TransactionHistoryPage />} />
        <Route path="/statement" element={<StatementPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/notifications" element={<NotificationsPage />} />
        <Route
          path="/admin"
          element={
            <RequireAdmin>
              <AdminDashboard />
            </RequireAdmin>
          }
        />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
