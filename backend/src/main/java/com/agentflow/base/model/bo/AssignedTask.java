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
}
