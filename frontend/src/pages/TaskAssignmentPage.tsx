import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react';
import { api } from '../app/api';
import AppShell from '../components/AppShell';
import ActionIconButton from '../components/ActionIconButton';

type Tab = 'tasks' | 'members' | 'inbox';
interface Context { userId: string; username: string; roles: string[]; companyId?: string; companyName?: string }
interface Employee { userId: string; fullName: string; username: string; email: string; bindingId?: string }
interface Binding { id: string; employeeId: string; employeeName: string; employeeEmail: string }
interface Assignee { userId: string; fullName: string; username: string; type: string }
interface Task {
  id: string; name: string; content?: string; deadline: string; assigneeId: string;
  assigneeName: string; assigneeUsername: string; creatorName: string; assignedAt: string;
  status: 'ASSIGNED' | 'RETURNED' | 'WITHDRAWN'; returnReason?: string;
}

const futureLocal = () => {
  const date = new Date(Date.now() + 24 * 60 * 60 * 1000);
  date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
  return date.toISOString().slice(0, 16);
};

export default function TaskAssignmentPage() {
  const [context, setContext] = useState<Context>();
  const [tab, setTab] = useState<Tab>('inbox');
  const [companyName, setCompanyName] = useState('');
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [bindings, setBindings] = useState<Binding[]>([]);
  const [assignees, setAssignees] = useState<Assignee[]>([]);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [inbox, setInbox] = useState<Task[]>([]);
  const [employeeEmail, setEmployeeEmail] = useState('');
  const [taskForm, setTaskForm] = useState({ id: '', name: '', content: '', deadline: futureLocal(), assigneeId: '' });
  const [filters, setFilters] = useState({ name: '', assignee: '', sortBy: 'assignedAt', direction: 'desc' });
  const [returnReasons, setReturnReasons] = useState<Record<string, string>>({});
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const isManager = context?.roles.includes('MANAGER') ?? false;

  /** 載入登入情境與收件匣。 */
  const loadContext = useCallback(async () => {
    const current = await api<Context>('/task-assignment/context');
    setContext(current);
    setTab(current.roles.includes('MANAGER') ? 'tasks' : 'inbox');
    setInbox(await api<Task[]>('/task-assignment/inbox'));
    return current;
  }, []);

  /** 載入主管專用清單。 */
  const loadManagerData = useCallback(async () => {
    const [bindingRows, assigneeRows, taskRows] = await Promise.all([
      api<Binding[]>('/task-assignment/employee-bindings'),
      api<Assignee[]>('/task-assignment/assignees'),
      api<Task[]>('/task-assignment/tasks?sortBy=assignedAt&direction=desc'),
    ]);
    setBindings(bindingRows);
    setAssignees(assigneeRows);
    setTasks(taskRows);
  }, []);

  /** 統一重新整理並顯示結果。 */
  const refresh = useCallback(async () => {
    setError('');
    try {
      const current = await loadContext();
      if (current.companyId && current.roles.includes('MANAGER')) await loadManagerData();
    } catch (reason) {
      setError((reason as Error).message);
    }
  }, [loadContext, loadManagerData]);

  useEffect(() => { void refresh(); }, [refresh]);

  /** 執行異動後重新整理。 */
  async function mutate(action: () => Promise<unknown>, success: string) {
    setError(''); setMessage('');
    try {
      await action();
      setMessage(success);
      await refresh();
    } catch (reason) {
      setError((reason as Error).message);
    }
  }

  /** 依既有公司名稱建立自身公司綁定。 */
  async function bindCompany(event: FormEvent) {
    event.preventDefault();
    await mutate(() => api('/task-assignment/company-bindings', {
      method: 'POST', body: JSON.stringify({ companyName }),
    }), '公司綁定成功。');
  }

  /** 依員工信箱搜尋同公司員工。 */
  async function searchEmployees(event: FormEvent) {
    event.preventDefault();
    setError('');
    try {
      setEmployees(await api<Employee[]>(`/task-assignment/employees?email=${encodeURIComponent(employeeEmail)}`));
    } catch (reason) {
      setError((reason as Error).message);
    }
  }

  /** 建立主管與員工綁定。 */
  async function bindEmployee(employeeId: string) {
    await mutate(() => api('/task-assignment/employee-bindings', {
      method: 'POST', body: JSON.stringify({ employeeId }),
    }), '員工已綁定至目前主管。');
  }

  /** 儲存新增或編輯任務。 */
  async function saveTask(event: FormEvent) {
    event.preventDefault();
    const editing = Boolean(taskForm.id);
    await mutate(() => api(
      editing ? `/task-assignment/tasks/${taskForm.id}` : '/task-assignment/tasks',
      {
        method: editing ? 'PUT' : 'POST',
        body: JSON.stringify({
          name: taskForm.name,
          content: taskForm.content,
          deadline: new Date(taskForm.deadline).toISOString(),
          assigneeId: taskForm.assigneeId,
        }),
      },
    ), editing ? '任務已更新並重新指派。' : '任務指派成功。');
    setTaskForm({ id: '', name: '', content: '', deadline: futureLocal(), assigneeId: '' });
  }

  /** 依列表條件查詢任務。 */
  async function searchTasks(event: FormEvent) {
    event.preventDefault();
    const query = new URLSearchParams(filters);
    setTasks(await api<Task[]>(`/task-assignment/tasks?${query}`));
  }

  /** 由受派人退回任務。 */
  async function returnTask(taskId: string) {
    await mutate(() => api(`/task-assignment/tasks/${taskId}/return`, {
      method: 'POST', body: JSON.stringify({ reason: returnReasons[taskId] ?? '' }),
    }), '任務已退回。');
  }

  const activeTasks = useMemo(() => inbox.filter(task => task.status === 'ASSIGNED'), [inbox]);

  return <AppShell>
    <div className="content task-assignment-content">
      <header className="page-heading">
        <div><p className="eyebrow">Task assignment</p><h1>任務指派</h1>
          <p>{context?.companyName ? `${context.companyName} · ${context.username}` : '先綁定公司後開始協作。'}</p>
        </div>
        <button className="btn secondary" type="button" onClick={() => void refresh()}>重新整理</button>
      </header>

      {!context?.companyId && <form className="card company-onboarding" onSubmit={bindCompany}>
        <div><p className="eyebrow">Company onboarding</p><h2>輸入公司名稱</h2><p>請輸入管理員已建立的完整公司名稱。</p></div>
        <label>公司名稱<input data-testid="onboarding-company" required value={companyName} onChange={event => setCompanyName(event.target.value)} /></label>
        <button data-testid="onboarding-submit" className="btn primary">綁定公司</button>
      </form>}

      {context?.companyId && <>
        <div className="tabs management-tabs" role="tablist" aria-label="任務指派標籤">
          {isManager && <button data-testid="task-management-tab" className={tab === 'tasks' ? 'active' : ''} onClick={() => setTab('tasks')}>任務管理</button>}
          {isManager && <button data-testid="member-binding-tab" className={tab === 'members' ? 'active' : ''} onClick={() => setTab('members')}>員工綁定</button>}
          <button data-testid="task-inbox-tab" className={tab === 'inbox' ? 'active' : ''} onClick={() => setTab('inbox')}>我的任務</button>
        </div>

        {tab === 'members' && isManager && <section className="management-grid">
          <form className="card management-form" onSubmit={searchEmployees}>
            <div><p className="eyebrow">Member binding</p><h2>依信箱搜尋員工</h2></div>
            <label>員工信箱<input data-testid="employee-email-search" type="email" value={employeeEmail} onChange={event => setEmployeeEmail(event.target.value)} /></label>
            <button data-testid="employee-search-submit" className="btn primary">搜尋</button>
            <div className="candidate-list">{employees.map(employee => <div className="candidate-row" key={employee.userId}>
              <span><strong>{employee.fullName}</strong><small>{employee.email}</small></span>
              <button data-testid="employee-bind" className="btn secondary" type="button" disabled={Boolean(employee.bindingId)} onClick={() => void bindEmployee(employee.userId)}>
                {employee.bindingId ? '已綁定' : '綁定'}
              </button>
            </div>)}</div>
          </form>
          <div className="card management-list"><table><thead><tr><th>員工</th><th>信箱</th><th>操作</th></tr></thead>
            <tbody>{bindings.map(binding => <tr data-testid="employee-binding-row" key={binding.id}>
              <td data-label="員工">{binding.employeeName}</td><td data-label="信箱">{binding.employeeEmail}</td>
              <td data-label="操作"><div className="table-actions"><ActionIconButton label="取消員工綁定" icon="×" tone="danger" onClick={() => void mutate(
                () => api(`/task-assignment/employee-bindings/${binding.id}`, { method: 'DELETE' }),
                '員工綁定已取消。',
              )} /></div></td>
            </tr>)}</tbody></table>
            {bindings.length === 0 && <p className="empty-state">尚未綁定員工。</p>}
          </div>
        </section>}

        {tab === 'tasks' && isManager && <section className="task-workspace">
          <form className="card management-form task-form" onSubmit={saveTask}>
            <div><p className="eyebrow">Assignment editor</p><h2>{taskForm.id ? '修改任務' : '指派新任務'}</h2></div>
            <label>任務名稱<input data-testid="task-name" required maxLength={160} value={taskForm.name} onChange={event => setTaskForm({ ...taskForm, name: event.target.value })} /></label>
            <label>任務內容<textarea data-testid="task-content" maxLength={4000} value={taskForm.content} onChange={event => setTaskForm({ ...taskForm, content: event.target.value })} /></label>
            <label>期限<input data-testid="task-deadline" required type="datetime-local" value={taskForm.deadline} onChange={event => setTaskForm({ ...taskForm, deadline: event.target.value })} /></label>
            <label>受派人<select data-testid="task-assignee" required value={taskForm.assigneeId} onChange={event => setTaskForm({ ...taskForm, assigneeId: event.target.value })}>
              <option value="">請選擇</option>{assignees.map(row => <option key={row.userId} value={row.userId}>{row.fullName}（{row.username}）</option>)}
            </select></label>
            <div className="actions">{taskForm.id && <button type="button" className="btn secondary" onClick={() => setTaskForm({ id: '', name: '', content: '', deadline: futureLocal(), assigneeId: '' })}>取消</button>}
              <button data-testid="task-save" className="btn primary">{taskForm.id ? '儲存並重新指派' : '指派任務'}</button></div>
          </form>
          <div className="card management-list">
            <form className="task-filter" onSubmit={searchTasks}>
              <input data-testid="task-search-name" placeholder="任務名稱" value={filters.name} onChange={event => setFilters({ ...filters, name: event.target.value })} />
              <input data-testid="task-search-assignee" placeholder="受派人帳號" value={filters.assignee} onChange={event => setFilters({ ...filters, assignee: event.target.value })} />
              <select data-testid="task-sort" value={filters.sortBy} onChange={event => setFilters({ ...filters, sortBy: event.target.value })}><option value="assignedAt">指派日期</option><option value="deadline">期限</option><option value="name">名稱</option><option value="assignee">受派人</option></select>
              <select value={filters.direction} onChange={event => setFilters({ ...filters, direction: event.target.value })}><option value="desc">降冪</option><option value="asc">升冪</option></select>
              <button data-testid="task-search-submit" className="btn secondary">查詢</button>
            </form>
            <table><thead><tr><th>任務</th><th>受派人</th><th>期限</th><th>狀態</th><th>操作</th></tr></thead>
              <tbody>{tasks.map(task => <tr data-testid="task-row" key={task.id}><td data-label="任務"><strong>{task.name}</strong><small>{task.content || '—'}</small></td>
                <td data-label="受派人">{task.assigneeName}<small>{task.assigneeUsername}</small></td><td data-label="期限">{new Date(task.deadline).toLocaleString()}</td><td data-label="狀態"><span className={`status ${task.status.toLowerCase()}`}>{task.status}</span>{task.returnReason && <small>{task.returnReason}</small>}</td>
                <td data-label="操作" className="row-actions"><ActionIconButton label="修改任務" icon="✎" disabled={task.status === 'WITHDRAWN'} onClick={() => setTaskForm({ id: task.id, name: task.name, content: task.content ?? '', deadline: new Date(task.deadline).toISOString().slice(0, 16), assigneeId: task.assigneeId })} />
                  <ActionIconButton label="撤回任務" icon="↩" tone="danger" disabled={task.status !== 'ASSIGNED'} onClick={() => void mutate(() => api(`/task-assignment/tasks/${task.id}/withdraw`, { method: 'POST' }), '任務已撤回。')} />
                  <ActionIconButton label="刪除任務" icon="×" tone="danger" disabled={task.status === 'RETURNED'} onClick={() => void mutate(() => api(`/task-assignment/tasks/${task.id}`, { method: 'DELETE' }), '任務已刪除。')} /></td></tr>)}</tbody>
            </table>{tasks.length === 0 && <p className="empty-state">尚無符合條件的任務。</p>}
          </div>
        </section>}

        {tab === 'inbox' && <div className="card management-list">
          <div className="table-toolbar"><div><p className="eyebrow">Inbox</p><h2>我的任務</h2></div><span>{activeTasks.length} 筆進行中</span></div>
          <table><thead><tr><th>任務</th><th>指派人</th><th>期限</th><th>狀態 / 退回</th></tr></thead>
            <tbody>{inbox.map(task => <tr data-testid="inbox-task-row" key={task.id}><td data-label="任務"><strong>{task.name}</strong><small>{task.content || '—'}</small></td><td data-label="指派人">{task.creatorName}</td><td data-label="期限">{new Date(task.deadline).toLocaleString()}</td>
              <td data-label="狀態／退回">{task.status === 'ASSIGNED' ? <div className="return-control"><input data-testid="return-reason" placeholder="退回原因" value={returnReasons[task.id] ?? ''} onChange={event => setReturnReasons({ ...returnReasons, [task.id]: event.target.value })} /><ActionIconButton data-testid="task-return" label="退回任務" icon="↩" tone="danger" onClick={() => void returnTask(task.id)} /></div> : <><span className={`status ${task.status.toLowerCase()}`}>{task.status}</span>{task.returnReason && <small>{task.returnReason}</small>}</>}</td>
            </tr>)}</tbody></table>{inbox.length === 0 && <p className="empty-state">目前沒有收到任務。</p>}
        </div>}
      </>}

      {(message || error) && <div className="management-status"><p data-testid={error ? 'task-error' : 'task-success'} className={error ? 'error' : 'success'}>{error || message}</p></div>}
    </div>
  </AppShell>;
}
