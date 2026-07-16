import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../app/hooks';
import { login } from '../features/auth/authSlice';

export default function LoginPage() {
  const dispatch = useAppDispatch(); const navigate = useNavigate(); const status = useAppSelector(s => s.auth.status); const error = useAppSelector(s => s.auth.error);
  const [username, setUsername] = useState('admin'); const [password, setPassword] = useState('admin123');
  async function submit(event: FormEvent) { event.preventDefault(); const result = await dispatch(login({ username, password })); if (login.fulfilled.match(result)) navigate(result.payload.roles.includes('SYSTEM_ADMIN') ? '/users' : '/test/testTemp/'); }
  return <main className="auth-page"><section className="auth-visual"><div className="brand"><span className="brand-mark">A</span>AgentFlow</div><div><p className="eyebrow">Secure workspace</p><h1>讓權限清楚，讓工作流保持流動。</h1><p>以 JWT 與角色授權保護每一個管理操作。</p></div></section><section className="auth-panel"><form className="auth-box" onSubmit={submit}><p className="eyebrow">Welcome back</p><h2>登入工作空間</h2><label>帳號<input data-testid="username" value={username} onChange={e => setUsername(e.target.value)} /></label><label>密碼<input data-testid="password" type="password" value={password} onChange={e => setPassword(e.target.value)} /></label>{error && <p className="error" role="alert">{error}</p>}<button data-testid="login-submit" className="btn primary" disabled={status === 'loading'}>{status === 'loading' ? '登入中…' : '登入'}</button></form></section></main>;
}

