package io.github.yoonseo6399.rpgquest

import io.github.yoonseo6399.rpgquest.command.HelloCommand
import io.github.yoonseo6399.rpgquest.quest.npc.NpcCommand
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.argument.ArgumentTypes
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer
import net.minecraft.command.argument.serialize.StringArgumentSerializer
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

class Rpgquest : ModInitializer {
    companion object {
        const val MOD_ID = """rpgquest"""
        val LOGGER = LoggerFactory.getLogger(MOD_ID)
    }
    override fun onInitialize() {
        
        RpgCoroutineScope.initialize()
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            HelloCommand.register(dispatcher)
            NpcCommand.register(dispatcher)
        }
    }
}

