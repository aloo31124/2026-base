import { useEffect, useState, type FormEvent } from 'react';
import { api } from '../app/api';
import AppShell from '../components/AppShell';

interface PasswordPolicy {
  minLength: number;
  requireLetter: boolean;
  requireNumber: boolean;
  updatedAt?: string;
}

interface RegistrationRecord {
  id: string;
  method: 'EMAIL' | 'LINE';
  identifier: string;
  success: boolean;
  completedAt: string;
}

export default function RegistrationManagementPage() {
  const [policy, setPolicy] = useState<PasswordPolicy>({
    minLength: 8,
    requireLetter: true,
    requireNumber: true,
  });
  const [records, setRecords] = useState<RegistrationRecord[]>([]);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  /** 頁面載入時並行取得政策與最近註冊紀錄。 */
  useEffect(() => {
    void loadPage();
  }, []);

  /** 讀取管理頁需要的兩組資料。 */
  async function loadPage() {
    setError('');
    try {
      const [currentPolicy, recentRecords] = await Promise.all([
        api<PasswordPolicy>('/admin/registration-management/policy'),
        api<RegistrationRecord[]>('/admin/registration-management/registrations'),
      ]);
      setPolicy(currentPolicy);
      setRecords(recentRecords);
    } catch (reason) {
      setError((reason as Error).message);
    }
  }

  /** 儲存管理員選定的動態密碼政策。 */
  async function savePolicy(event: FormEvent) {
    event.preventDefault();
    setSaving(true);
    setMessage('');
    setError('');
    try {
      const updated = await api<PasswordPolicy>('/admin/registration-management/policy', {
        method: 'PUT',
        body: JSON.stringify(policy),
      });
      setPolicy(updated);
      setMessage('密碼政策已儲存。');
    } catch (reason) {
      setError((reason as Error).message);
    } finally {
      setSaving(false);
    }
  }

  return <AppShell>
    <div className="content registration-management-content">
      <header className="page-heading">
        <div>
          <p className="eyebrow">Registration access control</p>
          <h1>註冊登入管理</h1>
          <p>設定新密碼安全條件，並檢視信箱與 LINE 首次註冊紀錄。</p>
        </div>
        <button className="btn secondary" type="button" onClick={() => void loadPage()}>重新整理</button>
      </header>

      <div className="info-banner">
        政策更新只影響之後的信箱註冊與忘記密碼，不會要求既有使用者立即變更密碼。
      </div>

      <section className="card policy-card">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Password policy</p>
            <h2>密碼複雜度</h2>
          </div>
          {policy.updatedAt && <small>更新：{new Date(policy.updatedAt).toLocaleString('zh-TW')}</small>}
        </div>
        <form onSubmit={savePolicy}>
          <label htmlFor="policy-min-length">最小密碼長度</label>
          <input
            id="policy-min-length"
            data-testid="policy-min-length"
            type="number"
            min={8}
            max={72}
            required
            value={policy.minLength}
            onChange={event => setPolicy({ ...policy, minLength: Number(event.target.value) })}
          />
          <div className="policy-options">
            <label className="policy-option">
              <input
                data-testid="policy-require-letter"
                type="checkbox"
                checked={policy.requireLetter}
                onChange={event => setPolicy({ ...policy, requireLetter: event.target.checked })}
              />
              <span><strong>至少一個英文字母</strong><small>接受大寫或小寫 A–Z</small></span>
            </label>
            <label className="policy-option">
              <input
                data-testid="policy-require-number"
                type="checkbox"
                checked={policy.requireNumber}
                onChange={event => setPolicy({ ...policy, requireNumber: event.target.checked })}
              />
              <span><strong>至少一個數字</strong><small>接受 0–9</small></span>
            </label>
          </div>
          <div className="actions">
            <button data-testid="policy-save" className="btn primary" type="submit" disabled={saving}>
              {saving ? '儲存中…' : '儲存密碼政策'}
            </button>
          </div>
        </form>
        <div className="auth-status" aria-live="polite">
          {message && <p data-testid="policy-success" className="success">{message}</p>}
          {error && <p data-testid="registration-management-error" className="error">{error}</p>}
        </div>
      </section>

      <section className="card registration-record-card">
        <div className="mail-log-heading">
          <div>
            <p className="eyebrow">Registration audit</p>
            <h2>最近註冊紀錄</h2>
          </div>
          <span className="tag">{records.length} 筆</span>
        </div>
        {records.length === 0
          ? <p data-testid="registration-empty" className="empty-state">尚無 LINE 或信箱首次註冊紀錄。</p>
          : <div className="registration-record-table"><table>
            <thead><tr><th>註冊方式</th><th>識別資料</th><th>狀態</th><th>完成時間</th></tr></thead>
            <tbody>{records.map(record => <tr key={record.id} data-testid="registration-record-row">
              <td><span className={`tag ${record.method === 'LINE' ? 'line' : ''}`}>
                {record.method === 'LINE' ? 'LINE 註冊' : '信箱註冊'}
              </span></td>
              <td>{record.identifier}</td>
              <td><span className={`tag ${record.success ? 'managed' : 'failed'}`}>
                {record.success ? '成功' : '失敗'}
              </span></td>
              <td>{new Date(record.completedAt).toLocaleString('zh-TW')}</td>
            </tr>)}</tbody>
          </table></div>}
      </section>
    </div>
  </AppShell>;
}
