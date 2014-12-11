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

import java.util.Collection;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.web.unlinkednodes.providers.UnlinkedNodesModelProviderFactory;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author guisil
 */
public class LinkNodesPanel extends FeedbackPanelAwarePanel {
    
    @SpringBean
    protected WorkspaceTreeService workspaceService;
    
    @SpringBean(name = "unlinkedNodesProviderFactory")
    private UnlinkedNodesModelProviderFactory providerFactory;
    
    private IModel<WorkspaceTreeNode> model;
    
    private UnlinkedNodesPanel unlinkedNodesPanel;
    private ExternalNodesPanel externalNodesPanel;
    
    private Workspace currentWorkspace;
    
    
    public LinkNodesPanel(String id, IModel<WorkspaceTreeNode> model, Workspace currentWs, FeedbackPanel feedbackPanel) {
        super(id, model, feedbackPanel);
        this.model = model;
        
        this.currentWorkspace = currentWs;
        
        unlinkedNodesPanel =
                new UnlinkedNodesPanel(
                    "unlinkedNodesPanel",
                    new Model<>(currentWorkspace), providerFactory.createTreeModelProvider(workspaceService, currentWs.getWorkspaceID()),
                    feedbackPanel);
        add(unlinkedNodesPanel);

        externalNodesPanel = new ExternalNodesPanel("externalNodesPanel", new Model<>(currentWorkspace), feedbackPanel) {

            @Override
            public void addComponentToTarget(AjaxRequestTarget target) {
                target.add(unlinkedNodesPanel);
            }
        };
        add(externalNodesPanel);
    }
    
    
    // to override from the page where the panel is included
    protected void refreshTreeAndPanels() {

    }
    
    public Collection<WorkspaceTreeNode> getSelectedUnlinkedNodes() {
        return unlinkedNodesPanel.getSelectedUnlinkedNodes();
    }
}
