package com.accoladehq.calendar.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) representing an available or booked time slot.
 * <p>
 * This class is used to send slot details (date, start time, and end time) to
 * the client. It is typically returned by the
 * {@code Search Available Slots API} and also when confirming a booking.
 * </p>
 *
 * <p><b>Example JSON:</b></p>
 * <pre>
 * {
 *   "date": "2025-08-15",
 *   "start": "10:00",
 *   "end": "11:00"
 * }
 * </pre>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SlotDto {

    /**
     * The date of the time slot.
     */
    @Schema(example = "2025-08-15", description = "Date of the time slot in yyyy-MM-dd format")
    private LocalDate date;

    /**
     * The start time of the slot.
     */
    @Schema(example = "10:00", description = "Start time in HH:mm format")
    private LocalTime start;

    /**
     * The end time of the slot.
     */
    @Schema(example = "11:00", description = "End time in HH:mm format")
    private LocalTime end;
}
