describe('我的任務', () => {
  const suffix = Date.now().toString();
  const companyName = `Cypress我的任務公司-${suffix}`;
  const managerUsername = `cy.my.manager.${suffix}`;
  const employeeUsername = `cy.my.employee.${suffix}`;
  const taskName = `Cypress我的任務-${suffix}`;
  let adminToken = ''; let managerToken = ''; let employeeSession: { token: string; fullName: string; roles: string[] };
  let companyId = ''; let employeeId = ''; let taskId = '';

  /** 使用 JWT 呼叫測試 API。 */
  const request = (token: string, method: string, url: string, body?: object) => cy.request({ method, url: `http://localhost:8080/api${url}`, body, headers: { Authorization: `Bearer ${token}` } });

  before(() => {
    cy.request('POST', 'http://localhost:8080/api/auth/login', { username: 'admin', password: 'admin123' }).then(r => { adminToken = r.body.data.token; })
      .then(() => request(adminToken, 'POST', '/admin/company-supervisor-management/companies', { name: companyName, description: '我的任務 E2E' })).then(r => { companyId = r.body.data.id; })
      .then(() => request(adminToken, 'POST', '/admin/users', { fullName: 'E2E主管', username: managerUsername, email: `${managerUsername}@example.com`, password: 'password123' })).then(r => request(adminToken, 'POST', '/admin/company-supervisor-management/supervisors', { userId: r.body.data.id, title: 'E2E主管' }))
      .then(r => request(adminToken, 'POST', '/admin/company-supervisor-management/bindings', { companyId, supervisorId: r.body.data.id }))
      .then(() => request(adminToken, 'POST', '/admin/users', { fullName: 'E2E員工', username: employeeUsername, email: `${employeeUsername}@example.com`, password: 'password123' })).then(r => { employeeId = r.body.data.id; })
      .then(() => cy.request('POST', 'http://localhost:8080/api/auth/login', { username: employeeUsername, password: 'password123' })).then(r => { employeeSession = r.body.data; return request(employeeSession.token, 'POST', '/task-assignment/company-bindings', { companyName }); })
      .then(() => cy.request('POST', 'http://localhost:8080/api/auth/login', { username: managerUsername, password: 'password123' })).then(r => { managerToken = r.body.data.token; return request(managerToken, 'POST', '/task-assignment/employee-bindings', { employeeId }); })
      .then(() => request(managerToken, 'POST', '/task-assignment/tasks', { name: taskName, content: 'Cypress 工作內容', deadline: new Date(Date.now() + 86400000).toISOString(), assigneeId: employeeId })).then(r => { taskId = r.body.data.id; });
  });

  it('可查詢、編輯進度、上傳附件並提交審核', () => {
    cy.visit('/my-tasks', { onBeforeLoad(win) { win.localStorage.setItem('session', JSON.stringify(employeeSession)); win.localStorage.setItem('token', employeeSession.token); } });
    cy.contains('我的任務').should('be.visible');
    cy.get('[data-testid="my-task-search-name"]').type(taskName); cy.get('[data-testid="my-task-search"]').click();
    cy.contains('[data-testid="my-task-row"]', taskName).should('be.visible').find('[data-testid="my-task-edit"]').click();
    cy.url().should('include', `/my-tasks/${taskId}`);
    cy.get('[data-testid="my-task-work-status"]').select('COMPLETED');
    cy.get('[data-testid="my-task-progress-content"]').type('Cypress 已完成工作');
    cy.get('[data-testid="my-task-progress"]').invoke('val', 80).trigger('change');
    cy.get('[data-testid="my-task-save"]').click(); cy.get('[data-testid="my-task-message"]').should('contain', '更新成功');
    cy.get('[data-testid="my-task-attachment"]').selectFile({ contents: Cypress.Buffer.from('ok'), fileName: 'evidence.txt', mimeType: 'text/plain' });
    cy.contains('evidence.txt').should('be.visible');
    cy.get('[data-testid="my-task-submit"]').click(); cy.url().should('include', '/my-tasks');
    cy.contains('[data-testid="my-task-row"]', taskName).should('be.visible');
  });
});
