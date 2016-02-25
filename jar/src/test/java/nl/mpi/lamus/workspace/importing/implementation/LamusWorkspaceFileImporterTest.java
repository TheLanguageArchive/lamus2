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
import java.net.URL;
import java.util.UUID;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.importing.WorkspaceFileImporter;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
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
import static org.junit.Assert.*;
import org.junit.Rule;

/**
 *
 * @author guisil
 */
public class LamusWorkspaceFileImporterTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private WorkspaceFileImporter fileImporter;
    
    @Mock WorkspaceFileHandler mockWorkspaceFileHandler;
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock MetadataAPI mockMetadataAPI;
    
    @Mock Workspace mockWorkspace;
    @Mock WorkspaceNode mockWorkspaceNode;
    @Mock MetadataDocument mockMetadataDocument;
    @Mock File mockArchiveFile;
    
    @Mock File mockNodeFile;
    @Mock StreamResult mockNodeFileStreamResult;
    
    
    public LamusWorkspaceFileImporterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        fileImporter = new LamusWorkspaceFileImporter(mockWorkspaceFileHandler, mockWorkspaceDao, mockMetadataAPI);
    }
    
    @After
    public void tearDown() {
    }


    @Test
    public void importMetadataFileToWorkspace()
            throws MalformedURLException, URISyntaxException, IOException, TransformerException, MetadataException {
        
        final int workspaceID = 10;
        final String archivePath = "file://archive/path/file.cmdi";
        final URI testURI = new URI("http://some/location/file.cmdi");
        final URL testURL = testURI.toURL();
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockArchiveFile).getAbsolutePath(); will(returnValue(archivePath));
            oneOf(mockWorkspaceNode).getWorkspaceID(); will(returnValue(workspaceID));
        
            oneOf(mockWorkspaceFileHandler).getFileForImportedWorkspaceNode(mockArchiveFile, mockWorkspaceNode); will(returnValue(mockNodeFile));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockNodeFile); will(returnValue(mockNodeFileStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockNodeFileStreamResult);
            oneOf(mockNodeFile).toURI(); will(returnValue(testURI));
            oneOf(mockWorkspaceNode).setWorkspaceURL(testURI.toURL());
            oneOf(mockWorkspaceDao).updateNodeWorkspaceURL(mockWorkspaceNode);
        }});
        
        fileImporter.importMetadataFileToWorkspace(mockArchiveFile, mockWorkspaceNode, mockMetadataDocument);
    }
    
    @Test
    public void importMetadataFileToWorkspaceThrowsIOException()
            throws URISyntaxException, MalformedURLException, IOException, TransformerException, MetadataException {
        
        final int workspaceID = 10;
        final String archivePath = "file://archive/path/file.cmdi";
        final URL archiveURL = new URL("file:/archive/some.url/file.cmdi");
        final IOException expectedException = new IOException("some exception message");
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockArchiveFile).getAbsolutePath(); will(returnValue(archivePath));
            oneOf(mockWorkspaceNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockWorkspaceFileHandler).getFileForImportedWorkspaceNode(mockArchiveFile, mockWorkspaceNode); will(returnValue(mockNodeFile));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockNodeFile); will(returnValue(mockNodeFileStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockNodeFileStreamResult);
                    will(throwException(expectedException));
        }});
        
        try {
            fileImporter.importMetadataFileToWorkspace(mockArchiveFile, mockWorkspaceNode, mockMetadataDocument);
            fail("Should have thrown exception");
        } catch(IOException ex) {
            assertEquals("Exception is different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void importMetadataFileToWorkspaceThrowsTransformerException()
            throws URISyntaxException, MalformedURLException, IOException, TransformerException, MetadataException {
        
        final int workspaceID = 10;
        final String archivePath = "file://archive/path/file.cmdi";
        final URI archiveURI = new URI("hdl:" + UUID.randomUUID().toString());
        final URL archiveURL = new URL("file:/archive/some.url/file.cmdi");
        final TransformerException expectedException = new TransformerException("some exception message");
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockArchiveFile).getAbsolutePath(); will(returnValue(archivePath));
            oneOf(mockWorkspaceNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockWorkspaceFileHandler).getFileForImportedWorkspaceNode(mockArchiveFile, mockWorkspaceNode); will(returnValue(mockNodeFile));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockNodeFile); will(returnValue(mockNodeFileStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockNodeFileStreamResult);
                    will(throwException(expectedException));
        }});
        
        try {
            fileImporter.importMetadataFileToWorkspace(mockArchiveFile, mockWorkspaceNode, mockMetadataDocument);
            fail("Should have thrown exception");
        } catch(TransformerException ex) {
            assertEquals("Exception is different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void importMetadataFileToWorkspaceThrowsMetadataException()
            throws URISyntaxException, MalformedURLException, IOException, TransformerException, MetadataException {
        
        final int workspaceID = 10;
        final String archivePath = "file://archive/path/file.cmdi";
        final URI archiveURI = new URI("hdl:" + UUID.randomUUID().toString());
        final URL archiveURL = new URL("file:/archive/some.url/file.cmdi");
        final MetadataException expectedException = new MetadataException("some exception message");
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockArchiveFile).getAbsolutePath(); will(returnValue(archivePath));
            oneOf(mockWorkspaceNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockWorkspaceFileHandler).getFileForImportedWorkspaceNode(mockArchiveFile, mockWorkspaceNode); will(returnValue(mockNodeFile));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockNodeFile); will(returnValue(mockNodeFileStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockNodeFileStreamResult);
                    will(throwException(expectedException));
        }});
        
        try {
            fileImporter.importMetadataFileToWorkspace(mockArchiveFile, mockWorkspaceNode, mockMetadataDocument);
            fail("Should have thrown exception");
        } catch(MetadataException ex) {
            assertEquals("Exception is different from expected", expectedException, ex);
        }
    }

    @Test
    public void importMetadataFileToWorkspaceThrowsIllegalArgumentException()
            throws URISyntaxException, MalformedURLException, IOException, TransformerException, MetadataException {
        
        final int workspaceID = 10;
        final String archivePath = "file://archive/path/file.cmdi";
        final URI archiveURI = new URI("hdl:" + UUID.randomUUID().toString());
        final URI wsFileURI = new URI("file.cmdi"); // a URI which is not a URL
        final URL archiveURL = new URL("file:/archive/some.url/file.cmdi");
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockArchiveFile).getAbsolutePath(); will(returnValue(archivePath));
            oneOf(mockWorkspaceNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockWorkspaceFileHandler).getFileForImportedWorkspaceNode(mockArchiveFile, mockWorkspaceNode); will(returnValue(mockNodeFile));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockNodeFile); will(returnValue(mockNodeFileStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockNodeFileStreamResult);
            oneOf(mockNodeFile).toURI(); will(returnValue(wsFileURI));
        }});
        
        try {
            fileImporter.importMetadataFileToWorkspace(mockArchiveFile, mockWorkspaceNode, mockMetadataDocument);
            fail("Should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals(IllegalArgumentException.class, ex.getClass());
            assertNotNull(ex);
        }
    }
}