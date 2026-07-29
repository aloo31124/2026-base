describe('註冊登入管理', () => {
  const email = `registration.management.${Date.now()}@example.com`;
  const password = 'password123';

  /** 透過畫面完成指定帳號登入。 */
  function login(username: string, userPassword: string) {
    cy.visit('/login');
    cy.get('[data-testid="username"]').clear().type(username);
    cy.get('[data-testid="password"]').clear().type(userPassword);
    cy.get('[data-testid="login-submit"]').click();
    cy.url().should('not.include', '/login');
  }

  /** 透過公開畫面建立一筆信箱首次註冊紀錄。 */
  function registerEmail() {
    cy.visit('/register');
    cy.get('[data-testid="registration-email"]').type(email);
    cy.get('[data-testid="registration-send-code"]').click();
    cy.get('[data-testid="registration-code"]').type('123456');
    cy.get('[data-testid="registration-verify-code"]').click();
    cy.get('[data-testid="registration-password"]').type(password);
    cy.get('[data-testid="registration-confirm-password"]').type(password);
    cy.get('[data-testid="registration-submit"]').click();
    cy.url().should('include', '/test/testTemp/');
    cy.contains('登出').click();
  }

  it('管理員可設定密碼政策並檢視信箱註冊成功紀錄', () => {
    registerEmail();
    login('admin', 'admin123');
    cy.intercept('GET', '**/api/admin/registration-management/policy').as('getPolicy');
    cy.visit('/registration-management');
    cy.wait('@getPolicy');
    cy.contains('註冊登入管理').should('be.visible');

    cy.get('[data-testid="policy-min-length"]')
      .type('{selectall}10');
    cy.get('[data-testid="policy-require-letter"]').check();
    cy.get('[data-testid="policy-require-number"]').check();
    cy.get('[data-testid="policy-save"]').click();
    cy.get('[data-testid="policy-success"]').should('contain', '密碼政策已儲存');

    cy.get('[data-testid="registration-record-row"]')
      .contains(email)
      .parents('tr')
      .should('contain', '信箱註冊')
      .and('contain', '成功');
  });

  it('一般使用者會被前端路由導向指定無權限頁', () => {
    login('user', 'admin123');
    cy.visit('/registration-management');
    cy.url().should('include', '/unauthorized');
    cy.get('[data-testid="unauthorized-message"]')
      .should('contain', '[註冊登入管理] [頁面] 無系統管理員權限。');
  });
});
