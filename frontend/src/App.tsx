import { Navigate, Route, Routes } from 'react-router-dom';
import { useAppSelector } from './app/hooks';
import LoginPage from './pages/LoginPage';
import UsersPage from './pages/UsersPage';
import TestPage from './pages/TestPage';
import UnauthorizedPage from './pages/UnauthorizedPage';
import LineOAuthCallbackPage from './pages/LineOAuthCallbackPage';
import RegisterPage from './pages/RegisterPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';

function Guard({ children, admin = false }: { children: React.ReactNode; admin?: boolean }) {
  const session = useAppSelector(s => s.auth.session);
  if (!session) return <Navigate to="/login" replace />;
  if (admin && !session.roles.includes('SYSTEM_ADMIN')) return <Navigate to="/unauthorized" replace />;
  return children;
}

export default function App() {
  return <Routes>
    <Route path="/login" element={<LoginPage />} />
    <Route path="/register" element={<RegisterPage />} />
    <Route path="/forgot-password" element={<ForgotPasswordPage />} />
    <Route path="/reset-password" element={<ResetPasswordPage />} />
    <Route path="/api/auth/line/callback" element={<LineOAuthCallbackPage />} />
    <Route path="/users" element={<Guard admin><UsersPage /></Guard>} />
    <Route path="/test/testTemp/" element={<Guard><TestPage /></Guard>} />
    <Route path="/unauthorized" element={<UnauthorizedPage />} />
    <Route path="*" element={<Navigate to="/users" replace />} />
  </Routes>;
}
