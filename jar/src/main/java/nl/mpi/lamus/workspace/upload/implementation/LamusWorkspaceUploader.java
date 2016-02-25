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

import eu.clarin.cmdi.validator.CMDIValidatorException;
import eu.clarin.cmdi.validator.CMDIValidatorInitException;
import nl.mpi.lamus.workspace.importing.implementation.FileImportProblem;
import nl.mpi.lamus.workspace.importing.implementation.ImportProblem;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.transform.TransformerException;
import nl.mpi.archiving.corpusstructure.core.NodeNotFoundException;
import nl.mpi.handle.util.HandleParser;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.DisallowedPathException;
import nl.mpi.lamus.exception.MetadataValidationException;
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
import nl.mpi.lamus.metadata.validation.WorkspaceFileValidator;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.upload.WorkspaceUploadHelper;
import nl.mpi.lamus.workspace.upload.WorkspaceUploader;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
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
    private final MetadataAPI metadataAPI;
    private final MetadataApiBridge metadataApiBridge;
    private final WorkspaceFileValidator workspaceFileValidator;
    private final ArchiveFileLocationProvider archiveFileLocationProvider;
    private final ArchiveFileHelper archiveFileHelper;
    private final NodeUtil nodeUtil;
    private final HandleParser handleParser;
    
    @Autowired
    public LamusWorkspaceUploader(NodeDataRetriever ndRetriever,
        WorkspaceDirectoryHandler wsDirHandler, WorkspaceFileHandler wsFileHandler,
        WorkspaceNodeFactory wsNodeFactory,
        WorkspaceDao wsDao, WorkspaceUploadHelper wsUploadHelper, MetadataAPI mdAPI,
        MetadataApiBridge mdApiBridge, WorkspaceFileValidator wsFileValidator,
        ArchiveFileLocationProvider afLocationProvider, ArchiveFileHelper archiveFileHelper,
        NodeUtil nodeUtil, HandleParser hdlParser) {
        
        this.nodeDataRetriever = ndRetriever;
        this.workspaceDirectoryHandler = wsDirHandler;
        this.workspaceFileHandler = wsFileHandler;
        this.workspaceNodeFactory = wsNodeFactory;
        this.workspaceDao = wsDao;
        this.workspaceUploadHelper = wsUploadHelper;
        this.metadataAPI = mdAPI;
        this.metadataApiBridge = mdApiBridge;
        this.workspaceFileValidator = wsFileValidator;
        this.archiveFileLocationProvider = afLocationProvider;
        this.archiveFileHelper = archiveFileHelper;
        this.nodeUtil = nodeUtil;
        this.handleParser = hdlParser;
    }

    /**
     * @see WorkspaceUploader#getWorkspaceUploadDirectory(int)
     */
    @Override
    public File getWorkspaceUploadDirectory(int workspaceID) {
        return this.workspaceDirectoryHandler.getUploadDirectoryForWorkspace(workspaceID);
    }

    /**
     * @see WorkspaceUploader#uploadFileIntoWorkspace(int, java.io.InputStream, java.lang.String)
     */
    @Override
    public File uploadFileIntoWorkspace(int workspaceID, InputStream inputStream, String filename)
            throws IOException, DisallowedPathException {
        
        
        assureFilenameIsValid(filename);
        
        try {
            workspaceDirectoryHandler.ensurePathIsAllowed(filename);
        } catch(DisallowedPathException ex) {
            logger.warn(ex.getMessage());
            throw ex;
        }
        
        File workspaceUploadDirectory = this.workspaceDirectoryHandler.getUploadDirectoryForWorkspace(workspaceID);
        File fileToCopy = new File(workspaceUploadDirectory, filename);
        
        workspaceFileHandler.copyInputStreamToTargetFile(inputStream, fileToCopy);
        
        return fileToCopy;
    }
    
    /**
     * @see WorkspaceUploader#uploadZipFileIntoWorkspace(int, java.util.zip.ZipInputStream)
     */
    @Override
    public ZipUploadResult uploadZipFileIntoWorkspace(int workspaceID, ZipInputStream zipInputStream)
            throws IOException, DisallowedPathException {
        
        File workspaceUploadDirectory = this.workspaceDirectoryHandler.getUploadDirectoryForWorkspace(workspaceID);
        
        ZipUploadResult uploadResults = new ZipUploadResult();
        
        Collection<File> copiedFiles = new ArrayList<>();
        Collection<File> createdDirectories = new ArrayList<>();
        
        ZipEntry nextEntry = zipInputStream.getNextEntry();
        while(nextEntry != null) {
            
            String entryName = nextEntry.getName();

            assureFilenameIsValid(entryName);
            
            try {
                workspaceDirectoryHandler.ensurePathIsAllowed(entryName);
            } catch(DisallowedPathException ex) {
                logger.warn(ex.getMessage());
                deleteCreatedFilesAndDirectories(copiedFiles, createdDirectories);
                throw ex;
            }
            
            File entryFile = new File(workspaceUploadDirectory, entryName);
            if(nextEntry.isDirectory()) {
             
                File createdDirectory = workspaceDirectoryHandler.createDirectoryInWorkspace(workspaceID, entryName);
                createdDirectories.add(createdDirectory);
                nextEntry = zipInputStream.getNextEntry();
                continue;
            }
            
            File entryBaseDirectory = entryFile.getParentFile();
            String entryFilename = entryFile.getName();
            
            File fileAttempt = new File(entryBaseDirectory, entryFilename);
            if(fileAttempt.exists()) {
                uploadResults.addFailedUpload(new FileImportProblem(fileAttempt, "A file with the same path already exists", null));
                nextEntry = zipInputStream.getNextEntry();
                continue;
            }
            
            workspaceFileHandler.copyInputStreamToTargetFile(zipInputStream, fileAttempt);
            
            nextEntry = zipInputStream.getNextEntry();
            copiedFiles.add(fileAttempt);
            uploadResults.addSuccessfulUpload(fileAttempt);
        }
        
        return uploadResults;
    }

    /**
     * @see WorkspaceUploader#processUploadedFiles(int, java.util.Collection)
     */
    @Override
    public Collection<ImportProblem> processUploadedFiles(int workspaceID, Collection<File> uploadedFiles)
            throws WorkspaceException {
        
        //collection containing all the upload problems
        Collection<ImportProblem> allUploadProblems = new ArrayList<>();
        
        // collection containing the files that for some reason are not sucessfully processed, along with the reason for it
        Collection<ImportProblem> failedFiles = new ArrayList<>();
        
        // collection containing the nodes that were eventually uploaded, to be used later for checking eventual links between them
        Collection<WorkspaceNode> uploadedNodes = new ArrayList<>();
        
        WorkspaceNode topNode = this.workspaceDao.getWorkspaceTopNode(workspaceID);
        File topNodeArchiveLocalFile;
        try {
            topNodeArchiveLocalFile = this.nodeDataRetriever.getNodeLocalFile(topNode.getArchiveURI());
        } catch (NodeNotFoundException ex) {
            String errorMessage = "Error retrieving archive URL from the top node of workspace " + workspaceID;
            logger.error(errorMessage, ex);
            throw new WorkspaceException(errorMessage, workspaceID, ex);
        }
        
        for(File currentFile : uploadedFiles) {
            
            URI uploadedFileUri;
            URL uploadedFileUrl;
            try {
                uploadedFileUri = currentFile.toURI();
                uploadedFileUrl = uploadedFileUri.toURL();
            } catch (MalformedURLException ex) {
                String errorMessage = "Error retrieving URL from file " + currentFile.getPath();
                logger.error(errorMessage, ex);
                failedFiles.add(new FileImportProblem(currentFile, errorMessage, ex));
                continue;
            }
            
            TypecheckedResults typecheckedResults = null;
            try {
                typecheckedResults = this.nodeDataRetriever.triggerResourceFileCheck(uploadedFileUrl, currentFile.getName());
            } catch(TypeCheckerException ex) {
                String errorMessage = "Error while typechecking file [" + currentFile.getName() + "]";
                failUploadForFile(currentFile, errorMessage, null, failedFiles);
                continue;
            }
            
            StringBuilder message = new StringBuilder();
            boolean isArchivable = nodeDataRetriever.isCheckedResourceArchivable(typecheckedResults, topNodeArchiveLocalFile, message);
            
            if(!isArchivable) {
                String errorMessage = "File [" + currentFile.getName() + "] not archivable: " + message;
                failUploadForFile(currentFile, errorMessage, null, failedFiles);
                continue;
            } else {
                String debugMessage = "File [" + currentFile.getName() + "] archivable: " + message;
                logger.debug(debugMessage);
            }
            
            //to be used if the file is metadata
            MetadataDocument mdDocument = null;
            
            if(uploadedFileUrl.toString().endsWith("cmdi")) {
                
                try {
                    mdDocument = metadataAPI.getMetadataDocument(uploadedFileUrl);
                } catch (IOException | MetadataException ex) {
                    String errorMessage = "Error retrieving metadata document for file [" + currentFile.getName() + "]";
                    failUploadForFile(currentFile, errorMessage, null, failedFiles);
                    continue;
                }
                
                try {
                    workspaceFileValidator.triggerSchemaValidationForFile(workspaceID, currentFile);
                } catch(MetadataValidationException ex) {
                    String errorMessage = "Metadata file [" + currentFile.getName() + "] is invalid";
                    failUploadForFile(currentFile, errorMessage, null, failedFiles);
                    continue;
                } catch(CMDIValidatorInitException | CMDIValidatorException ex) {
                    String errorMessage = "Problems with the metadata validation when processing [" + currentFile.getName() + "]";
                    failUploadForFile(currentFile, errorMessage, ex, failedFiles);
                    continue;
                }
                
                logger.debug("Metadata API validation successful for file " + currentFile.getName());
                
                try{
                    workspaceFileValidator.triggerSchematronValidationForFile(workspaceID, currentFile);
                } catch(MetadataValidationException ex) {
                    String issuesMessage = workspaceFileValidator.validationIssuesToString(ex.getValidationIssues());
                    if(workspaceFileValidator.validationIssuesContainErrors(ex.getValidationIssues())) {
                        failUploadForFile(currentFile, issuesMessage, null, failedFiles);
                        continue;
                    } else {
                        logger.warn(issuesMessage);
                    }
                }
            }
            
            String nodeMimetype = typecheckedResults.getCheckedMimetype();
            WorkspaceNodeType nodeType = nodeUtil.convertMimetype(nodeMimetype);
            
            URI archiveUri = null;
            if(uploadedFileUrl.toString().endsWith("cmdi")) {
                archiveUri = metadataApiBridge.getSelfHandleFromDocument(mdDocument);
                if(archiveUri != null && !archiveUri.toString().trim().isEmpty()) {
                    try {
                    archiveUri = handleParser.prepareAndValidateHandleWithHdlPrefix(archiveUri);
                    } catch(IllegalArgumentException ex) {
                        try {
                            // invalid handle - should be removed
                            archiveUri = null;
                            metadataApiBridge.removeSelfHandleAndSaveDocument(uploadedFileUrl);
                        } catch (IOException | TransformerException | MetadataException ex1) {
                            logger.error("Couldn't remove invalid self-handle from file [" + uploadedFileUrl + "]", ex1);
                        }
                    }
                }
            }
            URI originUri = null;
            if(archiveFileLocationProvider.isFileInOrphansDirectory(currentFile)) {
                originUri = uploadedFileUri;
            }
            
            URI profileSchemaURI = mdDocument != null ? mdDocument.getDocumentType().getSchemaLocation() : null;
            
            String documentName = null;
            if(mdDocument != null) {
                documentName = metadataApiBridge.getDocumentNameForProfile(mdDocument, profileSchemaURI);
                if(documentName == null) {
                    documentName = mdDocument.getDisplayValue();
                }
            }
            
            WorkspaceNode uploadedNode = this.workspaceNodeFactory.getNewWorkspaceNodeFromFile(
                    workspaceID, archiveUri, originUri, uploadedFileUrl, profileSchemaURI, documentName,
                    nodeMimetype, nodeType, WorkspaceNodeStatus.UPLOADED, false);
        
            this.workspaceDao.addWorkspaceNode(uploadedNode);
            
            uploadedNodes.add(uploadedNode);
        }
        
        //Searching for links among the uploaded files
        Collection<ImportProblem> failedLinks = workspaceUploadHelper.assureLinksInWorkspace(workspaceID, uploadedNodes);
        
        allUploadProblems.addAll(failedFiles);
        allUploadProblems.addAll(failedLinks);
        
        return allUploadProblems;
    }

    
    private void failUploadForFile(File file, String errorMessage, Exception cause, Collection<ImportProblem> failedFiles) {
        logger.error(errorMessage);
        failedFiles.add(new FileImportProblem(file, errorMessage, cause));
        try {
            //remove unarchivable file after adding it to the collection of failed uploads
                // but only if it was an uploaded file
                // if it's being imported from the orphans folder, it shouldn't be deleted
            if(!archiveFileLocationProvider.isFileInOrphansDirectory(file)) {
                workspaceFileHandler.deleteFile(file);
            }
        } catch (IOException ex) {
            logger.warn("Couldn't delete unarchivable file: " + ex.getMessage());
        }
    }
    
    private void deleteCreatedFilesAndDirectories(Collection<File> filesToDelete, Collection<File> directoriesToDelete) {
        
        for(File file : filesToDelete) {
            deleteFileOrDirectory(file);
        }
        for(File dir : directoriesToDelete) {
            deleteFileOrDirectory(dir);
        }
    }
    
    private void deleteFileOrDirectory(File fileOrDir) {
        
        logger.debug("Deleting previously created file or directory (" + fileOrDir + ")");
        
        try {
            workspaceFileHandler.deleteFile(fileOrDir);
        } catch(DirectoryNotEmptyException ex) {
            logger.warn("Couldn't delete created directory (" + fileOrDir + ") because it's not empty");
        } catch(IOException ex) {
            logger.warn("Couldn't delete uploaded file (" + fileOrDir + ")");
        }
    }
    
    private void assureFilenameIsValid(String filename) throws DisallowedPathException {
        
        Path filePath = Paths.get(filename);
        String filenameToUse = filePath.getName(filePath.getNameCount() - 1).toString();
        
        String correctedFilename = archiveFileHelper.correctPathElement(filenameToUse, "Uploading file");
        if(!filenameToUse.equals(correctedFilename)) {
            String message = "Filename '" + filenameToUse + "' is not valid. It should not contain special characters.";
            logger.warn(message);
            throw new DisallowedPathException(filenameToUse, message);
        }
    }
}
