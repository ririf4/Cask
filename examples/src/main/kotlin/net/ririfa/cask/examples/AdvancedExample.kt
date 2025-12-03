package net.ririfa.cask.examples

import net.ririfa.cask.cask
import java.util.concurrent.TimeUnit

/**
 * 高度な設定を使用したキャッシュの例
 */
fun main() {
    println("=== Advanced Cask Example ===\n")

    var evictionCount = 0

    val cache = cask<String, UserData> {
        // TTL設定 - 5秒
        ttlSeconds = 5

        // 最大サイズ - 3エントリ
        maxSize = 3

        // LRU戦略を使用
        lru = true

        // データローダー
        loader { userId ->
            println("Loading user data for: $userId")
            UserData(userId, "User $userId", System.currentTimeMillis())
        }

        // 退去コールバック
        onEvict { key, value ->
            evictionCount++
            println("⚠️  Evicted: $key -> $value")
        }
    }

    println("Adding 4 users to cache (max size is 3):\n")

    // 4つのエントリを追加（最大サイズは3なので、1つ退去される）
    cache.get("user:1")
    Thread.sleep(100)

    cache.get("user:2")
    Thread.sleep(100)

    cache.get("user:3")
    Thread.sleep(100)

    println("\nCache size before 4th entry: ${cache.size()}")

    cache.get("user:4")  // user:1が退去される（LRU）
    println("Cache size after 4th entry: ${cache.size()}")
    println("Evictions so far: $evictionCount\n")

    // user:1は退去されたので再度loaderが実行される
    println("Accessing user:1 again (should reload):")
    cache.get("user:1")
    println()

    // TTLテスト
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
