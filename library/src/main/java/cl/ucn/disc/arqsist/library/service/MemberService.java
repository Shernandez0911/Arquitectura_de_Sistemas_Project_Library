/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.service;

import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.model.Member;

import java.util.List;

/**
 * The Member Service.
 */
public final class MemberService {

    /**
     * The Member DAO.
     */
    private final MemberDao memberDao;

    /**
     * The Constructor.
     *
     * @param memberDao the member DAO
     */
    public MemberService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    /**
     * Register a new member.
     *
     * @param member the member to register
     * @return the registered member
     */
    public Member register(Member member) {
        memberDao.create(member);
        return member;
    }

    /**
     * @return a list of all members.
     */
    public List<Member> findAll() {
        return memberDao.findAll();
    }

    /**
     * Find a member by ID.
     *
     * @param id the member ID
     * @return the member
     * @throws NotFoundException if the member does not exist
     */
    public Member findById(int id) {
        // ORMLite returns null when the row does not exist.
        Member member = memberDao.findById(id);
        if (member == null) {
            throw new NotFoundException("Member not found: " + id);
        }
        return member;
    }
}
