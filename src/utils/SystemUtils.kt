package utils

object SystemUtils {
  fun env(name: String) = System.getenv(name) ?: throw IllegalStateException("No env '$name'")
  fun envOrNull(name: String): String? = System.getenv(name)
}
