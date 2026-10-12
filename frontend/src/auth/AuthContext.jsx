import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { api, apiError } from '../api/client.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('banksphere_token'));
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('banksphere_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [initializing, setInitializing] = useState(Boolean(token));

  useEffect(() => {
    let mounted = true;
    async function loadProfile() {
      if (!token) {
        setInitializing(false);
        return;
      }
      try {
        const { data } = await api.get('/auth/me');
        if (mounted) {
          setUser(data);
          localStorage.setItem('banksphere_user', JSON.stringify(data));
        }
      } catch {
        logout();
      } finally {
        if (mounted) {
          setInitializing(false);
        }
      }
    }
    loadProfile();
    return () => {
      mounted = false;
    };
  }, [token]);

  function persistSession(authResponse) {
    localStorage.setItem('banksphere_token', authResponse.token);
    localStorage.setItem('banksphere_user', JSON.stringify(authResponse.user));
    setToken(authResponse.token);
    setUser(authResponse.user);
  }

  async function login(credentials) {
    try {
      const { data } = await api.post('/auth/login', credentials);
      persistSession(data);
      return { ok: true, user: data.user };
    } catch (error) {
      return { ok: false, message: apiError(error) };
    }
  }

  async function register(payload) {
    try {
      const { data } = await api.post('/auth/register', payload);
      persistSession(data);
      return { ok: true, user: data.user };
    } catch (error) {
      return { ok: false, message: apiError(error) };
    }
  }

  function logout() {
    localStorage.removeItem('banksphere_token');
    localStorage.removeItem('banksphere_user');
    setToken(null);
    setUser(null);
  }

  function hasRole(role) {
    return user?.roles?.includes(role);
  }

  const value = useMemo(
    () => ({ token, user, initializing, login, register, logout, hasRole }),
    [token, user, initializing],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
