import { useEffect, useState, type FormEvent } from "react";
import { api } from "../app/api";
import AppShell from "../components/AppShell";
import TaskTrendChart, { type TaskTrendPoint } from "../components/TaskTrendChart";

interface CompanyOption {
  id: string;
  name: string;
}

interface TaskTrendReport {
  companyId?: string;
  companyName: string;
  from: string;
  to: string;
  totalTasks: number;
  companyCount: number;
  points: TaskTrendPoint[];
}

/** 將 Date 轉為瀏覽器本地 yyyy-MM-dd，避免 UTC 跨日。 */
function localDateValue(date: Date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

/** 建立首次進入所需的最近一年含今日篩選。 */
function defaultRange() {
  const to = new Date();
  const from = new Date(to);
  from.setFullYear(from.getFullYear() - 1);
  return { from: localDateValue(from), to: localDateValue(to) };
}

export default function SystemReportPage() {
  const initialRange = defaultRange();
  const [companies, setCompanies] = useState<CompanyOption[]>([]);
  const [filters, setFilters] = useState({ companyId: "", ...initialRange });
  const [report, setReport] = useState<TaskTrendReport>();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  /** 依目前篩選載入任務趨勢並保留可重試狀態。 */
  async function loadTrend(nextFilters = filters) {
    setLoading(true);
    setError("");
    try {
      const query = new URLSearchParams({ from: nextFilters.from, to: nextFilters.to });
      if (nextFilters.companyId) query.set("companyId", nextFilters.companyId);
      setReport(await api<TaskTrendReport>(`/admin/system-reports/task-trend?${query}`));
    } catch (reason) {
      setError((reason as Error).message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    /** 首次同時取得公司選項與預設最近一年趨勢。 */
    async function initialize() {
      try {
        setCompanies(await api<CompanyOption[]>("/admin/system-reports/companies"));
      } catch (reason) {
        setError((reason as Error).message);
      }
      await loadTrend(initialRangeWithCompany());
    }

    /** 提供 useEffect 固定的首次篩選值。 */
    function initialRangeWithCompany() {
      return { companyId: "", ...initialRange };
    }

    void initialize();
    // 首次載入後由使用者提交篩選觸發後續查詢。
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  /** 套用公司與日期篩選。 */
  async function applyFilters(event: FormEvent) {
    event.preventDefault();
    await loadTrend(filters);
  }

  return (
    <AppShell>
      <div className="content system-report-content">
        <header className="page-heading">
          <div>
            <p className="eyebrow">System reports</p>
            <h1>系統報表</h1>
            <p>檢視整個系統中所有公司的任務量與時間趨勢。</p>
          </div>
          <button className="btn secondary" type="button" onClick={() => void loadTrend()}>
            重新整理
          </button>
        </header>

        <div className="tabs report-tabs" role="tablist" aria-label="系統報表標籤">
          <button
            data-testid="task-trend-tab"
            className="active"
            type="button"
            role="tab"
            aria-selected="true"
          >
            任務趨勢
          </button>
        </div>

        <form className="card report-filter" onSubmit={applyFilters}>
          <div className="report-filter-heading">
            <div>
              <p className="eyebrow">Filter</p>
              <h2>公司與時間範圍</h2>
            </div>
            <p>預設顯示最近一年，起迄日皆納入統計。</p>
          </div>
          <div className="report-filter-fields">
            <label>
              公司
              <select
                data-testid="trend-company"
                value={filters.companyId}
                onChange={(event) => setFilters({ ...filters, companyId: event.target.value })}
              >
                <option value="">全部公司</option>
                {companies.map((company) => (
                  <option key={company.id} value={company.id}>
                    {company.name}
                  </option>
                ))}
              </select>
            </label>
            <label>
              開始日期
              <input
                data-testid="trend-from"
                type="date"
                required
                value={filters.from}
                onChange={(event) => setFilters({ ...filters, from: event.target.value })}
              />
            </label>
            <label>
              結束日期
              <input
                data-testid="trend-to"
                type="date"
                required
                value={filters.to}
                onChange={(event) => setFilters({ ...filters, to: event.target.value })}
              />
            </label>
            <button data-testid="trend-apply" className="btn primary" disabled={loading}>
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
            正在載入任務趨勢…
          </section>
        )}

        {report && (
          <>
            <section className="report-metrics" aria-label="任務趨勢摘要">
              <article className="card metric-card">
                <span>統計範圍</span>
                <strong data-testid="trend-scope">{report.companyName}</strong>
              </article>
              <article className="card metric-card">
                <span>任務總數</span>
                <strong data-testid="trend-total">{report.totalTasks}</strong>
              </article>
              <article className="card metric-card">
                <span>包含公司</span>
                <strong>{report.companyCount}</strong>
              </article>
              <article className="card metric-card">
                <span>日期範圍</span>
                <strong className="metric-range">
                  {report.from} – {report.to}
                </strong>
              </article>
            </section>

            <section className="card trend-card">
              <header className="trend-card-heading">
                <div>
                  <p className="eyebrow">Task trend</p>
                  <h2>所有公司任務量</h2>
                </div>
                <span className="tag managed">每日</span>
              </header>
              {report.totalTasks === 0 && (
                <p data-testid="trend-empty" className="report-empty">
                  所選範圍目前沒有任務，折線圖以 0 呈現。
                </p>
              )}
              <TaskTrendChart points={report.points} scope={report.companyName} />
            </section>
          </>
        )}
      </div>
    </AppShell>
  );
}
