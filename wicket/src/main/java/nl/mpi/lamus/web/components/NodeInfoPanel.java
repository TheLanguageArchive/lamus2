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

import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * Panel containing information regarding the currently selected node.
 * @author guisil
 */
public class NodeInfoPanel extends Panel {
    
    private final Form<WorkspaceTreeNode> form;
    
    
    public NodeInfoPanel(String id) {
        super(id);
        
        form = new Form<>("nodeInfoForm");
        form.add(new Label("name"));
	form.add(new Label("archiveURI"));
	form.add(new Label("archiveURL"));
	form.add(new Label("type"));
        
        // Put details/submit form in container for refresh through AJAX 
	final MarkupContainer formContainer = new WebMarkupContainer("nodeInfoContainer");
	formContainer.add(form);
	// Add container to page
	add(formContainer);
    }
    
    /**
     * Sets the model for the node information.
     * @param model 
     */
    public void setNodeInfoModel(IModel<WorkspaceTreeNode> model) {
        form.setModel(model);
    }
}
