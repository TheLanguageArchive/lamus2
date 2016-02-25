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
package nl.mpi.lamus.web.pages.providers;

import nl.mpi.lamus.web.model.WorkspaceModelProvider;
import nl.mpi.lamus.web.pages.CreateWorkspacePage;
import nl.mpi.lamus.web.pages.IndexPage;
import nl.mpi.lamus.web.pages.SelectWorkspacePage;
import nl.mpi.lamus.web.pages.WorkspacePage;
import nl.mpi.lamus.web.pages.management.ManageWorkspacesPage;
import nl.mpi.lamus.workspace.model.Workspace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Page provider class.
 * @author guisil
 */
@Component
public class LamusWicketPagesProvider {
    
    @Autowired
    private WorkspaceModelProvider workspaceModelProvider;
    
    
    /**
     * Retrieves an Index Page.
     * @return IndexPage instance
     */
    public IndexPage getIndexPage() {
        return new IndexPage();
    }
    
    /**
     * Retrieves a Create Workspace Page.
     * @return CreateWorkspace instance
     */
    public CreateWorkspacePage getCreateWorkspacePage() {
        return new CreateWorkspacePage();
    }
    
    /**
     * Retrieves a Select Workspace Page.
     * @return SelectWorkspacePage instance
     */
    public SelectWorkspacePage getSelectWorkspacePage() {
        return new SelectWorkspacePage();
    }
    
    /**
     * Retrieves a Workspace Page.
     * @param workspace Workspace to use in the model
     * @return WorkspacePage instance
     */
    public WorkspacePage getWorkspacePage(Workspace workspace) {
        return new WorkspacePage(workspaceModelProvider.getWorkspaceModel(workspace.getWorkspaceID()));
    }
    
    /**
     * Retrieves a Manage Workspace Page.
     * @return ManageWorkspacePage instance
     */
    public ManageWorkspacesPage getManageWorkspacesPage() {
        return new ManageWorkspacesPage();
    }
}
