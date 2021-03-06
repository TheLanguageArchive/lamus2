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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.NodeNotFoundException;
import nl.mpi.archiving.tree.GenericTreeModelProvider;
import nl.mpi.archiving.tree.LinkedTreeModelProvider;
import nl.mpi.archiving.tree.LinkedTreeNode;
import nl.mpi.archiving.tree.corpusstructure.LinkedCorpusNode;
import nl.mpi.archiving.tree.wicket.components.ArchiveTreePanel;
import nl.mpi.lamus.exception.NodeAccessException;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.web.AbstractLamusWicketTest;
import nl.mpi.lamus.web.model.mock.MockCorpusNode;
import nl.mpi.lamus.web.pages.providers.LamusWicketPagesProvider;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.workspace.model.Workspace;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.springframework.test.annotation.DirtiesContext;

/**
 *
 * @author guisil
 */
public class CreateWorkspacePageTest extends AbstractLamusWicketTest {

    private CreateWorkspacePage createWsPage;
    
    @Mock private WorkspaceTreeService mockWorkspaceServiceBean;
    @Mock private LamusWicketPagesProvider mockPagesProviderBean;
    @Mock private Workspace mockWorkspace;
    @Mock private WorkspacePage mockWorkspacePage;
    @Mock private Collection<String> mockManagerUsers;
    
    private GenericTreeModelProvider mockTreeModelProviderBean;
    private MockCorpusNode mockArchiveRootNode;
    private final String mockArchiveRootNodeName;
    private final URI mockArchiveRootNodeURI;
    private final String mockRegisterUrl = "https://test.mpi.nl/registerUrl";
    private final String mockManualUrl = "http:/test.mpi.nl/lamus/manual";
    
    private MockCorpusNode expectedSelectedNode;

    public CreateWorkspacePageTest() throws URISyntaxException {
        mockArchiveRootNodeName = "RootNode";
        mockArchiveRootNodeURI = new URI("root:node:0");
    }

    @Override
    protected void setUpTest() throws Exception {
        
        MockitoAnnotations.initMocks(this);
        
        mockTreeModelProviderBean = new LinkedTreeModelProvider(mockArchiveRootNode());
        
        when(mockWorkspaceServiceBean.createWorkspace(AbstractLamusWicketTest.MOCK_USER_ID, expectedSelectedNode.getNodeURI())).thenReturn(mockWorkspace);
        when(mockPagesProviderBean.getWorkspacePage(mockWorkspace)).thenReturn(mockWorkspacePage);
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_SERVICE, mockWorkspaceServiceBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_CREATE_WORKSPACE_TREE_PROVIDER, mockTreeModelProviderBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_PAGES_PROVIDER, mockPagesProviderBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_REGISTER_URL, mockRegisterUrl);
        addMock(AbstractLamusWicketTest.BEAN_NAME_MANUAL_URL, mockManualUrl);
        addMock(AbstractLamusWicketTest.BEAN_NAME_MANAGER_USERS, mockManagerUsers);
        
        createWsPage = new CreateWorkspacePage();
        getTester().startPage(createWsPage);
    }

    @Override
    protected void tearDownTest() throws Exception {
        
    }
    
    //TODO Tests are still very incomplete
    
    @Test
    @DirtiesContext
    public void pageRendered() {
        
        getTester().assertRenderedPage(CreateWorkspacePage.class);
    }
    
    @Test
    @DirtiesContext
    public void componentsRendered() {
        
        getTester().assertComponent("archiveTree", ArchiveTreePanel.class);
        getTester().assertEnabled("archiveTree");
        
        getTester().assertComponent("nodeIdForm", Form.class);
        getTester().assertEnabled("nodeIdForm");

        getTester().assertComponent("nodeIdForm:name", Label.class);
        getTester().assertEnabled("nodeIdForm:name");
        getTester().assertLabel("nodeIdForm:name", "");
        
        getTester().assertComponent("nodeIdForm:nodeURI", Label.class);
        getTester().assertEnabled("nodeIdForm:nodeURI");
        getTester().assertLabel("nodeIdForm:nodeURI", "");
        
        getTester().assertComponent("nodeIdForm:createWorkspace", AjaxButton.class);
        getTester().assertDisabled("nodeIdForm:createWorkspace"); //initially disabled
    }
    
    @Test
    @DirtiesContext
    public void formSubmitted() throws NodeAccessException, WorkspaceImportException, NodeNotFoundException {
        
        getTester().getComponentFromLastRenderedPage("nodeIdForm:createWorkspace").setEnabled(true);
        
        Form<CorpusNode> form = (Form<CorpusNode>) getTester().getComponentFromLastRenderedPage("nodeIdForm");
        form.setModel(new CompoundPropertyModel<CorpusNode>(expectedSelectedNode));
        
        FormTester formTester = getTester().newFormTester("nodeIdForm", false);
        
        formTester.submit("createWorkspace");

        verify(mockWorkspaceServiceBean).createWorkspace(AbstractLamusWicketTest.MOCK_USER_ID, expectedSelectedNode.getNodeURI());
        verify(mockPagesProviderBean).getWorkspacePage(mockWorkspace);

        getTester().assertRenderedPage(WorkspacePage.class);
    }
    
    
    private LinkedTreeNode mockArchiveRootNode() throws URISyntaxException {

        mockArchiveRootNode = new MockCorpusNode();
        mockArchiveRootNode.setName(mockArchiveRootNodeName);
        mockArchiveRootNode.setNodeURI(mockArchiveRootNodeURI);

        List<LinkedCorpusNode> children = new ArrayList<>();

        MockCorpusNode child1 = new MockCorpusNode();
        child1.setParent(mockArchiveRootNode);
        child1.setName("Child1");
        child1.setNodeURI(new URI("child:node:1"));
        children.add(child1);

        MockCorpusNode child2 = new MockCorpusNode();
        child2.setParent(mockArchiveRootNode);
        child2.setName("Child2");
        child2.setNodeURI(new URI("child:node:2"));
        children.add(child2);

        mockArchiveRootNode.setChildren(children);
        
        expectedSelectedNode = child1;

        return mockArchiveRootNode;
    }
}