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

import nl.mpi.lamus.workspace.model.Workspace;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

/**
 *
 * @author guisil
 */
public class WorkspaceInfoPanel extends Panel {
    
    public WorkspaceInfoPanel(String id, IModel<Workspace> model) {
        super(id, model);
        
        WebMarkupContainer container = new WebMarkupContainer("workspaceInfoContainer", new CompoundPropertyModel<Workspace>(model));
	container.add(new Label("userID"));
	container.add(new Label("workspaceID"));
	container.add(new Label("status"));
        
        add(container);
    }
}
