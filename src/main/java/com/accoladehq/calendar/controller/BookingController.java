package com.accoladehq.calendar.controller;

import com.accoladehq.calendar.dto.AvailabilityRequest;
import com.accoladehq.calendar.dto.BookRequest;
import com.accoladehq.calendar.dto.SearchSlotsRequest;
import com.accoladehq.calendar.dto.SlotDto;
import com.accoladehq.calendar.entity.Appointment;
import com.accoladehq.calendar.service.BookingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing calendar booking operations.
 * Handles creation of owners, setting availability, searching slots,
 * booking appointments, and listing upcoming appointments.
 */
@RestController
@RequestMapping("/api")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    /**
     * Create a calendar owner.
     * In a real system, this would be derived from authentication context.
     *
     * @param username    The unique username of the owner.
     * @param displayName Optional display name of the owner.
     * @return Response message indicating owner creation or existence.
     */
    @PostMapping("/owners")
    public ResponseEntity<String> createOwner(@RequestParam String username,
                                              @RequestParam(required = false) String displayName) {
        try {
            bookingService.upsertOwnerIfNotExists(username, displayName == null ? username : displayName);
            return ResponseEntity.ok("Owner created/exists");
        } catch (Exception e) {
            logger.error("Error creating owner [{}]: {}", username, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to create owner");
        }
    }

    /**
     * Set availability for a calendar owner.
     *
     * @param req Availability request containing start and end times.
     * @return Success or failure message.
     */
    @PostMapping("/availability")
    public ResponseEntity<String> addAvailability(@Valid @RequestBody AvailabilityRequest req) {
        try {
            return bookingService.addAvailability(req);
        } catch (Exception e) {
            logger.error("Error adding availability for owner [{}]: {}", req.getUsername(), e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to add availability");
        }
    }

    /**
     * Search available slots for a given date and owner.
     *
     * @param req Search request containing username and date.
     * @return List of available slots.
     */
    @PostMapping("/slots/search")
    public ResponseEntity<List<SlotDto>> search(@Valid @RequestBody SearchSlotsRequest req) {
        try {
            return ResponseEntity.ok(bookingService.searchAvailableSlots(req));
        } catch (Exception e) {
            logger.error("Error searching slots for owner [{}]: {}", req.getUsername(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Book an available appointment slot.
     *
     * @param req Booking request containing owner username, date, start time, and invitee details.
     * @return The booked slot details.
     */
    @PostMapping("/appointments")
    public ResponseEntity<?> book(@Valid @RequestBody BookRequest req) {
        try {
            var slot = bookingService.bookAppointment(req);
            return ResponseEntity.ok(slot);
        } catch (IllegalStateException e) {
            logger.error("Booking failed for [{}]: {}", req.getUsername(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error booking appointment for [{}]: {}", req.getUsername(), e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to book appointment");
        }
    }

    /**
     * Retrieve a list of upcoming appointments for a calendar owner.
     *
     * @param username The owner's username.
     * @return List of upcoming appointments.
     */
    @GetMapping("/owners/{username}/appointments")
    public ResponseEntity<List<Appointment>> listUpcoming(@PathVariable String username) {
        try {
            return ResponseEntity.ok(bookingService.listUpcoming(username));
        } catch (Exception e) {
            logger.error("Error fetching upcoming appointments for owner [{}]: {}", username, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
