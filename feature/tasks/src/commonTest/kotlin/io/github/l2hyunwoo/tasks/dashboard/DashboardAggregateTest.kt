package io.github.l2hyunwoo.tasks.dashboard

import io.github.l2hyunwoo.data.tasks.model.Task
import io.github.l2hyunwoo.data.tasks.model.TaskPriority
import io.github.l2hyunwoo.data.tasks.model.TaskStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DashboardAggregateTest {
    private fun task(
        id: String,
        status: TaskStatus,
        priority: TaskPriority,
    ) = Task(
        id = id,
        taskId = "KUDOS-$id",
        taskNumber = id.hashCode(),
        title = id,
        status = status,
        priority = priority,
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
    )

    private val fixture =
        listOf(
            task("1", TaskStatus.BACKLOG, TaskPriority.LOW),
            task("2", TaskStatus.TODO, TaskPriority.HIGH),
            task("3", TaskStatus.TODO, TaskPriority.MEDIUM),
            task("4", TaskStatus.IN_PROGRESS, TaskPriority.URGENT),
            task("5", TaskStatus.DONE, TaskPriority.HIGH),
            task("6", TaskStatus.DONE, TaskPriority.MEDIUM),
        )

    @Test
    fun totalCountIsTaskCount() {
        assertEquals(6, aggregate(fixture).totalCount)
    }

    @Test
    fun completionRatioIsDoneOverTotal() {
        // 2 DONE of 6 = 1/3.
        assertEquals(2f / 6f, aggregate(fixture).completionRatio)
    }

    @Test
    fun statusCountsBucketEveryStatusIncludingZero() {
        val counts = aggregate(fixture).statusCounts
        assertEquals(1, counts[TaskStatus.BACKLOG])
        assertEquals(2, counts[TaskStatus.TODO])
        assertEquals(1, counts[TaskStatus.IN_PROGRESS])
        assertEquals(2, counts[TaskStatus.DONE])
        // Every enum key is present even when its count is non-zero here; assert completeness.
        assertEquals(TaskStatus.entries.size, counts.size)
    }

    @Test
    fun priorityCountsBucketEveryPriority() {
        val counts = aggregate(fixture).priorityCounts
        assertEquals(1, counts[TaskPriority.URGENT])
        assertEquals(2, counts[TaskPriority.HIGH])
        assertEquals(2, counts[TaskPriority.MEDIUM])
        assertEquals(1, counts[TaskPriority.LOW])
        assertEquals(TaskPriority.entries.size, counts.size)
    }

    @Test
    fun statusCountsSumToTotal() {
        val state = aggregate(fixture)
        assertEquals(state.totalCount, state.statusCounts.values.sum())
    }

    @Test
    fun doneCountReadsDoneBucket() {
        assertEquals(2, aggregate(fixture).doneCount)
    }

    @Test
    fun emptyListYieldsZeroStateWithAllKeysPresent() {
        val state = aggregate(emptyList())
        assertEquals(0, state.totalCount)
        assertEquals(0f, state.completionRatio)
        assertEquals(0, state.doneCount)
        // Empty case still carries a well-formed shape: every enum key present at 0.
        assertEquals(TaskStatus.entries.size, state.statusCounts.size)
        assertEquals(TaskPriority.entries.size, state.priorityCounts.size)
        assertTrue(state.statusCounts.values.all { it == 0 })
        assertTrue(state.priorityCounts.values.all { it == 0 })
    }

    @Test
    fun allDoneIsFullyComplete() {
        val tasks =
            listOf(
                task("a", TaskStatus.DONE, TaskPriority.LOW),
                task("b", TaskStatus.DONE, TaskPriority.HIGH),
            )
        assertEquals(1f, aggregate(tasks).completionRatio)
    }
}
