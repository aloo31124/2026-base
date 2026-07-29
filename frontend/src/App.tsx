import { Navigate, Route, Routes } from 'react-router-dom';
import { useAppSelector } from './app/hooks';
import LoginPage from './pages/LoginPage';
import UsersPage from './pages/UsersPage';
import TestPage from './pages/TestPage';
import UnauthorizedPage from './pages/UnauthorizedPage';

function Guard({ children, admin = false }: { children: React.ReactNode; admin?: boolean }) {
  const session = useAppSelector(s => s.auth.session);
  if (!session) return <Navigate to="/login" replace />;
  if (admin && !session.roles.includes('SYSTEM_ADMIN')) return <Navigate to="/unauthorized" replace />;
  return children;
}

export default function App() {
  return <Routes>
    <Route path="/login" element={<LoginPage />} />
    <Route path="/users" element={<Guard admin><UsersPage /></Guard>} />
    <Route path="/test/testTemp/" element={<Guard><TestPage /></Guard>} />
    <Route path="/unauthorized" element={<UnauthorizedPage />} />
    <Route path="*" element={<Navigate to="/users" replace />} />
  </Routes>;
}

