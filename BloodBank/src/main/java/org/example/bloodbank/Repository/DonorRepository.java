package org.example.bloodbank.Repository;

import org.example.bloodbank.Model.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DonorRepository extends JpaRepository<Donor, Long> {
    List<Donor> findByBloodGroup(String bloodGroup);
}
