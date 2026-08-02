describe("我的報表", () => {
  const today = new Date().toISOString().slice(0, 10);
  const lastYear = new Date();
  lastYear.setFullYear(lastYear.getFullYear() - 1);
  const from = lastYear.toISOString().slice(0, 10);
  const employeeId = "00000000-0000-0000-0000-000000000023";
  const session = {
    token: "my-report-token",
    tokenType: "Bearer",
    username: "employee.report",
    fullName: "陳員工",
    roles: ["EMPLOYEE"],
  };

  /** 建立我的報表回應，讓篩選前後可驗證確定性結果。 */
  function report(completedOnly = false, empty = false) {
    const total = empty ? 0 : completedOnly ? 1 : 3;
    return {
      success: true,
      message: "我的報表查詢成功。",
      data: {
        from,
        to: today,
        assigneeId: employeeId,
        assigneeName: "陳員工",
        workStatus: completedOnly ? "COMPLETED" : null,
        totalTasks: total,
        trendPoints: [
          { date: from, taskCount: 0 },
          { date: today, taskCount: total },
        ],
        statusBuckets: [
          { status: "PENDING", label: "待處理", taskCount: completedOnly || empty ? 0 : 1, percentage: completedOnly || empty ? 0 : 33.3 },
          { status: "IN_PROGRESS", label: "進行中", taskCount: completedOnly || empty ? 0 : 1, percentage: completedOnly || empty ? 0 : 33.3 },
          { status: "COMPLETED", label: "已完成", taskCount: empty ? 0 : 1, percentage: completedOnly ? 100 : empty ? 0 : 33.4 },
        ],
      },
    };
  }

  /** 安裝本人 filters 與可依查詢條件切換的報表 stub。 */
  function installReportApi() {
    cy.intercept("GET", "**/api/my/reports/filters", {
      body: {
        success: true,
        message: "我的報表篩選選項查詢成功。",
        data: {
          assignees: [{ id: employeeId, name: "陳員工" }],
          workStatuses: [
            { value: "PENDING", label: "待處理" },
            { value: "IN_PROGRESS", label: "進行中" },
            { value: "COMPLETED", label: "已完成" },
          ],
          defaultFrom: from,
          defaultTo: today,
        },
      },
    }).as("filters");
    cy.intercept("GET", "**/api/my/reports/report*", (request) => {
      const completedOnly = request.url.includes("workStatus=COMPLETED");
      request.alias = completedOnly ? "filteredReport" : "initialReport";
      request.reply(report(completedOnly));
    });
  }

  /** 驗證 Sheet 第 23–25 列的總覽、趨勢、狀態比例與共用篩選。 */
  it("員工可查看並篩選自己的任務趨勢與狀態比例", () => {
    installReportApi();
    cy.visit("/my-reports", {
      onBeforeLoad(window) {
        window.localStorage.setItem("session", JSON.stringify(session));
        window.localStorage.setItem("token", session.token);
      },
    });

    cy.wait(["@filters", "@initialReport"]);
    cy.contains("我的報表").should("be.visible");
    cy.get('[data-testid="my-total"]').should("contain", "3");
    cy.get('[data-testid="my-assignee-name"]').should("contain", "陳員工");
    cy.get('[data-testid="my-trend-chart"]').should("be.visible");
    cy.get('[data-testid="my-from"]').should("have.value", from);
    cy.get('[data-testid="my-to"]').should("have.value", today);

    cy.get('[data-testid="my-assignee"]').select(employeeId);
    cy.get('[data-testid="my-work-status"]').select("COMPLETED");
    cy.get('[data-testid="my-apply"]').click();
    cy.wait("@filteredReport").its("request.url").should("include", `assigneeId=${employeeId}`)
      .and("include", "workStatus=COMPLETED");
    cy.get('[data-testid="my-total"]').should("contain", "1");

    cy.get('[data-testid="my-status-tab"]').click();
    cy.get('[data-testid="my-status-pie"]').should("be.visible");
    cy.get('[data-testid="status-COMPLETED"]').should("contain", "100.0%");
    cy.get('[data-testid="my-assignee"]').should("have.value", employeeId);
    cy.get('[data-testid="my-work-status"]').should("have.value", "COMPLETED");
  });

  /** 驗證空資料提示與未登入頁面守衛。 */
  it("空資料會顯示 0 狀態，未登入則導向登入頁", () => {
    cy.intercept("GET", "**/api/my/reports/filters", {
      body: {
        success: true,
        data: {
          assignees: [{ id: employeeId, name: "陳員工" }],
          workStatuses: [],
          defaultFrom: from,
          defaultTo: today,
        },
      },
    }).as("emptyFilters");
    cy.intercept("GET", "**/api/my/reports/report*", { body: report(false, true) }).as("emptyReport");
    cy.visit("/my-reports", {
      onBeforeLoad(window) {
        window.localStorage.setItem("session", JSON.stringify(session));
        window.localStorage.setItem("token", session.token);
      },
    });
    cy.wait(["@emptyFilters", "@emptyReport"]);
    cy.get('[data-testid="my-report-empty"]').should("be.visible");
    cy.get('[data-testid="my-status-tab"]').click();
    cy.get('[data-testid="my-status-pie"]').should("be.visible");

    cy.clearLocalStorage();
    cy.visit("/my-reports");
    cy.location("pathname").should("eq", "/login");
  });
});
