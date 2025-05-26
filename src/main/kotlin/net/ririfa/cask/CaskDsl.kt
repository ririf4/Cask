package net.ririfa.cask

import net.ririfa.cask.util.CaskLoader
import net.ririfa.cask.util.EvictionPolicy
import java.time.Duration

fun <K, V> cask(init: CaskBuilder<K, V>.() -> Unit): Cask<K, V> {
    return CaskBuilder.newBuilder<K, V>().apply(init).build()
}

var <K, V> CaskBuilder<K, V>.ttl: Duration
    get() = throw UnsupportedOperationException("Write-only")
    set(value) { ttl(value) }

var <K, V> CaskBuilder<K, V>.maxSize: Int
    get() = throw UnsupportedOperationException("Write-only")
    set(value) { maxSize(value) }

fun <K, V> CaskBuilder<K, V>.loader(block: CaskLoader<K, V>) = loader(block)

fun <K, V> CaskBuilder<K, V>.onEvict(block: (K, V?) -> Unit) =
    onEvict(CaskBiConsumer { k, v -> block(k, v) })

fun <K, V> CaskBuilder<K, V>.allowNullValues() = allowNullValues(true)