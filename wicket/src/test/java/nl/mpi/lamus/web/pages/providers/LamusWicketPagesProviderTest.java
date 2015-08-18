/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.web.pages.providers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nl.mpi.archiving.tree.GenericTreeModelProvider;
import nl.mpi.archiving.tree.LinkedTreeModelProvider;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.web.AbstractLamusWicketTest;
import nl.mpi.lamus.web.model.WorkspaceModel;
import nl.mpi.lamus.web.model.WorkspaceModelProvider;
import nl.mpi.lamus.web.model.mock.MockWorkspace;
import nl.mpi.lamus.web.model.mock.MockWorkspaceTreeNode;
import nl.mpi.lamus.web.pages.CreateWorkspacePage;
import nl.mpi.lamus.web.pages.IndexPage;
import nl.mpi.lamus.web.pages.SelectWorkspacePage;
import nl.mpi.lamus.web.pages.WorkspacePage;
import nl.mpi.lamus.web.pages.management.ManageWorkspacesPage;
import nl.mpi.lamus.web.unlinkednodes.providers.UnlinkedNodesModelProvider;
import nl.mpi.lamus.web.unlinkednodes.providers.UnlinkedNodesModelProviderFactory;
import nl.mpi.lamus.workspace.actions.WsNodeActionsProvider;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import nl.mpi.lamus.workspace.tree.implementation.WorkspaceTreeModelProviderFactory;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author guisil
 */
public class LamusWicketPagesProviderTest extends AbstractLamusWicketTest {
    
    private LamusWicketPagesProvider pagesProvider;
    
    
    private int mockWorkspaceTopNodeID = 1;
        
    private final Workspace mockWs1 = new MockWorkspace() {{
        setWorkspaceID(1);
        setTopNodeID(mockWorkspaceTopNodeID);
    }};
    private final Workspace mockWs2 = new MockWorkspace() {{
        setWorkspaceID(2);
    }};
    private final Collection<Workspace> mockWsList = new ArrayList<Workspace>() {{
        add(mockWs1);
        add(mockWs2);
    }};
    
    private final WorkspaceNode mockUnlinkedWsN1 = new MockWorkspaceTreeNode() {{
        setWorkspaceID(mockWorkspaceID);
        setWorkspaceNodeID(2);
    }};
    private final WorkspaceNode mockUnlinkedWsN2 = new MockWorkspaceTreeNode() {{
        setWorkspaceID(mockWorkspaceID);
        setWorkspaceNodeID(3);
    }};
    private final List<WorkspaceNode> mockUnlinkedNodesList = new ArrayList<WorkspaceNode>() {{
        add(mockUnlinkedWsN1);
        add(mockUnlinkedWsN2);
    }};

    @Mock private WorkspaceTreeService mockWorkspaceServiceBean;
    @Mock private WorkspaceDao mockWorkspaceDao;
    @Mock private NodeUtil mockNodeUtilBean;
    @Mock private WorkspaceTreeModelProviderFactory mockWorkspaceTreeModelProviderFactoryBean;
    @Mock private UnlinkedNodesModelProviderFactory mockUnlinkedNodesModelProviderFactoryBean;
    @Mock private GenericTreeModelProvider mockCreateWorkspaceTreeModelProviderBean;
    @Mock private WsNodeActionsProvider mockTreeNodeActionsProviderBean;
    @Mock private UnlinkedNodesModelProvider mockUnlinkedNodesModelProviderBean;
    @Mock private Collection<String> mockManagerUsers;
    
    @Mock private WorkspaceModelProvider mockWorkspaceModelProvider;
    @Mock private WorkspaceModel mockWorkspaceModel;
    
    @Mock private LamusWicketPagesProvider mockPagesProviderBean;
    
    
    private int mockWorkspaceID = 1;
    
    @Mock private Workspace mockWorkspace;
    @Mock private WorkspaceTreeNode mockWorkspaceTopNode;
    @Mock private LinkedTreeModelProvider mockTreeModelProvider;
    
    @Mock private CreateWorkspacePage mockCreateWorkspacePage;
    @Mock private SelectWorkspacePage mockSelectWorkspacePage;
    @Mock private WorkspacePage mockWorkspacePage;

    TemporaryFolder testFolder = new TemporaryFolder();
    private File mockUploadDirectory;
    
    private final String mockRegisterUrl = "https://test.mpi.nl/registerUrl";
    private final String mockManualUrl = "http://test.mpi.nl/lamus/manual";
    
