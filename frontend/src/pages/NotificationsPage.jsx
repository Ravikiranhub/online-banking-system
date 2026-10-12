import { Bell } from 'lucide-react';
import { useEffect, useState } from 'react';
import { api, apiError } from '../api/client.js';
import DataState from '../components/DataState.jsx';
import { formatDate } from '../utils/format.js';

export default function NotificationsPage() {
  const [state, setState] = useState({ loading: true, error: '', notifications: [] });

  useEffect(() => {
    api
      .get('/customer/notifications')
      .then(({ data }) => setState({ loading: false, error: '', notifications: data }))
      .catch((error) => setState({ loading: false, error: apiError(error), notifications: [] }));
  }, []);

  return (
    <DataState loading={state.loading} error={state.error}>
      <section className="page-heading">
        <div>
          <span className="eyebrow">Account messages</span>
          <h2>Notifications</h2>
        </div>
      </section>
      <DataState empty={!state.notifications.length} title="No notifications">
        <div className="notification-list">
          {state.notifications.map((notification) => (
            <article className="notification-item" key={notification.id}>
              <Bell size={20} />
              <div>
                <h3>{notification.title}</h3>
                <p>{notification.message}</p>
                <small>{formatDate(notification.createdAt)}</small>
              </div>
            </article>
          ))}
        </div>
      </DataState>
    </DataState>
  );
}
