package io.github.yoonseo6399.rpgquest.quest

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Vec3d
import java.io.Closeable
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
sealed class QuestCondition(
    val autoTerminate: Boolean = true
) : Closeable {
    var activeQuest: ActiveQuest? = null
    var continuation : Continuation<Unit>? = null
    companion object {
        val checkForCondition = mutableListOf<QuestCondition>()
        fun initialize(){
            ServerTickEvents.END_SERVER_TICK.register { server ->
                val removed = mutableListOf<QuestCondition>()
                checkForCondition.filter { if(it.activeQuest != null) it.check(it.activeQuest!!) else false }.forEach {
                    it.continuation?.resume(Unit)
                    if(it.autoTerminate) removed.add(it)
                }
                checkForCondition.removeAll(removed)
            }
        }
    }

    class InteractNpcs(quest: Quest) : QuestCondition() {
        override fun close() {
            TODO("Not yet implemented")
        }
    }
    open class RepeatedCheck( val predicate : ActiveQuest.() -> Boolean) : QuestCondition() {
        init {
            checkForCondition.add(this)
        }

        override fun check(quest: ActiveQuest): Boolean {
            return quest.predicate()
        }

        override fun close() {
            checkForCondition.remove(this)
        }
    }
    class Arrive(pos : Vec3d,radius : Double) : RepeatedCheck({ player.pos.distanceTo(pos) <= radius })



    open fun check(quest: ActiveQuest) : Boolean = true
    suspend fun await(quest: ActiveQuest){
        activeQuest = quest
        if(check(quest)) {
            if(autoTerminate) close()
            return
        }

        return suspendCancellableCoroutine { continuation ->
            this.continuation = continuation
        }
    }
}