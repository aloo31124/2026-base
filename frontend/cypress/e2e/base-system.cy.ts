describe('基礎架構使用者分權', () => {
  const login = (username = 'admin', password = 'admin123') => {
    cy.visit('/login');
    cy.get('[data-testid="username"]').clear().type(username);
    cy.get('[data-testid="password"]').clear().type(password);
    cy.get('[data-testid="login-submit"]').click();
    cy.url().should('not.include', '/login');
  };

  it('系統管理員可登入、新增使用者並看到管理員新增與員工角色', () => {
    const username = `cypress.${Date.now()}`;
    login();
    cy.url().should('include', '/users');
    cy.get('[data-testid="add-user"]').click();
    cy.get('[data-testid="full-name"]').type('Cypress 使用者');
    cy.get('[data-testid="new-username"]').type(username);
    cy.get('[data-testid="email"]').type(`${username}@agentflow.local`);
    cy.get('[data-testid="new-password"]').type('password123');
    cy.get('[data-testid="save-user"]').click();
    cy.get(`[data-testid="user-${username}"]`).should('contain', '管理員新增').and('contain', 'EMPLOYEE');
  });

  it('一般使用者無法進入系統管理員頁面', () => {
    login('user', 'admin123');
    cy.visit('/users');
    cy.get('[data-testid="unauthorized-message"]').should('contain', '[使用者角色] [頁面] 無系統管理員權限。');
  });

  it('測試頁可完成 test 資料表 CRUD 操作', () => {
    login();
    cy.visit('/test/testTemp/');
    cy.get('[data-testid="test-name"]').clear().type('Cypress CRUD');
    cy.get('[data-testid="create-test"]').click();
    cy.get('[data-testid="test-row"]').last().should('contain', 'Cypress CRUD').within(() => cy.contains('編輯').click());
    cy.get('[data-testid="test-row"]').last().should('contain', 'DONE').within(() => cy.contains('刪除').click());
  });
});
