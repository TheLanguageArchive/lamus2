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
import java.util.zip.ZipInputStream;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.importing.implementation.ImportProblem;

/**
 * Provides functionality to upload files into the workspace,
 * including the processing of those files after the upload.
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
     * Given an InputStream and a filename, this method uploads the corresponding file.
     * @param workspaceID ID of the workspace
     * @param inputStream InputStream to upload
     * @param filename name of the file
     * @return uploaded File
     */
    public File uploadFileIntoWorkspace(int workspaceID, InputStream inputStream, String filename)
            throws IOException;
    
    /**
     * Given a ZipInputStream and the filename, this method uploads its content.
     * @param workspaceID ID of the workspace
     * @param zipInputStream ZipInputStream to upload
     * @return Collection containing copied files
     */
    public Collection<File> uploadZipFileIntoWorkspace(int workspaceID, ZipInputStream zipInputStream)
            throws IOException;
    
    /**
     * After the files are uploaded, process the files by performing
     * typechecks and checking for links between them and the existing tree.
     * @param workspaceID ID of the workspace
     * @param uploadedFiles Collection of previously uploaded files
     * @return collection containing objects which describe eventual upload problems
     */
    public Collection<ImportProblem> processUploadedFiles(int workspaceID, Collection<File> uploadedFiles)
            throws WorkspaceException;
}
