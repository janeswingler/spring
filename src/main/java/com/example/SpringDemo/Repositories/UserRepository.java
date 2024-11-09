package com.example.SpringDemo.Repositories;

import com.example.SpringDemo.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // You can define custom query methods if needed, like findByUsername or findByEmail
    User findByUsername(String username);

}
