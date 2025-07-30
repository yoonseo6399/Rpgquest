package io.github.yoonseo6399.rpgquest.quest

import io.github.yoonseo6399.rpgquest.RpgCoroutineScope
import kotlinx.coroutines.launch
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text

// 아이템 얻기, 선행 퀘스트 달성, 특정 장소 도달, npc 상호작용
open class Quest(val startCondition: List<QuestCondition>, val subQuest: List<String>, val behavior: QuestBehaviors, val settings: Settings){
    companion object {
        val LINER get() = Text.literal("-------------------------------------------------------------")
        //val CODEC = RecordCodecBuilder.create {  }
    }
    open fun notify(player: PlayerEntity) {
        player.sendMessage(LINER,false)
        player.sendMessage(settings.notifyActivation,false)
        player.sendMessage(LINER,false)
    }

    data class Settings(var notifyActivation : Text? = null,var autoAssign : Boolean = false){
        companion object {
            val Default get() = Settings(null)
        }
    }
}
open class ActiveQuest(
    startCondition: List<QuestCondition>,
    subQuest: List<String>,
    behavior: QuestBehaviors,
    settings: Settings,
    val player: PlayerEntity
) : Quest(startCondition, subQuest, behavior, settings) {
    constructor(quest: Quest,player: PlayerEntity) : this(quest.startCondition,quest.subQuest,quest.behavior,quest.settings,player)
    var status : QuestStatus? = null
    init {
        RpgCoroutineScope.launch {
            if(settings.notifyActivation != null) notify(player)
            startCondition.forEachIndexed { i,e ->
                status = QuestStatus.WaitForCondition(i)
                e.await(this@ActiveQuest)
            }
            //TODO notify
            status = QuestStatus.Behavior( { behavior.currentIndex } )
            behavior.show(player)
            QuestManager.getInstance(player.server!!).completion(player,this@ActiveQuest)
            subQuest.forEach { QuestManager.getInstance(player.server!!).assign(player,it) }
        }
    }
}
sealed class QuestStatus(index : Int) {
    class WaitForCondition(index: Int) : QuestStatus(index)
    class Behavior(index: Int) : QuestStatus(index){
        constructor(provider: () -> Int) : this(provider.invoke())
    }
}