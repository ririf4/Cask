package net.ririfa.cask

import net.ririfa.cask.impl.CaskBuilderImpl
import net.ririfa.cask.util.EvictionPolicy
import java.time.Duration
import java.util.concurrent.ScheduledExecutorService

interface CaskBuilder<K, V> {
    fun ttl(ttl: Duration): CaskBuilder<K, V>
    fun maxSize(size: Int): CaskBuilder<K, V>
    fun loader(loader: Function1<K, V>): CaskBuilder<K, V>
    fun onEvict(onEvict: CaskBiConsumer<K, V>): CaskBuilder<K, V>
    fun allowNullValues(allow: Boolean): CaskBuilder<K, V>
    fun evictionPolicy(policy: EvictionPolicy): CaskBuilder<K, V>
    fun evictionStrategy(strategy: EvictionStrategy<K, V>): CaskBuilder<K, V>
    fun shareGcExecutor(shared: Boolean): CaskBuilder<K, V>
    fun withCustomGcExecutor(executor: ScheduledExecutorService): CaskBuilder<K, V>
    fun build(): Cask<K, V>

    companion object {
        fun <K, V> newBuilder(): CaskBuilder<K, V> {
            return CaskBuilderImpl()
        }
    }
}
