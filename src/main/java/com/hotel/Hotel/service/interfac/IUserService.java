package com.hotel.Hotel.service.interfac;

import com.hotel.Hotel.dto.LoginRequest;
import com.hotel.Hotel.dto.Response;
import com.hotel.Hotel.entity.User;

public interface IUserService {
    Response register(User user);
    Response login(LoginRequest loginRequest);
    Response getAllUsers();
    Response getUserBookingHistory(String userId);
    Response deleteUser(String userId);
    Response getUserById(String userId);
    Response getMyInfo(String userId);
}
