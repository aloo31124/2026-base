describe('信箱註冊登入', () => {
  const email = `cypress.${Date.now()}@agentflow.local`;
  const code = (purpose: string) => cy.request(`http://localhost:8080/api/auth/email/test-code?email=${encodeURIComponent(email)}&purpose=${purpose}`).its('body.data');

  it('驗證信箱、首次註冊並以信箱帳密登入', () => {
    cy.visit('/register');
    cy.get('[data-testid="register-name"]').type('Cypress 信箱會員');
    cy.get('[data-testid="register-email"]').type(email);
    cy.get('[data-testid="register-password"]').type('Member123');
    cy.get('[data-testid="send-register-code"]').click();
    cy.get('[data-testid="send-register-code"]').should('contain', '重新寄送');
    code('REGISTER').then(value => {
      cy.get('[data-testid="register-code"]').type(String(value));
      cy.get('[data-testid="register-submit"]').click();
    });
    cy.url().should('include', '/test/testTemp/');
    cy.clearLocalStorage();
    cy.visit('/login');
    cy.get('[data-testid="username"]').clear().type(email);
    cy.get('[data-testid="password"]').clear().type('Member123');
    cy.get('[data-testid="login-submit"]').click();
    cy.url().should('include', '/test/testTemp/');
  });

  it('經信箱驗證重設密碼並用新密碼登入', () => {
    cy.clearLocalStorage(); cy.visit('/forgot-password');
    cy.get('[data-testid="forgot-email"]').type(email);
    cy.get('[data-testid="forgot-submit"]').click();
    cy.url().should('include', '/reset-password');
    code('RESET_PASSWORD').then(value => {
      cy.get('[data-testid="reset-code"]').type(String(value));
      cy.get('[data-testid="reset-password"]').type('Changed456');
      cy.get('[data-testid="reset-submit"]').click();
    });
    cy.url().should('include', '/login');
    cy.get('[data-testid="username"]').clear().type(email);
    cy.get('[data-testid="password"]').clear().type('Changed456');
    cy.get('[data-testid="login-submit"]').click();
    cy.url().should('include', '/test/testTemp/');
  });

  it('錯誤驗證碼會顯示明確訊息', () => {
    const invalid = `invalid.${Date.now()}@agentflow.local`;
    cy.visit('/register');
    cy.get('[data-testid="register-name"]').type('錯誤碼測試');
    cy.get('[data-testid="register-email"]').type(invalid);
    cy.get('[data-testid="register-password"]').type('Member123');
    cy.get('[data-testid="send-register-code"]').click();
    cy.get('[data-testid="send-register-code"]').should('contain', '重新寄送');
    cy.get('[data-testid="register-code"]').type('000000');
    cy.get('[data-testid="register-submit"]').click();
    cy.get('[role="alert"]').should('contain', '驗證碼錯誤');
  });
});
