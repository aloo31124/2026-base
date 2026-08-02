import { type ReactNode, useEffect, useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useAppDispatch, useAppSelector } from "../app/hooks";
import { logout } from "../features/auth/authSlice";

export default function AppShell({ children }: { children: ReactNode }) {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const session = useAppSelector((s) => s.auth.session);
  const [sidebarCollapsed, setSidebarCollapsed] = useState(
    () => localStorage.getItem("agentflow-sidebar-collapsed") === "true",
  );
  const [darkMode, setDarkMode] = useState(
    () => localStorage.getItem("agentflow-theme") === "dark",
  );

  useEffect(() => {
    document.documentElement.dataset.theme = darkMode ? "dark" : "light";
    localStorage.setItem("agentflow-theme", darkMode ? "dark" : "light");
  }, [darkMode]);

  useEffect(() => {
    const media = window.matchMedia("(max-width: 720px)");
    const syncMobileSidebar = () => {
      if (media.matches) setSidebarCollapsed(true);
    };
    syncMobileSidebar();
    media.addEventListener("change", syncMobileSidebar);
    return () => media.removeEventListener("change", syncMobileSidebar);
  }, []);

  useEffect(() => {
    const addMobileLabels = () => {
      document.querySelectorAll("table").forEach((table) => {
        const headings = Array.from(table.querySelectorAll("thead th")).map(
          (cell) => cell.textContent?.trim() ?? "",
        );
        table.querySelectorAll("tbody tr").forEach((row) => {
          row.querySelectorAll("td").forEach((cell, index) => {
            if (!cell.dataset.label) cell.dataset.label = headings[index] ?? "";
          });
        });
      });
    };
    addMobileLabels();
    const observer = new MutationObserver(addMobileLabels);
    observer.observe(document.body, { childList: true, subtree: true });
    return () => observer.disconnect();
  }, []);

  const toggleSidebar = () => {
    setSidebarCollapsed((current) => {
      const next = !current;
      localStorage.setItem("agentflow-sidebar-collapsed", String(next));
      return next;
    });
  };

  return (
    <div className="shell">
      <header className="app-header">
        <div className="header-leading">
          <button
            className="icon-button sidebar-toggle"
            aria-label={sidebarCollapsed ? "展開左選單" : "收合左選單"}
            aria-expanded={!sidebarCollapsed}
            onClick={toggleSidebar}
          >
            {sidebarCollapsed ? "☰" : "×"}
          </button>
          <div className="brand">
            <span className="brand-mark">A</span>AgentFlow
          </div>
        </div>
        <div className="header-actions">
          <button
            className="icon-button"
            aria-label={darkMode ? "切換日間模式" : "切換夜間模式"}
            onClick={() => setDarkMode((current) => !current)}
          >
            {darkMode ? "☀" : "☾"}
          </button>
          <div className="avatar">
            {session?.fullName.slice(0, 2).toUpperCase()}
          </div>
          <div>
            <strong>{session?.fullName}</strong>
            <small>
              {session?.roles.includes("SYSTEM_ADMIN")
                ? "系統管理員"
                : session?.roles.includes("MANAGER")
                  ? "主管"
                  : "員工"}
            </small>
          </div>
          <button
            className="btn secondary"
            onClick={() => {
              dispatch(logout());
              navigate("/login");
            }}
          >
            登出
          </button>
        </div>
      </header>
      <aside className={`sidebar${sidebarCollapsed ? " collapsed" : ""}`}>
        <nav aria-label="主要導覽">
          {session?.roles.includes("SYSTEM_ADMIN") && (
            <NavLink to="/users">
              <span className="nav-icon">♙</span>
              <span>使用者分權</span>
            </NavLink>
          )}
          {session?.roles.includes("SYSTEM_ADMIN") && (
            <NavLink to="/email-verification">
              <span className="nav-icon">✉</span>
              <span>信箱驗證</span>
            </NavLink>
          )}
          {session?.roles.includes("SYSTEM_ADMIN") && (
            <NavLink to="/registration-management">
              <span className="nav-icon">▣</span>
              <span>註冊登入管理</span>
            </NavLink>
          )}
          {session?.roles.includes("SYSTEM_ADMIN") && (
            <NavLink to="/system-reports">
              <span className="nav-icon">⌁</span>
              <span>系統報表</span>
            </NavLink>
          )}
          {session?.roles.includes("SYSTEM_ADMIN") && (
            <NavLink to="/company-supervisor-management">
              <span className="nav-icon">⌂</span>
              <span>公司主管管理</span>
            </NavLink>
          )}
          <NavLink to="/task-assignment">
            <span className="nav-icon">↗</span>
            <span>任務指派</span>
          </NavLink>
          {session?.roles.includes("MANAGER") && (
            <NavLink to="/manager-reports">
              <span className="nav-icon">◔</span>
              <span>主管報表</span>
            </NavLink>
          )}
          <NavLink to="/my-tasks">
            <span className="nav-icon">✓</span>
            <span>我的任務</span>
          </NavLink>
          <NavLink to="/test/testTemp/">
            <span className="nav-icon">⌁</span>
            <span>資料連線測試</span>
          </NavLink>
        </nav>
      </aside>
      <main
        className={`app-main${sidebarCollapsed ? " sidebar-collapsed" : ""}`}
      >
        {children}
      </main>
    </div>
  );
}
