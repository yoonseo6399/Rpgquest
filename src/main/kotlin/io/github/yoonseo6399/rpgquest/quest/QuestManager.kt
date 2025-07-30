package io.github.yoonseo6399.rpgquest.quest

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
    import io.github.yoonseo6399.rpgquest.Rpgquest
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Uuids
import net.minecraft.world.PersistentState
import net.minecraft.world.PersistentStateType
import net.minecraft.world.World
import java.util.*

data class QuestManager(
    val registeredQuests : MutableMap<String, Quest> = mutableMapOf(),
    val playerDone : MutableMap<UUID, MutableSet<String>> = mutableMapOf(),
    val playerActive : MutableMap<UUID, MutableSet<String>> = mutableMapOf()
) : PersistentState() {
    companion object {
        //val instance = QuestManager()
        val f : Codec<MutableMap<String, Quest>> = Codec.unboundedMap(Codec.STRING,Quest.CODEC).xmap({ it.toMutableMap() },{ it.toMap() })
        val s = Codec.unboundedMap(Uuids.CODEC,Codec.STRING.listOf().xmap({ it.toMutableSet() },{ it.toList() })).xmap({ it.toMutableMap() },{ it.toMap() })
        private val CODEC: Codec<QuestManager> = RecordCodecBuilder.create { i -> i.group( //TODO
            f.fieldOf("registeredQuests").forGetter(QuestManager::registeredQuests),
            s.fieldOf("playerDone").forGetter { it.playerDone },
            s.fieldOf("playerActive").forGetter { it.playerActive },
        ).apply(i, ::QuestManager) }
        private val type = PersistentStateType(
            Rpgquest.MOD_ID, ::QuestManager, CODEC, null
        )
        fun getInstance(server: MinecraftServer): QuestManager {
            val serverWorld: ServerWorld = server.getWorld(World.OVERWORLD) ?: throw IllegalStateException("Server world is null")
            val state: QuestManager = serverWorld.persistentStateManager.getOrCreate(type)
            state.markDirty()
            return state
            //return instance
        }
    }
    val activeQuests = mutableMapOf<ActiveQuest,String>()

    fun completion(player: PlayerEntity, questID: String) {
        player.sendMessage(Text.literal("퀘스트를 끝마쳤습니다"),false)
        makeSafe(player)
        playerActive[player.uuid]!!.remove(questID)
        playerDone[player.uuid]!!.add(questID)
    }
    fun completion(playerEntity: PlayerEntity,activeQuest: ActiveQuest) {
        val id = activeQuests[activeQuest] ?: return Rpgquest.LOGGER.warn("tlqkf 대체 어떻게 activeQuest 가 할당되지 않은상태에서 completion 했지?")
        completion(playerEntity,id)
    }
    fun assign(player: PlayerEntity, questID: String) : ActiveQuest?{
        val quest = get(questID) ?: return null
        makeSafe(player)
        val acquest =ActiveQuest(quest,player)
        playerActive[player.uuid]!! += questID
        activeQuests += acquest to questID
        return acquest
    }
    fun makeSafe(playerEntity: PlayerEntity){
        playerActive.getOrPut(playerEntity.uuid) { mutableSetOf() }
        playerDone.getOrPut(playerEntity.uuid) { mutableSetOf() }
    }

    fun register(id : String, quest : Quest){
        registeredQuests[id] = quest
    }
    fun get(id : String) = registeredQuests[id]

    fun getDoneQuest(playerEntity: PlayerEntity) : Set<String>{
        return playerDone.getOrPut(playerEntity.uuid) { mutableSetOf() }
    }
    fun getActiveQuest(playerEntity: PlayerEntity) = playerActive[playerEntity.uuid]
}

fun PlayerEntity.hasDoneWith(questID : String) = QuestManager.getInstance(this.server!!).getDoneQuest(this).contains(questID)