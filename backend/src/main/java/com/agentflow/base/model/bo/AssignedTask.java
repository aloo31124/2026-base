package com.agentflow.base.model.bo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "assigned_task")
public class AssignedTask extends BaseEntity {
    public enum Status {
        ASSIGNED,
        RETURNED,
        WITHDRAWN
    }

    public enum WorkStatus { PENDING, IN_PROGRESS, COMPLETED }

    @Nationalized
    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Nationalized
    @Column(name = "content", length = 4000)
    private String content;

    @Column(name = "deadline", nullable = false)
    private Instant deadline;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_user_id", nullable = false)
    private UserAccount creator;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignee_user_id", nullable = false)
    private UserAccount assignee;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_status", length = 20)
    private WorkStatus workStatus = WorkStatus.PENDING;

    @Nationalized
    @Column(name = "progress_content", length = 4000)
    private String progressContent;

    @Column(name = "progress_percent")
    private Integer progressPercent = 10;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Nationalized
    @Column(name = "extension_reason", length = 500)
    private String extensionReason;

    @Column(name = "extension_requested_at")
    private Instant extensionRequestedAt;

    @Nationalized
    @Column(name = "return_reason", length = 500)
    private String returnReason;

    @Column(name = "returned_at")
    private Instant returnedAt;

    /** 提供 JPA 建立實體。 */
    protected AssignedTask() {
    }

    /**
     * 建立已指派任務。
     *
     * @param name 名稱
     * @param content 內容
     * @param deadline 期限
     * @param creator 建立主管
     * @param assignee 受派人
     */
    public AssignedTask(String name, String content, Instant deadline, UserAccount creator, UserAccount assignee) {
        this.name = name;
        this.content = content;
        this.deadline = deadline;
        this.creator = creator;
        this.assignee = assignee;
        this.assignedAt = Instant.now();
        this.status = Status.ASSIGNED;
        this.workStatus = WorkStatus.PENDING;
        this.progressPercent = 10;
    }

    /**
     * 修改內容並重新指派退回任務。
     */
    public void update(String name, String content, Instant deadline, UserAccount assignee) {
        this.name = name;
        this.content = content;
        this.deadline = deadline;
        this.assignee = assignee;
        this.status = Status.ASSIGNED;
        this.assignedAt = Instant.now();
        this.returnReason = null;
        this.returnedAt = null;
        this.submittedAt = null;
        this.extensionReason = null;
        this.extensionRequestedAt = null;
    }

    /** 更新受派人的工作狀態、進度說明與百分比。 */
    public void updateProgress(WorkStatus workStatus, String progressContent, int progressPercent) {
        this.workStatus = workStatus;
        this.progressContent = progressContent;
        this.progressPercent = progressPercent;
    }

    /** 將已完成工作提交給原指派者審核。 */
    public void submitForReview() {
        this.submittedAt = Instant.now();
    }

    /** 保存延期申請並等待指派者審核。 */
    public void requestExtension(String reason) {
        this.extensionReason = reason;
        this.extensionRequestedAt = Instant.now();
    }

    /** 將指派中任務撤回。 */
    public void withdraw() {
        this.status = Status.WITHDRAWN;
    }

    /**
     * 由受派人退回任務。
     *
     * @param reason 退回原因
     */
    public void returnTask(String reason) {
        this.status = Status.RETURNED;
        this.returnReason = reason;
        this.returnedAt = Instant.now();
    }

    public String getName() { return name; }
    public String getContent() { return content; }
    public Instant getDeadline() { return deadline; }
    public UserAccount getCreator() { return creator; }
    public UserAccount getAssignee() { return assignee; }
    public Instant getAssignedAt() { return assignedAt; }
    public Status getStatus() { return status; }
    public String getReturnReason() { return returnReason; }
    public Instant getReturnedAt() { return returnedAt; }
    public WorkStatus getWorkStatus() { return workStatus == null ? WorkStatus.PENDING : workStatus; }
    public String getProgressContent() { return progressContent; }
    public int getProgressPercent() { return progressPercent == null ? 10 : progressPercent; }
    public Instant getSubmittedAt() { return submittedAt; }
    public String getExtensionReason() { return extensionReason; }
    public Instant getExtensionRequestedAt() { return extensionRequestedAt; }
}
