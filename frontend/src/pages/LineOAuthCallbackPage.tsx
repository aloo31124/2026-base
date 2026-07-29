import { useEffect, useRef } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../app/hooks';
import { clearAuthError, completeLineLogin } from '../features/auth/authSlice';

export default function LineOAuthCallbackPage() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const started = useRef(false);
  const status = useAppSelector(state => state.auth.status);
  const error = useAppSelector(state => state.auth.error);

  useEffect(() => {
    if (started.current) return;
    started.current = true;
    dispatch(clearAuthError());
    const state = params.get('state') ?? '';
    if (!state) return;
    void dispatch(completeLineLogin({
      code: params.get('code') ?? undefined,
      state,
      error: params.get('error') ?? params.get('errorCode') ?? undefined,
      errorDescription: params.get('error_description') ?? params.get('errorMessage') ?? undefined,
    })).then(result => {
      if (completeLineLogin.fulfilled.match(result)) {
        navigate(result.payload.roles.includes('SYSTEM_ADMIN') ? '/users' : '/test/testTemp/', { replace: true });
      }
    });
  }, [dispatch, navigate, params]);

  const missingState = !params.get('state');
  const message = missingState ? 'LINE callback 缺少 state，請重新登入。' : error;
  return <main className="oauth-callback-page"><section className="card oauth-callback-card" aria-live="polite">
    <span className="line-callback-mark">LINE</span>
    {status === 'loading' && !message ? <>
      <div className="oauth-spinner" aria-hidden="true" />
      <p className="eyebrow">Verifying identity</p>
      <h1 data-testid="line-callback-loading">正在完成 LINE 登入</h1>
      <p>請稍候，我們正在安全驗證回傳資料並建立工作階段。</p>
    </> : <>
      <p className="eyebrow">Login not completed</p>
      <h1 data-testid="line-callback-error">無法完成 LINE 登入</h1>
      <p className="error" role="alert">{message ?? 'LINE 登入未完成，請重新操作。'}</p>
      <Link className="btn primary" to="/login">回到登入頁</Link>
    </>}
  </section></main>;
}
