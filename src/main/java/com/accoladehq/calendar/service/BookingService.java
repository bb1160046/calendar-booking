package com.accoladehq.calendar.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.accoladehq.calendar.dto.AvailabilityRequest;
import com.accoladehq.calendar.dto.BookRequest;
import com.accoladehq.calendar.dto.SearchSlotsRequest;
import com.accoladehq.calendar.dto.SlotDto;
import com.accoladehq.calendar.entity.Appointment;

/**
 * Service interface for managing calendar bookings, availability, and appointments.
 * <p>
 * Provides methods for:
 * <ul>
 *   <li>Managing calendar owners</li>
 *   <li>Setting availability rules</li>
 *   <li>Searching for available time slots</li>
 *   <li>Booking appointments</li>
 *   <li>Listing upcoming appointments</li>
 * </ul>
 */
public interface BookingService {

    /**
     * Creates or updates a calendar owner if it does not already exist.
     *
     * @param username    the unique username of the owner
     * @param displayName the display name for the owner (can be the same as username if null)
     */
    void upsertOwnerIfNotExists(String username, String displayName);

    /**
     * Adds availability for a calendar owner.
     *
     * @param req the availability request containing username, start time, and end time
     * @return a {@link ResponseEntity} containing a success or failure message
     */
    ResponseEntity<String> addAvailability(AvailabilityRequest req);

    /**
     * Searches for available slots for a given owner and date.
     *
     * @param req the search request containing username and date
     * @return a list of {@link SlotDto} representing available slots
     */
    List<SlotDto> searchAvailableSlots(SearchSlotsRequest req);

    /**
     * Books an appointment for the specified slot.
     *
     * @param req the booking request containing username, date, start time, and invitee details
     * @return the booked slot details as a {@link SlotDto}
     * @throws IllegalStateException if the slot is already booked or unavailable
     */
    SlotDto bookAppointment(BookRequest req) throws IllegalStateException;

    /**
     * Lists all upcoming appointments for a given calendar owner.
     *
     * @param username the username of the calendar owner
     * @return a list of {@link Appointment} sorted by date and start time
     */
    List<Appointment> listUpcoming(String username);
}
