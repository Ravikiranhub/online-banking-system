export default function MetricTile({ icon: Icon, label, value, tone = 'navy' }) {
  return (
    <div className={`metric-tile metric-${tone}`}>
      {Icon ? <Icon size={22} /> : null}
      <div>
        <span>{label}</span>
        <strong>{value}</strong>
      </div>
    </div>
  );
}
