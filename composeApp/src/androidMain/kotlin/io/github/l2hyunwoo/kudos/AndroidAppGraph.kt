package io.github.l2hyunwoo.kudos

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides

@DependencyGraph(
    scope = AppScope::class,
    additionalScopes = [],
)
interface AndroidAppGraph: AppGraph {
    @DependencyGraph.Factory
    fun interface Factory {
        fun createAndroidAppGraph(
            @Provides applicationContext: Context,
        ):AndroidAppGraph
    }
}