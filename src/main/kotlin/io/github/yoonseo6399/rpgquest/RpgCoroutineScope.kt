package io.github.yoonseo6399.rpgquest

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import kotlin.coroutines.CoroutineContext

object RpgCoroutineScope : CoroutineScope {
    private var internalContext : CoroutineContext = SupervisorJob() + Dispatchers.Default

    override val coroutineContext: CoroutineContext
        get() = internalContext

    fun initialize(){
        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            Rpgquest.LOGGER.info("CoroutineScope Initializing")
            internalContext = SupervisorJob() + server.asCoroutineDispatcher()
        }
        ServerLifecycleEvents.SERVER_STOPPING.register { server ->
            Rpgquest.LOGGER.info("Canceling RpgScope")
            this.cancel()
        }
    }
}