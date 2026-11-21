/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library;

import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.db.Database;
import cl.ucn.disc.arqsist.library.grpc.MemberClient;
import cl.ucn.disc.arqsist.library.grpc.MemberGrpcService;
import cl.ucn.disc.arqsist.library.model.Member;
import cl.ucn.disc.arqsist.library.service.MemberService;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the member search round trip: client, in-process transport, adapter, service and DAO.
 * The database holds three members, created in this order: Ada Lovelace, Grace Hopper
 * and Alan Turing.
 */
final class MemberSearchTest {

    /**
     * The in-process server.
     */
    private Server server;

    /**
     * The client under test.
     */
    private MemberClient client;

    /**
     * Builds an in-memory database with three members, starts the in-process server
     * and creates the client.
     *
     * @throws Exception if the database or the server cannot start.
     */
    @BeforeEach
    void setUp() throws Exception {
        // The in-memory database keeps every test isolated.
        Database db = new Database("jdbc:sqlite::memory:");
        MemberService memberService = new MemberService(new MemberDao(db.connectionSource()));
        memberService.register(new Member("Ada Lovelace", "ada@example.com"));
        memberService.register(new Member("Grace Hopper", "grace@example.com"));
        memberService.register(new Member("Alan Turing", "alan@example.com"));

        // The in-process transport removes the network from the test.
        String serverName = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(serverName).directExecutor()
                .addService(new MemberGrpcService(memberService))
                .build()
                .start();
        ManagedChannel channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
        client = new MemberClient(channel);
    }

    /**
     * Closes the client and stops the server: the test owns their lifecycle.
     */
    @AfterEach
    void tearDown() {
        client.close();
        server.shutdownNow();
    }

    /**
     * Case 1: the query "ada" finds Ada Lovelace only.
     */
    @Test
    void searchFindsAMemberBySubstring() {
        assertEquals(List.of("Ada Lovelace"), names(client.search("ada")));
    }

    /**
     * Case 2: the match is case-insensitive.
     */
    @Test
    void searchIgnoresTheCaseOfTheQuery() {
        assertEquals(List.of("Ada Lovelace"), names(client.search("ADA")));
    }

    /**
     * Case 3: the service trims the query.
     */
    @Test
    void searchTrimsTheQuery() {
        assertEquals(List.of("Ada Lovelace"), names(client.search(" ada ")));
    }

    /**
     * Case 4: the query "a" matches the three members, ordered by ID.
     */
    @Test
    void searchOrdersTheResultById() {
        assertEquals(List.of("Ada Lovelace", "Grace Hopper", "Alan Turing"), names(client.search("a")));
    }

    /**
     * Case 5: a query with no match returns an empty list and the call succeeds.
     */
    @Test
    void searchWithoutMatchReturnsAnEmptyList() {
        assertTrue(client.search("zzz").isEmpty());
    }

    /**
     * Case 6: the characters % and _ are literal text, so no name matches them.
     */
    @Test
    void searchTreatsWildcardsAsLiteralText() {
        assertTrue(client.search("%").isEmpty());
        assertTrue(client.search("_").isEmpty());
    }

    /**
     * Case 7: a blank query reaches the client as IllegalArgumentException,
     * from the INVALID_ARGUMENT status.
     */
    @Test
    void searchWithABlankQueryIsAnInvalidArgument() {
        assertThrows(IllegalArgumentException.class, () -> client.search(""));
        assertThrows(IllegalArgumentException.class, () -> client.search(" "));
    }

    /**
     * Extracts the names of the members, in the same order.
     *
     * @param members the members.
     * @return the names of the members.
     */
    private static List<String> names(List<Member> members) {
        return members.stream().map(Member::getName).toList();
    }
}
