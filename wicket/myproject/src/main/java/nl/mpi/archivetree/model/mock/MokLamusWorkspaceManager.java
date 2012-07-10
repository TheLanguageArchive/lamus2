/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.archivetree.model.mock;

import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.workspace.exception.FailedToCreateWorkspaceDirectoryException;
import nl.mpi.lamus.workspace.factory.WorkspaceFactory;
import nl.mpi.lamus.workspace.importing.WorkspaceImportRunner;
import nl.mpi.lamus.workspace.management.WorkspaceManager;
import nl.mpi.lamus.workspace.management.implementation.LamusWorkspaceManager;
import nl.mpi.lamus.workspace.model.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;

/**
 *
 * @author jeafer
 */
class MokLamusWorkspaceManager implements WorkspaceManager {

    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceManager.class);
    private final TaskExecutor executor;
    private final WorkspaceFactory workspaceFactory;
    private final WorkspaceDao workspaceDao;
    private final WorkspaceDirectoryHandler workspaceDirectoryHandler;
    private final WorkspaceImportRunner workspaceImportRunner;

    public Workspace createWorkspace(String userID, int topNodeArchiveID) {
        Workspace newWorkspace = workspaceFactory.getNewWorkspace(userID, topNodeArchiveID);
        workspaceDao.addWorkspace(newWorkspace);
        try {
            workspaceDirectoryHandler.createWorkspaceDirectory(newWorkspace);
        } catch (FailedToCreateWorkspaceDirectoryException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
        workspaceImportRunner.setWorkspace(newWorkspace);
        workspaceImportRunner.setTopNodeArchiveID(topNodeArchiveID);
        executor.execute(workspaceImportRunner);

        return newWorkspace;
    }
}
