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
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.archive.CorpusStructureBridge;
import nl.mpi.lamus.cmdi.profile.AllowedCmdiProfiles;
import nl.mpi.lamus.cmdi.profile.CmdiProfile;
import nl.mpi.lamus.workspace.exporting.ExporterHelper;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see ExporterHelper
 * @author guisil
 */
@Component
public class WorkspaceExporterHelper implements ExporterHelper {
    
    private final NodeUtil nodeUtil;
    private final CorpusStructureBridge corpusStructureBridge;
    private final ArchiveFileHelper archiveFileHelper;
    private final ArchiveFileLocationProvider archiveFileLocationProvider;
    private final AllowedCmdiProfiles allowedCmdiProfiles;
    
    @Autowired
    public WorkspaceExporterHelper(NodeUtil nUtil, CorpusStructureBridge csBridge,
            ArchiveFileHelper afHelper, AllowedCmdiProfiles cmdiProfiles, ArchiveFileLocationProvider afLocationProvider) {
        nodeUtil = nUtil;
        corpusStructureBridge = csBridge;
        archiveFileHelper = afHelper;
        allowedCmdiProfiles = cmdiProfiles;
        archiveFileLocationProvider = afLocationProvider;
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
        
        if(parentCorpusNamePathToClosestTopNode == null && !acceptNullPath) { // path hasn't been bootstrapped yet
            throw new IllegalArgumentException("The name path closest top node should be provided to this exporter (" + exporterType.toString() + ").");
        }
        if(parentCorpusNamePathToClosestTopNode == null && !nodeUtil.isNodeMetadata(currentNode)) {
            throw new IllegalArgumentException("The name path closest top node should have been bootstrapped before the current node (" + currentNode.getName() + ").");
        }
        
        if(nodeUtil.isNodeMetadata(currentNode)) {
        	String corpusNameDirectoryOfClosestTopNode = null;
        	if (parentNode != null && parentNode.getArchiveURI() != null && parentCorpusNamePathToClosestTopNode != null) {
        		//parent is in the DB -> calculate parent path from parent URL
        		if (currentNode.getArchiveURI() != null) {
        			corpusNameDirectoryOfClosestTopNode = corpusStructureBridge.getCorpusNamePathToClosestTopNode(currentNode);
        		} else {
        			corpusNameDirectoryOfClosestTopNode = archiveFileLocationProvider.getFolderNameBeforeCorpusstructure(parentNode.getArchiveURL().toString());
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
            	namePathToReturn = corpusNameDirectoryOfClosestTopNode != null ? 
            			parentCorpusNamePathToClosestTopNode + File.separator + corpusNameDirectoryOfClosestTopNode : 
            				parentCorpusNamePathToClosestTopNode + File.separator + archiveFileHelper.correctPathElement(parentNode.getName(), "getNamePathToUseForThisExporter");
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
        
        return namePathToReturn;
    }
}
