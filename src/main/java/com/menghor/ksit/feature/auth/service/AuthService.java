package com.menghor.ksit.feature.auth.service;

import com.menghor.ksit.feature.auth.dto.request.RegisterDto;
import com.menghor.ksit.feature.auth.dto.resposne.AuthResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.LoginDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDto;

/**
 * Service for authentication-related operations
 */
public interface AuthService {
    
    /**
     * Authenticate a user and generate JWT token
     * 
     * @param loginDto Login credentials
     * @return Authentication response with token and user details
     */
    AuthResponseDto login(LoginDto loginDto);
    
    /**
     * Register a new staff or admin user
     * 
     * @param registerDto Staff/admin registration data
     * @return Created user details
     */
    UserDto registerStaff(StaffRegisterDto registerDto);
    
    /**
     * Register a new student user
     * 
     * @param registerDto Student registration data
     * @return Created user details
     */
    UserDto registerStudent(StudentRegisterDto registerDto);
    
    /**
     * Register any type of user (generic method)
     * 
     * @param registerDto Registration data
     * @return Created user details
     */
    UserDto register(RegisterDto registerDto);
}