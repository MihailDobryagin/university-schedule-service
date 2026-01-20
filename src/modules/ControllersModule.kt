package modules

import controllers.HealthController
import controllers.HealthControllerImpl
import controllers.ManageController
import controllers.ManageControllerImpl
import controllers.schedules.ScheduleController
import controllers.schedules.ScheduleControllerImpl
import org.koin.dsl.module

val ControllersModule = module {
  single<HealthController> { HealthControllerImpl() }
  single<ManageController> { ManageControllerImpl(get()) }
  single<ScheduleController> { ScheduleControllerImpl(get()) }
}
