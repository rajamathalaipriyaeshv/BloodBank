package org.example.bloodbank.Model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Tracks each donation event: which donor donated, what blood group, and when.
 * A Donation is the record of the act; the resulting BloodUnit is the physical unit added to inventory.
 */
@Entity
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "blood_unit_id")
    private BloodUnit bloodUnit;

    private String bloodGroup;
    private LocalDate donationDate;

    public Donation() {}

    public Donation(Donor donor, BloodUnit bloodUnit, String bloodGroup, LocalDate donationDate) {
        this.donor = donor;
        this.bloodUnit = bloodUnit;
        this.bloodGroup = bloodGroup;
        this.donationDate = donationDate;
    }

    public Long getId() { return id; }
    public Donor getDonor() { return donor; }
    public void setDonor(Donor donor) { this.donor = donor; }
    public BloodUnit getBloodUnit() { return bloodUnit; }
    public void setBloodUnit(BloodUnit bloodUnit) { this.bloodUnit = bloodUnit; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public LocalDate getDonationDate() { return donationDate; }
    public void setDonationDate(LocalDate donationDate) { this.donationDate = donationDate; }
}
