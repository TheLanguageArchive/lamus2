/*
 * Copyright (C) 2014 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.archive.implementation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import nl.mpi.lamus.archive.JsonTransformationHandler;
import nl.mpi.lamus.workspace.model.WorkspaceNodeReplacement;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNodeReplacement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @see JsonTransformationHandler
 * @author guisil
 */
@Component
public class LamusJsonTransformationHandler implements JsonTransformationHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusJsonTransformationHandler.class);

    /**
     * @see JsonTransformationHandler#createJsonObjectFromNodeReplacementCollection(java.util.Collection)
     */
    @Override
    public JsonObject createJsonObjectFromNodeReplacementCollection(Collection<WorkspaceNodeReplacement> nodeReplacementCollection) {
        
        JsonObjectBuilder mainObjectBuilder = Json.createObjectBuilder();
        JsonArrayBuilder versionsArrayBuilder = Json.createArrayBuilder();
        
        for(WorkspaceNodeReplacement nodeReplacement : nodeReplacementCollection) {
            
            versionsArrayBuilder.add(
                    Json.createObjectBuilder()
                        .add("fromId", nodeReplacement.getOldNodeURI().toString())
                        .add("toId", nodeReplacement.getNewNodeURI().toString()));
        }
        
        mainObjectBuilder.add("list", versionsArrayBuilder);
        
        return mainObjectBuilder.build();
    }

    /**
     * @see JsonTransformationHandler#createNodeReplacementCollectionFromJsonObject(javax.json.JsonObject)
     */
    @Override
    public Collection<WorkspaceNodeReplacement> createNodeReplacementCollectionFromJsonObject(JsonObject jsonObject) throws URISyntaxException {
        
        Collection<WorkspaceNodeReplacement> nodeReplacementCollection = new ArrayList<>();
        
        JsonArray versionsArray = null;
        try {
            versionsArray = jsonObject.getJsonArray("list");
        } catch(ClassCastException ex) {
            logger.debug("'versions' is not a JsonArray, will try to cast to JsonObject", ex);
        }
        
        if(versionsArray != null) {
            logger.info("Creating NodeReplacement collection from received JSON array with size " + versionsArray.size());
            for(int i = 0; i < versionsArray.size(); i++) {
                JsonObject currentObject = versionsArray.getJsonObject(i);
                WorkspaceNodeReplacement currentReplacement = getNodeReplacementFromJsonObject(currentObject);
                nodeReplacementCollection.add(currentReplacement);
            }
        } else {
            logger.warn("A JSON array could not be retrieved from the received JSON object. Will return empty collection of NoreReplacement.");
        }
        
        return nodeReplacementCollection;
    }

    /**
     * @see JsonTransformationHandler#getCrawlerIdFromJsonObject(javax.json.JsonObject)
     */
    @Override
    public String getCrawlerIdFromJsonObject(JsonObject jsonObject) {
        
        return jsonObject.getString("id");
    }

    /**
     * @see JsonTransformationHandler#getCrawlerStateFromJsonObject(javax.json.JsonObject)
     */
    @Override
    public String getCrawlerStateFromJsonObject(JsonObject crawlerStateJsonObject) {
        
        JsonObject detailedCrawlerStateObject = crawlerStateJsonObject.getJsonObject("state");
        
        return detailedCrawlerStateObject.getString("state");
    }
    
    
    private WorkspaceNodeReplacement getNodeReplacementFromJsonObject(JsonObject innerObject) throws URISyntaxException {
        
        WorkspaceNodeReplacement replacementToReturn;
        
        URI oldNodeURI = new URI(innerObject.getString("fromId"));
        URI newNodeURI = new URI(innerObject.getString("toId"));
        String status = innerObject.getString("status").toUpperCase();
        if("OK".equals(status)) {
            replacementToReturn = new LamusWorkspaceNodeReplacement(oldNodeURI, newNodeURI, status);
        } else {
            String error = innerObject.getString("error");
            replacementToReturn = new LamusWorkspaceNodeReplacement(oldNodeURI, newNodeURI, status, error);
        }
        
        return replacementToReturn;
    }
}
