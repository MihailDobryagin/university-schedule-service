package domain.admin.telegram

class WhiteListRepositoryImpl : WhiteListRepository {
  private val allowedUsers = mutableSetOf<String>()

  override suspend fun addToWhiteList(username: String) {
    allowedUsers.add(username)
  }

  override suspend fun whitelistUsers(): Set<String> = allowedUsers
}
