package com.accoladehq.calendar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.accoladehq.calendar.entity.AvailabilityRule;
import com.accoladehq.calendar.entity.CalendarOwner;

import java.util.List;

/**
 * Repository for managing {@link AvailabilityRule} entities.
 * <p>
 * Provides query and delete operations related to an owner's availability rules.
 * </p>
 */
public interface AvailabilityRuleRepository extends JpaRepository<AvailabilityRule, Long> {

    /**
     * Finds all availability rules for a given owner.
     *
     * @param owner the calendar owner
     * @return list of availability rules belonging to the owner
     */
    List<AvailabilityRule> findByOwner(CalendarOwner owner);

    /**
     * Deletes all availability rules for a given owner.
     *
     * @param owner the calendar owner
     */
    void deleteByOwner(CalendarOwner owner);
}
