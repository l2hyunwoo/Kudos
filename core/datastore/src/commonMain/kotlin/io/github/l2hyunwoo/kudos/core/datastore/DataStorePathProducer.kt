package io.github.l2hyunwoo.kudos.core.datastore

fun interface DataStorePathProducer {
    fun producePath(fileName: String): String
}
