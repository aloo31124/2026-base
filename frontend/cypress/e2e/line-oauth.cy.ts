describe('LINE OAuth 註冊登入', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('從登入頁前往 LINE 驗證、首次註冊並建立 JWT session', () => {
    cy.visit('/login');
    cy.get('[data-testid="line-login"]').should('be.visible').and('contain', '使用 LINE 登入').click();
    cy.url().should('include', '/test/testTemp/');
    cy.window().then(win => {
      const session = JSON.parse(win.localStorage.getItem('session') ?? '{}');
      expect(session.tokenType).to.equal('Bearer');
      expect(session.token).to.be.a('string').and.not.be.empty;
      expect(session.roles).to.include('EMPLOYEE');
      expect(session.fullName).to.equal('LINE 測試使用者');
    });
  });

  it('使用者取消 LINE 授權時顯示可操作錯誤並寫入 DENIED 稽核', () => {
    cy.request('GET', 'http://localhost:8080/api/auth/line/authorize').then(response => {
      const authorizationUrl = new URL(response.body.data.authorizationUrl);
      const state = authorizationUrl.searchParams.get('state')!;
      cy.visit(`/api/auth/line/callback?state=${encodeURIComponent(state)}&error=access_denied`);
      cy.get('[data-testid="line-callback-error"]').should('contain', '無法完成 LINE 登入');
      cy.get('[role="alert"]').should('contain', '授權已取消');
      cy.request(`http://localhost:8080/api/auth/line/mock/audit?state=${encodeURIComponent(state)}`)
        .its('body.data').should('deep.include', { status: 'DENIED', resultCode: 'ACCESS_DENIED', completed: true });
    });
  });

  it('已使用 callback state 無法重播', () => {
    cy.request('GET', 'http://localhost:8080/api/auth/line/authorize').then(response => {
      const authorizationUrl = new URL(response.body.data.authorizationUrl);
      const state = authorizationUrl.searchParams.get('state')!;
      cy.request({
        method: 'POST',
        url: 'http://localhost:8080/api/auth/line/callback',
        body: { code: 'cypress-replay-user', state },
      });
      cy.visit(`/api/auth/line/callback?code=cypress-replay-user&state=${encodeURIComponent(state)}`);
      cy.get('[data-testid="line-callback-error"]').should('be.visible');
      cy.get('[role="alert"]').should('contain', 'state 無效或已使用');
    });
  });
});
