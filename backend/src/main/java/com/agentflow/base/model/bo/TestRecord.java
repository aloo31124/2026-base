package com.agentflow.base.model.bo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "test")
public class TestRecord extends BaseEntity {
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    @Column(name = "description", nullable = false, length = 500)
    private String description;
    @Column(name = "test_status", nullable = false, length = 24)
    private String testStatus;

    protected TestRecord() {}
    public TestRecord(String name, String description, String testStatus) { this.name = name; this.description = description; this.testStatus = testStatus; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getTestStatus() { return testStatus; }
    public void update(String name, String description, String testStatus) { this.name = name; this.description = description; this.testStatus = testStatus; }
}

