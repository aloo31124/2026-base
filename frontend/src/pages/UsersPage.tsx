import { useEffect, useState, type FormEvent } from 'react';
import { useAppDispatch, useAppSelector } from '../app/hooks';
import AppShell from '../components/AppShell';
import { assignManager, createUser, disableUser, fetchUsers, type UserRow } from '../features/users/usersSlice';

const blank = { fullName: '', username: '', email: '', password: '' };
export default function UsersPage() {
  const dispatch = useAppDispatch(); const { rows, status, error } = useAppSelector(s => s.users); const [tab, setTab] = useState<'users' | 'roles'>('users'); const [editing, setEditing] = useState(false); const [form, setForm] = useState(blank);
  useEffect(() => { void dispatch(fetchUsers()); }, [dispatch]);
  async function submit(event: FormEvent) { event.preventDefault(); const result = await dispatch(createUser(form)); if (createUser.fulfilled.match(result)) { setEditing(false); setForm(blank); } }
  return <AppShell><div className="content"><div className="tabs"><button className={tab === 'users' ? 'active' : ''} onClick={() => setTab('users')}>使用者</button><button className={tab === 'roles' ? 'active' : ''} onClick={() => setTab('roles')}>角色</button></div>
    {tab === 'users' ? <><header className="page-heading"><div><p className="eyebrow">User access management</p><h1>使用者</h1><p>管理可登入工作空間的成員與帳號狀態。</p></div><button data-testid="add-user" className="btn primary" onClick={() => setEditing(true)}>＋ 新增使用者</button></header>
      <div className="info-banner">由系統管理員新增的帳號會標記為「管理員新增」，並預設擁有員工角色。</div>
      {editing && <form className="card form-card" onSubmit={submit}><h2>新增使用者</h2><div className="form-grid"><label>姓名<input data-testid="full-name" required value={form.fullName} onChange={e => setForm({ ...form, fullName: e.target.value })} /></label><label>帳號<input data-testid="new-username" required value={form.username} onChange={e => setForm({ ...form, username: e.target.value })} /></label><label>信箱<input data-testid="email" type="email" required value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} /></label><label>初始密碼<input data-testid="new-password" type="password" minLength={8} required value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} /></label></div><div className="actions"><button type="button" className="btn secondary" onClick={() => setEditing(false)}>取消</button><button data-testid="save-user" className="btn primary">建立使用者</button></div></form>}
      <UserTable rows={rows} onDisable={id => void dispatch(disableUser(id))} />{status === 'loading' && <p>讀取中…</p>}{error && <p className="error">{error}</p>}</>
      : <><header className="page-heading"><div><p className="eyebrow">Role assignment</p><h1>角色</h1><p>每位使用者保有員工角色，可額外授予主管。</p></div></header><section className="card"><table><thead><tr><th>使用者</th><th>帳號</th><th>目前角色</th><th>操作</th></tr></thead><tbody>{rows.map(row => <tr key={row.id}><td>{row.fullName}</td><td>{row.username}</td><td>{row.roles.map(role => <span className="tag" key={role}>{role}</span>)}</td><td><button className="btn secondary" disabled={row.roles.includes('MANAGER')} onClick={() => void dispatch(assignManager(row.id))}>{row.roles.includes('MANAGER') ? '已授予' : '授予主管'}</button></td></tr>)}</tbody></table></section></>}
  </div></AppShell>;
}

function UserTable({ rows, onDisable }: { rows: UserRow[]; onDisable: (id: string) => void }) {
  return <section className="card table-card"><table><thead><tr><th>姓名</th><th>帳號</th><th>信箱</th><th>註冊方式</th><th>角色</th><th>操作</th></tr></thead><tbody>{rows.map(row => <tr key={row.id} data-testid={`user-${row.username}`}><td>{row.fullName}</td><td>{row.username}</td><td>{row.email}</td><td><span className="tag managed">{row.registrationMethod}</span></td><td>{row.roles.map(role => <span className="tag" key={role}>{role}</span>)}</td><td><button className="btn danger" disabled={!row.active} onClick={() => onDisable(row.id)}>{row.active ? '停用' : '已停用'}</button></td></tr>)}</tbody></table></section>;
}
