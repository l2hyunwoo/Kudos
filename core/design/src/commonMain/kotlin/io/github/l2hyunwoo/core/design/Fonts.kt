package io.github.l2hyunwoo.core.design

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import kudos.core.design.generated.resources.Res
import kudos.core.design.generated.resources.plus_jakarta_sans_variable
import kudos.core.design.generated.resources.space_grotesk_variable
import org.jetbrains.compose.resources.Font

// Variable-font weights are realized through the `wght` axis: each Font entry pins the same
// .ttf to a weight, and CMP forwards a matching FontVariation.Settings(weight) into Skia/Android
// so the variable face is interpolated to that weight (see compose-resources FontResources.skiko.kt).
private val coveredWeights = listOf(
    FontWeight.Normal,
    FontWeight.Medium,
    FontWeight.SemiBold,
    FontWeight.Bold,
    FontWeight.ExtraBold,
    FontWeight.Black,
)

@Composable
private fun variableFontFamily(resource: org.jetbrains.compose.resources.FontResource): FontFamily =
    FontFamily(
        coveredWeights.map { weight ->
            Font(
                resource = resource,
                weight = weight,
                style = FontStyle.Normal,
                variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
            )
        },
    )

@Composable
fun spaceGroteskFamily(): FontFamily = variableFontFamily(Res.font.space_grotesk_variable)

@Composable
fun plusJakartaSansFamily(): FontFamily = variableFontFamily(Res.font.plus_jakarta_sans_variable)
