package com.agentflow.base.model.bo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "company_membership")
public class CompanyMembership extends BaseEntity {
    public enum MemberType {
        SUPERVISOR,
        EMPLOYEE
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserAccount user;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_type", nullable = false, length = 20)
    private MemberType memberType;

    /**
     * 提供 JPA 建立公司成員綁定。
     */
    protected CompanyMembership() {
    }

    /**
     * 建立公司與使用者的唯一綁定。
     *
     * @param company 公司
     * @param user 使用者
     * @param memberType 成員類型
     */
    public CompanyMembership(Company company, UserAccount user, MemberType memberType) {
        this.company = company;
        this.user = user;
        this.memberType = memberType;
    }

    /**
     * 取得公司。
     *
     * @return 公司
     */
    public Company getCompany() {
        return company;
    }

    /**
     * 取得使用者。
     *
     * @return 使用者
     */
    public UserAccount getUser() {
        return user;
    }

    /**
     * 取得成員類型。
     *
     * @return 成員類型
     */
    public MemberType getMemberType() {
        return memberType;
    }
}
