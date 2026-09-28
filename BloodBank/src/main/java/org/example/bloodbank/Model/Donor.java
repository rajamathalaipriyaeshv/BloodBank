package org.example.bloodbank.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

@Entity
public class Donor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Donor name must not be blank")
    private String name;

    @NotBlank(message = "Blood group must not be blank")
    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Blood group must be one of A+, A-, B+, B-, AB+, AB-, O+, O-")
    private String bloodGroup;

    private LocalDate lastDonationDate;

    public Donor() {}

    public Donor(String name, String bloodGroup, LocalDate lastDonationDate) {
        this.name = name;
        this.bloodGroup = bloodGroup;
        this.lastDonationDate = lastDonationDate;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public LocalDate getLastDonationDate() { return lastDonationDate; }
    public void setLastDonationDate(LocalDate lastDonationDate) { this.lastDonationDate = lastDonationDate; }
}