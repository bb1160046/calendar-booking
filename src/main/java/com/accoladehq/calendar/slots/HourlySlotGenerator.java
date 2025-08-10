package com.accoladehq.calendar.slots;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.accoladehq.calendar.dto.SlotDto;

/**
 * Slot generator that produces fixed hourly time slots (60 minutes each).
 * <p>
 * Slots start at the given {@code windowStart} time and continue at
 * 60-minute intervals until (but not including) the {@code windowEnd} time.
 * The generator ensures that:
 * <ul>
 *   <li>Slots already booked are excluded.</li>
 *   <li>If the given date is today, slots that have already started are excluded.</li>
 * </ul>
 */
@Component
public class HourlySlotGenerator implements SlotGenerator {

    /**
     * Generates available hourly slots for the given date and time window.
     *
     * @param date          the date for which to generate slots
     * @param windowStart   the start time of the availability window (inclusive)
     * @param windowEnd     the end time of the availability window (exclusive for slot start)
     * @param bookedStarts  a list of start times that are already booked
     * @return a list of {@link SlotDto} objects representing available slots
     */
    @Override
    public List<SlotDto> generateSlots(LocalDate date, LocalTime windowStart, LocalTime windowEnd, List<LocalTime> bookedStarts) {
        List<SlotDto> results = new ArrayList<>();
        
        // Validate time window
        if (windowStart == null || windowEnd == null || !windowStart.isBefore(windowEnd)) {
            return results;
        }
        LocalTime nowTime = LocalTime.now();
        for (LocalTime slotStart = windowStart; !slotStart.plusHours(1).isAfter(windowEnd); slotStart = slotStart.plusHours(1)) {
            // Skip if slot is already booked
            if (bookedStarts.contains(slotStart)) {
                continue;
            }
            // If the date is today, skip any slot that has already started
            if (date.isEqual(LocalDate.now()) && !slotStart.isAfter(nowTime)) {
                continue;
            }
            results.add(new SlotDto(date, slotStart, slotStart.plusHours(1)));
        }
        return results;
    }
}
