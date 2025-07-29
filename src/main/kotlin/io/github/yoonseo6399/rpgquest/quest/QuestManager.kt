package io.github.yoonseo6399.rpgquest.quest

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text

object QuestManager {
    val activeQuest = mutableListOf<ActiveQuest>()
    fun completion(player: PlayerEntity, quest: Quest) {
        player.sendMessage(Text.literal("퀘스트를 끝마쳤습니다"),false)
    }
    fun assign(player: PlayerEntity, quest: Quest) : ActiveQuest{
        return ActiveQuest(quest,player).also { activeQuest += it }
    }
}