package com.odop.root.services;

import com.odop.root.models.Admin;
import com.odop.root.models.Customer;
import com.odop.root.models.Vendor;
import com.odop.root.repository.AdminRepository;
import com.odop.root.repository.CustomerRepository;
import com.odop.root.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("DEBUG [UserDetailsService]: loadUserByUsername called for: " + username);
        Admin admin = adminRepository.findByEmailAddress(username);
        if (admin != null) {
            System.out.println("DEBUG [UserDetailsService]: Admin found: " + admin.getAdminId());
            return new User(admin.getEmailAddress(), admin.getPassword(), mapRolesToAuthorities(admin.getRoles()));
        }

        Customer customer = customerRepository.findByEmailAddress(username);
        if (customer != null) {
            System.out.println("DEBUG [UserDetailsService]: Customer found: " + customer.getCustomerId());
            return new User(customer.getEmailAddress(), customer.getPassword(), mapRolesToAuthorities(customer.getRoles()));
        }

        Vendor vendor = vendorRepository.findByEmailAddress(username);
        if (vendor != null) {
            System.out.println("DEBUG [UserDetailsService]: Vendor found: " + vendor.getVendorId());
            return new User(vendor.getEmailAddress(), vendor.getPassword(), mapRolesToAuthorities(vendor.getRoles()));
        }

        System.out.println("DEBUG [UserDetailsService]: No user found for username: " + username);
        throw new UsernameNotFoundException("User not found with username: " + username);
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<String> roles) {
        if (roles == null) {
            System.out.println("DEBUG [UserDetailsService]: roles is null, returning empty authorities");
            return java.util.Collections.emptyList();
        }
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());
    }
}
