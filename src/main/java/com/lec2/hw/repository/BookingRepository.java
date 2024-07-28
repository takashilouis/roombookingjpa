package com.lec2.hw.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lec2.hw.model.Booking;

public interface BookingRepository extends JpaRepository<Booking,Integer> {

}
