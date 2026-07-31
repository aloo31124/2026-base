import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../app/hooks';
import { clearAuthError, login, requestLineAuthorization } from '../features/auth/authSlice';

export default function LoginPage() {
  const dispatch = useAppDispatch(); const navigate = useNavigate(); const status = useAppSelector(s => s.auth.status); const error = useAppSelector(s => s.auth.error);
  const [username, setUsername] = useState('admin'); const [password, setPassword] = useState('admin123');
  /** 登入後依角色進入管理頁或公司／任務協作流程。 */
  async function submit(event: FormEvent) {
    event.preventDefault();
    const result = await dispatch(login({ username, password }));
    if (login.fulfilled.match(result)) {
      navigate(result.payload.roles.includes('SYSTEM_ADMIN') ? '/users' : '/task-assignment');
    }
  }
  async function lineLogin() {
    dispatch(clearAuthError());
    const result = await dispatch(requestLineAuthorization());
    if (requestLineAuthorization.fulfilled.match(result)) window.location.assign(result.payload.authorizationUrl);
  }
  return <main className="auth-page"><section className="auth-visual"><div className="brand"><span className="brand-mark">A</span>AgentFlow</div><div><p className="eyebrow">Secure workspace</p><h1>讓權限清楚，讓工作流保持流動。</h1><p>以安全驗證與角色授權保護每一個管理操作。</p><div className="workflow-preview"><span className="workflow-node">身分驗證</span><i className="workflow-line" /><span className="workflow-node">角色授權</span><i className="workflow-line" /><span className="workflow-node">安全工作</span></div></div><small>© 2026 AgentFlow. Built for focused teams.</small></section><section className="auth-panel"><div className="auth-box"><p className="eyebrow">Welcome back</p><h2>登入工作空間</h2><p className="auth-hint">繼續管理你的代理工作流與自動化任務。</p><button type="button" data-testid="line-login" className="line-login-button" disabled={status === 'loading'} onClick={() => void lineLogin()}><span className="line-mark" aria-hidden="true">LINE</span><span>{status === 'loading' ? '正在前往 LINE…' : '使用 LINE 登入'}</span></button><div className="auth-divider"><span>或使用帳號密碼</span></div><form className="auth-form" onSubmit={submit}><label>帳號或信箱<input data-testid="username" value={username} onChange={e => setUsername(e.target.value)} autoComplete="username" /></label><label>密碼<input data-testid="password" type="password" value={password} onChange={e => setPassword(e.target.value)} autoComplete="current-password" /></label>{error && <p className="error" role="alert" data-testid="auth-error">{error}</p>}<button data-testid="login-submit" className="btn primary" disabled={status === 'loading'}>{status === 'loading' ? '登入中…' : '登入'}</button></form><div className="auth-links"><Link data-testid="email-register-link" to="/register">使用信箱註冊</Link><Link data-testid="forgot-password-link" to="/forgot-password">忘記密碼？</Link></div></div></section></main>;
}
