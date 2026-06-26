package io.github.l2hyunwoo.kudos

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import io.github.l2hyunwoo.kudos.core.common.DataScope
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExportObjCClass

/**
 * The iOS dependency graph. Mirrors [AndroidAppGraph]: the data layer is wired by
 * cross-module @ContributesBinding aggregation, and the platform-specific providers
 * (Darwin Ktor engine, NSFileManager-based DataStore path) are contributed by the
 * iosMain @ContributesTo(DataScope) modules (IosNetworkGraph, IosDataStoreGraph),
 * analogous to AndroidNetworkGraph / AndroidDataStoreGraph.
 */
@OptIn(BetaInteropApi::class)
@ExportObjCClass
@DependencyGraph(
    scope = AppScope::class,
    additionalScopes = [DataScope::class],
)
interface IosAppGraph : AppGraph {

    @DependencyGraph.Factory
    fun interface Factory {
        fun createIosAppGraph(): IosAppGraph
    }
}
