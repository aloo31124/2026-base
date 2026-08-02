describe("系統報表", () => {
  const suffix = Date.now().toString();
  const companyName = `Cypress報表公司-${suffix}`;
  const managerUsername = `cy.report.manager.${suffix}`;
  const employeeUsername = `cy.report.employee.${suffix}`;
  const taskName = `Cypress報表任務-${suffix}`;
  let adminSession: {
    token: string;
    tokenType: string;
    username: string;
    fullName: string;
    roles: string[];
  };
  let managerToken = "";
  let employeeToken = "";
  let companyId = "";

  /** 以指定 token 呼叫 API。 */
  const request = (token: string, method: string, url: string, body?: object) =>
    cy.request({
      method,
      url: `http://localhost:8080/api${url}`,
      body,
      headers: { Authorization: `Bearer ${token}` },
    });

  before(() => {
    let managerId = "";
    let employeeId = "";
    let supervisorId = "";
    cy.request("POST", "http://localhost:8080/api/auth/login", {
      username: "admin",
      password: "admin123",
    })
      .then((login) => {
        adminSession = login.body.data;
        return request(adminSession.token, "POST", "/admin/company-supervisor-management/companies", {
          name: companyName,
          description: "Cypress 系統報表公司",
        });
      })
      .then((company) => {
        companyId = company.body.data.id;
        return request(adminSession.token, "POST", "/admin/users", {
          fullName: "Cypress報表主管",
          username: managerUsername,
          email: `${managerUsername}@example.com`,
          password: "password123",
        });
      })
      .then((manager) => {
        managerId = manager.body.data.id;
        return request(adminSession.token, "POST", "/admin/users", {
          fullName: "Cypress報表員工",
          username: employeeUsername,
          email: `${employeeUsername}@example.com`,
          password: "password123",
        });
      })
      .then((employee) => {
        employeeId = employee.body.data.id;
        return request(adminSession.token, "POST", "/admin/company-supervisor-management/supervisors", {
          userId: managerId,
          title: "Cypress報表主管",
        });
      })
      .then((supervisor) => {
        supervisorId = supervisor.body.data.id;
        return request(adminSession.token, "POST", "/admin/company-supervisor-management/bindings", {
          companyId,
          supervisorId,
        });
      })
      .then(() =>
        cy.request("POST", "http://localhost:8080/api/auth/login", {
          username: managerUsername,
          password: "password123",
        }),
      )
      .then((managerLogin) => {
        managerToken = managerLogin.body.data.token;
        return cy.request("POST", "http://localhost:8080/api/auth/login", {
          username: employeeUsername,
          password: "password123",
        });
      })
      .then((employeeLogin) => {
        employeeToken = employeeLogin.body.data.token;
        return request(employeeToken, "POST", "/task-assignment/company-bindings", { companyName });
      })
      .then(() => request(managerToken, "POST", "/task-assignment/employee-bindings", { employeeId }))
      .then(() =>
        request(managerToken, "POST", "/task-assignment/tasks", {
          name: taskName,
          content: "Cypress 趨勢測試",
          deadline: new Date(Date.now() + 86_400_000).toISOString(),
          assigneeId: employeeId,
        }),
      );
  });

  /** 驗證預設一年、公司與日期篩選、折線圖及摘要。 */
  it("系統管理員可用公司與日期篩選任務趨勢折線圖", () => {
    cy.visit("/system-reports", {
      onBeforeLoad(window) {
        window.localStorage.setItem("session", JSON.stringify(adminSession));
        window.localStorage.setItem("token", adminSession.token);
      },
    });

    cy.contains("系統報表").should("be.visible");
    cy.get('[data-testid="task-trend-tab"]').should("have.class", "active");
    cy.get('[data-testid="trend-chart"]').should("be.visible");
    cy.get('[data-testid="trend-from"]').invoke("val").should("match", /^\d{4}-\d{2}-\d{2}$/);
    cy.get('[data-testid="trend-to"]').invoke("val").should("match", /^\d{4}-\d{2}-\d{2}$/);

    cy.get('[data-testid="trend-company"]').select(companyId);
    cy.get('[data-testid="trend-apply"]').click();
    cy.get('[data-testid="trend-scope"]').should("contain", companyName);
    cy.get('[data-testid="trend-total"]').should("contain", "1");

    const today = new Date().toISOString().slice(0, 10);
    cy.get('[data-testid="trend-from"]').clear().type(today);
    cy.get('[data-testid="trend-to"]').clear().type(today);
    cy.get('[data-testid="trend-apply"]').click();
    cy.get('[data-testid="trend-chart-point"]').should("have.length", 1);
    cy.get('[data-testid="trend-empty"]').should("not.exist");
  });
});
