import { useState, type FormEvent } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../app/hooks';
import { resetPassword } from '../features/auth/authSlice';
import { AuthLayout } from './RegisterPage';

export default function ResetPasswordPage(){
  const [params]=useSearchParams();const [email,setEmail]=useState(params.get('email')??'');const [code,setCode]=useState('');const [newPassword,setNewPassword]=useState('');const dispatch=useAppDispatch();const navigate=useNavigate();const {status,error}=useAppSelector(s=>s.auth);
  async function submit(e:FormEvent){e.preventDefault();const r=await dispatch(resetPassword({email,code,newPassword}));if(resetPassword.fulfilled.match(r))navigate('/login?reset=success');}
  return <AuthLayout eyebrow="Account recovery" title="設定新密碼" hint="輸入信箱收到的驗證碼與新密碼。"><form className="auth-form" onSubmit={submit}><label>電子郵件<input data-testid="reset-email" type="email" required value={email} onChange={e=>setEmail(e.target.value)}/></label><label>驗證碼<input data-testid="reset-code" required maxLength={6} value={code} onChange={e=>setCode(e.target.value)}/></label><label>新密碼<input data-testid="reset-password" type="password" required minLength={8} value={newPassword} onChange={e=>setNewPassword(e.target.value)}/></label>{error&&<p className="error" role="alert">{error}</p>}<button data-testid="reset-submit" className="btn primary" disabled={status==='loading'}>更新密碼</button><Link className="auth-text-link" to="/login">返回登入</Link></form></AuthLayout>;
}
