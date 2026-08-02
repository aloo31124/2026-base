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

interface ManagerReportFilters {
  companyName: string;
  assignees: AssigneeOption[];
  workStatuses: WorkStatusOption[];
  defaultFrom: string;
  defaultTo: string;
}

interface ManagerReport {
  companyName: string;
  from: string;
  to: string;
  assigneeId?: string;
  assigneeName: string;
  workStatus?: string;
  companyTotalTasks: number;
  managerTotalTasks: number;
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

export default function ManagerReportPage() {
  const [options, setOptions] = useState<ManagerReportFilters>();
  const [filters, setFilters] = useState<FilterState>(EMPTY_FILTERS);
  const [report, setReport] = useState<ManagerReport>();
  const [activeTab, setActiveTab] = useState<"trend" | "status">("trend");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  /** 依指定篩選取得公司摘要與兩種圖表資料。 */
  async function loadReport(nextFilters: FilterState) {
    setLoading(true);
    setError("");
    try {
      const query = new URLSearchParams({ from: nextFilters.from, to: nextFilters.to });
      if (nextFilters.assigneeId) query.set("assigneeId", nextFilters.assigneeId);
      if (nextFilters.workStatus) query.set("workStatus", nextFilters.workStatus);
      setReport(await api<ManagerReport>(`/manager/reports/report?${query}`));
    } catch (reason) {
      setError((reason as Error).message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    /** 載入主管專屬選項後，以後端提供的最近一年範圍查詢。 */
    async function initialize() {
      setLoading(true);
      try {
        const initial = await api<ManagerReportFilters>("/manager/reports/filters");
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

  return (
    <AppShell>
      <div className="content system-report-content manager-report-content">
        <header className="page-heading">
          <div>
            <p className="eyebrow">Manager reports</p>
            <h1>主管報表</h1>
            <p>
              檢視
              <strong data-testid="manager-company-name"> {options?.companyName ?? "所屬公司"} </strong>
              的任務總覽與自己指派的執行情況。
            </p>
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

        <div className="tabs report-tabs" role="tablist" aria-label="主管報表標籤">
          <button
            data-testid="assigned-trend-tab"
            className={activeTab === "trend" ? "active" : ""}
            type="button"
            role="tab"
            aria-selected={activeTab === "trend"}
            onClick={() => setActiveTab("trend")}
          >
            指派任務趨勢
          </button>
          <button
            data-testid="status-ratio-tab"
            className={activeTab === "status" ? "active" : ""}
            type="button"
            role="tab"
            aria-selected={activeTab === "status"}
            onClick={() => setActiveTab("status")}
          >
            指派任務狀態比
          </button>
        </div>

        <form className="card report-filter" onSubmit={applyFilters}>
          <div className="report-filter-heading">
            <div>
              <p className="eyebrow">Filter</p>
              <h2>執行者、執行狀態與時間範圍</h2>
            </div>
            <p>條件會保留並同時套用至兩個圖表標籤。</p>
          </div>
          <div className="report-filter-fields manager-report-filter-fields">
            <label>
              執行者
              <select
                data-testid="manager-assignee"
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
                data-testid="manager-work-status"
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
                data-testid="manager-from"
                type="date"
                required
                value={filters.from}
                onChange={(event) => setFilters({ ...filters, from: event.target.value })}
              />
            </label>
            <label>
              結束日期
              <input
                data-testid="manager-to"
                type="date"
                required
                value={filters.to}
                onChange={(event) => setFilters({ ...filters, to: event.target.value })}
              />
            </label>
            <button
              data-testid="manager-apply"
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
            正在載入主管報表…
          </section>
        )}

        {report && (
          <>
            <section className="report-metrics" aria-label="主管報表摘要">
              <article className="card metric-card">
                <span>公司任務總數</span>
                <strong data-testid="company-total">{report.companyTotalTasks}</strong>
              </article>
              <article className="card metric-card">
                <span>我的指派任務</span>
                <strong data-testid="manager-total">{report.managerTotalTasks}</strong>
              </article>
              <article className="card metric-card">
                <span>執行者範圍</span>
                <strong>{report.assigneeName}</strong>
              </article>
              <article className="card metric-card">
                <span>日期範圍</span>
                <strong className="metric-range">{report.from} – {report.to}</strong>
              </article>
            </section>

            {report.managerTotalTasks === 0 && (
              <p data-testid="manager-report-empty" className="report-empty">
                所選範圍目前沒有符合條件的指派任務，圖表以 0 呈現。
              </p>
            )}

            {activeTab === "trend" ? (
              <section className="card trend-card" role="tabpanel">
                <header className="trend-card-heading">
                  <div>
                    <p className="eyebrow">Assigned task trend</p>
                    <h2>自己指派的任務量</h2>
                  </div>
                  <span className="tag managed">每日</span>
                </header>
                <TaskTrendChart
                  testId="manager-trend-chart"
                  points={report.trendPoints}
                  scope={`${report.companyName} · ${report.assigneeName}`}
                />
              </section>
            ) : (
              <section className="card trend-card status-ratio-card" role="tabpanel">
                <header className="trend-card-heading">
                  <div>
                    <p className="eyebrow">Assigned task status ratio</p>
                    <h2>自己指派的任務狀態比例</h2>
                  </div>
                  <span className="tag managed">目前狀態</span>
                </header>
                <TaskStatusPieChart
                  buckets={report.statusBuckets}
                  scope={`${report.companyName} · ${report.assigneeName}`}
                />
              </section>
            )}
          </>
        )}
      </div>
    </AppShell>
  );
}
