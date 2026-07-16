import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { api } from '../../app/api';

export interface UserRow { id: string; fullName: string; username: string; email: string; registrationMethod: string; active: boolean; roles: string[] }
interface UsersState { rows: UserRow[]; status: 'idle' | 'loading' | 'failed'; error?: string }
const initialState: UsersState = { rows: [], status: 'idle' };

export const fetchUsers = createAsyncThunk('users/fetch', () => api<UserRow[]>('/admin/users'));
export const createUser = createAsyncThunk('users/create', (payload: { fullName: string; username: string; email: string; password: string }) => api<UserRow>('/admin/users', { method: 'POST', body: JSON.stringify(payload) }));
export const disableUser = createAsyncThunk('users/disable', (id: string) => api<UserRow>(`/admin/users/${id}/disable`, { method: 'PATCH' }));
export const assignManager = createAsyncThunk('users/manager', (id: string) => api<UserRow>(`/admin/users/${id}/roles`, { method: 'POST', body: JSON.stringify({ roleCode: 'MANAGER' }) }));

const slice = createSlice({
  name: 'users', initialState, reducers: {},
  extraReducers: (builder) => builder
    .addCase(fetchUsers.pending, (state) => { state.status = 'loading'; })
    .addCase(fetchUsers.fulfilled, (state, action) => { state.status = 'idle'; state.rows = action.payload; })
    .addCase(fetchUsers.rejected, (state, action) => { state.status = 'failed'; state.error = action.error.message; })
    .addCase(createUser.fulfilled, (state, action) => { state.rows.push(action.payload); })
    .addCase(disableUser.fulfilled, (state, action) => { state.rows = state.rows.map(row => row.id === action.payload.id ? action.payload : row); })
    .addCase(assignManager.fulfilled, (state, action) => { state.rows = state.rows.map(row => row.id === action.payload.id ? action.payload : row); }),
});
export default slice.reducer;

