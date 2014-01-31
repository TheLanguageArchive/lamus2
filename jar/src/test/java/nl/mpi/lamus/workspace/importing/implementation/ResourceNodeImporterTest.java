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
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.UnknownNodeException;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.typechecking.TypecheckedResults;
import nl.mpi.lamus.exception.TypeCheckerException;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.importing.NodeImporter;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNodeLink;
import nl.mpi.metadata.api.model.Reference;
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
    @Mock WorkspaceNodeFactory mockWorkspaceNodeFactory;
    @Mock WorkspaceNodeLinkFactory mockWorkspaceNodeLinkFactory;
    
    private Workspace testWorkspace;
    
    @Mock ReferencingMetadataDocument mockReferencingMetadataDocument;
    @Mock Reference mockChildLinkWithoutHandle;
    @Mock ResourceProxy mockChildLinkWithHandle;
    
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
        nodeImporter = new ResourceNodeImporter(mockCorpusStructureProvider, mockNodeResolver, mockWorkspaceDao,
                mockNodeDataRetriever, mockWorkspaceNodeFactory, mockWorkspaceNodeLinkFactory);
//        nodeImporter.setWorkspace(testWorkspace);
        nodeImporterWithoutWorkspace = new ResourceNodeImporter(mockCorpusStructureProvider, mockNodeResolver, mockWorkspaceDao,
                mockNodeDataRetriever, mockWorkspaceNodeFactory, mockWorkspaceNodeLinkFactory);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void importResourceNodeWithHandle()
            throws URISyntaxException, MalformedURLException, UnknownNodeException, TypeCheckerException, WorkspaceImportException {

        final int parentWorkspaceNodeID = 1;
        final int childWorkspaceNodeID = 10;
        final String childNodeName = "file name label";
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE; //TODO WHat to use here?
        final String childNodeMimetype = "text/plain";
        final URI childNodeSchemaLocation = new URI("file:/some.location");
        final URI childURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000010");
        final URL childWsURL = new URL("file:/workspace/folder/childname.txt");
        final URL childOriginURL = new URL("file:/some.uri/childname.txt");
        final URL childArchiveURL = childOriginURL;
        final OurURL childOurURL = new OurURL(childArchiveURL.toString());
        final WorkspaceNodeStatus childStatus = WorkspaceNodeStatus.NODE_VIRTUAL;
        final boolean childOnSite = Boolean.TRUE;
        
        final URI parentURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000001");
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URL parentOriginURL = new URL("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURL;
        final WorkspaceNodeStatus parentStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURL, parentStatus, "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(childWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                childNodeName, "", childNodeType, childWsURL, childURI, childArchiveURL, childOriginURL, childStatus, childNodeMimetype);
        final WorkspaceNodeLink testNodeLink = new LamusWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID, childURI);
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildLinkWithHandle).getHandle(); will(returnValue(childURI));
            
            oneOf(mockCorpusStructureProvider).getNode(childURI); will(returnValue(mockCorpusNode));
            
            //TODO Maybe use the method getStream instead, so it can be passed directly to the typechecker?
            oneOf(mockNodeResolver).getUrl(mockCorpusNode); will(returnValue(childArchiveURL));
            
            
            oneOf(mockChildLinkWithHandle).getMimetype(); will(returnValue(childNodeMimetype));
            oneOf(mockNodeDataRetriever).shouldResourceBeTypechecked(mockChildLinkWithHandle, childOurURL);
                will(returnValue(true));
                
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(childOurURL);
                will(returnValue(mockTypecheckedResults));
                
            oneOf(mockNodeDataRetriever).verifyTypecheckedResults(childOurURL, mockChildLinkWithHandle, mockTypecheckedResults);
            
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(childNodeMimetype));
            
            oneOf(mockCorpusNode).getName(); will(returnValue(childNodeName));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(childOnSite));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceResourceNode(testWorkspace.getWorkspaceID(),
                    childURI, childArchiveURL, mockChildLinkWithHandle, childNodeMimetype, childNodeName, childOnSite);
                will(returnValue(testChildNode));

            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);

            oneOf (mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID, childURI);
                will(returnValue(testNodeLink));
            oneOf (mockWorkspaceDao).addWorkspaceNodeLink(testNodeLink);
        }});
        
        //TODO PID SHOULD BE COMING FROM THE CHILD LINK (HandleCarrier)
        nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, mockChildLinkWithHandle);
    }
    
    @Test
    public void importResourceNodeWithoutHandle()
            throws URISyntaxException, MalformedURLException, UnknownNodeException, TypeCheckerException, WorkspaceImportException {

        final int parentWorkspaceNodeID = 1;
        final int childWorkspaceNodeID = 10;
        final String childNodeName = "file name label";
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE; //TODO WHat to use here?
        final String childNodeMimetype = "text/plain";
        final URI childNodeSchemaLocation = new URI("file:/some.location");
        final URL childWsURL = new URL("file:/workspace/folder/childname.txt");
        final URL childOriginURL = new URL("file:/some.uri/childname.txt");
        final URL childArchiveURL = childOriginURL;
        final URI childURI = childArchiveURL.toURI();
        final OurURL childOurURL = new OurURL(childArchiveURL.toString());
        final WorkspaceNodeStatus childStatus = WorkspaceNodeStatus.NODE_VIRTUAL;
        final boolean childOnSite = Boolean.TRUE;
        
        final URI parentURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000001");
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URL parentOriginURL = new URL("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURL;
        final WorkspaceNodeStatus parentStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURL, parentStatus, "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(childWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                childNodeName, "", childNodeType, childWsURL, childURI, childArchiveURL, childOriginURL, childStatus, childNodeMimetype);
        final WorkspaceNodeLink testNodeLink = new LamusWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID, childURI);
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildLinkWithoutHandle).getURI(); will(returnValue(childURI));
            
            oneOf(mockCorpusStructureProvider).getNode(childURI); will(returnValue(mockCorpusNode));
            
            //TODO Maybe use the method getStream instead, so it can be passed directly to the typechecker?
            oneOf(mockNodeResolver).getUrl(mockCorpusNode); will(returnValue(childArchiveURL));
            
            
            oneOf(mockChildLinkWithoutHandle).getMimetype(); will(returnValue(childNodeMimetype));
            oneOf(mockNodeDataRetriever).shouldResourceBeTypechecked(mockChildLinkWithoutHandle, childOurURL);
                will(returnValue(true));
                
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(childOurURL);
                will(returnValue(mockTypecheckedResults));
                
            oneOf(mockNodeDataRetriever).verifyTypecheckedResults(childOurURL, mockChildLinkWithoutHandle, mockTypecheckedResults);
            
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(childNodeMimetype));
            
            oneOf(mockCorpusNode).getName(); will(returnValue(childNodeName));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(childOnSite));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceResourceNode(testWorkspace.getWorkspaceID(),
                    childURI, childArchiveURL, mockChildLinkWithoutHandle, childNodeMimetype, childNodeName, childOnSite);
                will(returnValue(testChildNode));

            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);

            oneOf (mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID, childURI);
                will(returnValue(testNodeLink));
            oneOf (mockWorkspaceDao).addWorkspaceNodeLink(testNodeLink);
        }});
        
        //TODO PID SHOULD BE COMING FROM THE CHILD LINK (HandleCarrier)
        nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, mockChildLinkWithoutHandle);
    }
    
    @Test
    public void workspaceWasNotSet() throws URISyntaxException, MalformedURLException, WorkspaceImportException {
        
        final int parentWorkspaceNodeID = 1;
        final URI childNodeSchemaLocation = new URI("file:/some.location");
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URL parentOriginURL = new URL("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURL;
        final URI parentURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000001");
        final URI childURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000010");
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURL, WorkspaceNodeStatus.NODE_ISCOPY, "cmdi");
        
        try {
            nodeImporterWithoutWorkspace.importNode(null, testParentNode, mockReferencingMetadataDocument, mockChildLinkWithHandle);
            fail("Should have thrown exception");
        } catch(IllegalArgumentException ex) {
            String expectedErrorMessage = "ResourceNodeImporter.importNode: workspace not set";
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertNull("Cause should be null", ex.getCause());
        }
    }
    
    @Test
    public void nodeUrlNull()
            throws URISyntaxException, MalformedURLException, UnknownNodeException, TypeCheckerException, WorkspaceImportException {

        final int parentWorkspaceNodeID = 1;
        final int childWorkspaceNodeID = 10;
        final String childNodeName = "file name label";
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE; //TODO WHat to use here?
        final String childNodeMimetype = "text/plain";
        final URI childNodeSchemaLocation = new URI("file:/some.location");
        final URI childURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000010");
        final URL childWsURL = new URL("file:/workspace/folder/childname.txt");
        final URL childOriginURL = new URL("file:/some.uri/childname.txt");
        final URL childArchiveURL = childOriginURL;
        final OurURL childOurURL = new OurURL(childArchiveURL.toString());
        
        final URI parentURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000001");
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URL parentOriginURL = new URL("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURL;
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURL, WorkspaceNodeStatus.NODE_ISCOPY, "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(childWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                childNodeName, "", childNodeType, childWsURL, childURI, childArchiveURL, childOriginURL, WorkspaceNodeStatus.NODE_CREATED, childNodeMimetype);
        final WorkspaceNodeLink testNodeLink = new LamusWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID, childURI);
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildLinkWithHandle).getHandle(); will(returnValue(childURI));
            
            oneOf(mockCorpusStructureProvider).getNode(childURI); will(returnValue(mockCorpusNode));
            
            //TODO Maybe use the method getStream instead, so it can be passed directly to the typechecker?
            oneOf(mockNodeResolver).getUrl(mockCorpusNode); will(returnValue(null));
        }});
        
        try {
            nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, mockChildLinkWithHandle);
            fail("Should have thrown exception");
        } catch(IllegalArgumentException ex) {
            String expectedErrorMessage = "ResourceNodeImporter.importNode: error getting URL for link " + childURI;
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertNull("Cause should be null", ex.getCause());
        }
        
    }
    
    @Test
    public void unknownNodeExceptionThrown()
            throws URISyntaxException, MalformedURLException, UnknownNodeException, TypeCheckerException, WorkspaceImportException {

        final int parentWorkspaceNodeID = 1;
        final int childWorkspaceNodeID = 10;
        final String childNodeName = "file name label";
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE; //TODO WHat to use here?
        final String childNodeMimetype = "text/plain";
        final URI childNodeSchemaLocation = new URI("file:/some.location");
        final URI childURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000010");
        final URL childWsURL = new URL("file:/workspace/folder/childname.txt");
        final URL childOriginURL = new URL("file:/some.uri/childname.txt");
        final URL childArchiveURL = childOriginURL;
        final OurURL childOurURL = new OurURL(childArchiveURL.toString());
        
        final URI parentURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000001");
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URL parentOriginURL = new URL("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURL;
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURL, WorkspaceNodeStatus.NODE_ISCOPY, "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(childWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                childNodeName, "", childNodeType, childWsURL, childURI, childArchiveURL, childOriginURL, WorkspaceNodeStatus.NODE_CREATED, childNodeMimetype);
        final WorkspaceNodeLink testNodeLink = new LamusWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID, childURI);
        
        final UnknownNodeException expectedException = new UnknownNodeException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildLinkWithHandle).getHandle(); will(returnValue(childURI));
            
            oneOf(mockCorpusStructureProvider).getNode(childURI); will(throwException(expectedException));
        }});
        
        try {
            nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, mockChildLinkWithHandle);
            fail("Should have thrown exception");
        } catch(WorkspaceImportException ex) {
            String expectedErrorMessage = "ResourceNodeImporter.importNode: error getting object URL for node " + childURI;
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
        
    }
    
    @Test
    public void typecheckerExceptionThrown()
            throws URISyntaxException, MalformedURLException, UnknownNodeException, TypeCheckerException, WorkspaceImportException {

        final int parentWorkspaceNodeID = 1;
        final int childWorkspaceNodeID = 10;
        final String childNodeName = "file name label";
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE; //TODO WHat to use here?
        final String childNodeMimetype = "text/plain";
        final URI childNodeSchemaLocation = new URI("file:/some.location");
        final URI childURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000010");
        final URL childWsURL = new URL("file:/workspace/folder/childname.txt");
        final URL childOriginURL = new URL("file:/some.uri/childname.txt");
        final URL childArchiveURL = childOriginURL;
        final OurURL childOurURL = new OurURL(childArchiveURL.toString());
        
        final URI parentURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000001");
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URL parentOriginURL = new URL("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURL;
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURL, WorkspaceNodeStatus.NODE_ISCOPY, "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(childWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                childNodeName, "", childNodeType, childWsURL, childURI, childArchiveURL, childOriginURL, WorkspaceNodeStatus.NODE_CREATED, childNodeMimetype);
        final WorkspaceNodeLink testNodeLink = new LamusWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID, childURI);
        
        final TypeCheckerException expectedException = new TypeCheckerException("some exception message", null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildLinkWithHandle).getHandle(); will(returnValue(childURI));
            
            oneOf(mockCorpusStructureProvider).getNode(childURI); will(returnValue(mockCorpusNode));
            
            //TODO Maybe use the method getStream instead, so it can be passed directly to the typechecker?
            oneOf(mockNodeResolver).getUrl(mockCorpusNode); will(returnValue(childArchiveURL));
            
            
            oneOf(mockChildLinkWithHandle).getMimetype(); will(returnValue(childNodeMimetype));
            oneOf(mockNodeDataRetriever).shouldResourceBeTypechecked(mockChildLinkWithHandle, childOurURL);
                will(returnValue(true));
                
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(childOurURL);
                will(throwException(expectedException));
        }});
        
        try {
            nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, mockChildLinkWithHandle);
            fail("Should have thrown exception");
        } catch(WorkspaceImportException ex) {
            String expectedErrorMessage = "ResourceNodeImporter.importNode: error during type checking";
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
        
    }
    
    
    //TODO MalformedURLException?
    
    //TODO test if/else possible branches
}
