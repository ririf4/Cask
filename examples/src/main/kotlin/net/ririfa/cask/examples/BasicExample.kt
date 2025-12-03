package net.ririfa.cask.examples

import net.ririfa.cask.cask

/**
 * 基本的なキャッシュの使用例
 */
fun main() {
    println("=== Basic Cask Example ===\n")

    // シンプルなキャッシュを作成
    val cache = cask<String, String> {
        // データローダー - キャッシュミス時に実行される
        loader { key ->
            println("Loading data for key: $key")
            "Value for $key"
        }
    }

    // 最初のアクセス - loaderが実行される
    println("First access:")
    val value1 = cache.get("user:1")
    println("Result: $value1\n")

    // 2回目のアクセス - キャッシュから取得
    println("Second access (cached):")
    val value2 = cache.get("user:1")
    println("Result: $value2\n")

    // 手動でデータを追加
    println("Manual put:")
    cache.put("user:2", "Manually added value")
    val value3 = cache.get("user:2")
    println("Result: $value3\n")

    // キャッシュサイズ
    println("Cache size: ${cache.size()}\n")

    // エントリの無効化
    println("Invalidating user:1:")
    cache.invalidate("user:1")
    println("Cache size after invalidation: ${cache.size()}\n")

    // 無効化後のアクセス - 再度loaderが実行される
    println("Access after invalidation:")
    val value4 = cache.get("user:1")
    println("Result: $value4\n")
}
