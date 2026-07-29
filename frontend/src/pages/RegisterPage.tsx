import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../app/hooks';
import { registerByEmail, sendRegistrationCode } from '../features/auth/authSlice';

export default function RegisterPage() {
  const dispatch=useAppDispatch(); const navigate=useNavigate(); const {status,error}=useAppSelector(s=>s.auth);
  const [form,setForm]=useState({fullName:'',email:'',password:'',code:''}); const [sent,setSent]=useState(false);
  async function send(){const r=await dispatch(sendRegistrationCode(form.email)); if(sendRegistrationCode.fulfilled.match(r)) setSent(true);}
  async function submit(e:FormEvent){e.preventDefault();const r=await dispatch(registerByEmail(form));if(registerByEmail.fulfilled.match(r))navigate('/test/testTemp/');}
  return <AuthLayout eyebrow="Create account" title="建立信箱帳號" hint="驗證信箱後即可安全登入 AgentFlow。">
    <form className="auth-form" onSubmit={submit}>
      <label>姓名<input data-testid="register-name" required maxLength={80} value={form.fullName} onChange={e=>setForm({...form,fullName:e.target.value})}/></label>
      <label>電子郵件<input data-testid="register-email" required type="email" value={form.email} onChange={e=>setForm({...form,email:e.target.value})}/></label>
      <label>密碼<input data-testid="register-password" required type="password" minLength={8} value={form.password} onChange={e=>setForm({...form,password:e.target.value})}/></label>
      <div className="code-row"><label>驗證碼<input data-testid="register-code" required inputMode="numeric" maxLength={6} value={form.code} onChange={e=>setForm({...form,code:e.target.value})}/></label><button data-testid="send-register-code" type="button" className="btn secondary" disabled={status==='loading'||!form.email} onClick={()=>void send()}>{sent?'重新寄送':'寄送驗證碼'}</button></div>
      {error&&<p className="error" role="alert">{error}</p>}<button data-testid="register-submit" className="btn primary" disabled={status==='loading'}>完成註冊</button>
      <Link className="auth-text-link" to="/login">已有帳號？返回登入</Link>
    </form>
  </AuthLayout>;
}

export function AuthLayout({eyebrow,title,hint,children}:{eyebrow:string;title:string;hint:string;children:React.ReactNode}){
  return <main className="auth-page"><section className="auth-visual"><div className="brand"><span className="brand-mark">A</span>AgentFlow</div><div><p className="eyebrow">Secure workspace</p><h1>讓權限清楚，讓工作流保持流動。</h1><p>以安全驗證與角色授權保護每一個管理操作。</p></div><small>© 2026 AgentFlow.</small></section><section className="auth-panel"><div className="auth-box"><p className="eyebrow">{eyebrow}</p><h2>{title}</h2><p className="auth-hint">{hint}</p>{children}</div></section></main>;
}
