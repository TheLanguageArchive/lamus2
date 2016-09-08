/*
 * Copyright (C) 2014 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.workspace.upload.implementation;

import nl.mpi.lamus.workspace.importing.implementation.MatchImportProblem;
import nl.mpi.lamus.workspace.importing.implementation.LinkImportProblem;
import nl.mpi.lamus.workspace.importing.implementation.ImportProblem;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.handle.util.HandleParser;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.workspace.management.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.upload.WorkspaceUploadNodeMatcher;
import nl.mpi.lamus.workspace.upload.WorkspaceUploadReferenceHandler;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceUploadReferenceHandler
 * 
 * @author guisil
 */
@Component
public class LamusWorkspaceUploadReferenceHandler implements WorkspaceUploadReferenceHandler {

    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceUploadReferenceHandler.class);
    
    private final WorkspaceUploadNodeMatcher workspaceUploadNodeMatcher;
    private final WorkspaceDao workspaceDao;
    private final WorkspaceNodeLinkManager workspaceNodeLinkManager;
    private final HandleParser handleParser;
    private final MetadataAPI metadataAPI;
    private final MetadataApiBridge metadataApiBridge;
    private final WorkspaceFileHandler workspaceFileHandler;
    
    @Autowired
    private WorkspaceDirectoryHandler workspaceDirectoryHandler;
    
    @Autowired
    private ArchiveFileLocationProvider archiveFileLocationProvider;
    
    @Autowired
    public LamusWorkspaceUploadReferenceHandler(
            WorkspaceUploadNodeMatcher wsUploadNodeMatcher,
            WorkspaceDao wsDao, WorkspaceNodeLinkManager wsNodeLinkManager,
            HandleParser handleParser, MetadataAPI mdAPI,
            MetadataApiBridge mdApiBridge, WorkspaceFileHandler wsFileHandler) {
        this.workspaceUploadNodeMatcher = wsUploadNodeMatcher;
        this.workspaceDao = wsDao;
        this.workspaceNodeLinkManager = wsNodeLinkManager;
        this.handleParser = handleParser;
        this.metadataAPI = mdAPI;
        this.metadataApiBridge = mdApiBridge;
        this.workspaceFileHandler = wsFileHandler;
    }
    
    /**
     * @see WorkspaceUploadReferenceHandler#matchReferencesWithNodes(
     *  nl.mpi.lamus.workspace.model.Workspace, java.util.Collection,
     *  nl.mpi.lamus.workspace.model.WorkspaceNode,
     *  nl.mpi.metadata.api.model.ReferencingMetadataDocument, java.util.Map)
     */
    @Override
    public Collection<ImportProblem> matchReferencesWithNodes(
            Workspace workspace, Collection<WorkspaceNode> nodesToCheck,
            WorkspaceNode currentNode, ReferencingMetadataDocument currentDocument,
            Map<MetadataDocument, WorkspaceNode> documentsWithInvalidSelfHandles) {
        
        logger.debug("Trying to match references with nodes in the database");
        
        Collection<ImportProblem> failedLinks = new ArrayList<>();
        
        //check if document has external self-handle
        URI currentSelfHandle = metadataApiBridge.getSelfHandleFromDocument(currentDocument);
        
        if(currentSelfHandle != null && !handleParser.isHandleUriWithKnownPrefix(currentSelfHandle)) {
            documentsWithInvalidSelfHandles.put(currentDocument, currentNode);
        }
        
        List<Reference> references = currentDocument.getDocumentReferences();
        
        for(Reference ref : references) {
            
            logger.debug("Searching match for reference " + ref.getURI());
            
            if(metadataApiBridge.isReferenceTypeAPage(ref)) {
                continue;
            }
            
            URI refLocalURI = ref.getLocation();
            URI refURI = ref.getURI();
            WorkspaceNode matchedNode = null;
            boolean externalNode = false;

            if(refLocalURI != null) {
                
                logger.debug("Reference has localURI " + refLocalURI);
                
                if(refLocalURI.isAbsolute()) {
                	matchedNode = workspaceUploadNodeMatcher.findNodeForPath(nodesToCheck, refLocalURI.toString());
                } else {
                	Path docPath = Paths.get(currentDocument.getFileLocation().getPath());
                	Path path = Paths.get(docPath.getParent().toString(),  refLocalURI.getPath().toString()).normalize();
                	if(path.toFile().exists()) {
                		matchedNode = workspaceUploadNodeMatcher.findNodeForPath(nodesToCheck, path.toString());
                	} else {
                		//if it is a resource made available via the orphans folder look in the original orphans folder since these are not
                		//copied to the workspace
                		Path wsOrphansDirectoryPath = workspaceDirectoryHandler.getOrphansDirectoryInWorkspace(workspace.getWorkspaceID()).toPath().normalize();
                		if (path.startsWith(wsOrphansDirectoryPath)) {
                			try {
                				Path orphansDirectoryPath = archiveFileLocationProvider.getOrphansDirectory(workspace.getTopNodeArchiveURL().toURI()).toPath();
                				Path pathInOrphansDirectory = Paths.get(orphansDirectoryPath.toString(), wsOrphansDirectoryPath.relativize(path).toString());
                				if (pathInOrphansDirectory.toFile().exists()) {
                					matchedNode = workspaceUploadNodeMatcher.findNodeForPath(nodesToCheck, pathInOrphansDirectory.toString());
                				}
                			} catch (URISyntaxException e) {
                				logger.warn("Cannot search for reference: [" + refLocalURI.toString() + "] in orphans directory. Top node archive URL: " + workspace.getTopNodeArchiveURL().toString() + 
                						" cannot be converted to URI. Trying to match by file name only...");
                				matchedNode = workspaceUploadNodeMatcher.findNodeForPath(nodesToCheck, refLocalURI.toString());
                			}
                		} else {
                			//if it is a resource already archived look for it in the archive since these are not copied to the workspace
                			Path pathInArchive = Paths.get(workspace.getTopNodeArchiveURL().toString(), wsOrphansDirectoryPath.relativize(path).toString());
                			if (pathInArchive.toFile().exists()) {
                				matchedNode = workspaceUploadNodeMatcher.findNodeForPath(nodesToCheck, pathInArchive.toString());
                			} else {
                				logger.warn("File for reference: [" + refLocalURI.toString() + "] cannot be found in the workspace, nor archive, nor in the original orphans directory via its localURI." +
                						" Trying to match by file name only...");
                				matchedNode = workspaceUploadNodeMatcher.findNodeForPath(nodesToCheck, refLocalURI.toString());
                			}
                		}
                	}
                }

                if(matchedNode != null) {
                    if(refURI != null && !refURI.toString().isEmpty() && handleParser.isHandleUriWithKnownPrefix(refURI)) {
                            matchedNode.setArchiveURI(handleParser.prepareAndValidateHandleWithHdlPrefix(refURI));
                            workspaceDao.updateNodeArchiveUri(matchedNode);
                    } else {
                        clearReferenceUri(currentDocument, ref, matchedNode);
                    }
                }
            }
            
            if(matchedNode == null) {
                
                if(handleParser.isHandleUriWithKnownPrefix(refURI)) {
                    
                    logger.debug("Match not found yet. Trying to find it using handle " + refURI);
                    
                    URI preparedHandle = handleParser.prepareAndValidateHandleWithHdlPrefix(refURI);
                    try {
                        matchedNode = workspaceUploadNodeMatcher.findNodeForHandle(workspace, nodesToCheck, preparedHandle);
                    } catch(IllegalStateException ex) {
                        removeReference(currentDocument, ref, currentNode);
                        logger.error(ex.getMessage());
                        failedLinks.add(new MatchImportProblem(currentNode, ref, ex.getMessage(), null));
                        continue;
                    }
                    
                    if(matchedNode != null) {
                        //set handle in DB
                        if(!handleParser.areHandlesEquivalent(preparedHandle, matchedNode.getArchiveURI())) {
                            matchedNode.setArchiveURI(preparedHandle);
                            workspaceDao.updateNodeArchiveUri(matchedNode);
                        }
                        if(!refURI.equals(preparedHandle)) {
                            updateHandle(currentDocument, ref, preparedHandle, currentNode);
                        }
                    }
                } else {
                    
                    logger.debug("Match not found yet. Trying to find it using URI " + refURI);
                    
                    matchedNode = workspaceUploadNodeMatcher.findNodeForPath(nodesToCheck, refURI.toString());
                }
                
                //check if it's an external reference
                if(matchedNode == null) {
                    
                    logger.debug("Match not found yet. Trying to find external match for URI " + refURI);
                    
                    matchedNode = workspaceUploadNodeMatcher.findExternalNodeForUri(workspace, refURI);
                    if(matchedNode != null) {
                        externalNode = true;
                    }
                }
            }

            if(matchedNode != null) {
                
                logger.debug("Node " + matchedNode.getWorkspaceNodeID() + " matched. Updating parent file and linking nodes in the database");
                
                // update localURI, even if it was present already, since it could be a relative path and not matching the later calls using the absolute path
                if(!externalNode) {
                    updateLocalUrl(currentDocument, ref, matchedNode);
                }
                
                Collection<WorkspaceNode> alreadyLinkedParents = workspaceDao.getParentWorkspaceNodes(matchedNode.getWorkspaceNodeID());
                
                if(alreadyLinkedParents.isEmpty()) {
                    linkNodes(currentNode, matchedNode, workspace.getWorkspaceID(), failedLinks);
                } else {
                    
                    boolean sameParent = true;
                    for(WorkspaceNode existingParent : alreadyLinkedParents) {
                        boolean areHandlesEquivalent;
                        try {
                            areHandlesEquivalent = handleParser.areHandlesEquivalent(currentNode.getArchiveURI(), existingParent.getArchiveURI());
                        } catch(IllegalArgumentException ex) {
                            sameParent = false;
                            break;
                        }
                        if(!areHandlesEquivalent) {
                            sameParent = false;
                            break;
                        }
                    }
                    
                    if(!sameParent) {
                        //Multiple parents NOT allowed - won't be linked
                        String message = "Matched node (ID " + matchedNode.getWorkspaceNodeID() + ") cannot be linked to parent node (ID " + currentNode.getWorkspaceNodeID() + ") because it already has a parent. Multiple parents are not allowed.";
                        logger.error(message);
                        failedLinks.add(new LinkImportProblem(currentNode, matchedNode, message, null));
                    } else {
                        linkNodes(currentNode, matchedNode, workspace.getWorkspaceID(), failedLinks);
                    }
                }
                
                // check if it is an info link and update the node type accordingly
                if(metadataApiBridge.isReferenceAnInfoLink(currentDocument, ref)) {
                    matchedNode.setType(WorkspaceNodeType.RESOURCE_INFO);
                    workspaceDao.updateNodeType(matchedNode);
                }
                
            } else {
                removeReference(currentDocument, ref, currentNode);
                String fileLocation = currentDocument.getFileLocation().toString();
                String message = "Reference (" + ref.getURI() + ") in node " + currentNode.getWorkspaceNodeID() + " not matched. "
                		+ "Reference removed from '" + fileLocation.substring(fileLocation.lastIndexOf(File.separator) + 1) + "'";
                failedLinks.add(new MatchImportProblem(currentNode, ref, message, null));
            }
            
        }
        return failedLinks;
    }
    
    
    private void clearReferenceUri(ReferencingMetadataDocument document, Reference ref, WorkspaceNode referencedNode) {
        
        ref.setURI(URI.create(""));
        
        try {
            File documentFile = new File(document.getFileLocation().getPath());
            StreamResult documentStreamResult = workspaceFileHandler.getStreamResultForNodeFile(documentFile);
            metadataAPI.writeMetadataDocument(document, documentStreamResult);
        } catch (IOException ex) {
            logger.error("Error clearing the reference for node " + referencedNode.getWorkspaceNodeID(), ex);
        } catch (TransformerException | MetadataException ex) {
            logger.error("Error updating the reference for node " + referencedNode.getWorkspaceNodeID(), ex);
        }
    }
    
    private void removeReference(ReferencingMetadataDocument document, Reference ref, WorkspaceNode currentNode) {
        try {
            document.removeDocumentReference(ref);
            File documentFile = new File(document.getFileLocation().getPath());
            StreamResult documentStreamResult = workspaceFileHandler.getStreamResultForNodeFile(documentFile);
            metadataAPI.writeMetadataDocument(document, documentStreamResult);
        } catch (MetadataException | IOException | TransformerException ex) {
            logger.error("Error removing reference '" + ref.getURI() + "' from node " + currentNode.getWorkspaceNodeID(), ex);
        }
    }
    
    private void updateHandle(ReferencingMetadataDocument document, Reference ref, URI updatedHandle, WorkspaceNode referencedNode) {
        
        StringBuilder message = new StringBuilder();
        
        URI oldHandle = ref.getURI();
        message.append("[old URI: '").append(oldHandle).append("']");
        ref.setURI(updatedHandle);
        
        try {
            File documentFile = new File(document.getFileLocation().getPath());
            StreamResult documentStreamResult = workspaceFileHandler.getStreamResultForNodeFile(documentFile);
            metadataAPI.writeMetadataDocument(document, documentStreamResult);
        } catch (IOException | TransformerException | MetadataException ex) {
            logger.error("Error updating the reference for node " + referencedNode.getWorkspaceNodeID() + message.toString(), ex);
        }
    }
    
    private void updateLocalUrl(ReferencingMetadataDocument document, Reference ref, WorkspaceNode referencedNode) {
        
        URI locationToUse = null;
        try {
            URL urlToUse = referencedNode.getWorkspaceURL();
            if(urlToUse == null) {
                urlToUse = referencedNode.getArchiveURL();
            }
            if(urlToUse != null) {
                locationToUse = urlToUse.toURI();
            } else {
                throw new IllegalArgumentException("No location found for matched node " + referencedNode.getWorkspaceNodeID());
            }
        } catch (URISyntaxException ex) {
            logger.warn("Problems updating localUrl in reference (URI: " + ref.getURI() + ")");
            return;
        }
        
        StringBuilder message = new StringBuilder();
        
        String oldLocation = (ref.getLocation() != null) ? ref.getLocation().toString() : "";
        message.append("[old URL: '").append(oldLocation).append("']");
        ref.setLocation(locationToUse);
        
        try {
            File documentFile = new File(document.getFileLocation().getPath());
            StreamResult documentStreamResult = workspaceFileHandler.getStreamResultForNodeFile(documentFile);
            metadataAPI.writeMetadataDocument(document, documentStreamResult);
        } catch (IOException | TransformerException | MetadataException ex) {
            logger.error("Error updating the reference for node " + referencedNode.getWorkspaceNodeID() + message.toString(), ex);
        }
    }
    
    private void linkNodes(WorkspaceNode parent, WorkspaceNode child, int workspaceID, Collection<ImportProblem> failedLinks) {
        try {
            workspaceNodeLinkManager.linkNodesOnlyInDb(parent, child);
        } catch (WorkspaceException ex) {
            String message = "Error linking nodes " + parent.getWorkspaceNodeID() + " and " + child.getWorkspaceNodeID() + " in workspace " + workspaceID;
            logger.error(message, ex);
            failedLinks.add(new LinkImportProblem(parent, child, message, ex));
        }
    }
}
