/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.service;

/**
 * Thrown when a requested record (book, member, loan or reservation) does not exist.
 */
public class NotFoundException extends RuntimeException {

    /**
     * Creates the exception with a description of the missing record.
     *
     * @param message the detail message, for example "Book not found: 42".
     */
    public NotFoundException(String message) {
        super(message);
    }
}
