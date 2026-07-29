import { createAsyncThunk, createSlice, type PayloadAction } from '@reduxjs/toolkit';
import { api } from '../../app/api';

export interface Session { token: string; tokenType: string; username: string; fullName: string; roles: string[] }
interface AuthState { session: Session | null; status: 'idle' | 'loading' | 'failed'; error?: string }
export interface LineAuthorizeResponse { authorizationUrl: string; expiresAt: string }
export interface LineCallbackRequest { code?: string; state: string; error?: string; errorDescription?: string }
export interface EmailCodeResponse { expiresAt: string; resendAvailableAt: string }

const saved = localStorage.getItem('session');
const initialState: AuthState = { session: saved ? JSON.parse(saved) : null, status: 'idle' };

export const login = createAsyncThunk('auth/login', async (credentials: { username: string; password: string }) =>
  api<Session>('/auth/login', { method: 'POST', body: JSON.stringify(credentials) }));

export const requestLineAuthorization = createAsyncThunk('auth/lineAuthorize', async () =>
  api<LineAuthorizeResponse>('/auth/line/authorize'));

export const completeLineLogin = createAsyncThunk('auth/lineCallback', async (request: LineCallbackRequest) =>
  api<Session>('/auth/line/callback', { method: 'POST', body: JSON.stringify(request) }));
export const sendRegistrationCode = createAsyncThunk('auth/sendRegistrationCode', async (email: string) =>
  api<EmailCodeResponse>('/auth/email/registrations/code', { method: 'POST', body: JSON.stringify({ email }) }));
export const registerByEmail = createAsyncThunk('auth/registerByEmail', async (request: { fullName: string; email: string; password: string; code: string }) =>
  api<Session>('/auth/email/registrations', { method: 'POST', body: JSON.stringify(request) }));
export const sendPasswordResetCode = createAsyncThunk('auth/sendPasswordResetCode', async (email: string) =>
  api<EmailCodeResponse>('/auth/email/password-resets/code', { method: 'POST', body: JSON.stringify({ email }) }));
export const resetPassword = createAsyncThunk('auth/resetPassword', async (request: { email: string; newPassword: string; code: string }) =>
  api<void>('/auth/email/password-resets', { method: 'POST', body: JSON.stringify(request) }));

function saveSession(state: AuthState, session: Session) {
  state.status = 'idle';
  state.session = session;
  state.error = undefined;
  localStorage.setItem('session', JSON.stringify(session));
  localStorage.setItem('token', session.token);
}

const slice = createSlice({
  name: 'auth', initialState,
  reducers: {
    logout(state) { state.session = null; localStorage.removeItem('session'); localStorage.removeItem('token'); },
    clearAuthError(state) { state.error = undefined; if (state.status === 'failed') state.status = 'idle'; },
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
    .addCase(completeLineLogin.rejected, (state, action) => { state.status = 'failed'; state.error = action.error.message; })
    .addCase(sendRegistrationCode.pending, pending)
    .addCase(sendRegistrationCode.fulfilled, done)
    .addCase(sendRegistrationCode.rejected, failed)
    .addCase(registerByEmail.pending, pending)
    .addCase(registerByEmail.fulfilled, (state, action: PayloadAction<Session>) => saveSession(state, action.payload))
    .addCase(registerByEmail.rejected, failed)
    .addCase(sendPasswordResetCode.pending, pending)
    .addCase(sendPasswordResetCode.fulfilled, done)
    .addCase(sendPasswordResetCode.rejected, failed)
    .addCase(resetPassword.pending, pending)
    .addCase(resetPassword.fulfilled, done)
    .addCase(resetPassword.rejected, failed),
});
function pending(state: AuthState) { state.status = 'loading'; state.error = undefined; }
function done(state: AuthState) { state.status = 'idle'; state.error = undefined; }
function failed(state: AuthState, action: { error: { message?: string } }) { state.status = 'failed'; state.error = action.error.message; }
export const { logout, clearAuthError } = slice.actions;
export default slice.reducer;
