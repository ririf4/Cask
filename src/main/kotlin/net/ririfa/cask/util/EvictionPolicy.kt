package net.ririfa.cask.util

enum class EvictionPolicy {
    LRU, FIFO, CUSTOM, NONE;

    val accessOrder: Boolean
        get() = this == LRU
}
