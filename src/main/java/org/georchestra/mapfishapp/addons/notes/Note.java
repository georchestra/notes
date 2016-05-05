/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.mapfishapp.addons.notes;

/**
 * This class hold informations about note. All field are mandatory except 'login' field.
 */
public class Note {

	private Boolean followUp;
    private String email;
    private String comment;
    private String mapContext;
    private String login;
    private double latitude;
    private double longitude;

    /**
     * Create a new instance of note, Note that 'login' and 'email' fields are optional, so they will be affected by following setter.
     *
     * @param followUp Inform reporter of update on this issue
     * @param email E-mail address of reporter
     * @param comment Description of note
     * @param mapContext Description of layers enable when user create a note, this correspond to mapfishapp map context
     * @param latitude Latitude of note
     * @param longitude Longitude of note
     */
    public Note(Boolean followUp, String comment, String mapContext, double latitude, double longitude) {
        this.followUp = followUp;
        this.comment = comment;
        this.mapContext = mapContext;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Set login of reporter, as it is an optional field, this is only way to specify login.
     *
     * @param login login of reporter
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Set email of reporter, as it is an optional field, this is the only way to specify email.
     *
     * @param email email of reporter
     */
    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getFollowUp() {
        return followUp;
    }

    public String getEmail() {
        return email;
    }

    public String getComment() {
        return comment;
    }

    public String getMapContext() {
        return mapContext;
    }

    public String getLogin() {
        return login;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }


}
