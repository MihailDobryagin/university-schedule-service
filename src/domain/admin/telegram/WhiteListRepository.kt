package domain.admin.telegram

interface WhiteListRepository {
  suspend fun addToWhiteList(username: String)
  suspend fun whitelistUsers(): Set<String>
}
