package net.ririfa.cask.impl

import net.ririfa.cask.Cask
import net.ririfa.cask.CaskBiConsumer
import net.ririfa.cask.EvictionStrategy
import net.ririfa.cask.util.CaskLoader
import net.ririfa.cask.util.EvictionPolicy
import java.time.Duration
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class CaskImpl<K, V>(
    private val ttl: Duration,
    private val maxSize: Int,
    private val loader: CaskLoader<K, V>,
    private val onEvict: CaskBiConsumer<K, V>?,
    private val allowNulls: Boolean,
    private val evictionPolicy: EvictionPolicy,
    private val customEviction: EvictionStrategy<K, CacheEntry<V>>? = null,
    gcExecutor: ScheduledExecutorService
) : Cask<K, V> {

    private val cache = object : LinkedHashMap<K, CacheEntry<V>>(maxSize, 0.75f, evictionPolicy.accessOrder) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, CacheEntry<V>>): Boolean {
            val custom = customEviction?.shouldEvict(this, eldest.key, eldest.value) == true
            val default = when (evictionPolicy) {
                EvictionPolicy.NONE -> false
                else -> size > maxSize
            }
            val shouldEvict = custom || default
            if (shouldEvict) {
                onEvict?.accept(eldest.key, eldest.value.value)
            }
            return shouldEvict
        }

    }

    override fun get(key: K): V? {
        synchronized(cache) {
            val now = System.currentTimeMillis()
            val entry = cache[key]
            if (entry != null && now - entry.createdAt < ttl.toMillis()) {
                entry.lastAccessedAt = now
                return entry.value
            }

            val loaded = loader.invoke(key) ?: return null
            put(key, loaded)
            entry?.lastAccessedAt = now
            return loaded
        }
    }

    override fun put(key: K, value: V?) {
        if (value == null && !allowNulls) return
        synchronized(cache) {
            cache[key] = CacheEntry(value, System.currentTimeMillis())
        }
    }

    override fun invalidate(key: K) {
        synchronized(cache) {
            cache.remove(key)
        }
    }

    override fun clear() {
        synchronized(cache) {
            cache.clear()
        }
    }

    override fun refresh(key: K) {
        val loaded = loader.invoke(key) ?: return
        put(key, loaded)
    }

    override fun size(): Int = cache.size

    data class CacheEntry<V>(
        val value: V?,
        var createdAt: Long = System.currentTimeMillis(),
        var lastAccessedAt: Long = createdAt
    )

    init {
        gcExecutor.scheduleAtFixedRate({
            val now = System.currentTimeMillis()
            val expiredKeys = mutableListOf<K>()

            synchronized(cache) {
                for ((key, entry) in cache) {
                    if (now - entry.createdAt >= ttl.toMillis()) {
                        expiredKeys.add(key)
                    }
                }

                for (key in expiredKeys) {
                    val removed = cache.remove(key)
                    if (removed != null) {
                        onEvict?.accept(key, removed.value)
                    }
                }
            }
        }, 5_000, 5_000, TimeUnit.MILLISECONDS)
    }
}
