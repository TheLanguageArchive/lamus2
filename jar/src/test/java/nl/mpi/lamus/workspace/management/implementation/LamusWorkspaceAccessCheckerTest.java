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
package nl.mpi.lamus.workspace.management.implementation;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.CorpusNodeType;
import nl.mpi.archiving.corpusstructure.core.NodeNotFoundException;
import nl.mpi.archiving.corpusstructure.core.database.dao.ArchiveObjectDao;
import nl.mpi.archiving.corpusstructure.core.database.pojo.ArchiveObject;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.CorpusStructureAccessChecker;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.ExternalNodeException;
import nl.mpi.lamus.exception.LockedNodeException;
import nl.mpi.lamus.exception.NodeAccessException;
import nl.mpi.lamus.exception.UnauthorizedNodeException;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.workspace.management.WorkspaceAccessChecker;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.*;
import org.junit.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceAccessCheckerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    
    private WorkspaceAccessChecker nodeAccessChecker;
    @Mock private CorpusStructureProvider mockCorpusStructureProvider;
    @Mock private ArchiveObjectDao mockArchiveObjectDao;
    @Mock private WorkspaceDao mockWorkspaceDao;
    @Mock private CorpusStructureAccessChecker mockCorpusStructureAccessChecker;
    
    @Mock private CorpusNode mockCorpusNode;
    @Mock private ArchiveObject mockArchiveObject;
    @Mock private ArchiveObject mockArchiveObject_Descendant_1;
    @Mock private ArchiveObject mockArchiveObject_Descendant_2;
    
    @Mock private WorkspaceNode mockWorkspaceNode1;
    @Mock private WorkspaceNode mockWorkspaceNode2;
    
    public LamusWorkspaceAccessCheckerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        nodeAccessChecker = new LamusWorkspaceAccessChecker(
                mockCorpusStructureProvider, mockArchiveObjectDao,
                mockWorkspaceDao, mockCorpusStructureAccessChecker);
    }
    
    @After
    public void tearDown() {
    }
    
    /**
     * Test of ensureWorkspaceCanBeCreated method, of class NodeAccessCheckerImpl.
     */
    @Test
    public void cannotCreateWorkspaceIfNodeIsUnknown() throws URISyntaxException, NodeAccessException {
        
        final String userID = "someUser";
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final String expectedMessage = "Archive node not found: " + archiveNodeURI;
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusStructureProvider).getNode(archiveNodeURI); will(returnValue(null));
        }});
        
        try {
            nodeAccessChecker.ensureWorkspaceCanBeCreated(userID, archiveNodeURI);
            fail("should have thrown an exception");
        } catch(NodeNotFoundException ex) {
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
            assertEquals("Exception node URI different from expected", archiveNodeURI, ex.getNode());
        }
    }
 
    @Test
    public void cannotCreateWorkspaceIfNodeIsExternal() throws URISyntaxException, NodeNotFoundException {
        
        final String userID = "someUser";
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final String expectedMessage = "Node with URI '" + archiveNodeURI.toString() + "' is external";
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusStructureProvider).getNode(archiveNodeURI); will(returnValue(mockCorpusNode));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(Boolean.FALSE));
        }});
        
        try {
            nodeAccessChecker.ensureWorkspaceCanBeCreated(userID, archiveNodeURI);
            fail("should have thrown exception");
        } catch(NodeAccessException ex) {
            assertTrue("Exception has a type different from expected", ex instanceof ExternalNodeException);
            assertEquals("Node URI different from expected", archiveNodeURI, ex.getNodeURI());
            assertEquals("Message different from expected", expectedMessage, ex.getMessage());
        }
    }
    
    @Test
    public void cannotCreateWorkspaceIfNodeIsNotMetadata() throws URISyntaxException, NodeAccessException, NodeNotFoundException {
        
        final String userID = "someUser";
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final String expectedMessage = "Selected node should be Metadata: " + archiveNodeURI;
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusStructureProvider).getNode(archiveNodeURI); will(returnValue(mockCorpusNode));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(Boolean.TRUE));
            exactly(2).of(mockCorpusNode).getType(); will(returnValue(CorpusNodeType.RESOURCE_AUDIO));
        }});
        
        try {
            nodeAccessChecker.ensureWorkspaceCanBeCreated(userID, archiveNodeURI);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Message different from expected", expectedMessage, ex.getMessage());
        }
    }
    
    @Test
    public void cannotCreateWorkspaceIfNodeIsNotFound() throws URISyntaxException, NodeNotFoundException, NodeAccessException {
        
        final String userID = "someUser";
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final NodeNotFoundException expectedException = new NodeNotFoundException(archiveNodeURI);
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusStructureProvider).getNode(archiveNodeURI); will(returnValue(mockCorpusNode));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(Boolean.TRUE));
            exactly(2).of(mockCorpusNode).getType(); will(returnValue(CorpusNodeType.METADATA));
            oneOf(mockCorpusStructureAccessChecker).hasWriteAccess(userID, archiveNodeURI); will(throwException(expectedException));
        }});
        
        try {
            nodeAccessChecker.ensureWorkspaceCanBeCreated(userID, archiveNodeURI);
            fail("should have thrown exception");
        } catch(NodeNotFoundException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void cannotCreateWorkspaceIfNodeIsNotAccessibleToUser() throws URISyntaxException, NodeNotFoundException {
        
        final String userID = "someUser";
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final String expectedMessage = "Node with URI '" + archiveNodeURI + "' is not writeable by user " + userID;
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusStructureProvider).getNode(archiveNodeURI); will(returnValue(mockCorpusNode));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(Boolean.TRUE));
            exactly(2).of(mockCorpusNode).getType(); will(returnValue(CorpusNodeType.METADATA));
            oneOf(mockCorpusStructureAccessChecker).hasWriteAccess(userID, archiveNodeURI); will(returnValue(Boolean.FALSE));
        }});
        
        try {
            nodeAccessChecker.ensureWorkspaceCanBeCreated(userID, archiveNodeURI);
            fail("should have thrown exception");
        } catch(NodeAccessException ex) {
            assertTrue("Exception has a type different from expected", ex instanceof UnauthorizedNodeException);
            assertEquals("User ID different from expected", userID, ((UnauthorizedNodeException) ex).getUserID());
            assertEquals("Node URI different from expected", archiveNodeURI, ex.getNodeURI());
            assertEquals("Message different from expected", expectedMessage, ex.getMessage());
        }
    }
    
    @Test
    public void cannotCreateWorkspaceIfNodeIsLocked() throws URISyntaxException, NodeNotFoundException {
        
        final String userID = "someUser";
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final int workspaceID = 10;
        final String expectedMessage = "Node with URI '" + archiveNodeURI + "' is already locked by workspace " + workspaceID;
        
        final Collection<WorkspaceNode> lockedNodes = new ArrayList<>();
        lockedNodes.add(mockWorkspaceNode1);
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusStructureProvider).getNode(archiveNodeURI); will(returnValue(mockCorpusNode));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(Boolean.TRUE));
            exactly(2).of(mockCorpusNode).getType(); will(returnValue(CorpusNodeType.METADATA));
            oneOf(mockCorpusStructureAccessChecker).hasWriteAccess(userID, archiveNodeURI); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceDao).isNodeLocked(archiveNodeURI); will(returnValue(Boolean.TRUE));
            
            oneOf(mockWorkspaceDao).getWorkspaceNodeByArchiveURI(archiveNodeURI); will(returnValue(lockedNodes));
            oneOf(mockWorkspaceNode1).getWorkspaceID(); will(returnValue(workspaceID));
        }});
        
        try {
            nodeAccessChecker.ensureWorkspaceCanBeCreated(userID, archiveNodeURI);
            fail("should have thrown exception");
        } catch(NodeAccessException ex) {
            assertTrue("Exception has a type different from expected", ex instanceof LockedNodeException);
            assertEquals("Workspace ID different from expected", workspaceID, ((LockedNodeException) ex).getWorkspaceID());
            assertEquals("Node URI different from expected", archiveNodeURI, ex.getNodeURI());
            assertEquals("Message different from expected", expectedMessage, ex.getMessage());
        }
    }
    
    @Test
    public void cannotCreateWorkspaceIfNodeIsLockedMultipleTimes() throws URISyntaxException, NodeNotFoundException {
        
        final String userID = "someUser";
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final String expectedMessage = "Node with URI '" + archiveNodeURI + "' is already locked by multiple workspaces";
        
        final Collection<WorkspaceNode> lockedNodes = new ArrayList<>();
        lockedNodes.add(mockWorkspaceNode1);
        lockedNodes.add(mockWorkspaceNode2);
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusStructureProvider).getNode(archiveNodeURI); will(returnValue(mockCorpusNode));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(Boolean.TRUE));
            exactly(2).of(mockCorpusNode).getType(); will(returnValue(CorpusNodeType.METADATA));
            oneOf(mockCorpusStructureAccessChecker).hasWriteAccess(userID, archiveNodeURI); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceDao).isNodeLocked(archiveNodeURI); will(returnValue(Boolean.TRUE));
            
            oneOf(mockWorkspaceDao).getWorkspaceNodeByArchiveURI(archiveNodeURI); will(returnValue(lockedNodes));
        }});
        
        try {
            nodeAccessChecker.ensureWorkspaceCanBeCreated(userID, archiveNodeURI);
            fail("should have thrown exception");
        } catch(NodeAccessException ex) {
            assertTrue("Exception has a type different from expected", ex instanceof LockedNodeException);
            assertEquals("Workspace ID different from expected", -1, ((LockedNodeException) ex).getWorkspaceID());
            assertEquals("Node URI different from expected", archiveNodeURI, ex.getNodeURI());
            assertEquals("Message different from expected", expectedMessage, ex.getMessage());
        }
    }
    
    @Test
    public void cannotCreateWorkspaceIfDescendantNodeIsNotAccessibleToUser() throws URISyntaxException, NodeAccessException, NodeNotFoundException {
        
        final String userID = "someUser";
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final long archiveNodeID = 10;
        
        final Collection<ArchiveObject> descendants = new ArrayList<>();
        descendants.add(mockArchiveObject_Descendant_1);
        
        final URI archiveNodeURI_Descendant_1 = new URI(UUID.randomUUID().toString());
        final String expectedMessage = "Node with URI '" + archiveNodeURI_Descendant_1 + "' is not writeable by user " + userID;
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusStructureProvider).getNode(archiveNodeURI); will(returnValue(mockCorpusNode));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(Boolean.TRUE));
            exactly(2).of(mockCorpusNode).getType(); will(returnValue(CorpusNodeType.METADATA));
            oneOf(mockCorpusStructureAccessChecker).hasWriteAccess(userID, archiveNodeURI); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceDao).isNodeLocked(archiveNodeURI); will(returnValue(Boolean.FALSE));
            
            oneOf(mockArchiveObjectDao).select(archiveNodeURI); will(returnValue(mockArchiveObject));
            oneOf(mockArchiveObject).getId(); will(returnValue(archiveNodeID));
            oneOf(mockArchiveObjectDao).selectDescendants(archiveNodeID); will(returnValue(descendants));
            
            //loop
            oneOf(mockArchiveObject_Descendant_1).isOnsite(); will(returnValue(Boolean.TRUE));
            oneOf(mockArchiveObject_Descendant_1).getCanonicalUri(); will(returnValue(archiveNodeURI_Descendant_1));
            oneOf(mockCorpusStructureAccessChecker).hasWriteAccess(userID, archiveNodeURI_Descendant_1); will(returnValue(Boolean.FALSE));
        }});
        
        try {
            nodeAccessChecker.ensureWorkspaceCanBeCreated(userID, archiveNodeURI);
            fail("should have thrown an exception");
        } catch(NodeAccessException ex) {
            assertTrue("Exception has a type different from expected", ex instanceof UnauthorizedNodeException);
            assertEquals("User ID different from expected", userID, ((UnauthorizedNodeException) ex).getUserID());
            assertEquals("Node URI different from expected", archiveNodeURI_Descendant_1, ex.getNodeURI());
            assertEquals("Message different from expected", expectedMessage, ex.getMessage());
        }
    }
    
    @Test
    public void cannotCreateWorkspaceIfDescendantNodeIsLocked() throws URISyntaxException, NodeNotFoundException {
        
        final String userID = "someUser";
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final long archiveNodeID = 10;
        
        final Collection<ArchiveObject> descendants = new ArrayList<>();
        descendants.add(mockArchiveObject_Descendant_1);
        
        final URI archiveNodeURI_Descendant_1 = new URI(UUID.randomUUID().toString());
        final int workspaceID = 10;
        final String expectedMessage = "Node with URI '" + archiveNodeURI_Descendant_1 + "' is already locked by workspace " + workspaceID;
        
        final Collection<WorkspaceNode> lockedNodes = new ArrayList<>();
        lockedNodes.add(mockWorkspaceNode1);
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusStructureProvider).getNode(archiveNodeURI); will(returnValue(mockCorpusNode));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(Boolean.TRUE));
            exactly(2).of(mockCorpusNode).getType(); will(returnValue(CorpusNodeType.METADATA));
            oneOf(mockCorpusStructureAccessChecker).hasWriteAccess(userID, archiveNodeURI); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceDao).isNodeLocked(archiveNodeURI); will(returnValue(Boolean.FALSE));
            
            oneOf(mockArchiveObjectDao).select(archiveNodeURI); will(returnValue(mockArchiveObject));
            oneOf(mockArchiveObject).getId(); will(returnValue(archiveNodeID));
            oneOf(mockArchiveObjectDao).selectDescendants(archiveNodeID); will(returnValue(descendants));
            
            //loop
            oneOf(mockArchiveObject_Descendant_1).isOnsite(); will(returnValue(Boolean.TRUE));
            oneOf(mockArchiveObject_Descendant_1).getCanonicalUri(); will(returnValue(archiveNodeURI_Descendant_1));
            oneOf(mockCorpusStructureAccessChecker).hasWriteAccess(userID, archiveNodeURI_Descendant_1); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceDao).isNodeLocked(archiveNodeURI_Descendant_1); will(returnValue(Boolean.TRUE));
            
            oneOf(mockWorkspaceDao).getWorkspaceNodeByArchiveURI(archiveNodeURI_Descendant_1); will(returnValue(lockedNodes));
            oneOf(mockWorkspaceNode1).getWorkspaceID(); will(returnValue(workspaceID));
        }});
        
        try {
            nodeAccessChecker.ensureWorkspaceCanBeCreated(userID, archiveNodeURI);
            fail("should have thrown an exception");
        } catch(NodeAccessException ex) {
            assertTrue("Exception has a type different from expected", ex instanceof LockedNodeException);
            assertEquals("Workspace ID different from expected", workspaceID, ((LockedNodeException) ex).getWorkspaceID());
            assertEquals("Node URI different from expected", archiveNodeURI_Descendant_1, ex.getNodeURI());
            assertEquals("Message different from expected", expectedMessage, ex.getMessage());
        }
    }
    
    @Test
    public void cannotCreateWorkspaceIfSecondDescendantNodeIsLocked() throws URISyntaxException, NodeNotFoundException {
        
        final String userID = "someUser";
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final long archiveNodeID = 10;
        
        final Collection<ArchiveObject> descendants = new ArrayList<>();
        descendants.add(mockArchiveObject_Descendant_1);
        descendants.add(mockArchiveObject_Descendant_2);
        
        final URI archiveNodeURI_Descendant_1 = new URI(UUID.randomUUID().toString());
        final URI archiveNodeURI_Descendant_2 = new URI(UUID.randomUUID().toString());
        final int workspaceID = 10;
        final String expectedMessage = "Node with URI '" + archiveNodeURI_Descendant_2 + "' is already locked by workspace " + workspaceID;
        
        final Collection<WorkspaceNode> lockedNodes = new ArrayList<>();
        lockedNodes.add(mockWorkspaceNode2);
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusStructureProvider).getNode(archiveNodeURI); will(returnValue(mockCorpusNode));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(Boolean.TRUE));
            exactly(2).of(mockCorpusNode).getType(); will(returnValue(CorpusNodeType.METADATA));
            oneOf(mockCorpusStructureAccessChecker).hasWriteAccess(userID, archiveNodeURI); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceDao).isNodeLocked(archiveNodeURI); will(returnValue(Boolean.FALSE));
            
            oneOf(mockArchiveObjectDao).select(archiveNodeURI); will(returnValue(mockArchiveObject));
            oneOf(mockArchiveObject).getId(); will(returnValue(archiveNodeID));
            oneOf(mockArchiveObjectDao).selectDescendants(archiveNodeID); will(returnValue(descendants));
            
            //loop
            oneOf(mockArchiveObject_Descendant_1).isOnsite(); will(returnValue(Boolean.TRUE));
            oneOf(mockArchiveObject_Descendant_1).getCanonicalUri(); will(returnValue(archiveNodeURI_Descendant_1));
            oneOf(mockCorpusStructureAccessChecker).hasWriteAccess(userID, archiveNodeURI_Descendant_1); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceDao).isNodeLocked(archiveNodeURI_Descendant_1); will(returnValue(Boolean.FALSE));
            
            oneOf(mockArchiveObject_Descendant_2).isOnsite(); will(returnValue(Boolean.TRUE));
            oneOf(mockArchiveObject_Descendant_2).getCanonicalUri(); will(returnValue(archiveNodeURI_Descendant_2));
            oneOf(mockCorpusStructureAccessChecker).hasWriteAccess(userID, archiveNodeURI_Descendant_2); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceDao).isNodeLocked(archiveNodeURI_Descendant_2); will(returnValue(Boolean.TRUE));
            
            oneOf(mockWorkspaceDao).getWorkspaceNodeByArchiveURI(archiveNodeURI_Descendant_2); will(returnValue(lockedNodes));
            oneOf(mockWorkspaceNode2).getWorkspaceID(); will(returnValue(workspaceID));
        }});
        
        try {
            nodeAccessChecker.ensureWorkspaceCanBeCreated(userID, archiveNodeURI);
            fail("should have thrown an exception");
        } catch(NodeAccessException ex) {
            assertTrue("Exception has a type different from expected", ex instanceof LockedNodeException);
            assertEquals("Workspace ID different from expected", workspaceID, ((LockedNodeException) ex).getWorkspaceID());
            assertEquals("Node URI different from expected", archiveNodeURI_Descendant_2, ex.getNodeURI());
            assertEquals("Message different from expected", expectedMessage, ex.getMessage());
        }
    }
    
    @Test
    public void cannotCreateWorkspaceIfDescendantNodeIsLockedMultipleTimes() throws URISyntaxException, NodeNotFoundException {
        
        final String userID = "someUser";
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final long archiveNodeID = 10;
        
        final Collection<ArchiveObject> descendants = new ArrayList<>();
        descendants.add(mockArchiveObject_Descendant_1);
        
        final URI archiveNodeURI_Descendant_1 = new URI(UUID.randomUUID().toString());
        final String expectedMessage = "Node with URI '" + archiveNodeURI_Descendant_1 + "' is already locked by multiple workspaces";
        
        final Collection<WorkspaceNode> lockedNodes = new ArrayList<>();
        lockedNodes.add(mockWorkspaceNode1);
        lockedNodes.add(mockWorkspaceNode2);
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusStructureProvider).getNode(archiveNodeURI); will(returnValue(mockCorpusNode));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(Boolean.TRUE));
            exactly(2).of(mockCorpusNode).getType(); will(returnValue(CorpusNodeType.METADATA));
            oneOf(mockCorpusStructureAccessChecker).hasWriteAccess(userID, archiveNodeURI); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceDao).isNodeLocked(archiveNodeURI); will(returnValue(Boolean.FALSE));
            
            oneOf(mockArchiveObjectDao).select(archiveNodeURI); will(returnValue(mockArchiveObject));
            oneOf(mockArchiveObject).getId(); will(returnValue(archiveNodeID));
            oneOf(mockArchiveObjectDao).selectDescendants(archiveNodeID); will(returnValue(descendants));
            
            //loop
            oneOf(mockArchiveObject_Descendant_1).isOnsite(); will(returnValue(Boolean.TRUE));
            oneOf(mockArchiveObject_Descendant_1).getCanonicalUri(); will(returnValue(archiveNodeURI_Descendant_1));
            oneOf(mockCorpusStructureAccessChecker).hasWriteAccess(userID, archiveNodeURI_Descendant_1); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceDao).isNodeLocked(archiveNodeURI_Descendant_1); will(returnValue(Boolean.TRUE));
            
            oneOf(mockWorkspaceDao).getWorkspaceNodeByArchiveURI(archiveNodeURI_Descendant_1); will(returnValue(lockedNodes));
        }});
        
        try {
            nodeAccessChecker.ensureWorkspaceCanBeCreated(userID, archiveNodeURI);
            fail("should have thrown an exception");
        } catch(NodeAccessException ex) {
            assertTrue("Exception has a type different from expected", ex instanceof LockedNodeException);
            assertEquals("Workspace ID different from expected", -1, ((LockedNodeException) ex).getWorkspaceID());
            assertEquals("Node URI different from expected", archiveNodeURI_Descendant_1, ex.getNodeURI());
            assertEquals("Message different from expected", expectedMessage, ex.getMessage());
        }
    }
    
    @Test
    public void canCreateWorkspaceIfNodeWithoutDescendantsIsNotLocked() throws URISyntaxException, NodeAccessException, NodeNotFoundException {
        
        final String userID = "someUser";
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final long archiveNodeID = 10;
        
        final Collection<ArchiveObject> noDescendants = new ArrayList<>();
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusStructureProvider).getNode(archiveNodeURI); will(returnValue(mockCorpusNode));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(Boolean.TRUE));
            exactly(2).of(mockCorpusNode).getType(); will(returnValue(CorpusNodeType.METADATA));
            oneOf(mockCorpusStructureAccessChecker).hasWriteAccess(userID, archiveNodeURI); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceDao).isNodeLocked(archiveNodeURI); will(returnValue(Boolean.FALSE));
            
            oneOf(mockArchiveObjectDao).select(archiveNodeURI); will(returnValue(mockArchiveObject));
            oneOf(mockArchiveObject).getId(); will(returnValue(archiveNodeID));
            oneOf(mockArchiveObjectDao).selectDescendants(archiveNodeID); will(returnValue(noDescendants));
        }});
        
        nodeAccessChecker.ensureWorkspaceCanBeCreated(userID, archiveNodeURI);
    }
    
    @Test
    public void canCreateWorkspaceIfNodeAndDescendantsAreNotLocked_includingExternalDescendant() throws URISyntaxException, NodeAccessException, NodeNotFoundException {
        
        final String userID = "someUser";
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final long archiveNodeID = 10;
        
        final Collection<ArchiveObject> descendants = new ArrayList<>();
        descendants.add(mockArchiveObject_Descendant_1);
        descendants.add(mockArchiveObject_Descendant_2);
        
        final URI archiveNodeURI_Descendant_2 = new URI(UUID.randomUUID().toString());
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusStructureProvider).getNode(archiveNodeURI); will(returnValue(mockCorpusNode));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(Boolean.TRUE));
            exactly(2).of(mockCorpusNode).getType(); will(returnValue(CorpusNodeType.METADATA));
            oneOf(mockCorpusStructureAccessChecker).hasWriteAccess(userID, archiveNodeURI); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceDao).isNodeLocked(archiveNodeURI); will(returnValue(Boolean.FALSE));
            
            oneOf(mockArchiveObjectDao).select(archiveNodeURI); will(returnValue(mockArchiveObject));
            oneOf(mockArchiveObject).getId(); will(returnValue(archiveNodeID));
            oneOf(mockArchiveObjectDao).selectDescendants(archiveNodeID); will(returnValue(descendants));
            
            //loop
            oneOf(mockArchiveObject_Descendant_1).isOnsite(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockArchiveObject_Descendant_2).isOnsite(); will(returnValue(Boolean.TRUE));
            oneOf(mockArchiveObject_Descendant_2).getCanonicalUri(); will(returnValue(archiveNodeURI_Descendant_2));
            oneOf(mockCorpusStructureAccessChecker).hasWriteAccess(userID, archiveNodeURI_Descendant_2); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceDao).isNodeLocked(archiveNodeURI_Descendant_2); will(returnValue(Boolean.FALSE));
        }});
        
        nodeAccessChecker.ensureWorkspaceCanBeCreated(userID, archiveNodeURI);
    }
    
    @Test
    public void hasAccessToWorkspaceIfUserIsTheSame()
            throws URISyntaxException, MalformedURLException, WorkspaceNotFoundException, WorkspaceAccessException {
        
        final int workspaceID = 1;
        final String userID = "someUser";
        final int topNodeID = 1;
        final URI topNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL topNodeArchiveURL = new URL("file:/archive/folder/someNode.cmdi");
        final Date startDate = Calendar.getInstance().getTime();
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final WorkspaceStatus status = WorkspaceStatus.INITIALISED;
        final String message = "workspace is in good shape";
        final String crawlerID = "";
        final Workspace testWorkspace = new LamusWorkspace(workspaceID, userID, topNodeID, topNodeArchiveURI, topNodeArchiveURL,
                startDate, null, startDate, null, usedStorageSpace, maxStorageSpace, status, message, crawlerID);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(testWorkspace));
        }});
        
        nodeAccessChecker.ensureUserHasAccessToWorkspace(userID, workspaceID);
    }
    
    @Test
    public void throwsExceptionIfUserIsNotTheSame() throws URISyntaxException, MalformedURLException, WorkspaceNotFoundException {
        
        final int workspaceID = 1;
        final String userID = "someUser";
        final int topNodeID = 1;
        final URI topNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL topNodeArchiveURL = new URL("file:/archive/folder/someNode.cmdi");
        final Date startDate = Calendar.getInstance().getTime();
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final WorkspaceStatus status = WorkspaceStatus.INITIALISED;
        final String message = "workspace is in good shape";
        final String crawlerID = "";
        final Workspace testWorkspace = new LamusWorkspace(workspaceID, userID, topNodeID, topNodeArchiveURI, topNodeArchiveURL,
                startDate, null, startDate, null, usedStorageSpace, maxStorageSpace, status, message, crawlerID);
        
        
        final String otherUserID = "someOtherUser";
        final String expectedErrorMessage = "User with ID " + otherUserID + " does not have access to workspace with ID " + workspaceID;
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(testWorkspace));
        }});
        
        try {
            nodeAccessChecker.ensureUserHasAccessToWorkspace(otherUserID, workspaceID);
            fail("exception should have been thrown");
        } catch(WorkspaceAccessException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertNull("Cause should be null", ex.getCause());
        }
    }
    
    @Test
    public void throwsExceptionIfWorkspaceIsNotFound() throws WorkspaceNotFoundException, WorkspaceAccessException {
        
        final int workspaceID = 1;
        final String userID = "someUser";
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(throwException(expectedException));
        }});
        
        try {
            nodeAccessChecker.ensureUserHasAccessToWorkspace(userID, workspaceID);
            fail("should have thrown exception");
        } catch(WorkspaceNotFoundException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
}
