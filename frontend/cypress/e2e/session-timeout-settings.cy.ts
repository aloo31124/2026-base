describe("後台登出時間設定", () => {
  const createToken = () => {
    const payload = btoa(
      JSON.stringify({
        sub: "admin",
        roles: ["SYSTEM_ADMIN", "EMPLOYEE"],
        exp: Math.floor(Date.now() / 1000) + 7200,
      }),
    )
      .replaceAll("+", "-")
      .replaceAll("/", "_")
      .replace(/=+$/, "");
    return `test-header.${payload}.test-signature`;
  };

  beforeEach(() => {
    const token = createToken();
    cy.intercept("GET", "**/api/admin/registration-management/policy", {
      body: {
        success: true,
        message: "密碼政策查詢成功。",
        data: {
          minLength: 8,
          requireLetter: true,
          requireNumber: true,
        },
      },
    });
    cy.intercept(
      "GET",
      "**/api/admin/registration-management/session-timeout",
      {
        body: {
          success: true,
          message: "登出時間設定查詢成功。",
          data: {
            timeoutMinutes: 120,
            updatedAt: "2026-08-02T04:00:00Z",
          },
        },
      },
    ).as("getSessionTimeout");
    cy.intercept(
      "GET",
      "**/api/admin/registration-management/registrations",
      {
        body: { success: true, message: "註冊紀錄查詢成功。", data: [] },
      },
    );
    cy.intercept(
      "PUT",
      "**/api/admin/registration-management/session-timeout",
      (request) => {
        expect(request.body).to.deep.equal({ timeoutMinutes: 30 });
        request.reply({
          body: {
            success: true,
            message: "登出時間設定更新成功。",
            data: {
              timeoutMinutes: 30,
              updatedAt: "2026-08-02T04:30:00Z",
            },
          },
        });
      },
    ).as("updateSessionTimeout");

    cy.visit("/registration-management", {
      onBeforeLoad(win) {
        const session = {
          token,
          tokenType: "Bearer",
          username: "admin",
          fullName: "系統管理員",
          roles: ["SYSTEM_ADMIN", "EMPLOYEE"],
        };
        win.localStorage.setItem("session", JSON.stringify(session));
        win.localStorage.setItem("token", token);
      },
    });
  });

  it("管理員可檢視並更新新登入使用的登出時間", () => {
    cy.wait("@getSessionTimeout");
    cy.get('[data-testid="session-timeout-minutes"]')
      .should("have.value", "120")
      .and("have.attr", "min", "5")
      .and("have.attr", "max", "1440")
      .type("{selectall}30");

    cy.contains("新設定只套用於儲存後新建立的登入").should("be.visible");
    cy.get('[data-testid="session-timeout-save"]').click();
    cy.wait("@updateSessionTimeout");
    cy.get('[data-testid="session-timeout-success"]')
      .should("be.visible")
      .and("contain", "登出時間已儲存");
    cy.get('[data-testid="session-timeout-minutes"]').should(
      "have.value",
      "30",
    );
  });
});
