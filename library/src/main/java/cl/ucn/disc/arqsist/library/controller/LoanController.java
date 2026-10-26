/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.controller;

import cl.ucn.disc.arqsist.library.service.LoanService;
import io.javalin.config.JavalinConfig;

import java.util.Objects;

/**
 * HTTP routes for loans.
 */
public final class LoanController {

    /**
     * The service that lends, lists and closes loans.
     */
    private final LoanService loanService;

    /**
     * Creates the controller.
     *
     * @param loanService the loan service.
     */
    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    /**
     * Registers the loan routes: {@code POST /loans}, {@code GET /loans},
     * {@code POST /loans/{id}/return} and {@code GET /loans/overdue}.
     *
     * @param config the Javalin configuration that receives the routes.
     */
    public void register(JavalinConfig config) {
        config.routes.post("/loans", ctx -> {
            int memberId = Integer.parseInt(Objects.requireNonNull(ctx.queryParam("memberId")));
            int bookId = Integer.parseInt(Objects.requireNonNull(ctx.queryParam("bookId")));
            ctx.json(loanService.checkout(memberId, bookId));
        });
        config.routes.get("/loans", ctx -> ctx.json(loanService.findAll()));
        config.routes.post("/loans/{id}/return", ctx -> ctx.json(loanService.returnLoan(Integer.parseInt(ctx.pathParam("id")))));
        config.routes.get("/loans/overdue", ctx -> ctx.json(loanService.overdueLoans()));
    }
}
