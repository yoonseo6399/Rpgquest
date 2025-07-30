package io.github.yoonseo6399.rpgquest

import io.github.yoonseo6399.rpgquest.command.HelloCommand
import io.github.yoonseo6399.rpgquest.quest.QuestCommand
import io.github.yoonseo6399.rpgquest.quest.QuestCondition
import io.github.yoonseo6399.rpgquest.quest.npc.NpcCommand
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import org.slf4j.LoggerFactory


class Rpgquest : ModInitializer {
    companion object {
        const val MOD_ID = """rpgquest"""
        val LOGGER = LoggerFactory.getLogger(MOD_ID)
        var syncMode = false
    }
    override fun onInitialize() {
        RpgCoroutineScope.initialize()
        CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, _ ->
            HelloCommand.register(dispatcher)
            NpcCommand.register(dispatcher)
            QuestCommand.register(dispatcher,registryAccess)
        }
        QuestCondition.initialize()
    }
}

