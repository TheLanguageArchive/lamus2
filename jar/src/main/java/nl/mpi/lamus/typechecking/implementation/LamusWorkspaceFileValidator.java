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
package nl.mpi.lamus.typechecking.implementation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.MetadataValidationException;
import nl.mpi.lamus.typechecking.MetadataChecker;
import nl.mpi.lamus.typechecking.WorkspaceFileValidator;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceFileValidator
 * @author guisil
 */
@Component
public class LamusWorkspaceFileValidator implements WorkspaceFileValidator{

    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceFileValidator.class);
    
    private final WorkspaceDao workspaceDao;
    private final MetadataChecker metadataChecker;
    
    @Autowired
    public LamusWorkspaceFileValidator(WorkspaceDao wsDao, MetadataChecker mdChecker) {
        workspaceDao = wsDao;
        metadataChecker = mdChecker;
    }
    
    /**
     * @see WorkspaceFileValidator#validateMetadataFilesInWorkspace(int)
     */
    @Override
    public void validateMetadataFilesInWorkspace(int workspaceID) throws MetadataValidationException {
        
        logger.debug("Performing schematron validation for metadata files in the tree of workspace " + workspaceID);
        
        Collection<MetadataValidationIssue> validationIssues = new ArrayList<>();
        
        Collection<WorkspaceNode> metadataNodesInTree = workspaceDao.getMetadataNodesInTreeForWorkspace(workspaceID);
        try {
            for(WorkspaceNode node : metadataNodesInTree) {
                File nodeFile = new File(node.getWorkspaceURL().getPath());
                Collection<MetadataValidationIssue> issues = metadataChecker.validateSubmittedFile(nodeFile);
                validationIssues.addAll(issues);
            }
        } catch(Exception ex) {
            throwMetadataValidationException(workspaceID, ex, validationIssues);
        }
        
        if(!validationIssues.isEmpty()) {
            throwMetadataValidationException(workspaceID, null, validationIssues);
        }
        
        logger.debug("Schematron metadata validation for workspace " + workspaceID + "  was performed without any issues");
    }

    /**
     * @see WorkspaceFileValidator#validateMetadataFile(int, java.io.File)
     */
    @Override
    public void validateMetadataFile(int workspaceID, File file) throws MetadataValidationException {
        
        logger.debug("Performing schematron validation for metadata file " + file.getName() + " in workspace " + workspaceID);
        
        Collection<MetadataValidationIssue> validationIssues = new ArrayList<>();
        
        try {
            validationIssues = metadataChecker.validateUploadedFile(file);
        } catch(Exception ex) {
            throwMetadataValidationException(workspaceID, ex, validationIssues);
        }
        
        if(!validationIssues.isEmpty()) {
            throwMetadataValidationException(workspaceID, null, validationIssues);
        }
        
        logger.debug("Schematron validation for metadata file " + file.getName() + " in workspace " + workspaceID + " was performed without any issues");
    }

    /**
     * @see WorkspaceFileValidator#validationIssuesContainErrors(java.util.Collection)
     */
    @Override
    public boolean validationIssuesContainErrors(Collection<MetadataValidationIssue> issues) {
        
        for(MetadataValidationIssue issue : issues) {
            if(MetadataValidationIssueLevel.ERROR.equals(issue.getAssertionErrorLevel())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see WorkspaceFileValidator#validationIssuesToString(java.util.Collection)
     */
    @Override
    public String validationIssuesToString(Collection<MetadataValidationIssue> issues) {
        
        StringBuilder issuesString = new StringBuilder();
        for(MetadataValidationIssue issue : issues) {
            issuesString.append(issue.toString()).append("\n");
        }
        return issuesString.toString();
    }
    
    
    private void throwMetadataValidationException(int workspaceID, Exception cause, Collection<MetadataValidationIssue> issues)
                throws MetadataValidationException {
        
        String errorMessage = "Problems with schematron metadata validation";
        MetadataValidationException exceptionToThrow = new MetadataValidationException(errorMessage, workspaceID, cause);
        if(issues != null) {
            exceptionToThrow.addValidationIssues(issues);
        }
        throw exceptionToThrow;
    }
}
