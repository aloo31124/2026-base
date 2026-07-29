describe('公司主管管理', () => {
  const companyName = `Cypress公司-${Date.now()}`;
  const updatedCompanyName = `${companyName}-更新`;

  /** 透過登入畫面完成指定帳號登入。 */
  function login(username: string, password: string) {
    cy.visit('/login');
    cy.get('[data-testid="username"]').clear().type(username);
    cy.get('[data-testid="password"]').clear().type(password);
    cy.get('[data-testid="login-submit"]').click();
    cy.url().should('not.include', '/login');
  }

  it('管理員可完成公司、主管與綁定 CRUD 及名稱查詢', () => {
    login('admin', 'admin123');
    cy.visit('/company-supervisor-management');
    cy.contains('公司主管管理').should('be.visible');

    cy.get('[data-testid="company-name"]').type(companyName);
    cy.get('[data-testid="company-description"]').type('Cypress 建立的公司');
    cy.get('[data-testid="company-save"]').click();
    cy.get('[data-testid="company-supervisor-success"]').should('contain', '公司已建立');

    cy.contains('[data-testid="company-row"]', companyName).within(() => {
      cy.contains('修改').click();
    });
    cy.get('[data-testid="company-name"]').clear().type(updatedCompanyName);
    cy.get('[data-testid="company-description"]').clear().type('Cypress 已更新');
    cy.get('[data-testid="company-save"]').click();
    cy.get('[data-testid="company-search"]').type(updatedCompanyName);
    cy.get('[data-testid="company-search-submit"]').click();
    cy.contains('[data-testid="company-row"]', updatedCompanyName).should('contain', 'Cypress 已更新');

    cy.get('[data-testid="supervisor-tab"]').click();
    cy.get('[data-testid="supervisor-user"]').select('Demo User（user）');
    cy.get('[data-testid="supervisor-title"]').type('測試主管');
    cy.get('[data-testid="supervisor-save"]').click();
    cy.contains('[data-testid="supervisor-row"]', 'Demo User').should('contain', '測試主管');

    cy.contains('[data-testid="supervisor-row"]', 'Demo User').within(() => {
      cy.contains('修改').click();
    });
    cy.get('[data-testid="supervisor-title"]').clear().type('資深測試主管');
    cy.get('[data-testid="supervisor-save"]').click();
    cy.get('[data-testid="supervisor-search"]').type('資深測試主管');
    cy.get('[data-testid="supervisor-search-submit"]').click();
    cy.contains('[data-testid="supervisor-row"]', 'Demo User').should('contain', '資深測試主管');

    cy.get('[data-testid="binding-tab"]').click();
    cy.get('[data-testid="binding-company"]').select(updatedCompanyName);
    cy.get('[data-testid="binding-supervisor"]').select('Demo User（user）');
    cy.get('[data-testid="binding-save"]').click();
    cy.get('[data-testid="binding-company-search"]').type(updatedCompanyName);
    cy.get('[data-testid="binding-supervisor-search"]').type('Demo User');
    cy.get('[data-testid="binding-search-submit"]').click();
    cy.contains('[data-testid="binding-row"]', updatedCompanyName)
      .should('contain', 'Demo User')
      .and('contain', '資深測試主管');

    cy.contains('[data-testid="binding-row"]', updatedCompanyName).within(() => {
      cy.contains('取消綁定').click();
    });
    cy.get('[data-testid="supervisor-tab"]').click();
    cy.contains('[data-testid="supervisor-row"]', 'Demo User').within(() => {
      cy.contains('刪除').click();
    });
    cy.get('[data-testid="company-tab"]').click();
    cy.contains('[data-testid="company-row"]', updatedCompanyName).within(() => {
      cy.contains('刪除').click();
    });
    cy.contains('[data-testid="company-row"]', updatedCompanyName).should('not.exist');
  });

  it('一般使用者會被前端路由導向指定無權限頁', () => {
    login('user2', 'admin234');
    cy.visit('/company-supervisor-management');
    cy.url().should('include', '/unauthorized');
    cy.get('[data-testid="unauthorized-message"]')
      .should('contain', '[公司主管管理] [頁面] 無系統管理員權限。');
  });
});
