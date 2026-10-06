/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */

package cl.ucn.disc.arqsist.library.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * A person registered in the library who can borrow and reserve books.
 */
@DatabaseTable(tableName = "members")
public final class Member {

    /**
     * Generated primary key.
     */
    @DatabaseField(generatedId = true)
    private int id;

    /**
     * The full name of the member.
     */
    @DatabaseField(canBeNull = false)
    private String name;

    /**
     * The email address of the member.
     */
    @DatabaseField(canBeNull = false)
    private String email;

    /**
     * No-argument constructor required by ORMLite and by JSON deserialization.
     */
    public Member() {
    }

    /**
     * Creates a member.
     *
     * @param name  the full name.
     * @param email the email address.
     */
    public Member(String name, String email) {
        this.name = name;
        this.email = email;
    }

    /**
     * Returns the member id.
     *
     * @return the id.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the member id.
     *
     * @param id the new id.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the full name.
     *
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the full name.
     *
     * @param name the new name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the email address.
     *
     * @return the email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address.
     *
     * @param email the new email.
     */
    public void setEmail(String email) {
        this.email = email;
    }
}
