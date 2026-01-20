package utils

import java.util.zip.CRC32

object StringUtils {
  val String.checkSum: Long
    get() = CRC32().apply { update(encodeToByteArray()) }.value
}
