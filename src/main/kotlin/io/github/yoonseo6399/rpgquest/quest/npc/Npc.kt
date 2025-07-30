package io.github.yoonseo6399.rpgquest.quest.npc

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtOps
import net.minecraft.text.Text
import net.minecraft.util.StringIdentifiable


data class Npc(
    val name: String,val type : NpcType
){
    companion object {
        val CODEC = RecordCodecBuilder.create { i -> i.group(
            Codec.STRING.fieldOf("name").forGetter(Npc::name),
            NpcType.CODEC.fieldOf("type").forGetter(Npc::type)
        ).apply(i,::Npc) }
    }

    val namePrefix get() = Text.literal("§7[ $name ] ")
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


val Entity.npc: Npc?
    get() {
        var npc : Npc? = null
        this.get(DataComponentTypes.CUSTOM_DATA)?.apply { npc = it.toNpcData() }
        return npc
    }

fun Npc.toNbtElement(): NbtElement? {
    return Npc.CODEC.encodeStart(NbtOps.INSTANCE, this).result().let { if(it.isPresent) it.get() else null }
}

fun NbtCompound.toNpcData(): Npc? {
    return Npc.CODEC.parse(NbtOps.INSTANCE, get("npc_data")).result().let { if(it.isPresent) it.get() else null }
}

fun Entity.isNpc(): Boolean = npc != null

fun Entity.setNpcData(data: Npc) {
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

