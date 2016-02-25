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
import java.util.UUID;
import javax.json.JsonObject;
import javax.ws.rs.WebApplicationException;
import nl.mpi.lamus.archive.CorpusStructureServiceBridge;
import nl.mpi.lamus.archive.JsonTransformationHandler;
import nl.mpi.lamus.exception.CrawlerInvocationException;
import nl.mpi.lamus.exception.CrawlerStateRetrievalException;
import nl.mpi.lamus.exception.NodeUrlUpdateException;
import nl.mpi.lamus.exception.VersionCreationException;
import nl.mpi.lamus.util.JerseyHelper;
import nl.mpi.lamus.workspace.model.WorkspaceNodeReplacement;
import nl.mpi.lamus.workspace.model.WorkspaceReplacedNodeUrlUpdate;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNodeReplacement;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceReplacedNodeUrlUpdate;
import org.jmock.Expectations;
import static org.jmock.Expectations.returnValue;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;
import static org.junit.Assert.*;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author guisil
 */
public class LamusCorpusStructureServiceBridgeTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock JsonTransformationHandler mockJsonTransformationHandler;
    @Mock JerseyHelper mockJerseyHelper;
    
    @Mock Collection<WorkspaceNodeReplacement> mockNodeReplacementsCollection;
    @Mock JsonObject mockRequestJsonObject;
    @Mock JsonObject mockResponseJsonObject;
    @Mock Collection<WorkspaceReplacedNodeUrlUpdate> mockReplacedNodeUrlUpdatesCollection;
    
    @Mock WebApplicationException mockWebApplicationException;
    
    
    private CorpusStructureServiceBridge csServiceBridge;
    
    private final String corpusStructureServiceLocation = "http://some.fake/location";
    private final String corpusStructureServiceVersioningPath = "version";
    private final String corpusStructureServiceVersionCreationPath = "create";
    private final String corpusStructureServiceCrawlerPath = "crawler";
    private final String corpusStructureServiceCrawlerStartPath = "start";
    private final String corpusStructureServiceCrawlerDetailsPath = "details";
    private final String corpusStructureServiceArchiveObjectsPath = "archiveObjects";
    private final String corpusStructureServiceArchiveObjectsUpdateUrlPath = "updateUrl";
    
    
    public LamusCorpusStructureServiceBridgeTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        csServiceBridge = new LamusCorpusStructureServiceBridge(mockJsonTransformationHandler, mockJerseyHelper);
        
        ReflectionTestUtils.setField(csServiceBridge, "corpusStructureServiceLocation", corpusStructureServiceLocation);
        ReflectionTestUtils.setField(csServiceBridge, "corpusStructureServiceVersioningPath", corpusStructureServiceVersioningPath);
        ReflectionTestUtils.setField(csServiceBridge, "corpusStructureServiceVersionCreationPath", corpusStructureServiceVersionCreationPath);
        ReflectionTestUtils.setField(csServiceBridge, "corpusStructureServiceCrawlerPath", corpusStructureServiceCrawlerPath);
        ReflectionTestUtils.setField(csServiceBridge, "corpusStructureServiceCrawlerStartPath", corpusStructureServiceCrawlerStartPath);
        ReflectionTestUtils.setField(csServiceBridge, "corpusStructureServiceCrawlerDetailsPath", corpusStructureServiceCrawlerDetailsPath);
        ReflectionTestUtils.setField(csServiceBridge, "corpusStructureServiceArchiveObjectsPath", corpusStructureServiceArchiveObjectsPath);
        ReflectionTestUtils.setField(csServiceBridge, "corpusStructureServiceArchiveObjectsUpdateUrlPath", corpusStructureServiceArchiveObjectsUpdateUrlPath);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void createVersionsOk() throws URISyntaxException, VersionCreationException {
        
        URI firstOldNodeURI = new URI(UUID.randomUUID().toString());
        URI firstNewNodeURI = new URI(UUID.randomUUID().toString());
        String firstReplacementStatus = "Ok";
        WorkspaceNodeReplacement firstNodeReplacement = new LamusWorkspaceNodeReplacement(firstOldNodeURI, firstNewNodeURI, firstReplacementStatus.toUpperCase());
        
        URI secondOldNodeURI = new URI(UUID.randomUUID().toString());
        URI secondNewNodeURI = new URI(UUID.randomUUID().toString());
        String secondReplacementStatus = "oK";
        WorkspaceNodeReplacement secondNodeReplacement = new LamusWorkspaceNodeReplacement(secondOldNodeURI, secondNewNodeURI, secondReplacementStatus.toUpperCase());
        
        final Collection<WorkspaceNodeReplacement> expectedNodeReplacementCollection = new ArrayList<>();
        expectedNodeReplacementCollection.add(firstNodeReplacement);
        expectedNodeReplacementCollection.add(secondNodeReplacement);
        
        context.checking(new Expectations() {{

            oneOf(mockJsonTransformationHandler).createVersioningJsonObjectFromNodeReplacementCollection(mockNodeReplacementsCollection);
                will(returnValue(mockRequestJsonObject));
            
            oneOf(mockJerseyHelper).postRequestCorpusStructure(mockRequestJsonObject, corpusStructureServiceLocation, corpusStructureServiceVersioningPath, corpusStructureServiceVersionCreationPath);
                will(returnValue(mockResponseJsonObject));
            
            oneOf(mockJsonTransformationHandler).createNodeReplacementCollectionFromJsonObject(mockResponseJsonObject);
                will(returnValue(expectedNodeReplacementCollection));
            
        }});
        
        csServiceBridge.createVersions(mockNodeReplacementsCollection);
    }
    
    @Test
    public void createVersionsURISyntaxException() throws URISyntaxException, VersionCreationException {
        
        final String expectedMessage = "Error with a URI during version creation";
        final URISyntaxException expectedCause = new URISyntaxException("some exception message", "some good reason for the exception");
        
        context.checking(new Expectations() {{

            oneOf(mockJsonTransformationHandler).createVersioningJsonObjectFromNodeReplacementCollection(mockNodeReplacementsCollection);
                will(returnValue(mockRequestJsonObject));
            
            oneOf(mockJerseyHelper).postRequestCorpusStructure(mockRequestJsonObject, corpusStructureServiceLocation, corpusStructureServiceVersioningPath, corpusStructureServiceVersionCreationPath);
                will(returnValue(mockResponseJsonObject));
            
            oneOf(mockJsonTransformationHandler).createNodeReplacementCollectionFromJsonObject(mockResponseJsonObject);
                will(throwException(expectedCause));
            
        }});
        
        try {
            csServiceBridge.createVersions(mockNodeReplacementsCollection);
            fail("should have thrown an exception");
        } catch(VersionCreationException ex) {
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
            assertEquals("Exception cause different from expected", expectedCause, ex.getCause());
        }
    }
    
    @Test
    public void createVersionsAnotherException() throws URISyntaxException, VersionCreationException {
        
        context.checking(new Expectations() {{

            oneOf(mockJsonTransformationHandler).createVersioningJsonObjectFromNodeReplacementCollection(mockNodeReplacementsCollection);
                will(returnValue(mockRequestJsonObject));
            
            oneOf(mockJerseyHelper).postRequestCorpusStructure(mockRequestJsonObject, corpusStructureServiceLocation, corpusStructureServiceVersioningPath, corpusStructureServiceVersionCreationPath);
                will(throwException(mockWebApplicationException));
            
            ignoring(mockWebApplicationException);
        }});
        
        try {
            csServiceBridge.createVersions(mockNodeReplacementsCollection);
            fail("should have thrown an exception");
        } catch(VersionCreationException ex) {
            assertEquals("Exception cause different from expected", mockWebApplicationException, ex.getCause());
        }
    }
    
    @Test
    public void createVersionsFailed() throws URISyntaxException, VersionCreationException {
        
        URI firstOldNodeURI = new URI(UUID.randomUUID().toString());
        URI firstNewNodeURI = new URI(UUID.randomUUID().toString());
        String firstReplacementStatus = "Ok";
        WorkspaceNodeReplacement firstNodeReplacement = new LamusWorkspaceNodeReplacement(firstOldNodeURI, firstNewNodeURI, firstReplacementStatus.toUpperCase());
        
        URI secondOldNodeURI = new URI(UUID.randomUUID().toString());
        URI secondNewNodeURI = new URI(UUID.randomUUID().toString());
        String secondReplacementStatus = "FAILED";
        String secondReplacementError = "reference is invalid for ArchiveObjectDaoImpl";
        WorkspaceNodeReplacement secondNodeReplacement = new LamusWorkspaceNodeReplacement(secondOldNodeURI, secondNewNodeURI, secondReplacementStatus.toUpperCase(), secondReplacementError);
        
        final Collection<WorkspaceNodeReplacement> expectedNodeReplacementCollection = new ArrayList<>();
        expectedNodeReplacementCollection.add(firstNodeReplacement);
        expectedNodeReplacementCollection.add(secondNodeReplacement);
        
        final String expectedMessage = "Error during version creation. Status: " + secondReplacementStatus + "; error: " + secondReplacementError;
        
        context.checking(new Expectations() {{

            oneOf(mockJsonTransformationHandler).createVersioningJsonObjectFromNodeReplacementCollection(mockNodeReplacementsCollection);
                will(returnValue(mockRequestJsonObject));
            
            oneOf(mockJerseyHelper).postRequestCorpusStructure(mockRequestJsonObject, corpusStructureServiceLocation, corpusStructureServiceVersioningPath, corpusStructureServiceVersionCreationPath);
                will(returnValue(mockResponseJsonObject));
            
            oneOf(mockJsonTransformationHandler).createNodeReplacementCollectionFromJsonObject(mockResponseJsonObject);
                will(returnValue(expectedNodeReplacementCollection));
            
        }});
        
        try {
            csServiceBridge.createVersions(mockNodeReplacementsCollection);
            fail("should have thrown an exception");
        } catch(VersionCreationException ex) {
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
        }
    }
    
    @Test
    public void callCrawlerOk() throws URISyntaxException, CrawlerInvocationException {
        
        final URI uriToCrawl = new URI(UUID.randomUUID().toString());
        final String crawlerId = UUID.randomUUID().toString();
        
        context.checking(new Expectations() {{
            
            oneOf(mockJerseyHelper).postRequestCallCrawler(uriToCrawl, corpusStructureServiceLocation, corpusStructureServiceCrawlerPath, corpusStructureServiceCrawlerStartPath);
                will(returnValue(mockResponseJsonObject));
                
            oneOf(mockJsonTransformationHandler).getCrawlerIdFromJsonObject(mockResponseJsonObject);
                will(returnValue(crawlerId));
        }});
        
        String retrievedCrawlerID = csServiceBridge.callCrawler(uriToCrawl);
        
        assertEquals("Retrieved crawler ID different from expected", crawlerId, retrievedCrawlerID);
    }
    
    @Test
    public void callCrawlerFailed() throws URISyntaxException, CrawlerInvocationException {
        
        final URI uriToCrawl = new URI(UUID.randomUUID().toString());
        
        final String expectedMessage = "Error during crawler invocation for node " + uriToCrawl;
        
        context.checking(new Expectations() {{
            
            oneOf(mockJerseyHelper).postRequestCallCrawler(uriToCrawl, corpusStructureServiceLocation, corpusStructureServiceCrawlerPath, corpusStructureServiceCrawlerStartPath);
                will(throwException(mockWebApplicationException));
            
            ignoring(mockWebApplicationException);
        }});
        
        try {
            csServiceBridge.callCrawler(uriToCrawl);
            fail("should have thrown an exception");
        } catch(CrawlerInvocationException ex) {
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
            assertEquals("Exception cause different from expected", mockWebApplicationException, ex.getCause());
        }
    }
    
    @Test
    public void getCrawlerState() throws CrawlerStateRetrievalException {
        
        final String crawlerID = UUID.randomUUID().toString();
        final String expectedCrawlerState = "SUCCESS";
        
        context.checking(new Expectations() {{
            
            oneOf(mockJerseyHelper).getRequestCrawlerDetails(crawlerID, corpusStructureServiceLocation, corpusStructureServiceCrawlerPath, corpusStructureServiceCrawlerDetailsPath);
                will(returnValue(mockResponseJsonObject));
            
            oneOf(mockJsonTransformationHandler).getCrawlerStateFromJsonObject(mockResponseJsonObject);
                will(returnValue(expectedCrawlerState));
        }});
        
        String retrievedCrawlerState = csServiceBridge.getCrawlerState(crawlerID);
        
        assertEquals("Retrieved crawler state different from expected", expectedCrawlerState, retrievedCrawlerState);
    }
    
    @Test
    public void getCrawlerStateFailed() throws CrawlerStateRetrievalException {
        
        final String crawlerID = UUID.randomUUID().toString();

        final String expectedMessage = "Error during crawler state retrieval; crawlerID: " + crawlerID;
        
        context.checking(new Expectations() {{
            
            oneOf(mockJerseyHelper).getRequestCrawlerDetails(crawlerID, corpusStructureServiceLocation, corpusStructureServiceCrawlerPath, corpusStructureServiceCrawlerDetailsPath);
                will(throwException(mockWebApplicationException));
            
            ignoring(mockWebApplicationException);
        }});
        
        try {
            csServiceBridge.getCrawlerState(crawlerID);
            fail("should have thrown exception");
        } catch(CrawlerStateRetrievalException ex) {
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
            assertEquals("Exception cause different from expected", mockWebApplicationException, ex.getCause());
        }
    }
    
    @Test
    public void updateReplacedNodesUrlsOk() throws URISyntaxException, NodeUrlUpdateException {
        
        URI firstNodeUri = URI.create(UUID.randomUUID().toString());
        URI firstNodeUpdatedUrl = URI.create("https://archive/location/versions/firstnode.cmdi");
        String firstNodeUpdateStatus = "Ok";
        WorkspaceReplacedNodeUrlUpdate firstReplacedNodeUrlUpdate = new LamusWorkspaceReplacedNodeUrlUpdate(firstNodeUri, firstNodeUpdatedUrl, firstNodeUpdateStatus);
        
        URI secondNodeUri = URI.create(UUID.randomUUID().toString());
        URI secondNodUpdatedUrl = URI.create("https://archive/location/versions/secondnode.cmdi");
        String secondNodeUpdateStatus = "Ok";
        WorkspaceReplacedNodeUrlUpdate secondReplacedNodeUrlUpdate = new LamusWorkspaceReplacedNodeUrlUpdate(secondNodeUri, secondNodUpdatedUrl, secondNodeUpdateStatus);

        final Collection<WorkspaceReplacedNodeUrlUpdate> expectedReplacedNodeUrlUpdateColletion = new ArrayList<>();
        expectedReplacedNodeUrlUpdateColletion.add(firstReplacedNodeUrlUpdate);
        expectedReplacedNodeUrlUpdateColletion.add(secondReplacedNodeUrlUpdate);
        
        context.checking(new Expectations() {{
            
            oneOf(mockJsonTransformationHandler).createUrlUpdateJsonObjectFromReplacedNodeUrlUpdateCollection(mockReplacedNodeUrlUpdatesCollection);
                will(returnValue(mockRequestJsonObject));
            
            oneOf(mockJerseyHelper).postRequestCorpusStructure(mockRequestJsonObject, corpusStructureServiceLocation, corpusStructureServiceArchiveObjectsPath, corpusStructureServiceArchiveObjectsUpdateUrlPath);
                will(returnValue(mockResponseJsonObject));
            
            oneOf(mockJsonTransformationHandler).createReplacedNodeUrlUpdateCollectionFromJsonObject(mockResponseJsonObject);
                will(returnValue(expectedReplacedNodeUrlUpdateColletion));
        }});
        
        csServiceBridge.updateReplacedNodesUrls(mockReplacedNodeUrlUpdatesCollection);
    }
    
    @Test
    public void updateReplacedNodesUrlsURISyntaxException() throws URISyntaxException {
        
        final String expectedMessage = "Error with a URI during node URL update";
        final URISyntaxException expectedCause = new URISyntaxException("some exception message", "some good reason for the exception");
        
        context.checking(new Expectations() {{

            oneOf(mockJsonTransformationHandler).createUrlUpdateJsonObjectFromReplacedNodeUrlUpdateCollection(mockReplacedNodeUrlUpdatesCollection);
                will(returnValue(mockRequestJsonObject));
            
            oneOf(mockJerseyHelper).postRequestCorpusStructure(mockRequestJsonObject, corpusStructureServiceLocation, corpusStructureServiceArchiveObjectsPath, corpusStructureServiceArchiveObjectsUpdateUrlPath);
                will(returnValue(mockResponseJsonObject));
            
            oneOf(mockJsonTransformationHandler).createReplacedNodeUrlUpdateCollectionFromJsonObject(mockResponseJsonObject);
                will(throwException(expectedCause));
        }});
        
        try {
            csServiceBridge.updateReplacedNodesUrls(mockReplacedNodeUrlUpdatesCollection);
            fail("should have thrown an exception");
        } catch(NodeUrlUpdateException ex) {
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
            assertEquals("Exception cause different from expected", expectedCause, ex.getCause());
        }
    }
    
    @Test
    public void updateReplacedNodesUrlAnotherException() throws URISyntaxException {
        
        context.checking(new Expectations() {{

            oneOf(mockJsonTransformationHandler).createUrlUpdateJsonObjectFromReplacedNodeUrlUpdateCollection(mockReplacedNodeUrlUpdatesCollection);
                will(returnValue(mockRequestJsonObject));
            
            oneOf(mockJerseyHelper).postRequestCorpusStructure(mockRequestJsonObject, corpusStructureServiceLocation, corpusStructureServiceArchiveObjectsPath, corpusStructureServiceArchiveObjectsUpdateUrlPath);
                will(throwException(mockWebApplicationException));
            
            ignoring(mockWebApplicationException);
        }});
        
        try {
            csServiceBridge.updateReplacedNodesUrls(mockReplacedNodeUrlUpdatesCollection);
            fail("should have thrown an exception");
        } catch(NodeUrlUpdateException ex) {
            assertEquals("Exception cause different from expected", mockWebApplicationException, ex.getCause());
        }
    }
    
    @Test
    public void updateReplacedNodesUrlFailed() throws URISyntaxException {
        
        URI firstNodeUri = URI.create(UUID.randomUUID().toString());
        URI firstNodeUpdatedUrl = URI.create("https://archive/location/versions/firstnode.cmdi");
        String firstNodeUpdateStatus = "Ok";
        WorkspaceReplacedNodeUrlUpdate firstReplacedNodeUrlUpdate = new LamusWorkspaceReplacedNodeUrlUpdate(firstNodeUri, firstNodeUpdatedUrl, firstNodeUpdateStatus);
        
        URI secondNodeUri = URI.create(UUID.randomUUID().toString());
        URI secondNodeUpdatedUrl = URI.create("https://archive/location/versions/secondnode.cmdi");
        String secondNodeUpdateStatus = "FAILED";
        String secondNodeUpdateError = "reference is invalid for ArchiveObjectDaoImpl";
        WorkspaceReplacedNodeUrlUpdate secondReplacedNodeUrlUpdate = new LamusWorkspaceReplacedNodeUrlUpdate(secondNodeUri, secondNodeUpdatedUrl, secondNodeUpdateStatus, secondNodeUpdateError);

        final Collection<WorkspaceReplacedNodeUrlUpdate> expectedReplacedNodeUrlUpdateColletion = new ArrayList<>();
        expectedReplacedNodeUrlUpdateColletion.add(firstReplacedNodeUrlUpdate);
        expectedReplacedNodeUrlUpdateColletion.add(secondReplacedNodeUrlUpdate);
        
        final String expectedMessage = "Error during node URL update. Status: " + secondNodeUpdateStatus + "; error: " + secondNodeUpdateError;
        
        context.checking(new Expectations() {{

            oneOf(mockJsonTransformationHandler).createUrlUpdateJsonObjectFromReplacedNodeUrlUpdateCollection(mockReplacedNodeUrlUpdatesCollection);
                will(returnValue(mockRequestJsonObject));
            
            oneOf(mockJerseyHelper).postRequestCorpusStructure(mockRequestJsonObject, corpusStructureServiceLocation, corpusStructureServiceArchiveObjectsPath, corpusStructureServiceArchiveObjectsUpdateUrlPath);
                will(returnValue(mockResponseJsonObject));
            
            oneOf(mockJsonTransformationHandler).createReplacedNodeUrlUpdateCollectionFromJsonObject(mockResponseJsonObject);
                will(returnValue(expectedReplacedNodeUrlUpdateColletion));
        }});
        
        try {
            csServiceBridge.updateReplacedNodesUrls(mockReplacedNodeUrlUpdatesCollection);
            fail("should have thrown an exception");
        } catch(NodeUrlUpdateException ex) {
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
        }
    }
}