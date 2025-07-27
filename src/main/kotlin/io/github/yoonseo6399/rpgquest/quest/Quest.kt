package io.github.yoonseo6399.rpgquest.quest

import io.github.yoonseo6399.rpgquest.RpgCoroutineScope
import io.github.yoonseo6399.rpgquest.Rpgquest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier
import kotlin.time.Duration

@Serializable
open class Quest(val id : String,val startCondition: QuestCondition?,val subQuest: Quest?,val dialogue: Dialogue,val settings: Settings){
    val identifier
        get() = Identifier.of(Rpgquest.MOD_ID, "quest.$id")

    @Serializable
    data class Settings(var notifyActivation : Boolean){
        companion object {
            val Default get() = Settings(false)
        }
    }
}
open class ActiveQuest(
    id: String,
    startCondition: QuestCondition?,
    subQuest: Quest?,
    dialogue: Dialogue,
    settings: Settings,
    val player: PlayerEntity
) : Quest(id, startCondition, subQuest, dialogue, settings) {
    constructor(quest: Quest,player: PlayerEntity) : this(quest.id,quest.startCondition,quest.subQuest,quest.dialogue,quest.settings,player)
    init {
        RpgCoroutineScope.launch {
            startCondition?.await()
            //TODO notify
            dialogue.show(player)
            QuestManager.completion(player,this@ActiveQuest)
        }
    }
}
object QuestManager {
    val activeQuest = mutableListOf<ActiveQuest>()
    fun completion(player: PlayerEntity,quest: Quest) {

    }
    fun assign(player: PlayerEntity,quest: Quest) : ActiveQuest{
        return ActiveQuest(quest,player).also { activeQuest += it }
    }
}

enum class ConditionType {
    REPEAT, EVENT
}
enum class QuestBranch {

}
@Serializable
class Dialogue() {
    val lines = mutableListOf<DialogueLine>()
    fun show(player: PlayerEntity) {
        RpgCoroutineScope.launch {
            for (line in lines) {
                line.show(player)
            }
        }
    }
}
@Serializable
sealed class DialogueLine() {
    abstract suspend fun show(player : PlayerEntity)
    class Delay(val delay: Duration) : DialogueLine(){
        override suspend fun show(player: PlayerEntity) {
            delay(delay)
        }
    }
    class Npc()
}

//class DialogueBuilder(){
//    fun npc(text : String) : DialogueLine{
//
//    }
//    fun player(text: String) : DialogueLine{
//
//    }
//    fun delay(duration: Duration) : DialogueLine{
//        return DialogueLine.Delay(duration)
//    }
//}
class Npc(entity: Entity)
