package org.example.bloodbank.Repository;

import org.example.bloodbank.Model.BloodUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BloodUnitRepository extends JpaRepository<BloodUnit, Long> {
    List<BloodUnit> findByBloodGroupAndIssuedFalse(String bloodGroup);
    List<BloodUnit> findByIssuedFalse();
}
