package net.ririfa.cask.util

enum class EvictionPolicy {
    LRU, FIFO, CUSTOM, NONE;

    val accessOrder: Boolean
        get() = this == LRU
}

@Deprecated("LFU is not yet implemented. Use LRU, FIFO, or CUSTOM instead.")
val EvictionPolicy.Companion.LFU: Nothing
    get() = throw UnsupportedOperationException("LFU eviction policy is not yet implemented")
