package io.github.yoonseo6399.rpgquest.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object HelloCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            CommandManager.literal("hello")
                .executes(this::run)
        )
    }

    private fun run(context: CommandContext<ServerCommandSource>): Int {
        context.source.sendFeedback({ Text.literal("Hello, world!") }, false)
        return 1
    }
}
