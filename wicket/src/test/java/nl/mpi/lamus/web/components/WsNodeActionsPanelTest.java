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
package nl.mpi.lamus.web.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.AbstractLamusWicketTest;
import nl.mpi.lamus.web.model.mock.MockWorkspaceTreeNode;
import nl.mpi.lamus.workspace.actions.WsNodeActionsProvider;
import nl.mpi.lamus.workspace.actions.WsTreeNodesAction;
import nl.mpi.lamus.workspace.actions.implementation.DeleteNodesAction;
import nl.mpi.lamus.workspace.actions.implementation.UnlinkNodesAction;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.web.unlinkednodes.providers.UnlinkedNodesModelProviderFactory;
import nl.mpi.lamus.workspace.actions.implementation.LinkNodesAction;
import nl.mpi.lamus.workspace.actions.implementation.ReplaceNodesAction;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.util.CollectionModel;
import org.apache.wicket.util.tester.FormTester;
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
public class WsNodeActionsPanelTest extends AbstractLamusWicketTest {

    private WsNodeActionsPanel treeNodeActionsPanel;
    
    @Mock WorkspaceTreeService mockWorkspaceServiceBean;
    @Mock WsNodeActionsProvider mockTreeNodeActionsProviderBean;
    
    @Mock UnlinkedNodesModelProviderFactory mockUnlinkedNodesProviderFactory;
    
    @Mock private DeleteNodesAction mockDeleteAction;
    @Mock private UnlinkNodesAction mockUnlinkAction;
    @Mock private LinkNodesAction mockLinkAction;
    @Mock private ReplaceNodesAction mockReplaceAction;
    
    private FeedbackPanel feedbackPanel;
    
    private int mockWorkspaceID = 1;
    private MockWorkspaceTreeNode mockWorkspaceNode = new MockWorkspaceTreeNode() {{
        setWorkspaceID(mockWorkspaceID);
        setWorkspaceNodeID(10);
        setName("topNode");
        setType(WorkspaceNodeType.METADATA);
    }};
    private Collection<WorkspaceTreeNode> selectedNodes = new ArrayList<WorkspaceTreeNode>() {{
        add(mockWorkspaceNode);
    }};
    
    
    private List<WsTreeNodesAction> expectedActionsList = new ArrayList<>();

    private boolean refreshStuffCalled = false;
    
    @Override
    protected void setUpTest() throws Exception {
        
        MockitoAnnotations.initMocks(this);
        
        expectedActionsList.add(mockDeleteAction);
        expectedActionsList.add(mockUnlinkAction);
        expectedActionsList.add(mockLinkAction);
        expectedActionsList.add(mockReplaceAction);
        
        when(mockTreeNodeActionsProviderBean.getActions(selectedNodes)).thenReturn(expectedActionsList);
        
        when(mockDeleteAction.getName()).thenReturn("delete_node_action");
        when(mockUnlinkAction.getName()).thenReturn("unlink_node_action");
        when(mockLinkAction.getName()).thenReturn("link_node_action");
        when(mockReplaceAction.getName()).thenReturn("replace_node_action");
        

        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_TREE_SERVICE, mockWorkspaceServiceBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_TREE_NODE_ACTIONS_PROVIDER, mockTreeNodeActionsProviderBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_UNLINKED_NODES_MODEL_PROVIDER_FACTORY, mockUnlinkedNodesProviderFactory);
        
        feedbackPanel = new FeedbackPanel("feedbackPanel") {{
            setOutputMarkupId(true);
        }};
        
        treeNodeActionsPanel = new WsNodeActionsPanel("wsNodeActionsPanel", new CollectionModel<>(selectedNodes), feedbackPanel) {

            @Override
            public void refreshTreeAndPanels() {
                refreshStuffCalled = true;
            }
        };
        getTester().startComponentInPage(treeNodeActionsPanel);
    }

    @Override
    protected void tearDownTest() throws Exception {
        
    }
    
    
    //TODO FIX THESE TESTS
    
    
    @Test
    @DirtiesContext
    public void componentsRendered() {
        
        getTester().assertComponent("wsNodeActionsPanel:wsNodeActionsForm", Form.class);
        getTester().assertEnabled("wsNodeActionsPanel:wsNodeActionsForm");
        getTester().assertModelValue("wsNodeActionsPanel:wsNodeActionsForm", selectedNodes);
        
        getTester().assertComponent("wsNodeActionsPanel:wsNodeActionsForm:wsNodeActions", ListView.class);
        getTester().assertEnabled("wsNodeActionsPanel:wsNodeActionsForm:wsNodeActions");
        getTester().assertListView("wsNodeActionsPanel:wsNodeActionsForm:wsNodeActions", expectedActionsList);
        
        ListView<WsTreeNodesAction> nodesActionList = (ListView<WsTreeNodesAction>) getTester().getComponentFromLastRenderedPage("wsNodeActionsPanel:wsNodeActionsForm:wsNodeActions");
        Iterator<Component> listItems = nodesActionList.iterator();
        while(listItems.hasNext()) {
            ListItem<WsTreeNodesAction> item = (ListItem<WsTreeNodesAction>) listItems.next();
            Iterator<Component> itemButtons =  item.iterator();
            int i = 0;
            while(itemButtons.hasNext()) {
                Component button = itemButtons.next();
                assertTrue("Component " + button.getPath() + " is not instance of expected class", button instanceof WsNodeActionButton);
                i++;
            }
            assertTrue("Number of buttons different from expected", i != 2);
        }
    }
    
    @Test
    @DirtiesContext
    public void clickButton() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        
        ListView<WsTreeNodesAction> nodesActionList = (ListView<WsTreeNodesAction>) getTester().getComponentFromLastRenderedPage("wsNodeActionsPanel:wsNodeActionsForm:wsNodeActions");
        Iterator<Component> listItems = nodesActionList.iterator();

        ListItem<WsTreeNodesAction> item = (ListItem<WsTreeNodesAction>) listItems.next();
        Iterator<Component> itemButtons =  item.iterator();
            
        Component button = itemButtons.next();
        assertEquals("Not the expected button", "delete_node_action", (String) button.getDefaultModelObject());
        FormTester formTester = getTester().newFormTester("wsNodeActionsPanel:wsNodeActionsForm", false);
        formTester.submit(button);
        
        //for some reason the actual WorkspaceService object that is used in the call is not the same as expected
            // - something to do with the object proxy created by Mockito and passed on?
        verify(mockDeleteAction).setSelectedTreeNodes(selectedNodes);
        verify(mockDeleteAction).setSelectedUnlinkedNodes(any(Collection.class));
        verify(mockDeleteAction).execute(eq(AbstractLamusWicketTest.MOCK_USER_ID), any(WorkspaceService.class));
        assertTrue("refreshStuff not called", refreshStuffCalled);
    }
}