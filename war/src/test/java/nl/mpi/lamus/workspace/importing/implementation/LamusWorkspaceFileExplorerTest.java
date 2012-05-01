/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.exception.FileImporterException;
import nl.mpi.lamus.workspace.exception.FileImporterInitialisationException;
import nl.mpi.lamus.workspace.importing.FileImporter;
import nl.mpi.lamus.workspace.importing.FileImporterFactory;
import nl.mpi.lamus.workspace.importing.WorkspaceFileExplorer;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.metadata.cmdi.api.model.DataResourceProxy;
import nl.mpi.metadata.cmdi.api.model.MetadataResourceProxy;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceFileExplorerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    private WorkspaceFileExplorer fileExplorer;
    @Mock private ArchiveObjectsDB mockArchiveObjectsDB;
    @Mock private WorkspaceDao mockWorkspaceDao;
    @Mock private FileImporterFactory mockFileImporterFactory;
    @Mock private FileImporter mockFileImporter;
    @Mock private WorkspaceNode mockNodeToExplore;
    @Mock private ReferencingMetadataDocument mockNodeDocument;
    
    public LamusWorkspaceFileExplorerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        fileExplorer = new LamusWorkspaceFileExplorer(mockArchiveObjectsDB, mockWorkspaceDao, mockFileImporterFactory);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of explore method, of class LamusWorkspaceFileExplorer.
     */
    @Test
    public void exploreSuccessfully() throws URISyntaxException, FileImporterInitialisationException, FileImporterException {

        final URI metadataURI = new URI("https://testURL.mpi.nl/test.cmdi");
        final URI resourceURI = new URI("https://testURL.mpi.nl/test.jpg");
        final Reference metadataLink = new MetadataResourceProxy("1", metadataURI, "cmdi");
        final Reference resourceLink = new DataResourceProxy("2", resourceURI, "jpg");
        final Collection<Reference> testLinks = new ArrayList<Reference>();
        testLinks.add(metadataLink);
        testLinks.add(resourceLink);
        
        final String metadataLink_archiveIDStr = "MPI123#";
        final String resourceLink_archiveIDStr = "MPI456#";
        final String[] testLinksArchiveIDs = new String[testLinks.size()];
        testLinksArchiveIDs[0] = (metadataLink_archiveIDStr);
        testLinksArchiveIDs[1] = (resourceLink_archiveIDStr);
        
        final Class metadataFileImporterType = MetadataFileImporter.class;
        final Class resourceFileImporterType = ResourceFileImporter.class;
        final Class[] testLinksFileImporterTypes = new Class[testLinks.size()];
        testLinksFileImporterTypes[0] = metadataFileImporterType;
        testLinksFileImporterTypes[1] = resourceFileImporterType;
        
        context.checking(new Expectations() {{
            
            int current = 0;
            for(Reference currentLink : testLinks) {
                oneOf (mockArchiveObjectsDB).getObjectId(currentLink.getURI()); will(returnValue(testLinksArchiveIDs[current]));
                
                oneOf (mockFileImporterFactory).getFileImporterTypeForReference(currentLink.getClass()); will(returnValue(testLinksFileImporterTypes[current]));
                oneOf (mockFileImporterFactory).getNewFileImporterOfType(testLinksFileImporterTypes[current]); will(returnValue(mockFileImporter));
                
                oneOf (mockFileImporter).importFile(mockNodeToExplore, mockNodeDocument, currentLink, NodeIdUtils.TOINT(testLinksArchiveIDs[current]));
                
                current++;
            }
            
        }});

        
        fileExplorer.explore(mockNodeToExplore, mockNodeDocument, testLinks);
        
    }
}
