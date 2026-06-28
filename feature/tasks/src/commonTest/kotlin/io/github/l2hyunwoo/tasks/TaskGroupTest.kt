package io.github.l2hyunwoo.tasks

import io.github.l2hyunwoo.data.tasks.model.Task
import io.github.l2hyunwoo.data.tasks.model.TaskPriority
import io.github.l2hyunwoo.data.tasks.model.TaskStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class TaskGroupTest {
    private fun task(
        id: String,
        number: Int,
        priority: TaskPriority,
        status: TaskStatus = TaskStatus.TODO,
        dueDate: String? = null,
    ) = Task(
        id = id,
        taskId = "KUDOS-$number",
        taskNumber = number,
        title = id,
        status = status,
        priority = priority,
        dueDate = dueDate,
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
    )

    @Test
    fun groupsAreOrderedByPriorityWithUrgentFirst() {
        val today = "2024-06-15"
        val tasks =
            listOf(
                task("a", 1, TaskPriority.LOW, dueDate = today),
                task("b", 2, TaskPriority.URGENT, dueDate = today),
                task("c", 3, TaskPriority.MEDIUM, dueDate = today),
                task("d", 4, TaskPriority.HIGH, dueDate = today),
            )

        val groups = groupTasksByDueDate(tasks, today)
        val todayGroup = groups.single { it.kind == TaskGroupKind.TODAY }

        assertEquals(listOf("b", "d", "c", "a"), todayGroup.tasks.map { it.id })
    }

    @Test
    fun samePriorityIsTieBrokenByTaskNumber() {
        val today = "2024-06-15"
        val tasks =
            listOf(
                task("late", 30, TaskPriority.HIGH, dueDate = today),
                task("early", 10, TaskPriority.HIGH, dueDate = today),
                task("mid", 20, TaskPriority.HIGH, dueDate = today),
            )

        val groups = groupTasksByDueDate(tasks, today)
        val todayGroup = groups.single { it.kind == TaskGroupKind.TODAY }

        assertEquals(listOf("early", "mid", "late"), todayGroup.tasks.map { it.id })
    }

    @Test
    fun doneGroupKeepsInputOrderNotPrioritySorted() {
        val tasks =
            listOf(
                task("d1", 1, TaskPriority.LOW, status = TaskStatus.DONE),
                task("d2", 2, TaskPriority.URGENT, status = TaskStatus.DONE),
                task("d3", 3, TaskPriority.MEDIUM, status = TaskStatus.DONE),
            )

        val groups = groupTasksByDueDate(tasks, "2024-06-15")
        val doneGroup = groups.single { it.kind == TaskGroupKind.DONE }

        // DONE must NOT priority-sort: input order is preserved.
        assertEquals(listOf("d1", "d2", "d3"), doneGroup.tasks.map { it.id })
    }

    @Test
    fun prioritySortIsAppliedPerGroupIndependently() {
        val today = "2024-06-15"
        val tasks =
            listOf(
                // OVERDUE bucket
                task("o-low", 1, TaskPriority.LOW, dueDate = "2024-06-10"),
                task("o-urgent", 2, TaskPriority.URGENT, dueDate = "2024-06-10"),
                // NO_DUE bucket
                task("n-medium", 3, TaskPriority.MEDIUM),
                task("n-high", 4, TaskPriority.HIGH),
            )

        val groups = groupTasksByDueDate(tasks, today)

        assertEquals(
            listOf("o-urgent", "o-low"),
            groups.single { it.kind == TaskGroupKind.OVERDUE }.tasks.map { it.id },
        )
        assertEquals(
            listOf("n-high", "n-medium"),
            groups.single { it.kind == TaskGroupKind.NO_DUE }.tasks.map { it.id },
        )
    }
}
