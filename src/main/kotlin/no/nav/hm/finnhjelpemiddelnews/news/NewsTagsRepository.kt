package no.nav.hm.finnhjelpemiddelnews.news

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

@JdbcRepository(dialect = Dialect.POSTGRES)
interface NewsTagsRepository : CoroutineCrudRepository<NewsTags, NewsTagsId> {
    suspend fun findByIdNewsId(newsId: UUID): List<NewsTags>
    suspend fun findByIdNewsIdIn(newsIds: List<UUID>): List<NewsTags>
    suspend fun deleteByIdNewsId(newsId: UUID)
}
