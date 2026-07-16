import { createAsyncThunk, createSlice, type PayloadAction } from '@reduxjs/toolkit';
import { api } from '../../app/api';

export interface Session { token: string; tokenType: string; username: string; fullName: string; roles: string[] }
interface AuthState { session: Session | null; status: 'idle' | 'loading' | 'failed'; error?: string }

const saved = localStorage.getItem('session');
const initialState: AuthState = { session: saved ? JSON.parse(saved) : null, status: 'idle' };

export const login = createAsyncThunk('auth/login', async (credentials: { username: string; password: string }) =>
  api<Session>('/auth/login', { method: 'POST', body: JSON.stringify(credentials) }));

const slice = createSlice({
  name: 'auth', initialState,
  reducers: { logout(state) { state.session = null; localStorage.removeItem('session'); localStorage.removeItem('token'); } },
  extraReducers: (builder) => builder
    .addCase(login.pending, (state) => { state.status = 'loading'; state.error = undefined; })
    .addCase(login.fulfilled, (state, action: PayloadAction<Session>) => { state.status = 'idle'; state.session = action.payload; localStorage.setItem('session', JSON.stringify(action.payload)); localStorage.setItem('token', action.payload.token); })
    .addCase(login.rejected, (state, action) => { state.status = 'failed'; state.error = action.error.message; }),
});
export const { logout } = slice.actions;
export default slice.reducer;

