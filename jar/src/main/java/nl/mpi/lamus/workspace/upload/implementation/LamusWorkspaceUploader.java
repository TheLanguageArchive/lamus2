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
import java.net.URL;
import java.util.Collection;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.typechecking.FileTypeHandler;
import nl.mpi.lamus.typechecking.TypecheckedResults;
import nl.mpi.lamus.typechecking.TypecheckerConfiguration;
import nl.mpi.lamus.typechecking.TypecheckerJudgement;
import nl.mpi.lamus.workspace.exception.TypeCheckerException;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.upload.WorkspaceUploader;
import nl.mpi.util.OurURL;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author guisil
 */
@Component
public class LamusWorkspaceUploader implements WorkspaceUploader {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceUploader.class);

    private NodeDataRetriever nodeDataRetriever;
    private WorkspaceDirectoryHandler workspaceDirectoryHandler;
    private WorkspaceNodeFactory workspaceNodeFactory;
    private WorkspaceDao workspaceDao;
    private TypecheckerConfiguration typecheckerConfiguration;
    private FileTypeHandler fileTypeHandler;
    
    @Autowired
    public LamusWorkspaceUploader(NodeDataRetriever ndRetriever,
        WorkspaceDirectoryHandler wsDirHandler, WorkspaceNodeFactory wsNodeFactory,
        WorkspaceDao wsDao, TypecheckerConfiguration typecheckerConfig,
        FileTypeHandler fileTypeHandler) {
        
        this.nodeDataRetriever = ndRetriever;
        this.workspaceDirectoryHandler = wsDirHandler;
        this.workspaceNodeFactory = wsNodeFactory;
        this.workspaceDao = wsDao;
        this.typecheckerConfiguration = typecheckerConfig;
        this.fileTypeHandler = fileTypeHandler;
    }

    /**
     * @see WorkspaceUploader#getWorkspaceUploadDirectory(int)
     */
    @Override
    public File getWorkspaceUploadDirectory(int workspaceID) {
        return this.workspaceDirectoryHandler.getUploadDirectoryForWorkspace(workspaceID);
    }
    
    /**
     * @see WorkspaceUploader#uploadFiles(int, java.util.Collection)
     */
