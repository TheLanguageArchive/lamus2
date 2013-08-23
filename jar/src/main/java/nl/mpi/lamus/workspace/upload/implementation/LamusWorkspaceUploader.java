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
import java.util.Collection;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.upload.WorkspaceUploader;
import org.apache.commons.fileupload.FileItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 *
 * @author guisil
 */
@Component
public class LamusWorkspaceUploader implements WorkspaceUploader {
    
    @Autowired
    @Qualifier("workspaceUploadDirectory")
    private File workspaceUploadDirectory;

    private NodeDataRetriever nodeDataRetriever;
    private WorkspaceFileHandler workspaceFileHandler;
    private WorkspaceNodeFactory workspaceNodeFactory;
    private WorkspaceDao workspaceDao;
    
    @Autowired
    public LamusWorkspaceUploader(NodeDataRetriever ndRetriever, WorkspaceFileHandler wsFileHandler,
        WorkspaceNodeFactory wsNodeFactory, WorkspaceDao wsDao) {
        
        this.nodeDataRetriever = ndRetriever;
        this.workspaceFileHandler = wsFileHandler;
        this.workspaceNodeFactory = wsNodeFactory;
        this.workspaceDao = wsDao;
    }
    
    @Override
    public void uploadFiles(int workspaceID, Collection<FileItem> fileItems) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
