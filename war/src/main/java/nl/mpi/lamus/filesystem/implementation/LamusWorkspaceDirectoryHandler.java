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
import nl.mpi.lamus.configuration.Configuration;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.workspace.Workspace;
import nl.mpi.lamus.workspace.exception.FailedToCreateWorkspaceDirectoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceDirectoryHandler implements WorkspaceDirectoryHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceDirectoryHandler.class);

    private Configuration configuration;
    
    LamusWorkspaceDirectoryHandler(Configuration configuration) {
        this.configuration = configuration;
    }

    public File createWorkspaceDirectory(Workspace workspace) throws FailedToCreateWorkspaceDirectoryException {
        
        logger.debug("Creating directory for workspace " + workspace.getWorkspaceID());
        
        File baseDirectory = new File(this.configuration.getWorkspaceBaseDirectory());
        File workspaceDirectory = new File(baseDirectory, "" + workspace.getWorkspaceID());
        
        if(workspaceDirectory.exists()) {
            logger.info("Directory for workspace " + workspace.getWorkspaceID() + " already exists");
            return workspaceDirectory;
        } else {
            if(workspaceDirectory.mkdirs()) {
                logger.info("Directory for workspace " + workspace.getWorkspaceID() + " successfully created");
                return workspaceDirectory;
            } else {
                String errorMessage = "Directory for workspace " + workspace.getWorkspaceID() + " could not be created";
                throw new FailedToCreateWorkspaceDirectoryException(errorMessage, workspace);
            }
        }
    }
    
}
