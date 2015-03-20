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
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @see WorkspaceFileValidator
 * @author guisil
 */
public class LamusWorkspaceFileValidator implements WorkspaceFileValidator{

    private final WorkspaceDao workspaceDao;
    private final MetadataChecker metadataChecker;
    
    @Autowired
    public LamusWorkspaceFileValidator(WorkspaceDao wsDao, MetadataChecker mdChecker) {
        workspaceDao = wsDao;
        metadataChecker = mdChecker;
    }
    
    /**
     * @see WorkspaceFileValidator#validateWorkspaceFiles(nl.mpi.lamus.workspace.model.Workspace)
     */
    @Override
    public void validateWorkspaceFiles(Workspace workspace) throws MetadataValidationException {
        
        Collection<MetadataValidationIssue> validationIssues = new ArrayList<>();
        
        Collection<WorkspaceNode> metadataNodesInTree = workspaceDao.getMetadataNodesInTreeForWorkspace(workspace.getWorkspaceID());
        try {
            for(WorkspaceNode node : metadataNodesInTree) {
                File nodeFile = new File(node.getWorkspaceURL().toString());
                Collection<MetadataValidationIssue> issues = metadataChecker.validateSubmittedFile(nodeFile);
                validationIssues.addAll(issues);
            }
        } catch(Exception ex) {
            String errorMessage = "Problems with metadata validation";
            throwMetadataValidationException(errorMessage, workspace.getWorkspaceID(), ex, null);
        }
        
        if(!validationIssues.isEmpty()) {
            String errorMessage = "Problems with metadata validation";
            throwMetadataValidationException(errorMessage, workspace.getWorkspaceID(), null, validationIssues);
        }
    }
    
    
    private void throwMetadataValidationException(String errorMessage,
            int workspaceID, Exception cause, Collection<MetadataValidationIssue> issues)
                throws MetadataValidationException {
        
        MetadataValidationException exceptionToThrow = new MetadataValidationException(errorMessage, workspaceID, cause);
        if(issues != null) {
            exceptionToThrow.addValidationIssues(issues);
        }
        throw exceptionToThrow;
    }
}
