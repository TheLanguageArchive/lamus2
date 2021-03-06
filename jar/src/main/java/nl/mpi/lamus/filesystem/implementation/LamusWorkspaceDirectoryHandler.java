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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import nl.mpi.lamus.exception.DisallowedPathException;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.workspace.model.Workspace;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceDirectoryHandler
 * 
 * @author guisil
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
    
    @Autowired
    @Qualifier("orphansDirectoryName")
    private String orphansDirectoryName;
    
    @Resource
    @Qualifier("disallowedFolderNamesWorkspace")
    private Collection<String> disallowedFolderNamesWorkspace;

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

    /**
     * @see WorkspaceDirectoryHandler#getDirectoryForWorkspace(int)
     */
    @Override
    public File getDirectoryForWorkspace(int workspaceID) {
        return new File(this.workspaceBaseDirectory, "" + workspaceID);
    }

    /**
     * @see WorkspaceDirectoryHandler#getUploadDirectoryForWorkspace(int)
     */
    @Override
    public File getUploadDirectoryForWorkspace(int workspaceID) {
        File workspaceDirectory = this.getDirectoryForWorkspace(workspaceID);
        return new File(workspaceDirectory, this.workspaceUploadDirectoryName);
    }
    
    /**
     * @see WorkspaceDirectoryHandler#getOrphansDirectoryInWorkspace(int)
     */
    @Override
    public File getOrphansDirectoryInWorkspace(int workspaceID) {
        File workspaceDirectory = this.getDirectoryForWorkspace(workspaceID);
        return new File(workspaceDirectory, this.orphansDirectoryName);
    }
    
    /**
     * @see WorkspaceDirectoryHandler#createUploadDirectoryForWorkspace(int)
     */
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
    
    /**
     * @see WorkspaceDirectoryHandler#createOrphansDirectoryInWorkspace(int)
     */
    @Override
    public void createOrphansDirectoryInWorkspace(int workspaceID) throws IOException {
        File workspaceOrphansDirectory = getOrphansDirectoryInWorkspace(workspaceID);
        
        if(workspaceOrphansDirectory.exists()) {
            logger.info("Orphans directory in workspace " + workspaceID + " already exists");
        } else {
            if(workspaceOrphansDirectory.mkdirs()) {
                logger.info("Orphans directory in workspace " + workspaceID + " successfully created");
            } else {
                String errorMessage = "Orphans directory in workspace " + workspaceID + " could not be created";
                throw new IOException(errorMessage);
            }
        }
    }

    /**
     * @see WorkspaceDirectoryHandler#createDirectoryInWorkspace(int, java.lang.String)
     */
    @Override
    public File createDirectoryInWorkspace(int workspaceID, String directoryName) throws IOException {
        
        String normalizedDirectoryName = FilenameUtils.normalizeNoEndSeparator(directoryName);
        File workspaceUploadDirectory = getUploadDirectoryForWorkspace(workspaceID);
        File finalDirectory = new File(workspaceUploadDirectory, normalizedDirectoryName);
        finalDirectory.mkdir();
        
        return finalDirectory;
    }
    
    /**
     * @see WorkspaceDirectoryHandler#ensurePathIsAllowed(String)
     */
    @Override
    public void ensurePathIsAllowed(String path) throws DisallowedPathException {

    	Path pathToCheck = Paths.get("", path);
        
        Iterator<Path> pathIterator = pathToCheck.iterator();
        while(pathIterator.hasNext()) {
            Path currentPathItem = pathIterator.next();
            String nameToMatch = currentPathItem.getFileName().toString();
            for(String regex : disallowedFolderNamesWorkspace) {
                if(Pattern.matches(regex, nameToMatch)) {
                    String message = "The path [" + path + "] contains a disallowed file/folder name (" + nameToMatch + ")";
                    throw new DisallowedPathException(path, message);
                }
            }
        }
    }
}
