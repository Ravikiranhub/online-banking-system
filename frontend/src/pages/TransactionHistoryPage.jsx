import { Search } from 'lucide-react';
import { useEffect, useState } from 'react';
import { api, apiError } from '../api/client.js';
import DataState from '../components/DataState.jsx';
import Pagination from '../components/Pagination.jsx';
import { formatCurrency, formatDate, typeLabel } from '../utils/format.js';

const initialFilters = { query: '', type: '', from: '', to: '' };

export default function TransactionHistoryPage() {
  const [filters, setFilters] = useState(initialFilters);
  const [pageNumber, setPageNumber] = useState(0);
  const [state, setState] = useState({ loading: true, error: '', page: null });

  useEffect(() => {
    let mounted = true;
    setState((current) => ({ ...current, loading: true, error: '' }));
    api
      .get('/transactions', { params: { ...clean(filters), page: pageNumber, size: 8 } })
      .then(({ data }) => mounted && setState({ loading: false, error: '', page: data }))
      .catch((error) => mounted && setState({ loading: false, error: apiError(error), page: null }));
    return () => {
      mounted = false;
    };
  }, [filters, pageNumber]);

  function update(event) {
    setFilters((current) => ({ ...current, [event.target.name]: event.target.value }));
    setPageNumber(0);
  }

  return (
    <section>
      <div className="page-heading">
        <div>
          <span className="eyebrow">Search and filter</span>
          <h2>Transaction History</h2>
        </div>
      </div>
      <div className="filter-bar">
        <label>
          Search
          <div className="input-with-icon">
            <Search size={17} />
            <input name="query" value={filters.query} onChange={update} placeholder="Reference, description, account" />
          </div>
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
      <DataState loading={state.loading} error={state.error} empty={!state.page?.content?.length} title="No matching transactions">
        <div className="table-wrap section-panel">
          <table>
            <thead>
              <tr>
                <th>Date</th>
                <th>Reference</th>
                <th>Account</th>
                <th>Type</th>
                <th>Amount</th>
                <th>Balance</th>
                <th>Description</th>
              </tr>
            </thead>
            <tbody>
              {state.page?.content?.map((transaction) => (
                <tr key={transaction.id || transaction.transactionReference}>
                  <td>{formatDate(transaction.occurredAt)}</td>
                  <td>{transaction.transactionReference}</td>
                  <td>{transaction.accountNumber}</td>
                  <td>{typeLabel(transaction.type)}</td>
                  <td>{formatCurrency(transaction.amount)}</td>
                  <td>{formatCurrency(transaction.balanceAfter)}</td>
                  <td>{transaction.description}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <Pagination page={state.page} onPageChange={setPageNumber} />
        </div>
      </DataState>
    </section>
  );
}

export function clean(values) {
  return Object.fromEntries(Object.entries(values).filter(([, value]) => value !== '' && value !== null && value !== undefined));
}
