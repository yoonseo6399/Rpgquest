package io.github.yoonseo6399.rpgquest.quest.npc

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandExceptionType
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.serialization.Codec
import io.github.yoonseo6399.rpgquest.kserializerToCodec
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry



import net.minecraft.command.CommandSource
import net.minecraft.command.EntitySelector
import net.minecraft.command.argument.ArgumentTypes
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.command.argument.EnumArgumentType
import net.minecraft.data.report.CommandSyntaxProvider
import net.minecraft.entity.Entity
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.network.PacketByteBuf
import com.google.gson.JsonObject
import com.google.gson.JsonArray
import java.util.concurrent.CompletableFuture
import kotlin.jvm.Throws
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.command.argument.serialize.ArgumentSerializer

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
                                entity.setAttached(NPC_DATA_ATTACHMENT, NpcData(name,type))
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
                        if(e.isNpc()) it.source.sendFeedback({ Text.literal("data : ${e.npcData}")},false)

                        1
                    }
            )
        ))
    }
    //val InvalidChoiceException = DynamicCommandExceptionType { id -> Text.literal("'$id'는 유효한 선택이 아닙니다.") }
}
//class ListedChoiceArgumentType(val list : List<String>) : ArgumentType<String> {
//
//    @Throws(CommandSyntaxException::class)
//    override fun parse(reader: StringReader): String {
//        val argument = reader.string
//        if(list.contains(argument)) {
//
//            return argument
//        }
//        throw InvalidChoiceException.create(argument)
//    }
//
//    override fun <S : Any?> listSuggestions(
//        context: CommandContext<S?>?,
//        builder: SuggestionsBuilder?
//    ): CompletableFuture<Suggestions?>? {
//        return CommandSource.suggestMatching(list, builder)
//    }
//
//    companion object {
//        fun listedChoice(list : List<String>) = ListedChoiceArgumentType(list)
//
//        val InvalidChoiceException = DynamicCommandExceptionType { id ->
//            Text.literal("'${id}'는 유효한 선택이 아닙니다.")
//        }
//    }
//}
