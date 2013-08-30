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
import nl.mpi.lamus.typechecking.TypecheckHandler;
import nl.mpi.lamus.typechecking.TypecheckedResults;
import nl.mpi.lamus.typechecking.TypecheckerJudgement;
import nl.mpi.lamus.workspace.exception.TypeCheckerException;
import nl.mpi.lamus.workspace.model.NodeTypeMapper;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.util.OurURL;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see FileTypeHandler
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusFileTypeHandler implements FileTypeHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusFileTypeHandler.class);
    
    private TypecheckHandler typecheckHandler;
    private NodeTypeMapper typeMapper;
    
    private String mimetype = "Unknown";
    private WorkspaceNodeType nodeType = WorkspaceNodeType.UNKNOWN;
    private String analysis = "okay";

    @Autowired
    public LamusFileTypeHandler(TypecheckHandler typecheckHandler, NodeTypeMapper typeMapper) {
        this.typecheckHandler = typecheckHandler;
        this.typeMapper = typeMapper;
    }

    /**
     * @see FileTypeHandler#getTypecheckHandler()
     */
    @Override
    public TypecheckHandler getTypecheckHandler() {
        return this.typecheckHandler;
    }

    /**
     * @see FileTypeHandler#getMimetype()
     */
    @Override
    public String getMimetype() {
        return this.mimetype;
    }

    /**
     * @see FileTypeHandler#getAnalysis()
     */
    @Override
    public String getAnalysis() {
        if (analysis.startsWith("true ")) {
            return analysis.substring(5);
        }
        if (analysis.startsWith("false ")) {
            return analysis.substring(6);
        }
        return analysis;
    }

    /**
     * @see FileTypeHandler#getNodeType()
     */
    @Override
    public WorkspaceNodeType getNodeType() {
        return this.nodeType;
    }

    /**
     * @see FileTypeHandler#checkType(nl.mpi.util.OurURL, java.lang.String, java.lang.String)
     */
    @Override
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
                try {
                
                    logger.debug("LamusFileTypeHandler.checkType: File contents unavailable, using name based method for " + filename);
                    tryName = true;
                    this.typecheckHandler.typecheck(null, filename.toLowerCase());
                    this.analysis = "okay (name)";
                    this.mimetype = this.typecheckHandler.getTypecheckMimetype();
                    if(this.mimetype == null) {
                        String checkResult = this.typecheckHandler.getTypecheckResult();
                        logger.warn("LamusFileTypeHandler.checkType: No archivable file type for file NAME: " + filename + "; result: " + checkResult);
                        this.mimetype = "Unknown";
                        this.analysis = checkResult;
                    }
                } catch(IOException ioe) {
                    String errorMessage = "LamusFileTypeHandler.checkType: File type checker could not access file: " + filename;
                    logger.warn(errorMessage);
                    this.mimetype = "Unknown"; // do NOT use filename as fallback if file is not accessible
                    this.analysis = "Read error for " + filename + " - " + ioe.getMessage();
                    throw new TypeCheckerException(errorMessage, ioe);
                }
            } else {
                
                try {
                    iStream = resourceURL.openStream();
//                    if((filename.endsWith("/") || filename.indexOf(".") == -1) &&
//                        !"file".equals(resourceURL.getProtocol())) {
//                        filename += "/index.html"; // assumption for http: and similar ending with "/"
//                    }

                    this.typecheckHandler.typecheck(iStream, filename.toLowerCase());

                    this.mimetype = this.typecheckHandler.getTypecheckMimetype();

                    if (this.mimetype == null) {
                        logger.warn("LamusFileTypeHandler.checkType: No archivable file type for FILE: " + resourceURL + " (" + filename + "); result: "+
                                this.typecheckHandler.getTypecheckResult());
                        this.mimetype = "Unknown";
                        this.analysis = this.typecheckHandler.getTypecheckResult();
                    } else {
                        this.analysis = "okay (content, name)";
                    }
                } catch (IOException ioe) {
                    String errorMessage = "LamusFileTypeHandler.checkType: File type checker could not access file: " + resourceURL;
                    logger.warn(errorMessage);
                    this.mimetype = "Unknown";
                    this.analysis = "Read error for " + resourceURL + " - " + ioe.getMessage();
                    throw new TypeCheckerException(errorMessage, ioe);
                } finally {
                    IOUtils.closeQuietly(iStream);
                }
            }
                
                
                
                // do NOT derive file name from URL, as the URL could be an extension-free WorkSpace URL...
