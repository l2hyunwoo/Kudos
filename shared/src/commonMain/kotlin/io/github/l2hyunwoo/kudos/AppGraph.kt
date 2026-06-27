package io.github.l2hyunwoo.kudos

import io.github.l2hyunwoo.category.CategoryContext
import io.github.l2hyunwoo.project.ProjectContext
import io.github.l2hyunwoo.tasks.TasksContext
import io.github.l2hyunwoo.tasks.detail.TaskDetailContext

interface AppGraph :
    TasksContext.Factory,
    CategoryContext.Factory,
    ProjectContext.Factory,
    TaskDetailContext.Factory
