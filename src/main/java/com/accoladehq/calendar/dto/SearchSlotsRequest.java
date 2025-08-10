package com.accoladehq.calendar.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request object for searching available time slots for a given calendar owner.
 * <p>
 * This DTO is used by an invitee (or the system) to retrieve a list of open
 * appointment slots for a specific date, based on the calendar owner's
 * availability rules and any existing bookings.
 * </p>
 *
 * <p><b>Example JSON:</b></p>
 * <pre>
 * {
 *   "username": "john_doe",
 *   "date": "2025-08-15"
 * }
 * </pre>
 *
 * <p>
 * Constraints:
 * <ul>
 *   <li>The {@code username} must correspond to an existing calendar owner.</li>
 *   <li>The {@code date} must not be in the past.</li>
 * </ul>
 */
@Data
public class SearchSlotsRequest {

    /**
     * The username of the calendar owner whose slots are being queried.
     */
    @NotNull
    private String username;

    /**
     * The date for which available time slots are requested.
     * <p>
     * Must not be in the past.
     * </p>
     */
    @NotNull
    private LocalDate date;

    /**
     * Default no-args constructor.
     */
    public SearchSlotsRequest() {
    }

    /**
     * Constructs a search request with the provided details.
     *
     * @param username the username of the calendar owner
     * @param date     the date for which to search available slots
     */
    public SearchSlotsRequest(String username, LocalDate date) {
        this.username = username;
        this.date = date;
    }
}
