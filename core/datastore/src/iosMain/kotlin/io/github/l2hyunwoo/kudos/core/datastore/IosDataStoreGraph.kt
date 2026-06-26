package io.github.l2hyunwoo.kudos.core.datastore

import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.l2hyunwoo.kudos.core.common.DataScope
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@ContributesTo(DataScope::class)
interface IosDataStoreGraph {
    @OptIn(ExperimentalForeignApi::class)
    @Provides
    fun provideDataStorePathProducer(): DataStorePathProducer {
        return DataStorePathProducer { fileName ->
            val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null,
            )
            requireNotNull(documentDirectory).path + "/$fileName"
        }
    }
}
