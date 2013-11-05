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
import java.util.List;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.pages.LamusPage;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
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
    
        /**
     * List view for files in upload folder.
     */
    private class WorkspaceNodeListView extends ListView<WorkspaceNode> {

        /**
         * Construct.
         *
         * @param name Component name
         * @param files The file list model
         */
        public WorkspaceNodeListView(String name, final IModel<List<WorkspaceNode>> nodes) {
            super(name, nodes);
        }

        /**
         * @see ListView#populateItem(ListItem) Add clickable icon to remove
         * unwanted uploaded files
         */
        @Override
        protected void populateItem(ListItem<WorkspaceNode> listItem) {
//            final File file = listItem.getModelObject();
//            listItem.add(new Label("file", file.getName()));
            
            final WorkspaceNode node = listItem.getModelObject();
            listItem.add(new Label("node", node.getName()));
            
            Link link = new Link("delete") {

                @Override
                public void onClick() {
//                    Files.remove(file);
//                    info("Deleted " + file);
                    workspaceService.deleteNode(LamusSession.get().getUserId(), node);
                    info("Deleted " + node.getName());
                }
            };
            link.add(new Image("image2", DELETE_IMAGE_RESOURCE_REFERENCE));
            listItem.add(link);
        }
    }
    
    public UnlinkedNodesPanel(String id, IModel<Workspace> model) {
        super(id, model);
        
        File uploadFolder = workspaceService.getWorkspaceUploadDirectory(getModelObject().getWorkspaceID());
        
        
        add(new Label("dir", uploadFolder.getAbsolutePath()));
        
        WorkspaceNodeListView nodeListView = new WorkspaceNodeListView("unlinkedNodesList", new LoadableDetachableModel<List<WorkspaceNode>>() {
            
            @Override
            protected List<WorkspaceNode> load() {
//                return Arrays.asList(getUploadFolder().listFiles());
                
                List<WorkspaceNode> unlinkedNodes =
                        workspaceService.listUnlinkedNodes(LamusSession.get().getUserId(), UnlinkedNodesPanel.this.getModelObject().getWorkspaceID());
                
                
                //TODO change FileListView so that receives WorkspaceNodes and presents them as such
                
//                List<File> filesList = new ArrayList<File>();
//                
//                for(WorkspaceNode node : unlinkedNodes) {
//                    File file = (node.getWorkspaceURL() != null ?
//                            new File(node.getWorkspaceURL().getPath()) :
//                            new File(node.getArchiveURL().getPath()));
//                    filesList.add(file);
//                }
//                
//                return filesList;
                
                return unlinkedNodes;
            }
        });
        add(nodeListView);
    }
}
