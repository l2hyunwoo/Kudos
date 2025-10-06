package io.github.l2hyunwoo.kudos

import io.github.l2hyunwoo.category.CategoryContext
import io.github.l2hyunwoo.tasks.TasksContext

interface AppGraph : TasksContext.Factory, CategoryContext.Factory
