package net.ririfa.cask

import net.ririfa.cask.util.CaskLoader
import java.time.Duration

fun <K, V> cask(init: CaskBuilder<K, V>.() -> Unit): Cask<K, V> {
    return CaskBuilder.newBuilder<K, V>().apply(init).build()
}

var <K, V> CaskBuilder<K, V>.ttl: Long
    get() = throw UnsupportedOperationException("Write-only")
    set(value) { ttl(Duration.ofSeconds(value)) }

var <K, V> CaskBuilder<K, V>.maxSize: Int
    get() = throw UnsupportedOperationException("Write-only")
    set(value) { maxSize(value) }

fun <K, V> CaskBuilder<K, V>.loader(block: CaskLoader<K, V>) = this.loader(block)

fun <K, V> CaskBuilder<K, V>.onEvict(block: (K, V?) -> Unit) =
    this.onEvict(CaskBiConsumer { k, v -> block(k, v) })

fun <K, V> CaskBuilder<K, V>.allowNullValues() = this.allowNullValues(true)
