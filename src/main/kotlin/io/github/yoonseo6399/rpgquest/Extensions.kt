package io.github.yoonseo6399.rpgquest

import net.minecraft.item.ItemStack
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text

interface TextLike {
    fun toText() : Text
}

fun ItemStack.prettyText() : Text {
    return name.copy().styled { it.withHoverEvent(HoverEvent.ShowItem(this@prettyText)) }
}