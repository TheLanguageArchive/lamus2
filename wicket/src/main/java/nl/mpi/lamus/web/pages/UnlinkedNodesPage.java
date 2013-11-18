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

import nl.mpi.lamus.web.components.UnlinkedNodesPanel;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.workspace.model.Workspace;
import org.apache.wicket.model.IModel;

/**
 * Display nodes that are not linked in the workspace
 *
 * @author Jean-Charles Ferrières <jean-charles.ferrieres@mpi.nl>
 */
public class UnlinkedNodesPage extends WorkspacePage {
 
    /**
     * Constructor.
     *
     * @param parameters Page parameters
     */
    public UnlinkedNodesPage(final IModel<Workspace> model) {
        
        super(model);
        
        final String currentUserID = LamusSession.get().getUserId();

        add(new UnlinkedNodesPanel("unlinkedNodesPanel", model));

    }
}
