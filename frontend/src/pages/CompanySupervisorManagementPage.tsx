import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { api } from '../app/api';
import AppShell from '../components/AppShell';
import ActionIconButton from '../components/ActionIconButton';

type Tab = 'companies' | 'supervisors' | 'bindings';
type BindingKind = 'supervisor' | 'employee';

interface Company {
  id: string;
  name: string;
  description?: string;
}

interface Supervisor {
  id: string;
  userId: string;
  fullName: string;
  username: string;
  email: string;
  title: string;
  companyId?: string;
  companyName?: string;
}

interface Binding {
  id: string;
  companyId: string;
  companyName: string;
  supervisorId: string;
  userId: string;
  supervisorName: string;
  supervisorUsername: string;
  title: string;
}

interface EmployeeBinding {
  id: string;
  companyId: string;
  companyName: string;
  userId: string;
  employeeName: string;
  employeeUsername: string;
  employeeEmail: string;
}

interface UserOption {
  id: string;
  fullName: string;
  username: string;
  email: string;
  active: boolean;
  roles: string[];
}

const blankCompany = { id: '', name: '', description: '' };
const blankSupervisor = { id: '', userId: '', title: '' };
const blankBinding = { companyId: '', supervisorId: '' };
const blankEmployeeBinding = { companyId: '', userId: '' };

