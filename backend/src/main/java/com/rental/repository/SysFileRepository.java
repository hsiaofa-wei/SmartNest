package com.rental.repository;

import com.rental.entity.SysFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysFileRepository extends JpaRepository<SysFile, Long> {
}

