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
package nl.mpi.lamus.workspace.importing.implementation;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import nl.mpi.corpusstructure.ArchiveAccessContext;
import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.lamus.configuration.Configuration;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.typechecking.FileTypeHandler;
import nl.mpi.lamus.typechecking.FileTypeHandlerFactory;
import nl.mpi.lamus.workspace.exception.FileImporterException;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceParentNodeReferenceFactory;
import nl.mpi.lamus.workspace.importing.FileImporter;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNodeLink;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.metadata.api.model.ResourceReference;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.metadata.api.model.HandleCarrier;
import nl.mpi.util.OurURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class ResourceFileImporter implements FileImporter<ResourceReference> {

    private static final Logger logger = LoggerFactory.getLogger(ResourceFileImporter.class);
    
    private ArchiveObjectsDB archiveObjectsDB;
    private WorkspaceDao workspaceDao;
    private Configuration configuration;
    private ArchiveFileHelper archiveFileHelper;
    private FileTypeHandlerFactory fileTypeHandlerFactory;
    private WorkspaceNodeFactory workspaceNodeFactory;
    private WorkspaceParentNodeReferenceFactory workspaceParentNodeReferenceFactory;
    private WorkspaceNodeLinkFactory workspaceNodeLinkFactory;
    private Workspace workspace;
    
    public ResourceFileImporter() {
        
    }
    
    public ResourceFileImporter(ArchiveObjectsDB aoDB, WorkspaceDao wsDao, Configuration config,
            ArchiveFileHelper archiveFileHelper, FileTypeHandlerFactory fileTypeHandlerFactory,
            WorkspaceNodeFactory wsNodeFactory, WorkspaceParentNodeReferenceFactory wsParentNodeReferenceFactory,
            WorkspaceNodeLinkFactory wsNodeLinkFactory, Workspace ws) {
        this.archiveObjectsDB = aoDB;
        this.workspaceDao = wsDao;
        this.configuration = config;
        this.archiveFileHelper = archiveFileHelper;
        this.fileTypeHandlerFactory = fileTypeHandlerFactory;
        this.workspaceNodeFactory = wsNodeFactory;
        this.workspaceParentNodeReferenceFactory = wsParentNodeReferenceFactory;
        this.workspaceNodeLinkFactory = wsNodeLinkFactory;
        this.workspace = ws;
    }
    
    public void importFile(WorkspaceNode parentNode, ReferencingMetadataDocument parentDocument,
            Reference childLink, int childNodeArchiveID) throws FileImporterException {
        
        //TODO if onsite and not in orphans folder: typechecker - gettype()
        
        
        //TODO get url, etc
        URI childURI = childLink.getURI();
        URL childURL;
        try {
            childURL = childURI.toURL();
        } catch (MalformedURLException ex) {
            String errorMessage = "Error getting URL for link " + childURI;
            logger.error(errorMessage, ex);
            throw new FileImporterException(errorMessage, workspace, this.getClass(), ex);
        }
        String childFileName = archiveFileHelper.getFileBasename(childURI.toString());
        String childTitle = archiveFileHelper.getFileTitle(childURI.toString());
        
        //TODO get mime type (from link?)
        WorkspaceNodeType childType = WorkspaceNodeType.UNKNOWN; //TODO What to use here?
        String childMimeType = childLink.getMimetype();
        
        FileTypeHandler fileTypeHandler = fileTypeHandlerFactory.getNewFileTypeHandlerForWorkspace(workspace);
        
        //TODO get onsite
        boolean childIsOnSite = archiveObjectsDB.isOnSite(NodeIdUtils.TONODEID(childNodeArchiveID));
        OurURL childURLWithContext = archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(childNodeArchiveID), ArchiveAccessContext.getFileUrlContext());
        if("file".equals(childURLWithContext.getProtocol())) {
            childIsOnSite = false;
        }
        
        // if not onsite, don't use typechecker
        boolean performTypeCheck = true;
        if(!childIsOnSite) {
            
            //TODO is this call supposed to be like this?
            fileTypeHandler.setValues(childMimeType, "Unspecified", childType); // unspecified category - unused anyway
            
            //TODO change URID for the link in the file that is copied
//            childLink.setURID("NONE"); // flag resource as not on site
            
            performTypeCheck = false;
        } else {
            if(childURL.getProtocol() == null ||
                    childURL.getProtocol().length() == 0 ||
                    "file".equals(childURL.getProtocol())) {

                //TODO check if file is larger than the checker limit
                    // if so, do not typecheck
                File resFile = new File(childURL.getFile());
                if (resFile.length() > configuration.getTypeReCheckSizeLimit()) { // length==0 if !exists, no error
                    // skip checks for big files if from archive
                    if (archiveFileHelper.getOrphansDirectoryName() != null &&
                        resFile.getAbsolutePath().toString().indexOf(archiveFileHelper.getOrphansDirectoryName()) == -1) {
                        // really skip checks: no orphan either
                        performTypeCheck = false;
                        logger.debug("ResourceFileImporter.importFile: Type specified in link " + childLink.getMimetype() +
                            " trusted without checks for big (" + resFile.length() + " bytes) file from archive: " + resFile);
                        
                        //TODO is this call supposed to be like this?
                        fileTypeHandler.setValues(childMimeType, "Unspecified", childType); // unspecified category - unused anyway
                    } else {
                        // will take a while, so log that we did not get stuck
                        if (resFile.length() > (1024*1024)) { // can differ from recheckLimit
                            logger.debug("ResourceFileImporter.importFile: Check for 'big' orphan (" +
                                    resFile.length() + " bytes) may take some time: " + resFile);
                        // small orphans are always checked, but without logging
                        }
                    }
                } // else not too big, just check again
                
            }
        }
        //TODO get file type using typechecker
        if(performTypeCheck) {
            fileTypeHandler.calculateCV(childURL, childFileName, childType, null);
            //TODO what to pass as node type?
            //TODO use mimetype from CMDI?
                // - this would cause the typechecker not to be executed, since the mimetype is known
                    // but anyway these files are already in the archive, so is it really needed to perform a type check?
        }
        
        //TODO etc...
        String childCheckedFormat = fileTypeHandler.getFormat();
        if(childCheckedFormat.startsWith("Un") &&  //TODO use a better way to identify these cases
                childMimeType != null) {
            //TODO WARN
            if (performTypeCheck ) {
                logger.warn("ResourceFileImporter.importFile: Unrecognized file contents, assuming format " + childMimeType +
                        " as specified in metadata file for file: " + childURL);
                logger.info("ResourceFileImporter.importFile: File type check result was: " + 
                        fileTypeHandler.getAnalysis() + " for: " + childURL);
            } else {
                logger.debug("ResourceFileImporter.importFile: Using format " + childMimeType + 
                        " as specified in metadata file for file: " + childURL);
            }
        } else if(!childCheckedFormat.equals(childMimeType)) {
            //TODO do stuff... WARN
            if (childMimeType != null) {
                logger.warn("ResourceFileImporter.importFile: Metadata file claimed format " + childMimeType + " but contents are " +
                    fileTypeHandler.getFormat() + " for file: " + childURL);
            }

            childMimeType = childCheckedFormat;
            //TODO if "un", WARN
            if (childMimeType.startsWith("Un")) {
                logger.info("ResourceFileImporter.importFile: File type check result was: " + 
                        fileTypeHandler.getAnalysis() + " for: " + childURL);
            }
        }
        
        //TODO needsProtection?
        
        
        //TODO create node accordingly and add it to the database
        WorkspaceNode childNode = workspaceNodeFactory.getNewWorkspaceNode(workspace.getWorkspaceID(), childNodeArchiveID, childURL);
        
        //TODO adjust node values according to file/link
        setWorkspaceNodeInformationFromMetadataDocument(childLink, childNode, childTitle, childType, childMimeType, childURL);
        workspaceDao.addWorkspaceNode(childNode);
        
        //TODO add parent link in the database
        WorkspaceParentNodeReference parentNodeReference = 
                workspaceParentNodeReferenceFactory.getNewWorkspaceParentNodeReference(parentNode, childLink);
        
        WorkspaceNodeLink nodeLink = workspaceNodeLinkFactory.getNewWorkspaceNodeLink(
                parentNodeReference.getParentWorkspaceNodeID(), childNode.getWorkspaceNodeID(), childURI);
        workspaceDao.addWorkspaceNodeLink(nodeLink);
        
        //TODO DO NOT copy file to workspace folder... it will only exist as a link in the DB
            // and any changes made to it will be done during workspace submission
        
        //TODO something
        
        
//        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private void setWorkspaceNodeInformationFromMetadataDocument(Reference nodeLink, WorkspaceNode wsNode,
            String nodeName, WorkspaceNodeType nodeType, String nodeMimeType, URL nodeURL) {
        
        wsNode.setName(nodeName);
        String nodeTitle = "(type=" + nodeMimeType + ")";
        wsNode.setTitle(nodeTitle);
        wsNode.setType(nodeType);
        wsNode.setFormat(nodeMimeType);
        String nodePid = WorkspacePidValue.NONE.toString();
        if(nodeLink instanceof HandleCarrier) {
            nodePid = ((HandleCarrier) nodeLink).getHandle();
        } else {
            logger.warn("Resource reference '" + nodeURL.toString() + "' does not contain a handle.");
        }
        wsNode.setPid(nodePid);
        wsNode.setStatus(WorkspaceNodeStatus.NODE_VIRTUAL);
    }
}
