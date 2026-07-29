import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../app/api';

interface TicketResult {
  ticketId: string;
  purpose: 'PASSWORD_RESET';
  expiresAt: string;
}

export default function ForgotPasswordPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState<1 | 2 | 3 | 4>(1);
  const [email, setEmail] = useState('');
  const [code, setCode] = useState('');
  const [ticketId, setTicketId] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  /** 寄送密碼重設驗證碼。 */
  async function sendCode(event: FormEvent) {
    event.preventDefault();
    await run(async () => {
      await api('/auth/email/password-reset-code', {
        method: 'POST',
        body: JSON.stringify({ email }),
      });
      setMessage('密碼重設驗證碼已寄送。');
      setStep(2);
    });
  }

  /** 核銷驗證碼並取得密碼重設票券。 */
  async function verifyCode(event: FormEvent) {
    event.preventDefault();
    await run(async () => {
      const result = await api<TicketResult>('/auth/email/verify', {
        method: 'POST',
        body: JSON.stringify({ email, code, purpose: 'PASSWORD_RESET' }),
      });
      setTicketId(result.ticketId);
      setMessage('信箱驗證成功，請設定新密碼。');
      setStep(3);
    });
  }

  /** 以一次性票券更新密碼。 */
  async function resetPassword(event: FormEvent) {
    event.preventDefault();
    await run(async () => {
      await api('/auth/email/password-reset', {
        method: 'POST',
        body: JSON.stringify({ email, ticketId, password, confirmPassword }),
      });
      setMessage('密碼已更新，請使用新密碼登入。');
      setStep(4);
    });
  }

  /** 統一處理分步請求狀態。 */
  async function run(action: () => Promise<void>) {
    setLoading(true);
    setError('');
    try {
      await action();
    } catch (reason) {
      setError((reason as Error).message);
    } finally {
      setLoading(false);
    }
  }

  return <main className="auth-page">
    <section className="auth-visual">
      <div className="brand"><span className="brand-mark">A</span>AgentFlow</div>
      <div>
        <p className="eyebrow">Account recovery</p>
        <h1>透過信箱驗證，安全取回帳號。</h1>
        <p>驗證碼有效 10 分鐘，完成重設後即可使用新密碼登入。</p>
      </div>
      <small>© 2026 AgentFlow. Built for focused teams.</small>
    </section>

    <section className="auth-panel">
      <div className="auth-box">
        <p className="eyebrow">Reset password</p>
        <h2>忘記密碼</h2>
        {step < 4 && <div className="verification-steps" aria-label="重設進度">
          {[1, 2, 3].map(value =>
            <span key={value} className={step >= value ? 'active' : ''}>{value}</span>
          )}
        </div>}

        {step === 1 && <form className="auth-form" onSubmit={sendCode}>
          <label htmlFor="reset-email">註冊信箱
            <input id="reset-email" data-testid="reset-email" type="email" required value={email}
              onChange={event => setEmail(event.target.value)} />
          </label>
          <button data-testid="reset-send-code" className="btn primary" disabled={loading}>發送驗證碼</button>
        </form>}

        {step === 2 && <form className="auth-form" onSubmit={verifyCode}>
          <label htmlFor="reset-code">6 位數驗證碼
            <input id="reset-code" data-testid="reset-code" inputMode="numeric" pattern="\d{6}" maxLength={6}
              required value={code} onChange={event => setCode(event.target.value.replace(/\D/g, ''))} />
          </label>
          <button data-testid="reset-verify-code" className="btn primary" disabled={loading}>驗證信箱</button>
        </form>}

        {step === 3 && <form className="auth-form" onSubmit={resetPassword}>
          <label htmlFor="reset-password">新密碼
            <input id="reset-password" data-testid="reset-password" type="password" minLength={8} maxLength={72}
              required value={password} onChange={event => setPassword(event.target.value)} />
          </label>
          <label htmlFor="reset-confirm-password">確認新密碼
            <input id="reset-confirm-password" data-testid="reset-confirm-password" type="password" minLength={8}
              maxLength={72} required value={confirmPassword}
              onChange={event => setConfirmPassword(event.target.value)} />
          </label>
          <button data-testid="reset-submit" className="btn primary" disabled={loading}>更新密碼</button>
        </form>}

        {step === 4 && <button data-testid="reset-return-login" className="btn primary"
          onClick={() => navigate('/login')}>返回登入</button>}

        <div className="auth-status" aria-live="polite">
          {message && <p className="success" data-testid="reset-message">{message}</p>}
          {error && <p className="error" role="alert" data-testid="reset-error">{error}</p>}
        </div>
        {step < 4 && <button type="button" className="auth-text-link" onClick={() => navigate('/login')}>
          返回登入
        </button>}
      </div>
    </section>
  </main>;
}
