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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import nl.mpi.archiving.corpusstructure.core.NodeNotFoundException;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.typechecking.TypecheckedResults;
import nl.mpi.lamus.exception.TypeCheckerException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.typechecking.MetadataChecker;
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

    private final NodeDataRetriever nodeDataRetriever;
    private final WorkspaceDirectoryHandler workspaceDirectoryHandler;
    private final WorkspaceFileHandler workspaceFileHandler;
    private final WorkspaceNodeFactory workspaceNodeFactory;
    private final WorkspaceDao workspaceDao;
    private final WorkspaceUploadHelper workspaceUploadHelper;
    private final MetadataApiBridge metadataApiBridge;
    private final MetadataChecker metadataChecker;
    
    @Autowired
    public LamusWorkspaceUploader(NodeDataRetriever ndRetriever,
        WorkspaceDirectoryHandler wsDirHandler, WorkspaceFileHandler wsFileHandler,
        WorkspaceNodeFactory wsNodeFactory,
        WorkspaceDao wsDao, WorkspaceUploadHelper wsUploadHelper,
        MetadataApiBridge mdApiBridge, MetadataChecker mdChecker) {
        
        this.nodeDataRetriever = ndRetriever;
        this.workspaceDirectoryHandler = wsDirHandler;
        this.workspaceFileHandler = wsFileHandler;
        this.workspaceNodeFactory = wsNodeFactory;
        this.workspaceDao = wsDao;
        this.workspaceUploadHelper = wsUploadHelper;
        this.metadataApiBridge = mdApiBridge;
        this.metadataChecker = mdChecker;
    }

    /**
     * @see WorkspaceUploader#getWorkspaceUploadDirectory(int)
     */
    @Override
    public File getWorkspaceUploadDirectory(int workspaceID) {
        return this.workspaceDirectoryHandler.getUploadDirectoryForWorkspace(workspaceID);
    }

    /**
     * @see WorkspaceUploader#uploadZipFileIntoWorkspace(int, java.util.zip.ZipInputStream)
     */
    @Override
    public Collection<File> uploadZipFileIntoWorkspace(int workspaceID, ZipInputStream zipInputStream)
            throws IOException {
        
        File workspaceUploadDirectory = this.workspaceDirectoryHandler.getUploadDirectoryForWorkspace(workspaceID);
        
        //TODO code to restrict names of created folders has been commented out.
            // If it turns out that folders in workspaces also need to be restricted, this should be uncommented and other changes made accordingly.
            // Not only the folders should be renamed if necessary, but also the paths of its children.
            // Also the links in metadata files become incorrect after this, so they should be changed as well, or just completely reject the files and instruct the user to change the folder names before uploading.
        
//        Map<String, String> changedDirectoryNames = new HashMap<>();
        
        Collection<File> copiedFiles = new ArrayList<>();
        
        ZipEntry nextEntry = zipInputStream.getNextEntry();
        while(nextEntry != null) {
            
            File entryFile = new File(workspaceUploadDirectory, nextEntry.getName());
            if(nextEntry.isDirectory()) {
             
                File createdDirectory = workspaceDirectoryHandler.createDirectoryInWorkspace(workspaceID, nextEntry.getName());
                
//                if(!createdDirectory.getName().equals(entryFile.getName())) {
//                    changedDirectoryNames.put(entryFile.getName(), createdDirectory.getName());
//                }
                
                nextEntry = zipInputStream.getNextEntry();
                continue;
            }
            
            File changedEntryFile = entryFile;
//            for(String changedName : changedDirectoryNames.keySet()) {
//                if(entryFile.getPath().contains(changedName)) {
//                    String newName = entryFile.getPath().replace(changedName, changedDirectoryNames.get(changedName));
//                    changedEntryFile = new File(newName);
//                    
//                }
//            }
            
            workspaceFileHandler.copyInputStreamToTargetFile(zipInputStream, changedEntryFile);
            
            nextEntry = zipInputStream.getNextEntry();
            copiedFiles.add(changedEntryFile);

        }
        
        return copiedFiles;
    }

    /**
     * @see WorkspaceUploader#processUploadedFiles(int, java.util.Collection)
     */
    @Override
    public Collection<UploadProblem> processUploadedFiles(int workspaceID, Collection<File> uploadedFiles)
            throws IOException, TypeCheckerException, WorkspaceException {
        
        //collection containing all the upload problems
        Collection<UploadProblem> allUploadProblems = new ArrayList<>();
        
        // collection containing the files that for some reason are not sucessfully processed, along with the reason for it
        Collection<UploadProblem> failedFiles = new ArrayList<>();
        
        // collection containing the nodes that were eventually uploaded, to be used later for checking eventual links between them
        Collection<WorkspaceNode> uploadedNodes = new ArrayList<>();
        
        WorkspaceNode topNode = this.workspaceDao.getWorkspaceTopNode(workspaceID);
        URL topNodeArchiveURL;
        try {
            topNodeArchiveURL = this.nodeDataRetriever.getNodeArchiveURL(topNode.getArchiveURI());
        } catch (NodeNotFoundException ex) {
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
                failedFiles.add(new FileUploadProblem(currentFile, errorMessage, ex));
                continue;
            }
            
            TypecheckedResults typecheckedResults = this.nodeDataRetriever.triggerResourceFileCheck(uploadedFileURL, currentFile.getName());
            
            StringBuilder message = new StringBuilder();
            boolean isArchivable = nodeDataRetriever.isCheckedResourceArchivable(typecheckedResults, topNodeArchiveURL, message);
            
            if(!isArchivable) {
                String errorMessage = "File [" + currentFile.getName() + "] not archivable: " + message;
                failUploadForFile(currentFile, errorMessage, failedFiles);
                continue;
            } else {
                String debugMessage = "File [" + currentFile.getName() + "] archivable: " + message;
                logger.debug(debugMessage);
            }
            
            if(uploadedFileURL.toString().endsWith("cmdi")) {

                if(!metadataApiBridge.isMetadataFileValid(uploadedFileURL)) {
                    String errorMessage = "Metadata file [" + currentFile.getName() + "] is invalid";
                    failUploadForFile(currentFile, errorMessage, failedFiles);
                    continue;
                }
                
                try {
                    if(!metadataChecker.isProfileAllowed(currentFile)) {
                        String errorMessage = "Profile of metadata file [" + currentFile.getName() + "] not allowed.";
                        failUploadForFile(currentFile, errorMessage, failedFiles);
                        continue;
                    } else {
                        String debugMessage = "Profile of metadata file [" + currentFile.getName() + "] allowed.";
                        logger.debug(debugMessage);
                    }
                } catch (Exception ex) {
                    throw new UnsupportedOperationException("not handled yet", ex);
                }
                
                
            }
            
            String nodeMimetype = typecheckedResults.getCheckedMimetype();
            
            URI archiveURI = null;
            if(uploadedFileURL.toString().endsWith("cmdi")) {
                archiveURI = metadataApiBridge.getSelfHandleFromFile(uploadedFileURL);   
            }
            
            WorkspaceNode uploadedNode = this.workspaceNodeFactory.getNewWorkspaceNodeFromFile(
                    workspaceID, archiveURI, null, uploadedFileURL, nodeMimetype, WorkspaceNodeStatus.NODE_UPLOADED, false);
        
            this.workspaceDao.addWorkspaceNode(uploadedNode);
            
            uploadedNodes.add(uploadedNode);
        }
        
        //Searching for links among the uploaded files
        Collection<UploadProblem> failedLinks = workspaceUploadHelper.assureLinksInWorkspace(workspaceID, uploadedNodes);
        
        allUploadProblems.addAll(failedFiles);
        allUploadProblems.addAll(failedLinks);
        
        return allUploadProblems;
    }

    
    private void failUploadForFile(File file, String errorMessage, Collection<UploadProblem> failedFiles) throws IOException {
        logger.error(errorMessage);
        failedFiles.add(new FileUploadProblem(file, errorMessage, null));
        
        //remove unarchivable file after adding it to the collection of failed uploads
        FileUtils.forceDelete(file);
    }
}
