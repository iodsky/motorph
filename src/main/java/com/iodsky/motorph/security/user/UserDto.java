package com.iodsky.motorph.security.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserDto {

   private UUID id;
   private String email;
   private Long employeeId;
   private String role;
   private LocalDateTime createdAt;
   private LocalDateTime updatedAt;

}
