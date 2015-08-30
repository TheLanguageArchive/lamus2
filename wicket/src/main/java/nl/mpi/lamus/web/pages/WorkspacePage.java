/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nl.mpi.lamus.web.components.ButtonPanel;
import nl.mpi.archiving.tree.LinkedTreeModelProvider;
import nl.mpi.archiving.tree.wicket.components.ArchiveTreeNodeIconProvider;
import nl.mpi.archiving.tree.wicket.components.ArchiveTreePanel;
import nl.mpi.archiving.tree.wicket.components.ArchiveTreePanelListener;
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.web.components.LinkNodesPanel;
import nl.mpi.lamus.web.components.UploadPanel;
import nl.mpi.lamus.web.components.WorkspaceInfoPanel;
import nl.mpi.lamus.web.components.WsNodeActionsPanel;
import nl.mpi.lamus.web.model.ClearSelectedTreeNodes;
import nl.mpi.lamus.web.unlinkednodes.model.ClearSelectedUnlinkedNodes;
import nl.mpi.lamus.web.unlinkednodes.model.SelectedUnlinkedNodesWrapper;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import nl.mpi.lamus.workspace.tree.implementation.WorkspaceTreeModelProviderFactory;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tree.LinkType;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.CollectionModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author guisil
 */
public class WorkspacePage extends LamusPage {

    // Services to be injected
    @SpringBean
    protected WorkspaceTreeService workspaceTreeService;
    @SpringBean(name = "workspaceTreeProviderFactory")
    private WorkspaceTreeModelProviderFactory workspaceTreeProviderFactory;
    
    @SpringBean(required = false)
    private ArchiveTreeNodeIconProvider<WorkspaceTreeNode> treeIconProvider;
    
    // Page model
    private final IModel<Workspace> model;
    private ArchiveTreePanel wsTreePanel;
    private final WsNodeActionsPanel wsNodeActionsPanel;
    private LinkNodesPanel linkNodesPanel;
    
    private WorkspaceInfoPanel wsInfoPanel;
    
    
    private Collection<WorkspaceTreeNode> selectedUnlinkedNodes = new ArrayList<>();
    
    private LinkedTreeModelProvider workspaceTreeProvider;

    
    public WorkspacePage(final IModel<Workspace> model) {
	super();
	this.model = model;

	wsTreePanel = createWorkspaceTreePanel("workspaceTree");
        wsTreePanel.setOutputMarkupId(true);
	add(wsTreePanel);
	add(new ButtonPanel("buttonPanel", model, getFeedbackPanel()));

	wsNodeActionsPanel =
                new WsNodeActionsPanel("wsNodeActionsPanel",
                new CollectionModel<WorkspaceTreeNode>(wsTreePanel.getSelectedNodes()),
                getFeedbackPanel()) {
	    @Override
	    public void refreshTreeAndPanels() {
		WorkspacePage.this.refreshTreeAndPanels();
	    }

            @Override
            public void refreshSelectedUnlinkedNodes() {
                WorkspacePage.this.refreshSelectedUnlinkedNodes();
            }
	};

	wsNodeActionsPanel.setOutputMarkupId(true);
	add(wsNodeActionsPanel);
        
        List<AbstractTab> tabs = new ArrayList<>();
        tabs.add(new AbstractTab(new Model<>(getLocalizer().getString("workspace_info_tab_panel", this))) {
            @Override
            public WebMarkupContainer getPanel(String panelId) {
                if(wsInfoPanel == null) {
                    wsInfoPanel = new WorkspaceInfoPanel(panelId, model);
                    wsInfoPanel.setOutputMarkupId(true);
                }
                return wsInfoPanel;
            }
        });
        tabs.add(new AbstractTab(new Model<>(getLocalizer().getString("link_nodes_tab_panel", this))) {
            @Override
            public WebMarkupContainer getPanel(String panelId) {
                return createLinkNodesPanel(panelId);
            }
        });
        tabs.add(new AbstractTab(new Model<>(getLocalizer().getString("upload_files_tab_panel", this))) {
            @Override
            public WebMarkupContainer getPanel(String panelId) {
                return new UploadPanel(panelId, model, getFeedbackPanel());
            }
        });
        AjaxTabbedPanel tabbedPanel = new AjaxTabbedPanel("workspaceTabs", tabs);
        tabbedPanel.setOutputMarkupId(true);
        add(tabbedPanel);
    }

