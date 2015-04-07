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
import java.net.URI;
import java.net.URL;
import java.util.List;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.NodeNotFoundException;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.typechecking.FileTypeHandler;
import nl.mpi.lamus.typechecking.TypecheckedResults;
import nl.mpi.lamus.exception.TypeCheckerException;
import nl.mpi.lamus.typechecking.TypecheckerConfiguration;
import nl.mpi.lamus.typechecking.TypecheckerJudgement;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.metadata.api.model.Reference;
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
    private final FileTypeHandler fileTypeHandler;
    private TypecheckerConfiguration typecheckerConfiguration;
    private final ArchiveFileHelper archiveFileHelper;
    
    @Autowired
    public LamusNodeDataRetriever(CorpusStructureProvider csProvider,
        NodeResolver nodeResolver, FileTypeHandler fileTypeHandler,
        TypecheckerConfiguration typecheckerConfiguration, ArchiveFileHelper archiveFileHelper) {
        this.corpusStructureProvider = csProvider;
        this.nodeResolver = nodeResolver;
        this.fileTypeHandler = fileTypeHandler;
        this.typecheckerConfiguration = typecheckerConfiguration;
        this.archiveFileHelper = archiveFileHelper;
    }

    @Override
    public URL getNodeArchiveURL(URI nodeArchiveURI) throws NodeNotFoundException {
        
        CorpusNode archiveNode = corpusStructureProvider.getNode(nodeArchiveURI);
        if(archiveNode == null) {
            String message = "Archive node not found: " + nodeArchiveURI;
            NodeNotFoundException ex = new NodeNotFoundException(nodeArchiveURI, message);
            logger.error(ex.getMessage(), ex);
            throw ex;
        }
        URL nodeArchiveURL = nodeResolver.getUrl(archiveNode);
        return nodeArchiveURL;
    }
    
    /**
     * @see NodeDataRetriever#shouldResourceBeTypechecked(nl.mpi.metadata.api.model.Reference, java.io.File, nl.mpi.archiving.corpusstructure.core.CorpusNode)
     */
    @Override
    public boolean shouldResourceBeTypechecked(Reference resourceReference, File resourceFile, CorpusNode resourceNode) {
        
        // if not onsite, don't use typechecker
        boolean performTypeCheck = true;
        
        if(resourceNode != null && !resourceNode.isOnSite()) {
            
            performTypeCheck = false;
            logger.info("LamusNodeDataRetriever: node points to non-local object according to DB ("
                    + resourceNode.getNodeURI() + "); will not be checked");
            
        } else if(resourceNode != null && archiveFileHelper.isFileSizeAboveTypeReCheckSizeLimit(resourceFile)) { //only avoid re-checking already archived files if they're too large

            performTypeCheck = false;
            logger.info("LamusNodeDataRetriever: node (" + resourceNode.getNodeURI() + ") already archived too large ("
                 + resourceFile.length() + " bytes); will not re-check");
                
        } else if(resourceNode == null) {

            performTypeCheck = true;
            
            // will take a while, so log that we did not get stuck
            if (resourceFile.length() > (1024*1024)) { // can differ from recheckLimit
                logger.info("LamusNodeDataRetriever: Check for large file (" +
                        resourceFile.length() + " bytes) may take some time: " + resourceFile);
            }
        }
        
        //if already archived and not too large, check anyway - default value
        
        return performTypeCheck;
    }
    
    /**
     * @see NodeDataRetriever#triggerResourceFileCheck(java.net.URL, java.lang.String)
     */
    @Override
    public TypecheckedResults triggerResourceFileCheck(URL resourceFileUrl, String resourceFilename) throws TypeCheckerException {
        
        return fileTypeHandler.checkType(resourceFileUrl, resourceFilename);
    }
    
    //TODO TEST this method
    
    /**
     * @see NodeDataRetriever#verifyTypecheckedResults(java.io.File,
     *      nl.mpi.metadata.api.model.Reference, nl.mpi.lamus.typechecking.TypecheckedResults)
     */
    @Override
    public void verifyTypecheckedResults(File resourceFile, Reference resourceReference, TypecheckedResults typecheckedResults) {
        
        String resourceReferenceMimetype = resourceReference.getMimetype();
        String resourceCheckedMimetype = typecheckedResults.getCheckedMimetype();
        
        if(typecheckedResults.isTypeUnspecified() && resourceReferenceMimetype != null) {

            logger.warn("ResourceNodeImporter.importNode: Unrecognized file contents, assuming format " + resourceReferenceMimetype +
                    " as specified in metadata file for file: " + resourceFile.getAbsolutePath());
            logger.info("ResourceNodeImporter.importNode: File type check result was: " + 
                    typecheckedResults.getAnalysis() + " for: " + resourceFile.getAbsolutePath());
        } else if(!resourceCheckedMimetype.equals(resourceReferenceMimetype)) {

            if (resourceReferenceMimetype != null) {
                logger.warn("ResourceNodeImporter.importNode: Metadata file claimed format " + resourceReferenceMimetype + " but contents are " +
                    typecheckedResults.getCheckedMimetype() + " for file: " + resourceFile.getAbsolutePath());

                if (resourceReferenceMimetype.startsWith("Un")) {
                    logger.info("ResourceNodeImporter.importNode: File type check result was: " + 
                        typecheckedResults.getAnalysis() + " for: " + resourceFile.getAbsolutePath());
                }
            }
        }
    }
    
    /**
     * @see NodeDataRetriever#isCheckedResourceArchivable(java.net.URL, java.lang.StringBuilder)
     */
    @Override
    public boolean isCheckedResourceArchivable(TypecheckedResults typecheckedResults, URL urlToCheckInConfiguration, StringBuilder message) {
        
        TypecheckerJudgement acceptableJudgement = this.typecheckerConfiguration.getAcceptableJudgementForLocation(urlToCheckInConfiguration);
        return this.fileTypeHandler.isCheckedResourceArchivable(typecheckedResults, acceptableJudgement, message);
    }
    
    /**
     * @see NodeDataRetriever#isNodeToBeProtected(java.net.URI)
     */
    @Override
    public boolean isNodeToBeProtected(URI archiveNodeUri) {
        
        List<URI> parents = corpusStructureProvider.getParentNodeURIs(archiveNodeUri);
        
        if(parents.size() > 1) {
            return true;
        } else {
            return false;
        }
    }
}
