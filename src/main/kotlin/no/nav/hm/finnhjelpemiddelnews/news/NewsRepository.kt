package no.nav.hm.finnhjelpemiddelnews.news

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import io.micronaut.data.repository.kotlin.CoroutinePageableCrudRepository
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import java.util.UUID

@JdbcRepository(dialect = Dialect.POSTGRES)
interface NewsRepository : CoroutineCrudRepository<News, UUID>, CoroutinePageableCrudRepository<News, UUID> {
    suspend fun findOne(id: UUID): News
    suspend fun findByTitle(title: String): News
    suspend fun findByIdIn(ids: List<UUID>, pageable: Pageable): Page<News>
    suspend fun findByTitleIlikeOrDescriptionIlike(title: String, description: String, pageable: Pageable): Page<News>
}
