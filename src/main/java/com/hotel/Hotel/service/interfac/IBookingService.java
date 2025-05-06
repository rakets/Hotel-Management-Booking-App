package com.hotel.Hotel.service.interfac;

import com.hotel.Hotel.dto.Response;
import com.hotel.Hotel.entity.Booking;

public interface IBookingService {
    Response saveBooking(Long roomId, Long userId, Booking bookingRequest);
    Response findBookingByConfirmationCode(String confirmationCode);
    Response getAllBooking();
    Response cancelBooking(Long bookingId);
}
