package io.github.l2hyunwoo.data.categories.model

enum class CategoryColor(val hexCode: String) {
    LILAC("#C9B8F0"),
    MINT("#A6E3C9"),
    SKY("#A8CDF2"),
    PEACH("#F4BFA6"),
    BUTTER("#F2D89C"),
    ROSE("#F2B5C8");

    companion object {
        fun fromHex(hex: String): CategoryColor? = entries.find { it.hexCode == hex }
    }
}
