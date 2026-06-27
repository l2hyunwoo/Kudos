package io.github.l2hyunwoo.tasks

import androidx.compose.runtime.Immutable
import io.github.l2hyunwoo.data.tasks.model.Task
import io.github.l2hyunwoo.data.tasks.model.TaskStatus
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

// Today's local civil date as "YYYY-MM-DD", derived from epoch days via Howard Hinnant's
// civil_from_days so commonMain needs no datetime dependency (mirrors MainScreen.todayLabel()).
// Uses the device's UTC-based day, which is acceptable for due-date bucketing here.
fun todayIso(): String {
    val epochDays = Clock.System.now().epochSeconds.floorDiv(86_400L)
    val z = epochDays + 719_468L
    val era = (if (z >= 0) z else z - 146_096L) / 146_097L
    val doe = z - era * 146_097L
    val yoe = (doe - doe / 1460L + doe / 36_524L - doe / 146_096L) / 365L
    val year = yoe + era * 400L
    val doy = doe - (365L * yoe + yoe / 4L - yoe / 100L)
    val mp = (5L * doy + 2L) / 153L
    val day = (doy - (153L * mp + 2L) / 5L + 1L).toInt()
    val month = (if (mp < 10L) mp + 3L else mp - 9L).toInt()
    val civilYear = (if (month <= 2) year + 1L else year).toInt()
    return "${pad4(civilYear)}-${pad2(month)}-${pad2(day)}"
}

private fun pad2(v: Int): String = if (v < 10) "0$v" else v.toString()

private fun pad4(v: Int): String = v.toString().padStart(4, '0')
