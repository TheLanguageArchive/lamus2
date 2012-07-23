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
package nl.mpi.lamus.filesystem.implementation;

import java.io.File;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.workspace.exception.FailedToCreateWorkspaceDirectoryException;
import nl.mpi.lamus.workspace.model.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusWorkspaceDirectoryHandler implements WorkspaceDirectoryHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceDirectoryHandler.class);

    @Autowired
    @Qualifier("workspaceBaseDirectory")
    private File workspaceBaseDirectory;

    public void createWorkspaceDirectory(Workspace workspace) throws FailedToCreateWorkspaceDirectoryException {
        
        logger.debug("Creating directory for workspace " + workspace.getWorkspaceID());
        
        File workspaceDirectory = new File(workspaceBaseDirectory, "" + workspace.getWorkspaceID());
        
        if(workspaceDirectory.exists()) {
            logger.info("Directory for workspace " + workspace.getWorkspaceID() + " already exists");
        } else {
            if(workspaceDirectory.mkdirs()) {
                logger.info("Directory for workspace " + workspace.getWorkspaceID() + " successfully created");
            } else {
                String errorMessage = "Directory for workspace " + workspace.getWorkspaceID() + " could not be created";
                throw new FailedToCreateWorkspaceDirectoryException(errorMessage, workspace, null);
            }
        }
    }
}
