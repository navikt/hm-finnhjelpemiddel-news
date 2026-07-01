package no.nav.hm.finnhjelpemiddelnews.news

import io.micronaut.data.annotation.Query
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

    @Query(
        value = "SELECT news_.* FROM news news_ WHERE news_.published_from <= NOW() AND news_.published_to >= NOW()",
        countQuery = "SELECT COUNT(*) FROM news WHERE published_from <= NOW() AND published_to >= NOW()"
    )
    suspend fun findAllPublished(pageable: Pageable): Page<News>

    @Query(
        value = "SELECT news_.* FROM news news_ WHERE (news_.title ILIKE :search OR news_.description ILIKE :search) AND news_.published_from <= NOW() AND news_.published_to >= NOW()",
        countQuery = "SELECT COUNT(*) FROM news WHERE (title ILIKE :search OR description ILIKE :search) AND published_from <= NOW() AND published_to >= NOW()"
    )
    suspend fun searchPublished(search: String, pageable: Pageable): Page<News>

    @Query(
        value = "SELECT news_.* FROM news news_ WHERE news_.id IN (:ids) AND news_.published_from <= NOW() AND news_.published_to >= NOW()",
        countQuery = "SELECT COUNT(*) FROM news WHERE id IN (:ids) AND published_from <= NOW() AND published_to >= NOW()"
    )
    suspend fun findPublishedByIds(ids: List<UUID>, pageable: Pageable): Page<News>

    @Query(
        value = "SELECT news_.* FROM news news_ WHERE news_.id IN (:ids) AND (news_.title ILIKE :search OR news_.description ILIKE :search) AND news_.published_from <= NOW() AND news_.published_to >= NOW()",
        countQuery = "SELECT COUNT(*) FROM news WHERE id IN (:ids) AND (title ILIKE :search OR description ILIKE :search) AND published_from <= NOW() AND published_to >= NOW()"
    )
    suspend fun searchPublishedByIds(ids: List<UUID>, search: String, pageable: Pageable): Page<News>
}
