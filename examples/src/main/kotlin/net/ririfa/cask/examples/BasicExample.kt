package net.ririfa.cask.examples

import net.ririfa.cask.cask

/**
 * Basic cache usage example
 */
fun main() {
    println("=== Basic Cask Example ===\n")

    // Create a simple cache
    val cache = cask<String, String> {
        // Data loader - invoked on cache miss
        loader { key ->
            println("Loading data for key: $key")
            "Value for $key"
        }
    }

    // First access - loader will be invoked
    println("First access:")
    val value1 = cache.get("user:1")
    println("Result: $value1\n")

    // Second access - retrieved from cache
    println("Second access (cached):")
    val value2 = cache.get("user:1")
    println("Result: $value2\n")

    // Manually add data
    println("Manual put:")
    cache.put("user:2", "Manually added value")
    val value3 = cache.get("user:2")
    println("Result: $value3\n")

    // Cache size
    println("Cache size: ${cache.size()}\n")

    // Invalidate an entry
    println("Invalidating user:1:")
    cache.invalidate("user:1")
    println("Cache size after invalidation: ${cache.size()}\n")

    // Access after invalidation - loader will be invoked again
    println("Access after invalidation:")
    val value4 = cache.get("user:1")
    println("Result: $value4\n")
}
