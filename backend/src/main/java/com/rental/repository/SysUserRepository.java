package com.rental.repository;

import com.rental.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long> {
    Optional<SysUser> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByPhone(String phone);
    long countByRole(SysUser.UserRole role);
    Page<SysUser> findByRole(SysUser.UserRole role, Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM SysUser u WHERE FUNCTION('MONTH', u.createTime) = :month")
    long countByCreateTimeMonth(@Param("month") int month);
}

