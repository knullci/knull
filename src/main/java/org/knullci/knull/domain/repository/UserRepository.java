package org.knullci.knull.domain.repository;

import org.knullci.knull.domain.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User domain operations.
 */
public interface UserRepository {

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by ID
     */
    Optional<User> findById(Long id);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Get all users
     */
    List<User> findAll();

    /**
     * Save (create or update) a user
     * 
     * @return the saved user with ID
     */
    User save(User user);

    /**
     * Delete a user by ID
     */
    void deleteById(Long id);

    /**
     * Count total users
     */
    long count();

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Update last login time
     */
    void updateLastLoginTime(Long userId);
}
