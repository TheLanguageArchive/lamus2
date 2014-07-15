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
import java.util.Calendar;
import java.util.Collection;
import java.util.UUID;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import nl.mpi.lamus.archive.JsonTransformationHandler;
import nl.mpi.lamus.workspace.model.WorkspaceNodeReplacement;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNodeReplacement;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author guisil
 */
public class LamusJsonTransformationHandlerTest {
    
    
    private JsonTransformationHandler jsonTransformationHandler;
    
    public LamusJsonTransformationHandlerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        jsonTransformationHandler = new LamusJsonTransformationHandler();
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void createJsonObjectFromNodeReplacementCollection() throws URISyntaxException {

        URI firstOldNodeURI = new URI(UUID.randomUUID().toString());
        URI firstNewNodeURI = new URI(UUID.randomUUID().toString());
        WorkspaceNodeReplacement firstNodeReplacement = new LamusWorkspaceNodeReplacement(firstOldNodeURI, firstNewNodeURI);
        
        URI secondOldNodeURI = new URI(UUID.randomUUID().toString());
        URI secondNewNodeURI = new URI(UUID.randomUUID().toString());
        WorkspaceNodeReplacement secondNodeReplacement = new LamusWorkspaceNodeReplacement(secondOldNodeURI, secondNewNodeURI);
        
        Collection<WorkspaceNodeReplacement> nodeReplacementCollection = new ArrayList<WorkspaceNodeReplacement>();
        nodeReplacementCollection.add(firstNodeReplacement);
        nodeReplacementCollection.add(secondNodeReplacement);

        
        JsonObject resultJsonObject = jsonTransformationHandler.createJsonObjectFromNodeReplacementCollection(nodeReplacementCollection);
        
        assertNotNull("Retrieved json object should not be null", resultJsonObject);
        assertNotNull("'create' json object should not be null", resultJsonObject.getJsonObject("create"));
        JsonObject createJsonObject = resultJsonObject.getJsonObject("create");
        assertNotNull("'versions' json array should not be null", createJsonObject.getJsonArray("versions"));
        JsonArray versionsJsonArray = createJsonObject.getJsonArray("versions");
        assertFalse("'versions' json array should not be empty", versionsJsonArray.isEmpty());
        assertEquals("first json object in array has different 'from' value than expected", firstOldNodeURI.toString(), versionsJsonArray.getJsonObject(0).getString("from"));
        assertEquals("first json object in array has different 'to' value than expected", firstNewNodeURI.toString(), versionsJsonArray.getJsonObject(0).getString("to"));
        assertEquals("second json object in array has different 'from' value than expected", secondOldNodeURI.toString(), versionsJsonArray.getJsonObject(1).getString("from"));
        assertEquals("second json object in array has different 'to' value than expected", secondNewNodeURI.toString(), versionsJsonArray.getJsonObject(1).getString("to"));
    }
    
    @Test
    public void createNodeReplacementCollectionFromMultipleJsonObjectStatusOk() throws URISyntaxException {
        
        URI firstOldNodeURI = new URI(UUID.randomUUID().toString());
        URI firstNewNodeURI = new URI(UUID.randomUUID().toString());
        String firstReplacementStatus = "Ok";
        WorkspaceNodeReplacement firstNodeReplacement = new LamusWorkspaceNodeReplacement(firstOldNodeURI, firstNewNodeURI, firstReplacementStatus.toUpperCase());
        
        URI secondOldNodeURI = new URI(UUID.randomUUID().toString());
        URI secondNewNodeURI = new URI(UUID.randomUUID().toString());
        String secondReplacementStatus = "oK";
        WorkspaceNodeReplacement secondNodeReplacement = new LamusWorkspaceNodeReplacement(secondOldNodeURI, secondNewNodeURI, secondReplacementStatus.toUpperCase());
        
        Collection<WorkspaceNodeReplacement> expectedNodeReplacementCollection = new ArrayList<WorkspaceNodeReplacement>();
        expectedNodeReplacementCollection.add(firstNodeReplacement);
        expectedNodeReplacementCollection.add(secondNodeReplacement);

        
        JsonObjectBuilder mainObjectBuilder = Json.createObjectBuilder();
        JsonObjectBuilder createObjectBuilder = Json.createObjectBuilder();
        JsonArrayBuilder versionsArrayBuilder = Json.createArrayBuilder();

        versionsArrayBuilder.add(
                Json.createObjectBuilder()
                    .add("from", firstOldNodeURI.toString())
                    .add("to", firstNewNodeURI.toString())
                    .add("status", firstReplacementStatus));
        versionsArrayBuilder.add(
                Json.createObjectBuilder()
                    .add("from", secondOldNodeURI.toString())
                    .add("to", secondNewNodeURI.toString())
                    .add("status", secondReplacementStatus));
        
        createObjectBuilder.add("versions", versionsArrayBuilder);
        mainObjectBuilder.add("created", createObjectBuilder);
        
        JsonObject createdObject = mainObjectBuilder.build();
        
        
        Collection<WorkspaceNodeReplacement> resultNodeReplacementCollection = jsonTransformationHandler.createNodeReplacementCollectionFromJsonObject(createdObject);
        
        assertEquals("Node replacement collection different from expected", expectedNodeReplacementCollection, resultNodeReplacementCollection);
    }
    
