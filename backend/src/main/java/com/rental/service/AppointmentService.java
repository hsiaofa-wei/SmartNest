package com.rental.service;

import com.rental.entity.ViewAppointment;
import com.rental.repository.ViewAppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Arrays;

@Service
public class AppointmentService {
    @Autowired
    private ViewAppointmentRepository appointmentRepository;

    public Page<ViewAppointment> getAppointmentsByTenant(Long tenantId, Pageable pageable) {
        return appointmentRepository.findByTenantId(tenantId, pageable);
    }

    public Page<ViewAppointment> getAppointmentsByLandlord(Long landlordId, String status, String startDate, String endDate, Pageable pageable) {
        if (status != null && !status.isEmpty()) {
            ViewAppointment.AppointmentStatus appointmentStatus = ViewAppointment.AppointmentStatus.valueOf(status);
            return appointmentRepository.findByLandlordIdAndStatus(landlordId, appointmentStatus, pageable);
        }
        return appointmentRepository.findByLandlordId(landlordId, pageable);
    }

    @Transactional
    public ViewAppointment createAppointment(ViewAppointment appointment) {
        // 检查是否已存在有效的预约（未取消、未拒绝、未完成的预约）
        List<ViewAppointment.AppointmentStatus> invalidStatuses = Arrays.asList(
            ViewAppointment.AppointmentStatus.CANCELLED,
            ViewAppointment.AppointmentStatus.REJECTED,
            ViewAppointment.AppointmentStatus.COMPLETED
        );
        
        List<ViewAppointment> existingAppointments = appointmentRepository.findByTenantIdAndHouseIdAndStatusNotIn(
            appointment.getTenantId(),
            appointment.getHouseId(),
            invalidStatuses
        );
        
        // 检查是否有未处理或已确认的预约
        if (existingAppointments != null && !existingAppointments.isEmpty()) {
            throw new RuntimeException("该房源已存在有效预约");
        }
        
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public void updateAppointmentStatus(Long id, ViewAppointment.AppointmentStatus status, String reply) {
        ViewAppointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("预约不存在"));
        appointment.setStatus(status);
        if (reply != null) {
            appointment.setLandlordReply(reply);
        }
        appointmentRepository.save(appointment);
    }
    
    public List<ViewAppointment> getConfirmedAppointments(Long tenantId, Long houseId) {
        return appointmentRepository.findByTenantIdAndHouseIdAndStatus(tenantId, houseId, ViewAppointment.AppointmentStatus.CONFIRMED);
    }
}

