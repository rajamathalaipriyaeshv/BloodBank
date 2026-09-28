package org.example.bloodbank.Service;

import org.example.bloodbank.Model.*;
import org.example.bloodbank.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class BloodBankService {

    private static final int MINIMUM_DONATION_GAP_DAYS = 90;
    private static final int BLOOD_UNIT_EXPIRY_DAYS = 42;
    private static final int EXPIRY_WARNING_DAYS = 7;

    private final DonorRepository donorRepository;
    private final BloodUnitRepository bloodUnitRepository;
    private final DonationRepository donationRepository;
    private final IssueRecordRepository issueRecordRepository;

    public BloodBankService(DonorRepository donorRepository,
                            BloodUnitRepository bloodUnitRepository,
                            DonationRepository donationRepository,
                            IssueRecordRepository issueRecordRepository) {
        this.donorRepository = donorRepository;
        this.bloodUnitRepository = bloodUnitRepository;
        this.donationRepository = donationRepository;
        this.issueRecordRepository = issueRecordRepository;
    }

    // ─── DONOR ──────────────────────────────────────────────────────────────────

    public Donor registerDonor(Donor donor) {
        return donorRepository.save(donor);
    }

    public List<Donor> getAllDonors() {
        return donorRepository.findAll();
    }

    public Donor getDonorById(Long id) {
        return donorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Donor not found with id: " + id));
    }

    /** Returns the most recent issue date for any blood unit donated by this donor. */
    public String getLastIssueDateByDonorId(Long donorId) {
        return issueRecordRepository
                .findTopByBloodUnitDonorIdOrderByIssueDateDesc(donorId)
                .map(r -> r.getIssueDate().toString())
                .orElse(null);
    }

    /**
     * Requirement 2: Check donor eligibility based on minimum gap since last donation.
     * Business rule enforced at service layer — not just DB level.
     */
    public boolean checkEligibility(Long donorId) {
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new IllegalArgumentException("Donor not found with id: " + donorId));
        if (donor.getLastDonationDate() == null) return true;

        long daysBetween = ChronoUnit.DAYS.between(donor.getLastDonationDate(), LocalDate.now());
        return daysBetween >= MINIMUM_DONATION_GAP_DAYS;
    }

    // ─── DONATION ────────────────────────────────────────────────────────────────

    /**
     * Requirement 1: Log a donation – adding a unit to inventory with collection and expiry date.
     * Blood group is automatically taken from the donor's stored profile — not passed by the caller.
     * Business Rule: A donation cannot be accepted from a donor who has not completed the minimum gap.
     */
    public Donation addDonation(Long donorId) {
        // Business rule check FIRST — enforced at service layer
        if (!checkEligibility(donorId)) {
            throw new IllegalArgumentException(
                    "A donation cannot be accepted from a donor who has not completed the minimum gap " +
                    "(" + MINIMUM_DONATION_GAP_DAYS + " days) since their last donation.");
        }

        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new IllegalArgumentException("Donor not found with id: " + donorId));

        // Blood group is fetched from the donor's stored profile
        String bloodGroup = donor.getBloodGroup();

        // Update donor's last donation date
        donor.setLastDonationDate(LocalDate.now());
        donorRepository.save(donor);

        // Create blood unit in inventory
        LocalDate collectionDate = LocalDate.now();
        LocalDate expiryDate = collectionDate.plusDays(BLOOD_UNIT_EXPIRY_DAYS);
        BloodUnit unit = new BloodUnit(bloodGroup, collectionDate, expiryDate);
        unit.setDonor(donor);
        bloodUnitRepository.save(unit);

        // Create donation record
        Donation donation = new Donation(donor, unit, bloodGroup, LocalDate.now());
        return donationRepository.save(donation);
    }

    public List<Donation> getAllDonations() {
        return donationRepository.findAll();
    }

    public List<Donation> getDonationsByDonor(Long donorId) {
        return donationRepository.findByDonorId(donorId);
    }

    // ─── ISSUE ───────────────────────────────────────────────────────────────────

    /**
     * Requirement 3: Issue a request against a record, replacing inventory by blood group.
     * Business Rule: Units missing or past expiry must be excluded from being issued against new requests.
     */
    public IssueRecord issueUnit(String bloodGroup, String requestedBy) {
        List<BloodUnit> units = bloodUnitRepository.findByBloodGroupAndIssuedFalse(bloodGroup);
        LocalDate today = LocalDate.now();
        LocalDate cutOffDate = today.plusDays(EXPIRY_WARNING_DAYS);

        BloodUnit eligible = null;
        for (BloodUnit unit : units) {
            // Business rule: must NOT be expiring within 7 days or already expired
            if (unit.getExpiryDate().isAfter(cutOffDate)) {
                eligible = unit;
                break;
            }
        }

        if (eligible == null) {
            throw new IllegalArgumentException(
                    "No eligible blood units available for group: " + bloodGroup +
                    ". Units nearing expiry (within " + EXPIRY_WARNING_DAYS + " days) or already expired are excluded.");
        }

        eligible.setIssued(true);
        bloodUnitRepository.save(eligible);

        // Record the issuance
        IssueRecord record = new IssueRecord(eligible, bloodGroup, today, requestedBy);
        return issueRecordRepository.save(record);
    }

    public List<IssueRecord> getAllIssueRecords() {
        return issueRecordRepository.findAll();
    }

    // ─── INVENTORY ───────────────────────────────────────────────────────────────

    /**
     * Requirement 4: Auto-flag units nearing expiry within 7 days.
     */
    public List<BloodUnit> getNearingExpiryUnits() {
        List<BloodUnit> availableUnits = bloodUnitRepository.findByIssuedFalse();
        LocalDate today = LocalDate.now();
        LocalDate warningDate = today.plusDays(EXPIRY_WARNING_DAYS);

        List<BloodUnit> nearingExpiry = new ArrayList<>();
        for (BloodUnit unit : availableUnits) {
            if (!unit.getExpiryDate().isBefore(today) && !unit.getExpiryDate().isAfter(warningDate)) {
                nearingExpiry.add(unit);
            }
        }
        return nearingExpiry;
    }

    /**
     * Requirement 5: View current stock levels per blood group.
     */
    public Map<String, Long> getStockLevelsByGroup() {
        List<BloodUnit> availableUnits = bloodUnitRepository.findByIssuedFalse();
        LocalDate today = LocalDate.now();

        Map<String, Long> stockMap = new LinkedHashMap<>();
        // Pre-seed all blood groups so all appear in the response (even with 0)
        for (String bg : List.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")) {
            stockMap.put(bg, 0L);
        }
        for (BloodUnit unit : availableUnits) {
            if (!unit.getExpiryDate().isBefore(today)) {
                stockMap.merge(unit.getBloodGroup(), 1L, Long::sum);
            }
        }
        return stockMap;
    }

    public List<BloodUnit> getAllBloodUnits() {
        return bloodUnitRepository.findAll();
    }
}