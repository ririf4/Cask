# Cask

[![Maven Central](https://img.shields.io/maven-central/v/net.ririfa/Cask)](https://central.sonatype.com/artifact/net.ririfa/Cask)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

軽量で高性能なKotlinキャッシュライブラリ

## 特徴

- **シンプルで直感的なDSL** - Kotlinの言語機能を活かした読みやすいAPI
- **柔軟な退去戦略** - LRU、LFU、FIFO、カスタム戦略に対応
- **自動期限切れ管理** - TTLベースの自動エントリ削除
- **軽量設計** - 外部依存はSLF4Jのみ
- **スレッドセーフ** - マルチスレッド環境でも安全に使用可能
- **null値対応** - オプションでnull値のキャッシュが可能

## インストール

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

## クイックスタート

### 基本的な使い方

```kotlin
import net.ririfa.cask.cask

// 最小限の設定でキャッシュを作成
val cache = cask<String, String> {
    loader { key ->
        // データベースやAPIからデータをロード
        fetchFromDatabase(key)
    }
}

// キャッシュからデータを取得（存在しない場合はloaderが実行される）
val value = cache.get("user:123")

// 手動でデータを追加
cache.put("user:456", userData)

// エントリを無効化
cache.invalidate("user:123")

// データを再ロード
cache.refresh("user:123")
```

### 詳細設定

```kotlin
val cache = cask<String, User> {
    // TTL設定（デフォルト: 10分）
    ttlMinutes = 30  // 30分
    // または
    ttlSeconds = 1800
    ttlHours = 1

    // 最大サイズ（デフォルト: 100）
    maxSize = 1000

    // データローダー（必須）
    loader { userId ->
        userRepository.findById(userId)
    }

    // 退去時のコールバック（オプション）
    onEvict { key, value ->
        logger.info("Evicted: $key")
    }

    // null値を許可（デフォルト: false）
    allowNullValues()

    // LRU戦略（デフォルト）
    lru = true
}
```

## 退去戦略

Caskは2つの組み込み退去戦略を提供します：

### LRU (Least Recently Used) - デフォルト

最も長い間アクセスされていないエントリを削除します。

```kotlin
val cache = cask<String, String> {
    lru = true
    loader { key -> loadData(key) }
}
```

### FIFO (First In First Out)

最も古く追加されたエントリを削除します。

```kotlin
val cache = cask<String, String> {
    fifo = true
    loader { key -> loadData(key) }
}
```

### カスタム退去戦略

独自の退去ロジックを実装できます：

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
            // カスタムロジック
            return value.length > 1000
        }
    })
    loader { key -> loadData(key) }
}
```

## 高度な使い方

### TimeUnit を使用したTTL設定

```kotlin
import java.util.concurrent.TimeUnit

val cache = cask<String, String> {
    ttl(5, TimeUnit.HOURS)
    loader { key -> loadData(key) }
}
```

### カスタムGCエグゼキューター

```kotlin
import java.util.concurrent.Executors

val customExecutor = Executors.newScheduledThreadPool(2)

val cache = cask<String, String> {
    withCustomGcExecutor(customExecutor)
    shareGcExecutor(false)
    loader { key -> loadData(key) }
}

// アプリケーション終了時
customExecutor.shutdown()
```

### 共有GCエグゼキューター（デフォルト）

複数のキャッシュインスタンスで単一のGCスレッドを共有します：

```kotlin
val cache1 = cask<String, String> {
    shareGcExecutor(true)  // デフォルト
    loader { key -> loadData(key) }
}

val cache2 = cask<Int, User> {
    shareGcExecutor(true)
    loader { id -> loadUser(id) }
}

// アプリケーション終了時
CaskRuntime.shutdown()
```

## ベストプラクティス

1. **ローダー関数を軽量に保つ** - ローダー内で重い処理を避け、必要に応じて別途キャッシュ層を設ける
2. **適切なTTLを設定** - データの更新頻度に応じてTTLを調整
3. **maxSizeの適切な設定** - メモリ使用量とキャッシュヒット率のバランスを考慮
4. **退去コールバックの活用** - デバッグやメトリクス収集に`onEvict`を使用
5. **GCエグゼキューターの共有** - 複数のキャッシュインスタンスがある場合は共有GCエグゼキューターを使用

## パフォーマンス

- **メモリ効率**: 軽量な内部データ構造でメモリ使用量を最小化
- **スレッドセーフ**: 同期化ブロックを最小限に抑え、高いスループットを実現
- **GC最適化**: バックグラウンドスレッドで期限切れエントリを定期的にクリーンアップ（5秒間隔）

## 使用例

### Webアプリケーションでのユーザーデータキャッシュ

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

### APIレスポンスキャッシュ

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

### 計算結果のメモ化

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

## ライセンス

このプロジェクトはMITライセンスの下でライセンスされています。詳細は[LICENSE](LICENSE)ファイルをご覧ください。

## 貢献

プルリクエストやイシューの報告を歓迎します！

## リンク

- [GitHub Repository](https://github.com/ririf4/Cask)
- [Issue Tracker](https://github.com/ririf4/Cask/issues)
- [Maven Central](https://central.sonatype.com/artifact/net.ririfa/Cask)
