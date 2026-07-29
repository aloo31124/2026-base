import { Link } from 'react-router-dom';
import { useLocation } from 'react-router-dom';

export default function UnauthorizedPage() {
  const location = useLocation();
  const state = location.state as { message?: string } | null;
  const message = state?.message ?? '[使用者角色] [頁面] 無系統管理員權限。';
  return <main className="center-page"><section className="card">
    <p className="eyebrow">Access denied</p>
    <h1>無法存取</h1>
    <p data-testid="unauthorized-message">{message}</p>
    <Link className="btn primary" to="/test/testTemp/">返回可用頁面</Link>
  </section></main>;
}
