package io.github.yoonseo6399.rpgquest.quest

import kotlinx.coroutines.delay
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import kotlin.time.Duration

class QuestBehaviors() {
    val lines = mutableListOf<Behavior>()
    suspend fun show(player: PlayerEntity) {
        for (line in lines) {
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
    class Npc(val npc : io.github.yoonseo6399.rpgquest.quest.npc.Npc, val text: Text) : Behavior(){
        override suspend fun play(player: PlayerEntity) {
            player.sendMessage(text,false)
        }
    }
}