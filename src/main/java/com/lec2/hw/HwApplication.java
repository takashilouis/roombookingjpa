package com.lec2.hw;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.lec2.hw.model.Booking;
import com.lec2.hw.model.Room;
import com.lec2.hw.repository.RoomRepository;
import com.lec2.hw.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class HwApplication {

	private static final int TRANSACTIONS_PER_MACHINE = 5;

	public static class BookingRunnable implements Runnable{
		private final int userId;
		private final int roomId;
		private final UserService service;

		public BookingRunnable(UserService service, int userId, int roomId){
			this.userId = userId;
			this.service = service;
			this.roomId = roomId;
		}

		public void run(){
			Booking booking = service.optimisticBookRoom(userId, roomId);
			//Booking booking = service.pessimisticBookRoom(userId, roomId);
			log.info("userId = {} bookingId = {}", userId, booking.getUserId());
		}
	}
	public static void main(String[] args) throws InterruptedException{
		ConfigurableApplicationContext context = SpringApplication.run(HwApplication.class, args);

		UserService service = context.getBean(UserService.class);
		RoomRepository roomRepository = context.getBean(RoomRepository.class);
		
		Room createRoom = roomRepository.save(Room.builder().room("Room1").available(true).build());
		log.info("created Room {} ", createRoom);

		List<Thread> allThreads = new ArrayList<>();
		for(int userId = 1;userId <= TRANSACTIONS_PER_MACHINE; userId++){
			Thread th = new Thread(new BookingRunnable(service, userId, createRoom.getId()));
			log.info("start thread userId={}", userId);
			th.start();
			allThreads.add(th);
		}

		for(Thread th : allThreads){
			th.join();
		}
	}

}
