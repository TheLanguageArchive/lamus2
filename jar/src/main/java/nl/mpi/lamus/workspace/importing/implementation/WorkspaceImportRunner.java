/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.workspace.importing.implementation;

import java.net.URI;
import java.util.Collection;
import java.util.concurrent.Callable;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.workspace.importing.OrphanNodesImportHandler;
import nl.mpi.lamus.workspace.model.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runner that will trigger a thread that performs
 * the import of the nodes into the workspace.
 * 
 * @author guisil
 */
public class WorkspaceImportRunner implements Callable<Boolean>{

    private static final Logger logger = LoggerFactory.getLogger(WorkspaceImportRunner.class);
    
    private final WorkspaceDao workspaceDao;
    private final TopNodeImporter topNodeImporter;
    private final OrphanNodesImportHandler orphanNodesImportHandler;
    
    private Workspace workspace = null;
    private URI topNodeArchiveURI = null;
    
    public WorkspaceImportRunner(WorkspaceDao workspaceDao,
            TopNodeImporter topNodeImporter, OrphanNodesImportHandler orphanNodesImportHandler) {
        this.workspaceDao = workspaceDao;
        this.topNodeImporter = topNodeImporter;
        this.orphanNodesImportHandler = orphanNodesImportHandler;
    }
    
    /**
     * Setter for the workspace to which the imported files should be connected
     * @param ws workspace to be used for the import
     */
    public void setWorkspace(Workspace ws) {
        this.workspace = ws;
    }
    
    /**
     * Setter for the archive URI of the top node of the workspace
     * @param nodeArchiveURI archive URI of the top node of the workspace
     */
    public void setTopNodeArchiveURI(URI nodeArchiveURI) {
        this.topNodeArchiveURI = nodeArchiveURI;
    }
    
    /**
     * The import process is started in a separate thread.
     * The nodes will be explored and copied, starting with the top node.
     * @return true if import is successful
     */
    @Override
    public Boolean call() throws WorkspaceImportException, WorkspaceException {
        
        if(workspace == null) {
            throw new IllegalStateException("Workspace not set");
        }
        if(topNodeArchiveURI == null) {
            throw new IllegalStateException("Top node URI not set");
        }
        
        try {
            topNodeImporter.importNode(workspace, topNodeArchiveURI);
            
            workspace.setStatusMessageInitialised();
            workspaceDao.updateWorkspaceStatusMessage(workspace);
            
        } catch (WorkspaceImportException fiex) {
            workspace.setStatusMessageErrorDuringInitialisation();
            workspaceDao.updateWorkspaceStatusMessage(workspace);
            
            throw fiex;
        }
        
        Workspace updatedWorkspace = workspaceDao.getWorkspace(workspace.getWorkspaceID());
        Collection<ImportProblem> orphanImportProblems = orphanNodesImportHandler.exploreOrphanNodes(updatedWorkspace);
        
        if(!orphanImportProblems.isEmpty()) {
        
        	logger.warn("Some problems importing orphan files.");
            
            for(ImportProblem problem : orphanImportProblems) {
                logger.warn("[Orphan Import Problem] " + problem.getErrorMessage());
            }
        }
        
        return true;
    }
}
