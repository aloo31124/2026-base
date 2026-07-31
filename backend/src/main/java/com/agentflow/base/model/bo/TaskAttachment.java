package com.agentflow.base.model.bo;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "task_attachment")
public class TaskAttachment extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private AssignedTask task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploader_user_id", nullable = false)
    private UserAccount uploader;

    @Nationalized
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = 120)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "content", nullable = false)
    private byte[] content;

    protected TaskAttachment() {}

    /** 建立任務附件。 */
    public TaskAttachment(AssignedTask task, UserAccount uploader, String fileName, String contentType, byte[] content) {
        this.task = task;
        this.uploader = uploader;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = content.length;
        this.content = content;
    }

    public AssignedTask getTask() { return task; }
    public UserAccount getUploader() { return uploader; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public long getFileSize() { return fileSize; }
}
