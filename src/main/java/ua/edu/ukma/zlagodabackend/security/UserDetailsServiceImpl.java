package ua.edu.ukma.zlagodabackend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ua.edu.ukma.zlagodabackend.dao.EmployeeDao;
import ua.edu.ukma.zlagodabackend.model.Employee;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final EmployeeDao employeeDao;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        Employee employee = employeeDao.findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("Працівника з ID " + id + " не знайдено"));

        return EmployeeDetails.build(employee);
    }
}