    /**
     * Creates and adds an tree panel to be display in the opened/created workspace
     *
     * @param id
     * @return ArchiveTreePanel
     */
    private ArchiveTreePanel createWorkspaceTreePanel(String id) {
	WorkspaceTreeNode rootNode = null;
        try {
            rootNode = this.workspaceTreeService.getTreeNode(this.model.getObject().getTopNodeID(), null);
        } catch (WorkspaceNodeNotFoundException ex) {
            Session.get().error(ex.getMessage());
        }
        workspaceTreeProvider = this.workspaceTreeProviderFactory.createTreeModelProvider(rootNode);

	ArchiveTreePanel treePanel = new ArchiveTreePanel(id, workspaceTreeProvider, treeIconProvider);
        
        
	treePanel.addArchiveTreePanelListener(new ArchiveTreePanelListener() {
	    @Override
	    public void nodeSelectionChanged(AjaxRequestTarget target, ArchiveTreePanel treePanel) {
	        
                WorkspaceTreeNode selectedNode = getSelectedNode(treePanel);
                IModel<WorkspaceTreeNode> nodeInfoModel = getSelectedNodesModel(selectedNode);
                
                if(wsInfoPanel != null) {
                    wsInfoPanel.setNodeInfoPanelModel(nodeInfoModel);
                }
                
                wsNodeActionsPanel.setSelectedUnlinkedNodes(selectedUnlinkedNodes);
                wsNodeActionsPanel.setModel(new CollectionModel<WorkspaceTreeNode>(wsTreePanel.getSelectedNodes()));
                
                if(linkNodesPanel != null) {
                    
                    if(selectedNode != null) {
                        linkNodesPanel.setModel(Model.of(selectedNode));
                    } else {
                        linkNodesPanel.setModelObject(null);
                    }
                }
                
		if (target != null) {
                    
                    if(wsInfoPanel != null) {
                        target.add(wsInfoPanel.getNodeInfoPanel());
                    }
                    
		    target.add(wsNodeActionsPanel);
                    
                    send(wsNodeActionsPanel, Broadcast.BREADTH, selectedUnlinkedNodes);
                    
                    if(linkNodesPanel != null) {
                        target.add(linkNodesPanel);
                    }
		}
	    }
	});
	treePanel.setLinkType(LinkType.AJAX_FALLBACK);
	return treePanel;
    }
    
    private LinkNodesPanel createLinkNodesPanel(String panelId) {
        
        IModel<WorkspaceTreeNode> selectedNodeModel;
        if(!wsTreePanel.getSelectedNodes().isEmpty()) {
            WorkspaceTreeNode selectedNode = (WorkspaceTreeNode) wsTreePanel.getSelectedNodes().iterator().next();
            selectedNodeModel = new Model<>(selectedNode);
        } else {
            selectedNodeModel = new Model<>();
        }
        
        linkNodesPanel = new LinkNodesPanel(panelId, selectedNodeModel, model.getObject(), getFeedbackPanel()) {

            @Override
            protected void refreshTreeAndPanels() {
                WorkspacePage.this.refreshTreeAndPanels();
            }
        };
        linkNodesPanel.setOutputMarkupId(true);
        return linkNodesPanel;
    }
    
    private WorkspaceTreeNode getSelectedNode(ArchiveTreePanel treePanel) {
        WorkspaceTreeNode selectedNode = null;
        if(!treePanel.getSelectedNodes().isEmpty()) {
            selectedNode = (WorkspaceTreeNode) treePanel.getSelectedNodes().iterator().next();
        }
        return selectedNode;
    }
    
    private IModel<WorkspaceTreeNode> getSelectedNodesModel(WorkspaceTreeNode selectedNode) {
        
        IModel<WorkspaceTreeNode> nodeInfoModel;
        if(selectedNode != null) {
            nodeInfoModel = new CompoundPropertyModel<>(selectedNode);
        } else {
            nodeInfoModel = new Model<>();
        }
        return nodeInfoModel;
    }
    
    
    protected void refreshTreeAndPanels() {
        wsTreePanel.getTree().invalidateAll();
    }
    
    protected void refreshSelectedUnlinkedNodes() {
        if(linkNodesPanel != null) {
            wsNodeActionsPanel.setSelectedUnlinkedNodes(linkNodesPanel.getSelectedUnlinkedNodes());
        }
    }

    @Override
    public void onEvent(IEvent<?> event) {
        
        if(event.getPayload() instanceof SelectedUnlinkedNodesWrapper) {
            selectedUnlinkedNodes = ((SelectedUnlinkedNodesWrapper)event.getPayload()).getSelectedUnlinkedNodes();
            send(wsNodeActionsPanel, Broadcast.BREADTH, event.getPayload());
        }
        if(event.getPayload() instanceof ClearSelectedUnlinkedNodes) {
            selectedUnlinkedNodes = new ArrayList<>();
            send(linkNodesPanel, Broadcast.BREADTH, event.getPayload());
        }
        
        if(event.getPayload() instanceof ClearSelectedTreeNodes) {

            for(Object node : wsTreePanel.getTree().getTreeState().getSelectedNodes()) {
                wsTreePanel.getTree().getTreeState().selectNode(node, false);
            }
            
            wsNodeActionsPanel.setModelObject(new ArrayList<WorkspaceTreeNode>());
        }
    }
}
