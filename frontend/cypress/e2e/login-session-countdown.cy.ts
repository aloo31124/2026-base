describe("登入工作階段倒數", () => {
  const now = Date.UTC(2026, 7, 2, 4, 0, 0);

  const createToken = (expiresInSeconds: number) => {
    const payload = btoa(
      JSON.stringify({
        sub: "admin",
        roles: ["SYSTEM_ADMIN", "EMPLOYEE"],
        exp: Math.floor(now / 1000) + expiresInSeconds,
      }),
    )
      .replaceAll("+", "-")
      .replaceAll("/", "_")
      .replace(/=+$/, "");
    return `test-header.${payload}.test-signature`;
  };

  const visitAsAdmin = (expiresInSeconds: number) => {
    const token = createToken(expiresInSeconds);
    const session = {
      token,
      tokenType: "Bearer",
      username: "admin",
      fullName: "系統管理員",
      roles: ["SYSTEM_ADMIN", "EMPLOYEE"],
    };

    cy.intercept("GET", "**/api/admin/users", {
      statusCode: 200,
      body: { success: true, message: "查詢成功。", data: [] },
    });
    cy.visit("/users", {
      onBeforeLoad(win) {
        win.localStorage.setItem("session", JSON.stringify(session));
        win.localStorage.setItem("token", token);
      },
    });
  };

  beforeEach(() => {
    cy.clock(now).as("clock");
  });

  it("在右上角顯示依 JWT exp 計算且每秒更新的倒數", () => {
    visitAsAdmin(120);

    cy.get('[data-testid="session-countdown"]')
      .should("be.visible")
      .and("contain", "登入倒數");
    cy.get('[data-testid="session-countdown-value"]').should(
      "have.text",
      "00:02:00",
    );

    cy.tick(1000);
    cy.get('[data-testid="session-countdown-value"]').should(
      "have.text",
      "00:01:59",
    );
  });

  it("到期後自動清除登入資料並回到登入頁", () => {
    visitAsAdmin(2);
    cy.get('[data-testid="session-countdown-value"]').should(
      "have.text",
      "00:00:02",
    );

    cy.tick(2000);

    cy.location("pathname").should("eq", "/login");
    cy.window().then((win) => {
      expect(win.localStorage.getItem("session")).to.be.null;
      expect(win.localStorage.getItem("token")).to.be.null;
    });
    cy.get('[data-testid="session-expired-message"]')
      .should("be.visible")
      .and("contain", "登入時間已到，請重新登入。");
  });

  it("分頁恢復焦點時依目前時間立即校正", () => {
    visitAsAdmin(60);
    cy.get('[data-testid="session-countdown-value"]').should(
      "have.text",
      "00:01:00",
    );

    cy.get("@clock").then((clock) => {
      (
        clock as unknown as { setSystemTime: (timestamp: number) => void }
      ).setSystemTime(now + 30_000);
    });
    cy.window().trigger("focus");

    cy.get('[data-testid="session-countdown-value"]').should(
      "have.text",
      "00:00:30",
    );
  });

  it("頁面載入時已到期會清除過期 session", () => {
    visitAsAdmin(-1);

    cy.location("pathname").should("eq", "/login");
    cy.window().then((win) => {
      expect(win.localStorage.getItem("session")).to.be.null;
      expect(win.localStorage.getItem("token")).to.be.null;
    });
    cy.get('[data-testid="session-countdown"]').should("not.exist");
  });

  it("窄螢幕仍保留可讀的倒數數值", () => {
    cy.viewport(390, 844);
    visitAsAdmin(3661);

    cy.get('[data-testid="session-countdown"]')
      .should("be.visible")
      .and("have.attr", "role", "timer");
    cy.get('[data-testid="session-countdown-value"]')
      .should("be.visible")
      .and("have.text", "01:01:01");
  });
});
