import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { api } from '../app/api';
import AppShell from '../components/AppShell';

type Tab = 'companies' | 'supervisors' | 'bindings';

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

interface UserOption {
  id: string;
  fullName: string;
  username: string;
  email: string;
  active: boolean;
}

const blankCompany = { id: '', name: '', description: '' };
const blankSupervisor = { id: '', userId: '', title: '' };
const blankBinding = { companyId: '', supervisorId: '' };

export default function CompanySupervisorManagementPage() {
  const [tab, setTab] = useState<Tab>('companies');
  const [companies, setCompanies] = useState<Company[]>([]);
  const [supervisors, setSupervisors] = useState<Supervisor[]>([]);
  const [bindings, setBindings] = useState<Binding[]>([]);
  const [users, setUsers] = useState<UserOption[]>([]);
  const [companySearch, setCompanySearch] = useState('');
  const [supervisorSearch, setSupervisorSearch] = useState('');
  const [bindingSearch, setBindingSearch] = useState({ companyName: '', supervisorName: '' });
  const [companyForm, setCompanyForm] = useState(blankCompany);
  const [supervisorForm, setSupervisorForm] = useState(blankSupervisor);
  const [bindingForm, setBindingForm] = useState(blankBinding);
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

  /** 頁面初次載入時取得公司、主管、綁定與使用者選項。 */
  useEffect(() => {
    void loadAll();
  }, []);

  /** 讀取頁面所需的四組即時資料。 */
  async function loadAll() {
    setLoading(true);
    setError('');
    try {
      const [companyRows, supervisorRows, bindingRows, userRows] = await Promise.all([
        api<Company[]>('/admin/company-supervisor-management/companies'),
        api<Supervisor[]>('/admin/company-supervisor-management/supervisors'),
        api<Binding[]>('/admin/company-supervisor-management/bindings'),
        api<UserOption[]>('/admin/users'),
      ]);
      setCompanies(companyRows);
      setSupervisors(supervisorRows);
      setBindings(bindingRows);
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
        主管必須選自已註冊且啟用的使用者；公司或主管仍有綁定時，請先在「綁定」標籤取消關聯。
      </div>

      <div className="tabs management-tabs" role="tablist" aria-label="公司主管管理標籤">
        <button data-testid="company-tab" className={tab === 'companies' ? 'active' : ''} onClick={() => setTab('companies')}>公司</button>
        <button data-testid="supervisor-tab" className={tab === 'supervisors' ? 'active' : ''} onClick={() => setTab('supervisors')}>主管</button>
        <button data-testid="binding-tab" className={tab === 'bindings' ? 'active' : ''} onClick={() => setTab('bindings')}>綁定</button>
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
              <td><strong>{company.name}</strong></td><td>{company.description || '—'}</td>
              <td>
                <button className="btn secondary" onClick={() => setCompanyForm({ id: company.id, name: company.name, description: company.description ?? '' })}>修改</button>
                <button className="btn danger" onClick={() => void deleteCompany(company.id)}>刪除</button>
              </td>
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
              <td><strong>{supervisor.fullName}</strong><small>{supervisor.username} · {supervisor.email}</small></td>
              <td><span className="tag">{supervisor.title}</span></td><td>{supervisor.companyName || '尚未綁定'}</td>
              <td>
                <button className="btn secondary" onClick={() => setSupervisorForm({ id: supervisor.id, userId: supervisor.userId, title: supervisor.title })}>修改</button>
                <button className="btn danger" disabled={Boolean(supervisor.companyId)} onClick={() => void deleteSupervisor(supervisor.id)}>刪除</button>
              </td>
            </tr>)}</tbody>
          </table>
          {supervisors.length === 0 && <p className="empty-state">尚無符合條件的主管。</p>}
        </div>
      </section>}

      {tab === 'bindings' && <section className="management-grid">
        <form className="card management-form" onSubmit={saveBinding}>
          <div><p className="eyebrow">Company membership</p><h2>新增綁定</h2></div>
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
              <td><strong>{binding.companyName}</strong></td>
              <td>{binding.supervisorName}<small>{binding.supervisorUsername}</small></td>
              <td><span className="tag managed">{binding.title}</span></td>
              <td><button className="btn danger" onClick={() => void deleteBinding(binding.id)}>取消綁定</button></td>
            </tr>)}</tbody>
          </table>
          {bindings.length === 0 && <p className="empty-state">尚無符合條件的綁定。</p>}
        </div>
      </section>}

      <div className="management-status" aria-live="polite">
        {message && <p data-testid="company-supervisor-success" className="success">{message}</p>}
        {error && <p data-testid="company-supervisor-error" className="error">{error}</p>}
      </div>
    </div>
  </AppShell>;
}
