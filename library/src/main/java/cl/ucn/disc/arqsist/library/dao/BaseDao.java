/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Shared CRUD code for every DAO. Subclasses only choose the entity class.
 * Persistence errors are reported as unchecked exceptions.
 *
 * @param <T> the entity type handled by the DAO.
 */
public abstract class BaseDao<T> {

    /**
     * The ORMLite DAO that does the real work.
     */
    protected final Dao<T, Integer> dao;

    /**
     * Creates the DAO for one entity class.
     *
     * @param connectionSource the database connection.
     * @param clazz            the entity class.
     * @throws RuntimeException if the underlying DAO cannot be created.
     */
    protected BaseDao(ConnectionSource connectionSource, Class<T> clazz) {
        try {
            this.dao = DaoManager.createDao(connectionSource, clazz);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads every row of the entity table.
     *
     * @return all entities.
     * @throws RuntimeException if the query fails.
     */
    public List<T> findAll() {
        try {
            return dao.queryForAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads one entity by its id.
     *
     * @param id the entity id.
     * @return the entity, or {@code null} if it does not exist.
     * @throws RuntimeException if the query fails.
     */
    public T findById(int id) {
        try {
            return dao.queryForId(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Inserts a new entity.
     *
     * @param entity the entity to insert.
     * @throws RuntimeException if the insert fails.
     */
    public void create(T entity) {
        try {
            dao.create(entity);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Updates an existing entity.
     *
     * @param entity the entity to update.
     * @throws RuntimeException if the update fails.
     */
    public void update(T entity) {
        try {
            dao.update(entity);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes an entity.
     *
     * @param entity the entity to delete.
     * @throws RuntimeException if the delete fails.
     */
    public void delete(T entity) {
        try {
            dao.delete(entity);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Runs the callable in one database transaction. If it fails, every write is
     * rolled back. A domain error (for example {@code NotFoundException}) thrown
     * inside the callable is rethrown as it is, not hidden in an {@code SQLException}.
     *
     * @param callable the work to run in the transaction.
     * @param <R>      the type returned by the callable.
     * @return the value returned by the callable.
     * @throws SQLException     if the transaction fails for a database reason.
     * @throws RuntimeException the unchecked cause, when the callable threw one.
     */
    public <R> R transaction(Callable<R> callable) throws SQLException {
        try {
            return TransactionManager.callInTransaction(dao.getConnectionSource(), callable);
        } catch (SQLException e) {
            if (e.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            throw e;
        }
    }
}
