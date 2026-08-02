export interface TaskTrendPoint {
  date: string;
  taskCount: number;
}

interface TaskTrendChartProps {
  points: TaskTrendPoint[];
  scope: string;
}

const WIDTH = 800;
const HEIGHT = 280;
const PADDING_X = 52;
const PADDING_Y = 34;

/** 將任務趨勢轉為具座標、資料點與文字摘要的可存取 SVG 折線圖。 */
export default function TaskTrendChart({ points, scope }: TaskTrendChartProps) {
  const maxCount = Math.max(1, ...points.map((point) => point.taskCount));
  const plotWidth = WIDTH - PADDING_X * 2;
  const plotHeight = HEIGHT - PADDING_Y * 2;

  /** 依資料索引換算水平座標。 */
  const xFor = (index: number) =>
    PADDING_X + (points.length <= 1 ? plotWidth / 2 : (index / (points.length - 1)) * plotWidth);

  /** 依任務數換算由上而下的垂直座標。 */
  const yFor = (count: number) => PADDING_Y + plotHeight - (count / maxCount) * plotHeight;

  const polyline = points
    .map((point, index) => `${xFor(index)},${yFor(point.taskCount)}`)
    .join(" ");
  const total = points.reduce((sum, point) => sum + point.taskCount, 0);
  const first = points.at(0)?.date ?? "—";
  const last = points.at(-1)?.date ?? "—";

  return (
    <figure className="trend-chart-figure">
      <svg
        data-testid="trend-chart"
        className="trend-chart"
        viewBox={`0 0 ${WIDTH} ${HEIGHT}`}
        role="img"
        aria-label={`${scope}從 ${first} 至 ${last} 的任務量折線圖，共 ${total} 筆任務`}
      >
        <title>{scope}任務量趨勢</title>
        <desc>
          日期範圍 {first} 至 {last}，共 {points.length} 個每日資料點、{total} 筆任務。
        </desc>
        {[0, 0.5, 1].map((ratio) => {
          const y = PADDING_Y + plotHeight * ratio;
          const value = Math.round(maxCount * (1 - ratio));
          return (
            <g key={ratio} className="trend-grid-line">
              <line x1={PADDING_X} y1={y} x2={WIDTH - PADDING_X} y2={y} />
              <text x={PADDING_X - 10} y={y + 4} textAnchor="end">
                {value}
              </text>
            </g>
          );
        })}
        <polyline className="trend-line" points={polyline} />
        {points.map((point, index) => (
          <circle
            key={point.date}
            data-testid="trend-chart-point"
            className="trend-point"
            cx={xFor(index)}
            cy={yFor(point.taskCount)}
            r={points.length <= 31 ? 4 : 2.4}
          >
            <title>{`${point.date}：${point.taskCount} 筆任務`}</title>
          </circle>
        ))}
        {points.length > 0 && (
          <g className="trend-axis-labels">
            <text x={PADDING_X} y={HEIGHT - 8} textAnchor="start">
              {first}
            </text>
            <text x={WIDTH - PADDING_X} y={HEIGHT - 8} textAnchor="end">
              {last}
            </text>
          </g>
        )}
      </svg>
      <figcaption>
        {scope} · {first} 至 {last} · {total} 筆任務
      </figcaption>
    </figure>
  );
}
