import { Navigate, Route, Routes } from 'react-router-dom';
import { useAppSelector } from './app/hooks';
import LoginPage from './pages/LoginPage';
import UsersPage from './pages/UsersPage';
import TestPage from './pages/TestPage';
import UnauthorizedPage from './pages/UnauthorizedPage';
import LineOAuthCallbackPage from './pages/LineOAuthCallbackPage';
import EmailVerificationPage from './pages/EmailVerificationPage';
import EmailRegistrationPage from './pages/EmailRegistrationPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import RegistrationManagementPage from './pages/RegistrationManagementPage';

function Guard({
  children,
  admin = false,
  unauthorizedMessage,
}: {
  children: React.ReactNode;
  admin?: boolean;
  unauthorizedMessage?: string;
}) {
  const session = useAppSelector(s => s.auth.session);
  if (!session) return <Navigate to="/login" replace />;
  if (admin && !session.roles.includes('SYSTEM_ADMIN')) {
    return <Navigate to="/unauthorized" replace state={{ message: unauthorizedMessage }} />;
  }
  return children;
}

export default function App() {
  return <Routes>
    <Route path="/login" element={<LoginPage />} />
    <Route path="/register" element={<EmailRegistrationPage />} />
    <Route path="/forgot-password" element={<ForgotPasswordPage />} />
    <Route path="/api/auth/line/callback" element={<LineOAuthCallbackPage />} />
    <Route path="/users" element={<Guard admin><UsersPage /></Guard>} />
    <Route path="/email-verification" element={<Guard admin><EmailVerificationPage /></Guard>} />
    <Route path="/registration-management" element={
      <Guard admin unauthorizedMessage="[註冊登入管理] [頁面] 無系統管理員權限。">
        <RegistrationManagementPage />
      </Guard>
    } />
    <Route path="/test/testTemp/" element={<Guard><TestPage /></Guard>} />
    <Route path="/unauthorized" element={<UnauthorizedPage />} />
    <Route path="*" element={<Navigate to="/users" replace />} />
  </Routes>;
}
