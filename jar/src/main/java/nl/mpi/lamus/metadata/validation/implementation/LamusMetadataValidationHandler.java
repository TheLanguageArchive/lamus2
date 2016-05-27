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
            logger.warn("WRN: file ["+file+"] is valid (with warnings):");
            for (CMDIValidationReport.Message msg : report.getMessages()) {
                if (msg.getMessage().contains("Failed to read schema document ''")) {
                    skip++;
                    continue;
                }
                if ((msg.getLineNumber() != -1) &&
                        (msg.getColumnNumber() != -1)) {
                    logger.warn(" ("+msg.getSeverity().getShortcut()+") "+msg.getMessage()+" [line="+msg.getLineNumber()+", column="+msg.getColumnNumber()+"]");
                } else {
                    logger.warn(" ("+msg.getSeverity().getShortcut()+") "+msg.getMessage());
                }
            }
            break;
        case ERROR:
            logger.error("ERR: file ["+file+"] is invalid:");
            String errorMessage = "";
            for (CMDIValidationReport.Message msg : report.getMessages()) {
                if (msg.getMessage().contains("Failed to read schema document ''")) {
                    skip++;
                    continue;
                }
                if ((msg.getLineNumber() != -1) &&
                        (msg.getColumnNumber() != -1)) {
                	errorMessage = " ("+msg.getSeverity().getShortcut()+") "+msg.getMessage()+" [line="+msg.getLineNumber()+", column="+msg.getColumnNumber()+"]";
                } else {
                	errorMessage = " ("+msg.getSeverity().getShortcut()+") "+msg.getMessage();
                }
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
}
