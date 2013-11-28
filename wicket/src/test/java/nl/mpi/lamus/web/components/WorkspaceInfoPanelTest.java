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

import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.AbstractLamusWicketTest;
import nl.mpi.lamus.web.model.WorkspaceModel;
import nl.mpi.lamus.web.model.mock.MockWorkspace;
import nl.mpi.lamus.web.model.mock.MockWorkspaceTreeNode;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.CompoundPropertyModel;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
public class WorkspaceInfoPanelTest extends AbstractLamusWicketTest {
    
    private WorkspaceInfoPanel wsInfoPanel;
    
    @Mock private WorkspaceService mockWorkspaceService;
    
    
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
        
        when(mockWorkspaceService.getWorkspace(mockWorkspaceID)).thenReturn(mockWorkspace);
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_SERVICE, mockWorkspaceService);
        
        wsInfoPanel = new WorkspaceInfoPanel("workspaceInfoPanel", new WorkspaceModel(mockWorkspace));
        getTester().startComponentInPage(wsInfoPanel);
    }

    @Override
    protected void tearDownTest() throws Exception {
        
    }
    
    
    @Test
    @DirtiesContext
    public void componentsRendered() {
        
        getTester().assertComponent("workspaceInfoPanel:workspaceInfoContainer", WebMarkupContainer.class);
        getTester().assertEnabled("workspaceInfoPanel:workspaceInfoContainer");
        
        getTester().assertComponent("workspaceInfoPanel:workspaceInfoContainer:userID", Label.class);
        getTester().assertEnabled("workspaceInfoPanel:workspaceInfoContainer:userID");
        getTester().assertLabel("workspaceInfoPanel:workspaceInfoContainer:userID", mockWorkspace.getUserID());
        
        getTester().assertComponent("workspaceInfoPanel:workspaceInfoContainer:workspaceID", Label.class);
        getTester().assertEnabled("workspaceInfoPanel:workspaceInfoContainer:workspaceID");
        getTester().assertLabel("workspaceInfoPanel:workspaceInfoContainer:workspaceID", "" + mockWorkspace.getWorkspaceID());
        
        getTester().assertComponent("workspaceInfoPanel:workspaceInfoContainer:status", Label.class);
        getTester().assertEnabled("workspaceInfoPanel:workspaceInfoContainer:status");
        getTester().assertLabel("workspaceInfoPanel:workspaceInfoContainer:status", "" + mockWorkspace.getStatus());
    }
}