package com.misacordes.application.repositories;

import com.misacordes.application.entities.User;
// import lombok.RequiredArgsConstructor; // No utilizado
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.repository.CrudRepository; // No utilizado
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);
}
