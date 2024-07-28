package com.lec2.hw.service;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lec2.hw.model.Booking;
import com.lec2.hw.model.Room;
import com.lec2.hw.repository.BookingRepository;
import com.lec2.hw.repository.RoomRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserServiceImpl implements UserService{
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private BookingRepository bookingRepository;

    @Transactional
    @Override
    public Booking pessimisticBookRoom(int userId, int roomId){
        Room room = roomRepository.findOneByIdAndAvailableWithLock(roomId,true);

        if (room == null || !room.isAvailable()) {
            throw new RuntimeException(String.format("user%d failed to book room %d", userId, roomId));
        }

        room.setAvailable(false);
        roomRepository.save(room);

        Booking booking = Booking.builder().roomId(roomId).userId(userId).build();
        bookingRepository.save(booking);
        log.info("Booking successful for userId = {} with bookingId = {}", userId, booking.getId());
        return booking;
    }

    @Transactional
    @Override
    public Booking optimisticBookRoom(int userId, int roomId){
        Room room = roomRepository.findOneByIdAndAvailable(roomId,true);

        Booking booking = Booking.builder().roomId(roomId).userId(userId).build();
        bookingRepository.save(booking);

        log.info("start delay");
        //delay transaction
        try{
            int delay = ThreadLocalRandom.current().nextInt(4000, 6001); // Random delay between 4000ms to 6000ms
            Thread.sleep(delay);
        } catch (InterruptedException e){
            log.error("Error while booking room", e);
        }

        log.info("Finished delay");

        int updateRows = roomRepository.updateRoomAsUnavailable(roomId, room.getVersion()); //update room availability
        log.info("userId={}, updatedRows={}", userId, updateRows);

        if(updateRows == 0){
            log.info("user = {} cannot update room, ROLLBACK!", userId);
            throw new RuntimeException(String.format("user%d failed to book room %d", userId, roomId));
        }
        log.info("user = {} successfully booked room = {}", userId, roomId);
        return booking;
    }
}
