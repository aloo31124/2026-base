import { useEffect, useState, type FormEvent } from "react";
import { api } from "../app/api";
import AppShell from "../components/AppShell";
import TaskStatusPieChart, { type TaskStatusBucket } from "../components/TaskStatusPieChart";
import TaskTrendChart, { type TaskTrendPoint } from "../components/TaskTrendChart";

interface AssigneeOption {
  id: string;
  name: string;
}

interface WorkStatusOption {
  value: string;
  label: string;
}

interface MyReportFilters {
  assignees: AssigneeOption[];
  workStatuses: WorkStatusOption[];
  defaultFrom: string;
  defaultTo: string;
}

interface MyReport {
  from: string;
  to: string;
  assigneeId: string;
  assigneeName: string;
  workStatus?: string;
  totalTasks: number;
  trendPoints: TaskTrendPoint[];
  statusBuckets: TaskStatusBucket[];
}

interface FilterState {
  assigneeId: string;
  workStatus: string;
  from: string;
  to: string;
}

const EMPTY_FILTERS: FilterState = { assigneeId: "", workStatus: "", from: "", to: "" };

/** 提供登入員工自己的任務總覽、趨勢與狀態比例。 */
export default function MyReportPage() {
  const [options, setOptions] = useState<MyReportFilters>();
  const [filters, setFilters] = useState<FilterState>(EMPTY_FILTERS);
  const [report, setReport] = useState<MyReport>();
  const [activeTab, setActiveTab] = useState<"trend" | "status">("trend");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  /** 依指定篩選取得登入員工自己的兩種圖表資料。 */
  async function loadReport(nextFilters: FilterState) {
    setLoading(true);
    setError("");
    try {
      const query = new URLSearchParams({ from: nextFilters.from, to: nextFilters.to });
      if (nextFilters.assigneeId) query.set("assigneeId", nextFilters.assigneeId);
      if (nextFilters.workStatus) query.set("workStatus", nextFilters.workStatus);
      setReport(await api<MyReport>(`/my/reports/report?${query}`));
    } catch (reason) {
      setError((reason as Error).message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    /** 載入本人選項後，以後端提供的最近一年範圍查詢。 */
    async function initialize() {
      setLoading(true);
      try {
        const initial = await api<MyReportFilters>("/my/reports/filters");
        const initialFilters = {
          assigneeId: "",
          workStatus: "",
          from: initial.defaultFrom,
          to: initial.defaultTo,
        };
        setOptions(initial);
        setFilters(initialFilters);
        await loadReport(initialFilters);
      } catch (reason) {
        setError((reason as Error).message);
        setLoading(false);
      }
    }

    void initialize();
  }, []);

  /** 套用目前執行者、執行狀態與日期篩選。 */
  async function applyFilters(event: FormEvent) {
    event.preventDefault();
    await loadReport(filters);
  }

  const selectedStatus = options?.workStatuses.find((status) => status.value === report?.workStatus)?.label;

  return (
    <AppShell>
      <div className="content system-report-content my-report-content">
        <header className="page-heading">
          <div>
            <p className="eyebrow">My reports</p>
            <h1>我的報表</h1>
            <p>檢視自己的任務總覽、每日任務趨勢與目前工作狀態比例。</p>
          </div>
          <button
            className="btn secondary"
            type="button"
            disabled={!filters.from || loading}
            onClick={() => void loadReport(filters)}
          >
            重新整理
          </button>
        </header>

        <div className="tabs report-tabs" role="tablist" aria-label="我的報表標籤">
          <button
            data-testid="my-trend-tab"
            className={activeTab === "trend" ? "active" : ""}
            type="button"
            role="tab"
            aria-selected={activeTab === "trend"}
            onClick={() => setActiveTab("trend")}
          >
            任務趨勢
          </button>
          <button
            data-testid="my-status-tab"
            className={activeTab === "status" ? "active" : ""}
            type="button"
            role="tab"
            aria-selected={activeTab === "status"}
            onClick={() => setActiveTab("status")}
          >
            任務狀態比
          </button>
        </div>

        <form className="card report-filter" onSubmit={applyFilters}>
          <div className="report-filter-heading">
            <div>
              <p className="eyebrow">Filter</p>
              <h2>執行者、執行狀態與時間範圍</h2>
            </div>
            <p>執行者僅限本人；條件會同時套用至兩個圖表標籤。</p>
          </div>
          <div className="report-filter-fields manager-report-filter-fields">
            <label>
              執行者
              <select
                data-testid="my-assignee"
                value={filters.assigneeId}
                onChange={(event) => setFilters({ ...filters, assigneeId: event.target.value })}
              >
                <option value="">全部執行者</option>
                {options?.assignees.map((assignee) => (
                  <option key={assignee.id} value={assignee.id}>
                    {assignee.name}
                  </option>
                ))}
              </select>
            </label>
            <label>
              執行狀態
              <select
                data-testid="my-work-status"
                value={filters.workStatus}
                onChange={(event) => setFilters({ ...filters, workStatus: event.target.value })}
              >
                <option value="">全部狀態</option>
                {options?.workStatuses.map((status) => (
                  <option key={status.value} value={status.value}>
                    {status.label}
                  </option>
                ))}
              </select>
            </label>
            <label>
              開始日期
              <input
                data-testid="my-from"
                type="date"
                required
                value={filters.from}
                onChange={(event) => setFilters({ ...filters, from: event.target.value })}
              />
            </label>
            <label>
              結束日期
              <input
                data-testid="my-to"
                type="date"
                required
                value={filters.to}
                onChange={(event) => setFilters({ ...filters, to: event.target.value })}
              />
            </label>
            <button
              data-testid="my-apply"
              className="btn primary"
              disabled={loading || !filters.from || !filters.to}
            >
              {loading ? "查詢中…" : "套用篩選"}
            </button>
          </div>
        </form>

        {error && (
          <div className="info-banner report-error" role="alert">
            {error}
          </div>
        )}

        {loading && !report && (
          <section className="card report-loading" aria-live="polite">
            正在載入我的報表…
          </section>
        )}

        {report && (
          <>
            <section className="report-metrics" aria-label="我的報表摘要">
              <article className="card metric-card">
                <span>我的任務總數</span>
                <strong data-testid="my-total">{report.totalTasks}</strong>
              </article>
              <article className="card metric-card">
                <span>執行者範圍</span>
                <strong data-testid="my-assignee-name">{report.assigneeName}</strong>
              </article>
              <article className="card metric-card">
                <span>工作狀態</span>
                <strong>{selectedStatus ?? "全部狀態"}</strong>
              </article>
              <article className="card metric-card">
                <span>日期範圍</span>
                <strong className="metric-range">{report.from} – {report.to}</strong>
              </article>
            </section>

            {report.totalTasks === 0 && (
              <p data-testid="my-report-empty" className="report-empty">
                所選範圍目前沒有符合條件的自己任務，圖表以 0 呈現。
              </p>
            )}

            {activeTab === "trend" ? (
              <section className="card trend-card" role="tabpanel">
                <header className="trend-card-heading">
                  <div>
                    <p className="eyebrow">My task trend</p>
                    <h2>自己的任務量</h2>
                  </div>
                  <span className="tag managed">每日</span>
                </header>
                <TaskTrendChart
                  testId="my-trend-chart"
                  points={report.trendPoints}
                  scope={report.assigneeName}
                />
              </section>
            ) : (
              <section className="card trend-card status-ratio-card" role="tabpanel">
                <header className="trend-card-heading">
                  <div>
                    <p className="eyebrow">My task status ratio</p>
                    <h2>自己的任務狀態比例</h2>
                  </div>
                  <span className="tag managed">目前狀態</span>
                </header>
                <TaskStatusPieChart
                  testId="my-status-pie"
                  buckets={report.statusBuckets}
                  scope={report.assigneeName}
                />
              </section>
            )}
          </>
        )}
      </div>
    </AppShell>
  );
}
