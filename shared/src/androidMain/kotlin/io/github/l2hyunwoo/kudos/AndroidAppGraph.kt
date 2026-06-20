package io.github.l2hyunwoo.kudos

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import io.github.l2hyunwoo.kudos.core.common.DataScope

@DependencyGraph(
    scope = AppScope::class,
    additionalScopes = [DataScope::class],
)
interface AndroidAppGraph : AppGraph {
    @DependencyGraph.Factory
    fun interface Factory {
        fun createAndroidAppGraph(
            @Provides applicationContext: Context,
        ): AndroidAppGraph
    }
}
