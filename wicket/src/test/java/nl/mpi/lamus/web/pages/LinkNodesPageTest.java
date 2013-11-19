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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import nl.mpi.archiving.tree.LinkedTreeModelProvider;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.web.AbstractLamusWicketTest;
import nl.mpi.lamus.web.components.LinkNodesPanel;
import nl.mpi.lamus.web.model.WorkspaceModel;
import nl.mpi.lamus.web.model.mock.MockWorkspace;
import nl.mpi.lamus.web.model.mock.MockWorkspaceTreeNode;
import nl.mpi.lamus.web.providers.LamusWicketPagesProvider;
import nl.mpi.lamus.workspace.actions.TreeNodeActionsProvider;
import nl.mpi.lamus.workspace.actions.implementation.LinkNodesAction;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import nl.mpi.lamus.workspace.tree.implementation.WorkspaceTreeModelProviderFactory;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.tester.FormTester;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
public class LinkNodesPageTest extends AbstractLamusWicketTest {
    
    private LinkNodesPage linkNodesPage;
    
    @Mock private WorkspaceTreeService mockWorkspaceServiceBean;
    @Mock private WorkspaceTreeModelProviderFactory mockWorkspaceTreeModelProviderFactoryBean;
    
    @Mock private TreeNodeActionsProvider mockTreeNodeActionsProviderBean;
    
    @Mock private LinkedTreeModelProvider mockTreeModelProvider;
    
    @Mock private LamusWicketPagesProvider mockPagesProvider;
    
    @Mock private LinkNodesAction mockLinkNodesAction;
    
    
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
    
    private MockWorkspaceTreeNode mockUnlinkedNode = new MockWorkspaceTreeNode() {{
        setWorkspaceID(mockWorkspaceID);
        setWorkspaceNodeID(20);
        setName("unlinkedNode");
        setType(WorkspaceNodeType.RESOURCE_MR);
    }};
    private Collection<WorkspaceNode> mockUnlinkedNodesList = new ArrayList<WorkspaceNode>() {{
        add(mockUnlinkedNode);
    }};

    TemporaryFolder testFolder = new TemporaryFolder();
    private File mockUploadDirectory;
    
    private boolean refreshStuffCalled = false;
    
    @Override
    protected void setUpTest() throws Exception {
        
        testFolder.create();
        mockUploadDirectory = testFolder.newFolder("workspace_" + mockWorkspaceID, "upload");
        
        
        MockitoAnnotations.initMocks(this);
        
        when(mockWorkspaceServiceBean.getWorkspace(mockWorkspaceID)).thenReturn(mockWorkspace);
        when(mockWorkspaceServiceBean.getTreeNode(mockWorkspaceTopNodeID, null)).thenReturn(mockWorkspaceTopNode);
        when(mockWorkspaceServiceBean.getWorkspaceUploadDirectory(mockWorkspaceID)).thenReturn(mockUploadDirectory);
        when(mockWorkspaceTreeModelProviderFactoryBean.createTreeModelProvider(mockWorkspaceTopNode)).thenReturn(mockTreeModelProvider);
        when(mockTreeModelProvider.getRoot()).thenReturn(mockWorkspaceTopNode);
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_SERVICE, mockWorkspaceServiceBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_TREE_MODEL_PROVIDER_FACTORY, mockWorkspaceTreeModelProviderFactoryBean);
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_TREE_NODE_ACTIONS_PROVIDER, mockTreeNodeActionsProviderBean);
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_PAGES_PROVIDER, mockPagesProvider);
        
        linkNodesPage = new LinkNodesPage(new WorkspaceModel(mockWorkspace)) {

            @Override
            protected void refreshStuff() {
                refreshStuffCalled = true;
            }

            @Override
            protected WorkspaceTreeNode getSelectedNode() {
                return mockWorkspaceTopNode;
            }
        };
        getTester().startPage(linkNodesPage);
    }

    @Override
    protected void tearDownTest() throws Exception {
        
    }
    
    
    @Test
    @DirtiesContext
    public void componentsRendered() {
        
        getTester().assertComponent("linkNodesForm", Form.class);
        getTester().assertEnabled("linkNodesForm");
        
        getTester().assertComponent("linkNodesForm:linkNodesPanel", LinkNodesPanel.class);
        getTester().assertEnabled("linkNodesForm:linkNodesPanel");
    }
    
    
    //TODO mockUnlinkedNodesList should come from the method getSelectedUnlinkedNodes from the LinkNodesPanel
        // but still haven't figured out how to mock this - or, if I try to replace the actual panel with the mock, wicket will complain
    
//    @Test
//    @DirtiesContext
//    public void submitForm() {
//        
//        FormTester formTester = getTester().newFormTester("linkNodesForm", false);
//        
//        Form form = (Form) getTester().getComponentFromLastRenderedPage("linkNodesForm");
//        form.setModelObject(mockLinkNodesAction);
//        
//        formTester.submit();
//        
//        verify(mockLinkNodesAction).execute(AbstractLamusWicketTest.MOCK_USER_ID, mockWorkspaceTopNode, mockUnlinkedNodesList);
//        assertTrue("refreshStuff not called", refreshStuffCalled);
//    }
}