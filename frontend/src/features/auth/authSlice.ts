import { createAsyncThunk, createSlice, type PayloadAction } from '@reduxjs/toolkit';
import { api } from '../../app/api';
import { getSessionExpiresAt } from './sessionExpiry';

export interface Session { token: string; tokenType: string; username: string; fullName: string; roles: string[] }
interface AuthState { session: Session | null; status: 'idle' | 'loading' | 'failed'; error?: string; logoutReason?: 'session-expired' }
export interface LineAuthorizeResponse { authorizationUrl: string; expiresAt: string }
export interface LineCallbackRequest { code?: string; state: string; error?: string; errorDescription?: string }

function clearSavedSession() {
  localStorage.removeItem('session');
  localStorage.removeItem('token');
}

function loadSavedSession(): Session | null {
  const saved = localStorage.getItem('session');
  if (!saved) return null;

  try {
    const session = JSON.parse(saved) as Session;
    if (!session?.token) {
      clearSavedSession();
      return null;
    }
    const expiresAt = getSessionExpiresAt(session.token);
    if (expiresAt !== null && expiresAt <= Date.now()) {
      clearSavedSession();
      return null;
    }
    return session;
  } catch {
    clearSavedSession();
    return null;
  }
}

const initialState: AuthState = { session: loadSavedSession(), status: 'idle' };

export const login = createAsyncThunk('auth/login', async (credentials: { username: string; password: string }) =>
  api<Session>('/auth/login', { method: 'POST', body: JSON.stringify(credentials) }));

export const requestLineAuthorization = createAsyncThunk('auth/lineAuthorize', async () =>
  api<LineAuthorizeResponse>('/auth/line/authorize'));

export const completeLineLogin = createAsyncThunk('auth/lineCallback', async (request: LineCallbackRequest) =>
  api<Session>('/auth/line/callback', { method: 'POST', body: JSON.stringify(request) }));

function saveSession(state: AuthState, session: Session) {
  state.status = 'idle';
  state.session = session;
  state.error = undefined;
  state.logoutReason = undefined;
  localStorage.setItem('session', JSON.stringify(session));
  localStorage.setItem('token', session.token);
}

const slice = createSlice({
  name: 'auth', initialState,
  reducers: {
    logout(state, action: PayloadAction<'session-expired' | undefined>) { state.session = null; state.logoutReason = action.payload; clearSavedSession(); },
    clearAuthError(state) { state.error = undefined; if (state.status === 'failed') state.status = 'idle'; },
    acceptSession(state, action: PayloadAction<Session>) { saveSession(state, action.payload); },
  },
  extraReducers: (builder) => builder
    .addCase(login.pending, (state) => { state.status = 'loading'; state.error = undefined; })
    .addCase(login.fulfilled, (state, action: PayloadAction<Session>) => saveSession(state, action.payload))
    .addCase(login.rejected, (state, action) => { state.status = 'failed'; state.error = action.error.message; })
    .addCase(requestLineAuthorization.pending, (state) => { state.status = 'loading'; state.error = undefined; })
    .addCase(requestLineAuthorization.fulfilled, (state) => { state.status = 'idle'; })
    .addCase(requestLineAuthorization.rejected, (state, action) => { state.status = 'failed'; state.error = action.error.message; })
    .addCase(completeLineLogin.pending, (state) => { state.status = 'loading'; state.error = undefined; })
    .addCase(completeLineLogin.fulfilled, (state, action: PayloadAction<Session>) => saveSession(state, action.payload))
    .addCase(completeLineLogin.rejected, (state, action) => { state.status = 'failed'; state.error = action.error.message; }),
});
export const { logout, clearAuthError, acceptSession } = slice.actions;
export default slice.reducer;
