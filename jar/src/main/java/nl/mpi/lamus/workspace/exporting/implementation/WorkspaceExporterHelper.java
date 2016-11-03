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
package nl.mpi.lamus.workspace.exporting.implementation;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.archive.CorpusStructureBridge;
import nl.mpi.lamus.cmdi.profile.AllowedCmdiProfiles;
import nl.mpi.lamus.cmdi.profile.CmdiProfile;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.workspace.exporting.ExporterHelper;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @see ExporterHelper
 * @author guisil
 */
@Component
public class WorkspaceExporterHelper implements ExporterHelper {
	
	private static final Logger logger = LoggerFactory.getLogger(WorkspaceExporterHelper.class);
    
    private final NodeUtil nodeUtil;
    private final CorpusStructureBridge corpusStructureBridge;
    private final ArchiveFileHelper archiveFileHelper;
    private final ArchiveFileLocationProvider archiveFileLocationProvider;
    private final AllowedCmdiProfiles allowedCmdiProfiles;
    private final WorkspaceDao workspaceDao;
    
    @Autowired
    @Qualifier("metadataDirectoryName")
    private String metadataDirectoryName;
    
    @Autowired
    public WorkspaceExporterHelper(NodeUtil nUtil, CorpusStructureBridge csBridge,
            ArchiveFileHelper afHelper, AllowedCmdiProfiles cmdiProfiles, ArchiveFileLocationProvider afLocationProvider, WorkspaceDao wsDao) {
        nodeUtil = nUtil;
        corpusStructureBridge = csBridge;
        archiveFileHelper = afHelper;
        allowedCmdiProfiles = cmdiProfiles;
        archiveFileLocationProvider = afLocationProvider;
        workspaceDao = wsDao;
    }

    /**
     * @see ExporterHelper#getNamePathToUseForThisExporter(
     *  nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode,
     *  java.lang.String, boolean, java.lang.Class) 
     */
    @Override
    public String getNamePathToUseForThisExporter(
            WorkspaceNode currentNode, WorkspaceNode parentNode,
            String parentCorpusNamePathToClosestTopNode,
            boolean acceptNullPath, Class exporterType) {
        
        String namePathToReturn;
        
        logger.trace("Current node with WS URL: " + currentNode.getWorkspaceURL() + " Archive URL: " + currentNode.getArchiveURL() + " Archive URI: " + currentNode.getArchiveURI() + " parentCorpusNamePathToClosestTopNode: " + parentCorpusNamePathToClosestTopNode + " is metadata: " + nodeUtil.isNodeMetadata(currentNode) + " acceptNullPath: " + acceptNullPath);
        
        if(parentCorpusNamePathToClosestTopNode == null && !acceptNullPath) { // path hasn't been bootstrapped yet
            throw new IllegalArgumentException("The name path closest top node should be provided to this exporter (" + exporterType.toString() + ").");
        }
        if(parentCorpusNamePathToClosestTopNode == null && !nodeUtil.isNodeMetadata(currentNode)) {
            throw new IllegalArgumentException("The name path closest top node should have been bootstrapped before the current node (" + currentNode.getName() + ").");
        }
        
        if(nodeUtil.isNodeMetadata(currentNode)) {
        	String corpusNameDirectoryOfClosestTopNode = null;
        	if (parentNode != null)
        		logger.trace("Parent node ArchiveURI: " + parentNode.getArchiveURI() + " Archive URL: " + parentNode.getArchiveURL());
        	if (parentNode != null && parentNode.getArchiveURI() != null && parentCorpusNamePathToClosestTopNode != null) {
        		//parent is in the DB -> calculate parent path from parent URL
                CmdiProfile currentNodeProfile = allowedCmdiProfiles.getProfile(currentNode.getProfileSchemaURI().toString());
        		if (currentNode.getArchiveURI() != null) {
        			corpusNameDirectoryOfClosestTopNode = corpusStructureBridge.getCorpusNamePathToClosestTopNode(currentNode);
        		} else if (parentCorpusNamePathToClosestTopNode.isEmpty()) {
        			//is top node
        			//can return immediately
        			return archiveFileLocationProvider.getFolderNameBeforeCorpusstructure(parentNode.getArchiveURL().toString());
        		} else if ("session".equals(currentNodeProfile.getTranslateType())) {
        			// verify if there are already sibling sessions and use their existing path if so
        			corpusNameDirectoryOfClosestTopNode = getArchiveSiblingSessionsPath(parentNode);
        			logger.trace("Found sibling session on path: " + corpusNameDirectoryOfClosestTopNode);
        			if (corpusNameDirectoryOfClosestTopNode == null) {
        				corpusNameDirectoryOfClosestTopNode = archiveFileHelper.correctPathElement(parentNode.getName(), "getNamePathToUseForThisExporter");
        			} else
        				//can return immediately
        				return corpusNameDirectoryOfClosestTopNode;
        		} else {
        			corpusNameDirectoryOfClosestTopNode = archiveFileHelper.correctPathElement(parentNode.getName(), "getNamePathToUseForThisExporter");
        		}
        	}
            if(parentCorpusNamePathToClosestTopNode == null) { // path hasn't been bootstrapped yet  
                namePathToReturn = corpusStructureBridge.getCorpusNamePathToClosestTopNode(currentNode);
            } else if(parentCorpusNamePathToClosestTopNode.isEmpty()) { // is top node
            	namePathToReturn = corpusNameDirectoryOfClosestTopNode != null ? 
            			corpusNameDirectoryOfClosestTopNode : archiveFileHelper.correctPathElement(parentNode.getName(), "getNamePathToUseForThisExporter");
            } else if(CorpusStructureBridge.IGNORE_CORPUS_PATH.equals(parentCorpusNamePathToClosestTopNode)) {
                namePathToReturn = CorpusStructureBridge.IGNORE_CORPUS_PATH;
            } else {
            	if (corpusNameDirectoryOfClosestTopNode != null) {
            		int pathIndexInName = corpusNameDirectoryOfClosestTopNode.lastIndexOf(parentCorpusNamePathToClosestTopNode);
            		int nameIndexInPath = parentCorpusNamePathToClosestTopNode.lastIndexOf(corpusNameDirectoryOfClosestTopNode);
            		if (pathIndexInName != -1) {
            			namePathToReturn = corpusNameDirectoryOfClosestTopNode;
            		} else if (nameIndexInPath != -1) {
            			namePathToReturn = parentCorpusNamePathToClosestTopNode;
            		} else { 
            			namePathToReturn = parentCorpusNamePathToClosestTopNode + File.separator + corpusNameDirectoryOfClosestTopNode;
            			int separatorIndex = corpusNameDirectoryOfClosestTopNode.lastIndexOf(File.separator);
            			if (separatorIndex != -1) {
            				String corpusNameDirectoryParentPath = corpusNameDirectoryOfClosestTopNode.substring(0, separatorIndex);
            				String corpusNameDirectory = corpusNameDirectoryOfClosestTopNode.substring(separatorIndex + 1);
            				if (parentCorpusNamePathToClosestTopNode.contains(corpusNameDirectoryParentPath)) {
            					namePathToReturn = parentCorpusNamePathToClosestTopNode + File.separator + corpusNameDirectory;
            				}
            			}
            		}
            	} else {
            		namePathToReturn = parentCorpusNamePathToClosestTopNode + File.separator + archiveFileHelper.correctPathElement(parentNode.getName(), "getNamePathToUseForThisExporter");
            	}
            }
        } else if(nodeUtil.isNodeInfoFile(currentNode)) {
            
            CmdiProfile parentProfile = allowedCmdiProfiles.getProfile(parentNode.getProfileSchemaURI().toString());
            
            if("corpus".equals(parentProfile.getTranslateType())) {
                namePathToReturn = parentCorpusNamePathToClosestTopNode + File.separator + archiveFileHelper.correctPathElement(parentNode.getName(), "getNamePathToUseForThisExporter");
            } else if("session".equals(parentProfile.getTranslateType())) {
                namePathToReturn = parentCorpusNamePathToClosestTopNode;
            } else {
                throw new IllegalArgumentException("Metadata should be translated to either corpus or session");
            }
            
        } else {
            namePathToReturn = parentCorpusNamePathToClosestTopNode;
        }
        
        logger.trace("Returning name path: " + namePathToReturn);
        
        return namePathToReturn;
    }
    
