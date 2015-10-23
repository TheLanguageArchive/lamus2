/*
 * Copyright (C) 2015 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.typechecking;

import java.io.File;
import java.util.Collection;
import nl.mpi.lamus.exception.MetadataValidationException;
import nl.mpi.lamus.typechecking.implementation.MetadataValidationIssue;

/**
 * Validator for metadata files in the workspace.
 * @author guisil
 */
public interface WorkspaceFileValidator {
    
    /**
     * Given a workspace, it gets the relevant metadata files and validates them.
     * @param workspaceID ID of the workspace to validate
     */
    public void validateMetadataFilesInWorkspace(int workspaceID) throws MetadataValidationException;
    
    /**
     * Validates the given metadata file.
     * @param workspaceID ID of the workspace associated with file
     * @param file File to validate
     */
    public void validateMetadataFile(int workspaceID, File file) throws MetadataValidationException;
    
    /**
     * Checks if the given collection of issues contains any error.
     * @param issues Collection of validation issues
     * @return true if the collection contains at least one validation error
     */
    public boolean validationIssuesContainErrors(Collection<MetadataValidationIssue> issues);
    
    /**
     * Converts the given collection of issues into a string for presentation purposes.
     * @param issues Collection of issues
     * @return String containing the main information about the issues in the collection
     */
    public String validationIssuesToString(Collection<MetadataValidationIssue> issues);
}