//                try {
//                    iStream = resourceURL.openStream();
//                } catch (IOException ioex) {
//                    String errorMessage = "LamusFileTypeHandler.checkType: error opening stream to check file";
//                    logger.error(errorMessage, ioex);
//                    throw new TypeCheckerException(errorMessage, ioex);
//                }
                
                //TODO ????
//                if ((filename.endsWith("/") || filename.indexOf(".")==-1) &&
//                    !"file".equals(resourceURL.getProtocol())) {
//                    filename += "/index.html"; // assumption for http: and similar ending with "/"
//                }
                //TODO ????
//            }
            
            String checkResult = this.typecheckHandler.getTypecheckResult();
            setValuesForResult(checkResult, tryName, resourceURL, filename);
        } else {
        
            setValues(this.mimetype);
        }
    }
    
    /**
     * @see FileTypeHandler#checkType(java.io.InputStream, java.lang.String, java.lang.String)
     */
    @Override
    public void checkType(InputStream resourceInputStream, String resourceFilename,/* WorkspaceNodeType nodetype,*/ String mimetype) throws TypeCheckerException {
        
        if(mimetype == null) {
            this.mimetype = "Unknown";
        } else {
            this.mimetype = mimetype;
        }
        
        boolean tryName = false; // whether to use a name based method as fallback
        
        if ((mimetype == null) || mimetype.equals("Unknown") || mimetype.equals("Unspecified")) {
            
//            InputStream iStream = null;
            
//            if (resourceURL == null) {
//                try {
//                
//                    logger.debug("LamusFileTypeHandler.checkType: File contents unavailable, using name based method for " + filename);
//                    tryName = true;
//                    this.typecheckHandler.typecheck(null, filename.toLowerCase());
//                    this.analysis = "okay (name)";
//                    this.mimetype = this.typecheckHandler.getTypecheckMimetype();
//                    if(this.mimetype == null) {
//                        String checkResult = this.typecheckHandler.getTypecheckResult();
//                        logger.warn("LamusFileTypeHandler.checkType: No archivable file type for file NAME: " + filename + "; result: " + checkResult);
//                        this.mimetype = "Unknown";
//                        this.analysis = checkResult;
//                    }
//                } catch(IOException ioe) {
//                    String errorMessage = "LamusFileTypeHandler.checkType: File type checker could not access file: " + filename;
//                    logger.warn(errorMessage);
//                    this.mimetype = "Unknown"; // do NOT use filename as fallback if file is not accessible
//                    this.analysis = "Read error for " + filename + " - " + ioe.getMessage();
//                    throw new TypeCheckerException(errorMessage, ioe);
//                }
//            } else {
                
                try {
//                    iStream = resourceURL.openStream();
//                    if((filename.endsWith("/") || filename.indexOf(".") == -1) &&
//                        !"file".equals(resourceURL.getProtocol())) {
//                        filename += "/index.html"; // assumption for http: and similar ending with "/"
//                    }

//                    this.typecheckHandler.typecheck(iStream, filename.toLowerCase());
                    this.typecheckHandler.typecheck(resourceInputStream, resourceFilename.toLowerCase());

                    this.mimetype = this.typecheckHandler.getTypecheckMimetype();

                    if (this.mimetype == null) {
//                        logger.warn("LamusFileTypeHandler.checkType: No archivable file type for FILE: " + resourceURL + " (" + filename + "); result: "+
                        logger.warn("LamusFileTypeHandler.checkType: No archivable file type for FILE: " + resourceFilename + "; result: " +
                                this.typecheckHandler.getTypecheckResult());
                        this.mimetype = "Unknown";
                        this.analysis = this.typecheckHandler.getTypecheckResult();
                    } else {
                        this.analysis = "okay (content, name)";
                    }
                } catch (IOException ioe) {
                    String errorMessage = "LamusFileTypeHandler.checkType: File type checker could not access file: " + resourceFilename; //+ resourceURL;
                    logger.warn(errorMessage);
                    this.mimetype = "Unknown";
                    this.analysis = "Read error for " + /*resourceURL*/ resourceFilename + " - " + ioe.getMessage();
                    throw new TypeCheckerException(errorMessage, ioe);
                } //finally {
//                    IOUtils.closeQuietly(iStream);
//                }
//            }
                
                
                
                // do NOT derive file name from URL, as the URL could be an extension-free WorkSpace URL...
//                try {
//                    iStream = resourceURL.openStream();
//                } catch (IOException ioex) {
//                    String errorMessage = "LamusFileTypeHandler.checkType: error opening stream to check file";
//                    logger.error(errorMessage, ioex);
//                    throw new TypeCheckerException(errorMessage, ioex);
//                }
                
                //TODO ????
//                if ((filename.endsWith("/") || filename.indexOf(".")==-1) &&
//                    !"file".equals(resourceURL.getProtocol())) {
//                    filename += "/index.html"; // assumption for http: and similar ending with "/"
//                }
                //TODO ????
//            }
            
            String checkResult = this.typecheckHandler.getTypecheckResult();
//            setValuesForResult(checkResult, tryName, resourceURL, filename);
            setValuesForResult(checkResult, tryName, resourceFilename);
        } else {
        
            setValues(this.mimetype);
        }
    }
    
    
    

    /**
     * @see FileTypeHandler#setValues(java.lang.String)
     */
    @Override
    public void setValues(String mimetype) {
        this.mimetype = mimetype;
        this.nodeType = typeMapper.getNodeTypeForMimetype(mimetype);
    }
    
    
