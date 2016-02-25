/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.workspace.importing.implementation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.NodeNotFoundException;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.typechecking.FileTypeHandler;
import nl.mpi.lamus.typechecking.TypecheckedResults;
import nl.mpi.lamus.exception.TypeCheckerException;
import nl.mpi.lamus.typechecking.TypecheckerConfiguration;
import nl.mpi.lamus.typechecking.TypecheckerJudgement;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.cmdi.api.model.ResourceProxy;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;

/**
 *
 * @author guisil
 */
public class LamusNodeDataRetrieverTest {
        
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private NodeDataRetriever nodeDataRetriever;
    
    @Mock CorpusStructureProvider mockCorpusStructureProvider;
    @Mock NodeResolver mockNodeResolver;
    @Mock FileTypeHandler mockFileTypeHandler;
    @Mock TypecheckerConfiguration mockTypecheckerConfiguration;
    @Mock ArchiveFileHelper mockArchiveFileHelper;
    
    @Mock MetadataDocument mockMetadataDocument;
    @Mock Reference mockReferenceWithoutHandle;
    @Mock ResourceProxy mockReferenceWithHandle;
    
    @Mock TypecheckedResults mockTypecheckedResults;
    @Mock CorpusNode mockCorpusNode;
    @Mock WorkspaceNode mockWorkspaceNode;
    
    @Mock File mockFile;
    @Mock InputStream mockInputStream;
    @Mock File mockTopWsFile;
    
    public LamusNodeDataRetrieverTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        nodeDataRetriever = new LamusNodeDataRetriever(
                mockCorpusStructureProvider, mockNodeResolver,
                mockFileTypeHandler, mockTypecheckerConfiguration, mockArchiveFileHelper);
    }
    
    @After
    public void tearDown() {
    }


    @Test
    public void getArchiveURL() throws URISyntaxException, MalformedURLException, NodeNotFoundException {
        
        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL expectedURL = new URL("https://somewhere/in/the/archive/node.cmdi");
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusStructureProvider).getNode(nodeArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getUrl(mockCorpusNode); will(returnValue(expectedURL));
        }});
        
        URL retrievedURL = nodeDataRetriever.getNodeArchiveURL(nodeArchiveURI);
        
        assertEquals("Retrieved URL different from expected", expectedURL, retrievedURL);
    }
    
    @Test
    public void getArchiveURLThrowsArchiveNodeNotFoundException() throws URISyntaxException, MalformedURLException, NodeNotFoundException {
        
        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
        final String expectedMessage = "Archive node not found: " + nodeArchiveURI;
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusStructureProvider).getNode(nodeArchiveURI); will(returnValue(null));
        }});
        
        try {
            nodeDataRetriever.getNodeArchiveURL(nodeArchiveURI);
            fail("should have thrown exception");
        } catch(NodeNotFoundException ex) {
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
            assertEquals("Exception node URI different from expected", nodeArchiveURI, ex.getNode());
        }
    }
    
    @Test
    public void getLocalURL() throws URISyntaxException, MalformedURLException, NodeNotFoundException {
        
        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
        final File expectedFile = new File("/somewhere/in/the/archive/node.cmdi");
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusStructureProvider).getNode(nodeArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(expectedFile));
        }});
        
        File retrievedFile = nodeDataRetriever.getNodeLocalFile(nodeArchiveURI);
        
        assertEquals("Retrieved URL different from expected", expectedFile, retrievedFile);
    }
    
    @Test
    public void getLocalURLThrowsArchiveNodeNotFoundException() throws URISyntaxException, MalformedURLException, NodeNotFoundException {
        
        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
        final String expectedMessage = "Archive node not found: " + nodeArchiveURI;
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusStructureProvider).getNode(nodeArchiveURI); will(returnValue(null));
        }});
        
        try {
            nodeDataRetriever.getNodeLocalFile(nodeArchiveURI);
            fail("should have thrown exception");
        } catch(NodeNotFoundException ex) {
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
            assertEquals("Exception node URI different from expected", nodeArchiveURI, ex.getNode());
        }
    }
    
    @Test
    public void resource_NotArchived_Large() {
        
        final long fileLength = 1024 * 1024 + 1;
        
        context.checking(new Expectations() {{
            exactly(2).of(mockFile).length(); will(returnValue(fileLength));
        }});
        
        boolean result = nodeDataRetriever.shouldResourceBeTypechecked(mockReferenceWithHandle, mockFile, null);
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void resource_NotArchived_Small() {
        
        final long fileLength = 1024 * 1024 - 1;
        
        context.checking(new Expectations() {{
            oneOf(mockFile).length(); will(returnValue(fileLength));
        }});
        
        boolean result = nodeDataRetriever.shouldResourceBeTypechecked(mockReferenceWithHandle, mockFile, null);
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void resource_Archived_UnderSizeLimit() throws MalformedURLException {
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(Boolean.TRUE));
            oneOf(mockArchiveFileHelper).isFileSizeAboveTypeReCheckSizeLimit(mockFile); will(returnValue(Boolean.FALSE));
        }});
        
        boolean result = nodeDataRetriever.shouldResourceBeTypechecked(mockReferenceWithHandle, mockFile, mockCorpusNode);
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void resource_Archived_OnSite_OverSizeLimit() throws MalformedURLException {
        
        final URI resourceURI = URI.create(UUID.randomUUID().toString());
        final long fileLength = Long.MAX_VALUE;
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(Boolean.TRUE));
            oneOf(mockArchiveFileHelper).isFileSizeAboveTypeReCheckSizeLimit(mockFile); will(returnValue(Boolean.TRUE));
            oneOf(mockCorpusNode).getNodeURI(); will(returnValue(resourceURI));
            oneOf(mockFile).length(); will(returnValue(fileLength));
        }});
        
        boolean result = nodeDataRetriever.shouldResourceBeTypechecked(mockReferenceWithHandle, mockFile, mockCorpusNode);
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void resource_Archived_NotOnSite() {
        
        final URI resourceURI = URI.create(UUID.randomUUID().toString());
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(Boolean.FALSE));
            //logger
            oneOf(mockCorpusNode).getNodeURI(); will(returnValue(resourceURI));
        }});
        
        boolean result = nodeDataRetriever.shouldResourceBeTypechecked(mockReferenceWithHandle, mockFile, mockCorpusNode);
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void testTriggerFileStreamCheck() throws TypeCheckerException, MalformedURLException {
        
        final String filename = "file.txt";
        final URL fileUrl = new URL("file:/some/location/" + filename);
        
        context.checking(new Expectations() {{
            
            oneOf(mockFileTypeHandler).checkType(fileUrl, filename); will(returnValue(mockTypecheckedResults));
        }});
        
        TypecheckedResults results = nodeDataRetriever.triggerResourceFileCheck(fileUrl, filename);
        assertEquals("Typechecked results different from expected", mockTypecheckedResults, results);
    }

    @Test
    public void testTriggerFileStreamCheckThrowsException() throws TypeCheckerException, MalformedURLException {
        
        final String filename = "file.txt";
        final URL fileUrl = new URL("file:/some/location/" + filename);
        final TypeCheckerException expectedException = new TypeCheckerException(mockTypecheckedResults, "some error message", new IOException("some cause"));
        
        context.checking(new Expectations() {{
            
            oneOf(mockFileTypeHandler).checkType(fileUrl, filename);
                will(throwException(expectedException));
        }});
        
        try {
            nodeDataRetriever.triggerResourceFileCheck(fileUrl, filename);
            fail("An exception should have been thrown");
        } catch(TypeCheckerException ex) {
            assertEquals("Exception thrown different from expected", expectedException, ex);
        }
        
    }
    
    @Test
    public void checkedResourceIsArchivable() throws MalformedURLException {
        
        final TypecheckerJudgement acceptableJudgement = TypecheckerJudgement.ARCHIVABLE_LONGTERM;
        final StringBuilder message = new StringBuilder();
        final File topWsArchiveFile = new File("/someServer/location");
        
        context.checking(new Expectations() {{
            oneOf(mockTypecheckerConfiguration).getAcceptableJudgementForLocation(topWsArchiveFile);
                will(returnValue(acceptableJudgement));
            oneOf(mockFileTypeHandler).isCheckedResourceArchivable(mockTypecheckedResults, acceptableJudgement, message);
                will(returnValue(Boolean.TRUE));
        }});
        
        boolean result = nodeDataRetriever.isCheckedResourceArchivable(mockTypecheckedResults, topWsArchiveFile, message);
        
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void checkedResourceIsNotArchivable() throws MalformedURLException {
        
        final TypecheckerJudgement acceptableJudgement = TypecheckerJudgement.ARCHIVABLE_LONGTERM;
        final StringBuilder message = new StringBuilder();
        final File topWsArchiveFile = new File("/someServer/location");
        
        context.checking(new Expectations() {{
            oneOf(mockTypecheckerConfiguration).getAcceptableJudgementForLocation(topWsArchiveFile);
                will(returnValue(acceptableJudgement));
            oneOf(mockFileTypeHandler).isCheckedResourceArchivable(mockTypecheckedResults, acceptableJudgement, message);
                will(returnValue(Boolean.FALSE));
        }});
        
        boolean result = nodeDataRetriever.isCheckedResourceArchivable(mockTypecheckedResults, topWsArchiveFile, message);
        
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void nodeIsToBeProtected() throws URISyntaxException {
        
        final URI nodeUri = new URI(UUID.randomUUID().toString());
        
        final URI parentUri_1 = new URI(UUID.randomUUID().toString());
        final URI parentUri_2 = new URI(UUID.randomUUID().toString());
        final List<URI> parents = new ArrayList<>();
        parents.add(parentUri_1);
        parents.add(parentUri_2);
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusStructureProvider).getParentNodeURIs(nodeUri);
                will(returnValue(parents));
        }});
        
        boolean result = nodeDataRetriever.isNodeToBeProtected(nodeUri);
        
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void nodeIsNotToBeProtected() throws URISyntaxException {
        
        final URI nodeUri = new URI(UUID.randomUUID().toString());
        
        final URI parentUri_1 = new URI(UUID.randomUUID().toString());
        final List<URI> parents = new ArrayList<>();
        parents.add(parentUri_1);
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusStructureProvider).getParentNodeURIs(nodeUri);
                will(returnValue(parents));
        }});
        
        boolean result = nodeDataRetriever.isNodeToBeProtected(nodeUri);
        
        assertFalse("Result should be false", result);
    }
}