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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import nl.mpi.corpusstructure.ArchiveAccessContext;
import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.typechecking.FileTypeHandler;
import nl.mpi.lamus.typechecking.TypecheckedResults;
import nl.mpi.lamus.workspace.exception.TypeCheckerException;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
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
    
    @Mock ArchiveObjectsDB mockArchiveObjectsDB;
    @Mock MetadataAPI mockMetadataAPI;
    @Mock FileTypeHandler mockFileTypeHandler;
    @Mock ArchiveFileHelper mockArchiveFileHelper;
    
    @Mock MetadataDocument mockMetadataDocument;
    @Mock Reference mockReferenceWithoutHandle;
    @Mock ResourceProxy mockReferenceWithHandle;
    
    @Mock TypecheckedResults mockTypecheckedResults;
    
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
                mockArchiveObjectsDB, mockMetadataAPI, mockFileTypeHandler, mockArchiveFileHelper);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getArchiveNodeMetadataDocument method, of class LamusNodeDataRetriever.
     */
    @Test
    public void testGetArchiveNodeMetadataDocument() throws MalformedURLException, IOException, MetadataException {
        
        final int testChildArchiveID = 100;
        final OurURL testChildURL = new OurURL("http://some.url/node.something");

        context.checking(new Expectations() {{
            
            oneOf(mockArchiveObjectsDB).getObjectURL(NodeIdUtils.TONODEID(testChildArchiveID), ArchiveAccessContext.getFileUrlContext()); will(returnValue(testChildURL));
            oneOf(mockMetadataAPI).getMetadataDocument(testChildURL.toURL()); will(returnValue(mockMetadataDocument));
        }});
        
        MetadataDocument retrievedDocument = testNodeDataRetriever.getArchiveNodeMetadataDocument(testChildArchiveID);
        assertEquals("Retrieved metadata document different from expected", mockMetadataDocument, retrievedDocument);
    }
    
    //TODO test Exceptions
    
    
    @Test
    public void testGetResourceURLWithHandle() throws MalformedURLException {
        
        final String testHandle = "some:fakehandle";
        final OurURL expectedURL = new OurURL("http://some.fakeurl");
        
        context.checking(new Expectations() {{
            
            oneOf(mockReferenceWithHandle).getHandle(); will(returnValue(testHandle));
            oneOf(mockArchiveObjectsDB).getObjectURLForPid(testHandle); will(returnValue(expectedURL));
        }});
        
        OurURL retrievedURL = testNodeDataRetriever.getResourceURL(mockReferenceWithHandle);
        assertEquals("Retrieved URL different from expected", expectedURL, retrievedURL);
    }
    
    @Test
    public void testGetResourceNullURLWithHandle() throws MalformedURLException, URISyntaxException {
        
        final String testHandle = "some:fakehandle";
        final URI testURI = new URI("http://some.fakeurl");
        final OurURL expectedURL = new OurURL(testURI.toURL());
        
        context.checking(new Expectations() {{
            
            oneOf(mockReferenceWithHandle).getHandle(); will(returnValue(testHandle));
            oneOf(mockArchiveObjectsDB).getObjectURLForPid(testHandle); will(returnValue(null));
            oneOf(mockReferenceWithHandle).getURI(); will(returnValue(testURI));
        }});
        
        OurURL retrievedURL = testNodeDataRetriever.getResourceURL(mockReferenceWithHandle);
        assertEquals("Retrieved URL different from expected", expectedURL, retrievedURL);
    }
    
    @Test
    public void testGetResourceURLWithoutHandle() throws MalformedURLException, URISyntaxException {
        
        final URI testURI = new URI("http://some.fakeurl");
        final OurURL expectedURL = new OurURL(testURI.toURL());
        
        context.checking(new Expectations() {{
            
            oneOf(mockReferenceWithoutHandle).getURI(); will(returnValue(testURI));
        }});
        
        OurURL retrievedURL = testNodeDataRetriever.getResourceURL(mockReferenceWithoutHandle);
        assertEquals("Retrieved URL different from expected", expectedURL, retrievedURL);
    }
    
    @Test
    public void resourceToBeTypechecked() throws MalformedURLException {
        
        final int resourceNodeArchiveID = 10;
        final OurURL resourceURL = new OurURL("file:/some.uri/filename.txt");
        final File resourceFile = new File(resourceURL.getPath());
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileHelper).isUrlLocal(resourceURL); will(returnValue(true));
            oneOf(mockArchiveFileHelper).isFileSizeAboveTypeReCheckSizeLimit(with(equal(resourceFile))); will(returnValue(false));
        }});
        
        boolean result = testNodeDataRetriever.shouldResourceBeTypechecked(mockReferenceWithHandle, resourceURL, resourceNodeArchiveID);
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void resourceNotOnSite() throws MalformedURLException {
        
        final int resourceNodeArchiveID = 10;
        final OurURL resourceURL = new OurURL("http://some.uri/filename.txt");
        final String resourceMimetype = "text/plain";
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileHelper).isUrlLocal(resourceURL); will(returnValue(false));
            oneOf(mockReferenceWithHandle).getMimetype(); will(returnValue(resourceMimetype));
            oneOf(mockFileTypeHandler).setValues(resourceMimetype);
        }});
        
        boolean result = testNodeDataRetriever.shouldResourceBeTypechecked(mockReferenceWithHandle, resourceURL, resourceNodeArchiveID);
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void resourceOverSizeLimitInOrphansDirectory() throws MalformedURLException {
        
        final int resourceNodeArchiveID = 10;
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
        
        boolean result = testNodeDataRetriever.shouldResourceBeTypechecked(mockReferenceWithHandle, resourceURL, resourceNodeArchiveID);
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void resourceOverSizeLimitNotInOrphansDirectory() throws MalformedURLException {
        
        final int resourceNodeArchiveID = 10;
        final OurURL resourceURL = new OurURL("file:/some.uri/filename.txt");
        final File resourceFile = new File(resourceURL.getPath());
        final String resourceMimetype = "text/plain";
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileHelper).isUrlLocal(resourceURL); will(returnValue(true));
            oneOf(mockArchiveFileHelper).isFileSizeAboveTypeReCheckSizeLimit(with(equal(resourceFile))); will(returnValue(true));
            oneOf(mockArchiveFileHelper).isFileInOrphansDirectory(resourceFile); will(returnValue(false));
        }});
        
        boolean result = testNodeDataRetriever.shouldResourceBeTypechecked(mockReferenceWithHandle, resourceURL, resourceNodeArchiveID);
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void testGetResourceFileChecked() throws MalformedURLException, TypeCheckerException {
        
        final int resourceNodeArchiveID = 10;
        final OurURL resourceURL = new OurURL("file:/some.file.txt");
        final String fileBaseName = "some.file.txt";
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileHelper).getFileBasename(resourceURL.toString()); will(returnValue(fileBaseName));
            oneOf(mockFileTypeHandler).checkType(resourceURL, fileBaseName, null);
            oneOf(mockFileTypeHandler).getTypecheckedResults(); will(returnValue(mockTypecheckedResults));
        }});
        
        TypecheckedResults results = testNodeDataRetriever.getResourceFileChecked(
                resourceNodeArchiveID, mockReferenceWithHandle, resourceURL, resourceURL);
        assertEquals("Typechecked results different from expected", mockTypecheckedResults, results);
    }
    
    //TODO test remaining method
    
//    @Test
//    public void testVerifyTypecheckedResults() throws MalformedURLException {
//        
//        final String mimetype = "text/plain";
//        final OurURL resourceURL = new OurURL("file:/some.file.txt");
//        
//        context.checking(new Expectations() {{
//            
//            oneOf(mockReferenceWithHandle).getMimetype(); will(returnValue(mimetype));
//            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(mimetype));
//            oneOf(mockTypecheckedResults).isTypeUnspecified(); will(returnValue(false));
//        }});
//        
//        testNodeDataRetriever.verifyTypecheckedResults(resourceURL, mockReferenceWithHandle, mockTypecheckedResults);
//    }
}