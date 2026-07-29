import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../app/hooks';
import { sendPasswordResetCode } from '../features/auth/authSlice';
import { AuthLayout } from './RegisterPage';

export default function ForgotPasswordPage(){
  const [email,setEmail]=useState('');const dispatch=useAppDispatch();const navigate=useNavigate();const {status,error}=useAppSelector(s=>s.auth);
  async function submit(e:FormEvent){e.preventDefault();const r=await dispatch(sendPasswordResetCode(email));if(sendPasswordResetCode.fulfilled.match(r))navigate(`/reset-password?email=${encodeURIComponent(email)}`);}
  return <AuthLayout eyebrow="Account recovery" title="忘記密碼" hint="輸入信箱，我們會寄送一次性驗證碼。"><form className="auth-form" onSubmit={submit}><label>電子郵件<input data-testid="forgot-email" type="email" required value={email} onChange={e=>setEmail(e.target.value)}/></label>{error&&<p className="error" role="alert">{error}</p>}<button data-testid="forgot-submit" className="btn primary" disabled={status==='loading'}>寄送驗證碼</button><Link className="auth-text-link" to="/login">返回登入</Link></form></AuthLayout>;
}
