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
import java.util.List;
import nl.mpi.lamus.web.components.ButtonPanel;
import nl.mpi.archiving.tree.LinkedTreeModelProvider;
import nl.mpi.archiving.tree.wicket.components.ArchiveTreePanel;
import nl.mpi.archiving.tree.wicket.components.ArchiveTreePanelListener;
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.web.components.LinkNodesPanel;
import nl.mpi.lamus.web.components.NodeInfoPanel;
import nl.mpi.lamus.web.components.UploadPanel;
import nl.mpi.lamus.web.components.WorkspaceInfoPanel;
import nl.mpi.lamus.web.components.WsNodeActionsPanel;
import nl.mpi.lamus.web.unlinkednodes.providers.UnlinkedNodesModelProviderFactory;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import nl.mpi.lamus.workspace.tree.implementation.WorkspaceTreeModelProviderFactory;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
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
 */
public class WorkspacePage extends LamusPage {

    // Services to be injected
    @SpringBean
    protected WorkspaceTreeService workspaceTreeService;
    @SpringBean(name = "workspaceTreeProviderFactory")
    private WorkspaceTreeModelProviderFactory workspaceTreeProviderFactory;
    @SpringBean(name = "unlinkedNodesProviderFactory")
    private UnlinkedNodesModelProviderFactory unlinkedNodesProviderFactory;
    
    
    // Page model
    private final IModel<Workspace> model;
//    private final Form<WorkspaceTreeNode> nodeIdForm;
//    private final NodeInfoPanel nodeInfoPanel;
    private ArchiveTreePanel wsTreePanel;
    private final WsNodeActionsPanel wsNodeActionsPanel;
    private LinkNodesPanel linkNodesPanel;
    
    private WorkspaceInfoPanel wsInfoPanel;
    
    //TODO Make it possible to have multiple selection
//    private WorkspaceTreeNode selectedNode;

    public WorkspacePage(final IModel<Workspace> model) {
	super();
	this.model = model;
//	nodeIdForm = createNodeInfoForm("nodeInfoForm");
//        nodeInfoPanel = new NodeInfoPanel("nodeInfoPanel");
//        nodeInfoPanel.setOutputMarkupId(true);
//        add(nodeInfoPanel);

//	add(createWorkspaceInfo("workspaceInfo"));
//        add(new WorkspaceInfoPanel("workspaceInfoPanel", model));

	wsTreePanel = createWorkspaceTreePanel("workspaceTree");
	add(wsTreePanel);
	add(new ButtonPanel("buttonPanel", model));

	wsNodeActionsPanel = new WsNodeActionsPanel("wsNodeActionsPanel", new CollectionModel<WorkspaceTreeNode>(wsTreePanel.getSelectedNodes())) {
	    @Override
	    public void refreshStuff() {
		WorkspacePage.this.refreshStuff();
	    }
            
	};

	wsNodeActionsPanel.setOutputMarkupId(true);
	add(wsNodeActionsPanel);
        
        
        
        List<AbstractTab> tabs = new ArrayList<AbstractTab>();
        tabs.add(new AbstractTab(new Model<String>("Workspace Info")) {
            @Override
            public WebMarkupContainer getPanel(String panelId) {
                if(wsInfoPanel == null) {
                    wsInfoPanel = new WorkspaceInfoPanel(panelId, model);
                    wsInfoPanel.setOutputMarkupId(true);
                }
                return wsInfoPanel;
            }
        });
        tabs.add(new AbstractTab(new Model<String>("Unlinked Nodes")) {
            @Override
            public WebMarkupContainer getPanel(String panelId) {
//                return new UnlinkedNodesPanel(panelId, model, unlinkedNodesProviderFactory.createTreeModelProvider(model.getObject().getWorkspaceID()));
                
                
                //TODO can't really assume that only one will be selected - block linking from multiple nodes for now...
//                IModel<WorkspaceTreeNode> selectedNodeModel;
//                if(!wsTreePanel.getSelectedNodes().isEmpty()) {
//                    WorkspaceTreeNode selectedNode = (WorkspaceTreeNode) wsTreePanel.getSelectedNodes().iterator().next();
//                    selectedNodeModel = new Model<WorkspaceTreeNode>(selectedNode);
//                } else {
//                    selectedNodeModel = new Model<WorkspaceTreeNode>();
//                }
//                
//                linkNodesPanel = new LinkNodesPanel(panelId, selectedNodeModel, model.getObject()) {
//
//                    @Override
//                    protected void refreshStuff() {
//                        super.refreshStuff(); //To change body of generated methods, choose Tools | Templates.
//                    }
//                    
//                };
//                
//                linkNodesPanel.setOutputMarkupId(true);
//                
//                return linkNodesPanel;
                
                
                return createLinkNodesPanel(panelId);
                
            }
        });
        tabs.add(new AbstractTab(new Model<String>("Upload Files")) {
            @Override
            public WebMarkupContainer getPanel(String panelId) {
                return new UploadPanel(panelId, model);
            }
        });
        add(new TabbedPanel("workspaceTabs", tabs));
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
        LinkedTreeModelProvider workspaceTreeProvider = this.workspaceTreeProviderFactory.createTreeModelProvider(rootNode);

	ArchiveTreePanel treePanel = new ArchiveTreePanel(id, workspaceTreeProvider);
	treePanel.addArchiveTreePanelListener(new ArchiveTreePanelListener() {
	    @Override
	    public void nodeSelectionChanged(AjaxRequestTarget target, ArchiveTreePanel treePanel) {
		
//                final WorkspaceTreeNode node = (WorkspaceTreeNode) treePanel.getSelectedNodes().iterator().next();
                
                WorkspaceTreeNode selectedNode = getSelectedNode(treePanel);
                IModel<WorkspaceTreeNode> nodeInfoModel = getSelectedNodesModel(selectedNode);
                
//                nodeIdForm.setModel(new CompoundPropertyModel<WorkspaceTreeNode>(node));
//                nodeInfoPanel.setDefaultModel(nodeInfoModel);
                
                
                wsInfoPanel.setNodeInfoPanelModel(nodeInfoModel);
                
                
		wsNodeActionsPanel.setModelObject(wsTreePanel.getSelectedNodes());
                
                
                
                if(linkNodesPanel != null) {
                    
                    if(selectedNode != null) {
//                    WorkspaceTreeNode selectedNode = (WorkspaceTreeNode) wsTreePanel.getSelectedNodes().iterator().next();
                        linkNodesPanel.setModelObject(selectedNode);
                        
                    } else {
                        linkNodesPanel.setModelObject(null);
                        
                    }
                }

                
                
		if (target != null) {
//		    target.add(nodeIdForm);
//                    target.add(nodeInfoPanel);
                    
                    if(wsInfoPanel != null) {
//                        target.add(wsInfoPanel);
                        target.add(wsInfoPanel.getNodeInfoPanel());
                    }
                    
                    
		    target.add(wsNodeActionsPanel);
                    
                    if(linkNodesPanel != null) {
                        target.add(linkNodesPanel);
                    }
		}
	    }
	});
	treePanel.setLinkType(LinkType.AJAX_FALLBACK);
	return treePanel;
    }

