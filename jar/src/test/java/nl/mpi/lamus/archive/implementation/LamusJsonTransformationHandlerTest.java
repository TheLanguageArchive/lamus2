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
        
        Collection<WorkspaceNodeReplacement> nodeReplacementCollection = new ArrayList<>();
        nodeReplacementCollection.add(firstNodeReplacement);
        nodeReplacementCollection.add(secondNodeReplacement);

        
        JsonObject resultJsonObject = jsonTransformationHandler.createJsonObjectFromNodeReplacementCollection(nodeReplacementCollection);
        
        assertNotNull("Retrieved json object should not be null", resultJsonObject);
        assertNotNull("'list' json array should not be null", resultJsonObject.getJsonArray("list"));
        JsonArray versionsJsonArray = resultJsonObject.getJsonArray("list");
        assertFalse("'versions' json array should not be empty", versionsJsonArray.isEmpty());
        assertEquals("first json object in array has different 'fromId' value than expected", firstOldNodeURI.toString(), versionsJsonArray.getJsonObject(0).getString("fromId"));
        assertEquals("first json object in array has different 'toId' value than expected", firstNewNodeURI.toString(), versionsJsonArray.getJsonObject(0).getString("toId"));
        assertEquals("second json object in array has different 'fromId' value than expected", secondOldNodeURI.toString(), versionsJsonArray.getJsonObject(1).getString("fromId"));
        assertEquals("second json object in array has different 'toId' value than expected", secondNewNodeURI.toString(), versionsJsonArray.getJsonObject(1).getString("toId"));
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
        
        Collection<WorkspaceNodeReplacement> expectedNodeReplacementCollection = new ArrayList<>();
        expectedNodeReplacementCollection.add(firstNodeReplacement);
        expectedNodeReplacementCollection.add(secondNodeReplacement);

        
        JsonObjectBuilder mainObjectBuilder = Json.createObjectBuilder();
        JsonArrayBuilder versionsArrayBuilder = Json.createArrayBuilder();

        versionsArrayBuilder.add(
                Json.createObjectBuilder()
                    .add("fromId", firstOldNodeURI.toString())
                    .add("toId", firstNewNodeURI.toString())
                    .add("status", firstReplacementStatus));
        versionsArrayBuilder.add(
                Json.createObjectBuilder()
                    .add("fromId", secondOldNodeURI.toString())
                    .add("toId", secondNewNodeURI.toString())
                    .add("status", secondReplacementStatus));
        
        mainObjectBuilder.add("list", versionsArrayBuilder);
        
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
        
        Collection<WorkspaceNodeReplacement> expectedNodeReplacementCollection = new ArrayList<>();
        expectedNodeReplacementCollection.add(firstNodeReplacement);
        
        JsonObjectBuilder mainObjectBuilder = Json.createObjectBuilder();
        JsonArrayBuilder versionsArrayBuilder = Json.createArrayBuilder();

        versionsArrayBuilder.add(
                Json.createObjectBuilder()
                    .add("fromId", firstOldNodeURI.toString())
                    .add("toId", firstNewNodeURI.toString())
                    .add("status", firstReplacementStatus));
        
        mainObjectBuilder.add("list", versionsArrayBuilder);
        
        JsonObject createdObject = mainObjectBuilder.build();
        
        
        Collection<WorkspaceNodeReplacement> resultNodeReplacementCollection = jsonTransformationHandler.createNodeReplacementCollectionFromJsonObject(createdObject);
        
        assertEquals("Node replacement collection different from expected", expectedNodeReplacementCollection, resultNodeReplacementCollection);
    }
    
    @Test
    public void createNodeReplacementCollectionFromJsonObjectStatusFailed() throws URISyntaxException {
        
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
        
        Collection<WorkspaceNodeReplacement> expectedNodeReplacementCollection = new ArrayList<>();
        expectedNodeReplacementCollection.add(firstNodeReplacement);
        expectedNodeReplacementCollection.add(secondNodeReplacement);

        
        JsonObjectBuilder mainObjectBuilder = Json.createObjectBuilder();
        JsonArrayBuilder versionsArrayBuilder = Json.createArrayBuilder();

        versionsArrayBuilder.add(
                Json.createObjectBuilder()
                    .add("fromId", firstOldNodeURI.toString())
                    .add("toId", firstNewNodeURI.toString())
                    .add("status", firstReplacementStatus)
                    .add("error", firstReplacementError));
        versionsArrayBuilder.add(
                Json.createObjectBuilder()
                    .add("fromId", secondOldNodeURI.toString())
                    .add("toId", secondNewNodeURI.toString())
                    .add("status", secondReplacementStatus)
                    .add("error", secondReplacementError));
        
        mainObjectBuilder.add("list", versionsArrayBuilder);
        
        JsonObject createdObject = mainObjectBuilder.build();
        
        
        Collection<WorkspaceNodeReplacement> resultNodeReplacementCollection = jsonTransformationHandler.createNodeReplacementCollectionFromJsonObject(createdObject);
        
        assertEquals("Node replacement collection different from expected", expectedNodeReplacementCollection, resultNodeReplacementCollection);
    }
    
    @Test
    public void createNodeReplacementCollectionFromJsonObject_NullList() {
        //TODO worth testing?
        fail("not tested yet");
    }
    
    @Test
    public void getCrawlerIdFromJsonObject() throws URISyntaxException {
        
        String crawlerId = UUID.randomUUID().toString();
        URI crawlerRootUri = new URI("hdl:" + UUID.randomUUID().toString());
        
        JsonObjectBuilder mainObjectBuilder = Json.createObjectBuilder();

        mainObjectBuilder
                .add("id", crawlerId)
                .add("root", crawlerRootUri.toString());
        
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
        
        mainObjectBuilder.add("state", stateObjectBuilder);
        
        JsonObject detailedCrawlerStateObject = mainObjectBuilder.build();
        
        String retrievedState = jsonTransformationHandler.getCrawlerStateFromJsonObject(detailedCrawlerStateObject);
        
        assertEquals("Retrieved state different from expected", state, retrievedState);
    }
}