package io.github.yoonseo6399.rpgquest.quest.npc

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType


import net.minecraft.command.EntitySelector
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object NpcCommand {
    fun register(dispatcher : CommandDispatcher<ServerCommandSource>){
        dispatcher.register(CommandManager.literal("npc").then(
            CommandManager.literal("register").then(
                CommandManager.argument("entity", EntityArgumentType.entity()).then(
                    CommandManager.argument("name", StringArgumentType.string()).then(
                        CommandManager.argument("type", StringArgumentType.string())
                            .executes { context ->
                                val name = context.getArgument("name",String::class.java)
                                val rawType = context.getArgument("type", String::class.java)
                                val entity = context.getArgument("entity", EntitySelector::class.java).getEntity(context.source)
                                val type = NpcType.entries.firstOrNull { it.name == rawType }
                                if(type == null){
                                    context.source.sendError(Text.literal("NPC type mismatch : $rawType"))
                                    return@executes 0
                                }
                                entity.setNpcData(Npc(name,type))
                                return@executes 1
                            }
                    )
                )
            )
        ).then(
            CommandManager.literal("check").then(
                CommandManager.argument("entity", EntityArgumentType.entity())
                    .executes {
                        val e = it.getArgument("entity", EntitySelector::class.java).getEntity(it.source)
                        it.source.sendFeedback({ Text.literal("is NPC? : ${e.isNpc()}") }, false)
                        if(e.isNpc()) it.source.sendFeedback({ Text.literal("data : ${e.npc}")},false)

                        1
                    }
            )
        ))
    }
    //val InvalidChoiceException = DynamicCommandExceptionType { id -> Text.literal("'$id'는 유효한 선택이 아닙니다.") }
}
