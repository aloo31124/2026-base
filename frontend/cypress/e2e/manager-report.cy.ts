describe("主管報表", () => {
  const today = new Date().toISOString().slice(0, 10);
  const lastYear = new Date();
  lastYear.setFullYear(lastYear.getFullYear() - 1);
  const from = lastYear.toISOString().slice(0, 10);
  const session = {
    token: "manager-report-token",
    tokenType: "Bearer",
    username: "manager.report",
    fullName: "王主管",
    roles: ["MANAGER"],
  };

  /** 建立主管報表回應，讓篩選前後可驗證確定性結果。 */
  function report(completedOnly = false) {
    return {
      success: true,
      message: "主管報表查詢成功。",
      data: {
        companyName: "AgentFlow 測試公司",
        from,
        to: today,
        assigneeId: completedOnly ? "employee-1" : null,
        assigneeName: completedOnly ? "陳員工" : "全部執行者",
        workStatus: completedOnly ? "COMPLETED" : null,
        companyTotalTasks: 5,
        managerTotalTasks: completedOnly ? 1 : 3,
        trendPoints: [
          { date: from, taskCount: 0 },
          { date: today, taskCount: completedOnly ? 1 : 3 },
        ],
        statusBuckets: [
          { status: "PENDING", label: "待處理", taskCount: completedOnly ? 0 : 1, percentage: completedOnly ? 0 : 33.3 },
          { status: "IN_PROGRESS", label: "進行中", taskCount: completedOnly ? 0 : 1, percentage: completedOnly ? 0 : 33.3 },
          { status: "COMPLETED", label: "已完成", taskCount: 1, percentage: completedOnly ? 100 : 33.4 },
        ],
      },
      timestamp: new Date().toISOString(),
    };
  }

  /** 驗證公司摘要、共用篩選、折線圖、圓餅圖與標籤切換。 */
  it("主管可查看並篩選自己指派的任務趨勢與狀態比例", () => {
    cy.intercept("GET", "**/api/manager/reports/filters", {
      body: {
        success: true,
        message: "主管報表篩選選項查詢成功。",
        data: {
          companyName: "AgentFlow 測試公司",
          assignees: [{ id: "employee-1", name: "陳員工" }],
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
    cy.intercept("GET", "**/api/manager/reports/report*", (request) => {
      request.reply(report(request.url.includes("workStatus=COMPLETED")));
    }).as("report");

    cy.visit("/manager-reports", {
      onBeforeLoad(window) {
        window.localStorage.setItem("session", JSON.stringify(session));
        window.localStorage.setItem("token", session.token);
      },
    });

    cy.wait(["@filters", "@report"]);
    cy.contains("主管報表").should("be.visible");
    cy.get('[data-testid="manager-company-name"]').should("contain", "AgentFlow 測試公司");
    cy.get('[data-testid="company-total"]').should("contain", "5");
    cy.get('[data-testid="manager-total"]').should("contain", "3");
    cy.get('[data-testid="manager-trend-chart"]').should("be.visible");
    cy.get('[data-testid="manager-from"]').should("have.value", from);
    cy.get('[data-testid="manager-to"]').should("have.value", today);

    cy.get('[data-testid="manager-assignee"]').select("employee-1");
    cy.get('[data-testid="manager-work-status"]').select("COMPLETED");
    cy.get('[data-testid="manager-apply"]').click();
    cy.wait("@report");
    cy.get('[data-testid="manager-total"]').should("contain", "1");

    cy.get('[data-testid="status-ratio-tab"]').click();
    cy.get('[data-testid="manager-status-pie"]').should("be.visible");
    cy.get('[data-testid="status-COMPLETED"]').should("contain", "100.0%");
    cy.get('[data-testid="manager-assignee"]').should("have.value", "employee-1");
    cy.get('[data-testid="manager-work-status"]').should("have.value", "COMPLETED");
  });
});
