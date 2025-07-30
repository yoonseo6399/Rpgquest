package io.github.yoonseo6399.rpgquest.quest

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.github.yoonseo6399.rpgquest.quest.Quest.Settings
import io.github.yoonseo6399.rpgquest.quest.npc.isNpc
import io.github.yoonseo6399.rpgquest.quest.npc.npc
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.EntitySelector
import net.minecraft.command.argument.*
import net.minecraft.entity.decoration.InteractionEntity
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import kotlin.time.Duration.Companion.microseconds

object QuestCommand {
    const val QUESTBUILDER_ID = "QuestBuilder_ID"
    const val QUEST_ID = "Quest_ID"
    val questsPending = mutableMapOf<String, QuestBuildHolder>()
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>,registryAccess: CommandRegistryAccess) {
        dispatcher.register(

            CommandManager.literal("quest").then(
                assignNode(registryAccess)
            ).then(
                createNode(registryAccess)
            ).then(
                CommandManager.literal("modify").then(
                    CommandManager.argument(QUESTBUILDER_ID,StringArgumentType.string()).then(
                        behaviorNode(registryAccess)
                    ).then(
                        conditionNode(registryAccess)
                    ).then(
                        subQuestNode(registryAccess)
                    ).then(CommandManager.literal("discard").executes { context ->
                        val (id,questBuildHolder) = getPendingQuest(context) ?: return@executes -1
                        questsPending.remove(id)
                        context.source.sendFeedback({Text.literal("§g퀘스트 빌더 #$id 를 삭제했습니다.")},true)
                        return@executes 1
                    })
                )
            ).then(
                CommandManager.literal("register").then(
                    CommandManager.argument(QUESTBUILDER_ID, StringArgumentType.string()).executes { context ->
                        val (id,questBuildHolder) = getPendingQuest(context) ?: return@executes -1
                        val p = context.source.player ?: return@executes -1
                        QuestManager.getInstance(p.server!!).register(id,questBuildHolder.build())
                        questsPending.remove(id)
                        context.source.sendFeedback({Text.literal("§g퀘스트 빌더 #$id 를 퀘스트 #$id 로 저장하고 빌더를 삭제했습니다.")},true)
                        return@executes 1
                    }
                )
            ).then(
                CommandManager.literal("list").executes { context ->
                    context.source.sendFeedback({Text.literal("§g퀘스트 빌더")},false)
                    questsPending.forEach {
                        context.source.sendFeedback({Text.literal("id#${it.key}")},false)
                    }
                    context.source.sendFeedback({Text.literal("")},false)
                    context.source.sendFeedback({Text.literal("§g퀘스트")},false)
                    val p = context.source.player ?: return@executes -1
                    QuestManager.getInstance(p.server!!).registeredQuests.forEach {
                        context.source.sendFeedback({Text.literal("id#${it.key}")},false)
                    }
                    return@executes 1
                }
            )
        )
    }
    fun assignNode(registryAccess: CommandRegistryAccess) = CommandManager.literal("assign").then(CommandManager.argument(QUEST_ID, StringArgumentType.string()).executes { context ->
        val id = context.getArgument(QUEST_ID, String::class.java)
        val p = context.source.player ?: return@executes -1
        if(!QuestManager.getInstance(p.server!!).registeredQuests.contains(id)) {
            context.source.sendFeedback({Text.literal("§c퀘스트 #$id 가 존재하지 않습니다.")},false)
            return@executes -1
        }
        context.source.sendFeedback({Text.literal("§a퀘스트 #$id 를 ${p.name} 에게 부여했습니다.")},true)
        QuestManager.getInstance(p.server!!).assign(p,id)
        1
    })
    fun createNode(registryAccess: CommandRegistryAccess) = CommandManager.literal("create").then(
        CommandManager.argument(QUESTBUILDER_ID, StringArgumentType.string()).executes { context ->
            val id = context.getArgument(QUESTBUILDER_ID, String::class.java)
            val p = context.source.player ?: return@executes -1

            if(questsPending.contains(id)) {
                context.source.sendFeedback({Text.literal("§c이미 퀘스트 빌더 #$id 가 존재합니다.")},false)
                return@executes -1
            }
            val existingQuest = QuestManager.getInstance(p.server!!).get(id)
            if(existingQuest != null){
                questsPending[id] = QuestBuildHolder(existingQuest.startCondition.toMutableList(),existingQuest.subQuest.toMutableList(),existingQuest.behavior,existingQuest.settings)
                context.source.sendFeedback({Text.literal("§aswitching Existed Quest id #$id to Builder")},false)

            }else questsPending[id] = QuestBuildHolder.Default
            context.source.sendFeedback({Text.literal("§a새로운 퀘스트 빌더 #$id 를 생성하였습니다.")},true)
            context.source.sendFeedback({ Text.literal("§a퀘스트 생성을 마치려면, /quest register <ID> 를 사용하세요.") },false)
            1
        }
    )
    fun subQuestNode(registryAccess: CommandRegistryAccess) = CommandManager.literal("SubQuest").then(
        CommandManager.literal("add").then(
            CommandManager.argument("SubQuest-ID", StringArgumentType.string()).executes { context ->
                val (id,questBuildHolder) = getPendingQuest(context) ?: return@executes -1
                val subid = context.getArgument("SubQuest-ID", String::class.java)
                val p = context.source.player ?: return@executes -1
                if(!QuestManager.getInstance(p.server!!).registeredQuests.contains(subid)) {
                    context.source.sendFeedback({Text.literal("§c아이디 #$subid 에 퀘스트가 존재하지 않습니다.")},false)
                    return@executes -1
                }
                questBuildHolder.subQuest.add(subid)
                context.source.sendFeedback({Text.literal("§g퀘스트 #$id 에 서브퀘스트 #$subid 를 추가했습니다")},true)
                return@executes 1
            }
        )
    ).then(
        CommandManager.literal("remove").then(CommandManager.argument("SubQuest-ID", StringArgumentType.string()).executes { context ->
            val (id,questBuildHolder) = getPendingQuest(context) ?: return@executes -1
            val subid = context.getArgument("SubQuest-ID", String::class.java)
            val re = questBuildHolder.subQuest.removeIf { it == subid }
            if(re) context.source.sendFeedback({Text.literal("§g아이디 #$subid 퀘스트의 서브퀘스트 #$subid 를 삭제했습니다.")},true)
            else context.source.sendFeedback({Text.literal("§c아이디 #$subid 퀘스트의 서브퀘스트 #$subid 를 찾을 수 없습니다.")},false)
            return@executes 1
        })
    ).then(
        CommandManager.literal("list").executes { context ->
            val (id,questBuildHolder) = getPendingQuest(context) ?: return@executes -1
            context.source.sendFeedback({Text.literal("§c퀘스트 #$id 의 서브퀘스트로 지정된 퀘스트들 : ${questBuildHolder.subQuest}")},false)
            return@executes 1
        }
    )
    fun behaviorNode(registryAccess: CommandRegistryAccess): LiteralArgumentBuilder<ServerCommandSource> {
        return CommandManager.literal("Behavior").then(
            CommandManager.literal("add").then(
                CommandManager.literal("Delay").then(CommandManager.argument("Duration", TimeArgumentType.time()).executes { context ->
                    val duration = context.getArgument("Duration",Int::class.java)
                    val (id,questBuildHolder) = getPendingQuest(context) ?: return@executes -1
                    questBuildHolder.behavior.add(Behavior.Delay(duration.tick))
                    context.source.sendFeedback({ Text.literal("퀘스트#$id 의 행동 리스트를 수정했습니다")},true)
                    1
                })
            ).then(
                CommandManager.literal("GiveItem").then(
                    CommandManager.argument("Item", ItemStackArgumentType.itemStack(registryAccess)).then(
                        CommandManager.argument("Amount", IntegerArgumentType.integer(1,64)).executes { context ->
                        val (id,questBuildHolder) = getPendingQuest(context) ?: return@executes -1
                        val item = context.getArgument("Item", ItemStackArgument::class.java).createStack(context.getArgument("Amount",Int::class.java),false)
                        questBuildHolder.behavior.add(Behavior.GiveItem(item))
                        context.source.sendFeedback({ Text.literal("퀘스트#$id 의 행동 리스트를 수정했습니다")},true)
                        1
                    })
                )
            ).then(
                CommandManager.literal("Command").then(
                    CommandManager.argument("CommandLine", StringArgumentType.greedyString()).executes { context ->
                        val (id,questBuildHolder) = getPendingQuest(context) ?: return@executes -1
                        val command = context.getArgument("CommandLine",String::class.java)
                        questBuildHolder.behavior.add(Behavior.Command(command))
                        context.source.sendFeedback({ Text.literal("퀘스트#$id 의 행동 리스트를 수정했습니다")},true)
                        1
                    }
                )
            ).then(
                CommandManager.literal("Dialogue").then(
                    CommandManager.argument("Text", TextArgumentType.text(registryAccess)).executes { context ->
                        val (id,questBuildHolder) = getPendingQuest(context) ?: return@executes -1
                        val text = context.getArgument("Text", Text::class.java)
                        questBuildHolder.behavior.add(Behavior.Dialogue(null,text))
                        context.source.sendFeedback({ Text.literal("퀘스트#$id 의 행동 리스트를 수정했습니다")},true)
                        1
                    }.then(
                        CommandManager.literal("--byNpc").then(
                            CommandManager.argument("entity", EntityArgumentType.entity()).executes { context ->
                                val (id,questBuildHolder) = getPendingQuest(context) ?: return@executes -1
                                val text = context.getArgument("Text", Text::class.java)
                                val npc = context.getArgument("entity", EntitySelector::class.java).getEntity(context.source)
                                if(!npc.isNpc()){
                                    context.source.sendFeedback({ Text.literal("§cNpc 로 지정된 엔티티가 아닙니다. /npc register 항목을 참고하세요.")},false)
                                    return@executes -1
                                }
                                questBuildHolder.behavior.add(Behavior.Dialogue(npc.npc,text))
                                context.source.sendFeedback({ Text.literal("퀘스트#$id 의 행동 리스트를 수정했습니다.")},true)
                                1
                            }
                        )
                    )
                )
            )
        ).then(
            CommandManager.literal("remove").then(CommandManager.argument("index", IntegerArgumentType.integer()).executes { context ->
                val (id,questBuildHolder) = getPendingQuest(context) ?: return@executes -1
                val index = context.getArgument("index", Int::class.java)
                if(index !in 0..<questBuildHolder.behavior.size) {
                    context.source.sendFeedback({ Text.literal("index 가 범위를 벗어낫습니다. {0..${questBuildHolder.behavior.size-1}}")},false)
                    return@executes -1
                }
                val b = questBuildHolder.behavior.removeAt(index)
                context.source.sendFeedback({ Text.literal("퀘스트#$id 의 #$b 행동을 삭제했습니다.")},true)
                1
            })
        ).then(
            CommandManager.literal("list").executes { context ->
                val (id,questBuildHolder) = getPendingQuest(context) ?: return@executes -1
                context.source.sendFeedback({ Text.literal("퀘스트#$id 의 행동 리스트를 확인합니다.")},true)
                questBuildHolder.behavior.forEachIndexed { i,e ->
                    context.source.sendFeedback({ Text.literal("#$i - ${e.javaClass.simpleName}") },false)
                }
                return@executes 1
            }
        )
    }
    fun conditionNode(registryAccess: CommandRegistryAccess) : LiteralArgumentBuilder<ServerCommandSource> {
        return CommandManager.literal("Condition").then(
            CommandManager.literal("add").then(
                CommandManager.literal("ArriveAt").then(
                    CommandManager.argument("Pos", Vec3ArgumentType.vec3()).then(
                        CommandManager.argument("radius", DoubleArgumentType.doubleArg(0.0)).executes { context ->
                            val pos = context.getArgument("Pos", PosArgument::class.java).getPos(context.source)
                            val radius = context.getArgument("radius",Double::class.java)
                            val (id,questBuildHolder) = getPendingQuest(context) ?: return@executes -1
                            questBuildHolder.startCondition.add(QuestCondition.Arrive(pos,radius))
                            context.source.sendFeedback({ Text.literal("퀘스트#$id 의 조건 리스트를 수정했습니다")},true)
                            1
                        }
                    )
                )
            ).then(
                CommandManager.literal("InteractWith").then(
                    CommandManager.argument("entity", EntityArgumentType.entity()).executes { context ->
                        val (id,questBuildHolder) = getPendingQuest(context) ?: return@executes -1
                        val entity = context.getArgument("entity", EntitySelector::class.java).getEntity(context.source)
                        if(entity !is InteractionEntity && !entity.isNpc()) {
                            context.source.sendFeedback({ Text.literal("§cNpc 로 지정된 엔티티가 아니거나, Interaction 엔티티가 아닙니다.")},false)
                            return@executes -1
                        }
                        questBuildHolder.startCondition.add(QuestCondition.InteractWith(entity.uuid))
                        context.source.sendFeedback({ Text.literal("퀘스트#$id 의 조건 리스트를 수정했습니다")},true)
                        1
                    }
                )
            ).then(
                CommandManager.literal("RequireQuest").then(
                    CommandManager.argument("previous-Quest_ID", StringArgumentType.string()).executes { context ->
                        val (id, questBuildHolder) = getPendingQuest(context) ?: return@executes -1
                        val questid = context.getArgument("previous-Quest_ID", String::class.java)
                        questBuildHolder.startCondition.add(QuestCondition.Quest(questid))
                        context.source.sendFeedback({ Text.literal("퀘스트#$id 의 조건 리스트를 수정했습니다") }, true)
                        1
                    }
                )
            )
        ).then(
            CommandManager.literal("remove").then(CommandManager.argument("index", IntegerArgumentType.integer()).executes { context ->
                val (id,questBuildHolder) = getPendingQuest(context) ?: return@executes -1
                val index = context.getArgument("index", Int::class.java)
                if(index !in 0..<questBuildHolder.startCondition.size) {
                    context.source.sendFeedback({ Text.literal("index 가 범위를 벗어낫습니다. {0..${questBuildHolder.startCondition.size-1}}")},false)
                    return@executes -1
                }
                val b = questBuildHolder.startCondition.removeAt(index)
                context.source.sendFeedback({ Text.literal("퀘스트#$id 의 #$b 조건을 삭제했습니다.")},true)
                1
            })
        ).then(
            CommandManager.literal("list").executes { context ->
                val (id,questBuildHolder) = getPendingQuest(context) ?: return@executes -1
                context.source.sendFeedback({ Text.literal("퀘스트#$id 의 조건 리스트를 확인합니다.")},true)
                questBuildHolder.startCondition.forEachIndexed { i,e ->
                    context.source.sendFeedback({ Text.literal("#$i - ${e.javaClass.simpleName}") },false)
                }
                return@executes 1
            }
        )
    }

    fun getPendingQuest(context : CommandContext<ServerCommandSource>, autoMessage : Boolean = true) : Pair<String,QuestBuildHolder>?{
        val id = context.getArgument(QUESTBUILDER_ID,String::class.java) ?: return null
        val quest = questsPending[id]
        if(quest == null && autoMessage) context.source.player?.sendMessage(Text.literal("§c퀘스트 빌더 #$id 는 존재하지 않습니다."))
        return if(quest != null) id to quest else null
    }
}
data class QuestBuildHolder(val startCondition: MutableList<QuestCondition>, val subQuest: MutableList<String>, val behavior: QuestBehaviors, val settings: Settings){
    companion object {
        val Default
            get() = QuestBuildHolder(mutableListOf(),mutableListOf(), QuestBehaviors(),Quest.Settings.Default)
    }
    fun build() : Quest {
        return Quest(startCondition,subQuest,behavior,settings)
    }
}

val Int.tick
    get() = this.microseconds*50
fun kotlin.time.Duration.toTicks() = inWholeMicroseconds/50