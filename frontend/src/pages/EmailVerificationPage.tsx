import { useState, type FormEvent } from 'react';
import { api } from '../app/api';
import AppShell from '../components/AppShell';

interface SendMailResult {
  maskedRecipient: string;
  sentAt: string;
}

export default function EmailVerificationPage() {
  const [email, setEmail] = useState('');
  const [sending, setSending] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

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
    } catch (reason) {
      setError((reason as Error).message);
    } finally {
      setSending(false);
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
    </div>
  </AppShell>;
}
