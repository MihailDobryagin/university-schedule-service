package domain.telegram

import domain.Schedule.Class
import java.time.LocalDate

interface ClassesMessageComposer {
  fun compose(date: LocalDate, classes: List<Class>?): String
  fun composeForClass(lesson: Class): String
}
