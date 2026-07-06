package no.nav.hm.finnhjelpemiddelnews.media

import io.micronaut.serde.annotation.Serdeable
import java.time.LocalDateTime
import java.util.*

@Serdeable
data class MediaDTO(
    val oid: UUID,
    val uri: String,
    val sourceUri: String,
    val filename: String? = null,
    val type: MediaType,
    val size: Long,
    val md5: String,
    val status: String,
    val source: MediaSourceType,
    val objectType: ObjectType? = null,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now(),
)

enum class MediaType { IMAGE, OTHER }
enum class MediaSourceType { EXTERNALURL, UPLOAD, REGISTER, IMPORT }
