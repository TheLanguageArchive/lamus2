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
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.UnknownNodeException;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.ams.AmsBridge;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.management.WorkspaceAccessChecker;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import static org.junit.Assert.*;
import org.junit.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceAccessCheckerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    private WorkspaceAccessChecker nodeAccessChecker;
    @Mock private CorpusStructureProvider mockCorpusStructureProvider;
    @Mock private AmsBridge mockAmsBridge;
    @Mock private WorkspaceDao mockWorkspaceDao;
    
    @Mock private CorpusNode mockCorpusNode;
    
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
        nodeAccessChecker = new LamusWorkspaceAccessChecker(mockCorpusStructureProvider, mockAmsBridge, mockWorkspaceDao);
    }
    
    @After
    public void tearDown() {
    }
    
    /**
     * Test of canCreateWorkspace method, of class NodeAccessCheckerImpl.
     */
    @Test
    public void cannotCreateWorkspaceIfNodeIsUnknown() throws URISyntaxException, UnknownNodeException {
        
        final String userID = "someUser";
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        
        context.checking(new Expectations() {{
//            oneOf (mockArchiveObjectsDB).isOnSite(NodeIdUtils.TONODEID(archiveNodeID)); will(returnValue(false));
            oneOf(mockCorpusStructureProvider).getNode(archiveNodeURI); will(throwException(new UnknownNodeException("node not known")));
        }});
        
        //TODO Maybe some other return or exception thrown, to indicate why it's not possible to create the workspace
        
        boolean result = nodeAccessChecker.canCreateWorkspace(userID, archiveNodeURI);
        assertFalse("Result should be false when the selected top node is external.", result);
    }
 

    /**
     * Test of canCreateWorkspace method, of class NodeAccessCheckerImpl.
     */
    @Test
    public void cannotCreateWorkspaceIfNodeIsExternal() throws URISyntaxException, UnknownNodeException {
        
        final String userID = "someUser";
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        
        context.checking(new Expectations() {{
//            oneOf (mockArchiveObjectsDB).isOnSite(NodeIdUtils.TONODEID(archiveNodeID)); will(returnValue(false));
            oneOf(mockCorpusStructureProvider).getNode(archiveNodeURI); will(returnValue(mockCorpusNode));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(Boolean.FALSE));
        }});
        
        boolean result = nodeAccessChecker.canCreateWorkspace(userID, archiveNodeURI);
        assertFalse("Result should be false when the selected top node is external.", result);
    }
    
    /**
     * Test of canCreateWorkspace method, of class NodeAccessCheckerImpl.
     */
    @Test
    public void cannotCreateWorkspaceIfNodeIsNotAccessibleToUser() throws URISyntaxException, UnknownNodeException {
        
        final String userID = "someUser";
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        
        context.checking(new Expectations() {{
//            oneOf (mockArchiveObjectsDB).isOnSite(NodeIdUtils.TONODEID(archiveNodeID)); will(returnValue(true));
            oneOf(mockCorpusStructureProvider).getNode(archiveNodeURI); will(returnValue(mockCorpusNode));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(Boolean.TRUE));
            oneOf (mockAmsBridge).hasWriteAccess(userID, archiveNodeURI); will(returnValue(Boolean.FALSE));
        }});
        
        boolean result = nodeAccessChecker.canCreateWorkspace(userID, archiveNodeURI);
        assertFalse("Result should be false when the current user does not have write access in the selected top node.", result);
    }
    
    /**
     * Test of canCreateWorkspace method, of class NodeAccessCheckerImpl.
     */
    @Test
    public void cannotCreateWorkspaceIfNodeIsLocked() throws URISyntaxException, UnknownNodeException {
        
        final String userID = "someUser";
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        
        context.checking(new Expectations() {{
//            oneOf (mockArchiveObjectsDB).isOnSite(NodeIdUtils.TONODEID(archiveNodeID)); will(returnValue(true));
            oneOf(mockCorpusStructureProvider).getNode(archiveNodeURI); will(returnValue(mockCorpusNode));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(Boolean.TRUE));
            oneOf (mockAmsBridge).hasWriteAccess(userID, archiveNodeURI); will(returnValue(Boolean.TRUE));
            oneOf (mockWorkspaceDao).isNodeLocked(archiveNodeURI); will(returnValue(Boolean.TRUE));
        }});
        
        boolean result = nodeAccessChecker.canCreateWorkspace(userID, archiveNodeURI);
        assertFalse("Result should be false when the selected top node is locked.", result);
    }
    
    /**
     * Test of canCreateWorkspace method, of class NodeAccessCheckerImpl.
     */
    @Test
    public void canCreateWorkspaceIfNodeIsNotLocked() throws URISyntaxException, UnknownNodeException {
        
        final String userID = "someUser";
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        
        context.checking(new Expectations() {{
//            oneOf (mockArchiveObjectsDB).isOnSite(NodeIdUtils.TONODEID(archiveNodeID)); will(returnValue(true));
            oneOf(mockCorpusStructureProvider).getNode(archiveNodeURI); will(returnValue(mockCorpusNode));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(Boolean.TRUE));
            oneOf (mockAmsBridge).hasWriteAccess(userID, archiveNodeURI); will(returnValue(Boolean.TRUE));
            oneOf (mockWorkspaceDao).isNodeLocked(archiveNodeURI); will(returnValue(Boolean.FALSE));
        }});
        
        boolean result = nodeAccessChecker.canCreateWorkspace(userID, archiveNodeURI);
        assertTrue("Result should be true when the selected top node is not locked.", result);
    }
    
    @Test
    public void hasAccessToWorkspaceIfUserIsTheSame() throws URISyntaxException, MalformedURLException {
        
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
        final String archiveInfo = "still not sure what this would be";
        final Workspace testWorkspace = new LamusWorkspace(workspaceID, userID, topNodeID, topNodeArchiveURI, topNodeArchiveURL,
                startDate, null, startDate, null, usedStorageSpace, maxStorageSpace, status, message, archiveInfo);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(testWorkspace));
        }});
        
        boolean result = nodeAccessChecker.hasAccessToWorkspace(userID, workspaceID);
        assertTrue("Result should be true when the user is the creator of the workspace.", result);
    }
    
    @Test
    public void hasNoAccessToWorkspaceIfUserIsTheSame() throws URISyntaxException, MalformedURLException {
        
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
        final String archiveInfo = "still not sure what this would be";
        final Workspace testWorkspace = new LamusWorkspace(workspaceID, userID, topNodeID, topNodeArchiveURI, topNodeArchiveURL,
                startDate, null, startDate, null, usedStorageSpace, maxStorageSpace, status, message, archiveInfo);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(testWorkspace));
        }});
        
        boolean result = nodeAccessChecker.hasAccessToWorkspace("someOtherUser", workspaceID);
        assertFalse("Result should be false when the user is not the creator of the workspace.", result);
    }
    
    @Test
    public void hasNoAccessToWorkspaceIfWorkspaceIsNull() {
        
        final int workspaceID = 1;
        final String userID = "someUser";
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(null));
        }});
        
        boolean result = nodeAccessChecker.hasAccessToWorkspace(userID, workspaceID);
        assertFalse("Result should be false when the workspace does not exist.", result);
    }
    
}
