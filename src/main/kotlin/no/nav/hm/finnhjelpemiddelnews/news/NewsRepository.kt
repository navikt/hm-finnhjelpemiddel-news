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
        value = "SELECT news_.* FROM news news_ WHERE news_.id IN (:ids) AND (news_.title ILIKE :search OR news_.description ILIKE :search) AND (:active = false OR (news_.published_from <= NOW() AND news_.published_to >= NOW() AND news_.status = 'PUBLISHED'))",
        countQuery = "SELECT COUNT(*) FROM news WHERE id IN (:ids) AND (title ILIKE :search OR description ILIKE :search) AND (:active = false OR (published_from <= NOW() AND published_to >= NOW() AND status = 'PUBLISHED'))"
    )
    suspend fun searchAllByIds(ids: List<UUID>, search: String, pageable: Pageable, active: Boolean): Page<News>


    @Query(
        value = "SELECT news_.* FROM news news_ WHERE news_.id in (:ids) AND (:active = false OR (news_.published_from <= NOW() AND news_.published_to >= NOW() AND news_.status = 'PUBLISHED'))",
        countQuery = "SELECT COUNT(*) FROM news WHERE id IN (:ids) AND (:active = false OR (published_from <= NOW() AND published_to >= NOW() AND status = 'PUBLISHED'))"
    )
    suspend fun findAllByIds(ids: List<UUID>, pageable: Pageable, active: Boolean): Page<News>

    @Query(
        value = "SELECT news_.* FROM news news_ WHERE (news_.title ILIKE :search OR news_.description ILIKE :search) AND (:active = false OR (news_.published_from <= NOW() AND news_.published_to >= NOW() AND news_.status = 'PUBLISHED'))",
        countQuery = "SELECT COUNT(*) FROM news WHERE (title ILIKE :search OR description ILIKE :search) AND (:active = false OR (published_from <= NOW() AND published_to >= NOW() AND status = 'PUBLISHED'))"
    )
    suspend fun searchAll(search: String, pageable: Pageable, active: Boolean): Page<News>

    @Query(
        value = "SELECT news_.* FROM news news_ WHERE (:active = false OR (news_.published_from <= NOW() AND news_.published_to >= NOW() AND news_.status = 'PUBLISHED'))",
        countQuery = "SELECT COUNT(*) FROM news WHERE (:active = false OR (published_from <= NOW() AND published_to >= NOW() and status = 'PUBLISHED'))"
    )
    suspend fun findAllPaged(pageable: Pageable, active: Boolean): Page<News>
}
