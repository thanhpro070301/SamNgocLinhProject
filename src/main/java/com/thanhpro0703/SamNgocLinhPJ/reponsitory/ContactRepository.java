package com.thanhpro0703.SamNgocLinhPJ.reponsitory;
import com.thanhpro0703.SamNgocLinhPJ.entity.ContactEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ContactRepository extends JpaRepository<ContactEntity, Long> {
}