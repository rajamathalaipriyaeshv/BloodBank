package org.example.bloodbank.Model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Tracks each blood unit issuance event — who requested it, which unit, when, and for what blood group.
 */
@Entity
public class IssueRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "blood_unit_id", nullable = false)
    private BloodUnit bloodUnit;

    private String bloodGroup;
    private LocalDate issueDate;
    private String requestedBy;  // Optional: name/identifier of who requested

    public IssueRecord() {}

    public IssueRecord(BloodUnit bloodUnit, String bloodGroup, LocalDate issueDate, String requestedBy) {
        this.bloodUnit = bloodUnit;
        this.bloodGroup = bloodGroup;
        this.issueDate = issueDate;
        this.requestedBy = requestedBy;
    }

    public Long getId() { return id; }
    public BloodUnit getBloodUnit() { return bloodUnit; }
    public void setBloodUnit(BloodUnit bloodUnit) { this.bloodUnit = bloodUnit; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }
}
