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

import java.util.logging.Level;
    import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.exception.FileImporterException;
import nl.mpi.lamus.workspace.exception.FileImporterInitialisationException;
import nl.mpi.lamus.workspace.model.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class WorkspaceImportRunner implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(WorkspaceImportRunner.class);
    
    private final WorkspaceDao workspaceDao;
    private final Workspace workspace;
    private final int topNodeArchiveID;
    private final FileImporterFactory fileImporterFactory;
    
    //TODO use Spring injection
    public WorkspaceImportRunner(WorkspaceDao workspaceDao, Workspace workspace, int topNodeArchiveID, FileImporterFactory importerFactory) {
        this.workspaceDao = workspaceDao;
        this.workspace = workspace;
        this.topNodeArchiveID = topNodeArchiveID;
        this.fileImporterFactory = importerFactory;
    }
    
    public void run() {
        
        FileImporter topNodeImporter;
        
        try {
            
            Class<? extends FileImporter> topNodeFileImporterType = fileImporterFactory.getFileImporterTypeForTopNode();
            
            topNodeImporter = fileImporterFactory.getNewFileImporterOfType(topNodeFileImporterType);
            
        } catch (FileImporterInitialisationException ex) {
            logger.error("Error during initialisation of file importer.", ex);
            
            workspace.setStatusMessageErrorDuringInitialisation();
            workspaceDao.updateWorkspaceStatusMessage(workspace);
            
            //TODO use Callable/Future instead and notify the calling thread when this one is finished?
            return;
        }        
        try {
            //TODO create some other method that takes something else than a Reference
            //TODO or have a separate method for importing the top node
            topNodeImporter.importFile(null, null, null, topNodeArchiveID);


            
            //TODO import successful? notify main thread, change workspace status, etc...
        } catch (FileImporterException ex) {
            String errorMessage = "Error during initialisation of file importer.";
                //TODO LOG PROPERLY
                //TODO THROW EXCEPTION OR RETURN?
            logger.error(errorMessage, ex);
            
            workspace.setStatusMessageErrorDuringInitialisation();
            workspaceDao.updateWorkspaceStatusMessage(workspace);
            
            //TODO use Callable/Future instead and notify the calling thread when this one is finished?
            return;
        }
    }
    
}
