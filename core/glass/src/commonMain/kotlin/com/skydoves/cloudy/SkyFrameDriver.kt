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

import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Keeps a [Sky]'s captured backdrop and its [Modifier.cloudy] overlays refreshing WHILE content
 * behind the glass is scrolling, then parks so the app reaches zero frames at idle.
 *
 * ## Why a driver is needed
 *
 * A `Modifier.sky` recorder re-records the backdrop only when its own `draw()` runs, and an overlay
 * re-blurs only when its draw runs. A `LazyColumn` scroll does NOT draw-invalidate either node (the
 * list repaints its own items, but the recorder's and overlays' `draw()` are not re-invoked —
 * verified on this app). So the captured backdrop and the composited blur freeze at the pre-scroll
 * content while sharp rows scroll behind the glass — the reported intermittent "blur disappears".
 *
 * ## The scroll signal
 *
 * The recorder modifier delegates to a nested-scroll node and forwards every scroll/fling delta from
 * descendant scrollables to [onScrollActivity]. That is the precise "the backdrop is moving now"
 * signal the draw phase lacks. While scrolling, the driver re-invalidates the recorder (re-capture)
 * and the overlays (re-blur) each frame; a short [COAST_FRAMES] tail keeps refreshing through the
 * very end of a fling so the final settled frame is captured. When scrolling stops the coast counts
 * down and the loop parks.
 *
 * ## Why idle stays at zero frames
 *
 * The loop runs only while [coastFrames] is positive, and the coast is re-armed ONLY by real scroll
 * activity (or an explicit [requestRefresh]) — never by the loop's own invalidations. [withFrameNanos]
 * never posts a frame itself, so once the coast is spent the loop exits and nothing keeps the window
 * producing frames. No scroll, no animation → zero frames.
 */
internal class SkyFrameDriver {

  private val overlays = mutableListOf<() -> Unit>()

  private var recorderScope: CoroutineScope? = null
  private var recorderInvalidate: (() -> Unit)? = null
  private var pumpJob: Job? = null

  // Frames the loop should still run. Re-armed by scroll activity; counted down by the loop.
  private var coastFrames: Int = 0

  private companion object {
    // Tail of frames to keep refreshing after the last scroll delta, so the settled post-fling frame
    // is captured. Small, so the app idles within ~tens of ms of the scroll stopping.
    const val COAST_FRAMES = 4
  }

  /** Registers the `Modifier.sky` recorder: [scope] hosts the loop, [invalidate] forces a capture. */
  fun attachRecorder(scope: CoroutineScope, invalidate: () -> Unit) {
    recorderScope = scope
    recorderInvalidate = invalidate
  }

  fun detachRecorder(scope: CoroutineScope) {
    if (recorderScope === scope) {
      pumpJob?.cancel()
      pumpJob = null
      recorderScope = null
      recorderInvalidate = null
      coastFrames = 0
    }
  }

  /** Registers an overlay's re-blur invalidator. */
  fun addOverlay(invalidate: () -> Unit) {
    if (overlays.none { it === invalidate }) overlays += invalidate
  }

  fun removeOverlay(invalidate: () -> Unit) {
    overlays.removeAll { it === invalidate }
    if (overlays.isEmpty()) {
      pumpJob?.cancel()
      pumpJob = null
      coastFrames = 0
    }
  }

  /**
   * Reports scroll/fling activity behind the glass (forwarded from the recorder's nested-scroll
   * connection). Re-arms the refresh coast and starts the loop if it had parked.
   */
  fun onScrollActivity() {
    coastFrames = COAST_FRAMES
    ensurePump()
  }

  /** Explicit one-off refresh ([Sky.invalidate]). */
  fun requestRefresh() {
    coastFrames = COAST_FRAMES
    ensurePump()
  }

  private fun ensurePump() {
    if (pumpJob?.isActive == true) return
    if (overlays.isEmpty()) return
    val scope = recorderScope ?: return
    pumpJob = scope.launch {
      try {
        while (isActive && coastFrames > 0) {
          coastFrames--
          // Re-capture the moved backdrop, then re-blur the overlays against it, in this frame.
          recorderInvalidate?.invoke()
          for (i in overlays.indices) overlays[i]()
          // Parks for free at idle; resumes on the frame our invalidations just scheduled. When the
          // coast is spent (scroll stopped COAST_FRAMES ago) the loop exits and schedules no more.
          withFrameNanos { }
        }
      } finally {
        pumpJob = null
      }
    }
  }
}