    /**
     * Collect information about the workspace
     *
     * @param id
     * @return WebMarkupContainer
     */
//    private WebMarkupContainer createWorkspaceInfo(String id) {
//	WebMarkupContainer wsInfo = new WebMarkupContainer(id, new CompoundPropertyModel<Workspace>(model));
//	wsInfo.add(new Label("userID"));
//	wsInfo.add(new Label("workspaceID"));
//	wsInfo.add(new Label("status"));
//	return wsInfo;
//    }

    /**
     * Creates and adds node id form
     *
     * @param id
     * @return Form
     */
//    private Form<WorkspaceTreeNode> createNodeInfoForm(final String id) {
//	final Form<WorkspaceTreeNode> form = new Form<WorkspaceTreeNode>(id);
//	form.add(new Label("name"));
//	form.add(new Label("archiveURI"));
//	form.add(new Label("archiveURL"));
//	form.add(new Label("workspaceID"));
//	form.add(new Label("type"));
//
//	// Put details/submit form in container for refresh through AJAX 
//	final MarkupContainer formContainer = new WebMarkupContainer("nodeInfoContainer");
//	formContainer.add(form);
//	// Add container to page
//	add(formContainer);
//
//	return form;
//    }
    
    
    
    
    private LinkNodesPanel createLinkNodesPanel(String panelId) {
        
        IModel<WorkspaceTreeNode> selectedNodeModel;
        if(!wsTreePanel.getSelectedNodes().isEmpty()) {
            WorkspaceTreeNode selectedNode = (WorkspaceTreeNode) wsTreePanel.getSelectedNodes().iterator().next();
            selectedNodeModel = new Model<WorkspaceTreeNode>(selectedNode);
        } else {
            selectedNodeModel = new Model<WorkspaceTreeNode>();
        }
        
        linkNodesPanel = new LinkNodesPanel(panelId, selectedNodeModel, model.getObject()) {
            
            @Override
            protected void refreshStuff() {
                WorkspacePage.this.refreshStuff();
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
        
        IModel<WorkspaceTreeNode> nodeInfoModel = null;
        if(selectedNode != null) {
            nodeInfoModel = new CompoundPropertyModel<WorkspaceTreeNode>(selectedNode);
        } else {
            nodeInfoModel = new Model<WorkspaceTreeNode>();
        }
        return nodeInfoModel;
    }
    
    
    protected void refreshStuff() {
//        wsTreePanel = createWorkspaceTreePanel("workspaceTree");
//        addOrReplace(wsTreePanel);
        //TODO some other way of updating the tree?
        wsTreePanel.getTree().invalidateAll();
        
        
        if(linkNodesPanel != null) {
            linkNodesPanel.render();
        }
    }
}
