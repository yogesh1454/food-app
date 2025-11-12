package com.teadelivery.ordercatalog.vendor.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperatingHoursRequest {
    
    @NotNull(message = "Operating hours are required")
    private Map<String, List<TimeSlotRequest>> hours;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlotRequest {
        private String open;  // HH:mm format
        private String close; // HH:mm format
        
        public boolean isValidTimeRange() {
            if (open == null || close == null) {
                return false;
            }
            try {
                String[] openParts = open.split(":");
                String[] closeParts = close.split(":");
                
                int openHour = Integer.parseInt(openParts[0]);
                int openMin = Integer.parseInt(openParts[1]);
                int closeHour = Integer.parseInt(closeParts[0]);
                int closeMin = Integer.parseInt(closeParts[1]);
                
                int openTotalMin = openHour * 60 + openMin;
                int closeTotalMin = closeHour * 60 + closeMin;
                
                return closeTotalMin > openTotalMin;
            } catch (Exception e) {
                return false;
            }
        }
    }
    
    public boolean hasOverlappingSlots() {
        for (List<TimeSlotRequest> daySlots : hours.values()) {
            for (int i = 0; i < daySlots.size(); i++) {
                for (int j = i + 1; j < daySlots.size(); j++) {
                    if (slotsOverlap(daySlots.get(i), daySlots.get(j))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private boolean slotsOverlap(TimeSlotRequest slot1, TimeSlotRequest slot2) {
        try {
            int slot1Open = timeToMinutes(slot1.open);
            int slot1Close = timeToMinutes(slot1.close);
            int slot2Open = timeToMinutes(slot2.open);
            int slot2Close = timeToMinutes(slot2.close);
            
            return !(slot1Close <= slot2Open || slot2Close <= slot1Open);
        } catch (Exception e) {
            return false;
        }
    }
    
    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }
}
