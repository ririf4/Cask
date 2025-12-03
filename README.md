# Cask

[![Maven Central](https://img.shields.io/maven-central/v/net.ririfa/Cask)](https://central.sonatype.com/artifact/net.ririfa/Cask)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A lightweight, high-performance caching library for Kotlin

## Features

- **Simple and Intuitive DSL** - Readable API leveraging Kotlin's language features
- **Flexible Eviction Strategies** - Built-in LRU, FIFO, and custom strategies
- **Automatic Expiration Management** - TTL-based automatic entry removal
- **Lightweight Design** - Only external dependency is SLF4J
- **Thread-Safe** - Safe to use in multi-threaded environments
- **Null Value Support** - Optional support for caching null values

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("net.ririfa:Cask:0.0.1+alpha.2")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'net.ririfa:Cask:0.0.1+alpha.2'
}
```

### Maven

```xml
<dependency>
    <groupId>net.ririfa</groupId>
    <artifactId>Cask</artifactId>
    <version>0.0.1+alpha.2</version>
</dependency>
```

## Quick Start

### Basic Usage

```kotlin
import net.ririfa.cask.cask

// Create a cache with minimal configuration
val cache = cask<String, String> {
    loader { key ->
        // Load data from database or API
        fetchFromDatabase(key)
    }
}

// Get data from cache (loader will be invoked if not present)
val value = cache.get("user:123")

// Manually add data
cache.put("user:456", userData)

// Invalidate an entry
cache.invalidate("user:123")

// Refresh data
cache.refresh("user:123")
```

### Advanced Configuration

```kotlin
val cache = cask<String, User> {
    // TTL configuration (default: 10 minutes)
    ttlMinutes = 30  // 30 minutes
    // or
    ttlSeconds = 1800
    ttlHours = 1

    // Maximum size (default: 100)
    maxSize = 1000

    // Data loader (required)
    loader { userId ->
        userRepository.findById(userId)
    }

    // Eviction callback (optional)
    onEvict { key, value ->
        logger.info("Evicted: $key")
    }

    // Allow null values (default: false)
    allowNullValues()

    // LRU strategy (default)
    lru = true
}
```

## Eviction Strategies

Cask provides 2 built-in eviction strategies:

### LRU (Least Recently Used) - Default

Removes entries that haven't been accessed for the longest time.

```kotlin
val cache = cask<String, String> {
    lru = true
    loader { key -> loadData(key) }
}
```

### FIFO (First In First Out)

Removes the oldest entries first.

```kotlin
val cache = cask<String, String> {
    fifo = true
    loader { key -> loadData(key) }
}
```

### Custom Eviction Strategy

Implement your own eviction logic:

```kotlin
import net.ririfa.cask.EvictionStrategy
import net.ririfa.cask.util.EvictionPolicy

val cache = cask<String, String> {
    evictionPolicy(EvictionPolicy.CUSTOM)
    evictionStrategy(object : EvictionStrategy<String, String> {
        override fun shouldEvict(
            cache: Map<String, String>,
            key: String,
            value: String
        ): Boolean {
            // Custom logic
            return value.length > 1000
        }
    })
    loader { key -> loadData(key) }
}
```

## Advanced Usage

### TTL Configuration with TimeUnit

```kotlin
import java.util.concurrent.TimeUnit

val cache = cask<String, String> {
    ttl(5, TimeUnit.HOURS)
    loader { key -> loadData(key) }
}
```

### Custom GC Executor

```kotlin
import java.util.concurrent.Executors

val customExecutor = Executors.newScheduledThreadPool(2)

val cache = cask<String, String> {
    withCustomGcExecutor(customExecutor)
    shareGcExecutor(false)
    loader { key -> loadData(key) }
}

// On application shutdown
customExecutor.shutdown()
```

### Shared GC Executor (Default)

Share a single GC thread across multiple cache instances:

```kotlin
val cache1 = cask<String, String> {
    shareGcExecutor(true)  // default
    loader { key -> loadData(key) }
}

val cache2 = cask<Int, User> {
    shareGcExecutor(true)
    loader { id -> loadUser(id) }
}

// On application shutdown
CaskRuntime.shutdown()
```

## Best Practices

1. **Keep Loader Functions Lightweight** - Avoid heavy processing in loaders; use additional cache layers if needed
2. **Set Appropriate TTL** - Adjust TTL based on data update frequency
3. **Configure maxSize Properly** - Balance between memory usage and cache hit rate
4. **Utilize Eviction Callbacks** - Use `onEvict` for debugging and metrics collection
5. **Share GC Executor** - Use shared GC executor when you have multiple cache instances

## Performance

- **Memory Efficient**: Minimized memory usage with lightweight internal data structures
- **Thread-Safe**: High throughput with minimal synchronization blocks
- **GC Optimized**: Background thread periodically cleans up expired entries (every 5 seconds)

## Examples

### User Data Caching in Web Applications

```kotlin
class UserService(private val userRepository: UserRepository) {
    private val userCache = cask<Long, User> {
        ttlMinutes = 15
        maxSize = 5000
        lru = true

        loader { userId ->
            userRepository.findById(userId)
        }

        onEvict { userId, user ->
            logger.debug("User cache evicted: $userId")
        }
    }

    fun getUser(userId: Long): User? = userCache.get(userId)

    fun updateUser(user: User) {
        userRepository.save(user)
        userCache.invalidate(user.id)
    }
}
```

### API Response Caching

```kotlin
class ApiClient {
    private val responseCache = cask<String, ApiResponse> {
        ttlMinutes = 5
        maxSize = 500

        loader { endpoint ->
            httpClient.get(endpoint).body()
        }
    }

    suspend fun fetchData(endpoint: String): ApiResponse? {
        return responseCache.get(endpoint)
    }
}
```

### Memoization of Computation Results

```kotlin
class Calculator {
    private val resultCache = cask<Pair<Int, Int>, BigDecimal> {
        ttlHours = 24
        maxSize = 10000
        fifo = true

        loader { (a, b) ->
            expensiveCalculation(a, b)
        }
    }

    fun calculate(a: Int, b: Int): BigDecimal? {
        return resultCache.get(a to b)
    }
}
```

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Contributing

Pull requests and issue reports are welcome!

## Links

- [GitHub Repository](https://github.com/ririf4/Cask)
- [Issue Tracker](https://github.com/ririf4/Cask/issues)
- [Maven Central](https://central.sonatype.com/artifact/net.ririfa/Cask)