    @Test
    public void createNodeReplacementCollectionFromSingleJsonObjectStatusOk() throws URISyntaxException {
        
        URI firstOldNodeURI = new URI(UUID.randomUUID().toString());
        URI firstNewNodeURI = new URI(UUID.randomUUID().toString());
        String firstReplacementStatus = "Ok";
        WorkspaceNodeReplacement firstNodeReplacement = new LamusWorkspaceNodeReplacement(firstOldNodeURI, firstNewNodeURI, firstReplacementStatus.toUpperCase());
        
        Collection<WorkspaceNodeReplacement> expectedNodeReplacementCollection = new ArrayList<WorkspaceNodeReplacement>();
        expectedNodeReplacementCollection.add(firstNodeReplacement);
        
        JsonObjectBuilder mainObjectBuilder = Json.createObjectBuilder();
        JsonObjectBuilder createObjectBuilder = Json.createObjectBuilder();
        JsonObjectBuilder versionsObjectBuilder = Json.createObjectBuilder()
                .add("from", firstOldNodeURI.toString())
                .add("to", firstNewNodeURI.toString())
                .add("status", firstReplacementStatus);
        
        createObjectBuilder.add("versions", versionsObjectBuilder);
        mainObjectBuilder.add("created", createObjectBuilder);
        
        JsonObject createdObject = mainObjectBuilder.build();
        
        
        Collection<WorkspaceNodeReplacement> resultNodeReplacementCollection = jsonTransformationHandler.createNodeReplacementCollectionFromJsonObject(createdObject);
        
        assertEquals("Node replacement collection different from expected", expectedNodeReplacementCollection, resultNodeReplacementCollection);
    }
    
    @Test
    public void createNodeReplacementCollectoinFromJsonObjectStatusFailed() throws URISyntaxException {
        
        URI firstOldNodeURI = new URI(UUID.randomUUID().toString());
        URI firstNewNodeURI = new URI(UUID.randomUUID().toString());
        String firstReplacementStatus = "failed";
        String firstReplacementError = "reference is invalid for ArchiveObjectDaoImpl";
        WorkspaceNodeReplacement firstNodeReplacement = new LamusWorkspaceNodeReplacement(firstOldNodeURI, firstNewNodeURI, firstReplacementStatus.toUpperCase(), firstReplacementError);
        
        URI secondOldNodeURI = new URI(UUID.randomUUID().toString());
        URI secondNewNodeURI = new URI(UUID.randomUUID().toString());
        String secondReplacementStatus = "FAILED";
        String secondReplacementError = "reference is invalid for ArchiveObjectDaoImpl";
        WorkspaceNodeReplacement secondNodeReplacement = new LamusWorkspaceNodeReplacement(secondOldNodeURI, secondNewNodeURI, secondReplacementStatus.toUpperCase(), secondReplacementError);
        
        Collection<WorkspaceNodeReplacement> expectedNodeReplacementCollection = new ArrayList<WorkspaceNodeReplacement>();
        expectedNodeReplacementCollection.add(firstNodeReplacement);
        expectedNodeReplacementCollection.add(secondNodeReplacement);

        
        JsonObjectBuilder mainObjectBuilder = Json.createObjectBuilder();
        JsonObjectBuilder createObjectBuilder = Json.createObjectBuilder();
        JsonArrayBuilder versionsArrayBuilder = Json.createArrayBuilder();

        versionsArrayBuilder.add(
                Json.createObjectBuilder()
                    .add("from", firstOldNodeURI.toString())
                    .add("to", firstNewNodeURI.toString())
                    .add("status", firstReplacementStatus)
                    .add("error", firstReplacementError));
        versionsArrayBuilder.add(
                Json.createObjectBuilder()
                    .add("from", secondOldNodeURI.toString())
                    .add("to", secondNewNodeURI.toString())
                    .add("status", secondReplacementStatus)
                    .add("error", secondReplacementError));
        
        createObjectBuilder.add("versions", versionsArrayBuilder);
        mainObjectBuilder.add("created", createObjectBuilder);
        
        JsonObject createdObject = mainObjectBuilder.build();
        
        
        Collection<WorkspaceNodeReplacement> resultNodeReplacementCollection = jsonTransformationHandler.createNodeReplacementCollectionFromJsonObject(createdObject);
        
        assertEquals("Node replacement collection different from expected", expectedNodeReplacementCollection, resultNodeReplacementCollection);
    }
    
