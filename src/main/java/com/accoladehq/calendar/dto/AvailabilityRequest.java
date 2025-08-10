package com.accoladehq.calendar.dto;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request object for setting availability for a calendar owner.
 * <p>
 * This DTO is used when the calendar owner specifies the time range
 * during which they are available for booking appointments.
 * The availability is defined by a start and end time in 24-hour format.
 * </p>
 *
 * <p><b>Example JSON:</b></p>
 * <pre>
 * {
 *   "username": "john_doe",
 *   "startTime": "10:00",
 *   "endTime": "17:00"
 * }
 * </pre>
 *
 * <p>
 * Constraints:
 * <ul>
 *   <li>Start time must be before end time.</li>
 *   <li>Times must be aligned to the hour (e.g., 10:00, 15:00).</li>
 *   <li>Minimum availability window is 1 hour.</li>
 * </ul>
 */
@Data
public class AvailabilityRequest {

    /**
     * Unique username of the calendar owner.
     * <p>
     * This is used to identify which owner's availability is being set.
     * </p>
     */
    @NotNull
    private String username;

    /**
     * Start time of the availability window in {@code HH:mm} format.
     * <p>
     * Must be before {@link #endTime}.
     * </p>
     */
    @NotNull
    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string", example = "10:00", description = "Start time in HH:mm format")
    private LocalTime startTime;

    /**
     * End time of the availability window in {@code HH:mm} format.
     * <p>
     * Must be after {@link #startTime}.
     * </p>
     */
    @NotNull
    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string", example = "17:00", description = "End time in HH:mm format")
    private LocalTime endTime;

    /**
     * Default no-args constructor.
     */
    public AvailabilityRequest() {
    }

    /**
     * Constructs a new availability request with the given username and time range.
     *
     * @param username  the calendar owner's username
     * @param startTime the start time of the availability window
     * @param endTime   the end time of the availability window
     */
    public AvailabilityRequest(String username, LocalTime startTime, LocalTime endTime) {
        this.username = username;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
