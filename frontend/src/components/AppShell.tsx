import { type ReactNode } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../app/hooks';
import { logout } from '../features/auth/authSlice';

export default function AppShell({ children }: { children: ReactNode }) {
  const dispatch = useAppDispatch(); const navigate = useNavigate(); const session = useAppSelector(s => s.auth.session);
  return <div className="shell">
    <header className="app-header"><div className="brand"><span className="brand-mark">A</span>AgentFlow</div><div className="header-actions"><div className="avatar">{session?.fullName.slice(0, 2).toUpperCase()}</div><div><strong>{session?.fullName}</strong><small>{session?.roles.includes('SYSTEM_ADMIN') ? '系統管理員' : '員工'}</small></div><button className="btn secondary" onClick={() => { dispatch(logout()); navigate('/login'); }}>登出</button></div></header>
    <aside className="sidebar"><nav>
      {session?.roles.includes('SYSTEM_ADMIN') && <NavLink to="/users">使用者分權</NavLink>}
      {session?.roles.includes('SYSTEM_ADMIN') && <NavLink to="/email-verification">信箱驗證</NavLink>}
      {session?.roles.includes('SYSTEM_ADMIN') && <NavLink to="/registration-management">註冊登入管理</NavLink>}
      {session?.roles.includes('SYSTEM_ADMIN') && <NavLink to="/company-supervisor-management">公司主管管理</NavLink>}
      <NavLink to="/test/testTemp/">資料連線測試</NavLink>
    </nav></aside>
    <main className="app-main">{children}</main>
  </div>;
}
