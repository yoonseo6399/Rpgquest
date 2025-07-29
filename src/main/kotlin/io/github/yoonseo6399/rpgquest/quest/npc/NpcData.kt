package io.github.yoonseo6399.rpgquest.quest.npc

import com.mojang.datafixers.util.Function3
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.github.yoonseo6399.rpgquest.Rpgquest
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.minecraft.component.ComponentType
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.NbtComponent
import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtOps
import net.minecraft.util.Identifier
import net.minecraft.util.StringIdentifiable
import java.util.Optional


data class NpcData(
    val name: String,val type : NpcType
){
    companion object {
        val CODEC = RecordCodecBuilder.create { i -> i.group(
            Codec.STRING.fieldOf("name").forGetter(NpcData::name),
            NpcType.CODEC.fieldOf("type").forGetter(NpcData::type)
        ).apply(i,::NpcData) }
    }
}
enum class NpcType : StringIdentifiable {
    Mapper,Archaeologist;
    companion object {
        val CODEC = StringIdentifiable.createCodec(NpcType::values)
    }

    override fun asString(): String? {
        return toString()
    }
}


val Entity.npcData: NpcData?
    get() {
        var npcData : NpcData? = null
        this.get(DataComponentTypes.CUSTOM_DATA)?.apply { npcData = it.toNpcData() }
        return npcData
    }

fun NpcData.toNbtElement(): NbtElement? {
    return NpcData.CODEC.encodeStart(NbtOps.INSTANCE, this).result().let { if(it.isPresent) it.get() else null }
}

fun NbtCompound.toNpcData(): NpcData? {
    return NpcData.CODEC.parse(NbtOps.INSTANCE, get("npc_data")).result().let { if(it.isPresent) it.get() else null }
}

fun Entity.isNpc(): Boolean = npcData != null

fun Entity.setNpcData(data: NpcData) {
    val compound = get(DataComponentTypes.CUSTOM_DATA)?.apply { nbtCompound ->
        nbtCompound.put("npc_data",data.toNbtElement())
    }
    this.setComponent(DataComponentTypes.CUSTOM_DATA,compound)
}
//fun <T> Entity.setCustomData(type : AttachmentType<T>,data: T) {
//    val compound = get(DataComponentTypes.CUSTOM_DATA)?.apply { nbtCompound ->
//        nbtCompound.put(key,data.toNbtElement())
//    }
//    this.setComponent(DataComponentTypes.CUSTOM_DATA,compound)
//}

