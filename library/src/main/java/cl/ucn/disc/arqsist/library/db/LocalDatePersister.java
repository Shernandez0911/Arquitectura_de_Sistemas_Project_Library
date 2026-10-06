/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */
package cl.ucn.disc.arqsist.library.db;

import java.sql.SQLException;
import java.time.LocalDate;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.support.DatabaseResults;

/**
 * ORMLite persister that stores a {@link LocalDate} as an ISO-8601 text column
 * (for example {@code 2026-10-06}).
 */
public class LocalDatePersister extends BaseDataType {

    /**
     * The single shared instance used by ORMLite.
     */
    private static final LocalDatePersister singleTon = new LocalDatePersister();

    /**
     * Returns the shared instance. ORMLite calls this method to find the persister.
     *
     * @return the singleton persister.
     */
    public static LocalDatePersister getSingleton() {
        return singleTon;
    }

    /**
     * Creates the persister with the {@code STRING} SQL type and registers {@link LocalDate}.
     * Private so that only the singleton exists.
     */
    private LocalDatePersister() {
        super(SqlType.STRING, new Class<?>[] { LocalDate.class });
    }

    /**
     * Parses a default value declared on a field.
     *
     * @param fieldType  the field that declares the default.
     * @param defaultStr the default value as text.
     * @return the same text, because the column already stores text.
     * @throws SQLException never thrown by this implementation.
     */
    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
        return defaultStr;
    }

    /**
     * Reads the column value from a query result.
     *
     * @param fieldType the field being read.
     * @param results   the query result.
     * @param columnPos the position of the column in the result.
     * @return the column value as text.
     * @throws SQLException if the column cannot be read.
     */
    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        return results.getString(columnPos);
    }

    /**
     * Converts the stored text into a Java date.
     *
     * @param fieldType the field being read.
     * @param sqlArg    the stored text, in ISO-8601 format.
     * @param columnPos the position of the column in the result.
     * @return the parsed {@link LocalDate}.
     * @throws SQLException never thrown by this implementation; a malformed date raises a
     *                      {@link java.time.format.DateTimeParseException}.
     */
    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        return LocalDate.parse((String) sqlArg);
    }

    /**
     * Converts a Java date into the text that is stored.
     *
     * @param fieldType  the field being written.
     * @param javaObject the {@link LocalDate} to store.
     * @return the date in ISO-8601 format.
     * @throws SQLException never thrown by this implementation.
     */
    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
        return javaObject.toString();
    }
}
