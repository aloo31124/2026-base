import {
  type ReactNode,
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useAppDispatch, useAppSelector } from "../app/hooks";
import { logout } from "../features/auth/authSlice";
import SessionCountdown from "./SessionCountdown";

export default function AppShell({ children }: { children: ReactNode }) {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const session = useAppSelector((s) => s.auth.session);
  const sidebarRef = useRef<HTMLElement | null>(null);
  const headerRef = useRef<HTMLElement | null>(null);
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

  useEffect(() => {
    if (sidebarCollapsed || window.innerWidth > 720) return;

    const handleDocumentClick = (event: MouseEvent) => {
      const target = event.target;
      if (!(target instanceof Node)) return;

      const clickedToggle = headerRef.current
        ?.querySelector(".sidebar-toggle")
        ?.contains(target);
      const clickedSidebar = sidebarRef.current?.contains(target);

      if (!clickedSidebar && !clickedToggle) {
        setSidebarCollapsed(true);
        localStorage.setItem("agentflow-sidebar-collapsed", "true");
      }
    };

    document.addEventListener("click", handleDocumentClick);
    return () => document.removeEventListener("click", handleDocumentClick);
  }, [sidebarCollapsed]);

  const toggleSidebar = () => {
    setSidebarCollapsed((current) => {
      const next = !current;
      localStorage.setItem("agentflow-sidebar-collapsed", String(next));
      return next;
    });
  };

  const closeSidebar = () => {
    if (window.innerWidth <= 720) {
      setSidebarCollapsed(true);
      localStorage.setItem("agentflow-sidebar-collapsed", "true");
    }
  };

  const handleSessionExpiry = useCallback(() => {
    dispatch(logout("session-expired"));
    navigate("/login", { replace: true });
  }, [dispatch, navigate]);

  const handleManualLogout = () => {
    dispatch(logout(undefined));
    navigate("/login", { replace: true });
  };

  return (
    <div className="shell">
      <header className="app-header" ref={headerRef}>
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
          {session && (
            <SessionCountdown
              token={session.token}
              onExpire={handleSessionExpiry}
            />
          )}
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
            onClick={handleManualLogout}
          >
            登出
          </button>
        </div>
      </header>
      <aside
        className={`sidebar${sidebarCollapsed ? " collapsed" : ""}`}
        ref={sidebarRef}
      >
        <nav aria-label="主要導覽" onClick={closeSidebar}>
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
          <NavLink to="/my-tasks">
            <span className="nav-icon">✓</span>
            <span>我的任務</span>
          </NavLink>
          {session?.roles.includes("MANAGER") && (
            <NavLink to="/manager-reports">
              <span className="nav-icon">◔</span>
              <span>主管報表</span>
            </NavLink>
          )}
          <NavLink to="/my-reports">
            <span className="nav-icon">◒</span>
            <span>我的報表</span>
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
