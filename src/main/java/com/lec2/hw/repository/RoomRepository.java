package com.lec2.hw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lec2.hw.model.Room;

import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;

public interface RoomRepository extends JpaRepository<Room, Integer>{
    Room findOneByIdAndAvailable(Integer id, boolean available);

    @Modifying
    @Transactional
    @Query("UPDATE Room r SET r.available = false, r.version = r.version + 1 WHERE r.id = :roomId AND r.version = :version")
    int updateRoomAsUnavailable(@Param("roomId") int roomId, @Param("version") int version);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :id AND r.available = :available")
    Room findOneByIdAndAvailableWithLock(@Param("id") Integer id, @Param("available") boolean available);
}