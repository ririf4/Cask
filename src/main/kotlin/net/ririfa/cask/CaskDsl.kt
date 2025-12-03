package net.ririfa.cask

import net.ririfa.cask.util.CaskLoader
import net.ririfa.cask.util.EvictionPolicy
import java.time.Duration
import java.util.concurrent.TimeUnit

fun <K, V> cask(init: CaskBuilder<K, V>.() -> Unit): Cask<K, V> {
    return CaskBuilder.newBuilder<K, V>().apply(init).build()
}

var <K, V> CaskBuilder<K, V>.ttlSeconds: Long
    get() = throw UnsupportedOperationException("Write-only")
    set(value) { ttl(Duration.ofSeconds(value)) }

var <K, V> CaskBuilder<K, V>.ttlMinutes: Long
    get() = throw UnsupportedOperationException("Write-only")
    set(value) { ttl(Duration.ofMinutes(value)) }

var <K, V> CaskBuilder<K, V>.ttlHours: Long
    get() = throw UnsupportedOperationException("Write-only")
    set(value) { ttl(Duration.ofHours(value)) }

fun <K, V> CaskBuilder<K, V>.ttl(value: Long, unit: TimeUnit): CaskBuilder<K, V> {
    return ttl(Duration.ofMillis(unit.toMillis(value)))
}

var <K, V> CaskBuilder<K, V>.maxSize: Int
    get() = throw UnsupportedOperationException("Write-only")
    set(value) { maxSize(value) }

fun <K, V> CaskBuilder<K, V>.loader(block: CaskLoader<K, V>) = loader(block)

fun <K, V> CaskBuilder<K, V>.onEvict(block: (K, V?) -> Unit) =
    onEvict(CaskBiConsumer { k, v -> block(k, v) })

fun <K, V> CaskBuilder<K, V>.allowNullValues() = allowNullValues(true)

var <K, V> CaskBuilder<K, V>.lru: Boolean
    get() = throw UnsupportedOperationException("Write-only")
    set(value) { if (value) evictionPolicy(EvictionPolicy.LRU) }

var <K, V> CaskBuilder<K, V>.fifo: Boolean
    get() = throw UnsupportedOperationException("Write-only")
    set(value) { if (value) evictionPolicy(EvictionPolicy.FIFO) }