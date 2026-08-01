import { useEffect, useState, type FormEvent } from 'react';
import { api } from '../app/api';
import AppShell from '../components/AppShell';

interface SendMailResult {
  maskedRecipient: string;
  sentAt: string;
}

interface DeliveryLog {
  id: string;
  maskedRecipient: string;
  purpose: 'ADMIN_TEST' | 'REGISTRATION' | 'PASSWORD_RESET';
  status: 'SUCCESS' | 'FAILED';
  errorSummary?: string;
  completedAt: string;
}

export default function EmailVerificationPage() {
  const [email, setEmail] = useState('');
  const [sending, setSending] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');
  const [logs, setLogs] = useState<DeliveryLog[]>([]);

  /** 頁面載入時取得最近寄送紀錄。 */
  useEffect(() => {
    void loadLogs();
  }, []);

  /** 驗證輸入並呼叫管理員專用的 Gmail SMTP 測試 API。 */
  async function sendVerificationMail(event: FormEvent) {
    event.preventDefault();
    setSending(true);
    setSuccess('');
    setError('');

    try {
      const result = await api<SendMailResult>('/admin/email-verification/send', {
        method: 'POST',
        body: JSON.stringify({ email }),
      });
      setSuccess(`驗證碼已寄送至 ${result.maskedRecipient}`);
      await loadLogs();
    } catch (reason) {
      setError((reason as Error).message);
    } finally {
      setSending(false);
    }
  }

  /** 讀取管理員可見的最近二十筆寄送紀錄。 */
  async function loadLogs() {
    try {
      setLogs(await api<DeliveryLog[]>('/admin/email-verification/logs'));
    } catch (reason) {
      setError((reason as Error).message);
    }
  }

  return <AppShell>
    <div className="content mail-verification-content">
      <header className="page-heading">
        <div>
          <p className="eyebrow">Gmail SMTP test</p>
          <h1>信箱驗證</h1>
          <p>輸入收件信箱，確認 AgentFlow 的 Gmail SMTP 驗證碼寄送功能。</p>
        </div>
      </header>

      <section className="card mail-verification-card">
        <div className="mail-verification-intro">
          <span className="mail-icon" aria-hidden="true">✉</span>
          <div>
            <h2>寄送測試驗證碼</h2>
            <p>信件會包含 6 位數驗證碼，並提示於 10 分鐘內使用。</p>
          </div>
        </div>

        <form className="mail-verification-form" onSubmit={sendVerificationMail}>
          <label htmlFor="verification-email">收件信箱</label>
          <div className="mail-input-row">
            <input
              id="verification-email"
              data-testid="verification-email"
              type="email"
              autoComplete="email"
              placeholder="name@example.com"
              required
              maxLength={160}
              value={email}
              onChange={event => setEmail(event.target.value)}
              disabled={sending}
            />
            <button
              data-testid="send-verification-mail"
              className="btn primary"
              type="submit"
              disabled={sending}
            >
              {sending ? '寄送中…' : '寄送驗證碼'}
            </button>
          </div>
        </form>

        <div className="mail-security-note">
          此功能僅供系統管理員測試寄信設定；頁面與 API 都不會顯示驗證碼。
        </div>
        <div className="mail-result" aria-live="polite">
          {success && <p className="success">{success}</p>}
          {error && <p className="error">{error}</p>}
        </div>
      </section>

      <section className="card mail-log-card">
        <div className="mail-log-heading">
          <div>
            <p className="eyebrow">Delivery audit</p>
            <h2>最近寄送紀錄</h2>
          </div>
          <button className="btn secondary" type="button" onClick={() => void loadLogs()}>重新整理</button>
        </div>
        {logs.length === 0
          ? <p className="empty-state">尚無寄送紀錄。</p>
          : <div className="mail-log-table-wrap"><table>
            <thead><tr><th>收件者</th><th>用途</th><th>狀態</th><th>時間</th><th>說明</th></tr></thead>
            <tbody>{logs.map(log => <tr key={log.id} data-testid="mail-log-row">
              <td data-label="收件者">{log.maskedRecipient}</td>
              <td data-label="用途">{purposeLabel(log.purpose)}</td>
              <td data-label="狀態"><span className={`tag ${log.status === 'SUCCESS' ? 'managed' : 'failed'}`}>
                {log.status === 'SUCCESS' ? '成功' : '失敗'}
              </span></td>
              <td data-label="時間">{new Date(log.completedAt).toLocaleString('zh-TW')}</td>
              <td data-label="說明">{log.errorSummary ?? '—'}</td>
            </tr>)}</tbody>
          </table></div>}
      </section>
    </div>
  </AppShell>;
}

/** 將後端用途列舉轉為管理頁中文。 */
function purposeLabel(purpose: DeliveryLog['purpose']) {
  if (purpose === 'REGISTRATION') return '信箱註冊';
  if (purpose === 'PASSWORD_RESET') return '忘記密碼';
  return '管理員測試';
}
