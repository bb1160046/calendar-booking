package com.accoladehq.calendar;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.accoladehq.calendar.dto.SlotDto;
import com.accoladehq.calendar.slots.HourlySlotGenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HourlySlotGeneratorTest {

    private HourlySlotGenerator generator;

    @BeforeEach
    void setup() {
        generator = new HourlySlotGenerator();
    }

    @Test
    void testGenerateSlots_basic() {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(12, 0);
        List<LocalTime> booked = List.of();

        List<SlotDto> slots = generator.generateSlots(date, start, end, booked);

        assertEquals(3, slots.size());
        assertEquals(LocalTime.of(9, 0), slots.get(0).getStart());
        assertEquals(LocalTime.of(10, 0), slots.get(1).getStart());
        assertEquals(LocalTime.of(11, 0), slots.get(2).getStart());
    }

    @Test
    void testGenerateSlots_excludesBookedSlots() {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(12, 0);
        List<LocalTime> booked = List.of(LocalTime.of(10, 0));

        List<SlotDto> slots = generator.generateSlots(date, start, end, booked);

        assertEquals(2, slots.size());
        assertEquals(LocalTime.of(9, 0), slots.get(0).getStart());
        assertEquals(LocalTime.of(11, 0), slots.get(1).getStart());
    }

    @Test
    void testGenerateSlots_todayExcludesPastOrOngoingSlots() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // Define window starting a few hours before now, ending after now + 2 hours
        LocalTime windowStart = now.minusHours(2).withMinute(0).withSecond(0).withNano(0);
        LocalTime windowEnd = now.plusHours(3).withMinute(0).withSecond(0).withNano(0);

        List<LocalTime> booked = List.of();

        List<SlotDto> slots = generator.generateSlots(today, windowStart, windowEnd, booked);

        // All slots that start before or at 'now' should be excluded
        for (SlotDto slot : slots) {
            assertTrue(slot.getStart().isAfter(now),
                    "Slot start time should be after current time for today's slots");
        }
    }

    @Test
    void testGenerateSlots_invalidWindow_returnsEmpty() {
        LocalDate date = LocalDate.now().plusDays(1);

        // windowStart equals windowEnd
        List<SlotDto> slots1 = generator.generateSlots(date, LocalTime.of(10, 0), LocalTime.of(10, 0), List.of());
        assertTrue(slots1.isEmpty());

        // windowStart after windowEnd
        List<SlotDto> slots2 = generator.generateSlots(date, LocalTime.of(11, 0), LocalTime.of(10, 0), List.of());
        assertTrue(slots2.isEmpty());

        // windowStart or windowEnd null
        List<SlotDto> slots3 = generator.generateSlots(date, null, LocalTime.of(10, 0), List.of());
        assertTrue(slots3.isEmpty());

        List<SlotDto> slots4 = generator.generateSlots(date, LocalTime.of(9, 0), null, List.of());
        assertTrue(slots4.isEmpty());
    }
}

