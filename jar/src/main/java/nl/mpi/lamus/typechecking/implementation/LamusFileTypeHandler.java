/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
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

import java.io.IOException;
import java.io.InputStream;
import nl.mpi.bcarchive.typecheck.FileType;
import nl.mpi.lamus.typechecking.FileTypeHandler;
import nl.mpi.lamus.workspace.exception.TypeCheckerException;
import nl.mpi.lamus.workspace.model.TypeMapper;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.util.OurURL;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusFileTypeHandler implements FileTypeHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusFileTypeHandler.class);
    
    private FileType configuredTypeChecker;
    private TypeMapper typeMapper;
    
    private String mimetype = "Unknown";
    private WorkspaceNodeType nodeType = WorkspaceNodeType.UNKNOWN;
    private String analysis = "okay";

    public LamusFileTypeHandler(FileType configuredTypeChecker, TypeMapper typeMapper) {
        this.configuredTypeChecker = configuredTypeChecker;
        this.typeMapper = typeMapper;
    }

    public FileType getConfiguredTypeChecker() {
        return this.configuredTypeChecker;
    }

    public String getMimetype() {
        return this.mimetype;
    }

    public String getAnalysis() {
        if (analysis.startsWith("true ")) {
            return analysis.substring(5);
        }
        if (analysis.startsWith("false ")) {
            return analysis.substring(6);
        }
        return analysis;
    }

    public WorkspaceNodeType getNodeType() {
        return this.nodeType;
    }

    public void checkType(OurURL resourceURL, String filename,/* WorkspaceNodeType nodetype,*/ String mimetype) throws TypeCheckerException {
        
        if(mimetype == null) {
            this.mimetype = "Unknown";
        } else {
            this.mimetype = mimetype;
        }
        
        boolean tryName = false; // whether to use a name based method as fallback
        
        if ((mimetype == null) || mimetype.equals("Unknown") || mimetype.equals("Unspecified")) {
            
            InputStream iStream = null;
            
            if (resourceURL == null) {
                logger.debug("LamusFileTypeHandler.checkType: File contents unavailable, using name based method for " + filename);
                this.mimetype = "Unknown"; // use filename, not url, and skip checkStream step
                tryName = true;
            } else {
                
                
                // do NOT derive file name from URL, as the URL could be an extension-free WorkSpace URL...
                try {
                    iStream = resourceURL.openStream();
                } catch (IOException ioex) {
                    String errorMessage = "LamusFileTypeHandler.checkType: error opening stream to check file";
                    logger.error(errorMessage, ioex);
                    throw new TypeCheckerException(errorMessage, ioex);
                }
                
                //TODO ????
//                if ((filename.endsWith("/") || filename.indexOf(".")==-1) &&
//                    !"file".equals(resourceURL.getProtocol())) {
//                    filename += "/index.html"; // assumption for http: and similar ending with "/"
//                }
                //TODO ????
            }
            
            String checkResult = getCheckResult(iStream, filename);
            setValuesForResult(checkResult, tryName, resourceURL, filename);
        }
        
        setValues(this.mimetype);
    }

    public void setValues(String mimetype) {
        this.mimetype = mimetype;
        this.nodeType = typeMapper.getNodeTypeForMimetype(mimetype);
    }
    
    
    private String getCheckResult(InputStream iStream, String filename) throws TypeCheckerException {
        
        String checkResult = null;
        try {                
            checkResult = this.configuredTypeChecker.checkStream(iStream, filename.toLowerCase());
        } catch(IOException ioex) {
            String errorMessage = "LamusFileTypeHandler.checkType: error checking result from stream";
            logger.error(errorMessage, ioex);
            throw new TypeCheckerException(errorMessage, ioex);
        } finally {
            IOUtils.closeQuietly(iStream);
        }
        
        return checkResult;
    }
    
    private void setValuesForResult(String checkResult, boolean isCheckingName, OurURL resourceURL, String filename) {
        
        if(isCheckingName) {
            this.analysis = "okay (name)";
        } else {
            this.analysis = "okay (content, name)";
        }
        this.mimetype = FileType.resultToMimeType(checkResult);
        if (this.mimetype == null) { // no archivable file format, or wrong name
            if(isCheckingName) {
                logger.warn("LamusFileTypeHandler.checkType: No archivable file type for file NAME: " + resourceURL + " result: " + checkResult);
            } else {
                logger.warn("LamusFileTypeHandler.checkType: No archivable file type for FILE: " + resourceURL+" (" + filename+") result: " + checkResult);
            }
            this.mimetype = "Unknown"; // but do NOT use filename as fallback
            this.analysis = checkResult;
        }
    }
}
