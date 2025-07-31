package io.github.yoonseo6399.rpgquest.quest

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import kotlinx.coroutines.suspendCancellableCoroutine
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.command.EntitySelector
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.scoreboard.ScoreboardObjective
import net.minecraft.util.ActionResult
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.Uuids
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

sealed class QuestCondition(
    val autoTerminate: Boolean = true
) {
    var activeQuest: ActiveQuest? = null
    var continuation : Continuation<Unit>? = null
    companion object {
        val checkForCondition = mutableListOf<QuestCondition>()
        val checkForInteraction = mutableListOf<InteractWith>()
        fun initialize(){
            ServerTickEvents.END_SERVER_TICK.register { server ->
                val removed = mutableListOf<QuestCondition>()

                ArrayList(checkForCondition).forEach {
                    if(it.check(it.activeQuest!!)){
                        it.continuation?.resume(Unit)
                        if(it.autoTerminate) removed.add(it)
                    }
                }
                checkForCondition.removeAll(removed)
            }
            UseEntityCallback.EVENT.register(UseEntityCallback { player, world, hand, entity, hitResult ->
                val removed = mutableListOf<InteractWith>()
                ArrayList(checkForInteraction).forEach {
                    if(it.activeQuest!!.player == player && entity == it.entity) it.continuation?.resume(Unit)
                    if(it.autoTerminate) removed += it
                }
                checkForInteraction.removeAll(removed)
                ActionResult.SUCCESS
            })
        }

        val CODEC: Codec<QuestCondition> = Type.CODEC.dispatch({ it.type }) { type ->
            when (type) {
                Type.Arrive -> Arrive.CODEC
                Type.Quest -> Quest.CODEC
                Type.InteractWith -> InteractWith.CODEC
                Type.ObtainItem -> ObtainItem.CODEC
            }
        }
    }
    abstract val type : Type
    enum class Type : StringIdentifiable {
        InteractWith,Arrive,Quest,ObtainItem;

        override fun asString(): String? {
            return name
        }

        companion object {
            val CODEC: Codec<Type> = StringIdentifiable.createCodec<Type>(Type::values)
        }
    }

    class InteractWith(val uuid : UUID) : QuestCondition() {
        private var _entity : Entity? = null
        val entity : Entity?
            get() {
                if(_entity == null) _entity = activeQuest?.player?.world?.getEntity(uuid)
                return _entity
            }
        companion object {
            val CODEC: MapCodec<InteractWith> = RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    Uuids.CODEC.fieldOf("uuid").forGetter { it.uuid }
                ).apply(instance, ::InteractWith)
            }
        }
        override val type: Type
            get() = Type.InteractWith
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
    }
    abstract class RepeatedCheck( val predicate : ActiveQuest.() -> Boolean = { true }) : QuestCondition() {

        override fun check(quest: ActiveQuest): Boolean {
            return quest.predicate()
        }
    }
    class Arrive(val pos : Vec3d,val radius : Double) : RepeatedCheck({ player.pos.distanceTo(pos) <= radius }) {
        override val type: Type
            get() = Type.Arrive
        companion object {
            val CODEC: MapCodec<Arrive> = RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    Vec3d.CODEC.fieldOf("pos").forGetter { it.pos },
                    Codec.DOUBLE.fieldOf("radius").forGetter { it.radius }
                ).apply(instance, ::Arrive)
            }
        }
    }
    class ObtainItem(val itemStack: ItemStack,val requiredAmount : Int) : RepeatedCheck( { player.inventory.hasEnough(itemStack,requiredAmount)} ){
        override val type: Type
            get() = Type.ObtainItem
        companion object {
            val CODEC: MapCodec<ObtainItem> = RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    ItemStack.CODEC.fieldOf("itemStack").forGetter { it.itemStack },
                    Codec.INT.fieldOf("requiredAmount").forGetter { it.requiredAmount }
                ).apply(instance, ::ObtainItem)
            }
        }
    }
    class Quest(val id : String) : RepeatedCheck( { player.hasDoneWith(id) } ) {
        override val type: Type
            get() = Type.Quest
        companion object {
            val CODEC: MapCodec<Quest> = RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    Codec.STRING.fieldOf("id").forGetter { it.id }
                ).apply(instance, ::Quest)
            }
        }
    }


    open fun check(quest: ActiveQuest) : Boolean = true
    open suspend fun await(quest: ActiveQuest){
        activeQuest = quest
        checkForCondition.add(this)

        return suspendCancellableCoroutine { continuation ->
            this.continuation = continuation
        }
    }
}
fun PlayerInventory.hasEnough(item : ItemStack,amount : Int) : Boolean{
    var tot = 0
    forEach {
        if(ItemStack.areItemsAndComponentsEqual(it,item)) tot += it.count
        if(tot >= amount) return true
    }
    return false
}