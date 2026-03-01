package com.rental.repository;

import com.rental.entity.ViewAppointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ViewAppointmentRepository extends JpaRepository<ViewAppointment, Long> {
    Page<ViewAppointment> findByTenantId(Long tenantId, Pageable pageable);
    Page<ViewAppointment> findByLandlordId(Long landlordId, Pageable pageable);
    Page<ViewAppointment> findByHouseId(Long houseId, Pageable pageable);
    Page<ViewAppointment> findByLandlordIdAndStatus(Long landlordId, ViewAppointment.AppointmentStatus status, Pageable pageable);
    ViewAppointment findByHouseIdAndTenantIdAndLandlordIdAndStatus(Long houseId, Long tenantId, Long landlordId, ViewAppointment.AppointmentStatus status);
    List<ViewAppointment> findByTenantIdAndHouseIdAndStatus(Long tenantId, Long houseId, ViewAppointment.AppointmentStatus status);
    List<ViewAppointment> findByTenantIdAndHouseIdAndStatusNotIn(Long tenantId, Long houseId, List<ViewAppointment.AppointmentStatus> statuses);
}

