package io.github.yoonseo6399.rpgquest.quest

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.github.yoonseo6399.rpgquest.quest.npc.Npc
import kotlinx.coroutines.delay
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TextCodecs
import net.minecraft.util.StringIdentifiable
import net.minecraft.world.World
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


class QuestBehaviors(val lines : MutableList<Behavior> = mutableListOf<Behavior>()) : MutableList<Behavior> by lines {
    companion object {
        val CODEC : Codec<QuestBehaviors> = RecordCodecBuilder.create { i -> i.group(
            Behavior.CODEC.listOf().fieldOf("lines").forGetter { it.lines }
        ).apply(i) { QuestBehaviors(it) } }
    }
    var currentIndex = 0
    suspend fun show(player: PlayerEntity) {
        for ((i,line) in lines.withIndex()) {
            currentIndex = i
            line.play(player)
        }
    }
}

// 아이템 부여, 대화, 선택지
sealed class Behavior() {
    abstract val type : Type
    enum class Type : StringIdentifiable {
        Delay,GiveItem,Command,Dialogue;

        override fun asString(): String? {
            return name
        }

        companion object {
            val CODEC: Codec<Type> = StringIdentifiable.createCodec<Type>(Type::values)
        }
    }


    abstract suspend fun play(player : PlayerEntity)
    class Delay(val delay: Duration) : Behavior(){
        override val type: Type
            get() = Type.Delay
        override suspend fun play(player: PlayerEntity) {
            delay(delay)
        }
    }
    class GiveItem(val item : ItemStack) : Behavior(){
        override val type: Type
            get() = Type.GiveItem
        override suspend fun play(player: PlayerEntity) {
            player.inventory.offerOrDrop(item)
        }
    }
//    class TakeItem(val item : ItemStack,maxAmount: Int) : Behavior(){
//        override suspend fun play(player: PlayerEntity) {
//            val slot = player.inventory.getSlotWithStack(item)
//            player.inventory.
//        }
//    }
    class Command(val syntax: String) : Behavior(){
    override val type: Type
        get() = Type.Command
        override suspend fun play(player: PlayerEntity) {
            val world = player.server?.getWorld(World.OVERWORLD) ?: return
            val source = player.getCommandSource(world)
            source.dispatcher.parse(syntax,source)
        }
    }
    class Dialogue(val npc : Npc? = null, val text: Text) : Behavior(){
        override val type: Type
            get() = Type.Dialogue
        override suspend fun play(player: PlayerEntity) {
            var ftext = text
            if(npc != null) ftext = npc.namePrefix.append(text)
            player.sendMessage(ftext,false)
        }
    }

    companion object {
        val DELAY_CODEC: MapCodec<Behavior.Delay> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.LONG.fieldOf("delay").forGetter { it.delay.inWholeMilliseconds }
            ).apply(instance) { millis -> Behavior.Delay(millis.milliseconds) }
        }

        val GIVE_ITEM_CODEC: MapCodec<Behavior.GiveItem> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ItemStack.CODEC.fieldOf("item").forGetter { it.item }
            ).apply(instance) { item -> GiveItem(item) }
        }

        val COMMAND_CODEC: MapCodec<Behavior.Command> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.fieldOf("command").forGetter { it.syntax }
            ).apply(instance) { Command(it) }
        }

        val DIALOGUE_CODEC: MapCodec<Behavior.Dialogue> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Npc.CODEC.optionalFieldOf("npc").forGetter { Optional.ofNullable(it.npc) },
                TextCodecs.CODEC.fieldOf("text").forGetter { it.text }
            ).apply(instance) { npcOpt, text -> Dialogue(npcOpt.orElse(null), text) }
        }
        val CODEC: Codec<Behavior> = Type.CODEC.dispatch(Behavior::type,{ type ->
            when (type) {
                Type.Delay -> DELAY_CODEC
                Type.GiveItem -> GIVE_ITEM_CODEC
                Type.Command -> COMMAND_CODEC
                Type.Dialogue -> DIALOGUE_CODEC
            }
        })
    }
}

