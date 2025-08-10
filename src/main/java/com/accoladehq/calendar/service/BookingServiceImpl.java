package com.accoladehq.calendar.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.accoladehq.calendar.dto.AvailabilityRequest;
import com.accoladehq.calendar.dto.BookRequest;
import com.accoladehq.calendar.dto.SearchSlotsRequest;
import com.accoladehq.calendar.dto.SlotDto;
import com.accoladehq.calendar.entity.Appointment;
import com.accoladehq.calendar.entity.AvailabilityRule;
import com.accoladehq.calendar.entity.CalendarOwner;
import com.accoladehq.calendar.repository.AppointmentRepository;
import com.accoladehq.calendar.repository.AvailabilityRuleRepository;
import com.accoladehq.calendar.repository.CalendarOwnerRepository;
import com.accoladehq.calendar.slots.SlotGenerator;

import jakarta.transaction.Transactional;
import java.time.*;
import java.util.*;

/**
 * Implementation of {@link BookingService} for managing calendar owners,
 * availability, and appointments.
 */
@Service
public class BookingServiceImpl implements BookingService {

	private static final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);

	@Autowired
	private CalendarOwnerRepository ownerRepo;

	@Autowired
	private AvailabilityRuleRepository availabilityRepo;

	@Autowired
	private AppointmentRepository appointmentRepo;

	@Autowired
	private SlotGenerator slotGenerator;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void upsertOwnerIfNotExists(String username, String displayName) {
		try {
			ownerRepo.findByUsername(username).orElseGet(() -> {
				CalendarOwner o = new CalendarOwner();
				o.setUsername(username);
				o.setDisplayName(displayName);
				return ownerRepo.save(o);
			});
		} catch (Exception e) {
			logger.error("Error creating/updating owner: {}", username, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<String> addAvailability(AvailabilityRequest req) {
		try {
			CalendarOwner owner = ownerRepo.findByUsername(req.getUsername()).orElse(null);
			if (owner == null) {
				return ResponseEntity.badRequest().body("Owner not found");
			}
			if (!req.getStartTime().isBefore(req.getEndTime())) {
				return ResponseEntity.badRequest().body("Start time must be before end time");
			}
			if (Duration.between(req.getStartTime(), req.getEndTime()).toMinutes() < 60) {
				return ResponseEntity.badRequest().body("Availability window must be at least 1 hour");
			}
			if (req.getStartTime().getMinute() != 0 || req.getEndTime().getMinute() != 0) {
				return ResponseEntity.badRequest().body("Start and end times must be on the hour");
			}

			// Only one rule per owner → remove old rule and replace it
			availabilityRepo.deleteByOwner(owner);

			AvailabilityRule rule = new AvailabilityRule();
			rule.setOwner(owner);
			rule.setStartTime(req.getStartTime());
			rule.setEndTime(req.getEndTime());
			availabilityRepo.save(rule);

			return ResponseEntity.ok("Availability saved successfully for all days");
		} catch (Exception e) {
			logger.error("Error adding availability for user: {}", req.getUsername(), e);
			return ResponseEntity.internalServerError().body("Error saving availability");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SlotDto> searchAvailableSlots(SearchSlotsRequest req) {
		try {
			CalendarOwner owner = ownerRepo.findByUsername(req.getUsername()).orElse(null);
			if (owner == null) {
				logger.warn("Owner not found for username: {}", req.getUsername());
				return Collections.emptyList();
			}

			LocalDate date = req.getDate();
			if (date.isBefore(LocalDate.now())) {
				logger.warn("Attempt to search availability for past date: {}", date);
				return Collections.emptyList();
			}

			List<AvailabilityRule> rules = availabilityRepo.findByOwner(owner);
			if (rules.isEmpty()) {
				return Collections.emptyList();
			}

			List<Appointment> booked = appointmentRepo.findByOwnerAndDate(owner, date);
			List<LocalTime> bookedStarts = booked.stream().map(Appointment::getStartTime).toList();

			List<SlotDto> result = new ArrayList<>();
			for (AvailabilityRule rule : rules) {
				result.addAll(slotGenerator.generateSlots(date, rule.getStartTime(), rule.getEndTime(), bookedStarts));
			}

			result.sort(Comparator.comparing(SlotDto::getDate).thenComparing(SlotDto::getStart));
			return result;
		} catch (Exception e) {
			logger.error("Error searching available slots for user: {}", req.getUsername(), e);
			return Collections.emptyList();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public SlotDto bookAppointment(BookRequest req) {
		try {
			CalendarOwner owner = ownerRepo.findByUsername(req.getUsername()).orElse(null);
			if (owner == null) {
				logger.warn("Owner not found for username: {}", req.getUsername());
				return null;
			}

			LocalDate date = req.getDate();
			LocalTime start = req.getStartTime();
			LocalTime end = start.plusHours(1);

			if (date.isBefore(LocalDate.now())) {
				logger.warn("Attempt to book appointment in the past: {}", date);
				return null;
			}

			// Check if the slot is still available
			SearchSlotsRequest searchReq = new SearchSlotsRequest();
			searchReq.setUsername(req.getUsername());
			searchReq.setDate(date);

			List<SlotDto> available = searchAvailableSlots(searchReq);
			boolean ok = available.stream().anyMatch(s -> s.getStart().equals(start) && s.getEnd().equals(end));
			if (!ok) {
				logger.warn("Slot not available for booking: {} {} - {}", date, start, end);
				return null;
			}

			// Double-check uniqueness (race condition check)
			if (appointmentRepo.findByOwnerAndDateAndStartTime(owner, date, start).isPresent()) {
				logger.warn("Slot already booked (race condition check): {} {} - {}", date, start, end);
				return null;
			}

			// Create appointment
			Appointment appt = new Appointment();
			appt.setOwner(owner);
			appt.setDate(date);
			appt.setStartTime(start);
			appt.setEndTime(end);
			appt.setInviteeName(req.getInviteeName());
			appt.setInviteeEmail(req.getInviteeEmail());

			Appointment saved = appointmentRepo.save(appt);
			return new SlotDto(saved.getDate(), saved.getStartTime(), saved.getEndTime());

		} catch (Exception e) {
			logger.error("Error booking appointment for user: {}", req.getUsername(), e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Appointment> listUpcoming(String username) {
		try {
			CalendarOwner owner = ownerRepo.findByUsername(username).orElse(null);
			if (owner == null) {
				logger.warn("Owner not found for username: {}", username);
				return Collections.emptyList();
			}
			return appointmentRepo.findByOwnerAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(owner, LocalDate.now());
		} catch (Exception e) {
			logger.error("Error listing upcoming appointments for user: {}", username, e);
			return Collections.emptyList();
		}
	}

}
