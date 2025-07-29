package io.github.yoonseo6399.rpgquest.quest

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import io.github.yoonseo6399.rpgquest.quest.Quest.Settings
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.time.format.TextStyle

object QuestCommand {
    val questsPending = mutableMapOf<String, QuestBuildHolder>()
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(CommandManager.literal("quest").then(
            CommandManager.literal("assign").executes { context ->
                context.source.player?.let { QuestManager.assign(it,testQuest) }
                1
            }
        ).then(
            CommandManager.literal("create").then(
                CommandManager.argument("Quest_ID", StringArgumentType.string()).executes { context ->
                    val id = context.getArgument("Quest_ID", String::class.java)
                    val existingQuest = QuestRegistry.get(id)
                    if(existingQuest != null){
                        questsPending[id] = QuestBuildHolder(existingQuest.startCondition,existingQuest.subQuest,existingQuest.behavior,existingQuest.settings)
                    }else questsPending[id] = QuestBuildHolder.Default //TODO

                    context.source.player?.sendMessage(Text.literal("§anew QuestBuilder id #$id created"))
                    context.source.player?.sendMessage(Text.literal("§ato complete the creation of Quest, /quest register <ID>"))
                    1
                }
            )
        ))
    }
}
data class QuestBuildHolder(val startCondition: QuestCondition?, val subQuest: Quest?, val behavior: QuestBehaviors?, val settings: Settings?){
    companion object {
        val Default
            get() = QuestBuildHolder(null,null,null,null)
    }
}