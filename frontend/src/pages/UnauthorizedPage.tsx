import { Link } from 'react-router-dom';
export default function UnauthorizedPage() { return <main className="center-page"><section className="card"><p className="eyebrow">Access denied</p><h1>無法存取</h1><p data-testid="unauthorized-message">[使用者角色] [頁面] 無系統管理員權限。</p><Link className="btn primary" to="/test/testTemp/">返回可用頁面</Link></section></main>; }

