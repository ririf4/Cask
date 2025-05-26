package net.ririfa.cask.impl

import net.ririfa.cask.Cask
import net.ririfa.cask.CaskBiConsumer
import net.ririfa.cask.CaskBuilder
import net.ririfa.cask.CaskRuntime
import net.ririfa.cask.EvictionStrategy
import net.ririfa.cask.impl.CaskImpl.CacheEntry
import net.ririfa.cask.util.CaskLoader
import net.ririfa.cask.util.EvictionPolicy
import java.time.Duration
import java.util.concurrent.ScheduledExecutorService

class CaskBuilderImpl<K, V> : CaskBuilder<K, V> {
    private lateinit var iTtl: Duration
    private var iMaxSize: Int? = null
    private lateinit var iLoader: CaskLoader<K, V>
    private var iEvictor: CaskBiConsumer<K, V>? = null
    private var allowNulls: Boolean = false
    private var evictionPolicy: EvictionPolicy = EvictionPolicy.LRU
    private var customEviction: EvictionStrategy<K, CacheEntry<V>>? = null
    private var sharedGcExecutor: Boolean = false
    private var customGcExecutor: ScheduledExecutorService? = null

    override fun ttl(ttl: Duration): CaskBuilder<K, V> {
        this.iTtl = ttl
        return this
    }

    override fun maxSize(size: Int): CaskBuilder<K, V> {
        this.iMaxSize = size
        return this
    }

    override fun loader(loader: (K) -> V): CaskBuilder<K, V> {
        this.iLoader = loader
        return this
    }

    override fun onEvict(evictor: CaskBiConsumer<K, V>): CaskBuilder<K, V> {
        this.iEvictor = evictor
        return this
    }

    override fun allowNullValues(allow: Boolean): CaskBuilder<K, V> {
        this.allowNulls = allow
        return this
    }

    override fun evictionPolicy(policy: EvictionPolicy): CaskBuilder<K, V> {
        this.evictionPolicy = policy
        return this
    }

    override fun evictionStrategy(strategy: EvictionStrategy<K, V>): CaskBuilder<K, V> {
        @Suppress("UNCHECKED_CAST")
        this.customEviction = strategy as EvictionStrategy<K, CacheEntry<V>>
        return this
    }

    override fun shareGcExecutor(shared: Boolean): CaskBuilder<K, V> {
        this.sharedGcExecutor = shared
        return this
    }

    override fun withCustomGcExecutor(executor: ScheduledExecutorService): CaskBuilder<K, V> {
        this.customGcExecutor = executor
        return this
    }

    override fun build(): Cask<K, V> {
        if (!::iTtl.isInitialized || iMaxSize == null || !::iLoader.isInitialized) {
            throw IllegalStateException("CaskBuilder is not properly initialized. Ensure ttl, maxSize, and loader are set.")
        }
        if (iMaxSize!! <= 0) {
            throw IllegalArgumentException("maxSize must be greater than 0")
        }
        if (evictionPolicy == EvictionPolicy.CUSTOM && customEviction == null) {
            throw IllegalStateException("Custom eviction policy requires a strategy")
        }
        val gc = when {
            sharedGcExecutor -> CaskRuntime.provideExecutor()
            customGcExecutor != null -> customGcExecutor!!
            else -> throw IllegalStateException("GC executor must be shared or explicitly provided")
        }

        return CaskImpl(iTtl, iMaxSize!!, iLoader, iEvictor, allowNulls, evictionPolicy, customEviction, gc)
    }
}
