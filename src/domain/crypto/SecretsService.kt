package domain.crypto

interface SecretsService {
  suspend fun requestSecrets(): List<String>
}

