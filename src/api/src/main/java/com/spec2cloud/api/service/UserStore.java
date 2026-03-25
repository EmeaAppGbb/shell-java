package com.spec2cloud.api.service;

import com.spec2cloud.api.model.User;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory user store backed by ConcurrentHashMap.
 * <p>
 * To migrate to Cosmos DB:
 *   1. Replace ConcurrentHashMap with CosmosContainer operations
 *   2. Change save() to use container.upsertItem()
 *   3. Change findByUsername() to a SQL query: SELECT * FROM c WHERE c.username = @username
 *   4. Change findById() to container.readItem()
 *   5. Change findAll() to a SQL query: SELECT * FROM c
 * </p>
 */
@Service
public class UserStore {

    // Keyed by user ID
    private final ConcurrentHashMap<String, User> usersById = new ConcurrentHashMap<>();
    // Secondary index: username → user ID (for fast lookups)
    private final ConcurrentHashMap<String, String> usernameIndex = new ConcurrentHashMap<>();

    public Optional<User> findByUsername(String username) {
        String id = usernameIndex.get(username);
        return id != null ? Optional.ofNullable(usersById.get(id)) : Optional.empty();
    }

    public Optional<User> findById(String id) {
        return Optional.ofNullable(usersById.get(id));
    }

    public Collection<User> findAll() {
        return usersById.values();
    }

    public User save(User user) {
        usersById.put(user.id(), user);
        usernameIndex.put(user.username(), user.id());
        return user;
    }

    public int count() {
        return usersById.size();
    }

    /** Visible for testing — clears all data. */
    public void clear() {
        usersById.clear();
        usernameIndex.clear();
    }
}