    @Test
    public void getCrawlerIdFromJsonObject() throws URISyntaxException {
        
        String crawlerId = UUID.randomUUID().toString();
        URI crawlerRootUri = new URI("hdl:" + UUID.randomUUID().toString());
        
        JsonObjectBuilder mainObjectBuilder = Json.createObjectBuilder();
        JsonObjectBuilder crawlerStartObjectBuilder = Json.createObjectBuilder();
        
        crawlerStartObjectBuilder
                .add("id", crawlerId)
                .add("root", crawlerRootUri.toString());
        
        mainObjectBuilder.add("crawlerStart", crawlerStartObjectBuilder);
        
        JsonObject crawlerStartObject = mainObjectBuilder.build();
        
        
        String retrievedId = jsonTransformationHandler.getCrawlerIdFromJsonObject(crawlerStartObject);
        
        assertEquals("Retrieved crawler ID different from expected", crawlerId, retrievedId);
    }
    
    @Test
    public void getCrawlerStateFromJsonObject() throws URISyntaxException {
        
        UUID id = UUID.randomUUID();
        URI rootUri = new URI("hdl:" + UUID.randomUUID().toString());
        String state = "STARTED";
        long metadataCount = 10;
        long resourceCount = 75;
        long count = 100;
        long avgParseTimeMs = 30;
        long avgPersistTimeMs = 100;
        long avgProcessingTimeMs = 150;
        long started = Calendar.getInstance().getTimeInMillis();
        long ended = Calendar.getInstance().getTimeInMillis();
        long runtime = 10000;
        
        JsonObjectBuilder mainObjectBuilder = Json.createObjectBuilder();
        JsonObjectBuilder detailedCrawlerStateObjectBuilder = Json.createObjectBuilder();
        JsonObjectBuilder stateObjectBuilder = Json.createObjectBuilder();
        
        stateObjectBuilder
                .add("id", id.toString())
                .add("root", rootUri.toString())
                .add("state", state)
                .add("metadataCount", metadataCount)
                .add("resourceCount", resourceCount)
                .add("count", count)
                .add("avgParseTimeMs", avgParseTimeMs)
                .add("avgPersistTimeMs", avgPersistTimeMs)
                .add("avgProcessingTimeMs", avgProcessingTimeMs)
                .add("started", started)
                .add("ended", ended)
                .add("runtime", runtime);
        
        detailedCrawlerStateObjectBuilder.add("state", stateObjectBuilder);
        mainObjectBuilder.add("detailedCrawlerState", detailedCrawlerStateObjectBuilder);
        
        JsonObject detailedCrawlerStateObject = mainObjectBuilder.build();
        
        String retrievedState = jsonTransformationHandler.getCrawlerStateFromJsonObject(detailedCrawlerStateObject);
        
        assertEquals("Retrieved state different from expected", state, retrievedState);
    }
}