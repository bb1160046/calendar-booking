package com.accoladehq.calendar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.accoladehq.calendar.entity.CalendarOwner;

import java.util.Optional;

/**
 * Repository for managing {@link CalendarOwner} entities.
 * <p>
 * Provides operations to retrieve calendar owner details based on specific fields.
 * </p>
 */
public interface CalendarOwnerRepository extends JpaRepository<CalendarOwner, Long> {

    /**
     * Finds a calendar owner by their username.
     *
     * @param username the username of the calendar owner
     * @return an {@link Optional} containing the matching {@link CalendarOwner}, or empty if none found
     */
    Optional<CalendarOwner> findByUsername(String username);
}
