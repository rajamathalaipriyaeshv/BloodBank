package org.example.bloodbank.Controller;

import jakarta.validation.Valid;
import org.example.bloodbank.Model.*;
import org.example.bloodbank.Service.BloodBankService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bloodbank")
public class BloodBankController {

    private final BloodBankService bloodBankService;

    public BloodBankController(BloodBankService bloodBankService) {
        this.bloodBankService = bloodBankService;
    }

    // ─── DONORS ──────────────────────────────────────────────────────────────────

    /** Register a new donor */
    @PostMapping("/donors")
    public ResponseEntity<Donor> registerDonor(@Valid @RequestBody Donor donor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bloodBankService.registerDonor(donor));
    }

    /** Get all donors */
    @GetMapping("/donors")
    public ResponseEntity<List<Donor>> getAllDonors() {
        return ResponseEntity.ok(bloodBankService.getAllDonors());
    }

    /** Get a specific donor by ID */
    @GetMapping("/donors/{id}")
    public ResponseEntity<Donor> getDonorById(@PathVariable Long id) {
        return ResponseEntity.ok(bloodBankService.getDonorById(id));
    }

    /** Get the most recent date a donor's blood unit was issued */
    @GetMapping("/donors/{id}/last-issue-date")
    public ResponseEntity<Map<String, Object>> getLastIssueDate(@PathVariable Long id) {
        String date = bloodBankService.getLastIssueDateByDonorId(id);
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("donorId", id);
        result.put("lastIssueDate", date);
        return ResponseEntity.ok(result);
    }

    /** Check if a donor is eligible to donate (90-day gap rule) */
    @GetMapping("/donors/{id}/eligibility")
    public ResponseEntity<Map<String, Object>> checkEligibility(@PathVariable Long id) {
        boolean eligible = bloodBankService.checkEligibility(id);
        Map<String, Object> result = Map.of(
                "donorId", id,
                "eligible", eligible,
                "message", eligible
                        ? "Donor is eligible to donate."
                        : "Donor is NOT eligible. Must wait 90 days from last donation."
        );
        return ResponseEntity.ok(result);
    }

    // ─── DONATIONS ────────────────────────────────────────────────────────────────

    /**
     * Log a donation — adds a blood unit to inventory.
     * Blood group is auto-fetched from the donor's stored profile.
     * Enforces 90-day eligibility rule before accepting.
     */
    @PostMapping("/donate")
    public ResponseEntity<Donation> addDonation(@RequestParam Long donorId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bloodBankService.addDonation(donorId));
    }

    /** Get all donations */
    @GetMapping("/donations")
    public ResponseEntity<List<Donation>> getAllDonations() {
        return ResponseEntity.ok(bloodBankService.getAllDonations());
    }

    /** Get all donations by a specific donor */
    @GetMapping("/donors/{id}/donations")
    public ResponseEntity<List<Donation>> getDonationsByDonor(@PathVariable Long id) {
        return ResponseEntity.ok(bloodBankService.getDonationsByDonor(id));
    }

    // ─── BLOOD UNIT ISSUANCE ─────────────────────────────────────────────────────

    /**
     * Issue a blood unit of a given blood group.
     * Excludes units expiring within 7 days and already-issued/expired units.
     */
    @PostMapping("/issue")
    public ResponseEntity<IssueRecord> issueUnit(
            @RequestParam String bloodGroup,
            @RequestParam(required = false, defaultValue = "Unknown") String requestedBy) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bloodBankService.issueUnit(bloodGroup, requestedBy));
    }

    /** Get all issue records */
    @GetMapping("/issue-records")
    public ResponseEntity<List<IssueRecord>> getAllIssueRecords() {
        return ResponseEntity.ok(bloodBankService.getAllIssueRecords());
    }

    // ─── INVENTORY & REPORTS ──────────────────────────────────────────────────────

    /** Get all blood units in inventory */
    @GetMapping("/blood-units")
    public ResponseEntity<List<BloodUnit>> getAllBloodUnits() {
        return ResponseEntity.ok(bloodBankService.getAllBloodUnits());
    }

    /** Requirement 5: Current stock count grouped by blood group */
    @GetMapping("/stock-summary")
    public ResponseEntity<Map<String, Long>> getStockSummary() {
        return ResponseEntity.ok(bloodBankService.getStockLevelsByGroup());
    }

    /** Requirement 4: Units expiring within 7 days */
    @GetMapping("/expiring-soon")
    public ResponseEntity<List<BloodUnit>> getNearingExpiry() {
        return ResponseEntity.ok(bloodBankService.getNearingExpiryUnits());
    }
}