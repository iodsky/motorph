package com.iodsky.motorph.security.user;

import com.iodsky.motorph.csvimport.CsvMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class UserMapper implements CsvMapper<User, UserCsvRecord> {

    public UserDto toDto(User user) {
        return UserDto
                .builder()
                .id(user.getId())
                .employeeId(user.getEmployee().getId())
                .email(user.getEmail())
                .role(user.getUserRole().getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public User toEntity(UserRequest userRequest) {
        return User.builder()
                .email(userRequest.getEmail())
                .password(userRequest.getPassword())
                .build();
    }

    @Override
    public User toEntity(UserCsvRecord record) {
        return User.builder()
                .email(record.getEmail())
                .password(record.getPassword())
                .build();
    }

}
