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

import java.io.File;
import nl.mpi.archiving.tree.LinkedTreeModelProvider;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.web.AbstractLamusWicketTest;
import nl.mpi.lamus.web.model.mock.MockWorkspace;
import nl.mpi.lamus.web.model.mock.MockWorkspaceTreeNode;
import nl.mpi.lamus.web.pages.IndexPage;
import nl.mpi.lamus.web.pages.providers.LamusWicketPagesProvider;
import nl.mpi.lamus.workspace.actions.WsNodeActionsProvider;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import nl.mpi.lamus.workspace.tree.implementation.WorkspaceTreeModelProviderFactory;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.springframework.test.annotation.DirtiesContext;

/**
 *
 * @author guisil
 */
public class ButtonPanelTest extends AbstractLamusWicketTest {
    
    private ButtonPanel buttonPanel;
    
    @Mock private WorkspaceTreeService mockWorkspaceServiceBean;
    @Mock private WsNodeActionsProvider mockTreeNodeActionsProviderBean;
    
    @Mock private LamusWicketPagesProvider mockPagesProviderBean;
    
    @Mock private LinkedTreeModelProvider mockTreeModelProvider;
    
    @Mock private WorkspaceTreeModelProviderFactory mockWorkspaceTreeModelProviderFactoryBean;
    
    //TODO request storage
    @Mock private IndexPage mockIndexPage;
    
    
    TemporaryFolder testFolder = new TemporaryFolder();
    private File mockUploadDirectory;
    
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
    }};
    

    @Override
    protected void setUpTest() throws Exception {
        
        MockitoAnnotations.initMocks(this);
        
        when(mockWorkspaceServiceBean.getWorkspace(mockWorkspaceID)).thenReturn(mockWorkspace);
        when(mockPagesProviderBean.getIndexPage()).thenReturn(mockIndexPage);
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_SERVICE, mockWorkspaceServiceBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_PAGES_PROVIDER, mockPagesProviderBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_TREE_MODEL_PROVIDER_FACTORY, mockWorkspaceTreeModelProviderFactoryBean);
        
        buttonPanel = new ButtonPanel("buttonpanel", mockWorkspace);
        getTester().startComponentInPage(buttonPanel);
    }

    @Override
    protected void tearDownTest() throws Exception {
        
    }
    
    
    @Test
    @DirtiesContext
    public void componentsRendered() {
    
        getTester().assertComponent("buttonpanel:workspaceActionsForm", Form.class);
        getTester().assertEnabled("buttonpanel:workspaceActionsForm");
        
        getTester().assertComponent("buttonpanel:workspaceActionsForm:requestStorageButton", Button.class);
        getTester().assertEnabled("buttonpanel:workspaceActionsForm:requestStorageButton");
        
        //TODO other buttons
        
        getTester().assertComponent("buttonpanel:workspaceActionsForm:deleteWorkspaceButton", Button.class);
        getTester().assertEnabled("buttonpanel:workspaceActionsForm:deleteWorkspaceButton");
        
        //TODO other buttons
        
        getTester().assertComponent("buttonpanel:workspaceActionsForm:indexPageButton", Button.class);
        getTester().assertEnabled("buttonpanel:workspaceActionsForm:indexPageButton");
    }
    
    //TODO handle request storage
    
    
    @Test
    @DirtiesContext
    public void deleteWorkspaceButtonClicked() {
        
        getFormTester().submit("deleteWorkspaceButton");
        verify(mockWorkspaceServiceBean).deleteWorkspace(AbstractLamusWicketTest.MOCK_USER_ID, mockWorkspaceID);
        verify(mockPagesProviderBean).getIndexPage();
        getTester().assertRenderedPage(IndexPage.class);
    }
    
    
    private FormTester getFormTester() {
        return getTester().newFormTester("buttonpanel:workspaceActionsForm", false);
    }
}