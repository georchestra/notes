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

import org.apache.commons.dbcp.BasicDataSource;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * This class represent note storage backend. Note will be store to a database configured is this class with jdbc URL,
 * table name and srid. Backend database must have postgis extension.
 */
public class NoteBackend {

    private String id;
    private String table;
    private int srid;
    private String jdbcUrl;
    private BasicDataSource basicDataSource;

    /**
     * Create a new instance of NoteBackend and create a BasicDataSource configured with jdbc URL. Link to database will
     * not be tested until store() method is called.
     *
     * @param id string to identify this backend, must be unique across backends
     * @param table name of table to store notes, may contains schema
     * @param srid numeric identifier of projection linked to coordinates. Example : 4326 for EPSG:4326.
     * @param jdbcUrl jdbc URL used to connect to database. Example : jdbc:postgresql://localhost:5432/georchestra?user=www-data&password=www-data
     */
    public NoteBackend(String id, String table, int srid, String jdbcUrl) {
        this.id = id;
        this.table = table;
        this.srid = srid;
        this.jdbcUrl = jdbcUrl;

        this.basicDataSource = new BasicDataSource();
        this.basicDataSource.setDriverClassName("org.postgresql.Driver");
        this.basicDataSource.setTestOnBorrow(true);
        this.basicDataSource.setPoolPreparedStatements(true);
        this.basicDataSource.setMaxOpenPreparedStatements(-1);
        this.basicDataSource.setDefaultReadOnly(false);
        this.basicDataSource.setDefaultAutoCommit(true);
        this.basicDataSource.setUrl(jdbcUrl);
    }

    /**
     * Store note in database configured in this backend.
     * @param note note to store in this backend
     */
    public void store(Note note) throws SQLException {

        Connection connection = null;
        PreparedStatement st = null;
       // try {
            connection = this.basicDataSource.getConnection();
            st = connection.prepareStatement("INSERT INTO " + this.table + "(followup, email, comment, map_context, login, the_geom) VALUES (?,?,?,?,?,ST_SetSRID(ST_MakePoint(?,?),?))");
            st.setBoolean(1, note.getFollowUp());
            st.setString(2, note.getEmail());
            st.setString(3, note.getComment());
            st.setString(4, note.getMapContext());
            st.setString(5, note.getLogin());
            st.setDouble(6, note.getLongitude());
            st.setDouble(7, note.getLatitude());
            st.setInt(8, this.srid);
            st.executeUpdate();
//        }
//        catch (SQLException e) {
//            throw new RuntimeException(e);
//        } finally {
//            if (st != null) try { st.close(); } catch (SQLException e) {LOG.error(e);}
//            if (connection != null) try { connection.close(); } catch (SQLException e) {LOG.error(e);}
//        }
    }

    @Override
    public String toString() {
        return "NoteBackend{" +
                "id='" + id + '\'' +
                ", table='" + table + '\'' +
                ", srid='" + srid + '\'' +
                '}';
    }

    /**
     * Create and return a JSON description of this backend with identifier, projection to use and description
     *
     * @return JSONObject containing informations about this backend
     */
    public JSONObject toJson() {
        JSONObject res = new JSONObject();
        res.put("id", this.id);
        res.put("srid", this.srid);
        res.put("description", this.toString());
        return res;
    }
}
