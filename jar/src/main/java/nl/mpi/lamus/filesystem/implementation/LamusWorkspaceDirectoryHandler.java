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
import java.io.IOException;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.workspace.model.Workspace;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceDirectoryHandler
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusWorkspaceDirectoryHandler implements WorkspaceDirectoryHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceDirectoryHandler.class);

    @Autowired
    @Qualifier("workspaceBaseDirectory")
    private File workspaceBaseDirectory;
    
    @Autowired
    @Qualifier("workspaceUploadDirectoryName")
    private String workspaceUploadDirectoryName;

    /**
     * @see WorkspaceDirectoryHandler#createWorkspaceDirectory(nl.mpi.lamus.workspace.model.Workspace)
     */
    @Override
    public void createWorkspaceDirectory(int workspaceID) throws IOException {
        
        logger.debug("Creating directory for workspace " + workspaceID);
        
        File workspaceDirectory = this.getDirectoryForWorkspace(workspaceID);
        
        if(workspaceDirectory.exists()) {
            logger.info("Directory for workspace " + workspaceID + " already exists");
        } else {
            if(workspaceDirectory.mkdirs()) {
                logger.info("Directory for workspace " + workspaceID + " successfully created");
            } else {
                String errorMessage = "Directory for workspace " + workspaceID + " could not be created";
                throw new IOException(errorMessage);
            }
        }
    }
    
    /**
     * @see WorkspaceDirectoryHandler#deleteWorkspaceDirectory(int)
     */
    @Override
    public void deleteWorkspaceDirectory(int workspaceID) throws IOException {
        
        File workspaceDirectory = this.getDirectoryForWorkspace(workspaceID);
        
        if(!workspaceDirectory.exists()) {
            logger.info("Directory for workspace " + workspaceID + " doesn't exist");
        } else {
            FileUtils.deleteDirectory(workspaceDirectory);
        }
    }
    
    /**
     * @see WorkspaceDirectoryHandler#workspaceDirectoryExists(nl.mpi.lamus.workspace.model.Workspace)
     */
    @Override
    public boolean workspaceDirectoryExists(Workspace workspace) {
        
        logger.debug("Checking if directory for workspace " + workspace.getWorkspaceID() + " exists");
        
        File workspaceDirectory = this.getDirectoryForWorkspace(workspace.getWorkspaceID());
        return workspaceDirectory.exists();
    }

    @Override
    public File getDirectoryForWorkspace(int workspaceID) {
        return new File(this.workspaceBaseDirectory, "" + workspaceID);
    }

    @Override
    public File getUploadDirectoryForWorkspace(int workspaceID) {
        File workspaceDirectory = this.getDirectoryForWorkspace(workspaceID);
        return new File(workspaceDirectory, this.workspaceUploadDirectoryName);
    }
    
    @Override
    public void createUploadDirectoryForWorkspace(int workspaceID) throws IOException {
        File workspaceUploadDirectory = getUploadDirectoryForWorkspace(workspaceID);
        
        if(workspaceUploadDirectory.exists()) {
            logger.info("Upload directory for workspace " + workspaceID + " already exists");
        } else {
            if(workspaceUploadDirectory.mkdirs()) {
                logger.info("Upload directory for workspace " + workspaceID + " successfully created");
            } else {
                String errorMessage = "Upload directory for workspace " + workspaceID + " could not be created";
                throw new IOException(errorMessage);
            }
        }
    }
}
