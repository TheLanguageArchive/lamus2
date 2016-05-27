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
package nl.mpi.lamus.metadata.validation.implementation;

import eu.clarin.cmdi.validator.CMDIValidator;
import eu.clarin.cmdi.validator.CMDIValidatorConfig;
import eu.clarin.cmdi.validator.CMDIValidatorException;
import eu.clarin.cmdi.validator.CMDIValidatorInitException;
import eu.clarin.cmdi.validator.CMDIValidatorProcessor;
import eu.clarin.cmdi.validator.SimpleCMDIValidatorProcessor;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.MetadataValidationException;
import nl.mpi.lamus.metadata.validation.MetadataSchematronChecker;
import nl.mpi.lamus.metadata.validation.WorkspaceFileValidator;
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
    private final MetadataSchematronChecker metadataChecker;
    
    @Autowired
    public LamusWorkspaceFileValidator(WorkspaceDao wsDao, MetadataSchematronChecker mdChecker) {
        workspaceDao = wsDao;
        metadataChecker = mdChecker;
    }
    
    /**
     * @see WorkspaceFileValidator#triggerSchematronValidationForMetadataFilesInWorkspace(int)
     */
    @Override
    public void triggerSchematronValidationForMetadataFilesInWorkspace(int workspaceID) throws MetadataValidationException {
        
        logger.debug("Performing schematron validation for metadata files in the tree of workspace " + workspaceID);
        
        Collection<MetadataValidationIssue> validationIssues = new ArrayList<>();
        
        Collection<WorkspaceNode> metadataNodesInTree = workspaceDao.getMetadataNodesInTreeForWorkspace(workspaceID);
        try {
            Collection<File> allFilesToValidate = new ArrayList<>();
            for(WorkspaceNode node : metadataNodesInTree) {
                allFilesToValidate.add(new File(node.getWorkspaceURL().getPath()));
                
            }
            
            Collection<MetadataValidationIssue> issues = metadataChecker.validateSubmittedFile(allFilesToValidate);
                validationIssues.addAll(issues);
            
        } catch(Exception ex) {
            throwMetadataValidationException(
                    workspaceID, ex, "Problems with schematron metadata validation",
                    validationIssues);
        }
        
        if(!validationIssues.isEmpty()) {
            throwMetadataValidationException(
                    workspaceID, null, "Problems with schematron metadata validation",
                    validationIssues);
        }
        
        logger.debug("Schematron metadata validation for workspace " + workspaceID + "  was performed without any issues");
    }

    /**
     * @see WorkspaceFileValidator#triggerSchematronValidationForFile(int, java.io.File)
     */
    @Override
    public void triggerSchematronValidationForFile(int workspaceID, File file) throws MetadataValidationException {
        
        logger.debug("Performing schematron validation for metadata file " + file.getName() + " in workspace " + workspaceID);
        
        Collection<MetadataValidationIssue> validationIssues = new ArrayList<>();
        
        try {
            validationIssues = metadataChecker.validateUploadedFile(file);
        } catch(Exception ex) {
            throwMetadataValidationException(
                    workspaceID, ex, "Problems with schematron metadata validation",
                    validationIssues);
        }
        
        if(!validationIssues.isEmpty()) {
            throwMetadataValidationException(
                    workspaceID, null, "Problems with schematron metadata validation",
                    validationIssues);
        }
        
        logger.debug("Schematron validation for metadata file " + file.getName() + " in workspace " + workspaceID + " was performed without any issues");
    }

    /**
     * @see WorkspaceFileValidator#triggerSchemaValidationForFile(int, java.io.File)
     */
    @Override
    public void triggerSchemaValidationForFile(int workspaceID, File file)
            throws CMDIValidatorInitException, MetadataValidationException {
        
        LamusMetadataValidationHandler handler = new LamusMetadataValidationHandler(workspaceID);
        
        CMDIValidatorConfig.Builder builder = new CMDIValidatorConfig.Builder(file, handler);
        builder.disableSchematron();
        
        CMDIValidator validator = new CMDIValidator(builder.build());
        CMDIValidatorProcessor processor = new SimpleCMDIValidatorProcessor();
        
        try {
            processor.process(validator);
        } catch(CMDIValidatorException ex) {
            throwMetadataValidationException(
                    workspaceID, ex, "Problems with schema validation",
                    new ArrayList<MetadataValidationIssue>());
        }
    }

    /**
     * @see WorkspaceFileValidator#triggerSchemaValidationForMetadataFilesInWorkspace(int)
     */
    @Override
    public void triggerSchemaValidationForMetadataFilesInWorkspace(int workspaceID) 
    		throws MetadataValidationException, CMDIValidatorInitException {     
        
        logger.debug("Performing schema validation for metadata files in the tree of workspace " + workspaceID);
        Collection<MetadataValidationIssue> validationIssues = new ArrayList<>();
        
        Collection<WorkspaceNode> metadataNodesInTree = workspaceDao.getMetadataNodesInTreeForWorkspace(workspaceID);
        for(WorkspaceNode node : metadataNodesInTree) {
    		File fileToValidate = new File(node.getWorkspaceURL().getPath());
        	try {
                triggerSchemaValidationForFile(workspaceID, fileToValidate);                
        	} catch (MetadataValidationException ex) {
        		validationIssues.add(new MetadataValidationIssue(fileToValidate, ex.getCause().getMessage(), "error"));
        	}
        }  
        
        if(!validationIssues.isEmpty()) {
            throwMetadataValidationException(
                    workspaceID, null, "Problems with schema metadata validation",
                    validationIssues);
        }
        
        logger.debug("Schematron metadata validation for workspace " + workspaceID + "  was performed without any issues");
    }

    /**
     * @see WorkspaceFileValidator#validationIssuesContainErrors(java.util.Collection)
     */
    @Override
    public boolean validationIssuesContainErrors(Collection<MetadataValidationIssue> issues) {
        
        for(MetadataValidationIssue issue : issues) {
            if(MetadataValidationIssueSeverity.ERROR.equals(issue.getSeverity())) {
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
    
    
    private void throwMetadataValidationException(int workspaceID, Exception cause, String message, Collection<MetadataValidationIssue> issues)
                throws MetadataValidationException {
        
        MetadataValidationException exceptionToThrow = new MetadataValidationException(message, workspaceID, cause);
        if(issues != null) {
            exceptionToThrow.addValidationIssues(issues);
        }
        throw exceptionToThrow;
    }
}
