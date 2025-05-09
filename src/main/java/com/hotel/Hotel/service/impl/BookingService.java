package com.hotel.Hotel.service.impl;

import com.hotel.Hotel.dto.BookingDTO;
import com.hotel.Hotel.dto.Response;
import com.hotel.Hotel.entity.Booking;
import com.hotel.Hotel.entity.Room;
import com.hotel.Hotel.entity.User;
import com.hotel.Hotel.exception.OurException;
import com.hotel.Hotel.repo.BookingRepository;
import com.hotel.Hotel.repo.RoomRepository;
import com.hotel.Hotel.repo.UserRepository;
import com.hotel.Hotel.service.interfac.IBookingService;
import com.hotel.Hotel.service.interfac.IRoomService;
import com.hotel.Hotel.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService implements IBookingService {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private IRoomService roomService;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public Response saveBooking(Long roomId, Long userId, Booking bookingRequest) {
        Response response = new Response();
        try {
            if (bookingRequest.getCheckOutDate().isBefore(bookingRequest.getCheckInDate())) {
                throw new IllegalArgumentException("Chek in date must come after check out date");
            }
            Room room = roomRepository.findById(roomId).orElseThrow(() -> new OurException("Room Not Found"));
            User user = userRepository.findById(userId).orElseThrow(() -> new OurException("User Not Found"));

            List<Booking> existingBooking = room.getBookings();
            if (!roomIsAvailable(bookingRequest, existingBooking)) {
                throw new OurException("Room not Available for selected date range");
            }

            bookingRequest.setRoom(room);
            bookingRequest.setUser(user);
            String bookingConfirmationCode = Utils.generateRandomConfirmationCode(10);
            bookingRequest.setBookingConfirmationCode(bookingConfirmationCode);
            bookingRepository.save(bookingRequest);
            response.setStatusCode(200);
            response.setMessage("successful");
            response.setBookingConfirmationCode(bookingConfirmationCode);

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error Saving a booking: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response findBookingByConfirmationCode(String confirmationCode) {
        Response response = new Response();
        try {
            Booking booking = bookingRepository.findByBookingConfirmationCode(confirmationCode).orElseThrow(() -> new OurException("Booking Not Found"));
//            BookingDTO bookingDTO = Utils.mapBookingEntityToBookingDTO(booking);
            BookingDTO bookingDTO = Utils.mapBookingEntityToBookingDTOPlusBookedRoom(booking, true);
            response.setStatusCode(200);
            response.setMessage("successful");
            response.setBooking(bookingDTO);

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error Finding a booking: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response getAllBooking() {
        Response response = new Response();
        try {
            List<Booking> bookingList = bookingRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            List<BookingDTO> bookingDTOList = Utils.mapBookingListEntityToBookingListDTO(bookingList);
            response.setStatusCode(200);
            response.setMessage("successful");
            response.setBookingList(bookingDTOList);

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error Getting a booking: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response cancelBooking(Long bookingId) {
        Response response = new Response();
        try {
            bookingRepository.findById(bookingId).orElseThrow(() -> new OurException("Booking Does Not Exist"));
            bookingRepository.deleteById(bookingId);
            response.setStatusCode(200);
            response.setMessage("successful");

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error Cancelling a booking: " + e.getMessage());
        }
        return response;
    }

//    private boolean roomIsAvailable(Booking bookingRequest, List<Booking> existingBookings) {
//        return existingBookings.stream()
//                .noneMatch(existingBooking ->
//                        bookingRequest.getCheckOutDate().equals(existingBooking.getCheckInDate())
//                                || bookingRequest.getCheckOutDate().isBefore(existingBooking.getCheckOutDate())
//                                || (bookingRequest.getCheckInDate().isAfter(existingBooking.getCheckInDate())
//                                && bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckOutDate()))
//                                || (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate())
//
//                                && bookingRequest.getCheckOutDate().equals(existingBooking.getCheckOutDate()))
//                                || (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate())
//
//                                && bookingRequest.getCheckOutDate().isAfter(existingBooking.getCheckOutDate()))
//
//                                || (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate())
//                                && bookingRequest.getCheckOutDate().equals(existingBooking.getCheckInDate()))
//
//                                || (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate())
//                                && bookingRequest.getCheckOutDate().equals(bookingRequest.getCheckInDate()))
//                );
//    }

    private boolean roomIsAvailable(Booking bookingRequest, List<Booking> existingBookings) {
        return existingBookings.stream()
                .noneMatch(existing ->
                        bookingRequest.getCheckInDate().isBefore(existing.getCheckOutDate()) &&
                                bookingRequest.getCheckOutDate().isAfter(existing.getCheckInDate())
                );
    }

}
