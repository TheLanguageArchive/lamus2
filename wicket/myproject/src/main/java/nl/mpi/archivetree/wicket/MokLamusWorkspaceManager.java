/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.archivetree.wicket;

import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.workspace.exception.FailedToCreateWorkspaceDirectoryException;
import nl.mpi.lamus.workspace.factory.WorkspaceFactory;
import nl.mpi.lamus.workspace.importing.WorkspaceImportRunner;
import nl.mpi.lamus.workspace.management.WorkspaceManager;
import nl.mpi.lamus.workspace.model.Workspace;
import org.jmock.auto.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;

/**
 *
 * @author jeafer
 */
class MokLamusWorkspaceManager implements WorkspaceManager {

        
    private static final Logger logger = LoggerFactory.getLogger(MokLamusWorkspaceManager.class);
    
    @Mock private final TaskExecutor mockexecutor;
    @Mock private final WorkspaceFactory mockworkspaceFactory;
    @Mock private final WorkspaceDao mockworkspaceDao;
    @Mock private final WorkspaceDirectoryHandler mockworkspaceDirectoryHandler;
//    private final FileImporterFactory importerFactory;
    private final WorkspaceImportRunner mockworkspaceImportRunner;
    
    public MokLamusWorkspaceManager(TaskExecutor executor, WorkspaceFactory factory, WorkspaceDao dao,
        WorkspaceDirectoryHandler directoryHandler, WorkspaceImportRunner wsImportRunner) {
                this.mockexecutor = executor;
        this.mockworkspaceFactory = factory;
        this.mockworkspaceDao = dao;
        this.mockworkspaceDirectoryHandler = directoryHandler;
        this.mockworkspaceImportRunner = wsImportRunner;
    }

    public Workspace createWorkspace(String userID, int topNodeArchiveID) {
                
        Workspace newWorkspace = mockworkspaceFactory.getNewWorkspace(userID, topNodeArchiveID);
        mockworkspaceDao.addWorkspace(newWorkspace);
        try {
            mockworkspaceDirectoryHandler.createWorkspaceDirectory(newWorkspace);
        } catch(FailedToCreateWorkspaceDirectoryException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
        
        //TODO get more values to add to the node (e.g. from the file, using the metadataAPI)
//        OurURL tempUrl = archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(archiveNodeID), ArchiveAccessContext.getFileUrlContext());
//        URL archiveNodeURL = tempUrl.toURL();
//        WorkspaceNode topNode = workspaceNodeFactory.getNewWorkspaceNode(newWorkspace.getWorkspaceID(), archiveNodeID, archiveNodeURL);

        
        //TODO change this call - use Spring injection

//        FileImporterFactory importerFactory = new WorkspaceFileImporterFactory(newWorkspace);
//        Runnable workspaceImportRunner = new WorkspaceImportRunner(workspaceDao, newWorkspace, topNodeArchiveID, importerFactory);
        mockworkspaceImportRunner.setWorkspace(newWorkspace);
        mockworkspaceImportRunner.setTopNodeArchiveID(topNodeArchiveID);
//        Thread importThread = new Thread(workspaceImportRunner);
//        importThread.start();
        
        
        //TODO use Callable and Future instead of Runnable, in order to get the result of the thread
        
        
        mockexecutor.execute(mockworkspaceImportRunner);
        
        return newWorkspace;
    }    
}
