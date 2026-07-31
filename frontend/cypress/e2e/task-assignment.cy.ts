describe('任務指派', () => {
  const suffix = Date.now().toString();
  const companyName = `Cypress任務公司-${suffix}`;
  const managerUsername = `cy.manager.${suffix}`;
  const employeeUsername = `cy.employee.${suffix}`;
  const employeeEmail = `${employeeUsername}@example.com`;
  const taskName = `Cypress任務-${suffix}`;
  let managerSession: { token: string; tokenType: string; username: string; fullName: string; roles: string[] };
  let employeeToken = '';
  let adminToken = '';
  let companyId = '';
  let managerId = '';
  let employeeId = '';
  let supervisorId = '';

  /** 以指定 token 呼叫 API。 */
  const request = (token: string, method: string, url: string, body?: object) =>
    cy.request({ method, url: `http://localhost:8080/api${url}`, body, headers: { Authorization: `Bearer ${token}` } });

  before(() => {
    cy.request('POST', 'http://localhost:8080/api/auth/login', { username: 'admin', password: 'admin123' })
      .then(login => { adminToken = login.body.data.token; })
      .then(() => request(adminToken, 'POST', '/admin/company-supervisor-management/companies', { name: companyName, description: 'Cypress 任務公司' }))
      .then(company => { companyId = company.body.data.id; })
      .then(() => request(adminToken, 'POST', '/admin/users', { fullName: 'Cypress主管', username: managerUsername, email: `${managerUsername}@example.com`, password: 'password123' }))
      .then(manager => { managerId = manager.body.data.id; })
      .then(() => request(adminToken, 'POST', '/admin/users', { fullName: 'Cypress員工', username: employeeUsername, email: employeeEmail, password: 'password123' }))
      .then(employee => { employeeId = employee.body.data.id; })
      .then(() => request(adminToken, 'POST', '/admin/company-supervisor-management/supervisors', { userId: managerId, title: 'Cypress主管' }))
      .then(supervisor => { supervisorId = supervisor.body.data.id; })
      .then(() => request(adminToken, 'POST', '/admin/company-supervisor-management/bindings', { companyId, supervisorId }))
      .then(() => cy.request('POST', 'http://localhost:8080/api/auth/login', { username: managerUsername, password: 'password123' }))
      .then(managerLogin => { managerSession = managerLogin.body.data; })
      .then(() => cy.request('POST', 'http://localhost:8080/api/auth/login', { username: employeeUsername, password: 'password123' }))
      .then(employeeLogin => { employeeToken = employeeLogin.body.data.token; })
      .then(() => request(employeeToken, 'POST', '/task-assignment/company-bindings', { companyName }))
      .then(() => expect(employeeId).not.to.equal(''));
  });

  it('主管可綁定員工、建立查詢任務，員工可退回後主管可重新指派與撤回', () => {
    cy.visit('/task-assignment', {
      onBeforeLoad(window) {
        window.localStorage.setItem('session', JSON.stringify(managerSession));
        window.localStorage.setItem('token', managerSession.token);
      },
    });
    cy.contains('任務指派').should('be.visible');
    cy.get('[data-testid="member-binding-tab"]').click();
    cy.get('[data-testid="employee-email-search"]').type(employeeEmail);
    cy.get('[data-testid="employee-search-submit"]').click();
    cy.contains(employeeEmail).should('be.visible');
    cy.get('[data-testid="employee-bind"]').click();
    cy.get('[data-testid="task-success"]').should('contain', '已綁定');

    cy.get('[data-testid="task-management-tab"]').click();
    cy.get('[data-testid="task-name"]').type(taskName);
    cy.get('[data-testid="task-content"]').type('Cypress 任務內容');
    cy.get('[data-testid="task-assignee"]').select(`Cypress員工（${employeeUsername}）`);
    cy.get('[data-testid="task-save"]').click();
    cy.get('[data-testid="task-success"]').should('contain', '指派成功');
    cy.get('[data-testid="task-search-name"]').type(taskName);
    cy.get('[data-testid="task-search-assignee"]').type(employeeUsername);
    cy.get('[data-testid="task-search-submit"]').click();
    cy.contains('[data-testid="task-row"]', taskName).should('contain', employeeUsername);

    request(managerSession.token, 'GET', `/task-assignment/tasks?name=${encodeURIComponent(taskName)}&sortBy=assignedAt&direction=desc`)
      .then(result => {
        const taskId = result.body.data[0].id;
        return request(employeeToken, 'POST', `/task-assignment/tasks/${taskId}/return`, { reason: 'Cypress 資訊不足' });
      });
    cy.contains('[data-testid="task-row"]', taskName).within(() => cy.contains('修改').click());
    cy.get('[data-testid="task-content"]').clear().type('Cypress 已補充資訊');
    cy.get('[data-testid="task-save"]').click();
    cy.get('[data-testid="task-success"]').should('contain', '重新指派');
    cy.contains('[data-testid="task-row"]', taskName).within(() => cy.contains('撤回').click());
    cy.contains('[data-testid="task-row"]', taskName).should('contain', 'WITHDRAWN');
  });
});
