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
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.cmdi.api.model.ResourceProxy;
import nl.mpi.util.OurURL;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
@ActiveProfiles("testing")
public class LamusNodeDataRetrieverTest {
    
    @Configuration
    @Profile("testing")
    static class LamusNodeDataRetrieverTestProperties {
        
        @Bean
        @Qualifier("orphansDirectoryBaseName")
        public String orphansDirectoryBaseName() {
            return "sessions";
        }
    }
        
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private NodeDataRetriever testNodeDataRetriever;
    
    @Mock CorpusStructureProvider mockCorpusStructureProvider;
    @Mock NodeResolver mockNodeResolver;
    @Mock MetadataAPI mockMetadataAPI;
    @Mock FileTypeHandler mockFileTypeHandler;
    @Mock TypecheckerConfiguration mockTypecheckerConfiguration;
    @Mock ArchiveFileHelper mockArchiveFileHelper;
    
    @Mock MetadataDocument mockMetadataDocument;
    @Mock Reference mockReferenceWithoutHandle;
    @Mock ResourceProxy mockReferenceWithHandle;
    
    @Mock TypecheckedResults mockTypecheckedResults;
    @Mock CorpusNode mockCorpusNode;
    @Mock WorkspaceNode mockWorkspaceNode;
    
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
        testNodeDataRetriever = new LamusNodeDataRetriever(
                mockCorpusStructureProvider, mockNodeResolver,
                mockMetadataAPI, mockFileTypeHandler,
                mockTypecheckerConfiguration, mockArchiveFileHelper);
    }
    
    @After
    public void tearDown() {
    }


    @Test
    public void getArchiveURL() throws URISyntaxException, MalformedURLException, NodeNotFoundException {
        
        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL expectedURL = new URL("file:/somewhere/in/the/archive/node.cmdi");
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusStructureProvider).getNode(nodeArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getUrl(mockCorpusNode); will(returnValue(expectedURL));
        }});
        
        URL retrievedURL = testNodeDataRetriever.getNodeArchiveURL(nodeArchiveURI);
        
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
            testNodeDataRetriever.getNodeArchiveURL(nodeArchiveURI);
            fail("should have thrown exception");
        } catch(NodeNotFoundException ex) {
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
            assertEquals("Exception node URI different from expected", nodeArchiveURI, ex.getNode());
        }
    }
    
    @Test
    public void resourceToBeTypechecked() throws MalformedURLException {
        
        final OurURL resourceURL = new OurURL("file:/some.uri/filename.txt");
        final File resourceFile = new File(resourceURL.getPath());
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileHelper).isUrlLocal(resourceURL); will(returnValue(true));
            oneOf(mockArchiveFileHelper).isFileSizeAboveTypeReCheckSizeLimit(with(equal(resourceFile))); will(returnValue(false));
        }});
        
        boolean result = testNodeDataRetriever.shouldResourceBeTypechecked(mockReferenceWithHandle, resourceURL);
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void resourceNotOnSite() throws MalformedURLException {
        
        final OurURL resourceURL = new OurURL("http://some.uri/filename.txt");
        final String resourceMimetype = "text/plain";
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileHelper).isUrlLocal(resourceURL); will(returnValue(false));
            oneOf(mockReferenceWithHandle).getMimetype(); will(returnValue(resourceMimetype));
            oneOf(mockFileTypeHandler).setValues(resourceMimetype);
        }});
        
        boolean result = testNodeDataRetriever.shouldResourceBeTypechecked(mockReferenceWithHandle, resourceURL);
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void resourceOverSizeLimitInOrphansDirectory() throws MalformedURLException {
        
        final OurURL resourceURL = new OurURL("file:/some.uri/filename.txt");
        final File resourceFile = new File(resourceURL.getPath());
        final String resourceMimetype = "text/plain";
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileHelper).isUrlLocal(resourceURL); will(returnValue(true));
            oneOf(mockArchiveFileHelper).isFileSizeAboveTypeReCheckSizeLimit(with(equal(resourceFile))); will(returnValue(true));
            oneOf(mockArchiveFileHelper).isFileInOrphansDirectory(resourceFile); will(returnValue(true));
            exactly(2).of(mockReferenceWithHandle).getMimetype(); will(returnValue(resourceMimetype));
            oneOf(mockFileTypeHandler).setValues(resourceMimetype);
        }});
        
        boolean result = testNodeDataRetriever.shouldResourceBeTypechecked(mockReferenceWithHandle, resourceURL);
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void resourceOverSizeLimitNotInOrphansDirectory() throws MalformedURLException {
        
        final OurURL resourceURL = new OurURL("file:/some.uri/filename.txt");
        final File resourceFile = new File(resourceURL.getPath());
        final String resourceMimetype = "text/plain";
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileHelper).isUrlLocal(resourceURL); will(returnValue(true));
            oneOf(mockArchiveFileHelper).isFileSizeAboveTypeReCheckSizeLimit(with(equal(resourceFile))); will(returnValue(true));
            oneOf(mockArchiveFileHelper).isFileInOrphansDirectory(resourceFile); will(returnValue(false));
        }});
        
        boolean result = testNodeDataRetriever.shouldResourceBeTypechecked(mockReferenceWithHandle, resourceURL);
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void testTriggerResourceFileCheck() throws MalformedURLException, TypeCheckerException {
        
        final OurURL resourceOurURL = new OurURL("file:/some.file.txt");
        final String fileBaseName = "some.file.txt";
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileHelper).getFileBasename(resourceOurURL.toString()); will(returnValue(fileBaseName));
            oneOf(mockFileTypeHandler).checkType(resourceOurURL, fileBaseName, null);
            oneOf(mockFileTypeHandler).getTypecheckedResults(); will(returnValue(mockTypecheckedResults));
        }});
        
        TypecheckedResults results = testNodeDataRetriever.triggerResourceFileCheck(resourceOurURL);
        assertEquals("Typechecked results different from expected", mockTypecheckedResults, results);
    }
    
    //TODO test remaining method
    //TODO Is this really necessary?
    
    
    
    @Test
    public void testTriggerFileStreamCheck() throws IOException {
        
        final String filename = "file.txt";
        
        context.checking(new Expectations() {{
            
            oneOf(mockFileTypeHandler).checkType(mockInputStream, filename, null);
            oneOf(mockFileTypeHandler).getTypecheckedResults(); will(returnValue(mockTypecheckedResults));
        }});
        
        TypecheckedResults results = testNodeDataRetriever.triggerResourceFileCheck(mockInputStream, filename);
        assertEquals("Typechecked results different from expected", mockTypecheckedResults, results);
    }

    @Test
    public void testTriggerFileStreamCheckThrowsException() throws IOException {
        
        final String filename = "file.txt";
        final IOException ioException = new IOException("some error message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockFileTypeHandler).checkType(mockInputStream, filename, null);
                will(throwException(ioException));
        }});
        
        try {
            testNodeDataRetriever.triggerResourceFileCheck(mockInputStream, filename);
            fail("An exception should have been thrown");
        } catch(IOException ex) {
            assertEquals("Exception thrown different from expected", ioException, ex);
        }
        
    }
    
    @Test
    public void checkedResourceIsArchivable() throws MalformedURLException {
        
        final TypecheckerJudgement acceptableJudgement = TypecheckerJudgement.ARCHIVABLE_LONGTERM;
        final StringBuilder message = new StringBuilder();
        final URL topWsArchiveURL = new URL("http://someServer/location");
        
        context.checking(new Expectations() {{
            
            oneOf(mockTypecheckerConfiguration).getAcceptableJudgementForLocation(topWsArchiveURL);
                will(returnValue(acceptableJudgement));
            oneOf(mockFileTypeHandler).isCheckedResourceArchivable(acceptableJudgement, message);
                will(returnValue(Boolean.TRUE));
        }});
        
        boolean result = testNodeDataRetriever.isCheckedResourceArchivable(topWsArchiveURL, message);
        
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void checkedResourceIsNotArchivable() throws MalformedURLException {
        
        final TypecheckerJudgement acceptableJudgement = TypecheckerJudgement.ARCHIVABLE_LONGTERM;
        final StringBuilder message = new StringBuilder();
        final URL topWsArchiveURL = new URL("http://someServer/location");
        
        context.checking(new Expectations() {{
            
            oneOf(mockTypecheckerConfiguration).getAcceptableJudgementForLocation(topWsArchiveURL);
                will(returnValue(acceptableJudgement));
            oneOf(mockFileTypeHandler).isCheckedResourceArchivable(acceptableJudgement, message);
                will(returnValue(Boolean.FALSE));
        }});
        
        boolean result = testNodeDataRetriever.isCheckedResourceArchivable(topWsArchiveURL, message);
        
        assertFalse("Result should be false", result);
    }
}