import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../app/api';
import PasswordInput from '../components/PasswordInput';
import { useAppDispatch } from '../app/hooks';
import { acceptSession, type Session } from '../features/auth/authSlice';

interface MailResult {
  maskedRecipient: string;
  sentAt: string;
}

interface TicketResult {
  ticketId: string;
  purpose: 'REGISTRATION';
  expiresAt: string;
}

export default function EmailRegistrationPage() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const [step, setStep] = useState<1 | 2 | 3>(1);
  const [email, setEmail] = useState('');
  const [code, setCode] = useState('');
  const [ticketId, setTicketId] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  /** 檢查首次信箱並要求註冊驗證碼。 */
  async function sendCode(event: FormEvent) {
    event.preventDefault();
    await run(async () => {
      const result = await api<MailResult>('/auth/email/registration-code', {
        method: 'POST',
        body: JSON.stringify({ email }),
      });
      setMessage(`驗證碼已寄送至 ${result.maskedRecipient}`);
      setStep(2);
    });
  }

  /** 核銷六位數驗證碼並保存一次性票券。 */
  async function verifyCode(event: FormEvent) {
    event.preventDefault();
    await run(async () => {
      const result = await api<TicketResult>('/auth/email/verify', {
        method: 'POST',
        body: JSON.stringify({ email, code, purpose: 'REGISTRATION' }),
      });
      setTicketId(result.ticketId);
      setMessage('信箱驗證成功，請設定登入密碼。');
      setStep(3);
    });
  }

  /** 建立信箱帳號、保存工作階段並進入系統首頁。 */
  async function completeRegistration(event: FormEvent) {
    event.preventDefault();
    await run(async () => {
      const session = await api<Session>('/auth/email/register', {
        method: 'POST',
        body: JSON.stringify({ email, ticketId, password, confirmPassword }),
      });
      dispatch(acceptSession(session));
      navigate('/test/testTemp/');
    });
  }

  /** 統一管理分步請求的載入、成功與失敗狀態。 */
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
        <p className="eyebrow">Email registration</p>
        <h1>驗證你的信箱，開始安全工作。</h1>
        <p>三個步驟完成信箱驗證、密碼設定與帳號建立。</p>
        <div className="workflow-preview">
          <span className="workflow-node">確認信箱</span><i className="workflow-line" />
          <span className="workflow-node">核對驗證碼</span><i className="workflow-line" />
          <span className="workflow-node">設定密碼</span>
        </div>
      </div>
      <small>© 2026 AgentFlow. Built for focused teams.</small>
    </section>

    <section className="auth-panel">
      <div className="auth-box">
        <p className="eyebrow">Create account</p>
        <h2>信箱註冊</h2>
        <div className="verification-steps" aria-label="註冊進度">
          {[1, 2, 3].map(value =>
            <span key={value} className={step >= value ? 'active' : ''}>{value}</span>
          )}
        </div>

        {step === 1 && <form className="auth-form" onSubmit={sendCode}>
          <label htmlFor="registration-email">信箱
            <input
              id="registration-email"
              data-testid="registration-email"
              type="email"
              required
              maxLength={160}
              autoComplete="email"
              value={email}
              onChange={event => setEmail(event.target.value)}
            />
          </label>
          <button data-testid="registration-send-code" className="btn primary" disabled={loading}>
            {loading ? '寄送中…' : '發送驗證碼'}
          </button>
        </form>}

        {step === 2 && <form className="auth-form" onSubmit={verifyCode}>
          <label htmlFor="registration-code">6 位數驗證碼
            <input
              id="registration-code"
              data-testid="registration-code"
              inputMode="numeric"
              pattern="\d{6}"
              maxLength={6}
              required
              value={code}
              onChange={event => setCode(event.target.value.replace(/\D/g, ''))}
            />
          </label>
          <button data-testid="registration-verify-code" className="btn primary" disabled={loading}>
            {loading ? '驗證中…' : '驗證信箱'}
          </button>
        </form>}

        {step === 3 && <form className="auth-form" onSubmit={completeRegistration}>
          <label htmlFor="registration-password">密碼
            <PasswordInput
              id="registration-password"
              data-testid="registration-password"
              minLength={8}
              maxLength={72}
              required
              autoComplete="new-password"
              value={password}
              onChange={event => setPassword(event.target.value)}
            />
          </label>
          <label htmlFor="registration-confirm-password">確認密碼
            <PasswordInput
              id="registration-confirm-password"
              data-testid="registration-confirm-password"
              minLength={8}
              maxLength={72}
              required
              autoComplete="new-password"
              value={confirmPassword}
              onChange={event => setConfirmPassword(event.target.value)}
            />
          </label>
          <button data-testid="registration-submit" className="btn primary" disabled={loading}>
            {loading ? '建立中…' : '完成註冊並進入系統'}
          </button>
        </form>}

        <div className="auth-status" aria-live="polite">
          {message && <p className="success" data-testid="registration-message">{message}</p>}
          {error && <p className="error" role="alert" data-testid="registration-error">{error}</p>}
        </div>
        <button type="button" className="auth-text-link" onClick={() => navigate('/login')}>
          已有帳號？返回登入
        </button>
      </div>
    </section>
  </main>;
}
