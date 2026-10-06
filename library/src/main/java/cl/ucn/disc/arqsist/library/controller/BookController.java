/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.controller;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.service.BookService;
import io.javalin.config.JavalinConfig;

/**
 * HTTP routes for the book catalog.
 */
public final class BookController {

    /**
     * The catalog service.
     */
    private final BookService service;

    /**
     * Direct access to the book DAO, used by the list route.
     */
    private final BookDao dao;

    /**
     * Creates the controller.
     *
     * @param service the catalog service.
     * @param dao     the book DAO.
     */
    public BookController(BookService service, BookDao dao) {
        this.service = service;
        this.dao = dao;
    }

    /**
     * Registers the book routes: {@code GET /books}, {@code GET /books/{id}} and
     * {@code POST /books}.
     *
     * @param config the Javalin configuration that receives the routes.
     */
    public void register(JavalinConfig config) {
        config.routes.get("/books", ctx -> ctx.json(dao.findAll()));
        config.routes.get("/books/{id}", ctx -> ctx.json(service.findById(Integer.parseInt(ctx.pathParam("id")))));
        config.routes.post("/books", ctx -> ctx.json(service.create(ctx.bodyAsClass(Book.class))));
    }
}