export default function CompanySupervisorManagementPage() {
  const [tab, setTab] = useState<Tab>('companies');
  const [companies, setCompanies] = useState<Company[]>([]);
  const [supervisors, setSupervisors] = useState<Supervisor[]>([]);
  const [bindings, setBindings] = useState<Binding[]>([]);
  const [employeeBindings, setEmployeeBindings] = useState<EmployeeBinding[]>([]);
  const [boundEmployeeUserIds, setBoundEmployeeUserIds] = useState<string[]>([]);
  const [users, setUsers] = useState<UserOption[]>([]);
  const [bindingKind, setBindingKind] = useState<BindingKind>('supervisor');
  const [companySearch, setCompanySearch] = useState('');
  const [supervisorSearch, setSupervisorSearch] = useState('');
  const [bindingSearch, setBindingSearch] = useState({ companyName: '', supervisorName: '' });
  const [employeeBindingSearch, setEmployeeBindingSearch] = useState({ companyName: '', employeeName: '' });
  const [companyForm, setCompanyForm] = useState(blankCompany);
  const [supervisorForm, setSupervisorForm] = useState(blankSupervisor);
  const [bindingForm, setBindingForm] = useState(blankBinding);
  const [employeeBindingForm, setEmployeeBindingForm] = useState(blankEmployeeBinding);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const availableUsers = useMemo(
    () => users.filter(user => user.active && !supervisors.some(supervisor => supervisor.userId === user.id)),
    [users, supervisors],
  );
  const availableSupervisors = useMemo(
    () => supervisors.filter(supervisor => !supervisor.companyId),
    [supervisors],
  );
  const availableEmployees = useMemo(
    () => users.filter(user =>
      user.active
      && user.roles.includes('EMPLOYEE')
      && !supervisors.some(supervisor => supervisor.userId === user.id)
      && !boundEmployeeUserIds.includes(user.id),
    ),
    [boundEmployeeUserIds, supervisors, users],
  );

  /** 頁面初次載入時取得公司、主管、綁定與使用者選項。 */
  useEffect(() => {
    void loadAll();
  }, []);

  /** 讀取頁面所需的四組即時資料。 */
  async function loadAll() {
    setLoading(true);
    setError('');
    try {
      const [companyRows, supervisorRows, bindingRows, employeeBindingRows, userRows] = await Promise.all([
        api<Company[]>('/admin/company-supervisor-management/companies'),
        api<Supervisor[]>('/admin/company-supervisor-management/supervisors'),
        api<Binding[]>('/admin/company-supervisor-management/bindings'),
        api<EmployeeBinding[]>('/admin/company-supervisor-management/employee-bindings'),
        api<UserOption[]>('/admin/users'),
      ]);
      setCompanies(companyRows);
      setSupervisors(supervisorRows);
      setBindings(bindingRows);
      setEmployeeBindings(employeeBindingRows);
      setBoundEmployeeUserIds(employeeBindingRows.map(binding => binding.userId));
      setUsers(userRows);
    } catch (reason) {
      setError((reason as Error).message);
    } finally {
      setLoading(false);
    }
  }

  /** 執行寫入操作並在成功後重新整理所有相依資料。 */
  async function mutate(action: () => Promise<unknown>, successMessage: string) {
    setLoading(true);
    setMessage('');
    setError('');
    try {
      await action();
      await loadAll();
      setMessage(successMessage);
    } catch (reason) {
      setError((reason as Error).message);
      setLoading(false);
    }
  }

  /** 新增或修改公司。 */
  async function saveCompany(event: FormEvent) {
    event.preventDefault();
    const editing = Boolean(companyForm.id);
    await mutate(
      () => api<Company>(
        editing
          ? `/admin/company-supervisor-management/companies/${companyForm.id}`
          : '/admin/company-supervisor-management/companies',
        {
          method: editing ? 'PUT' : 'POST',
          body: JSON.stringify({ name: companyForm.name, description: companyForm.description }),
        },
      ),
      editing ? '公司已更新。' : '公司已建立。',
    );
    setCompanyForm(blankCompany);
  }

  /** 依名稱重新查詢公司列表。 */
  async function searchCompanies(event: FormEvent) {
    event.preventDefault();
    setError('');
    try {
      setCompanies(await api<Company[]>(
        `/admin/company-supervisor-management/companies?name=${encodeURIComponent(companySearch)}`,
      ));
    } catch (reason) {
      setError((reason as Error).message);
    }
  }

  /** 刪除目前沒有成員綁定的公司。 */
  async function deleteCompany(id: string) {
    await mutate(
      () => api<void>(`/admin/company-supervisor-management/companies/${id}`, { method: 'DELETE' }),
      '公司已刪除。',
    );
  }

  /** 新增主管或修改既有主管職稱。 */
  async function saveSupervisor(event: FormEvent) {
    event.preventDefault();
    const editing = Boolean(supervisorForm.id);
    await mutate(
      () => api<Supervisor>(
        editing
          ? `/admin/company-supervisor-management/supervisors/${supervisorForm.id}`
          : '/admin/company-supervisor-management/supervisors',
        {
          method: editing ? 'PUT' : 'POST',
          body: JSON.stringify(editing
            ? { title: supervisorForm.title }
            : { userId: supervisorForm.userId, title: supervisorForm.title }),
        },
      ),
      editing ? '主管職稱已更新。' : '主管已建立。',
    );
    setSupervisorForm(blankSupervisor);
  }

  /** 依姓名、帳號或職稱重新查詢主管。 */
  async function searchSupervisors(event: FormEvent) {
    event.preventDefault();
    setError('');
    try {
      setSupervisors(await api<Supervisor[]>(
        `/admin/company-supervisor-management/supervisors?keyword=${encodeURIComponent(supervisorSearch)}`,
      ));
    } catch (reason) {
      setError((reason as Error).message);
    }
  }

  /** 移除未綁定公司的主管身分。 */
  async function deleteSupervisor(id: string) {
    await mutate(
      () => api<void>(`/admin/company-supervisor-management/supervisors/${id}`, { method: 'DELETE' }),
      '主管已刪除。',
    );
  }

  /** 建立公司與主管綁定。 */
  async function saveBinding(event: FormEvent) {
    event.preventDefault();
    await mutate(
      () => api<Binding>('/admin/company-supervisor-management/bindings', {
        method: 'POST',
        body: JSON.stringify(bindingForm),
      }),
      '公司主管綁定成功。',
    );
    setBindingForm(blankBinding);
  }

  /** 依公司與主管條件重新查詢綁定。 */
  async function searchBindings(event: FormEvent) {
    event.preventDefault();
    setError('');
    try {
      const query = new URLSearchParams(bindingSearch);
      setBindings(await api<Binding[]>(`/admin/company-supervisor-management/bindings?${query}`));
    } catch (reason) {
      setError((reason as Error).message);
    }
  }

  /** 取消公司主管綁定。 */
  async function deleteBinding(id: string) {
    await mutate(
      () => api<void>(`/admin/company-supervisor-management/bindings/${id}`, { method: 'DELETE' }),
      '公司主管綁定已取消。',
    );
  }

  /** 建立公司與員工綁定。 */
  async function saveEmployeeBinding(event: FormEvent) {
    event.preventDefault();
    await mutate(
      () => api<EmployeeBinding>('/admin/company-supervisor-management/employee-bindings', {
        method: 'POST',
        body: JSON.stringify(employeeBindingForm),
      }),
      '公司員工綁定成功。',
    );
    setEmployeeBindingForm(blankEmployeeBinding);
  }

  /** 依公司與員工條件重新查詢綁定。 */
  async function searchEmployeeBindings(event: FormEvent) {
    event.preventDefault();
    setError('');
    try {
      const query = new URLSearchParams(employeeBindingSearch);
      setEmployeeBindings(await api<EmployeeBinding[]>(
        `/admin/company-supervisor-management/employee-bindings?${query}`,
      ));
    } catch (reason) {
      setError((reason as Error).message);
    }
  }

  /** 取消公司員工綁定。 */
  async function deleteEmployeeBinding(id: string) {
    await mutate(
      () => api<void>(
        `/admin/company-supervisor-management/employee-bindings/${id}`,
        { method: 'DELETE' },
      ),
      '公司員工綁定已取消。',
    );
  }

  return <AppShell>
    <div className="content company-supervisor-content">
      <header className="page-heading">
        <div>
          <p className="eyebrow">Company & supervisor administration</p>
          <h1>公司主管管理</h1>
          <p>維護公司主檔、既有使用者的主管身分，以及一人一公司的綁定關係。</p>
        </div>
        <button className="btn secondary" type="button" disabled={loading} onClick={() => void loadAll()}>
          {loading ? '讀取中…' : '重新整理'}
        </button>
      </header>

      <div className="info-banner">
        主管與員工必須選自已註冊且啟用的使用者；公司或主管仍有綁定時，請先在「綁定公司」標籤取消關聯。
      </div>

      <div className="tabs management-tabs" role="tablist" aria-label="公司主管管理標籤">
        <button data-testid="company-tab" className={tab === 'companies' ? 'active' : ''} onClick={() => setTab('companies')}>公司</button>
        <button data-testid="supervisor-tab" className={tab === 'supervisors' ? 'active' : ''} onClick={() => setTab('supervisors')}>主管</button>
        <button data-testid="binding-tab" className={tab === 'bindings' ? 'active' : ''} onClick={() => setTab('bindings')}>綁定公司</button>
      </div>

      {tab === 'companies' && <section className="management-grid">
        <form className="card management-form" onSubmit={saveCompany}>
          <div><p className="eyebrow">Company profile</p><h2>{companyForm.id ? '修改公司' : '新增公司'}</h2></div>
          <label>公司名稱<input data-testid="company-name" required maxLength={120} value={companyForm.name} onChange={event => setCompanyForm({ ...companyForm, name: event.target.value })} /></label>
          <label>公司說明<textarea data-testid="company-description" maxLength={500} value={companyForm.description} onChange={event => setCompanyForm({ ...companyForm, description: event.target.value })} /></label>
          <div className="actions">
            {companyForm.id && <button className="btn secondary" type="button" onClick={() => setCompanyForm(blankCompany)}>取消</button>}
            <button data-testid="company-save" className="btn primary" disabled={loading}>{companyForm.id ? '儲存修改' : '建立公司'}</button>
          </div>
        </form>
        <div className="card management-list">
          <form className="inline-form" onSubmit={searchCompanies}>
            <input data-testid="company-search" aria-label="公司名稱查詢" placeholder="依公司名稱查詢" value={companySearch} onChange={event => setCompanySearch(event.target.value)} />
            <button data-testid="company-search-submit" className="btn secondary">查詢</button>
          </form>
          <table><thead><tr><th>公司名稱</th><th>說明</th><th>操作</th></tr></thead>
            <tbody>{companies.map(company => <tr key={company.id} data-testid="company-row">
              <td data-label="公司名稱"><strong>{company.name}</strong></td><td data-label="說明">{company.description || '—'}</td>
              <td data-label="操作"><div className="table-actions">
                <ActionIconButton label="修改公司" icon="✎" onClick={() => setCompanyForm({ id: company.id, name: company.name, description: company.description ?? '' })} />
                <ActionIconButton label="刪除公司" icon="×" tone="danger" onClick={() => void deleteCompany(company.id)} />
              </div></td>
            </tr>)}</tbody>
          </table>
          {companies.length === 0 && <p className="empty-state">尚無符合條件的公司。</p>}
        </div>
      </section>}

      {tab === 'supervisors' && <section className="management-grid">
        <form className="card management-form" onSubmit={saveSupervisor}>
          <div><p className="eyebrow">Supervisor profile</p><h2>{supervisorForm.id ? '修改主管' : '新增主管'}</h2></div>
          <label>已註冊使用者
            <select data-testid="supervisor-user" required disabled={Boolean(supervisorForm.id)} value={supervisorForm.userId} onChange={event => setSupervisorForm({ ...supervisorForm, userId: event.target.value })}>
              <option value="">請選擇使用者</option>
              {availableUsers.map(user => <option key={user.id} value={user.id}>{user.fullName}（{user.username}）</option>)}
              {supervisorForm.id && <option value={supervisorForm.userId}>{supervisors.find(row => row.id === supervisorForm.id)?.fullName}</option>}
            </select>
          </label>
          <label>主管職稱<input data-testid="supervisor-title" required maxLength={80} value={supervisorForm.title} onChange={event => setSupervisorForm({ ...supervisorForm, title: event.target.value })} /></label>
          <div className="actions">
            {supervisorForm.id && <button className="btn secondary" type="button" onClick={() => setSupervisorForm(blankSupervisor)}>取消</button>}
            <button data-testid="supervisor-save" className="btn primary" disabled={loading}>{supervisorForm.id ? '儲存修改' : '建立主管'}</button>
          </div>
        </form>
        <div className="card management-list">
          <form className="inline-form" onSubmit={searchSupervisors}>
            <input data-testid="supervisor-search" aria-label="主管查詢" placeholder="姓名、帳號或職稱" value={supervisorSearch} onChange={event => setSupervisorSearch(event.target.value)} />
            <button data-testid="supervisor-search-submit" className="btn secondary">查詢</button>
          </form>
          <table><thead><tr><th>主管</th><th>職稱</th><th>公司</th><th>操作</th></tr></thead>
            <tbody>{supervisors.map(supervisor => <tr key={supervisor.id} data-testid="supervisor-row">
              <td data-label="主管"><strong>{supervisor.fullName}</strong><small>{supervisor.username} · {supervisor.email}</small></td>
              <td data-label="職稱"><span className="tag">{supervisor.title}</span></td><td data-label="公司">{supervisor.companyName || '尚未綁定'}</td>
              <td data-label="操作"><div className="table-actions">
                <ActionIconButton label="修改主管" icon="✎" onClick={() => setSupervisorForm({ id: supervisor.id, userId: supervisor.userId, title: supervisor.title })} />
                <ActionIconButton label="刪除主管" icon="×" tone="danger" disabled={Boolean(supervisor.companyId)} onClick={() => void deleteSupervisor(supervisor.id)} />
              </div></td>
            </tr>)}</tbody>
          </table>
          {supervisors.length === 0 && <p className="empty-state">尚無符合條件的主管。</p>}
        </div>
      </section>}

      {tab === 'bindings' && <section className="binding-section">
        <div className="binding-kind-tabs" role="tablist" aria-label="綁定成員類型">
          <button
            data-testid="supervisor-binding-kind"
            className={bindingKind === 'supervisor' ? 'active' : ''}
            type="button"
            onClick={() => setBindingKind('supervisor')}
          >
            主管
          </button>
          <button
            data-testid="employee-binding-kind"
            className={bindingKind === 'employee' ? 'active' : ''}
            type="button"
            onClick={() => setBindingKind('employee')}
          >
            員工
          </button>
        </div>

        {bindingKind === 'supervisor' ? <div className="management-grid">
          <form className="card management-form" onSubmit={saveBinding}>
            <div><p className="eyebrow">Supervisor membership</p><h2>主管綁定公司</h2></div>
            <label>公司
              <select data-testid="binding-company" required value={bindingForm.companyId} onChange={event => setBindingForm({ ...bindingForm, companyId: event.target.value })}>
                <option value="">請選擇公司</option>
                {companies.map(company => <option key={company.id} value={company.id}>{company.name}</option>)}
              </select>
            </label>
            <label>未綁定主管
              <select data-testid="binding-supervisor" required value={bindingForm.supervisorId} onChange={event => setBindingForm({ ...bindingForm, supervisorId: event.target.value })}>
                <option value="">請選擇主管</option>
                {availableSupervisors.map(supervisor => <option key={supervisor.id} value={supervisor.id}>{supervisor.fullName}（{supervisor.username}）</option>)}
              </select>
            </label>
            <div className="actions"><button data-testid="binding-save" className="btn primary" disabled={loading}>建立綁定</button></div>
          </form>
          <div className="card management-list">
            <form className="binding-search" onSubmit={searchBindings}>
              <input data-testid="binding-company-search" aria-label="綁定公司查詢" placeholder="公司名稱" value={bindingSearch.companyName} onChange={event => setBindingSearch({ ...bindingSearch, companyName: event.target.value })} />
              <input data-testid="binding-supervisor-search" aria-label="綁定主管查詢" placeholder="主管姓名或帳號" value={bindingSearch.supervisorName} onChange={event => setBindingSearch({ ...bindingSearch, supervisorName: event.target.value })} />
              <button data-testid="binding-search-submit" className="btn secondary">查詢</button>
            </form>
            <table><thead><tr><th>公司</th><th>主管</th><th>職稱</th><th>操作</th></tr></thead>
              <tbody>{bindings.map(binding => <tr key={binding.id} data-testid="binding-row">
                <td data-label="公司"><strong>{binding.companyName}</strong></td>
                <td data-label="主管">{binding.supervisorName}<small>{binding.supervisorUsername}</small></td>
                <td data-label="職稱"><span className="tag managed">{binding.title}</span></td>
                <td data-label="操作"><div className="table-actions"><ActionIconButton label="取消主管綁定" icon="×" tone="danger" onClick={() => void deleteBinding(binding.id)} /></div></td>
              </tr>)}</tbody>
            </table>
            {bindings.length === 0 && <p className="empty-state">尚無符合條件的主管綁定。</p>}
          </div>
        </div> : <div className="management-grid">
          <form className="card management-form" onSubmit={saveEmployeeBinding}>
            <div><p className="eyebrow">Employee membership</p><h2>員工綁定公司</h2></div>
            <label>公司
              <select
                data-testid="employee-binding-company"
                required
                value={employeeBindingForm.companyId}
                onChange={event => setEmployeeBindingForm({
                  ...employeeBindingForm,
                  companyId: event.target.value,
                })}
              >
                <option value="">請選擇公司</option>
                {companies.map(company => <option key={company.id} value={company.id}>{company.name}</option>)}
              </select>
            </label>
            <label>未綁定員工
              <select
                data-testid="employee-binding-user"
                required
                value={employeeBindingForm.userId}
                onChange={event => setEmployeeBindingForm({
                  ...employeeBindingForm,
                  userId: event.target.value,
                })}
              >
                <option value="">請選擇員工</option>
                {availableEmployees.map(user => <option key={user.id} value={user.id}>{user.fullName}（{user.username}）</option>)}
              </select>
            </label>
            <p className="form-hint">僅顯示啟用、具有員工角色、尚未成為主管且未綁定公司的使用者。</p>
            <div className="actions">
              <button data-testid="employee-binding-save" className="btn primary" disabled={loading}>建立綁定</button>
            </div>
          </form>
          <div className="card management-list">
            <form className="binding-search" onSubmit={searchEmployeeBindings}>
              <input
                data-testid="employee-binding-company-search"
                aria-label="員工綁定公司查詢"
                placeholder="公司名稱"
                value={employeeBindingSearch.companyName}
                onChange={event => setEmployeeBindingSearch({
                  ...employeeBindingSearch,
                  companyName: event.target.value,
                })}
              />
              <input
                data-testid="employee-binding-user-search"
                aria-label="綁定員工查詢"
                placeholder="員工姓名或帳號"
                value={employeeBindingSearch.employeeName}
                onChange={event => setEmployeeBindingSearch({
                  ...employeeBindingSearch,
                  employeeName: event.target.value,
                })}
              />
              <button data-testid="employee-binding-search-submit" className="btn secondary">查詢</button>
            </form>
            <table><thead><tr><th>公司</th><th>員工</th><th>信箱</th><th>操作</th></tr></thead>
              <tbody>{employeeBindings.map(binding => <tr key={binding.id} data-testid="employee-binding-row">
                <td data-label="公司"><strong>{binding.companyName}</strong></td>
                <td data-label="員工">{binding.employeeName}<small>{binding.employeeUsername}</small></td>
                <td data-label="信箱">{binding.employeeEmail}</td>
                <td data-label="操作"><div className="table-actions"><ActionIconButton label="取消員工綁定" icon="×" tone="danger" onClick={() => void deleteEmployeeBinding(binding.id)} /></div></td>
              </tr>)}</tbody>
            </table>
            {employeeBindings.length === 0 && <p className="empty-state">尚無符合條件的員工綁定。</p>}
          </div>
        </div>}
      </section>}

      <div className="management-status" aria-live="polite">
        {message && <p data-testid="company-supervisor-success" className="success">{message}</p>}
        {error && <p data-testid="company-supervisor-error" className="error">{error}</p>}
      </div>
    </div>
  </AppShell>;
}
