package io.github.yoonseo6399.rpgquest.quest

import kotlinx.coroutines.suspendCancellableCoroutine
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.entity.player.PlayerEntity
import java.io.Closeable
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

sealed class QuestCondition(
    val quest: Quest,
    val autoTerminate: Boolean = true
) : Closeable {
    var continuation : Continuation<Unit>? = null
    companion object {
        val checkForCondition = mutableListOf<QuestCondition>()
        fun initialize(){
            ServerTickEvents.END_SERVER_TICK.register { server ->
                val removed = mutableListOf<QuestCondition>()
                checkForCondition.mapNotNull { it.takeIf { it.check() } }.forEach {
                    it.continuation?.resume(Unit)
                    if(it.autoTerminate) removed.add(it)
                }
                checkForCondition.removeAll(removed)
            }
        }
    }

    class InteractNpcs(quest: Quest) : QuestCondition(quest) {
        override fun close() {
            TODO("Not yet implemented")
        }

    }
    class RepeatedCheck(quest: Quest,val predicate : Quest.() -> Boolean) : QuestCondition(quest) {
        init {
            checkForCondition.add(this)
        }

        override fun check(): Boolean {
            return quest.predicate()
        }

        override fun close() {
            checkForCondition.remove(this)
        }
    }



    open fun check() : Boolean = true
    suspend fun await(){
        if(check()) {
            if(autoTerminate) close()
            return
        }

        return suspendCancellableCoroutine { continuation ->
            this.continuation = continuation
        }
    }
}