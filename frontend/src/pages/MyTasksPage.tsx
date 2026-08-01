import { FormEvent, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../app/api';
import AppShell from '../components/AppShell';
import ActionIconButton from '../components/ActionIconButton';

export type TaskAttachment = { id: string; fileName: string; contentType: string; fileSize: number; createdAt: string };
export type MyTask = {
  id: string; name: string; content?: string; deadline: string; creatorName: string;
  assignedAt: string; status: string; workStatus: string; progressContent?: string;
  progressPercent: number; extensionReason?: string; attachments: TaskAttachment[];
};

export default function MyTasksPage() {
  const navigate = useNavigate();
  const [tasks, setTasks] = useState<MyTask[]>([]);
  const [name, setName] = useState('');
  const [assignedFrom, setAssignedFrom] = useState('');
  const [assignedTo, setAssignedTo] = useState('');
  const [deadlineFrom, setDeadlineFrom] = useState('');
  const [deadlineTo, setDeadlineTo] = useState('');
  const [sortBy, setSortBy] = useState('assignedAt');
  const [direction, setDirection] = useState('desc');
  const [error, setError] = useState('');

  /** 載入符合目前篩選與排序條件的本人任務。 */
  const load = async () => {
    const params = new URLSearchParams({ name, sortBy, direction });
    if (assignedFrom) params.set('assignedFrom', new Date(`${assignedFrom}T00:00:00`).toISOString());
    if (assignedTo) params.set('assignedTo', new Date(`${assignedTo}T23:59:59`).toISOString());
    if (deadlineFrom) params.set('deadlineFrom', new Date(`${deadlineFrom}T00:00:00`).toISOString());
    if (deadlineTo) params.set('deadlineTo', new Date(`${deadlineTo}T23:59:59`).toISOString());
    try { setTasks(await api<MyTask[]>(`/task-assignment/inbox?${params}`)); setError(''); }
    catch (reason) { setError((reason as Error).message); }
  };

  useEffect(() => { void load(); }, []);

  /** 從列表執行提交、退回或延期操作。 */
  const action = async (task: MyTask, kind: 'submit' | 'return' | 'extend') => {
    try {
      if (kind === 'submit') await api(`/task-assignment/tasks/${task.id}/submit`, { method: 'POST' });
      if (kind === 'return') {
        const reason = window.prompt('請輸入退回原因'); if (!reason) return;
        await api(`/task-assignment/tasks/${task.id}/return`, { method: 'POST', body: JSON.stringify({ reason }) });
      }
      if (kind === 'extend') {
        const reason = window.prompt('請輸入申請延期原因'); if (!reason) return;
        await api(`/task-assignment/tasks/${task.id}/extension-requests`, { method: 'POST', body: JSON.stringify({ reason }) });
      }
      await load();
    } catch (reason) { setError((reason as Error).message); }
  };

  /** 套用使用者輸入的列表查詢條件。 */
  const search = (event: FormEvent) => { event.preventDefault(); void load(); };

  return <AppShell><div className="content my-tasks-content">
    <header className="page-heading"><div><div className="eyebrow">My tasks</div><h1>我的任務</h1><p>查詢接收任務、更新進度並送交指派者。</p></div></header>
    <form className="card my-task-filter" onSubmit={search}>
      <label>任務名稱<input data-testid="my-task-search-name" value={name} onChange={e => setName(e.target.value)} /></label>
      <label>指派起日<input type="date" value={assignedFrom} onChange={e => setAssignedFrom(e.target.value)} /></label>
      <label>指派迄日<input type="date" value={assignedTo} onChange={e => setAssignedTo(e.target.value)} /></label>
      <label>期限起日<input type="date" value={deadlineFrom} onChange={e => setDeadlineFrom(e.target.value)} /></label>
      <label>期限迄日<input type="date" value={deadlineTo} onChange={e => setDeadlineTo(e.target.value)} /></label>
      <label>排序<select data-testid="my-task-sort" value={sortBy} onChange={e => setSortBy(e.target.value)}><option value="assignedAt">指派日期</option><option value="deadline">期限</option><option value="name">名稱</option><option value="status">狀態</option></select></label>
      <label>方向<select value={direction} onChange={e => setDirection(e.target.value)}><option value="desc">新到舊</option><option value="asc">舊到新</option></select></label>
      <button className="btn primary" data-testid="my-task-search" type="submit">查詢</button>
    </form>
    <section className="card table-card"><table><thead><tr><th>任務</th><th>指派者</th><th>指派日期</th><th>期限</th><th>工作狀態</th><th>進度</th><th>操作</th></tr></thead><tbody>
      {tasks.map(task => <tr data-testid="my-task-row" key={task.id}><td data-label="任務"><strong>{task.name}</strong></td><td data-label="指派者">{task.creatorName}</td><td data-label="指派日期">{new Date(task.assignedAt).toLocaleString()}</td><td data-label="期限">{new Date(task.deadline).toLocaleString()}</td><td data-label="工作狀態"><span className={`status ${task.workStatus.toLowerCase()}`}>{task.workStatus}</span></td><td data-label="進度">{task.progressPercent}%</td><td data-label="操作"><div className="icon-actions"><ActionIconButton label="提交審核" icon="✓" onClick={() => void action(task, 'submit')} /><ActionIconButton label="退回" icon="↩" tone="danger" onClick={() => void action(task, 'return')} /><ActionIconButton label="申請延期" icon="◷" onClick={() => void action(task, 'extend')} /><ActionIconButton label="編輯" icon="✎" data-testid="my-task-edit" onClick={() => navigate(`/my-tasks/${task.id}`)} /></div></td></tr>)}
    </tbody></table>{tasks.length === 0 && <p className="empty-state">目前沒有符合條件的任務。</p>}</section>
    {error && <p className="management-status error" data-testid="my-task-error">{error}</p>}
  </div></AppShell>;
}
