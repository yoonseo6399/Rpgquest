package io.github.yoonseo6399.rpgquest.quest

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents

object QuestRegistry {
    val registeredQuests = mutableMapOf<String,Quest>()
    fun initialize(){
        ServerLifecycleEvents.SERVER_STOPPING.register {
            saveAll()
        }
    }
    fun saveAll() {

    }
    fun register(id : String, quest : Quest){
        registeredQuests[id] = quest
    }
    fun get(id : String) = registeredQuests[id]

}