//    @Override
//    public void uploadFiles(int workspaceID, Collection<FileItem> fileItems) {
//        
//        WorkspaceNode topNode = this.workspaceDao.getWorkspaceTopNode(workspaceID);
//        File workspaceUploadDirectory = this.workspaceDirectoryHandler.getUploadDirectoryForWorkspace(workspaceID);
//        
//        //TODO too much repetition... do this some other way?
//        try {
//            this.workspaceDirectoryHandler.createUploadDirectoryForWorkspace(workspaceID);
//        } catch (IOException ex) {
//            throw new UnsupportedOperationException("exception not handled yet", ex);
//        }
//        
//        for(FileItem item : fileItems) {
//            
//            String itemName = item.getName();
//            
//            File uploadedFile = FileUtils.getFile(workspaceUploadDirectory, itemName);
//            URL uploadedFileURL;
//            OurURL uploadedFileOurURL;
//            try {
//                uploadedFileURL = uploadedFile.toURI().toURL();
//                uploadedFileOurURL = new OurURL(uploadedFileURL);
//            } catch (MalformedURLException ex) {
//                throw new UnsupportedOperationException("exception not handled yet", ex);
//            }
//            
//            InputStream itemInputStream;
//            try {
//                itemInputStream = item.getInputStream();
//            } catch (IOException ex) {
//                throw new UnsupportedOperationException("exception not handled yet", ex);
//            }
//            
//            TypecheckedResults typecheckedResults;
//            try {
//                typecheckedResults = this.nodeDataRetriever.triggerResourceFileCheck(itemInputStream, itemName);
//            } catch (TypeCheckerException ex) {
//                throw new UnsupportedOperationException("exception not handled yet", ex);
//            }
//            
////            this.nodeDataRetriever.verifyTypecheckedResults(uploadedFileOurURL, null, typecheckedResults);
//            
//            URL topNodeArchiveURL = this.nodeDataRetriever.getNodeArchiveURL(topNode.getArchiveURI());
//            
//            File workspaceTopNodeFile = FileUtils.toFile(topNodeArchiveURL);
//            TypecheckerJudgement acceptableJudgement = this.typecheckerConfiguration.getAcceptableJudgementForLocation(workspaceTopNodeFile);
//            StringBuilder message = new StringBuilder();
//            boolean archivable = this.fileTypeHandler.isCheckedResourceArchivable(uploadedFileOurURL, acceptableJudgement, message);
//            
//            if(!archivable) {
//                logger.error("File [" + item.getName() + "] not archivable: " + message);
//                //TODO show this error also in some other way?
//                continue;
//            }
//            try {
//                item.write(uploadedFile);
//            } catch (Exception ex) {
//                throw new UnsupportedOperationException("exception not handled yet", ex);
//            }
//            
//            WorkspaceNodeType nodeType = typecheckedResults.getCheckedNodeType();
//            String nodeMimetype = typecheckedResults.getCheckedMimetype();
//            
//            WorkspaceNode uploadedNode = this.workspaceNodeFactory.getNewWorkspaceNodeFromFile(
//                    workspaceID, null, uploadedFileURL, nodeType, nodeMimetype, WorkspaceNodeStatus.NODE_UPLOADED);
//            
//            this.workspaceDao.addWorkspaceNode(uploadedNode);
//        }
//    }

    @Override
    public void uploadFileIntoWorkspace(int workspaceID, InputStream inputStreamToCheck, String filename) throws IOException, TypeCheckerException {
        
        File workspaceUploadDirectory = this.workspaceDirectoryHandler.getUploadDirectoryForWorkspace(workspaceID);
        
        this.workspaceDirectoryHandler.createUploadDirectoryForWorkspace(workspaceID);
        
        File uploadedFile = FileUtils.getFile(workspaceUploadDirectory, filename);
        URL uploadedFileURL;
        OurURL uploadedFileOurURL;
        try {
            uploadedFileURL = uploadedFile.toURI().toURL();
//            uploadedFileOurURL = new OurURL(uploadedFileURL);
        } catch (MalformedURLException ex) {
            throw new UnsupportedOperationException("exception not handled yet", ex);
        }

            
        
        TypecheckedResults typecheckedResults = this.nodeDataRetriever.triggerResourceFileCheck(inputStreamToCheck, filename);
        
        WorkspaceNode topNode = this.workspaceDao.getWorkspaceTopNode(workspaceID);
        URL topNodeArchiveURL = this.nodeDataRetriever.getNodeArchiveURL(topNode.getArchiveURI());
            
        File workspaceTopNodeFile = FileUtils.toFile(topNodeArchiveURL);
        TypecheckerJudgement acceptableJudgement = this.typecheckerConfiguration.getAcceptableJudgementForLocation(workspaceTopNodeFile);
        
        StringBuilder message = new StringBuilder();
        boolean isArchivable = this.fileTypeHandler.isCheckedResourceArchivable(acceptableJudgement, message);
        
        if(!isArchivable) {
            logger.error("File [" + filename + "] not archivable: " + message);

            throw new TypeCheckerException(message.toString(), null);
        }

        FileUtils.copyInputStreamToFile(inputStreamToCheck, uploadedFile);
            
        WorkspaceNodeType nodeType = typecheckedResults.getCheckedNodeType();
        String nodeMimetype = typecheckedResults.getCheckedMimetype();
            
        WorkspaceNode uploadedNode = this.workspaceNodeFactory.getNewWorkspaceNodeFromFile(
                workspaceID, null, uploadedFileURL, nodeType, nodeMimetype, WorkspaceNodeStatus.NODE_UPLOADED);

        
        this.workspaceDao.addWorkspaceNode(uploadedNode);
        
        
        //TODO return something
    }

}
