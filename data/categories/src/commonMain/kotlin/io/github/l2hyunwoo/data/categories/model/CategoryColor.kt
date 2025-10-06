package io.github.l2hyunwoo.data.categories.model

enum class CategoryColor(val hexCode: String) {
    RED("#FF6B6B"),
    TEAL("#4ECDC4"),
    BLUE("#45B7D1"),
    SALMON("#FFA07A"),
    MINT("#98D8C8"),
    YELLOW("#F7DC6F"),
    PURPLE("#BB8FCE"),
    SKY("#85C1E2"),
    PEACH("#F8B88B"),
    TURQUOISE("#52C9B8");

    companion object {
        fun fromHex(hex: String): CategoryColor? = entries.find { it.hexCode == hex }
    }
}
