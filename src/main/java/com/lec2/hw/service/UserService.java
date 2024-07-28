package com.lec2.hw.service;

import org.springframework.stereotype.Service;

import com.lec2.hw.model.Booking;

@Service
public interface UserService {
    Booking pessimisticBookRoom(int userId, int roomId);
    Booking optimisticBookRoom(int userId, int roomId);
}
