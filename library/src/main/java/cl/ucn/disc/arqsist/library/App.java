/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library;

import cl.ucn.disc.arqsist.library.controller.BookController;
import cl.ucn.disc.arqsist.library.controller.LoanController;
import cl.ucn.disc.arqsist.library.controller.MemberController;
import cl.ucn.disc.arqsist.library.controller.ReservationController;
import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.dao.ReservationDao;
import cl.ucn.disc.arqsist.library.db.Database;
import cl.ucn.disc.arqsist.library.grpc.MemberGrpcService;
import cl.ucn.disc.arqsist.library.service.BookService;
import cl.ucn.disc.arqsist.library.service.LoanService;
import cl.ucn.disc.arqsist.library.service.MemberService;
import cl.ucn.disc.arqsist.library.service.NotFoundException;
import cl.ucn.disc.arqsist.library.service.ReservationService;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * The main class of the application.
 */
public final class App {

    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(App.class);

    /**
     * The time to wait for a server to stop, in seconds.
     */
    private static final long STOP_TIMEOUT_SECONDS = 5;

    /**
     * The main method of the application.
     */
    public static void main(String[] args) throws Exception {
        log.info("creating the database");
        Database db = new Database("jdbc:sqlite:database.db");
        log.info("seeding the database if empty");
        db.seedIfEmpty();

        log.info("creating the DAOs");
        BookDao bookDao = new BookDao(db.connectionSource());
        MemberDao memberDao = new MemberDao(db.connectionSource());
        LoanDao loanDao = new LoanDao(db.connectionSource());
        ReservationDao reservationDao = new ReservationDao(db.connectionSource());

        log.info("creating the services");
        Clock clock = Clock.system(ZoneId.of("America/Santiago"));
        BookService bookService = new BookService(bookDao);
        MemberService memberService = new MemberService(memberDao);
        LoanService loanService = new LoanService(loanDao, memberDao, bookService, clock);
        ReservationService reservationService =
                new ReservationService(reservationDao, memberDao, loanDao, bookService, clock);

        log.info("starting the member gRPC server on {}:{}", MemberGrpcService.HOST, MemberGrpcService.PORT);
        Server memberServer = NettyServerBuilder
                .forAddress(new InetSocketAddress(MemberGrpcService.HOST, MemberGrpcService.PORT))
                .addService(new MemberGrpcService(memberService))
                .build()
                .start();
        // Register the hook now: a later start failure must not leave the server running.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("stopping the gRPC server");
            stop(memberServer);
        }));

        log.info("creating the controllers");
        BookController bookController = new BookController(bookService);
        MemberController memberController = new MemberController(memberService);
        LoanController loanController = new LoanController(loanService);
        ReservationController reservationController = new ReservationController(reservationService);

        log.info("creating the web server");
        Javalin app = Javalin.create(config -> {
            // write LocalDate values as ISO strings, not as arrays
            config.jsonMapper(new JavalinJackson().updateMapper(mapper ->
                    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)));

            // static files
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/public";
                staticFiles.location = Location.CLASSPATH;
            });

            // map domain errors to HTTP status codes
            config.routes.exception(NotFoundException.class, (e, ctx) ->
                    ctx.status(404).json(Map.of("error", e.getMessage())));
            config.routes.exception(IllegalArgumentException.class, (e, ctx) ->
                    ctx.status(400).json(Map.of("error", e.getMessage())));
            config.routes.exception(IllegalStateException.class, (e, ctx) ->
                    ctx.status(409).json(Map.of("error", e.getMessage())));

            // register the controllers
            bookController.register(config);
            memberController.register(config);
            loanController.register(config);
            reservationController.register(config);
        });

        log.info("starting the web server on port 7070");
        app.start(7070);
        log.info("the web server is ready");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("stopping the web server");
            app.stop();
        }));
    }

    /**
     * Stop a gRPC server and wait for the running calls to end.
     *
     * @param server the server to stop
     */
    private static void stop(Server server) {
        // shutdown refuses new calls and lets the running calls end.
        server.shutdown();
        try {
            if (!server.awaitTermination(STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                // The calls did not end in time: cancel them.
                server.shutdownNow();
            }
        } catch (InterruptedException e) {
            server.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
