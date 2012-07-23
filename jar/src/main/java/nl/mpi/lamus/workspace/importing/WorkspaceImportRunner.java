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

import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.exception.FileExplorerException;
import nl.mpi.lamus.workspace.exception.FileImporterException;
import nl.mpi.lamus.workspace.importing.implementation.FileImporterFactoryBean;
import nl.mpi.lamus.workspace.model.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class WorkspaceImportRunner implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(WorkspaceImportRunner.class);
    
    private final WorkspaceDao workspaceDao;
    private final WorkspaceFileExplorer workspaceFileExplorer;
    private final FileImporterFactoryBean fileImporterFactoryBean;
    
    private Workspace workspace = null;
    private int topNodeArchiveID = -1;
    
    @Autowired
    public WorkspaceImportRunner(WorkspaceDao workspaceDao, WorkspaceFileExplorer workspaceFileExplorer,
        FileImporterFactoryBean fileImporterFactoryBean) {
        this.workspaceDao = workspaceDao;
        this.workspaceFileExplorer = workspaceFileExplorer;
        this.fileImporterFactoryBean = fileImporterFactoryBean;
    }
    
    public void setWorkspace(Workspace ws) {
        this.workspace = ws;
    }
    
    public void setTopNodeArchiveID(int nodeArchiveID) {
        this.topNodeArchiveID = nodeArchiveID;
    }
    
    public void run() {
        
        //TODO DO NOT RUN IF WORKSPACE OR TOP NODE ID ARE NOT DEFINED
        
        fileImporterFactoryBean.setFileImporterTypeForReference(null);
        FileImporter topNodeImporter;
        try {
            topNodeImporter = fileImporterFactoryBean.getObject();
        } catch (Exception ex) {
            String errorMessage = "Error during initialisation of file importer.";
            logger.error(errorMessage, ex);
            
            workspace.setStatusMessageErrorDuringInitialisation();
            workspaceDao.updateWorkspaceStatusMessage(workspace);
            
            //TODO use Callable/Future instead and notify the calling thread when this one is finished?
            return;
        }
        
        topNodeImporter.setWorkspace(workspace);
        
        try {
            //TODO create some other method that takes something else than a Reference
            //TODO or have a separate method for importing the top node
            topNodeImporter.importFile(null, null, null, topNodeArchiveID);

            
            //TODO import successful? notify main thread, change workspace status, etc...
            // no exceptions, so it was successful ?
            
            workspace.setStatusMessageInitialised();
            workspaceDao.updateWorkspaceStatusMessage(workspace);
            
        } catch (FileImporterException fiex) {
            String errorMessage = "Error during file import.";
                //TODO LOG PROPERLY
                //TODO THROW EXCEPTION OR RETURN?
            logger.error(errorMessage, fiex);
            
            workspace.setStatusMessageErrorDuringInitialisation();
            workspaceDao.updateWorkspaceStatusMessage(workspace);
            
            //TODO use Callable/Future instead and notify the calling thread when this one is finished?
        } catch (FileExplorerException feex) {
            String errorMessage = "Error during file explore.";
                //TODO LOG PROPERLY
                //TODO THROW EXCEPTION OR RETURN?
            logger.error(errorMessage, feex);
            
            workspace.setStatusMessageErrorDuringInitialisation();
            workspaceDao.updateWorkspaceStatusMessage(workspace);
            
            //TODO use Callable/Future instead and notify the calling thread when this one is finished?
        }
    }
    
}
