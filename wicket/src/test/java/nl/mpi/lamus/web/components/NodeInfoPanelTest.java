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

import java.net.URI;
import java.net.URL;
import nl.mpi.lamus.web.AbstractLamusWicketTest;
import nl.mpi.lamus.web.model.mock.MockWorkspaceTreeNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.test.annotation.DirtiesContext;

/**
 *
 * @author guisil
 */
public class NodeInfoPanelTest extends AbstractLamusWicketTest {
    
    private NodeInfoPanel nodeInfoPanel;
    
    private int mockWorkspaceID = 1;
    private int mockWorkspaceNodeID = 10;
    private MockWorkspaceTreeNode mockWorkspaceNode = new MockWorkspaceTreeNode() {{
        setWorkspaceID(mockWorkspaceID);
        setWorkspaceNodeID(mockWorkspaceNodeID);
        setName("topNode");
        setType(WorkspaceNodeType.METADATA);
    }};

    @Override
    protected void setUpTest() throws Exception {
        
        mockWorkspaceNode.setArchiveURI(new URI("node:10"));
        mockWorkspaceNode.setArchiveURL(new URL("file:/archive/topNode.cmdi"));
        
        nodeInfoPanel = new NodeInfoPanel("nodeInfoPanel");
        getTester().startComponentInPage(nodeInfoPanel);
    }

    @Override
    protected void tearDownTest() throws Exception {
        
    }
    
    
    @Test
    @DirtiesContext
    public void componentsRendered() {
        
        nodeInfoPanel.setDefaultModel(new CompoundPropertyModel<WorkspaceTreeNode>(mockWorkspaceNode));
        
        getTester().assertComponent("nodeInfoPanel:nodeInfoContainer", WebMarkupContainer.class);
        getTester().assertEnabled("nodeInfoPanel:nodeInfoContainer");
        
        getTester().assertComponent("nodeInfoPanel:nodeInfoContainer:nodeInfoForm", Form.class);
        getTester().assertEnabled("nodeInfoPanel:nodeInfoContainer:nodeInfoForm");
        
        getTester().assertComponent("nodeInfoPanel:nodeInfoContainer:nodeInfoForm:name", Label.class);
        getTester().assertEnabled("nodeInfoPanel:nodeInfoContainer:nodeInfoForm:name");
        getTester().assertLabel("nodeInfoPanel:nodeInfoContainer:nodeInfoForm:name", mockWorkspaceNode.getName());
        
        getTester().assertComponent("nodeInfoPanel:nodeInfoContainer:nodeInfoForm:archiveURI", Label.class);
        getTester().assertEnabled("nodeInfoPanel:nodeInfoContainer:nodeInfoForm:archiveURI");
        getTester().assertLabel("nodeInfoPanel:nodeInfoContainer:nodeInfoForm:archiveURI", mockWorkspaceNode.getArchiveURI().toString());
        
        getTester().assertComponent("nodeInfoPanel:nodeInfoContainer:nodeInfoForm:archiveURL", Label.class);
        getTester().assertEnabled("nodeInfoPanel:nodeInfoContainer:nodeInfoForm:archiveURL");
        getTester().assertLabel("nodeInfoPanel:nodeInfoContainer:nodeInfoForm:archiveURL", mockWorkspaceNode.getArchiveURL().toString());
        
        getTester().assertComponent("nodeInfoPanel:nodeInfoContainer:nodeInfoForm:workspaceID", Label.class);
        getTester().assertEnabled("nodeInfoPanel:nodeInfoContainer:nodeInfoForm:workspaceID");
        getTester().assertLabel("nodeInfoPanel:nodeInfoContainer:nodeInfoForm:workspaceID", "" + mockWorkspaceNode.getWorkspaceID());
        
        getTester().assertComponent("nodeInfoPanel:nodeInfoContainer:nodeInfoForm:type", Label.class);
        getTester().assertEnabled("nodeInfoPanel:nodeInfoContainer:nodeInfoForm:type");
        getTester().assertLabel("nodeInfoPanel:nodeInfoContainer:nodeInfoForm:type", "" + mockWorkspaceNode.getType());
    }
    
    @Test
    @DirtiesContext
    public void updateModelNodeIdForm() {
        
//        Form<WorkspaceTreeNode> nodeIdForm = (Form<WorkspaceTreeNode>) getTester().getComponentFromLastRenderedPage("nodeInfoPanel:nodeInfoContainer:nodeInfoForm");
//        nodeIdForm.setModel(new CompoundPropertyModel<WorkspaceTreeNode>(mockWorkspaceNode));
        
        nodeInfoPanel.setDefaultModel(new CompoundPropertyModel<WorkspaceTreeNode>(mockWorkspaceNode));
        
        getTester().assertLabel("nodeInfoPanel:nodeInfoContainer:nodeInfoForm:name", mockWorkspaceNode.getName());
        getTester().assertLabel("nodeInfoPanel:nodeInfoContainer:nodeInfoForm:archiveURI", mockWorkspaceNode.getArchiveURI().toString());
        getTester().assertLabel("nodeInfoPanel:nodeInfoContainer:nodeInfoForm:archiveURL", mockWorkspaceNode.getArchiveURL().toString());
        getTester().assertLabel("nodeInfoPanel:nodeInfoContainer:nodeInfoForm:workspaceID", "" + mockWorkspaceNode.getWorkspaceID());
        getTester().assertLabel("nodeInfoPanel:nodeInfoContainer:nodeInfoForm:type", mockWorkspaceNode.getType().toString());
    }
}