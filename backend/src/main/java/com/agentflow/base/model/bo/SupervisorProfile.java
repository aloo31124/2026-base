package com.agentflow.base.model.bo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "supervisor_profile")
public class SupervisorProfile extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserAccount user;

    @Nationalized
    @Column(name = "title", nullable = false, length = 80)
    private String title;

    /**
     * 提供 JPA 建立主管資料。
     */
    protected SupervisorProfile() {
    }

    /**
     * 建立既有使用者的主管資料。
     *
     * @param user 已註冊使用者
     * @param title 主管職稱
     */
    public SupervisorProfile(UserAccount user, String title) {
        this.user = user;
        this.title = title;
    }

    /**
     * 更新主管職稱。
     *
     * @param title 新職稱
     */
    public void updateTitle(String title) {
        this.title = title;
    }

    /**
     * 取得主管對應使用者。
     *
     * @return 使用者
     */
    public UserAccount getUser() {
        return user;
    }

    /**
     * 取得主管職稱。
     *
     * @return 職稱
     */
    public String getTitle() {
        return title;
    }
}
