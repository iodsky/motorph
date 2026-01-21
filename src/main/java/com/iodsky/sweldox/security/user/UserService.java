package com.iodsky.sweldox.security.user;

import com.iodsky.sweldox.employee.EmployeeService;
import com.iodsky.sweldox.employee.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User " + username + " not found"));
    }

    public User createUser(UserRequest userRequest) {

        User user = userMapper.toEntity(userRequest);

        Employee employee = employeeService.getEmployeeById(userRequest.getEmployeeId());
        user.setEmployee(employee);

        UserRole role = getUserRole(userRequest.getRole());
        user.setUserRole(role);

        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        return userRepository.save(user);
    }

    public Page<User> getAllUsers(int size, int limit, String role) {
        Pageable pageable = PageRequest.of(size, limit);
        if (role == null) {
            return userRepository.findAll(pageable);
        }

        if (!userRoleRepository.existsByRole(role)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + role);
        }

        return userRepository.findUserByUserRole_Role(role, pageable);
    }

    public User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof User user) {
            return user;
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user found");
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User " + email + " not found"));
    }

    private UserRole getUserRole(String role) {
        return userRoleRepository.findById(role)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role " + role));
    }

}
