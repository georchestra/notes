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

import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class define webservices to :
 * - store a new note : POST on /note/backend/foo
 * - gather informations about available backends : GET on /note/backends
 *
 * You can also display debug form with a GET on /note/backend/foo
 */
@Controller
public class NoteController {


    /**
     * GeorchestraConfiguration is used to retrieve backends from georchestra datadir. Backends need to be defined in
     * mapfishapp.properties file like :
     *
     * note.0.id = backend1
     * note.0.table = georchestra.notes
     * note.0.srid = 4326
     * note.0.jdbcUrl = jdbc:postgresql://localhost:5432/georchestra?user=www-data&password=www-data
     */
    @Autowired
    private GeorchestraConfiguration configuration;

    /**
     * Hold all configured backends by identifier
     */
    private Map<String, NoteBackend> backends;


    /**
     * This read configuration in datadir a create configured backends just after controller creation. This will
     * populate backends Map with properties defined in datadir.
     */
    @PostConstruct
    private void init(){

        this.backends = new HashMap<String, NoteBackend>();

        String id, table, jdbcUrl;
        int srid;
        int i = 0;

        while((id = this.configuration.getProperty("note." + i + ".id")) != null){
            table = this.configuration.getProperty("note." + i + ".table");
            srid = Integer.parseInt(this.configuration.getProperty("note." + i + ".srid"));
            jdbcUrl = this.configuration.getProperty("note." + i + ".jdbcUrl");

            this.backends.put(id, new NoteBackend(id, table, srid, jdbcUrl));
            i++;
        }

    }

    /**
     * Send backends description to UI. This will return JSON representation of all backends configured. JSON have a
     * 'backends' key linked a JSON array containing all backends. Each backend is describe by JSONOject with following
     * keys :
     * - id : identifier of backend, this identifier will be used to construct URL to post on
     * - srid : numeric identifier of projection, Example : 4326
     * - description : textual description of backend with identifier, table and srid
     *
     * @param response
     * @throws IOException if servlet cannot write output
     */
    @RequestMapping(value = "/note/backends", method = RequestMethod.GET)
	public void getBackends(HttpServletResponse response) throws IOException {

        JSONArray bes = new JSONArray();

        for (String id : this.backends.keySet())
            bes.put(this.backends.get(id).toJson());

        JSONObject res = new JSONObject();
        res.put("backends",bes);
        // Note: "text/html" required for http://docs.sencha.com/extjs/3.4.0/#!/api/Ext.form.BasicForm
        response.setContentType("text/html"); // Should be "application/json"
        response.getWriter().print(res.toString(4));

    }


    /**
     * Display debug form to test note storage without UI part. This will display simple HTML form that post to
     * specified backend.
     *
     * @param response
     * @param backendId identifier of backend to use
     * @throws Exception if servlet cannot write output
     */
    @RequestMapping(value = "/note/backend/{backendId}", method = RequestMethod.GET)
	public void getBackend(HttpServletResponse response, @PathVariable String backendId) throws Exception {

        NoteBackend backend =  this.backends.get(backendId);
        if(backend == null) {
            response.setStatus(404);
            response.getWriter().print("No such backend : " + backendId);
        } else {
            String res = backend.toString() + "<br>" +
                    "<form method=\"POST\">" +
                    "Email : <input type=\"checkbox\" name=\"followup\"><br>" +
                    "Email : <input type=\"text\" name=\"email\"><br>" +
                    "Comment : <input type=\"text\" name=\"comment\"><br>" +
                    "Map context : <input type=\"text\" name=\"map_context\"><br>" +
                    "Latitude : <input type=\"text\" name=\"latitude\"><br>" +
                    "Longitude : <input type=\"text\" name=\"longitude\"><br>" +
                    "<input type=\"submit\" value=\"Store\">" +
                    "</form>";

            response.setContentType("text/html");
            response.getWriter().print(res);
        }

    }

    /**
     * Request storage of one note to specified backend. Note description is passed through form parameter (not json
     * payload). The following parameters are accepted :
     * - followup (mandatory)
     * - email (optional)
     * - comment (mandatory)
     * - map_context (mandatory)
     * - latitude (mandatory)
     * - longitude (mandatory)
     *
     * Note 'login' field will be filled with 'sec-username' http header if user is conencted.
     *
     * @param request
     * @param response
     * @param backendId identifier of backend to store note
     * @throws IOException if controller cannot write on response object output writer
     * @throws SQLException if something went wrong during database storage of note
     */
    @RequestMapping(value = "/note/backend/{backendId}", method = RequestMethod.POST)
    public void storeNote(HttpServletRequest request, HttpServletResponse response, @PathVariable String backendId) throws IOException, SQLException {

        NoteBackend backend =  this.backends.get(backendId);
        // Note: "text/html" required for http://docs.sencha.com/extjs/3.4.0/#!/api/Ext.form.BasicForm
        response.setContentType("text/html");
        JSONObject res = new JSONObject();

        if(backend == null) {
            response.setStatus(404);
            res.put("success", false);
            res.put("msg", "No such backend : " + backendId);
        } else {
            double latitude = Double.parseDouble(request.getParameter("latitude"));
            double longitude = Double.parseDouble(request.getParameter("longitude"));

            Note Note = new Note(
                    Boolean.valueOf(request.getParameter("followup")),
                    request.getParameter("comment"),
                    request.getParameter("map_context"),
                    latitude,
                    longitude);

            if (request.getParameter("email") != null) {
                Note.setEmail(request.getParameter("email"));
            }

            if (request.getHeader("sec-username") != null)
                Note.setLogin(request.getHeader("sec-username"));
            backend.store(Note);
            res.put("success", true);
            res.put("msg", "Note stored");
        }

        response.getWriter().print(res.toString());
    }

}
