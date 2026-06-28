package io.github.l2hyunwoo.tasks

import androidx.compose.runtime.Immutable
import io.github.l2hyunwoo.data.tasks.model.Task
import io.github.l2hyunwoo.data.tasks.model.TaskStatus
import io.github.l2hyunwoo.kudos.core.common.date.isoFromEpochDay
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Clock

// Time-based grouping for the Home (Tasks) tab (DESIGN_SYSTEM_LUNAR 07-A, PRODUCT_SPEC B-5).
// Overdue leads (warning red), then Today, Upcoming, tasks without a due date, and finally completed.
enum class TaskGroupKind {
    OVERDUE,
    TODAY,
    UPCOMING,
    NO_DUE,
    DONE,
}

@Immutable
data class TaskGroup(
    val kind: TaskGroupKind,
    val tasks: ImmutableList<Task>,
)

// Fixed display order; only non-empty groups are emitted (Overdue first per the spec).
private val GroupOrder = listOf(
    TaskGroupKind.OVERDUE,
    TaskGroupKind.TODAY,
    TaskGroupKind.UPCOMING,
    TaskGroupKind.NO_DUE,
    TaskGroupKind.DONE,
)

// Buckets a flat task list by due date relative to [today] (a "YYYY-MM-DD" string).
// dueDate is a free-text ISO-8601 string (date-only "YYYY-MM-DD" or a full timestamp), so we compare
// the first 10 chars lexicographically — that prefix sorts chronologically for both forms and needs
// no date parsing / datetime dependency.
//
// Rules:
//  - DONE tasks go to their own trailing group (rendered dimmed by TaskRow), so completed-but-overdue
//    work never nags in the Overdue bucket.
//  - No due date → NO_DUE (trailing, before DONE).
//  - dueDate < today → OVERDUE, == today → TODAY, > today → UPCOMING.
fun groupTasksByDueDate(
    tasks: List<Task>,
    today: String,
): ImmutableList<TaskGroup> {
    val byKind = LinkedHashMap<TaskGroupKind, MutableList<Task>>()
    for (task in tasks) {
        // Bind to a local: dueDate is declared in another module, so smart-cast from the null check
        // wouldn't carry into the comparison.
        val due = task.dueDate
        val kind = when {
            task.status == TaskStatus.DONE -> TaskGroupKind.DONE
            due.isNullOrBlank() -> TaskGroupKind.NO_DUE
            else -> {
                val day = due.dueDay()
                when {
                    day < today -> TaskGroupKind.OVERDUE
                    day == today -> TaskGroupKind.TODAY
                    else -> TaskGroupKind.UPCOMING
                }
            }
        }
        byKind.getOrPut(kind) { mutableListOf() }.add(task)
    }
    return GroupOrder.mapNotNull { kind ->
        byKind[kind]?.let { bucket ->
            // Sort every non-DONE bucket by priority (URGENT=ordinal 0 first), tie-broken by
            // taskNumber so the order is deterministic and stable across recompositions. DONE keeps
            // input order so completed work doesn't reshuffle by urgency it no longer carries.
            val ordered = if (kind == TaskGroupKind.DONE) {
                bucket
            } else {
                bucket.sortedWith(
                    compareBy({ it.priority.ordinal }, { it.taskNumber }),
                )
            }
            TaskGroup(kind, ordered.toImmutableList())
        }
    }.toImmutableList()
}

// The "YYYY-MM-DD" date portion of an ISO-8601 string: take the first 10 chars when long enough,
// else the whole string (already date-only or otherwise short).
private fun String.dueDay(): String = if (length >= 10) substring(0, 10) else this

// Today's local civil date as "YYYY-MM-DD". Uses the device's UTC-based day, which is acceptable for
// due-date bucketing here. The epoch-day -> ISO conversion lives in core.common.date.
fun todayIso(): String = isoFromEpochDay(Clock.System.now().epochSeconds.floorDiv(86_400L))
