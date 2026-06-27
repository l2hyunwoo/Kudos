/*
 * Designed and developed by 2022 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.skydoves.cloudy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.layer.GraphicsLayer

/**
 * State holder for background blur (backdrop blur) functionality.
 *
 * [Sky] manages the captured background content and coordinate information
 * for backdrop blur rendering. The name "Sky" represents the background
 * (sky) that "cloudy" (blur) effects are applied over.
 *
 * ## Usage
 *
 * ```kotlin
 * val sky = rememberSky()
 *
 * Box(modifier = Modifier.sky(sky)) {
 *   // Background content (images, lists, etc.)
 *   AsyncImage(model = "background.jpg", modifier = Modifier.fillMaxSize())
 *
 *   // Glassmorphism overlay (cloud over the sky)
 *   Card(
 *     modifier = Modifier
 *       .align(Alignment.Center)
 *       .cloudy(sky = sky, radius = 25)
 *   ) {
 *     Text("Frosted Glass Card")
 *   }
 * }
 * ```
 *
 * ## State Lifecycle
 *
 * 1. [Sky] is created via [rememberSky]
 * 2. [Modifier.sky] captures content to [backgroundLayer]
 * 3. [Modifier.cloudy] with sky parameter reads and blurs the captured content
 * 4. Call [invalidate] when background content changes to trigger re-capture
 *
 * @see rememberSky
 * @see Modifier.sky
 * @see Modifier.cloudy
 */
@Stable
public class Sky internal constructor() {

  /**
   * GraphicsLayer containing the captured background content.
   * Set by [Modifier.sky] and read by [Modifier.cloudy].
   *
   * This is `null` initially and when the sky modifier is detached.
   */
  internal var backgroundLayer: GraphicsLayer? by mutableStateOf(null)

  /**
   * Bounds of the sky container in local coordinates.
   * Used to calculate relative positioning for child composables
   * that apply background blur.
   */
  internal var sourceBounds: Rect by mutableStateOf(Rect.Zero)

  /**
   * Number of [Modifier.sky] recorders of THIS sky that are currently recording the blur source
   * into a [GraphicsLayer]. `> 0` means a capture is in progress for this sky.
   *
   * A backdrop [Modifier.cloudy] overlay is, by design, a descendant of the [Modifier.sky]
   * container, so the sky's capture pass re-enters the overlay's own draw. If the overlay drew
   * its BLUR (which samples [backgroundLayer]) during that capture, the recorded display
   * list of the layer being recorded would contain a reference back to a layer that samples it —
   * a cyclic `RenderNode` graph that makes the platform render thread recurse until the stack
   * overflows (see https://github.com/skydoves/Cloudy/issues/112).
   *
   * The overlay reads [isCapturing] and skips ONLY its blur draw while this sky is being captured,
   * so the blur is absent from the blur source. It still draws its foreground children during the
   * capture (plain content does not reference [backgroundLayer], so no cycle forms); otherwise a
   * `cloudy` surface's foreground — e.g. a glass top bar's title — would vanish whenever a capture
   * pass ran. [Modifier.sky] then draws its subtree to the window in a second pass with the counter
   * back at zero, during which the overlay paints its blurred backdrop (sampling the now-blur-free
   * [backgroundLayer]) plus its foreground straight to the window canvas. The blur layer is
   * therefore never recorded into a capture layer, so no cycle can form.
   *
   * A COUNTER, not a boolean: the same hoisted [Sky] can be applied to [Modifier.sky] at more than
   * one nesting level at once (e.g. an outer screen container and an inner list container both
   * recording the same sky). Those recorders nest, so the inner recorder's draw runs while the
   * outer one is still capturing. A boolean cleared in the inner recorder's `finally` would expose
   * the overlay during the still-active outer capture and re-form the cycle; a depth counter stays
   * `> 0` until the outermost recorder of this sky finishes.
   *
   * Scoped to this [Sky] instance (not process-global): an overlay of a *different*, independent
   * sky is never suppressed by this sky's capture, so concurrent independent skys (e.g. two screens
   * mid-navigation) keep blurring with no false positives.
   *
   * Plain (non-snapshot) [Int]: capture and the nested draws it triggers run synchronously on the
   * same draw pass on one thread, so no recomposition or cross-thread visibility is needed.
   */
  internal var captureDepth: Int = 0

  /** `true` while any [Modifier.sky] recorder of this sky is recording. @see captureDepth */
  internal val isCapturing: Boolean
    get() = captureDepth > 0

  /** Runs [block] with this sky marked as capturing; supports nested recorders of the same sky. */
  internal inline fun <T> capturing(block: () -> T): T {
    captureDepth++
    try {
      return block()
    } finally {
      captureDepth--
    }
  }

  /**
   * Content version counter that increments every time the background
   * content is re-captured. Used by child modifiers to detect when
   * cached blur results should be invalidated.
   *
   * This counter enables proper cache invalidation during scrolling
   * on devices that use bitmap-based blur (API < 31).
   */
  internal var contentVersion: Long by mutableStateOf(0L)
    private set

  /**
   * Increments the content version, signaling that the background
   * content has changed. Called by [Modifier.sky] after capturing.
   */
  internal fun incrementContentVersion() {
    contentVersion++
  }

  /**
   * Invalidates the captured background content.
   *
   * Call this method when the background content changes and needs
   * to be re-captured for blur rendering. This increments [contentVersion],
   * which triggers dependent [Modifier.cloudy] modifiers to invalidate
   * their cached blur results.
   *
   * ## Example
   *
   * ```kotlin
   * val sky = rememberSky()
   * var imageUrl by remember { mutableStateOf("image1.jpg") }
   *
   * Box(modifier = Modifier.sky(sky)) {
   *   AsyncImage(
   *     model = imageUrl,
   *     onSuccess = { sky.invalidate() } // Re-capture when image loads
   *   )
   *
   *   Card(modifier = Modifier.cloudy(sky = sky, radius = 20)) {
   *     Text("Glass Card")
   *   }
   * }
   * ```
   */
  public fun invalidate() {
    incrementContentVersion()
  }
}

/**
 * Creates and remembers a [Sky] instance for background blur functionality.
 *
 * This function should be called at the top of your composable hierarchy
 * where you want to enable backdrop blur effects.
 *
 * ## Example
 *
 * ```kotlin
 * @Composable
 * fun GlassmorphismScreen() {
 *   val sky = rememberSky()
 *
 *   Box(modifier = Modifier.sky(sky)) {
 *     BackgroundImage()
 *     GlassCard(modifier = Modifier.cloudy(sky = sky, radius = 20))
 *   }
 * }
 * ```
 *
 * @return A remembered [Sky] instance that persists across recompositions.
 * @see Sky
 * @see Modifier.sky
 * @see Modifier.cloudy
 */
@Composable
public fun rememberSky(): Sky = remember { Sky() }
