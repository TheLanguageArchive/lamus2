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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.UUID;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.typechecking.FileTypeHandler;
import nl.mpi.lamus.typechecking.TypecheckedResults;
import nl.mpi.lamus.workspace.exception.NodeExplorerException;
import nl.mpi.lamus.workspace.exception.NodeImporterException;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceParentNodeReferenceFactory;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.importing.NodeImporter;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNodeLink;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceParentNodeReference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.metadata.cmdi.api.model.ResourceProxy;
import nl.mpi.util.OurURL;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.*;
import org.junit.*;
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
@ContextConfiguration(//classes = {ResourceFileImporterTestProperties.class},
        loader = AnnotationConfigContextLoader.class)
@ActiveProfiles("testing")
public class ResourceNodeImporterTest {
    
    @Configuration
    @Profile("testing")
    static class ResourceNodeImporterTestProperties {
        
        @Bean
        @Qualifier("orphansDirectoryBaseName")
        public String orphansDirectoryBaseName() {
            return "sessions";
        }
    }
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private NodeImporter nodeImporter;
    private NodeImporter nodeImporterWithoutWorkspace;
    
    @Mock CorpusStructureProvider mockCorpusStructureProvider;
    @Mock NodeResolver mockNodeResolver;
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock NodeDataRetriever mockNodeDataRetriever;
    @Mock ArchiveFileHelper mockArchiveFileHelper;
    @Mock WorkspaceNodeFactory mockWorkspaceNodeFactory;
    @Mock WorkspaceParentNodeReferenceFactory mockWorkspaceParentNodeReferenceFactory;
    @Mock WorkspaceNodeLinkFactory mockWorkspaceNodeLinkFactory;
    
    private Workspace testWorkspace;
    
    @Mock ReferencingMetadataDocument mockReferencingMetadataDocument;
    @Mock ResourceProxy mockChildLink;
    @Mock FileTypeHandler mockFileTypeHandler;
    
    @Mock TypecheckedResults mockTypecheckedResults;
    @Mock CorpusNode mockCorpusNode;
    
    private final int workspaceID = 1;
    
    public ResourceNodeImporterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        testWorkspace = new LamusWorkspace(workspaceID, "someUser", -1, null, null,
                Calendar.getInstance().getTime(), null, Calendar.getInstance().getTime(), null,
                0L, 10000L, WorkspaceStatus.INITIALISING, "Workspace initialising", "archiveInfo/something");
        nodeImporter = new ResourceNodeImporter(mockCorpusStructureProvider, mockNodeResolver, mockWorkspaceDao, mockNodeDataRetriever,
                mockArchiveFileHelper, mockFileTypeHandler, mockWorkspaceNodeFactory,
                mockWorkspaceParentNodeReferenceFactory, mockWorkspaceNodeLinkFactory);
//        nodeImporter.setWorkspace(testWorkspace);
        nodeImporterWithoutWorkspace = new ResourceNodeImporter(mockCorpusStructureProvider, mockNodeResolver, mockWorkspaceDao, mockNodeDataRetriever,
                mockArchiveFileHelper, mockFileTypeHandler, mockWorkspaceNodeFactory,
                mockWorkspaceParentNodeReferenceFactory, mockWorkspaceNodeLinkFactory);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of importNode method, of class ResourceNodeImporter.
     */
    @Test
    public void importResourceNodeSuccessfully() throws Exception {

        final int parentWorkspaceNodeID = 1;
        final int childWorkspaceNodeID = 10;
        final String childNodeName = "file name label";
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE_WR; //TODO WHat to use here?
        final String childNodeMimetype = "text/plain";
        final URI childNodeSchemaLocation = new URI("file:/some.location");
        final URI childURI = new URI(UUID.randomUUID().toString());
        final URL childWsURL = new URL("file:/workspace/folder/childname.txt");
        final URL childOriginURL = new URL("file:/some.uri/childname.txt");
        final URL childArchiveURL = childOriginURL;
        final OurURL childOurURL = new OurURL(childArchiveURL.toString());
        
        final URI parentURI = new URI(UUID.randomUUID().toString());
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URL parentOriginURL = new URL("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURL;
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURL, WorkspaceNodeStatus.NODE_ISCOPY, "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(childWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                childNodeName, "", childNodeType, childWsURL, childURI, childArchiveURL, childOriginURL, WorkspaceNodeStatus.NODE_CREATED, childNodeMimetype);
        final WorkspaceParentNodeReference testParentNodeReference = new LamusWorkspaceParentNodeReference(parentWorkspaceNodeID, mockChildLink);
        final WorkspaceNodeLink testNodeLink = new LamusWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID, childURI);
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildLink).getURI(); will(returnValue(childURI));
            
