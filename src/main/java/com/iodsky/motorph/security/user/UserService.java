package com.iodsky.motorph.security.user;

import com.iodsky.motorph.common.exception.BadRequestException;
import com.iodsky.motorph.common.exception.NotFoundException;
import com.iodsky.motorph.employee.EmployeeService;
import com.iodsky.motorph.employee.model.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserMapper userMapper;
    private final EmployeeService employeeService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("User " + username + " not found"));
    }

    public List<User> getAllUsers(String role) {
        if (role == null) {
            return userRepository.findAll();
        }

        if (!userRoleRepository.existsByRole(role)) {
            throw new BadRequestException("Invalid " + role);
        }

        return userRepository.findUserByUserRole_Role(role);
    }

    public User createUser(UserRequest userRequest) {

        User user = userMapper.toEntity(userRequest);

        Employee employee = employeeService.getEmployeeById(userRequest.getEmployeeId());
        user.setEmployee(employee);

        UserRole role = userRoleRepository.findById(userRequest.getRole())
                .orElseThrow(() -> new BadRequestException("Invalid role " + userRequest.getRole()));
        user.setUserRole(role);

        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        return userRepository.save(user);
    }

}
