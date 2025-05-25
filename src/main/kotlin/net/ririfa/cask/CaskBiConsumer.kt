package net.ririfa.cask

@FunctionalInterface
fun interface CaskBiConsumer<K, V> {
    fun accept(key: K, value: V?)
}