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

import nl.mpi.lamus.web.components.ButtonPanel;
import nl.mpi.archiving.tree.LinkedTreeModelProvider;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.web.AbstractLamusWicketTest;
import nl.mpi.lamus.web.model.WorkspaceModel;
import nl.mpi.lamus.web.model.mock.MockWorkspace;
import nl.mpi.lamus.web.pages.FreeNodesPage;
import nl.mpi.lamus.web.pages.IndexPage;
import nl.mpi.lamus.web.pages.LinkNodesPage;
import nl.mpi.lamus.web.pages.UploadPage;
import nl.mpi.lamus.web.providers.LamusWicketPagesProvider;
import nl.mpi.lamus.workspace.actions.TreeNodeActionsProvider;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.junit.Test;
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
    @Mock private TreeNodeActionsProvider mockTreeNodeActionsProviderBean;
    
    @Mock private LamusWicketPagesProvider mockPagesProviderBean;
    
    @Mock private LinkedTreeModelProvider mockTreeModelProvider;
    
    @Mock private UploadPage mockUploadPage;
    //TODO request storage
//    @Mock private FreeNodesPage mockFreeNodesPage;
//    @Mock private LinkNodesPage mockLinkNodesPage;
//    @Mock private IndexPage mockIndexPage;
    
    private int mockWorkspaceID = 1;
    private int mockWorkspaceTopNodeID = 10;
    private MockWorkspace mockWorkspace = new MockWorkspace() {{
        setUserID(AbstractLamusWicketTest.MOCK_USER_ID);
        setWorkspaceID(mockWorkspaceID);
        setStatus(WorkspaceStatus.INITIALISED);
        setTopNodeID(mockWorkspaceTopNodeID);
    }};
    
//    private WorkspaceModel mockWorkspaceModel = new WorkspaceModel(mockWorkspace);
    

    @Override
    protected void setUpTest() throws Exception {
        
        MockitoAnnotations.initMocks(this);
        
        when(mockWorkspaceServiceBean.getWorkspace(mockWorkspaceID)).thenReturn(mockWorkspace);
//        when(mockPagesProviderBean.getUploadPage(mockWorkspace)).thenReturn(mockUploadPage);
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_SERVICE, mockWorkspaceServiceBean);
        
        buttonPanel = new ButtonPanel("buttonpage", mockWorkspace);
        getTester().startComponentInPage(buttonPanel);
    }

    @Override
    protected void tearDownTest() throws Exception {
        
    }
    
    
    @Test
    @DirtiesContext
    public void componentsRendered() {
    
        getTester().assertComponent("buttonpage:workspaceActionsForm", Form.class);
        getTester().assertEnabled("buttonpage:workspaceActionsForm");
        
        getTester().assertComponent("buttonpage:workspaceActionsForm:uploadFilesButton", Button.class);
        getTester().assertEnabled("buttonpage:workspaceActionsForm:uploadFilesButton");
        
        getTester().assertComponent("buttonpage:workspaceActionsForm:requestStorageButton", Button.class);
        getTester().assertEnabled("buttonpage:workspaceActionsForm:requestStorageButton");
        
        getTester().assertComponent("buttonpage:workspaceActionsForm:unlinkedFilesButton", Button.class);
        getTester().assertEnabled("buttonpage:workspaceActionsForm:unlinkedFilesButton");
        
        getTester().assertComponent("buttonpage:workspaceActionsForm:linkNodesButton", Button.class);
        getTester().assertEnabled("buttonpage:workspaceActionsForm:linkNodesButton");
        
        //TODO other buttons
        
        getTester().assertComponent("buttonpage:workspaceActionsForm:deleteWorkspaceButton", Button.class);
        getTester().assertEnabled("buttonpage:workspaceActionsForm:deleteWorkspaceButton");
        
        //TODO other buttons
        
        getTester().assertComponent("buttonpage:workspaceActionsForm:indexPageButton", Button.class);
        getTester().assertEnabled("buttonpage:workspaceActionsForm:indexPageButton");
    }
    
    @Test
    @DirtiesContext
    public void buttonsClicked() {
        //TODO trigger button clicks
    }
}