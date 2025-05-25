package net.ririfa.cask

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

object CaskRuntime {
    private val defaultExecutor = Executors.newSingleThreadScheduledExecutor {
        Thread(it, "cask-gc").apply { isDaemon = true }
    }

    fun provideExecutor(): ScheduledExecutorService = defaultExecutor

    fun shutdown() = defaultExecutor.shutdown()
    fun shutdownNow() = defaultExecutor.shutdownNow()
}
