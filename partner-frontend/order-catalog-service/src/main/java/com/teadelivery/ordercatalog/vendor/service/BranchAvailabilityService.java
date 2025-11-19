package com.teadelivery.ordercatalog.vendor.service;

import com.teadelivery.ordercatalog.common.exception.BranchNotFoundException;
import com.teadelivery.ordercatalog.common.exception.UnauthorizedException;
import com.teadelivery.ordercatalog.vendor.dto.*;
import com.teadelivery.ordercatalog.vendor.mapper.BranchMapper;
import com.teadelivery.ordercatalog.vendor.model.VendorBranch;
import com.teadelivery.ordercatalog.vendor.repository.VendorBranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class BranchAvailabilityService {
    
    private final VendorBranchRepository branchRepository;
    
    @Transactional
    public BranchResponse updateOperatingHours(Long branchId, OperatingHoursRequest request, UUID requestingUserId) {
        log.info("Updating operating hours for branch: {}", branchId);
        
        VendorBranch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
        
        if (!branch.getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized to update operating hours for this branch");
        }
        
        // Validate no overlapping slots
        if (request.hasOverlappingSlots()) {
            throw new RuntimeException("Operating hours have overlapping time slots");
        }
        
        // Validate time ranges for all days
        for (Map.Entry<String, List<OperatingHoursRequest.TimeSlotRequest>> entry : request.getHours().entrySet()) {
            for (OperatingHoursRequest.TimeSlotRequest slot : entry.getValue()) {
                if (!slot.isValidTimeRange()) {
                    throw new RuntimeException("Invalid time range for " + entry.getKey());
                }
            }
        }
        
        // Convert to JSONB format
        Map<String, Object> operatingHours = convertToJsonb(request);
        branch.setOperatingHours(operatingHours);
        
        // Auto-update is_open based on current time
        updateBranchOpenStatus(branch);
        
        VendorBranch updatedBranch = branchRepository.save(branch);
        
        log.info("Operating hours updated for branch: {}", branchId);
        return BranchMapper.toResponse(updatedBranch);
    }
    
    @Transactional
    public BranchResponse toggleBranchStatus(Long branchId, BranchStatusRequest request, UUID requestingUserId) {
        log.info("Toggling branch status for branch: {}", branchId);
        
        VendorBranch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
        
        if (!branch.getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized to toggle status for this branch");
        }
        
        branch.setIsOpen(request.getIsOpen());
        VendorBranch updatedBranch = branchRepository.save(branch);
        
        log.info("Branch status toggled for branch: {}", branchId);
        return BranchMapper.toResponse(updatedBranch);
    }
    
    @Transactional(readOnly = true)
    public BranchAvailabilityResponse checkAvailability(Long branchId) {
        log.info("Checking availability for branch: {}", branchId);
        
        VendorBranch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
        
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Kolkata"));
        DayOfWeek today = LocalDate.now(ZoneId.of("Asia/Kolkata")).getDayOfWeek();
        
        List<Map<String, String>> todayHours = getOperatingHoursForDay(branch, today);
        boolean isWithinOperatingHours = isOpenAtTime(todayHours, now);
        
        String currentStatus = determineStatus(branch.getIsOpen(), isWithinOperatingHours);
        
        BranchAvailabilityResponse response = BranchAvailabilityResponse.builder()
            .branchId(branchId)
            .isOpen(branch.getIsOpen())
            .isActive(branch.getIsActive())
            .isWithinOperatingHours(isWithinOperatingHours)
            .currentStatus(currentStatus)
            .todayHours(todayHours)
            .nextOpenTime(findNextOpenTime(branch, now, today))
            .nextCloseTime(findNextCloseTime(todayHours, now))
            .build();
        
        return response;
    }
    
    @Transactional
    public void updateBranchOpenStatus(VendorBranch branch) {
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Kolkata"));
        DayOfWeek today = LocalDate.now(ZoneId.of("Asia/Kolkata")).getDayOfWeek();
        
        List<Map<String, String>> todayHours = getOperatingHoursForDay(branch, today);
        boolean shouldBeOpen = isOpenAtTime(todayHours, now);
        
        branch.setIsOpen(shouldBeOpen);
    }
    
    private String determineStatus(Boolean isOpen, boolean isWithinOperatingHours) {
        if (!isWithinOperatingHours) {
            return "CLOSED";
        }
        return isOpen ? "OPEN" : "OFFLINE";
    }
    
    private List<Map<String, String>> getOperatingHoursForDay(VendorBranch branch, DayOfWeek day) {
        Map<String, Object> hours = branch.getOperatingHours();
        if (hours == null) {
            return new ArrayList<>();
        }
        
        Object dayHours = hours.get(day.toString());
        if (dayHours instanceof List) {
            List<?> list = (List<?>) dayHours;
            List<Map<String, String>> result = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map) {
                    result.add((Map<String, String>) item);
                }
            }
            return result;
        }
        return new ArrayList<>();
    }
    
    private boolean isOpenAtTime(List<Map<String, String>> todayHours, LocalTime now) {
        for (Map<String, String> slot : todayHours) {
            String open = slot.get("open");
            String close = slot.get("close");
            
            if (open != null && close != null) {
                LocalTime openTime = LocalTime.parse(open);
                LocalTime closeTime = LocalTime.parse(close);
                
                if (now.isAfter(openTime) && now.isBefore(closeTime)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private LocalTime findNextOpenTime(VendorBranch branch, LocalTime now, DayOfWeek today) {
        // Simple implementation - find next opening time in next 7 days
        for (int i = 0; i < 7; i++) {
            DayOfWeek checkDay = DayOfWeek.of(((today.getValue() - 1 + i) % 7) + 1);
            List<Map<String, String>> hours = getOperatingHoursForDay(branch, checkDay);
            
            if (!hours.isEmpty()) {
                Map<String, String> firstSlot = hours.get(0);
                String openTime = firstSlot.get("open");
                if (openTime != null) {
                    return LocalTime.parse(openTime);
                }
            }
        }
        return null;
    }
    
    private LocalTime findNextCloseTime(List<Map<String, String>> todayHours, LocalTime now) {
        for (Map<String, String> slot : todayHours) {
            String open = slot.get("open");
            String close = slot.get("close");
            
            if (open != null && close != null) {
                LocalTime openTime = LocalTime.parse(open);
                LocalTime closeTime = LocalTime.parse(close);
                
                if (now.isBefore(closeTime)) {
                    return closeTime;
                }
            }
        }
        return null;
    }
    
    private Map<String, Object> convertToJsonb(OperatingHoursRequest request) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, List<OperatingHoursRequest.TimeSlotRequest>> entry : request.getHours().entrySet()) {
            List<Map<String, String>> slots = new ArrayList<>();
            for (OperatingHoursRequest.TimeSlotRequest slot : entry.getValue()) {
                Map<String, String> slotMap = new HashMap<>();
                slotMap.put("open", slot.getOpen());
                slotMap.put("close", slot.getClose());
                slots.add(slotMap);
            }
            result.put(entry.getKey(), slots);
        }
        return result;
    }
}
