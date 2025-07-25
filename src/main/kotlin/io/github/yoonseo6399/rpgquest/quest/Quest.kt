package io.github.yoonseo6399.rpgquest.quest

import io.github.yoonseo6399.rpgquest.RpgCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import kotlin.time.Duration

class Quest {
    val startCondition : QuestCondition? = null
    val nextQuest : Quest? = null

    fun assign(player : PlayerEntity) {
        RpgCoroutineScope.launch {
            startCondition?.await()

        }
    }
}
enum class ConditionType {
    REPEAT, EVENT
}
enum class QuestBranch {

}
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
