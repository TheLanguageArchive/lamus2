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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.mpi.lamus.web.components.ButtonPanel;
import nl.mpi.archiving.tree.LinkedTreeModelProvider;
import nl.mpi.archiving.tree.wicket.components.ArchiveTreePanel;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.web.AbstractLamusWicketTest;
import nl.mpi.lamus.web.components.WsTreeNodeActionsPanel;
import nl.mpi.lamus.web.model.WorkspaceModel;
import nl.mpi.lamus.web.model.mock.MockWorkspace;
import nl.mpi.lamus.web.model.mock.MockWorkspaceTreeNode;
import nl.mpi.lamus.web.providers.LamusWicketPagesProvider;
import nl.mpi.lamus.workspace.actions.TreeNodeActionsProvider;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import nl.mpi.lamus.workspace.tree.implementation.WorkspaceTreeModelProviderFactory;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.junit.Test;
import static org.junit.Assert.*;
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
    @Mock private WorkspaceTreeModelProviderFactory mockWorkspaceTreeModelProviderFactoryBean;
    
    @Mock private TreeNodeActionsProvider mockTreeNodeActionsProviderBean;
    
    @Mock private LinkedTreeModelProvider mockTreeModelProvider;
    
    @Mock private LamusWicketPagesProvider mockPagesProvider;
    
    private int mockWorkspaceID = 1;
    private int mockWorkspaceTopNodeID = 10;
    private MockWorkspace mockWorkspace = new MockWorkspace() {{
        setUserID(AbstractLamusWicketTest.MOCK_USER_ID);
        setWorkspaceID(mockWorkspaceID);
        setStatus(WorkspaceStatus.INITIALISED);
        setTopNodeID(mockWorkspaceTopNodeID);
    }};
    private MockWorkspaceTreeNode mockWorkspaceTopNode = new MockWorkspaceTreeNode() {{
        setWorkspaceID(mockWorkspaceID);
        setWorkspaceNodeID(mockWorkspaceTopNodeID);
        setName("topNode");
        setType(WorkspaceNodeType.METADATA);
    }};


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
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_TREE_MODEL_PROVIDER_FACTORY, mockWorkspaceTreeModelProviderFactoryBean);
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_TREE_NODE_ACTIONS_PROVIDER, mockTreeNodeActionsProviderBean);
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_PAGES_PROVIDER, mockPagesProvider);
        
        wsPage = new WorkspacePage(new WorkspaceModel(mockWorkspace));
        getTester().startPage(wsPage);
    }

    @Override
    protected void tearDownTest() throws Exception {
        
    }

    
    @Test
    @DirtiesContext
    public void pageRendered() {
        
        getTester().assertRenderedPage(WorkspacePage.class);
    }
    
    @Test
    @DirtiesContext
    public void componentsRendered() {
        
        getTester().assertComponent("buttonpage", ButtonPanel.class);
        getTester().assertEnabled("buttonpage");
        
        
        getTester().assertComponent("workspaceInfo", WebMarkupContainer.class);
        getTester().assertEnabled("workspaceInfo");
        
        getTester().assertComponent("workspaceInfo:userID", Label.class);
        getTester().assertEnabled("workspaceInfo:userID");
        getTester().assertLabel("workspaceInfo:userID", mockWorkspace.getUserID());

        getTester().assertComponent("workspaceInfo:workspaceID", Label.class);
        getTester().assertEnabled("workspaceInfo:workspaceID");
        getTester().assertLabel("workspaceInfo:workspaceID", "" + mockWorkspace.getWorkspaceID());
        
        getTester().assertComponent("workspaceInfo:status", Label.class);
        getTester().assertEnabled("workspaceInfo:status");
        getTester().assertLabel("workspaceInfo:status", mockWorkspace.getStatus().toString());
        
        
        getTester().assertComponent("nodeInfoContainer", WebMarkupContainer.class);
        getTester().assertEnabled("nodeInfoContainer");
        
        getTester().assertComponent("nodeInfoContainer:nodeInfoForm", Form.class);
        getTester().assertEnabled("nodeInfoContainer:nodeInfoForm");
        
        getTester().assertComponent("nodeInfoContainer:nodeInfoForm:name", Label.class);
        getTester().assertEnabled("nodeInfoContainer:nodeInfoForm:name");
        getTester().assertLabel("nodeInfoContainer:nodeInfoForm:name", ""); //TODO test when node selection changes
        
        getTester().assertComponent("nodeInfoContainer:nodeInfoForm:archiveURI", Label.class);
        getTester().assertEnabled("nodeInfoContainer:nodeInfoForm:archiveURI");
        getTester().assertLabel("nodeInfoContainer:nodeInfoForm:archiveURI", ""); //TODO test when node selection changes
        
        getTester().assertComponent("nodeInfoContainer:nodeInfoForm:archiveURL", Label.class);
        getTester().assertEnabled("nodeInfoContainer:nodeInfoForm:archiveURL");
        getTester().assertLabel("nodeInfoContainer:nodeInfoForm:archiveURL", ""); //TODO test when node selection changes
        
        getTester().assertComponent("nodeInfoContainer:nodeInfoForm:workspaceID", Label.class);
        getTester().assertEnabled("nodeInfoContainer:nodeInfoForm:workspaceID");
        getTester().assertLabel("nodeInfoContainer:nodeInfoForm:workspaceID", ""); //TODO test when node selection changes
        
        getTester().assertComponent("nodeInfoContainer:nodeInfoForm:type", Label.class);
        getTester().assertEnabled("nodeInfoContainer:nodeInfoForm:type");
        getTester().assertLabel("nodeInfoContainer:nodeInfoForm:type", ""); //TODO test when node selection changes
        
        
        getTester().assertComponent("workspaceTree", ArchiveTreePanel.class);
        getTester().assertEnabled("workspaceTree");
        
        
        getTester().assertComponent("wsNodeActionsPanel", WsTreeNodeActionsPanel.class);
        getTester().assertEnabled("wsNodeActionsPanel");
    }

    @Test
    @DirtiesContext
    public void changeSelectedNode() {
        
        
        //TODO How to trigger the "node selection" event in the tree?
    }
    
    @Test
    @DirtiesContext
    public void testRefreshStuff() {
        
        //TODO ...
    }
    
    @Test
    @DirtiesContext
    public void updateModelNodeIdForm() {
        
        Form<WorkspaceTreeNode> nodeIdForm = (Form<WorkspaceTreeNode>) getTester().getComponentFromLastRenderedPage("nodeInfoContainer:nodeInfoForm");
        nodeIdForm.setModel(new CompoundPropertyModel<WorkspaceTreeNode>(mockWorkspaceTopNode));
        
        getTester().assertLabel("nodeInfoContainer:nodeInfoForm:name", mockWorkspaceTopNode.getName());
        getTester().assertLabel("nodeInfoContainer:nodeInfoForm:archiveURI", mockWorkspaceTopNode.getArchiveURI().toString());
        getTester().assertLabel("nodeInfoContainer:nodeInfoForm:archiveURL", mockWorkspaceTopNode.getArchiveURL().toString());
        getTester().assertLabel("nodeInfoContainer:nodeInfoForm:workspaceID", "" + mockWorkspaceTopNode.getWorkspaceID());
        getTester().assertLabel("nodeInfoContainer:nodeInfoForm:type", mockWorkspaceTopNode.getType().toString());
    }
    
    @Test
    @DirtiesContext
    public void updateModelNodeActionsPanel() {
        
        Collection<WorkspaceTreeNode> selectedNodes = new ArrayList<WorkspaceTreeNode>();
        selectedNodes.add(mockWorkspaceTopNode);
        
        WsTreeNodeActionsPanel nodeActionsPanel = (WsTreeNodeActionsPanel) getTester().getComponentFromLastRenderedPage("wsNodeActionsPanel");
        nodeActionsPanel.setModelObject(selectedNodes);
        
        //TODO THIS SHOULD BE TESTED IN THE PANEL TESTS...
    }
}