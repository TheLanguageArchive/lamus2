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

import nl.mpi.lamus.workspace.model.Workspace;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public final class WorkspacePage extends LamusPage {

    private final IModel<Workspace> model;

    public WorkspacePage(Workspace workspace) {
	super();

	model = new CompoundPropertyModel<Workspace>(workspace);
	add(createWorkspaceInfo("workspaceInfo"));
    }

    private WebMarkupContainer createWorkspaceInfo(String id) {
	WebMarkupContainer wsInfo = new WebMarkupContainer(id, model);
	wsInfo.add(new Label("userID"));
	wsInfo.add(new Label("workspaceID"));
	wsInfo.add(new Label("status"));
	return wsInfo;
    }
}
