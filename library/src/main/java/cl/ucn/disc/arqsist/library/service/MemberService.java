/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.service;

import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.model.Member;

import java.util.List;

/**
 * Member service: registers and lists members.
 */
public final class MemberService {

    /**
     * Persistence access for members.
     */
    private final MemberDao memberDao;

    /**
     * Creates the service.
     *
     * @param memberDao the member DAO.
     */
    public MemberService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    /**
     * Registers a new member.
     *
     * @param member the member to register.
     * @return the registered member.
     */
    public Member register(Member member) {
        memberDao.create(member);
        return member;
    }

    /**
     * Lists every member.
     *
     * @return all members.
     */
    public List<Member> findAll() {
        return memberDao.findAll();
    }
}