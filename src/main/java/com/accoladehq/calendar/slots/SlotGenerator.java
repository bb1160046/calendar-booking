package com.accoladehq.calendar.slots;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.accoladehq.calendar.dto.SlotDto;

/**
 * Strategy interface for generating available booking slots based on a given
 * availability window and a list of existing bookings.
 * <p>
 * Implementations define how slots are generated (e.g., hourly, half-hourly,
 * custom duration) and can apply rules such as skipping past times for the
 * current day or avoiding already booked times.
 */
public interface SlotGenerator {

    /**
     * Generates a list of available slots for the given date and time window.
     *
     * @param date          the date for which to generate slots
     * @param windowStart   the start time of the availability window (inclusive)
     * @param windowEnd     the end time of the availability window (exclusive for slot start)
     * @param bookedStarts  list of start times that are already booked and should be excluded
     * @return a list of {@link SlotDto} objects representing available slots; 
     *         may be empty if no slots are available or inputs are invalid
     */
    List<SlotDto> generateSlots(LocalDate date, LocalTime windowStart, LocalTime windowEnd, List<LocalTime> bookedStarts);
}
