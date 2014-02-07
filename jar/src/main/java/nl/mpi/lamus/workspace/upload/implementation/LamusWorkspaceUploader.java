/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.workspace.upload.implementation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import nl.mpi.archiving.corpusstructure.core.UnknownNodeException;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.typechecking.TypecheckedResults;
import nl.mpi.lamus.exception.TypeCheckerException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.workspace.upload.WorkspaceUploadHelper;
import nl.mpi.lamus.workspace.upload.WorkspaceUploader;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceUploader
 * @author guisil
 */
@Component
public class LamusWorkspaceUploader implements WorkspaceUploader {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceUploader.class);

    private NodeDataRetriever nodeDataRetriever;
    private WorkspaceDirectoryHandler workspaceDirectoryHandler;
    private WorkspaceNodeFactory workspaceNodeFactory;
    private WorkspaceDao workspaceDao;
    private WorkspaceUploadHelper workspaceUploadHelper;
    private MetadataApiBridge metadataApiBridge;
    
    @Autowired
    public LamusWorkspaceUploader(NodeDataRetriever ndRetriever,
        WorkspaceDirectoryHandler wsDirHandler, WorkspaceNodeFactory wsNodeFactory,
        WorkspaceDao wsDao, WorkspaceUploadHelper wsUploadHelper,
        MetadataApiBridge mdApiBridge) {
        
        this.nodeDataRetriever = ndRetriever;
        this.workspaceDirectoryHandler = wsDirHandler;
        this.workspaceNodeFactory = wsNodeFactory;
        this.workspaceDao = wsDao;
        this.workspaceUploadHelper = wsUploadHelper;
        this.metadataApiBridge = mdApiBridge;
    }

    /**
     * @see WorkspaceUploader#getWorkspaceUploadDirectory(int)
     */
    @Override
    public File getWorkspaceUploadDirectory(int workspaceID) {
        return this.workspaceDirectoryHandler.getUploadDirectoryForWorkspace(workspaceID);
    }
    
    @Override
    public void uploadFileIntoWorkspace(int workspaceID, InputStream inputStreamToCheck, String filename)
            throws IOException, TypeCheckerException, WorkspaceException {
        
        File workspaceUploadDirectory = this.workspaceDirectoryHandler.getUploadDirectoryForWorkspace(workspaceID);
        
        File uploadedFile = FileUtils.getFile(workspaceUploadDirectory, filename);
        URL uploadedFileURL;
        try {
            uploadedFileURL = uploadedFile.toURI().toURL();
        } catch (MalformedURLException ex) {
            String errorMessage = "Error retrieving URL from file " + uploadedFile.getPath();
            logger.error(errorMessage, ex);
            throw new WorkspaceException(errorMessage, workspaceID, ex);
        }
        
        TypecheckedResults typecheckedResults = this.nodeDataRetriever.triggerResourceFileCheck(inputStreamToCheck, filename);
        
        WorkspaceNode topNode = this.workspaceDao.getWorkspaceTopNode(workspaceID);
        URL topNodeArchiveURL;
        try {
            topNodeArchiveURL = this.nodeDataRetriever.getNodeArchiveURL(topNode.getArchiveURI());
        } catch (UnknownNodeException ex) {
            String errorMessage = "Error retrieving archive URL from the top node of workspace " + workspaceID;
            logger.error(errorMessage, ex);
            throw new WorkspaceException(errorMessage, workspaceID, ex);
        }
            
        //TODO get this in some other way
            // the server in the URL should be replaced by the actual folder
                // or there should be a different way of specifying the folders with special typechecker configurations
//        File workspaceTopNodeFile = FileUtils.toFile(topNodeArchiveURL);
//        TypecheckerJudgement acceptableJudgement = this.typecheckerConfiguration.getAcceptableJudgementForLocation(workspaceTopNodeFile);

        StringBuilder message = new StringBuilder();
        boolean isArchivable = nodeDataRetriever.isCheckedResourceArchivable(topNodeArchiveURL, message);
        
        if(!isArchivable) {
            logger.error("File [" + filename + "] not archivable: " + message);
            throw new TypeCheckerException(message.toString(), null);
        }

        FileUtils.copyInputStreamToFile(inputStreamToCheck, uploadedFile);
            
        String nodeMimetype = typecheckedResults.getCheckedMimetype();
        
        URI archiveURI = null;
        if(uploadedFileURL.toString().endsWith("cmdi")) {
            archiveURI = metadataApiBridge.getSelfHandleFromFile(uploadedFileURL);
        }
        
        WorkspaceNode uploadedNode = this.workspaceNodeFactory.getNewWorkspaceNodeFromFile(
                workspaceID, archiveURI, null, uploadedFileURL, nodeMimetype, WorkspaceNodeStatus.NODE_UPLOADED);
        
        this.workspaceDao.addWorkspaceNode(uploadedNode);
    }

    /**
     * @see WorkspaceUploader#processUploadedFiles(int, java.util.Collection)
     */
    @Override
    public Map<File, String> processUploadedFiles(int workspaceID, Collection<File> uploadedFiles)
            throws IOException, WorkspaceException {
        
        // map containing the files that for some reason are not sucessfully processed, along with the reason for it
        Map<File, String> failedFiles = new HashMap<File, String>();
        
        // collection containing the nodes that were eventually uploaded, to be used later for checking eventual links between them
        Collection<WorkspaceNode> uploadedNodes = new ArrayList<WorkspaceNode>();
        
        WorkspaceNode topNode = this.workspaceDao.getWorkspaceTopNode(workspaceID);
        URL topNodeArchiveURL;
        try {
            topNodeArchiveURL = this.nodeDataRetriever.getNodeArchiveURL(topNode.getArchiveURI());
        } catch (UnknownNodeException ex) {
            String errorMessage = "Error retrieving archive URL from the top node of workspace " + workspaceID;
            logger.error(errorMessage, ex);
            throw new WorkspaceException(errorMessage, workspaceID, ex);
        }
        
        
        for(File currentFile : uploadedFiles) {
            
            URL uploadedFileURL;
            try {
                uploadedFileURL = currentFile.toURI().toURL();
            } catch (MalformedURLException ex) {
                String errorMessage = "Error retrieving URL from file " + currentFile.getPath();
                logger.error(errorMessage, ex);
                failedFiles.put(currentFile, errorMessage);
                continue;
            }
        
            InputStream currentInputStream = FileUtils.openInputStream(currentFile);
        
            TypecheckedResults typecheckedResults = this.nodeDataRetriever.triggerResourceFileCheck(currentInputStream, currentFile.getName());
            currentInputStream.close();
            
            
            
            //TODO get this in some other way
                // the server in the URL should be replaced by the actual folder
                    // or there should be a different way of specifying the folders with special typechecker configurations
//        File workspaceTopNodeFile = FileUtils.toFile(topNodeArchiveURL);
//        TypecheckerJudgement acceptableJudgement = this.typecheckerConfiguration.getAcceptableJudgementForLocation(workspaceTopNodeFile);

        
            
            StringBuilder message = new StringBuilder();
            boolean isArchivable = nodeDataRetriever.isCheckedResourceArchivable(topNodeArchiveURL, message);
            
            if(!isArchivable) {
                String errorMessage = "File [" + currentFile.getName() + "] not archivable: " + message;
                logger.error(errorMessage);
                failedFiles.put(currentFile, errorMessage);
                
                //remove unarchivable file after adding it to the collection of failed uploads
                FileUtils.forceDelete(currentFile);
                
                continue;
            }
            
            String nodeMimetype = typecheckedResults.getCheckedMimetype();
            
            URI archiveURI = null;
            if(uploadedFileURL.toString().endsWith("cmdi")) {
                archiveURI = metadataApiBridge.getSelfHandleFromFile(uploadedFileURL);
            }
            
            WorkspaceNode uploadedNode = this.workspaceNodeFactory.getNewWorkspaceNodeFromFile(
                    workspaceID, archiveURI, null, uploadedFileURL, nodeMimetype, WorkspaceNodeStatus.NODE_UPLOADED);
        
            this.workspaceDao.addWorkspaceNode(uploadedNode);
            
            uploadedNodes.add(uploadedNode);
        }
        
        //Searching for links among the uploaded files
        workspaceUploadHelper.assureLinksInWorkspace(workspaceID, uploadedNodes);
        
        
        //TODO DO SOMETHING WITH NODES/REFERENCES THAT CANNOT BE LINKED...
        //TODO DO SOMETHING WITH NODES/REFERENCES THAT CANNOT BE LINKED...
        //TODO DO SOMETHING WITH NODES/REFERENCES THAT CANNOT BE LINKED...
        
        
        
        
        return failedFiles;
    }

}
