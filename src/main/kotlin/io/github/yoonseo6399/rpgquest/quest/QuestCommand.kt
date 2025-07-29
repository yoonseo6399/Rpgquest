package io.github.yoonseo6399.rpgquest.quest

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object QuestCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(CommandManager.literal("quest").then(
            CommandManager.literal("assign").executes { context ->
                context.source.player?.let { QuestManager.assign(it,testQuest) }
                1
            }
        ))
    }
}