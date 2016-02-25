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
package nl.mpi.lamus.web.pages;

import java.net.URI;
import java.net.URL;
import nl.mpi.lamus.web.components.ButtonPanel;
import nl.mpi.archiving.tree.LinkedTreeModelProvider;
import nl.mpi.archiving.tree.wicket.components.ArchiveTreePanel;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.web.AbstractLamusWicketTest;
import nl.mpi.lamus.web.components.WsNodeActionsPanel;
import nl.mpi.lamus.web.model.WorkspaceModel;
import nl.mpi.lamus.web.model.mock.MockWorkspace;
import nl.mpi.lamus.web.model.mock.MockWorkspaceTreeNode;
import nl.mpi.lamus.web.pages.providers.LamusWicketPagesProvider;
import nl.mpi.lamus.web.unlinkednodes.providers.UnlinkedNodesModelProviderFactory;
import nl.mpi.lamus.workspace.actions.WsNodeActionsProvider;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import nl.mpi.lamus.workspace.tree.implementation.WorkspaceTreeModelProviderFactory;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.springframework.test.annotation.DirtiesContext;

/**
 *
 * @author guisil
 */
public class WorkspacePageTest extends AbstractLamusWicketTest {
    
    private WorkspacePage wsPage;
    
    @Mock private WorkspaceTreeService mockWorkspaceServiceBean;
    @Mock private WorkspaceDao mockWorkspaceDao;
    @Mock private NodeUtil mockNodeUtilBean;
    @Mock private WorkspaceTreeModelProviderFactory mockWorkspaceTreeModelProviderFactoryBean;
    @Mock private UnlinkedNodesModelProviderFactory mockUnlinkedNodesModelProviderFactory;
    
    @Mock private WsNodeActionsProvider mockTreeNodeActionsProviderBean;
    
    @Mock private LinkedTreeModelProvider mockTreeModelProvider;
    
    @Mock private LamusWicketPagesProvider mockPagesProvider;
    
    private int mockWorkspaceID = 1;
    private int mockWorkspaceTopNodeID = 10;
    private final MockWorkspace mockWorkspace = new MockWorkspace() {{
        setUserID(AbstractLamusWicketTest.MOCK_USER_ID);
        setWorkspaceID(mockWorkspaceID);
        setStatus(WorkspaceStatus.INITIALISED);
        setTopNodeID(mockWorkspaceTopNodeID);
    }};
    private final MockWorkspaceTreeNode mockWorkspaceTopNode = new MockWorkspaceTreeNode() {{
        setWorkspaceID(mockWorkspaceID);
        setWorkspaceNodeID(mockWorkspaceTopNodeID);
        setName("topNode");
        setType(WorkspaceNodeType.METADATA);
    }};

    private final String mockRegisterUrl = "https://test.mpi.nl/registerUrl";
    private final String mockManualUrl = "http://test.mpi.nl/lamus/manusl";
    

    @Override
    protected void setUpTest() throws Exception {
        
        mockWorkspaceTopNode.setArchiveURI(new URI("node:10"));
        mockWorkspaceTopNode.setArchiveURL(new URL("file:/archive/topNode.cmdi"));
        
        
        MockitoAnnotations.initMocks(this);
        
        when(mockWorkspaceServiceBean.getWorkspace(mockWorkspaceID)).thenReturn(mockWorkspace);
        when(mockWorkspaceServiceBean.getTreeNode(mockWorkspaceTopNodeID, null)).thenReturn(mockWorkspaceTopNode);
        when(mockWorkspaceTreeModelProviderFactoryBean.createTreeModelProvider(mockWorkspaceTopNode)).thenReturn(mockTreeModelProvider);
        when(mockTreeModelProvider.getRoot()).thenReturn(mockWorkspaceTopNode);
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_SERVICE, mockWorkspaceServiceBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_DAO, mockWorkspaceDao);
        addMock(AbstractLamusWicketTest.BEAN_NAME_NODE_UTIL, mockNodeUtilBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_TREE_MODEL_PROVIDER_FACTORY, mockWorkspaceTreeModelProviderFactoryBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_UNLINKED_NODES_MODEL_PROVIDER_FACTORY, mockUnlinkedNodesModelProviderFactory);
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_TREE_NODE_ACTIONS_PROVIDER, mockTreeNodeActionsProviderBean);
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_PAGES_PROVIDER, mockPagesProvider);
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_REGISTER_URL, mockRegisterUrl);
        addMock(AbstractLamusWicketTest.BEAN_NAME_MANUAL_URL, mockManualUrl);
        
        wsPage = new WorkspacePage(new WorkspaceModel(mockWorkspaceID));
        getTester().startPage(wsPage);
    }

    @Override
    protected void tearDownTest() throws Exception {
        
    }

    //TODO Tests are still very incomplete
    
    @Test
    @DirtiesContext
    public void pageRendered() {
        
        getTester().assertRenderedPage(WorkspacePage.class);
    }
    
    @Test
    @DirtiesContext
    public void componentsRendered() {
        
        getTester().assertComponent("buttonPanel", ButtonPanel.class);
        getTester().assertEnabled("buttonPanel");
        
        
        getTester().assertComponent("workspaceTree", ArchiveTreePanel.class);
        getTester().assertEnabled("workspaceTree");
        
        
        getTester().assertComponent("wsNodeActionsPanel", WsNodeActionsPanel.class);
        getTester().assertEnabled("wsNodeActionsPanel");
        
        
        getTester().assertComponent("workspaceTabs", TabbedPanel.class);
        getTester().assertEnabled("workspaceTabs");
    }
}