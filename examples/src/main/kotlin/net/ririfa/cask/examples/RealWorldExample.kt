package net.ririfa.cask.examples

import net.ririfa.cask.cask
import java.util.concurrent.atomic.AtomicInteger

/**
 * 実際のアプリケーションでの使用例
 * データベースアクセスをシミュレートしたキャッシュ層の実装
 */
fun main() {
    println("=== Real-World Example: Database Cache ===\n")

    // データベースへのアクセス回数をカウント
    val dbAccessCount = AtomicInteger(0)

    // ユーザーサービス
    val userService = UserService(dbAccessCount)

    println("Scenario 1: Multiple reads of same user\n")

    // 同じユーザーを複数回読み込む
    repeat(3) { i ->
        println("Request ${i + 1}:")
        val user = userService.getUser("user:100")
        println("  Result: $user")
    }
    println("\n✅ Database accessed only once! Count: ${dbAccessCount.get()}\n")

    println("Scenario 2: Multiple users\n")

    // 複数のユーザーを読み込む
    val userIds = listOf("user:101", "user:102", "user:103")
    userIds.forEach { userId ->
        println("Fetching $userId:")
        val user = userService.getUser(userId)
        println("  Result: $user")
    }
    println("\nTotal DB accesses: ${dbAccessCount.get()}\n")

    println("Scenario 3: User update and cache invalidation\n")

    // ユーザー情報を更新
    println("Updating user:100...")
    userService.updateUser(User("user:100", "Updated Name", "updated@example.com"))
    println("  Cache invalidated for user:100")

    // 更新後のユーザー情報を取得
    println("\nFetching user:100 after update:")
    val updatedUser = userService.getUser("user:100")
    println("  Result: $updatedUser")

    println("\nTotal DB accesses: ${dbAccessCount.get()}")
    println("✅ Cache efficiently reduced DB load!")
}

/**
 * ユーザーエンティティ
 */
data class User(
    val id: String,
    val name: String,
    val email: String
)

/**
 * データベースアクセスをシミュレートするリポジトリ
 */
class UserRepository(private val accessCounter: AtomicInteger) {
    private val database = mutableMapOf<String, User>()

    init {
        // 初期データ
        database["user:100"] = User("user:100", "Alice", "alice@example.com")
        database["user:101"] = User("user:101", "Bob", "bob@example.com")
        database["user:102"] = User("user:102", "Charlie", "charlie@example.com")
        database["user:103"] = User("user:103", "Diana", "diana@example.com")
    }

    fun findById(userId: String): User? {
        // データベースアクセスをシミュレート
        accessCounter.incrementAndGet()
        Thread.sleep(100)  // DB遅延をシミュレート
        println("    [DB] Loading from database: $userId")
        return database[userId]
    }

    fun save(user: User) {
        accessCounter.incrementAndGet()
        Thread.sleep(50)  // DB遅延をシミュレート
        println("    [DB] Saving to database: ${user.id}")
        database[user.id] = user
    }
}

/**
 * キャッシュ層を持つユーザーサービス
 */
class UserService(accessCounter: AtomicInteger) {
    private val repository = UserRepository(accessCounter)

    // ユーザーデータキャッシュ
    private val userCache = cask<String, User> {
        // 15分間キャッシュ
        ttlMinutes = 15

        // 最大1000ユーザー
        maxSize = 1000

        // LRU戦略
        lru = true

        // キャッシュミス時にDBからロード
        loader { userId ->
            repository.findById(userId)
        }

        // 退去時のログ出力
        onEvict { userId, user ->
            println("  [Cache] Evicted: $userId")
        }
    }

    fun getUser(userId: String): User? {
        return userCache.get(userId)
    }

    fun updateUser(user: User) {
        // DBに保存
        repository.save(user)

        // キャッシュを無効化して最新データを保証
        userCache.invalidate(user.id)
    }

    fun refreshUser(userId: String) {
        // 強制的に最新データをロード
        userCache.refresh(userId)
    }

    fun getCacheSize(): Int = userCache.size()
}
