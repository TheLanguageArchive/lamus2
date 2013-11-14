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
package nl.mpi.lamus.web.providers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import nl.mpi.archiving.tree.GenericTreeModelProvider;
import nl.mpi.archiving.tree.LinkedTreeModelProvider;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.web.AbstractLamusWicketTest;
import nl.mpi.lamus.web.model.WorkspaceModel;
import nl.mpi.lamus.web.model.mock.MockWorkspace;
import nl.mpi.lamus.web.pages.CreateWorkspacePage;
import nl.mpi.lamus.web.pages.IndexPage;
import nl.mpi.lamus.web.pages.SelectWorkspacePage;
import nl.mpi.lamus.web.pages.UploadPage;
import nl.mpi.lamus.web.pages.WorkspacePage;
import nl.mpi.lamus.workspace.actions.TreeNodeActionsProvider;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import nl.mpi.lamus.workspace.tree.implementation.WorkspaceTreeModelProviderFactory;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.springframework.test.annotation.DirtiesContext;

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

    @Mock private WorkspaceTreeService mockWorkspaceServiceBean;
    @Mock private WorkspaceTreeModelProviderFactory mockWorkspaceTreeModelProviderFactoryBean;
    @Mock private GenericTreeModelProvider mockCreateWorkspaceTreeModelProviderBean;
    @Mock private TreeNodeActionsProvider mockTreeNodeActionsProviderBean;
    
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
    
    @Override
    protected void setUpTest() throws Exception {
        
        testFolder.create();
        mockUploadDirectory = testFolder.newFolder("workspace_" + mockWorkspaceID, "upload");
        
        
        MockitoAnnotations.initMocks(this);
        
        // expected calls in SelectWorkspacePage
//        when(mockWorkspaceServiceBean.listUserWorkspaces(AbstractLamusWicketTest.MOCK_USER_ID)).thenReturn(mockWsList);
//        when(mockWorkspaceServiceBean.openWorkspace(AbstractLamusWicketTest.MOCK_USER_ID, 1)).thenReturn(mockWs1);
        
        // expected calls in WorkspacePage
        when(mockWorkspace.getWorkspaceID()).thenReturn(mockWorkspaceID);
        when(mockWorkspace.getTopNodeID()).thenReturn(mockWorkspaceTopNodeID);
        when(mockWorkspaceServiceBean.getTreeNode(mockWorkspaceTopNodeID, null)).thenReturn(mockWorkspaceTopNode);
        when(mockWorkspaceServiceBean.listUserWorkspaces(AbstractLamusWicketTest.MOCK_USER_ID)).thenReturn(mockWsList);
        when(mockWorkspaceServiceBean.getWorkspaceUploadDirectory(mockWorkspaceID)).thenReturn(mockUploadDirectory);
        when(mockWorkspaceTreeModelProviderFactoryBean.createTreeModelProvider(mockWorkspaceTopNode)).thenReturn(mockTreeModelProvider);
        when(mockTreeModelProvider.getRoot()).thenReturn(mockWorkspaceTopNode);
        
        when(mockPagesProviderBean.getCreateWorkspacePage()).thenReturn(mockCreateWorkspacePage);
        when(mockPagesProviderBean.getSelectWorkspacePage()).thenReturn(mockSelectWorkspacePage);
        when(mockPagesProviderBean.getWorkspacePage(mockWorkspace)).thenReturn(mockWorkspacePage);
        
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_SERVICE, mockWorkspaceServiceBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_TREE_MODEL_PROVIDER_FACTORY, mockWorkspaceTreeModelProviderFactoryBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_CREATE_WORKSPACE_TREE_PROVIDER, mockCreateWorkspaceTreeModelProviderBean);
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_TREE_NODE_ACTIONS_PROVIDER, mockTreeNodeActionsProviderBean);
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_PAGES_PROVIDER, mockPagesProviderBean);

        pagesProvider = new LamusWicketPagesProvider();
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
    public void getWorkspacePage() {
        
        WorkspacePage resultPage = pagesProvider.getWorkspacePage(mockWorkspace);
        
        verify(mockWorkspace).getTopNodeID();
        verify(mockWorkspaceServiceBean).getTreeNode(mockWorkspaceTopNodeID, null);
        verify(mockWorkspaceTreeModelProviderFactoryBean).createTreeModelProvider(mockWorkspaceTopNode);
        verify(mockTreeModelProvider).getRoot();
        
        assertNotNull("Page should not be null", resultPage);
    }
    
    @Test
    @DirtiesContext
    public void getUploadPage() {
        
        UploadPage resultPage = pagesProvider.getUploadPage(mockWorkspace);
        
        verify(mockWorkspace).getTopNodeID();
        verify(mockWorkspaceServiceBean).getTreeNode(mockWorkspaceTopNodeID, null);
        verify(mockWorkspaceTreeModelProviderFactoryBean).createTreeModelProvider(mockWorkspaceTopNode);
        verify(mockTreeModelProvider).getRoot();
        
        assertNotNull("Page should not be null", resultPage);
    }
}