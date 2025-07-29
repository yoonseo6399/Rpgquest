package io.github.yoonseo6399.rpgquest.quest

import com.mojang.serialization.codecs.RecordCodecBuilder
import io.github.yoonseo6399.rpgquest.RpgCoroutineScope
import io.github.yoonseo6399.rpgquest.Rpgquest
import io.github.yoonseo6399.rpgquest.quest.npc.Npc
import kotlinx.coroutines.launch
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import kotlin.time.Duration.Companion.seconds

val testBehaviors = QuestBehaviors().apply {
    lines.addAll(listOf(
        Behavior.Npc(Npc("a"), Text.literal("my first Dialogue")),
        Behavior.Delay(1 .seconds),
        Behavior.Npc(Npc("a"), Text.literal("my second Dialogue")),
        Behavior.GiveItem(ItemStack(Items.DIAMOND,64)),
        Behavior.Npc(Npc("a"), Text.literal("I'll give you some DIAMOND!!!!!!!!"))
    ))
}
val testQuest = Quest(QuestCondition.Arrive(Vec3d(71.0, 68.0, -795.0),5.0),null,testBehaviors,
    Quest.Settings.Default.apply { notifyActivation = Text.literal("a Quest Notification") })
// 아이템 얻기, 선행 퀘스트 달성, 특정 장소 도달, npc 상호작용
open class Quest(val startCondition: QuestCondition?, val subQuest: Quest?, val behavior: QuestBehaviors, val settings: Settings){
    companion object {
        val LINER get() = Text.literal("-------------------------------------------------------------")
        //val CODEC = RecordCodecBuilder.create {  }
    }
    open fun notify(player: PlayerEntity) {
        player.sendMessage(LINER,false)
        player.sendMessage(settings.notifyActivation,false)
        player.sendMessage(LINER,false)
    }

    data class Settings(var notifyActivation : Text?){
        companion object {
            val Default get() = Settings(null)
        }
    }
}
open class ActiveQuest(
    startCondition: QuestCondition?,
    subQuest: Quest?,
    behavior: QuestBehaviors,
    settings: Settings,
    val player: PlayerEntity
) : Quest(startCondition, subQuest, behavior, settings) {
    constructor(quest: Quest,player: PlayerEntity) : this(quest.startCondition,quest.subQuest,quest.behavior,quest.settings,player)
    init {
        RpgCoroutineScope.launch {
            if(settings.notifyActivation != null) notify(player)
            startCondition?.await(this@ActiveQuest)
            //TODO notify
            behavior.show(player)
            QuestManager.completion(player,this@ActiveQuest)
            subQuest?.let { QuestManager.assign(player,it) }
        }
    }
}
