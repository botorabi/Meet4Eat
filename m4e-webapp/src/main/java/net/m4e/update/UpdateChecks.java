/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.json.*;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.List;


/**
 * Class implementing update check related functionality.
 * 
 * @author boto
 * Date of creation Dec 5, 2017
 */
public class UpdateChecks {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final EntityManager entityManager;


    /**
     * Create an instance of update checks.
     * 
     * @param entityManager The injected entity manager
     */
    @Inject
    public UpdateChecks(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Import an update check entity from a JSON description.
     * 
     * @param updateCheckJson   The update check entry in JSON format
     * @return                  An update check entity or null if the JSON input was not valid or incomplete.
     */
    public UpdateCheckEntity importUpdateCheckJSON(String updateCheckJson) {
        if (updateCheckJson == null) {
            return null;
        }

        String id, name, flavor, version, os, url;
        boolean active;
        try {
            JsonReader jreader = Json.createReader(new StringReader(updateCheckJson));
            JsonObject jobject = jreader.readObject();
            id          = jobject.getString("id", null);
            name        = jobject.getString("name", null);
            os          = jobject.getString("os", null);
            version     = jobject.getString("version", null);
            flavor      = jobject.getString("flavor", "");
            url         = jobject.getString("url", null);
            active      = jobject.getBoolean("active", true);
        }
        catch(Exception ex) {
            LOGGER.warn("invalid update check entry detected: " + ex.getLocalizedMessage());
            return null;
        }

        if ((name == null) || (os == null) || (version == null) || (url == null)) {
            return null;
        }

        UpdateCheckEntity entity = new UpdateCheckEntity();
        if ((id != null) && !id.isEmpty()) {
            try {
                entity.setId(Long.parseLong(id));
            }
            catch(NumberFormatException ex) {
                LOGGER.warn("invalid update check entry ID");
                return null;
            }
        }
        else {
            entity.setId(0L);
        }
        entity.setName(name);
        entity.setOS(os);
        entity.setVersion(version);
        entity.setUrl(url);
        entity.setFlavor(flavor);
        entity.setIsActive(active);
        return entity;
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
            .add("url" , entity.getUrl())
            .add("active" , entity.getIsActive());                
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
        if ((result == null) || !result.getIsActive() || (result.getVersion().equals(clientver))) {
            update.add("updateVersion", "")
                  .add("os", "")
                  .add("url", "")
                  .add("releaseDate", 0L);
        }
        else {
            update.add("updateVersion", result.getVersion())
                  .add("os", result.getOS())
                  .add("url", result.getUrl())
                  .add("releaseDate", result.getReleaseDate());
        }
        return update;
    }
}
