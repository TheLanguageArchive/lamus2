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
import nl.mpi.lamus.archive.CorpusStructureServiceBridge;
import nl.mpi.lamus.archive.JsonTransformationHandler;
import nl.mpi.lamus.exception.VersionCreationException;
import nl.mpi.lamus.util.JerseyHelper;
import nl.mpi.lamus.workspace.model.WorkspaceNodeReplacement;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNodeReplacement;
import org.jmock.Expectations;
import static org.jmock.Expectations.returnValue;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
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
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock JsonTransformationHandler mockJsonTransformationHandler;
    @Mock JerseyHelper mockJerseyHelper;
    
    @Mock Collection<WorkspaceNodeReplacement> mockNodeReplacementsCollection;
    @Mock JsonObject mockRequestJsonObject;
    @Mock JsonObject mockResponseJsonObject;
    @Mock Collection<WorkspaceNodeReplacement> mockResponseNodeReplacementsCollection;
    
    
    private CorpusStructureServiceBridge csServiceBridge;
    
    private String corpusStructureServiceLocation = "http://some.fake/location";
    private String corpusStructureServiceVersioningPath = "version";
    private String corpusStructureServiceVersionCreationPath = "create";
    private String corpusStructureServiceCrawlerPath = "crawler";
    private String corpusStructureServiceCrawlerStartPath = "start";
    
    
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
        
        final Collection<WorkspaceNodeReplacement> expectedNodeReplacementCollection = new ArrayList<WorkspaceNodeReplacement>();
        expectedNodeReplacementCollection.add(firstNodeReplacement);
        expectedNodeReplacementCollection.add(secondNodeReplacement);
        
        context.checking(new Expectations() {{

            oneOf(mockJsonTransformationHandler).createJsonObjectFromNodeReplacementCollection(mockNodeReplacementsCollection);
                will(returnValue(mockRequestJsonObject));
            
            oneOf(mockJerseyHelper).postRequestWithJson(mockRequestJsonObject, corpusStructureServiceLocation, corpusStructureServiceVersioningPath, corpusStructureServiceVersionCreationPath);
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

            oneOf(mockJsonTransformationHandler).createJsonObjectFromNodeReplacementCollection(mockNodeReplacementsCollection);
                will(returnValue(mockRequestJsonObject));
            
            oneOf(mockJerseyHelper).postRequestWithJson(mockRequestJsonObject, corpusStructureServiceLocation, corpusStructureServiceVersioningPath, corpusStructureServiceVersionCreationPath);
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
        
        final Collection<WorkspaceNodeReplacement> expectedNodeReplacementCollection = new ArrayList<WorkspaceNodeReplacement>();
        expectedNodeReplacementCollection.add(firstNodeReplacement);
        expectedNodeReplacementCollection.add(secondNodeReplacement);
        
        final String expectedMessage = "Error during version creation. Status: " + secondReplacementStatus + "; error: " + secondReplacementError;
        
        context.checking(new Expectations() {{

            oneOf(mockJsonTransformationHandler).createJsonObjectFromNodeReplacementCollection(mockNodeReplacementsCollection);
                will(returnValue(mockRequestJsonObject));
            
            oneOf(mockJerseyHelper).postRequestWithJson(mockRequestJsonObject, corpusStructureServiceLocation, corpusStructureServiceVersioningPath, corpusStructureServiceVersionCreationPath);
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
    public void callCrawlerOk() throws URISyntaxException {
        
        final URI uriToCrawl = new URI(UUID.randomUUID().toString());
        final String crawlId = "12345667";
        
        context.checking(new Expectations() {{
            
            oneOf(mockJerseyHelper).postRequestWithUri(uriToCrawl, corpusStructureServiceLocation, corpusStructureServiceCrawlerPath, corpusStructureServiceCrawlerStartPath);
                will(returnValue(mockResponseJsonObject));
                
            oneOf(mockJsonTransformationHandler).getCrawlIdFromJsonObject(mockResponseJsonObject);
                will(returnValue(crawlId));
                
                //CHECK get ID of the crawl and get the details (succeded? failed?)
        }});
        
        
        csServiceBridge.callCrawler(uriToCrawl);
    }
    
    @Test
    public void callCrawlerFailed() {
        fail("not tested yet");
    }
}