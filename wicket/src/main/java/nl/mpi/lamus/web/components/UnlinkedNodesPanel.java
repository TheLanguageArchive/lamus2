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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.pages.LamusPage;
import nl.mpi.lamus.web.unlinkednodes.model.WorkspaceTreeNodeExpansion;
import nl.mpi.lamus.web.unlinkednodes.providers.UnlinkedNodesModelProvider;
import nl.mpi.lamus.web.unlinkednodes.tree.CheckedFolderContent;
import nl.mpi.lamus.web.unlinkednodes.tree.Content;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.DefaultTableTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.TableTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.CheckedFolder;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.extensions.markup.html.repeater.tree.table.NodeModel;
import org.apache.wicket.extensions.markup.html.repeater.tree.table.TreeColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.ProviderSubset;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author guisil
 */
public class UnlinkedNodesPanel extends GenericPanel<Workspace> {
    
    public static final PackageResourceReference DELETE_IMAGE_RESOURCE_REFERENCE = new PackageResourceReference(LamusPage.class, "delete.gif");
    
    @SpringBean
    private WorkspaceService workspaceService;
    
    private AbstractTree<WorkspaceTreeNode> unlinkedNodesTree;
    
//    private CheckGroup<WorkspaceTreeNode> checkgroup;
    
//    private ProviderSubset<WorkspaceTreeNode> checked;
    private Collection<WorkspaceTreeNode> checked;
    
//    private CheckedFolderContent content;
    
    
    public UnlinkedNodesPanel(String id, IModel<Workspace> model, UnlinkedNodesModelProvider provider) {
        super(id, model);
        
        File uploadFolder = workspaceService.getWorkspaceUploadDirectory(getModelObject().getWorkspaceID());
        
        add(new Label("dir", uploadFolder.getAbsolutePath()));
        
//        checked = new ProviderSubset<WorkspaceTreeNode>(provider, true);
        checked = new ArrayList<WorkspaceTreeNode>();
        
//        content = new CheckedFolderContent(provider);
        
//        checkgroup = new CheckGroup<WorkspaceTreeNode>("checkgroup", new ArrayList<WorkspaceTreeNode>()) {
//
//            @Override
//            protected boolean wantOnSelectionChangedNotifications() {
//                return true;
//            }
//
//            @Override
//            protected void onSelectionChanged(Collection<? extends WorkspaceTreeNode> newSelection) {
//                super.onSelectionChanged(newSelection); //To change body of generated methods, choose Tools | Templates.
//                
//                
//                
//                
//                //TODO listener?
//                    // and trigger visibility changes in the page (buttons, etc)
//            }
//            
//            @Override
//            protected void onModelChanged() {
//                super.onModelChanged(); //To change body of generated methods, choose Tools | Templates.
//            }
//        };
        
        unlinkedNodesTree = createTree("unlinkedNodesTableTree", provider, new WorkspaceTreeNodeExpansionModel());
        unlinkedNodesTree.setOutputMarkupPlaceholderTag(true);
        
//        checkgroup.setOutputMarkupId(true);
//        
//        checkgroup.add(unlinkedNodesTree);
//        
//        add(checkgroup);
        
        add(unlinkedNodesTree);
    }
    
    
    protected AbstractTree<WorkspaceTreeNode> createTree(String id, UnlinkedNodesModelProvider provider, IModel<Set<WorkspaceTreeNode>> state)
    {
        List<IColumn<WorkspaceTreeNode, String>> columns = createColumns();

        
        
        //TODO CREATE A SUBCLASS OF TABLE TREE WITH A METHOD THAT RETURNS THE SELECTED NODES FROM WITHIN THE NODES???
        
        
        final TableTree<WorkspaceTreeNode, String> tree = new DefaultTableTree<WorkspaceTreeNode, String>(id, columns, provider, Integer.MAX_VALUE, state) {

            @Override
            protected Component newContentComponent(String id, IModel<WorkspaceTreeNode> model) {
//                return content.newContentComponent(id, unlinkedNodesTree, model);
                
                
                if(model.getObject().getParent() != null) {
                    
                    return new Folder<WorkspaceTreeNode>(id, unlinkedNodesTree, model);
                    
                } else {
                    
                    return new CheckedFolder<WorkspaceTreeNode>(id, unlinkedNodesTree, model) {
                    
                        private static final long serialVersionUID = 1L;
                        
                        @Override
                        protected IModel<Boolean> newCheckBoxModel(final IModel<WorkspaceTreeNode> model) {
                            
                            return new IModel<Boolean>() {
                                private static final long serialVersionUID = 1L;

                                @Override
                                public Boolean getObject() {
                                    return isChecked(model.getObject());
                                }

                                @Override
                                public void setObject(Boolean object) {
                                    check(model.getObject(), object);
                                }

                                @Override
                                public void detach() {
                                }
                            };
                        }
                    };
                }
            }

            @Override
            protected Item<WorkspaceTreeNode> newRowItem(String id, int index, IModel<WorkspaceTreeNode> model) {
                return new OddEvenItem<WorkspaceTreeNode>(id, index, model);
            }
        };

        return tree;
    }
    
    
    private List<IColumn<WorkspaceTreeNode, String>> createColumns()
    {
        List<IColumn<WorkspaceTreeNode, String>> columns = new ArrayList<IColumn<WorkspaceTreeNode, String>>();

        columns.add(new PropertyColumn<WorkspaceTreeNode, String>(Model.of("ID"), "workspaceNodeID"));
        
        columns.add(new TreeColumn<WorkspaceTreeNode, String>(Model.of("Tree")));// {

//            @Override
//            public void populateItem(Item<ICellPopulator<WorkspaceTreeNode>> cellItem, String componentId, IModel<WorkspaceTreeNode> rowModel) {
//                
////                cellItem.add(new CheckBoxPanel(componentId, rowModel));
//                NodeModel<WorkspaceTreeNode> nodeModel = (NodeModel<WorkspaceNode>) rowModel;
//                cellItem.add(new Label(componentId, nodeModel)
//            }
//        });
        
        columns.add(new PropertyColumn<WorkspaceTreeNode, String>(Model.of("Name"), "name"));
        columns.add(new PropertyColumn<WorkspaceTreeNode, String>(Model.of("Type"), "type"));

        return columns;
    }

    
    public Collection<WorkspaceTreeNode> getSelectedUnlinkedNodes() {
        
//        return this.checkgroup.getModelObject();
//        return null;

        return checked;
    }
    
    
    
    
    
    
    private Iterator<WorkspaceTreeNode> getChecked() {
        return checked.iterator();
    }
    
    private boolean isChecked(WorkspaceTreeNode wsTreeNode) {
        return checked.contains(wsTreeNode);
    }

    private void check(WorkspaceTreeNode wsTreeNode, boolean check) {
        if (check && !checked.contains(wsTreeNode)) {
            checked.add(wsTreeNode);
        }
        else {
            checked.remove(wsTreeNode);
        }
    }
    
    
    
    

    private static class WorkspaceTreeNodeExpansionModel extends AbstractReadOnlyModel<Set<WorkspaceTreeNode>> {

        public WorkspaceTreeNodeExpansionModel() {
        }

        @Override
        public Set<WorkspaceTreeNode> getObject() {
            return WorkspaceTreeNodeExpansion.get();
        }
    }
}
