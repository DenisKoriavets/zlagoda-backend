package ua.edu.ukma.zlagodabackend.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ua.edu.ukma.zlagodabackend.model.Employee;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
public class EmployeeDetails implements UserDetails {

    @Getter
    private final String idEmployee;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public static EmployeeDetails build(Employee employee) {
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + employee.getEmplRole().toUpperCase())
        );

        return new EmployeeDetails(
                employee.getIdEmployee(),
                employee.getPassword(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return idEmployee;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}