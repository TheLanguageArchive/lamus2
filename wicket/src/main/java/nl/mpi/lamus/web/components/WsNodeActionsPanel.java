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
import java.util.List;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.model.ClearSelectedTreeNodes;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.web.unlinkednodes.model.ClearAllSelectedNodes;
import nl.mpi.lamus.web.unlinkednodes.model.ClearSelectedUnlinkedNodes;
import nl.mpi.lamus.web.unlinkednodes.model.SelectedUnlinkedNodesWrapper;
import nl.mpi.lamus.workspace.actions.WsNodeActionsProvider;
import nl.mpi.lamus.workspace.actions.WsTreeNodesAction;
import nl.mpi.lamus.workspace.actions.implementation.DeleteNodesAction;
import nl.mpi.lamus.workspace.actions.implementation.ReplaceNodesAction;
import nl.mpi.lamus.workspace.actions.implementation.UnlinkNodesAction;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Panel containing the workspace node action buttons.
 * @author guisil
 */
public class WsNodeActionsPanel extends FeedbackPanelAwarePanel<Collection<WorkspaceTreeNode>> {

    @SpringBean
    private WsNodeActionsProvider nodeActionsProvider;
    @SpringBean
    private WorkspaceService workspaceService;
    @SpringBean
    private WorkspaceDao workspaceDao;
    @SpringBean
    private NodeUtil nodeUtil;
    private final Form<Collection<WorkspaceTreeNode>> form;
    
    private Collection<WorkspaceTreeNode> selectedUnlinkedNodes;

    public WsNodeActionsPanel(String id, IModel<Collection<WorkspaceTreeNode>> model, FeedbackPanel feedbackPanel) {
    	super(id, model, feedbackPanel);
    	form = new Form<>("wsNodeActionsForm", model);
        form.add(createListView(nodeActionsProvider.getActions(model.getObject())));
        add(form);
    }

    /**
     * @see FeedbackPanelAwarePanel#onModelChanged()
     */
    @Override
    protected void onModelChanged() {
        super.onModelChanged(); //To change body of generated methods, choose Tools | Templates.
        
        form.setModelObject(getModelObject());
        form.addOrReplace(createListView(nodeActionsProvider.getActions(getModelObject())));
    }

    
    private ListView<WsTreeNodesAction> createListView(List<WsTreeNodesAction> nodeActionsList) {
        
        return new ListView<WsTreeNodesAction>("wsNodeActions", nodeActionsList) {

            @Override
            protected void populateItem(ListItem<WsTreeNodesAction> li) {
                
                Button nodeActionButton = new WsNodeActionButton(
                        "nodeActionButton", li.getModelObject(),
                        WsNodeActionsPanel.this.workspaceService,
                        WsNodeActionsPanel.this.workspaceDao,
                        WsNodeActionsPanel.this.nodeUtil) {

                    @Override
                    public void refreshStuff() {
                        WsNodeActionsPanel.this.refreshTreeAndPanels();
                    }

                    @Override
                    public void refreshSelectedUnlinkedNodes() {
                        WsNodeActionsPanel.this.refreshSelectedUnlinkedNodes();
                    }

                    @Override
                    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                        
                        final String currentUserId = LamusSession.get().getUserId();
                        try {

                            target.add(getFeedbackPanel());
                            
                            setActionParameters(WsNodeActionsPanel.this.getModelObject(), selectedUnlinkedNodes);

                            if(getAction() instanceof UnlinkNodesAction || getAction() instanceof DeleteNodesAction) {
                                //tell the page to clear the selected nodes from the tree
                                send(this, Broadcast.BUBBLE, new ClearSelectedTreeNodes());
                            }

                            executeAction(currentUserId);

                            //tell the unlinked nodes panel to clear the selected unlinked nodes
                            if(getAction() instanceof ReplaceNodesAction) {
                                send(this, Broadcast.BUBBLE, new ClearAllSelectedNodes());
                            } else {
                                send(this, Broadcast.BUBBLE, new ClearSelectedUnlinkedNodes());
                            }

                            //clear also the variable stored in the panel
                            clearSelectedUnlinkedNodes();

                        } catch(WorkspaceException | IllegalArgumentException | ProtectedNodeException ex) {
                            error(ex.getMessage());
                        }

                        target.add(WsNodeActionsPanel.this.getPage().get("workspaceTree"));
                        target.add(WsNodeActionsPanel.this.getPage().get("workspaceTabs"));
                        target.add(WsNodeActionsPanel.this);

                        refreshStuff();
                    }
                };
                
                nodeActionButton.add(AttributeModifier.append("class", new Model<>(getIconNameForNodeAction(li.getModelObject()))));
                nodeActionButton.add(new Label("nodeActionLabel", getLocalizer().getString(nodeActionButton.getModelObject(), this)));
                
                li.add(nodeActionButton);
            }
        };
    }
    
    /**
     * To override from the page where the panel is included.
     */
    public void refreshTreeAndPanels() {
    }
    
    /**
    * To override from the page where the panel is included.
    */
    public void refreshSelectedUnlinkedNodes() {
    }
    
    /**
     * Sets the currently selected unlinked nodes.
     * @param selectedUnlinkedNodes Collection of selected unlinked nodes
     */
    public void setSelectedUnlinkedNodes(Collection<WorkspaceTreeNode> selectedUnlinkedNodes) {
        this.selectedUnlinkedNodes = selectedUnlinkedNodes;
    }


    private void clearSelectedUnlinkedNodes() {
        setSelectedUnlinkedNodes(new ArrayList<WorkspaceTreeNode>());
    }
    
    private String getIconNameForNodeAction(WsTreeNodesAction action) {
        
        if("unlink_node_action".equals(action.getName())) {
            return "icon-unlink_node";
        }
        if("delete_node_action".equals(action.getName())) {
            return "icon-delete_node";
        }
        if("link_node_action".equals(action.getName())) {
            return "icon-link_node";
        }
        if("link_node_info_action".equals(action.getName())) {
            return "icon-link_node";
        }
        if("replace_node_action".equals(action.getName())) {
            return "icon-replace_node";
        }
        
        return "";
    }
    
    /**
     * @see FeedbackPanelAwarePanel#onEvent(org.apache.wicket.event.IEvent)
     */
    @Override
    public void onEvent(IEvent<?> event) {
        
        if(event.getPayload() instanceof SelectedUnlinkedNodesWrapper) {
            setSelectedUnlinkedNodes(((SelectedUnlinkedNodesWrapper) event.getPayload()).getSelectedUnlinkedNodes());
        }
    }
}
