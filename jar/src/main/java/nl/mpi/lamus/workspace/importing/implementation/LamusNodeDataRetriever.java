/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Level;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.UnknownNodeException;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.typechecking.FileTypeHandler;
import nl.mpi.lamus.typechecking.TypecheckedResults;
import nl.mpi.lamus.workspace.exception.TypeCheckerException;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.util.OurURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see NodeDataRetriever
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusNodeDataRetriever implements NodeDataRetriever {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusNodeDataRetriever.class);
    
    private final CorpusStructureProvider corpusStructureProvider;
    private final NodeResolver nodeResolver;
    private final MetadataAPI metadataAPI;
    private final FileTypeHandler fileTypeHandler;
    private final ArchiveFileHelper archiveFileHelper;
    
    @Autowired
    public LamusNodeDataRetriever(CorpusStructureProvider csProvider,
        NodeResolver nodeResolver, MetadataAPI mAPI,
        FileTypeHandler fileTypeHandler, ArchiveFileHelper archiveFileHelper) {
        this.corpusStructureProvider = csProvider;
        this.nodeResolver = nodeResolver;
        this.metadataAPI = mAPI;
        this.fileTypeHandler = fileTypeHandler;
        this.archiveFileHelper = archiveFileHelper;
    }

    /**
     * @see NodeDataRetriever#getArchiveNodeMetadataDocument(int)
     */
//    @Override
//    public MetadataDocument getArchiveNodeMetadataDocument(int nodeArchiveID)
//            throws IOException, MetadataException, UnknownNodeException {
//        
//        OurURL tempUrl = archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(nodeArchiveID), ArchiveAccessContext.getFileUrlContext());
//	if (tempUrl == null) {
//	    throw new UnknownNodeException("No known URL for node with ID " + nodeArchiveID);
//	}
//	URL nodeArchiveURL = tempUrl.toURL();
//
//	MetadataDocument document = metadataAPI.getMetadataDocument(nodeArchiveURL);
//
//        return document;
//    }
//    
    /**
     * @see NodeDataRetriever#getResourceURL(nl.mpi.metadata.api.model.Reference)
     */
