package net.ririfa.cask

interface Cask<K, V> {
    fun get(key: K): V?
    fun put(key: K, value: V?)
    fun invalidate(key: K)
    fun refresh(key: K)
    fun size(): Int
}
