export interface TaskStatusBucket {
  status: string;
  label: string;
  taskCount: number;
  percentage: number;
}

interface TaskStatusPieChartProps {
  buckets: TaskStatusBucket[];
  scope: string;
}

const COLORS = ["var(--primary)", "var(--accent)", "var(--secondary)"];

/** 將狀態數量轉為可存取圓餅圖與精確文字圖例。 */
export default function TaskStatusPieChart({ buckets, scope }: TaskStatusPieChartProps) {
  const total = buckets.reduce((sum, bucket) => sum + bucket.taskCount, 0);
  let allocated = 0;
  const segments = buckets.map((bucket, index) => {
    const start = allocated;
    allocated += bucket.percentage;
    return `${COLORS[index % COLORS.length]} ${start}% ${allocated}%`;
  });
  const background = total === 0
    ? "var(--surface-soft)"
    : `conic-gradient(${segments.join(", ")})`;

  return (
    <figure className="status-pie-figure">
      <div
        data-testid="manager-status-pie"
        className="status-pie"
        role="img"
        aria-label={`${scope}的任務狀態比例，共 ${total} 筆任務`}
        style={{ background }}
      >
        <div className="status-pie-center" aria-hidden="true">
          <strong>{total}</strong>
          <span>筆任務</span>
        </div>
      </div>
      <figcaption>
        <ul className="status-pie-legend" aria-label="任務狀態圖例">
          {buckets.map((bucket, index) => (
            <li key={bucket.status} data-testid={`status-${bucket.status}`}>
              <span
                className="status-pie-swatch"
                style={{ background: COLORS[index % COLORS.length] }}
                aria-hidden="true"
              />
              <span>{bucket.label}</span>
              <strong>{bucket.taskCount} 筆</strong>
              <span>{bucket.percentage.toFixed(1)}%</span>
            </li>
          ))}
        </ul>
      </figcaption>
    </figure>
  );
}
