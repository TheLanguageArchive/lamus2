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
import java.util.ArrayList;
import java.util.Collection;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.importing.NodeImporter;
import nl.mpi.lamus.workspace.importing.WorkspaceNodeExplorer;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.model.HandleCarrier;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.metadata.cmdi.api.model.DataResourceProxy;
import nl.mpi.metadata.cmdi.api.model.MetadataResourceProxy;
import nl.mpi.metadata.cmdi.api.model.ResourceProxy;
import nl.mpi.util.OurURL;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceNodeExplorerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private WorkspaceNodeExplorer nodeExplorer;
    @Mock private WorkspaceDao mockWorkspaceDao;
    @Mock private NodeImporterFactoryBean mockNodeImporterFactoryBean;
    @Mock private ArchiveFileHelper mockArchiveFileHelper;
    @Mock private NodeImporter mockNodeImporter;
    @Mock private WorkspaceNode mockNodeToExplore;
    @Mock private ReferencingMetadataDocument mockNodeDocument;
    
    @Mock private Workspace mockWorkspace;
    
    private final int workspaceID = 1;
    
    public LamusWorkspaceNodeExplorerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        nodeExplorer = new LamusWorkspaceNodeExplorer(mockWorkspaceDao, mockNodeImporterFactoryBean, mockArchiveFileHelper);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of explore method, of class LamusWorkspaceNodeExplorer.
     */
    @Test
    public void exploreSuccessfullyLinkWithHandle() throws Exception {

        final URI metadataURI = new URI("https://testURL.mpi.nl/test.cmdi");
        final URI resourceURI = new URI("https://testURL.mpi.nl/test.jpg");
        final ResourceProxy metadataLink = new MetadataResourceProxy("1", metadataURI, "cmdi");
        final ResourceProxy resourceLink = new DataResourceProxy("2", resourceURI, "jpg");
        
        final String metadataLinkHandle = "hdl:SOMETHING/00-0000-0000-0000-0000-1";
        metadataLink.setHandle(metadataLinkHandle);
        final String resourceLinkHandle = "hdl:SOMETHING/00-0000-0000-0000-0000-2";
        resourceLink.setHandle(resourceLinkHandle);
        
        final Collection<Reference> testLinks = new ArrayList<Reference>();
        testLinks.add(metadataLink);
        testLinks.add(resourceLink);
        
        final OurURL metadataURL = new OurURL("https://testURL.mpi.nl/test.cmdi");
        final OurURL resourceURL = new OurURL("http://testURL.mpi.nl/test.jpg");
        final OurURL[] urls = new OurURL[testLinks.size()];
        urls[0] = metadataURL;
        urls[1] = resourceURL;
        
        context.checking(new Expectations() {{
            
            int current = 0;
            for(Reference currentLink : testLinks) { //instances of HandleCarrier
                
//                oneOf(mockArchiveObjectsDB).getObjectForPID(((HandleCarrier)currentLink).getHandle()); will(returnValue(testLinksArchiveIDs[current]));
                
//                oneOf(mockArchiveObjectsDB).getObjectURLForPid(((HandleCarrier)currentLink).getHandle()); will(returnValue(urls[current]));
//                oneOf(mockArchiveObjectsDB).getObjectId(urls[current]); will(returnValue(testLinksArchiveIDs[current]));
                
                oneOf(mockNodeImporterFactoryBean).setNodeImporterTypeForReference(currentLink);
                oneOf(mockNodeImporterFactoryBean).getObject(); will(returnValue(mockNodeImporter));
//                oneOf (mockNodeImporter).setWorkspace(mockWorkspace);
                oneOf(mockNodeToExplore).getWorkspaceID(); will(returnValue(workspaceID));
                oneOf(mockNodeImporter).importNode(workspaceID, mockNodeToExplore, mockNodeDocument, currentLink, currentLink.getURI());
                
                current++;
            }
            
        }});

        
        nodeExplorer.explore(mockNodeToExplore, mockNodeDocument, testLinks);
        
    }
    
//    @Test
//    public void exploreSuccessfullyRelativeLink() throws Exception {
//
//        final URI metadataURI = new URI("https://testURL.mpi.nl/test.cmdi");
//        final URI resourceURI = new URI("test/test.jpg");
//        final Reference metadataLink = new MetadataResourceProxy("1", metadataURI, "cmdi");
//        final Reference resourceLink = new DataResourceProxy("2", resourceURI, "jpg");
//        final Collection<Reference> testLinks = new ArrayList<Reference>();
//        testLinks.add(metadataLink);
//        testLinks.add(resourceLink);
//        
//        final String metadataLink_archiveIDStr = "MPI123#";
//        final String resourceLink_archiveIDStr = "MPI456#";
//        final String[] testLinksArchiveIDs = new String[testLinks.size()];
//        testLinksArchiveIDs[0] = (metadataLink_archiveIDStr);
//        testLinksArchiveIDs[1] = (resourceLink_archiveIDStr);
//        
//        final Class metadataNodeImporterType = MetadataNodeImporter.class;
//        final Class resourceNodeImporterType = ResourceNodeImporter.class;
//        final Class[] testLinksNodeImporterTypes = new Class[testLinks.size()];
//        testLinksNodeImporterTypes[0] = metadataNodeImporterType;
//        testLinksNodeImporterTypes[1] = resourceNodeImporterType;
//        
//        context.checking(new Expectations() {{
//            
//            int current = 0;
//            for(Reference currentLink : testLinks) {
//                oneOf (mockArchiveObjectsDB).getObjectId(new OurURL(currentLink.getURI().toURL())); will(returnValue(testLinksArchiveIDs[current]));
//                
//                oneOf (mockNodeImporterFactoryBean).setNodeImporterTypeForReference(currentLink);
//                oneOf (mockNodeImporterFactoryBean).getObject(); will(returnValue(mockNodeImporter));
//                oneOf (mockNodeImporter).setWorkspace(mockWorkspace);
//                oneOf (mockNodeImporter).importNode(mockNodeToExplore, mockNodeDocument, currentLink, NodeIdUtils.TOINT(testLinksArchiveIDs[current]));
//                
//                current++;
//            }
//            
//        }});
//
//        
//        nodeExplorer.explore(mockWorkspace, mockNodeToExplore, mockNodeDocument, testLinks);
//        
//    }
}
