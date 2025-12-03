package net.ririfa.cask.examples

import net.ririfa.cask.cask
import java.util.concurrent.TimeUnit

/**
 * Advanced cache configuration example
 */
fun main() {
    println("=== Advanced Cask Example ===\n")

    var evictionCount = 0

    val cache = cask<String, UserData> {
        // TTL configuration - 5 seconds
        ttlSeconds = 5

        // Maximum size - 3 entries
        maxSize = 3

        // Use LRU strategy
        lru = true

        // Data loader
        loader { userId ->
            println("Loading user data for: $userId")
            UserData(userId, "User $userId", System.currentTimeMillis())
        }

        // Eviction callback
        onEvict { key, value ->
            evictionCount++
            println("⚠️  Evicted: $key -> $value")
        }
    }

    println("Adding 4 users to cache (max size is 3):\n")

    // Add 4 entries (max size is 3, so 1 will be evicted)
    cache.get("user:1")
    Thread.sleep(100)

    cache.get("user:2")
    Thread.sleep(100)

    cache.get("user:3")
    Thread.sleep(100)

    println("\nCache size before 4th entry: ${cache.size()}")

    cache.get("user:4")  // user:1 will be evicted (LRU)
    println("Cache size after 4th entry: ${cache.size()}")
    println("Evictions so far: $evictionCount\n")

    // user:1 was evicted, so loader will be invoked again
    println("Accessing user:1 again (should reload):")
    cache.get("user:1")
    println()

    // TTL test
    println("Waiting for TTL expiration (5 seconds)...")
    Thread.sleep(5100)

    println("Accessing user:2 after TTL (should reload):")
    cache.get("user:2")
    println()

    println("Total evictions: $evictionCount")
    println("Final cache size: ${cache.size()}")
}

data class UserData(
    val id: String,
    val name: String,
    val timestamp: Long
) {
    override fun toString(): String {
        return "UserData(id='$id', name='$name')"
    }
}
