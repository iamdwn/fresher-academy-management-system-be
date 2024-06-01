package java03.team01.FAMS.security;

import java03.team01.FAMS.model.entity.Role;
import java03.team01.FAMS.model.entity.User;
import java03.team01.FAMS.repository.UserRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CustomUserDetailsServiceTest {

    @Test
    public void testLoadUserByUsername() {
        // Given
        String usernameOrEmail = "testUser";
        String password = "testPassword";
        String roleName = "ROLE_USER";

        // Mocking the UserRepository
        UserRepository userRepository = mock(UserRepository.class);
        Role role = new Role();
        role.setRoleName(roleName);
        User user = new User();
        user.setUsername(usernameOrEmail);
        user.setPassword(password);
        user.setRole(role);
        Optional<User> optionalUser = Optional.of(user);
        when(userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)).thenReturn(optionalUser);

        // Creating an instance of CustomUserDetailsService
        CustomUserDetailsService userDetailsService = new CustomUserDetailsService(userRepository);

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(usernameOrEmail);

        // Then
        assertEquals(usernameOrEmail, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        Set<GrantedAuthority> authorities = (Set<GrantedAuthority>) userDetails.getAuthorities();
        assertEquals(1, authorities.size());
        GrantedAuthority authority = authorities.iterator().next();
        assertEquals(roleName, authority.getAuthority());
    }

    @Test
    public void testLoadUserByUsername_UserNotFound() {
        // Given
        String usernameOrEmail = "nonExistingUser";

        // Mocking the UserRepository to return empty Optional
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)).thenReturn(Optional.empty());

        // Creating an instance of CustomUserDetailsService
        CustomUserDetailsService userDetailsService = new CustomUserDetailsService(userRepository);

        // When and Then
        UsernameNotFoundException exception = org.junit.jupiter.api.Assertions.assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(usernameOrEmail)
        );
        assertEquals("User not found with username or email: " + usernameOrEmail, exception.getMessage());
    }
}