//    private String getCheckResult(InputStream iStream, String filename) throws TypeCheckerException {
//        
//        String checkResult = null;
//        try {                
//            checkResult = this.configuredTypeChecker.checkStream(iStream, filename.toLowerCase());
//        } catch(IOException ioex) {
//            String errorMessage = "LamusFileTypeHandler.checkType: error checking result from stream";
//            logger.error(errorMessage, ioex);
//            throw new TypeCheckerException(errorMessage, ioex);
//        } finally {
//            IOUtils.closeQuietly(iStream);
//        }
//        
//        return checkResult;
//    }
    

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
        this.nodeType = typeMapper.getNodeTypeForMimetype(mimetype);
    }
    
    //TODO Should this replace the other method???
    private void setValuesForResult(String checkResult, boolean isCheckingName, String filename) {
        
        if(isCheckingName) {
            this.analysis = "okay (name)";
        } else {
            this.analysis = "okay (content, name)";
        }
        this.mimetype = FileType.resultToMimeType(checkResult);
        if (this.mimetype == null) { // no archivable file format, or wrong name
            if(isCheckingName) {
                logger.warn("LamusFileTypeHandler.checkType: No archivable file type for file NAME: " + filename + " result: " + checkResult);
            } else {
                logger.warn("LamusFileTypeHandler.checkType: No archivable file type for FILE: " + filename + "; result: " + checkResult);
            }
            this.mimetype = "Unknown"; // but do NOT use filename as fallback
            this.analysis = checkResult;
        }
        this.nodeType = typeMapper.getNodeTypeForMimetype(mimetype);
    }
    
    /**
     * @see FileTypeHandler#isResourceArchivable(nl.mpi.util.OurURL, nl.mpi.lamus.typechecking.TypecheckerJudgement, java.lang.StringBuilder)
     */
    @Override
    public boolean isResourceArchivable(OurURL resourceURL, TypecheckerJudgement acceptableJudgementForCorpus, StringBuilder message) {
        
        boolean isArchivable = false;
        TypecheckerJudgement judgement = this.typecheckHandler.getTypecheckJudgement();
        
        if(judgement.compareTo(acceptableJudgementForCorpus) >= 0) {
            isArchivable = true;
            message.append("Resource with URL '").
                    append(resourceURL).
                    append("' is archivable. Judgement '").
                    append(judgement).
                    append("' acceptable.");
        } else {
            message.append("Resource with URL '").
                    append(resourceURL).
                    append("' is archivable. Judgement '").
                    append(judgement).
                    append("' not acceptable - minimum is ").
                    append(acceptableJudgementForCorpus).
                    append(".");
        }
        return isArchivable;
    }
    
    /**
     * @see FileTypeHandler#getTypecheckedResults()
     */
    @Override
    public TypecheckedResults getTypecheckedResults() {
        
        return new LamusTypecheckedResults(nodeType, mimetype, analysis, typecheckHandler.getTypecheckJudgement());
    }
}
