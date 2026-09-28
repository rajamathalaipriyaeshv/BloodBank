package org.example.bloodbank.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Entity
public class BloodUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Blood group must not be blank")
    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Blood group must be one of A+, A-, B+, B-, AB+, AB-, O+, O-")
    private String bloodGroup;

    @NotNull(message = "Collection date is required")
    private LocalDate collectionDate;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;

    private boolean issued = false;

    // Link back to the donor who donated this unit
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "donor_id")
    private Donor donor;

    public BloodUnit() {}

    public BloodUnit(String bloodGroup, LocalDate collectionDate, LocalDate expiryDate) {
        this.bloodGroup = bloodGroup;
        this.collectionDate = collectionDate;
        this.expiryDate = expiryDate;
    }

    public Long getId() { return id; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public LocalDate getCollectionDate() { return collectionDate; }
    public void setCollectionDate(LocalDate collectionDate) { this.collectionDate = collectionDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public boolean isIssued() { return issued; }
    public void setIssued(boolean issued) { this.issued = issued; }
    public Donor getDonor() { return donor; }
    public void setDonor(Donor donor) { this.donor = donor; }
}