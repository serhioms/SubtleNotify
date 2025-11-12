package ru.alumni.hub.subtlenotify.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.alumni.hub.subtlenotify.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Find user by userId
     * @param userId the user ID
     * @return Optional containing the user if found
     */
    Optional<User> findByUserId(String userId);

    /**
     * Check if user exists by userId
     * @param userId the user ID
     * @return true if user exists, false otherwise
     */
    boolean existsByUserId(String userId);

    /**
     * Delete user by userId
     * @param userId the user ID
     */
    void deleteByUserId(String userId);

}
