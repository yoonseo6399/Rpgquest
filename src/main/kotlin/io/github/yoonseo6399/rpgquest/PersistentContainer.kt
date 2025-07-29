package io.github.yoonseo6399.rpgquest

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.PersistentState
import net.minecraft.world.PersistentStateType
import net.minecraft.world.World

data class PersistentContainer(var questRegistry : Int = 0) : PersistentState() {


    companion object {
        private val CODEC: Codec<PersistentContainer> = RecordCodecBuilder.create { i -> i.group(
            Codec.INT.fieldOf("questRegistry").forGetter(PersistentContainer::questRegistry)
        ).apply(i, ::PersistentContainer) }

        private val type = PersistentStateType(
            Rpgquest.MOD_ID,
            ::PersistentContainer, // If there's no 'StateSaverAndLoader' yet create one and refresh variables
            CODEC, // If there is a 'StateSaverAndLoader' NBT, parse it with 'CODEC'
            null // Supposed to be an 'DataFixTypes' enum, but we can just pass null
        )

        fun getServerState(server: MinecraftServer): PersistentContainer {
            // (Note: arbitrary choice to use 'World.OVERWORLD' instead of 'World.END' or 'World.NETHER'.  Any work)
            val serverWorld: ServerWorld = server.getWorld(World.OVERWORLD) ?: throw IllegalStateException("Server world is null")

            // The first time the following 'getOrCreate' function is called, it creates a brand new 'StateSaverAndLoader' and
            // stores it inside the 'PersistentStateManager'. The subsequent calls to 'getOrCreate' pass in the saved
            // 'StateSaverAndLoader' NBT on disk to the codec in our type, using the codec to decode the nbt into our state
            val state: PersistentContainer = serverWorld.persistentStateManager.getOrCreate(type)

            // If state is not marked dirty, nothing will be saved when Minecraft closes.
            // Technically it's 'cleaner' if you only mark state as dirty when there was actually a change, but the vast majority
            // of mod writers are just going to be confused when their data isn't being saved, and so it's best just to 'markDirty' for them.
            // Besides, it's literally just setting a bool to true, and the only time there's a 'cost' is when the file is written to disk when
            // there were no actual change to any of the mods state (INCREDIBLY RARE).
            state.markDirty()

            return state
        }
    }
}