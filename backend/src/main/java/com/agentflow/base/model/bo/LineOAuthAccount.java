package com.agentflow.base.model.bo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "line_oauth_account")
public class LineOAuthAccount extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserAccount user;

    @Column(name = "line_user_id", nullable = false, unique = true, length = 80)
    private String lineUserId;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(name = "picture_url", length = 500)
    private String pictureUrl;

    protected LineOAuthAccount() {}

    public LineOAuthAccount(UserAccount user, String lineUserId, String displayName, String pictureUrl) {
        this.user = user;
        this.lineUserId = lineUserId;
        this.displayName = displayName;
        this.pictureUrl = pictureUrl;
    }

    public UserAccount getUser() { return user; }
    public String getLineUserId() { return lineUserId; }
    public String getDisplayName() { return displayName; }
    public String getPictureUrl() { return pictureUrl; }

    public void updateProfile(String displayName, String pictureUrl) {
        this.displayName = displayName;
        this.pictureUrl = pictureUrl;
    }
}
