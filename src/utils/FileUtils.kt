package utils

import java.io.File

object FileUtils {
  fun file(absolutePath: String) = File(absolutePath).apply {
    val correctConfiguration = exists() && canRead() && canWrite()
    if (!correctConfiguration) throw IllegalStateException("Invalid file")
  }
}
