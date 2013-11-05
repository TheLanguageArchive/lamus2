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
package nl.mpi.lamus.workspace.upload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import nl.mpi.lamus.workspace.exception.TypeCheckerException;
import org.apache.commons.fileupload.FileItem;

/**
 *
 * @author guisil
 */
public interface WorkspaceUploader {

    /**
     * Returns the Upload directory for the given workspace
     * @param workspaceID ID of the workspace
     * @return Upload directory
     */
    public File getWorkspaceUploadDirectory(int workspaceID);
    
    /**
     * Uploads the given collection of files into the given workspace
     * @param workspaceID ID of the workspace
     * @param fileItems Collection of file items to be uploaded
     */
//    public void uploadFiles(int workspaceID, Collection<FileItem> fileItems);
    
    /**
     * Given an InputStream and the filename, this method triggers a
     * typecheck and uploads the file, if archivable.
     * @param workspaceID ID of the workspace
     * @param inputStream InputStream to upload
     * @param filename name of the file to upload
     * @throws IOException 
     * @throws TypeCheckerException 
     */
    public void uploadFileIntoWorkspace(int workspaceID, InputStream inputStream, String filename) throws IOException, TypeCheckerException;
    
}