//    @Override
//    public OurURL getResourceURL(Reference resourceReference) throws MalformedURLException, UnknownNodeException {
//        
//        OurURL resourceURL = null;
//        
//        if(resourceReference instanceof HandleCarrier) {
//            String resourceHandle = ((HandleCarrier) resourceReference).getHandle();
//            resourceURL = this.archiveObjectsDB.getObjectURLForPid(resourceHandle);
//            
//            //TODO can't assume that the link always has a handle
//        }
//        
//        if(resourceURL == null) {
//            
//            //TODO Something else
//                //TODO get URL from nodeID instead?
//            
//            resourceURL = new OurURL(resourceReference.getURI().toURL());
//        }
//        
//        return resourceURL;
//    }
    
    //TODO review this method
    
    @Override
    public URL getNodeArchiveURL(URI nodeArchiveURI) {
        
        try {
            CorpusNode archiveNode = corpusStructureProvider.getNode(nodeArchiveURI);
            URL nodeArchiveURL = nodeResolver.getUrl(archiveNode);
            return nodeArchiveURL;
            
        } catch (UnknownNodeException ex) {
            throw new UnsupportedOperationException("exception not handled yet", ex);
        }
        
    }
    
    /**
     * @see NodeDataRetriever#shouldResourceBeTypechecked(nl.mpi.metadata.api.model.Reference, nl.mpi.util.OurURL, int)
     */
    @Override
    public boolean shouldResourceBeTypechecked(Reference resourceReference, OurURL resourceOurURL) {
        
        // if not onsite, don't use typechecker
        boolean performTypeCheck = true;
        if(!archiveFileHelper.isUrlLocal(resourceOurURL)) {
            
            //TODO is this call supposed to be like this?
            fileTypeHandler.setValues(resourceReference.getMimetype());
            
            //TODO change URID for the link in the file that is copied
//            childLink.setURID("NONE"); // flag resource as not on site
            
            performTypeCheck = false;
        } else {
                File resFile = new File(resourceOurURL.getPath());
                //TODO check if file is larger than the checker limit
                    // if so, do not typecheck
                if (archiveFileHelper.isFileSizeAboveTypeReCheckSizeLimit(resFile)) { // length==0 if !exists, no error
                    
                    // skip checks for big files if from archive
                    if(archiveFileHelper.isFileInOrphansDirectory(resFile)) {
                        // really skip checks: no orphan either
                        performTypeCheck = false;
                        logger.debug("ResourceNodeImporter.importNode: Type specified in link " + resourceReference.getMimetype() +
                            " trusted without checks for big (" + resFile.length() + " bytes) file from archive: " + resFile);
                        
                        //TODO is this call supposed to be like this?
                        fileTypeHandler.setValues(resourceReference.getMimetype());
                    } else {
                        // will take a while, so log that we did not get stuck
                        if (resFile.length() > (1024*1024)) { // can differ from recheckLimit
                            logger.debug("ResourceNodeImporter.importNode: Check for 'big' orphan (" +
                                    resFile.length() + " bytes) may take some time: " + resFile);
                        // small orphans are always checked, but without logging
                        }
                    }
                } // else not too big, just check again
                
//            }
        }
        
        return performTypeCheck;
    }
    
    //TODO review this method
    
    /**
     * @see NodeDataRetriever#triggerResourceFileCheck(nl.mpi.util.OurURL)
     */
    @Override
    public TypecheckedResults triggerResourceFileCheck(OurURL resourceURL) throws TypeCheckerException {
        
        String resourceFileName = archiveFileHelper.getFileBasename(resourceURL.toString());
        
        //TODO get file type using typechecker

        fileTypeHandler.checkType(resourceURL, resourceFileName,/* childType,*/ null);
        
        //TODO what to pass as node type?
        //TODO use mimetype from CMDI?
            // - this would cause the typechecker not to be executed, since the mimetype is known
                // but anyway these files are already in the archive, so is it really needed to perform a type check?
            
        //TODO etc...        
        
        return fileTypeHandler.getTypecheckedResults();
    }
    
    /**
     * @see NodeDataRetriever#triggerResourceFileCheck(java.io.InputStream, java.lang.String)
     */
    @Override
    public TypecheckedResults triggerResourceFileCheck(InputStream resourceInputStream, String resourceFilename) throws IOException {
        
        fileTypeHandler.checkType(resourceInputStream, resourceFilename, null);
        
        return fileTypeHandler.getTypecheckedResults();
    }
    
    //TODO review this method
    
    /**
     * @see NodeDataRetriever#verifyTypecheckedResults(nl.mpi.util.OurURL, nl.mpi.metadata.api.model.Reference, nl.mpi.lamus.typechecking.TypecheckedResults)
     */
    @Override
    public void verifyTypecheckedResults(OurURL resourceURL, Reference resourceReference, TypecheckedResults typecheckedResults) {
        
        String resourceReferenceMimetype = resourceReference.getMimetype();
        String resourceCheckedMimetype = typecheckedResults.getCheckedMimetype();
        
        if(typecheckedResults.isTypeUnspecified() && resourceReferenceMimetype != null) {
            //TODO WARN
            logger.warn("ResourceNodeImporter.importNode: Unrecognized file contents, assuming format " + resourceReferenceMimetype +
                    " as specified in metadata file for file: " + resourceURL);
            logger.info("ResourceNodeImporter.importNode: File type check result was: " + 
                    fileTypeHandler.getAnalysis() + " for: " + resourceURL);
        } else if(!resourceCheckedMimetype.equals(resourceReferenceMimetype)) {
            //TODO do stuff... WARN
            if (resourceReferenceMimetype != null) {
                logger.warn("ResourceNodeImporter.importNode: Metadata file claimed format " + resourceReferenceMimetype + " but contents are " +
                    fileTypeHandler.getMimetype() + " for file: " + resourceURL);

                //TODO if "un", WARN
                if (resourceReferenceMimetype.startsWith("Un")) {
                    logger.info("ResourceNodeImporter.importNode: File type check result was: " + 
                        fileTypeHandler.getAnalysis() + " for: " + resourceURL);
                }
            }
        }
    }

    /**
     * @see NodeDataRetriever#getNewArchiveURI()
     */
    @Override
    public URI getNewArchiveURI() {
        
        //TODO Maybe it should be generated in a different way?
        
        URI newArchiveURI;
        try {
            newArchiveURI = new URI(UUID.randomUUID().toString());
        } catch (URISyntaxException ex) {
            throw new UnsupportedOperationException("exception not handled yet", ex);
        }
        
//        node.setArchiveURI(newArchiveURI);
        return newArchiveURI;
    }
    
}
