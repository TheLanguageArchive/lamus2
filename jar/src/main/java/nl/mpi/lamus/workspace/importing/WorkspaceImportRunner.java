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
package nl.mpi.lamus.workspace.importing;

import java.util.concurrent.Callable;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.exception.NodeExplorerException;
import nl.mpi.lamus.workspace.exception.NodeImporterException;
import nl.mpi.lamus.workspace.importing.implementation.NodeImporterFactoryBean;
import nl.mpi.lamus.workspace.importing.implementation.TopNodeImporter;
import nl.mpi.lamus.workspace.model.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Runner that will trigger a thread that performs
 * the import of the nodes into the workspace.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class WorkspaceImportRunner implements Callable<Boolean>{

    private static final Logger logger = LoggerFactory.getLogger(WorkspaceImportRunner.class);
    
    private final WorkspaceDao workspaceDao;
    private final WorkspaceNodeExplorer workspaceFileExplorer;
    private final NodeImporterFactoryBean fileImporterFactoryBean;
    
    private final TopNodeImporter topNodeImporter;
    
    private Workspace workspace = null;
    private int topNodeArchiveID = -1;
    
    @Autowired
    public WorkspaceImportRunner(WorkspaceDao workspaceDao, WorkspaceNodeExplorer workspaceFileExplorer,
        NodeImporterFactoryBean fileImporterFactoryBean, TopNodeImporter topNodeImporter) {
        this.workspaceDao = workspaceDao;
        this.workspaceFileExplorer = workspaceFileExplorer;
        this.fileImporterFactoryBean = fileImporterFactoryBean;
        this.topNodeImporter = topNodeImporter;
    }
    
    /**
     * Setter for the workspace to which the imported files should be connected
     * @param ws workspace to be used for the import
     */
    public void setWorkspace(Workspace ws) {
        this.workspace = ws;
    }
    
    /**
     * Setter for the archive ID of the top node of the workspace
     * @param nodeArchiveID archive ID of the top node of the workspace
     */
    public void setTopNodeArchiveID(int nodeArchiveID) {
        this.topNodeArchiveID = nodeArchiveID;
    }
    
    /**
     * The import process is started in a separate thread.
     * The nodes will be explored and copied, starting with the top node.
     */
    @Override
    public Boolean call() throws NodeImporterException, NodeExplorerException {
        
        //TODO DO NOT RUN IF WORKSPACE OR TOP NODE ID ARE NOT DEFINED
        
//        topNodeImporter.setWorkspace(workspace);
        
        try {
            //TODO create some other method that takes something else than a Reference
            // or have a separate method for importing the top node
//            topNodeImporter.importNode(null, null, null, topNodeArchiveID);
            topNodeImporter.importNode(workspace.getWorkspaceID(), topNodeArchiveID);

            
            //TODO import successful? notify main thread, change workspace status, etc...
            // no exceptions, so it was successful ?
            
            workspace.setStatusMessageInitialised();
            workspaceDao.updateWorkspaceStatusMessage(workspace);
            
        } catch (NodeImporterException fiex) {
            String errorMessage = "Error during file import.";
                //TODO LOG PROPERLY
                //TODO THROW EXCEPTION OR RETURN?
            logger.error(errorMessage, fiex);
            
            workspace.setStatusMessageErrorDuringInitialisation();
            workspaceDao.updateWorkspaceStatusMessage(workspace);
            
            throw fiex;
            
            //TODO use Callable/Future instead and notify the calling thread when this one is finished?
        } catch (NodeExplorerException feex) {
            String errorMessage = "Error during file explore.";
                //TODO LOG PROPERLY
                //TODO THROW EXCEPTION OR RETURN?
            logger.error(errorMessage, feex);
            
            workspace.setStatusMessageErrorDuringInitialisation();
            workspaceDao.updateWorkspaceStatusMessage(workspace);
            
            throw feex;
            
            //TODO use Callable/Future instead and notify the calling thread when this one is finished?
        }
            
            
            //TODO When to return false?
            
            return true;
    }
    
}
