package domain.telegram

import domain.Schedule
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

interface ScheduleKeyboardComposer {
  fun compose(schedule: Schedule?, year: Int, month: Int): InlineKeyboardMarkup
}
