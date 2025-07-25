package io.github.yoonseo6399.rpgquest.quest.npc

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.github.yoonseo6399.rpgquest.Rpgquest
import io.github.yoonseo6399.rpgquest.kserializerToCodec
import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.minecraft.entity.Entity
import net.minecraft.util.Identifier

@Serializable
data class NpcData(
    val name: String,val type : NpcType
)
@Serializable
enum class NpcType {
    Mapper,Archaeologist
}
val NPC_DATA_ATTACHMENT: AttachmentType<NpcData> = AttachmentRegistry.createPersistent(
    Identifier.of(Rpgquest.MOD_ID,"npc_data"),
    kserializerToCodec(NpcData.serializer())
)

val Entity.npcData: NpcData?
    get() = this.getAttached(NPC_DATA_ATTACHMENT)

fun Entity.isNpc(): Boolean = this.hasAttached(NPC_DATA_ATTACHMENT)

fun Entity.setNpcData(data: NpcData) {
    this.setAttached(NPC_DATA_ATTACHMENT, data)
}

fun Entity.removeNpcData() {
    this.removeAttached(NPC_DATA_ATTACHMENT)
}
