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
        JsonObjectBuilder createObjectBuilder = Json.createObjectBuilder();
        JsonArrayBuilder versionsArrayBuilder = Json.createArrayBuilder();
        
        for(WorkspaceNodeReplacement nodeReplacement : nodeReplacementCollection) {
            
            versionsArrayBuilder.add(
                    Json.createObjectBuilder()
                        .add("from", nodeReplacement.getOldNodeURI().toString())
                        .add("to", nodeReplacement.getNewNodeURI().toString()));
        }
        
        createObjectBuilder.add("versions", versionsArrayBuilder);
        mainObjectBuilder.add("create", createObjectBuilder);
        
        return mainObjectBuilder.build();
    }

    /**
     * @see JsonTransformationHandler#createNodeReplacementCollectionFromJsonObject(javax.json.JsonObject)
     */
    @Override
    public Collection<WorkspaceNodeReplacement> createNodeReplacementCollectionFromJsonObject(JsonObject jsonObject) throws URISyntaxException {
        
        Collection<WorkspaceNodeReplacement> nodeReplacementCollection = new ArrayList<WorkspaceNodeReplacement>();
        
        JsonObject created = jsonObject.getJsonObject("created");
        
        JsonArray versionsArray = null;
        try {
            versionsArray = created.getJsonArray("versions");
        } catch(ClassCastException ex) {
            logger.debug("'versions' is not a JsonArray, will try to cast to JsonObject");
        }
        
        if(versionsArray != null) {
            for(int i = 0; i < versionsArray.size(); i++) {
                JsonObject currentObject = versionsArray.getJsonObject(i);
                WorkspaceNodeReplacement currentReplacement = getNodeReplacementFromJsonObject(currentObject);
                nodeReplacementCollection.add(currentReplacement);
            }
        } else {
            JsonObject versionsObject = created.getJsonObject("versions");
            WorkspaceNodeReplacement replacement = getNodeReplacementFromJsonObject(versionsObject);
            nodeReplacementCollection.add(replacement);
        }
        
        return nodeReplacementCollection;
    }

    /**
     * @see JsonTransformationHandler#getCrawlIdFromJsonObject(javax.json.JsonObject)
     */
    @Override
    public String getCrawlIdFromJsonObject(JsonObject jsonObject) {
        
        JsonObject startCrawlObject = jsonObject.getJsonObject("crawlerStart");
        
        String idToReturn = startCrawlObject.getString("id");
        
        //TODO CHECK IF OK??
        
        return idToReturn;
    }
    
    
    private WorkspaceNodeReplacement getNodeReplacementFromJsonObject(JsonObject innerObject) throws URISyntaxException {
        
        WorkspaceNodeReplacement replacementToReturn;
        
        URI oldNodeURI = new URI(innerObject.getString("from"));
        URI newNodeURI = new URI(innerObject.getString("to"));
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
