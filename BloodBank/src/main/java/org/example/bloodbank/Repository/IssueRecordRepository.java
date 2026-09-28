package org.example.bloodbank.Repository;

import org.example.bloodbank.Model.IssueRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IssueRecordRepository extends JpaRepository<IssueRecord, Long> {
    List<IssueRecord> findByBloodGroup(String bloodGroup);
    // Get the most recent issue record for blood units donated by a specific donor
    Optional<IssueRecord> findTopByBloodUnitDonorIdOrderByIssueDateDesc(Long donorId);
}

