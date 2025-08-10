package com.accoladehq.calendar;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.accoladehq.calendar.dto.*;
import com.accoladehq.calendar.entity.*;
import com.accoladehq.calendar.repository.*;
import com.accoladehq.calendar.service.BookingServiceImpl;
import com.accoladehq.calendar.slots.SlotGenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.*;
import java.util.*;

class BookingServiceImplTest {

    @InjectMocks
    BookingServiceImpl service;

    @Mock
    CalendarOwnerRepository ownerRepo;

    @Mock
    AvailabilityRuleRepository availabilityRepo;

    @Mock
    AppointmentRepository appointmentRepo;

    @Mock
    SlotGenerator slotGenerator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUpsertOwnerIfNotExists_whenOwnerExists() {
        CalendarOwner existingOwner = new CalendarOwner();
        existingOwner.setUsername("john");

        when(ownerRepo.findByUsername("john")).thenReturn(Optional.of(existingOwner));

        service.upsertOwnerIfNotExists("john", "John Doe");

        verify(ownerRepo, never()).save(any());
    }

    @Test
    void testUpsertOwnerIfNotExists_whenOwnerDoesNotExist() {
        when(ownerRepo.findByUsername("john")).thenReturn(Optional.empty());
        when(ownerRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.upsertOwnerIfNotExists("john", "John Doe");

        verify(ownerRepo).save(argThat(o -> 
            o.getUsername().equals("john") && o.getDisplayName().equals("John Doe")));
    }

    @Test
    void testAddAvailability_success() {
        CalendarOwner owner = new CalendarOwner();
        owner.setUsername("john");

        AvailabilityRequest req = new AvailabilityRequest("john", LocalTime.of(10, 0), LocalTime.of(17, 0));

        when(ownerRepo.findByUsername("john")).thenReturn(Optional.of(owner));

        ResponseEntity<String> response = service.addAvailability(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Availability saved successfully for all days");

        verify(availabilityRepo).deleteByOwner(owner);
        verify(availabilityRepo).save(any(AvailabilityRule.class));
    }

    @Test
    void testAddAvailability_ownerNotFound() {
        when(ownerRepo.findByUsername("john")).thenReturn(Optional.empty());

        AvailabilityRequest req = new AvailabilityRequest("john", LocalTime.of(10, 0), LocalTime.of(17, 0));

        ResponseEntity<String> response = service.addAvailability(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Owner not found");
    }

    @Test
    void testAddAvailability_invalidTimes() {
        CalendarOwner owner = new CalendarOwner();
        when(ownerRepo.findByUsername("john")).thenReturn(Optional.of(owner));

        // start time after end time
        AvailabilityRequest req1 = new AvailabilityRequest("john", LocalTime.of(18, 0), LocalTime.of(17, 0));
        ResponseEntity<String> r1 = service.addAvailability(req1);
        assertThat(r1.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(r1.getBody()).isEqualTo("Start time must be before end time");

        // duration less than 1 hour
        AvailabilityRequest req2 = new AvailabilityRequest("john", LocalTime.of(10, 0), LocalTime.of(10, 30));
        ResponseEntity<String> r2 = service.addAvailability(req2);
        assertThat(r2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(r2.getBody()).isEqualTo("Availability window must be at least 1 hour");

        // minutes not zero
        AvailabilityRequest req3 = new AvailabilityRequest("john", LocalTime.of(10, 15), LocalTime.of(12, 0));
        ResponseEntity<String> r3 = service.addAvailability(req3);
        assertThat(r3.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(r3.getBody()).isEqualTo("Start and end times must be on the hour");
    }

    @Test
    void testSearchAvailableSlots_success() {
        CalendarOwner owner = new CalendarOwner();
        owner.setUsername("john");

        LocalDate date = LocalDate.now().plusDays(1);
        when(ownerRepo.findByUsername("john")).thenReturn(Optional.of(owner));

        AvailabilityRule rule = new AvailabilityRule();
        rule.setOwner(owner);
        rule.setStartTime(LocalTime.of(10, 0));
        rule.setEndTime(LocalTime.of(18, 0));
        when(availabilityRepo.findByOwner(owner)).thenReturn(List.of(rule));

        Appointment appt = new Appointment();
        appt.setStartTime(LocalTime.of(12, 0));
        when(appointmentRepo.findByOwnerAndDate(owner, date)).thenReturn(List.of(appt));

        List<LocalTime> bookedStarts = List.of(LocalTime.of(12, 0));

        List<SlotDto> slots = List.of(
                new SlotDto(date, LocalTime.of(10, 0), LocalTime.of(11, 0)),
                new SlotDto(date, LocalTime.of(11, 0), LocalTime.of(12, 0))
        );

        when(slotGenerator.generateSlots(eq(date), eq(rule.getStartTime()), eq(rule.getEndTime()), eq(bookedStarts)))
                .thenReturn(slots);

        SearchSlotsRequest req = new SearchSlotsRequest();
        req.setUsername("john");
        req.setDate(date);

        List<SlotDto> result = service.searchAvailableSlots(req);

        assertEquals(2, result.size());
        assertEquals(LocalTime.of(10, 0), result.get(0).getStart());
    }

    @Test
    void testSearchAvailableSlots_ownerNotFound() {
        when(ownerRepo.findByUsername("john")).thenReturn(Optional.empty());

        SearchSlotsRequest req = new SearchSlotsRequest();
        req.setUsername("john");
        req.setDate(LocalDate.now().plusDays(1));

        List<SlotDto> result = service.searchAvailableSlots(req);
        assertTrue(result.isEmpty());
    }

    @Test
    void testBookAppointment_success() {
        CalendarOwner owner = new CalendarOwner();
        owner.setUsername("john");

        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = start.plusHours(1);

        BookRequest req = new BookRequest();
        req.setUsername("john");
        req.setDate(date);
        req.setStartTime(start);
        req.setInviteeName("Alice");
        req.setInviteeEmail("alice@example.com");

        when(ownerRepo.findByUsername("john")).thenReturn(Optional.of(owner));

        // Mock searchAvailableSlots to include the slot requested
        SlotDto slotDto = new SlotDto(date, start, end);
        BookingServiceImpl spyService = Mockito.spy(service);
        doReturn(List.of(slotDto)).when(spyService).searchAvailableSlots(any());

        when(appointmentRepo.findByOwnerAndDateAndStartTime(owner, date, start)).thenReturn(Optional.empty());

        Appointment savedAppt = new Appointment();
        savedAppt.setOwner(owner);
        savedAppt.setDate(date);
        savedAppt.setStartTime(start);
        savedAppt.setEndTime(end);
        savedAppt.setInviteeName("Alice");
        savedAppt.setInviteeEmail("alice@example.com");

        when(appointmentRepo.save(any())).thenReturn(savedAppt);

        // Use spy for testing bookAppointment
        SlotDto booked = spyService.bookAppointment(req);

        assertNotNull(booked);
        assertEquals(start, booked.getStart());
        assertEquals(end, booked.getEnd());
    }

    @Test
    void testBookAppointment_slotNotAvailable() {
        CalendarOwner owner = new CalendarOwner();
        owner.setUsername("john");

        BookRequest req = new BookRequest();
        req.setUsername("john");
        req.setDate(LocalDate.now().plusDays(1));
        req.setStartTime(LocalTime.of(10, 0));

        when(ownerRepo.findByUsername("john")).thenReturn(Optional.of(owner));

        BookingServiceImpl spyService = Mockito.spy(service);
        doReturn(Collections.emptyList()).when(spyService).searchAvailableSlots(any());

        SlotDto booked = spyService.bookAppointment(req);
        assertNull(booked);
    }

    @Test
    void testBookAppointment_slotAlreadyBooked() {
        CalendarOwner owner = new CalendarOwner();
        owner.setUsername("john");

        BookRequest req = new BookRequest();
        req.setUsername("john");
        req.setDate(LocalDate.now().plusDays(1));
        req.setStartTime(LocalTime.of(10, 0));

        when(ownerRepo.findByUsername("john")).thenReturn(Optional.of(owner));

        SlotDto slotDto = new SlotDto(req.getDate(), req.getStartTime(), req.getStartTime().plusHours(1));
        BookingServiceImpl spyService = Mockito.spy(service);
        doReturn(List.of(slotDto)).when(spyService).searchAvailableSlots(any());

        when(appointmentRepo.findByOwnerAndDateAndStartTime(owner, req.getDate(), req.getStartTime()))
                .thenReturn(Optional.of(new Appointment()));

        SlotDto booked = spyService.bookAppointment(req);
        assertNull(booked);
    }

    @Test
    void testListUpcoming_success() {
        CalendarOwner owner = new CalendarOwner();
        owner.setUsername("john");

        Appointment appt = new Appointment();
        appt.setDate(LocalDate.now().plusDays(1));
        appt.setStartTime(LocalTime.of(10, 0));

        when(ownerRepo.findByUsername("john")).thenReturn(Optional.of(owner));
        when(appointmentRepo.findByOwnerAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(owner, LocalDate.now()))
                .thenReturn(List.of(appt));

        List<Appointment> result = service.listUpcoming("john");

        assertEquals(1, result.size());
        assertEquals(appt.getDate(), result.get(0).getDate());
    }

    @Test
    void testListUpcoming_ownerNotFound() {
        when(ownerRepo.findByUsername("john")).thenReturn(Optional.empty());

        List<Appointment> result = service.listUpcoming("john");

        assertTrue(result.isEmpty());
    }
}
