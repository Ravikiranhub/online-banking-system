export default function Pagination({ page, onPageChange }) {
  if (!page || page.totalPages <= 1) return null;

  return (
    <div className="pagination">
      <button className="secondary-button" onClick={() => onPageChange(page.number - 1)} disabled={page.first}>
        Previous
      </button>
      <span>
        Page {page.number + 1} of {page.totalPages}
      </span>
      <button className="secondary-button" onClick={() => onPageChange(page.number + 1)} disabled={page.last}>
        Next
      </button>
    </div>
  );
}
