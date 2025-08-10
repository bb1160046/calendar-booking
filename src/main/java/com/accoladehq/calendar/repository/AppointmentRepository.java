package com.accoladehq.calendar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.accoladehq.calendar.entity.Appointment;
import com.accoladehq.calendar.entity.CalendarOwner;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing {@link Appointment} entities.
 * <p>
 * Provides query methods to find appointments by owner, date, and time.
 * Used for checking availability, retrieving booked slots, and listing upcoming appointments.
 * </p>
 */
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Finds appointments for a given owner and date where the start time is within a given range.
     *
     * @param owner the calendar owner
     * @param date the appointment date
     * @param from the start of the time range (inclusive)
     * @param to the end of the time range (inclusive)
     * @return list of matching appointments
     */
    List<Appointment> findByOwnerAndDateAndStartTimeBetween(
            CalendarOwner owner, LocalDate date, LocalTime from, LocalTime to
    );

    /**
     * Finds all appointments for a given owner on a specific date.
     *
     * @param owner the calendar owner
     * @param date the appointment date
     * @return list of matching appointments
     */
    List<Appointment> findByOwnerAndDate(CalendarOwner owner, LocalDate date);

    /**
     * Finds all upcoming appointments for a given owner, sorted by date and start time.
     *
     * @param owner the calendar owner
     * @param date the earliest date to include (usually today)
     * @return sorted list of upcoming appointments
     */
    List<Appointment> findByOwnerAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(
            CalendarOwner owner, LocalDate date
    );

    /**
     * Finds a specific appointment for an owner on a given date and start time.
     *
     * @param owner the calendar owner
     * @param date the appointment date
     * @param startTime the appointment start time
     * @return optional containing the appointment if found, otherwise empty
     */
    Optional<Appointment> findByOwnerAndDateAndStartTime(
            CalendarOwner owner, LocalDate date, LocalTime startTime
    );
}
