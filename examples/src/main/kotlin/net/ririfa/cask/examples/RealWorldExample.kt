package net.ririfa.cask.examples

import net.ririfa.cask.cask
import java.util.concurrent.atomic.AtomicInteger

/**
 * Real-world application example
 * Implementation of a cache layer simulating database access
 */
fun main() {
    println("=== Real-World Example: Database Cache ===\n")

    // Count database access
    val dbAccessCount = AtomicInteger(0)

    // User service
    val userService = UserService(dbAccessCount)

    println("Scenario 1: Multiple reads of same user\n")

    // Read the same user multiple times
    repeat(3) { i ->
        println("Request ${i + 1}:")
        val user = userService.getUser("user:100")
        println("  Result: $user")
    }
    println("\n✅ Database accessed only once! Count: ${dbAccessCount.get()}\n")

    println("Scenario 2: Multiple users\n")

    // Read multiple users
    val userIds = listOf("user:101", "user:102", "user:103")
    userIds.forEach { userId ->
        println("Fetching $userId:")
        val user = userService.getUser(userId)
        println("  Result: $user")
    }
    println("\nTotal DB accesses: ${dbAccessCount.get()}\n")

    println("Scenario 3: User update and cache invalidation\n")

    // Update user information
    println("Updating user:100...")
    userService.updateUser(User("user:100", "Updated Name", "updated@example.com"))
    println("  Cache invalidated for user:100")

    // Fetch updated user information
    println("\nFetching user:100 after update:")
    val updatedUser = userService.getUser("user:100")
    println("  Result: $updatedUser")

    println("\nTotal DB accesses: ${dbAccessCount.get()}")
    println("✅ Cache efficiently reduced DB load!")
}

/**
 * User entity
 */
data class User(
    val id: String,
    val name: String,
    val email: String
)

/**
 * Repository simulating database access
 */
class UserRepository(private val accessCounter: AtomicInteger) {
    private val database = mutableMapOf<String, User>()

    init {
        // Initial data
        database["user:100"] = User("user:100", "Alice", "alice@example.com")
        database["user:101"] = User("user:101", "Bob", "bob@example.com")
        database["user:102"] = User("user:102", "Charlie", "charlie@example.com")
        database["user:103"] = User("user:103", "Diana", "diana@example.com")
    }

    fun findById(userId: String): User? {
        // Simulate database access
        accessCounter.incrementAndGet()
        Thread.sleep(100)  // Simulate DB latency
        println("    [DB] Loading from database: $userId")
        return database[userId]
    }

    fun save(user: User) {
        accessCounter.incrementAndGet()
        Thread.sleep(50)  // Simulate DB latency
        println("    [DB] Saving to database: ${user.id}")
        database[user.id] = user
    }
}

/**
 * User service with cache layer
 */
class UserService(accessCounter: AtomicInteger) {
    private val repository = UserRepository(accessCounter)

    // User data cache
    private val userCache = cask<String, User> {
        // Cache for 15 minutes
        ttlMinutes = 15

        // Maximum 1000 users
        maxSize = 1000

        // LRU strategy
        lru = true

        // Load from DB on cache miss
        loader { userId ->
            repository.findById(userId)
        }

        // Log on eviction
        onEvict { userId, user ->
            println("  [Cache] Evicted: $userId")
        }
    }

    fun getUser(userId: String): User? {
        return userCache.get(userId)
    }

    fun updateUser(user: User) {
        // Save to DB
        repository.save(user)

        // Invalidate cache to ensure fresh data
        userCache.invalidate(user.id)
    }

    fun refreshUser(userId: String) {
        // Force load fresh data
        userCache.refresh(userId)
    }

    fun getCacheSize(): Int = userCache.size()
}
