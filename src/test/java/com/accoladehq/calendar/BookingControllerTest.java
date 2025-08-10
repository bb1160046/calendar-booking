package com.accoladehq.calendar;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.accoladehq.calendar.controller.BookingController;
import com.accoladehq.calendar.dto.AvailabilityRequest;
import com.accoladehq.calendar.dto.BookRequest;
import com.accoladehq.calendar.dto.SearchSlotsRequest;
import com.accoladehq.calendar.dto.SlotDto;
import com.accoladehq.calendar.entity.Appointment;
import com.accoladehq.calendar.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private BookingService bookingService;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void testCreateOwner() throws Exception {
		Mockito.doNothing().when(bookingService).upsertOwnerIfNotExists("john", "John Doe");

		mockMvc.perform(post("/api/owners").param("username", "john").param("displayName", "John Doe"))
				.andExpect(status().isOk()).andExpect(content().string("Owner created/exists"));
	}

	@Test
	void testAddAvailability() throws Exception {
		AvailabilityRequest req = new AvailabilityRequest("john", LocalTime.of(10, 0), LocalTime.of(17, 0));

		Mockito.when(bookingService.addAvailability(any()))
				.thenReturn(ResponseEntity.ok("Availability saved successfully"));

		mockMvc.perform(post("/api/availability").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req))).andExpect(status().isOk())
				.andExpect(content().string("Availability saved successfully"));
	}

	@Test
	void testSearchSlots() throws Exception {
		SearchSlotsRequest req = new SearchSlotsRequest();
		req.setUsername("john");
		req.setDate(LocalDate.now().plusDays(1));

		List<SlotDto> slots = Arrays.asList(new SlotDto(req.getDate(), LocalTime.of(10, 0), LocalTime.of(11, 0)),
				new SlotDto(req.getDate(), LocalTime.of(11, 0), LocalTime.of(12, 0)));

		Mockito.when(bookingService.searchAvailableSlots(any())).thenReturn(slots);

		mockMvc.perform(post("/api/slots/search").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req))).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].start").value("10:00:00"))
				.andExpect(jsonPath("$[1].start").value("11:00:00"));

	}

	@Test
	void testBookAppointment() throws Exception {
		BookRequest req = new BookRequest();
		req.setUsername("john");
		req.setDate(LocalDate.now().plusDays(1));
		req.setStartTime(LocalTime.of(10, 0));
		req.setInviteeName("Alice");
		req.setInviteeEmail("alice@example.com");

		SlotDto bookedSlot = new SlotDto(req.getDate(), req.getStartTime(), req.getStartTime().plusHours(1));

		Mockito.when(bookingService.bookAppointment(any())).thenReturn(bookedSlot);

		mockMvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req))).andExpect(status().isOk())
				.andExpect(jsonPath("$.start").value("10:00:00"));
	}

	@Test
	void testListUpcomingAppointments() throws Exception {
		Appointment appt = new Appointment();
		appt.setDate(LocalDate.now().plusDays(1));
		appt.setStartTime(LocalTime.of(10, 0));
		appt.setEndTime(LocalTime.of(11, 0));
		appt.setInviteeName("Alice");

		Mockito.when(bookingService.listUpcoming("john")).thenReturn(List.of(appt));

		mockMvc.perform(get("/api/owners/john/appointments")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].inviteeName").value("Alice"));
	}

	@Test
	void testCreateOwner_whenException_thenReturns500() throws Exception {
		doThrow(new RuntimeException("DB down")).when(bookingService).upsertOwnerIfNotExists("john", "John Doe");

		mockMvc.perform(post("/api/owners").param("username", "john").param("displayName", "John Doe"))
				.andExpect(status().isInternalServerError()).andExpect(content().string("Failed to create owner"));
	}

	@Test
	void testAddAvailability_whenException_thenReturns500() throws Exception {
		AvailabilityRequest req = new AvailabilityRequest("john", LocalTime.of(10, 0), LocalTime.of(17, 0));

		doThrow(new RuntimeException("DB error")).when(bookingService).addAvailability(any());

		mockMvc.perform(post("/api/availability").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req))).andExpect(status().isInternalServerError())
				.andExpect(content().string("Failed to add availability"));
	}

	@Test
	void testSearchSlots_whenException_thenReturns500() throws Exception {
		SearchSlotsRequest req = new SearchSlotsRequest();
		req.setUsername("john");
		req.setDate(LocalDate.now().plusDays(1));

		doThrow(new RuntimeException("Service down")).when(bookingService).searchAvailableSlots(any());

		mockMvc.perform(post("/api/slots/search").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req))).andExpect(status().isInternalServerError());
	}

	@Test
	void testBookAppointment_whenIllegalStateException_thenReturns400() throws Exception {
		BookRequest req = new BookRequest();
		req.setUsername("john");
		req.setDate(LocalDate.now().plusDays(1));
		req.setStartTime(LocalTime.of(10, 0));
		req.setInviteeName("Alice");
		req.setInviteeEmail("alice@example.com");

		doThrow(new IllegalStateException("Slot not available")).when(bookingService).bookAppointment(any());

		mockMvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req))).andExpect(status().isBadRequest())
				.andExpect(content().string("Slot not available"));
	}

	@Test
	void testBookAppointment_whenGenericException_thenReturns500() throws Exception {
		BookRequest req = new BookRequest();
		req.setUsername("john");
		req.setDate(LocalDate.now().plusDays(1));
		req.setStartTime(LocalTime.of(10, 0));
		req.setInviteeName("Alice");
		req.setInviteeEmail("alice@example.com");

		doThrow(new RuntimeException("Unexpected error")).when(bookingService).bookAppointment(any());

		mockMvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req))).andExpect(status().isInternalServerError())
				.andExpect(content().string("Failed to book appointment"));
	}

	@Test
	void testListUpcoming_whenException_thenReturns500() throws Exception {
		doThrow(new RuntimeException("DB error")).when(bookingService).listUpcoming("john");

		mockMvc.perform(get("/api/owners/john/appointments")).andExpect(status().isInternalServerError());
	}
}
