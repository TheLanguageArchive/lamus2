/*
 * Copyright (C) 2016 Max Planck Institute for Psycholinguistics
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

import eu.clarin.cmdi.validator.CMDIValidationHandlerAdapter;
import eu.clarin.cmdi.validator.CMDIValidationReport;
import eu.clarin.cmdi.validator.CMDIValidatorException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of the CMDIValidationHandlerAdapter class to be used in the
 * CNDI validation.
 * @author guisil
 */
public class LamusMetadataValidationHandler extends CMDIValidationHandlerAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusMetadataValidationHandler.class);
    
    private final int workspaceID;
    
    private Collection<String> validationWarnings = new ArrayList<String>();
    private Collection<String> validationErrors = new ArrayList<String>();

    
    public LamusMetadataValidationHandler(int workspaceID) {
        this.workspaceID = workspaceID;
    }
    
    /**
     * @see CMDIValidationHandlerAdapter#onValidationReport(eu.clarin.cmdi.validator.CMDIValidationReport)
     */
    @Override
    public void onValidationReport(final CMDIValidationReport report)
            throws CMDIValidatorException {
        final File file = report.getFile();
        int skip = 0;
        switch (report.getHighestSeverity()) {
        case INFO:
            logger.info("DBG: file["+file+"] is valid");
            break;
        case WARNING:
            for (CMDIValidationReport.Message msg : report.getMessages()) {
                if (msg.getMessage().contains("Failed to read schema document ''")) {
                    skip++;
                    continue;
                }
                logger.warn("WRN: file ["+file+"] is valid (with warnings):");
                String warningMsg = "";
                if ((msg.getLineNumber() != -1) &&
                        (msg.getColumnNumber() != -1)) {
                	warningMsg = " ("+msg.getSeverity().getShortcut()+") "+msg.getMessage()+" [line="+msg.getLineNumber()+", column="+msg.getColumnNumber()+"]";
                } else {
                	warningMsg = " ("+msg.getSeverity().getShortcut()+") "+msg.getMessage();
                }
            	validationWarnings.add(warningMsg);
            	logger.warn(warningMsg);
            }
            break;
        case ERROR:
            String errorMessage = "";
            for (CMDIValidationReport.Message msg : report.getMessages()) {
                if (msg.getMessage().contains("Failed to read schema document ''")) {
                    skip++;
                    continue;
                }
                logger.error("ERR: file ["+file+"] is invalid:");
                if ((msg.getLineNumber() != -1) &&
                        (msg.getColumnNumber() != -1)) {
                	errorMessage = "("+msg.getSeverity().getShortcut()+") "+msg.getMessage()+" [line="+msg.getLineNumber()+", column="+msg.getColumnNumber()+"]";
                } else {
                	errorMessage = "("+msg.getSeverity().getShortcut()+") "+msg.getMessage();
                }
                validationErrors.add(errorMessage);
                logger.error(errorMessage);
            }
            
            //An exception should be thrown in this case. Returning all the issues is not necessary at the moment.
            throw new CMDIValidatorException("Metadata file " + file + " is invalid. Reason: " + errorMessage);
            
        default:
            throw new CMDIValidatorException("unexpected severity: " +
                    report.getHighestSeverity());
        } // switch
        if (skip>0) {
            logger.warn("WRN: skipped ["+skip+"] warnings due to lax validation of foreign namespaces");
        }
    }

	public Collection<String> getValidationWarnings() {
		return validationWarnings;
	}
	
	public Collection<String> getValidationErrors() {
		return validationErrors;
	}
}
