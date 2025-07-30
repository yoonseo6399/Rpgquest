package io.github.yoonseo6399.rpgquest.quest

import kotlinx.coroutines.suspendCancellableCoroutine
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.entity.Entity
import net.minecraft.util.ActionResult
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
        val checkForInteraction = mutableListOf<InteractWith>()
        fun initialize(){
            ServerTickEvents.END_SERVER_TICK.register { server ->
                val removed = mutableListOf<QuestCondition>()
                checkForCondition.forEach {
                    if(it.check(it.activeQuest!!)){
                        it.continuation?.resume(Unit)
                        if(it.autoTerminate) removed.add(it)
                    }
                }
                checkForCondition.removeAll(removed)
            }
            UseEntityCallback.EVENT.register(UseEntityCallback { player, world, hand, entity, hitResult ->
                val removed = mutableListOf<InteractWith>()
                checkForInteraction.forEach {
                    if(it.activeQuest!!.player == player && entity == it.entity) it.continuation?.resume(Unit)
                    if(it.autoTerminate) removed += it
                }
                checkForInteraction.removeAll(removed)
                ActionResult.SUCCESS
            })
        }
    }

    class InteractWith(val entity : Entity) : QuestCondition() {
        override suspend fun await(quest: ActiveQuest){
            activeQuest = quest
            checkForInteraction += this
            return suspendCancellableCoroutine { continuation ->
                this.continuation = continuation
            }
        }
        override fun check(quest: ActiveQuest): Boolean {
            return false
        }
        override fun close() {
            checkForInteraction.remove(this)
        }
    }
    open class RepeatedCheck( val predicate : ActiveQuest.() -> Boolean) : QuestCondition() {
        override fun check(quest: ActiveQuest): Boolean {
            return quest.predicate()
        }

        override fun close() {
            checkForCondition.remove(this)
        }
    }
    class Arrive(pos : Vec3d,radius : Double) : RepeatedCheck({ player.pos.distanceTo(pos) <= radius })
    class Quest(id : String) : RepeatedCheck( { player.hasDoneWith(id) } )


    open fun check(quest: ActiveQuest) : Boolean = true
    open suspend fun await(quest: ActiveQuest){
        activeQuest = quest
        checkForCondition.add(this)
        if(check(quest)) {
            if(autoTerminate) close()
            return
        }

        return suspendCancellableCoroutine { continuation ->
            this.continuation = continuation
        }
    }
}