describe('信箱註冊登入', () => {
  const testCode = '123456';
  const email = `cypress.${Date.now()}@example.com`;
  const originalPassword = 'password123';
  const newPassword = 'newPassword456';

  /** 以指定帳號密碼完成登入。 */
  const login = (username: string, password: string) => {
    cy.visit('/login');
    cy.get('[data-testid="username"]').clear().type(username);
    cy.get('[data-testid="password"]').clear().type(password);
    cy.get('[data-testid="login-submit"]').click();
    cy.url().should('not.include', '/login');
  };

  it('可完成首次註冊、重複提醒、信箱登入及忘記密碼', () => {
    cy.visit('/login');
    cy.get('[data-testid="email-register-link"]').click();
    cy.get('[data-testid="registration-email"]').type(email);
    cy.get('[data-testid="registration-send-code"]').click();
    cy.get('[data-testid="registration-message"]').should('contain', '驗證碼已寄送');
    cy.get('[data-testid="registration-code"]').type(testCode);
    cy.get('[data-testid="registration-verify-code"]').click();
    cy.get('[data-testid="registration-message"]').should('contain', '信箱驗證成功');
    cy.get('[data-testid="registration-password"]').type(originalPassword);
    cy.get('[data-testid="registration-confirm-password"]').type(originalPassword);
    cy.get('[data-testid="registration-submit"]').click();
    cy.url().should('include', '/test/testTemp/');

    cy.contains('登出').click();
    login(email, originalPassword);
    cy.url().should('include', '/test/testTemp/');

    cy.contains('登出').click();
    cy.visit('/register');
    cy.get('[data-testid="registration-email"]').type(email);
    cy.get('[data-testid="registration-send-code"]').click();
    cy.get('[data-testid="registration-error"]').should('contain', '此信箱已註冊');

    cy.visit('/login');
    cy.get('[data-testid="forgot-password-link"]').click();
    cy.get('[data-testid="reset-email"]').type(email);
    cy.get('[data-testid="reset-send-code"]').click();
    cy.get('[data-testid="reset-message"]').should('contain', '驗證碼已寄送');
    cy.get('[data-testid="reset-code"]').type(testCode);
    cy.get('[data-testid="reset-verify-code"]').click();
    cy.get('[data-testid="reset-password"]').type(newPassword);
    cy.get('[data-testid="reset-confirm-password"]').type(newPassword);
    cy.get('[data-testid="reset-submit"]').click();
    cy.get('[data-testid="reset-message"]').should('contain', '密碼已更新');
    cy.get('[data-testid="reset-return-login"]').click();

    cy.get('[data-testid="username"]').clear().type(email);
    cy.get('[data-testid="password"]').clear().type(newPassword);
    cy.get('[data-testid="login-submit"]').click();
    cy.url().should('include', '/test/testTemp/');
  });

  it('系統管理員寄送測試信後可看到資料庫寄送紀錄', () => {
    login('admin', 'admin123');
    cy.visit('/email-verification');
    cy.get('[data-testid="verification-email"]').type(`audit.${Date.now()}@example.com`);
    cy.get('[data-testid="send-verification-mail"]').click();
    cy.contains('驗證碼已寄送').should('be.visible');
    cy.get('[data-testid="mail-log-row"]').first()
      .should('contain', '管理員測試')
      .and('contain', '成功');
  });
});
