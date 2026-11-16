package com.plantaomaluco.domain

import kotlinx.serialization.Serializable

enum class Gesture { TAP, DRAG }

enum class Tool(val gesture: Gesture) {
    TWEEZERS(Gesture.DRAG),
    NEEDLE(Gesture.DRAG),
    SPONGE(Gesture.DRAG),
    SPRAY(Gesture.TAP),
    BANDAGE(Gesture.TAP),
}

enum class ProblemType(val tool: Tool, val healPerUse: Float) {
    SPLINTER(Tool.TWEEZERS, 3f),
    CUT(Tool.NEEDLE, 1.8f),
    DIRT(Tool.SPONGE, 1.4f),
    GERM(Tool.SPRAY, 0.25f),
    BRUISE(Tool.BANDAGE, 0.5f),
}

@Serializable
data class ProblemSpec(
    val type: ProblemType,
    val x: Float,
    val y: Float,
)

enum class HairStyle { SHORT, SPIKY, BUN, LONG, BALD }

enum class Accessory { NONE, GLASSES, MUSTACHE, HELMET }

@Serializable
data class Look(
    val skin: String,
    val hair: String,
    val hairStyle: HairStyle,
    val gown: String,
    val accessory: Accessory,
) {
    companion object {
        val Mascot = Look(
            skin = "#FFD3B4",
            hair = "#5B3A29",
            hairStyle = HairStyle.SHORT,
            gown = "#7FC8E8",
            accessory = Accessory.NONE,
        )
    }
}

@Serializable
data class Patient(
    val id: Int,
    val name: String,
    val story: String,
    val image: String?,
    val look: Look,
    val maxHealth: Float,
    val drainPerSecond: Float,
    val wrongToolDamage: Float,
    val problems: List<ProblemSpec>,
)
