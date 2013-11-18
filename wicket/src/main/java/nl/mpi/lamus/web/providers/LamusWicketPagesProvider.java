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
package nl.mpi.lamus.web.providers;

import nl.mpi.lamus.web.model.WorkspaceModel;
import nl.mpi.lamus.web.pages.CreateWorkspacePage;
import nl.mpi.lamus.web.pages.IndexPage;
import nl.mpi.lamus.web.pages.LinkNodesPage;
import nl.mpi.lamus.web.pages.SelectWorkspacePage;
import nl.mpi.lamus.web.pages.UnlinkedNodesPage;
import nl.mpi.lamus.web.pages.UploadPage;
import nl.mpi.lamus.web.pages.WorkspacePage;
import nl.mpi.lamus.workspace.model.Workspace;
import org.springframework.stereotype.Component;

/**
 *
 * @author guisil
 */
@Component
public class LamusWicketPagesProvider {
    
    
    public IndexPage getIndexPage() {
        return new IndexPage();
    }
    
    public CreateWorkspacePage getCreateWorkspacePage() {
        return new CreateWorkspacePage();
    }
    
    public SelectWorkspacePage getSelectWorkspacePage() {
        return new SelectWorkspacePage();
    }
    
    public WorkspacePage getWorkspacePage(Workspace workspace) {
        return new WorkspacePage(new WorkspaceModel(workspace));
    }
    
    public UploadPage getUploadPage(Workspace workspace) {
        return new UploadPage(new WorkspaceModel(workspace));
    }
    
    public UnlinkedNodesPage getUnlinkedNodesPage(Workspace workspace) {
        return new UnlinkedNodesPage(new WorkspaceModel(workspace));
    }
    
    public LinkNodesPage getLinkNodesPage(Workspace workspace) {
        return new LinkNodesPage(new WorkspaceModel(workspace));
    }
}
