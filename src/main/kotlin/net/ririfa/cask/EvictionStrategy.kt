package net.ririfa.cask

fun interface EvictionStrategy<K, V> {
    fun shouldEvict(cache: Map<K, V>, key: K, entry: V): Boolean
}
