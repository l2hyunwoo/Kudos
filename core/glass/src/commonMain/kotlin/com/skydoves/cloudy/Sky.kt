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

import androidx.compose.runtime.Stable
import androidx.compose.runtime.Composable
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
   *
   * Published as a plain (non-snapshot) reference: an overlay re-reads the current layer every
   * time it draws (the recorder always records into the SAME instance per recorder), and the
   * per-frame refresh is driven by [SkyFrameDriver], not by a snapshot read. Writing this as
   * snapshot state during the recorder's draw is what created the original idle redraw loop —
   * the descendant overlay read the state, got invalidated, and forced another frame forever.
   */
  internal var backgroundLayer: GraphicsLayer? = null

  /**
   * Bounds of the sky container in local coordinates. Updated from [Modifier.sky]'s
   * `onGloballyPositioned`. Plain field: read during the overlay's draw, which the frame driver
   * already re-runs each frame, so no snapshot observation is needed.
   */
  internal var sourceBounds: Rect = Rect.Zero

  /**
   * `true` while THIS sky's single [Modifier.sky] recorder is recording the blur source into a
   * [GraphicsLayer].
   *
   * A backdrop [Modifier.cloudy] overlay is, by design, a descendant of the [Modifier.sky]
   * container, so the sky's capture pass re-enters the overlay's own draw. The overlay reads
   * [isCapturing] and draws NOTHING during the capture, so it is fully absent from the blur source:
   * if it drew its BLUR (which samples [backgroundLayer]) into the layer being recorded, that layer
   * would reference a layer that samples it — a cyclic `RenderNode` graph that overflows the render
   * thread stack (https://github.com/skydoves/Cloudy/issues/112). [Modifier.sky] then draws its
   * subtree to the window in a second pass with the flag back to `false`, during which the overlay
   * paints its blurred backdrop straight to the window canvas (its foreground lives OUTSIDE the
   * recorder — see [Modifier.sky]).
   *
   * A boolean, not a counter: a [Sky] drives exactly ONE [Modifier.sky] recorder — the single
   * backdrop-recording container per screen. Nesting two recorders of the same sky is unsupported
   * (it would also fold an inner recorder's content into the outer's blur source). Verified on this
   * app: the recorder depth never exceeds 1 across tab swaps and nav push/pop.
   *
   * Scoped to this [Sky] instance (not process-global): an overlay of a *different*, independent
   * sky is never suppressed by this sky's capture, so concurrent independent skys (e.g. two screens
   * mid-navigation) keep blurring with no false positives.
   *
   * Plain (non-snapshot) [Boolean]: capture and the nested draws it triggers run synchronously on
   * the same draw pass on one thread, so no recomposition or cross-thread visibility is needed.
   */
  internal var isCapturing: Boolean = false

  /** Runs [block] with this sky marked as capturing. */
  internal inline fun <T> capturing(block: () -> T): T {
    isCapturing = true
    try {
      return block()
    } finally {
      isCapturing = false
    }
  }

  /**
   * Per-frame refresh driver that keeps the blur tracking the backdrop while content moves.
   *
   * Why this exists: a `Modifier.sky` recorder placed on a NON-scrolling container (the common and
   * recommended layout — putting it on the scrolling list itself eats scroll gestures) is never
   * draw-invalidated by the list scrolling underneath it. Its captured [backgroundLayer] would
   * freeze at the pre-scroll content while the list moves, so the glass overlay would blur a stale
   * backdrop (or keep a frozen composited blur while sharp rows scroll behind it). The driver
   * invalidates the recorder + overlays each frame so the capture and the blur stay current.
   *
   * It runs ONLY while a window frame is being produced for some other reason (scroll, animation):
   * it parks on [withFrameNanos] and stops requesting frames once the backdrop settles, so the app
   * still goes fully idle (zero frames) when untouched. @see SkyFrameDriver.
   */
  internal val frameDriver: SkyFrameDriver = SkyFrameDriver()

  /**
   * Content version counter that increments every time [invalidate] (or the legacy capture path)
   * signals a background change. Used by the API < 31 bitmap blur to key its cache.
   *
   * NOT bumped per-draw anymore: an unconditional per-draw bump wrote snapshot state read during
   * the overlay's draw, re-invalidating it and self-perpetuating the idle redraw loop. The frame
   * driver now drives per-frame refresh, and this counter only marks discrete, explicit changes.
   */
  internal var contentVersion: Long by mutableStateOf(0L)
    private set

  /** Increments the content version, signaling a discrete background-content change. */
  internal fun incrementContentVersion() {
    contentVersion++
  }

  /**
   * Invalidates the captured background content.
   *
   * Call this method when the background content changes and needs
   * to be re-captured for blur rendering. This increments [contentVersion],
   * which triggers dependent [Modifier.cloudy] modifiers to invalidate
   * their cached blur results, and requests a refresh frame from the driver.
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
    frameDriver.requestRefresh()
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