    @Override
    protected void setUpTest() throws Exception {
        
        testFolder.create();
        mockUploadDirectory = testFolder.newFolder("workspace_" + mockWorkspaceID, "upload");
        
        
        MockitoAnnotations.initMocks(this);
        
        when(mockWorkspaceModelProvider.getWorkspaceModel(mockWorkspaceID)).thenReturn(mockWorkspaceModel);
        
        // expected calls in WorkspacePage
        when(mockWorkspace.getWorkspaceID()).thenReturn(mockWorkspaceID);
        when(mockWorkspace.getTopNodeID()).thenReturn(mockWorkspaceTopNodeID);
        when(mockWorkspaceServiceBean.getTreeNode(mockWorkspaceTopNodeID, null)).thenReturn(mockWorkspaceTopNode);
        when(mockWorkspaceServiceBean.listUserWorkspaces(AbstractLamusWicketTest.MOCK_USER_ID)).thenReturn(mockWsList);
        when(mockWorkspaceServiceBean.getWorkspaceUploadDirectory(mockWorkspaceID)).thenReturn(mockUploadDirectory);
        when(mockWorkspaceServiceBean.listUnlinkedNodes(AbstractLamusWicketTest.MOCK_USER_ID, mockWorkspaceID)).thenReturn(mockUnlinkedNodesList);
        when(mockWorkspaceTreeModelProviderFactoryBean.createTreeModelProvider(mockWorkspaceTopNode)).thenReturn(mockTreeModelProvider);
        when(mockTreeModelProvider.getRoot()).thenReturn(mockWorkspaceTopNode);
        when(mockUnlinkedNodesModelProviderFactoryBean.createTreeModelProvider(mockWorkspaceServiceBean, mockWorkspaceID)).thenReturn(mockUnlinkedNodesModelProviderBean);
        
        when(mockWorkspaceModel.getObject()).thenReturn(mockWorkspace);
        
        when(mockPagesProviderBean.getCreateWorkspacePage()).thenReturn(mockCreateWorkspacePage);
        when(mockPagesProviderBean.getSelectWorkspacePage()).thenReturn(mockSelectWorkspacePage);
        when(mockPagesProviderBean.getWorkspacePage(mockWorkspace)).thenReturn(mockWorkspacePage);
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_SERVICE, mockWorkspaceServiceBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_DAO, mockWorkspaceDao);
        addMock(AbstractLamusWicketTest.BEAN_NAME_NODE_UTIL, mockNodeUtilBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_TREE_MODEL_PROVIDER_FACTORY, mockWorkspaceTreeModelProviderFactoryBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_UNLINKED_NODES_MODEL_PROVIDER_FACTORY, mockUnlinkedNodesModelProviderFactoryBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_CREATE_WORKSPACE_TREE_PROVIDER, mockCreateWorkspaceTreeModelProviderBean);
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_TREE_NODE_ACTIONS_PROVIDER, mockTreeNodeActionsProviderBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_PAGES_PROVIDER, mockPagesProviderBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_REGISTER_URL, mockRegisterUrl);
        addMock(AbstractLamusWicketTest.BEAN_NAME_MANUAL_URL, mockManualUrl);
        addMock(AbstractLamusWicketTest.BEAN_NAME_MANAGER_USERS, mockManagerUsers);
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_MODEL_PROVIDER, mockWorkspaceModelProvider);
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_MODEL, mockWorkspaceModel);
        

        pagesProvider = new LamusWicketPagesProvider();
        ReflectionTestUtils.setField(pagesProvider, AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_MODEL_PROVIDER, mockWorkspaceModelProvider);
    }

    @Override
    protected void tearDownTest() throws Exception {
        testFolder.delete();
    }
    
    
    @Test
    @DirtiesContext
    public void getIndexPage() {
        
        IndexPage resultPage = pagesProvider.getIndexPage();
        
        assertNotNull("Page should not be null", resultPage);
    }
    
    @Test
    @DirtiesContext
    public void getCreateWorkspacePage() {
        
        CreateWorkspacePage resultPage = pagesProvider.getCreateWorkspacePage();
        
        assertNotNull("Page should not be null", resultPage);
    }
    
    @Test
    @DirtiesContext
    public void getSelectWorkspacePage() {
        
        SelectWorkspacePage resultPage = pagesProvider.getSelectWorkspacePage();
        
        verify(mockWorkspaceServiceBean).listUserWorkspaces(AbstractLamusWicketTest.MOCK_USER_ID);
        
        assertNotNull("Page should not be null", resultPage);
    }

    @Test
    @DirtiesContext
    public void getWorkspacePage() throws WorkspaceNodeNotFoundException {
        
        WorkspacePage resultPage = pagesProvider.getWorkspacePage(mockWorkspace);
        
        verify(mockWorkspace).getWorkspaceID();
        verify(mockWorkspaceModelProvider).getWorkspaceModel(mockWorkspaceID);
        verify(mockWorkspaceModel).getObject();
        
        verify(mockWorkspace).getTopNodeID();
        verify(mockWorkspaceServiceBean).getTreeNode(mockWorkspaceTopNodeID, null);
        verify(mockWorkspaceTreeModelProviderFactoryBean).createTreeModelProvider(mockWorkspaceTopNode);
        verify(mockTreeModelProvider).getRoot();
        
        assertNotNull("Page should not be null", resultPage);
    }
    
    @Test
    @DirtiesContext
    public void getManageWorkspacePage() {
        
        ManageWorkspacesPage resultPage = pagesProvider.getManageWorkspacesPage();
        
        assertNotNull("Page should not be null", resultPage);
    }
}