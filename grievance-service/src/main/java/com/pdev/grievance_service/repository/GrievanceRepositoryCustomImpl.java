package com.pdev.grievance_service.repository;

import com.pdev.grievance_service.dto.GrievanceSearchRequest;
import com.pdev.grievance_service.model.Grievance;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class GrievanceRepositoryCustomImpl
        implements GrievanceRepositoryCustom {

    private final EntityManager entityManager;

    public GrievanceRepositoryCustomImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // -------------------------------------------------------------------------
    //  SEARCH  (paginated)
    // -------------------------------------------------------------------------

    @Override
    public List searchGrievances(GrievanceSearchRequest request, int offset) {

        Map<String, String> allowedSortFields = Map.of(
                "createdAt", "g.submitted_at",
                "updatedAt", "g.updated_at",
                "status",    "g.status",
                "title",     "g.title"
        );

        String sortColumn = allowedSortFields.getOrDefault(
                request.getSortBy(), "g.submitted_at"
        );
        String direction = "asc".equalsIgnoreCase(request.getSortDir()) ? "ASC" : "DESC";

        // PostgreSQL-safe nullable-param pattern:
        //   Each param is bound as a plain string via JDBC.
        //   Inside SQL we use  (:param)::type  (PostgreSQL cast-suffix) so
        //   Postgres resolves the type even when the value is NULL.
        //   Every condition is fully wrapped in its own ( ... AND/OR ... )
        //   parentheses to avoid SQL operator-precedence bugs.
        String sql = ("""
                SELECT g.*
                FROM grievances g
                WHERE
                    ((:citizenId)::text IS NULL OR g.citizen_id = (:citizenId)::text)

                    AND ((:status)::text IS NULL OR CAST(g.status AS TEXT) = (:status)::text)

                    AND ((:search)::text IS NULL
                         OR LOWER(g.title) LIKE LOWER(CONCAT('%%', (:search)::text, '%%'))
                         OR LOWER(g.description) LIKE LOWER(CONCAT('%%', (:search)::text, '%%')))

                    AND ((:fromDate)::timestamp IS NULL OR g.submitted_at >= (:fromDate)::timestamp)

                    AND ((:toDate)::timestamp IS NULL OR g.submitted_at <= (:toDate)::timestamp)

                    AND ((:priority)::text IS NULL OR CAST(g.priority AS TEXT) = (:priority)::text)

                ORDER BY %s %s
                LIMIT :size
                OFFSET :offset
                """).formatted(sortColumn, direction);

        Query query = entityManager.createNativeQuery(sql, Grievance.class);

        bindParams(query, request);
        query.setParameter("size",   request.getSize());
        query.setParameter("offset", offset);

        return query.getResultList();
    }

    // -------------------------------------------------------------------------
    //  COUNT  (same WHERE clause, no LIMIT/OFFSET)
    // -------------------------------------------------------------------------

    @Override
    public long countGrievances(GrievanceSearchRequest request) {

        String sql = """
                SELECT COUNT(*)
                FROM grievances g
                WHERE
                    ((:citizenId)::text IS NULL OR g.citizen_id = (:citizenId)::text)

                    AND ((:status)::text IS NULL OR CAST(g.status AS TEXT) = (:status)::text)

                    AND ((:search)::text IS NULL
                         OR LOWER(g.title) LIKE LOWER(CONCAT('%%', (:search)::text, '%%'))
                         OR LOWER(g.description) LIKE LOWER(CONCAT('%%', (:search)::text, '%%')))

                    AND ((:fromDate)::timestamp IS NULL OR g.submitted_at >= (:fromDate)::timestamp)

                    AND ((:toDate)::timestamp IS NULL OR g.submitted_at <= (:toDate)::timestamp)

                    AND ((:priority)::text IS NULL OR CAST(g.priority AS TEXT) = (:priority)::text)
                """;

        Query query = entityManager.createNativeQuery(sql);
        bindParams(query, request);

        Number count = (Number) query.getSingleResult();
        return count.longValue();
    }

    // -------------------------------------------------------------------------
    //  Shared param binding
    // -------------------------------------------------------------------------

    /**
     * Binds all filter parameters for both the search and count queries.
     * Dates are passed as strings so the JDBC driver never sends a typeless
     * NULL for a temporal type — PostgreSQL resolves the type from the ::timestamp cast.
     */
    private void bindParams(Query query, GrievanceSearchRequest request) {
        query.setParameter("citizenId", request.getCitizenId());
        query.setParameter("status",    request.getStatus() != null
                ? request.getStatus().name() : null);
        query.setParameter("search",    request.getSearch());
        query.setParameter("fromDate",  request.getFromDate() != null
                ? request.getFromDate().toString() : null);
        query.setParameter("toDate",    request.getToDate() != null
                ? request.getToDate().toString() : null);
        query.setParameter("priority",  request.getPriority() != null
                ? request.getPriority().name() : null);
    }
}