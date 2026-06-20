package io.github.l2hyunwoo.kudos

import android.app.Application
import android.content.Context
import dev.zacsweers.metro.createGraphFactory

class KudosApp : Application() {
    val appGraph: AppGraph by lazy {
        createGraphFactory<AndroidAppGraph.Factory>()
            .createAndroidAppGraph(applicationContext = this)
    }
}

val Context.kudosAppGraph: AppGraph get() = (applicationContext as KudosApp).appGraph
