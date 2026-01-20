package domain

import domain.Schedule.Class
import java.time.LocalDate

interface NotificationService {
  suspend fun notifyUpcomingClasses(classesDate: LocalDate, classes: List<Class>)
  suspend fun notifyClass(lesson: Class)
}
