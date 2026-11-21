/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.dao;

import cl.ucn.disc.arqsist.library.model.Member;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.List;

/**
 * Data access object for {@link Member} entities.
 * All CRUD operations and the transaction helper are inherited from {@link BaseDao}.
 */
public final class MemberDao extends BaseDao<Member> {

    /**
     * Creates the DAO for {@link Member}.
     *
     * @param connectionSource the connection source used to create the underlying ORMLite DAO.
     */
    public MemberDao(ConnectionSource connectionSource) {
        super(connectionSource, Member.class);
    }

    /**
     * Finds the members whose name contains the given text. The match is a literal substring
     * match, so {@code %} and {@code _} have no special meaning, and it ignores the case of
     * ASCII letters. The text is passed as a query argument, never joined into the SQL.
     *
     * @param query the text to look for, already trimmed and not blank.
     * @return the matching members ordered by id, ascending. The list is empty if none match.
     * @throws RuntimeException if the query fails.
     */
    public List<Member> findByNameContaining(String query) {
        try {
            return dao.queryBuilder()
                    .orderBy("id", true)
                    .where()
                    .raw("instr(lower(name), lower(?)) > 0", new SelectArg(SqlType.STRING, query))
                    .query();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
