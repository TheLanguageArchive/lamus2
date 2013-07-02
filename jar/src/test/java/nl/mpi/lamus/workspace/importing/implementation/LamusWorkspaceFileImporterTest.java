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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.exception.WorkspaceNodeFilesystemException;
import nl.mpi.lamus.workspace.importing.WorkspaceFileImporter;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.model.MetadataDocument;
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

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceFileImporterTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private WorkspaceFileImporter fileImporter;
    
    @Mock WorkspaceFileHandler mockWorkspaceFileHandler;
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock MetadataAPI mockMetadataAPI;
    
    @Mock Workspace mockWorkspace;
    @Mock WorkspaceNode mockWorkspaceNode;
    @Mock MetadataDocument mockMetadataDocument;
    
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
    public void importMetadataFileToWorkspace() throws WorkspaceNodeFilesystemException, MalformedURLException, URISyntaxException {
        
        final URI testURI = new URI("http://some.uri");
        
        context.checking(new Expectations() {{
        
            oneOf(mockWorkspaceFileHandler).getFileForWorkspaceNode(mockWorkspaceNode); will(returnValue(mockNodeFile));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockNodeFile); will(returnValue(mockNodeFileStreamResult));
            oneOf(mockWorkspaceFileHandler).copyMetadataFile(
                    mockWorkspaceNode, mockMetadataAPI, mockMetadataDocument, mockNodeFile, mockNodeFileStreamResult);
            oneOf(mockNodeFile).toURI(); will(returnValue(testURI));
            oneOf(mockWorkspaceNode).setWorkspaceURL(testURI.toURL());
            oneOf(mockWorkspaceDao).updateNodeWorkspaceURL(mockWorkspaceNode);
        }});
        
        fileImporter.importMetadataFileToWorkspace(mockWorkspaceNode, mockMetadataDocument);
    }
    
    @Test
    public void importMetadataFileToWorkspaceThrowsException() throws URISyntaxException, WorkspaceNodeFilesystemException, MalformedURLException {
        
        final int workspaceID = 1;
        
        final URI testURI = new URI("urn:namespace-id:resource-id"); // a URI which is not a URL
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceFileHandler).getFileForWorkspaceNode(mockWorkspaceNode); will(returnValue(mockNodeFile));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockNodeFile); will(returnValue(mockNodeFileStreamResult));
            oneOf(mockWorkspaceFileHandler).copyMetadataFile(
                    mockWorkspaceNode, mockMetadataAPI, mockMetadataDocument, mockNodeFile, mockNodeFileStreamResult);
            oneOf(mockNodeFile).toURI(); will(returnValue(testURI));
            
                
            oneOf(mockNodeFile).toURI(); will(returnValue(testURI));
            oneOf(mockWorkspaceNode).getWorkspaceID(); will(returnValue(workspaceID));
        }});
        
        try {
            fileImporter.importMetadataFileToWorkspace(mockWorkspaceNode, mockMetadataDocument);
            fail("Should have thrown exception");
        } catch(WorkspaceNodeFilesystemException ex) {
            assertNotNull(ex);
            String errorMessage = "Failed to create URL from the Workspace file location: " + testURI;
            assertEquals(errorMessage, ex.getMessage());
            assertEquals(workspaceID, ex.getWorkspaceID());
            assertEquals(MalformedURLException.class, ex.getCause().getClass());
        }
    }
}