    private String getArchiveSiblingSessionsPath (WorkspaceNode parentNode) {
    	String pathToReturn = null;
		Collection<WorkspaceNode> archiveDescendants = workspaceDao.getDescendantWorkspaceNodesByType(parentNode.getWorkspaceNodeID(), WorkspaceNodeType.METADATA);
		Iterator<WorkspaceNode> it = archiveDescendants.iterator();
		CmdiProfile descendantProfile = null;
		WorkspaceNode descendant = null;
		boolean siblingFound = false;
		while (it.hasNext()) {
			descendant = it.next();
			if (descendant.getArchiveURI() != null) {
				descendantProfile = allowedCmdiProfiles.getProfile(descendant.getProfileSchemaURI().toString());
				if("session".equals(descendantProfile.getTranslateType())) {
					siblingFound = true;
					break;
				}
			}
		}
		if (siblingFound) {
			WorkspaceNode topNode = null;
			try {
				topNode = workspaceDao.getWorkspaceTopNode(parentNode.getWorkspaceID());
			} catch (WorkspaceNodeNotFoundException e) {
				throw new IllegalArgumentException("Workspace with id: " + parentNode.getWorkspaceID() + " not found.");
			}
			pathToReturn = descendant.getArchiveURL().getPath();
			logger.trace("Sibling session found on: " + pathToReturn);
			
			String topNodeFolderName = archiveFileLocationProvider.getFolderNameBeforeCorpusstructure(topNode.getArchiveURL().toString());

			pathToReturn = pathToReturn.substring(pathToReturn.lastIndexOf(topNodeFolderName));
			pathToReturn = pathToReturn.substring(0, pathToReturn.lastIndexOf(File.separator + metadataDirectoryName));
		}
		return pathToReturn;
    }
}
