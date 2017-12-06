/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.update;

import java.io.StringReader;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;


/**
 * Class implementing update check related functionality.
 * 
 * @author boto
 * Date of creation Dec 5, 2017
 */
public class UpdateChecks {

    /**
     * Used for logging
     */
    private final static String TAG = "UpdateChecks";

    private final EntityManager entityManager;

    /**
     * Create an instance of update checks.
     * 
     * @param entityManager    Entity manager
     */
    public UpdateChecks(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Export an update entity to JSON format.
     * 
     * @param entity    Entity to export
     * @return          JSON object builder containing the entity
     */
    public JsonObjectBuilder exportUpdateJSON(UpdateCheckEntity entity) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("id", entity.getId())
            .add("name", entity.getName())
            .add("os" , entity.getOS())
            .add("flavor" , entity.getFlavor())
            .add("version" , entity.getVersion())
            .add("releaseDate" , entity.getReleaseDate())
            .add("url" , entity.getUrl());
        return json;
    }

    /**
     * Export a list of update entities.
     * 
     * @param entities  Entities to export
     * @return          JSON array containing the update entities
     */
    public JsonArrayBuilder exportUpdatesJSON(List<UpdateCheckEntity> entities) {
        JsonArrayBuilder exp = Json.createArrayBuilder();
        entities.forEach((update) -> {
            exp.add(exportUpdateJSON(update));
        });
        return exp;
    }

    /**
     * Given a JSON request string return the results of an update check.
     * 
     * @param  jsonString   JSON string containing the information about the requesting client.
     *                       It must have the following fields: 'name', 'platform', 'version'.
     *                       Optionally a flavor can be given by the field 'flavor'.
     * @return              JSON response containing the results of the update check
     * @throws Exception    Throws an exception if the input is invalid or imcomplete
     */
    public JsonObjectBuilder checkForUpdate(String jsonString) throws Exception {

        if (jsonString == null) {
            throw new Exception("Invalid input");
        }

        String name, flavor, clientver, os;
        try {
            JsonReader jreader = Json.createReader(new StringReader(jsonString));
            JsonObject jobject = jreader.readObject();
            name       = jobject.getString("name", null);
            os         = jobject.getString("os", null);
            clientver  = jobject.getString("clientVersion", null);
            flavor     = jobject.getString("flavor", null);
        }
        catch(Exception ex) {
            throw new Exception("Invalid input format");
        }

        if ((name == null) || (os == null) || (clientver == null)) {
            throw new Exception("Incomplete request information");
        }

        TypedQuery<UpdateCheckEntity> query;
        if (flavor == null) {
            query = entityManager.createNamedQuery("UpdateCheckEntity.findUpdate", UpdateCheckEntity.class);
        }
        else {
            query = entityManager.createNamedQuery("UpdateCheckEntity.findFlavorUpdate", UpdateCheckEntity.class);            
            query.setParameter("flavor", flavor);
        }
        query.setParameter("name", name);
        query.setParameter("os", os);

        UpdateCheckEntity result = null;
        try {
            result = query.getSingleResult();
        }
        catch(Exception ex) {}

        JsonObjectBuilder update = Json.createObjectBuilder();
        if ((result == null) || (result.getVersion().equals(clientver))) {
            update.add("updateVersion", "")
                  .add("url", "")
                  .add("releaseDate", 0L);
        }
        else {
            update.add("updateVersion", result.getVersion())
                  .add("url", result.getUrl())
                  .add("releaseDate", result.getReleaseDate());
        }
        return update;
    }
}
