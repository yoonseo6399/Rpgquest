package io.github.yoonseo6399.rpgquest.quest

import io.github.yoonseo6399.rpgquest.quest.npc.Npc
import kotlinx.coroutines.delay
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.world.World
import kotlin.time.Duration

class QuestBehaviors(val lines : MutableList<Behavior> = mutableListOf<Behavior>()) : MutableList<Behavior> by lines {
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
    abstract suspend fun play(player : PlayerEntity)
    class Delay(val delay: Duration) : Behavior(){
        override suspend fun play(player: PlayerEntity) {
            delay(delay)
        }
    }
    class GiveItem(val item : ItemStack) : Behavior(){
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
        override suspend fun play(player: PlayerEntity) {
            val world = player.server?.getWorld(World.OVERWORLD) ?: return
            val source = player.getCommandSource(world)
            source.dispatcher.parse(syntax,source)
        }
    }
    class Dialogue(val npc : Npc? = null, val text: Text) : Behavior(){
        override suspend fun play(player: PlayerEntity) {
            var ftext = text
            if(npc != null) ftext = npc.namePrefix.append(text)
            player.sendMessage(ftext,false)
        }
    }
}