            oneOf(mockCorpusStructureProvider).getNode(childURI); will(returnValue(mockCorpusNode));
            
            //TODO Maybe use the method getStream instead, so it can be passed directly to the typechecker?
            oneOf(mockNodeResolver).getUrl(mockCorpusNode); will(returnValue(childArchiveURL));
            
            
            oneOf(mockChildLink).getMimetype(); will(returnValue(childNodeMimetype));
            oneOf(mockNodeDataRetriever).shouldResourceBeTypechecked(mockChildLink, childOurURL);
                will(returnValue(true));
                
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(childOurURL);
                will(returnValue(mockTypecheckedResults));
                
            oneOf(mockNodeDataRetriever).verifyTypecheckedResults(childOurURL, mockChildLink, mockTypecheckedResults);
            
            oneOf(mockTypecheckedResults).getCheckedNodeType(); will(returnValue(childNodeType));
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(childNodeMimetype));
            
            oneOf(mockCorpusNode).getName(); will(returnValue(childNodeName));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceResourceNode(testWorkspace.getWorkspaceID(),
                    childURI, childArchiveURL, mockChildLink, childNodeType, childNodeMimetype, childNodeName);
                will(returnValue(testChildNode));

            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);

            oneOf (mockWorkspaceParentNodeReferenceFactory).getNewWorkspaceParentNodeReference(testParentNode, mockChildLink);
                will(returnValue(testParentNodeReference));
            oneOf (mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID, childURI);
                will(returnValue(testNodeLink));
            oneOf (mockWorkspaceDao).addWorkspaceNodeLink(testNodeLink);
        }});
        
        //TODO PID SHOULD BE COMING FROM THE CHILD LINK (HandleCarrier)
        nodeImporter.importNode(testWorkspace.getWorkspaceID(), testParentNode, mockReferencingMetadataDocument, mockChildLink, childURI);
        
    }
    
    @Test
    public void workspaceWasNotSet() throws URISyntaxException, MalformedURLException, NodeExplorerException {
        
        final int parentWorkspaceNodeID = 1;
        final URI childNodeSchemaLocation = new URI("file:/some.location");
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URL parentOriginURL = new URL("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURL;
        final URI parentURI = new URI(UUID.randomUUID().toString());
        final URI childURI = new URI(UUID.randomUUID().toString());
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURL, WorkspaceNodeStatus.NODE_ISCOPY, "cmdi");
        
        try {
            nodeImporterWithoutWorkspace.importNode(-1, testParentNode, mockReferencingMetadataDocument, mockChildLink, childURI);
            fail("Should have thrown NodeImporterException");
        } catch(NodeImporterException ex) {
            assertNotNull(ex);
            String expectedErrorMessage = "ResourceNodeImporter.importNode: workspace not set";
            assertEquals(expectedErrorMessage, ex.getMessage());
            assertEquals(-1, ex.getWorkspaceID());
            assertEquals(ResourceNodeImporter.class, ex.getNodeImporterType());
            assertNull(ex.getCause());
        }
    }
    
    //TODO TEST OTHER POSSIBILITIES/EXCEPTIONS
}
