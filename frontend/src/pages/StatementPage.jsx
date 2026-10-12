import { Download } from 'lucide-react';
import { useState } from 'react';
import { apiError, downloadFile } from '../api/client.js';
import { clean } from './TransactionHistoryPage.jsx';

export default function StatementPage() {
  const [filters, setFilters] = useState({ query: '', type: '', from: '', to: '' });
  const [message, setMessage] = useState({ type: '', text: '' });

  function update(event) {
    setFilters((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  async function exportFile(format) {
    setMessage({ type: '', text: '' });
    try {
      await downloadFile(`/statements.${format}`, clean(filters), `banksphere-statement.${format}`);
      setMessage({ type: 'success', text: `${format.toUpperCase()} export started.` });
    } catch (error) {
      setMessage({ type: 'error', text: apiError(error) });
    }
  }

  return (
    <section>
      <div className="page-heading">
        <div>
          <span className="eyebrow">Downloadable records</span>
          <h2>Account Statement</h2>
        </div>
      </div>
      <div className="form-panel">
        {message.text ? <div className={`alert ${message.type}`}>{message.text}</div> : null}
        <div className="filter-bar no-margin">
          <label>
            Search
            <input name="query" value={filters.query} onChange={update} placeholder="Reference or description" />
          </label>
          <label>
            Type
            <select name="type" value={filters.type} onChange={update}>
              <option value="">All</option>
              <option value="DEPOSIT">Deposit</option>
              <option value="WITHDRAWAL">Withdrawal</option>
              <option value="TRANSFER_IN">Transfer in</option>
              <option value="TRANSFER_OUT">Transfer out</option>
            </select>
          </label>
          <label>
            From
            <input name="from" type="date" value={filters.from} onChange={update} />
          </label>
          <label>
            To
            <input name="to" type="date" value={filters.to} onChange={update} />
          </label>
        </div>
        <div className="button-row">
          <button className="primary-button" onClick={() => exportFile('csv')}>
            <Download size={18} />
            Export CSV
          </button>
          <button className="secondary-button" onClick={() => exportFile('pdf')}>
            <Download size={18} />
            Export PDF
          </button>
        </div>
      </div>
    </section>
  );
}
