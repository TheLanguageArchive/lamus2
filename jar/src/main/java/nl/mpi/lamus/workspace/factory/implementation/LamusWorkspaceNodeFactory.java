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
package nl.mpi.lamus.workspace.factory.implementation;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.Reference;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceNodeFactory
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusWorkspaceNodeFactory implements WorkspaceNodeFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceNodeFactory.class);
    
    private final ArchiveFileHelper archiveFileHelper;
    
    @Autowired
    public LamusWorkspaceNodeFactory(ArchiveFileHelper archiveFileHelper) {
        this.archiveFileHelper = archiveFileHelper;
    }

    /**
     * @see WorkspaceNodeFactory#getNewWorkspaceNode(int, java.net.URI, java.net.URL)
     */
    @Override
    public WorkspaceNode getNewWorkspaceNode(int workspaceID, URI archiveNodeURI, URL archiveNodeURL) {
        
        WorkspaceNode node = new LamusWorkspaceNode(workspaceID, archiveNodeURI, archiveNodeURL);
        return node;
    }
    
    /**
     * @see WorkspaceNodeFactory#getNewWorkspaceMetadataNode(int, java.net.URI, java.net.URL,
     *      nl.mpi.metadata.api.model.MetadataDocument, java.lang.String, boolean, boolean)
     */
    @Override
    public WorkspaceNode getNewWorkspaceMetadataNode(int workspaceID, URI archiveNodeURI, URL archiveNodeURL,
            MetadataDocument document, String name, boolean onSite, boolean isProtected) {
        
        WorkspaceNode node = new LamusWorkspaceNode(workspaceID, archiveNodeURI, archiveNodeURL);
        
        //TODO Use name instead? Was showing weird values for CMDI (e.g. "collection")
        
//        String displayValue = FilenameUtils.getName(archiveNodeURL.getPath());
        node.setName(name);
        node.setTitle(name);
        node.setType(WorkspaceNodeType.METADATA);
        node.setFormat("text/x-cmdi+xml"); //TODO get this based on what? typechecker?
        node.setProfileSchemaURI(document.getDocumentType().getSchemaLocation());

        if(onSite) {
            node.setStatus(WorkspaceNodeStatus.ARCHIVE_COPY);
        } else {
            node.setStatus(WorkspaceNodeStatus.EXTERNAL);
        }
        
        node.setProtected(isProtected);
        
        return node;
    }
    
    /**
     * @see WorkspaceNodeFactory#getNewWorkspaceNode(int, java.net.URI, java.net.URL,
     *          nl.mpi.metadata.api.model.Reference, java.lang.String,
     *          nl.mpi.lamus.workspace.model.WorkspaceNodeType, java.lang.String, boolean, boolean) 
     */
    @Override
    public WorkspaceNode getNewWorkspaceNode(int workspaceID, URI archiveNodeURI, URL archiveNodeURL,
            Reference resourceReference, String mimetype, WorkspaceNodeType nodeType,
            String name, boolean onSite, boolean isProtected) {
        
        WorkspaceNode node = new LamusWorkspaceNode(workspaceID, archiveNodeURI, archiveNodeURL);
        
        //TODO Use name instead? Was showing weird values for CMDI (e.g. "collection")
        
        String displayValue = FilenameUtils.getName(archiveNodeURL.getPath());
        node.setName(displayValue);
        node.setTitle(displayValue);
        node.setType(nodeType);
        node.setFormat(mimetype);
        
        if(onSite) {
            node.setStatus(WorkspaceNodeStatus.VIRTUAL);
        } else {
            node.setStatus(WorkspaceNodeStatus.EXTERNAL);
        }
        
        node.setProtected(isProtected);
        
        return node;
    }

    /**
     * @see WorkspaceNodeFactory#getNewWorkspaceNodeFromFile(int, java.net.URI,
     *      java.net.URI, java.net.URL, java.net.URI, java.lang.String,
     *      nl.mpi.lamus.workspace.model.WorkspaceNodeType, nl.mpi.lamus.workspace.model.WorkspaceNodeStatus, boolean)
     */
    @Override
    public WorkspaceNode getNewWorkspaceNodeFromFile(int workspaceID, URI archiveURI,
            URI originURI, URL workspaceURL, URI profileSchemaURI, String mimetype, WorkspaceNodeType nodeType,
            WorkspaceNodeStatus status, boolean isProtected) {
        
        WorkspaceNode node = new LamusWorkspaceNode();
        node.setWorkspaceID(workspaceID);
        String displayValue = FilenameUtils.getName(workspaceURL.getPath());
        node.setName(displayValue);
        node.setTitle(displayValue);
        node.setOriginURI(originURI);
        node.setWorkspaceURL(workspaceURL);
        node.setProfileSchemaURI(profileSchemaURI);
        node.setFormat(mimetype);
        node.setType(nodeType);
        node.setArchiveURI(archiveURI);
        
        node.setStatus(status);
        
        node.setProtected(isProtected);
        
        return node;
    }

    /**
     * @see WorkspaceNodeFactory#getNewExternalNode(int, java.net.URL)
     */
    @Override
    public WorkspaceNode getNewExternalNode(int workpaceID, URI originURI) {
        
        WorkspaceNode node = new LamusWorkspaceNode();
        node.setWorkspaceID(workpaceID);
        String uriSchemeSpecificPart = originURI.getSchemeSpecificPart();
        String displayValue;
        if(!uriSchemeSpecificPart.endsWith(File.separator)) {
            displayValue = FilenameUtils.getName(uriSchemeSpecificPart);
        } else {
            displayValue = FilenameUtils.getName(uriSchemeSpecificPart.substring(0, uriSchemeSpecificPart.length() - 1));
        }
        node.setName(displayValue);
        node.setTitle(displayValue);
        node.setOriginURI(originURI);
//        if(originURI.getPath().endsWith("cmdi")) { // Try to guess type or leave as unknown?
//            node.setType(WorkspaceNodeType.METADATA);
//        } else {
//            node.setType(WorkspaceNodeType.RESOURCE);
//        }
        node.setType(WorkspaceNodeType.UNKNOWN);
        node.setStatus(WorkspaceNodeStatus.EXTERNAL);
        
        node.setProtected(Boolean.FALSE);
        
        return node;
    }

    /**
     * @see WorkspaceNodeFactory#getNewExternalNodeFromArchive(int, nl.mpi.archiving.corpusstructure.core.CorpusNode, java.net.URI, java.net.URL)
     */
    @Override
    public WorkspaceNode getNewExternalNodeFromArchive(int workspaceID, CorpusNode archiveNode, URI archivePID, URL archiveURL) {
        
        WorkspaceNode node = new LamusWorkspaceNode();
        node.setWorkspaceID(workspaceID);
        
        String displayValue = FilenameUtils.getName(archiveURL.getPath());
        node.setName(displayValue);
        node.setTitle(displayValue);
        
        if(archivePID != null) {
            node.setArchiveURI(archivePID);
        } else {
            node.setArchiveURI(archiveNode.getNodeURI());
        }
        
        node.setArchiveURL(archiveURL);
        try {
            node.setOriginURI(archiveURL.toURI());
        } catch (URISyntaxException ex) {
            logger.warn("URL (" + archiveURL + ") couldn't be converted to URI. OriginURI not set.");
        }
        node.setType(WorkspaceNodeType.UNKNOWN);
        node.setStatus(WorkspaceNodeStatus.EXTERNAL);
        
        node.setProtected(Boolean.FALSE);
        
        return node;
   }
}
