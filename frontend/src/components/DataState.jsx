import { AlertCircle, Loader2 } from 'lucide-react';

export default function DataState({ loading, error, empty, title, children, action }) {
  if (loading) {
    return (
      <div className="state-box">
        <Loader2 className="spin" size={24} />
        <strong>{title || 'Loading'}</strong>
      </div>
    );
  }

  if (error) {
    return (
      <div className="state-box state-error">
        <AlertCircle size={24} />
        <strong>{title || 'Could not load data'}</strong>
        <span>{error}</span>
        {action}
      </div>
    );
  }

  if (empty) {
    return (
      <div className="state-box">
        <strong>{title || 'Nothing here yet'}</strong>
        {children}
        {action}
      </div>
    );
  }

  return children;
}
