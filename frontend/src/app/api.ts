const API_BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api';

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem('token');
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}), ...options.headers },
  });
  const body = await response.json();
  if (!response.ok || !body.success) throw new Error(body.message ?? '請求失敗');
  return body.data as T;
}

