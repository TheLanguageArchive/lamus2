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
package nl.mpi.lamus.workspace.model.implementation;

import nl.mpi.archiving.corpusstructure.core.CorpusNodeType;
import nl.mpi.lamus.cmdi.profile.CmdiProfile;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import org.springframework.stereotype.Component;

/**
 * @see NodeUtil
 * @author guisil
 */
@Component
public class LamusNodeUtil implements NodeUtil {
    
    /**
     * @see NodeUtil#convertArchiveNodeType(nl.mpi.archiving.corpusstructure.core.CorpusNodeType)
     */
    @Override
    public WorkspaceNodeType convertArchiveNodeType(CorpusNodeType corpusNodeType) {
        
        if(corpusNodeType == null) {
            return WorkspaceNodeType.UNKNOWN;
        }
        
        switch(corpusNodeType) {
            case METADATA:
                return WorkspaceNodeType.METADATA;
            case COLLECTION:
                return WorkspaceNodeType.METADATA;
            case RESOURCE_IMAGE:
                return WorkspaceNodeType.RESOURCE_IMAGE;
            case RESOURCE_VIDEO:
                return WorkspaceNodeType.RESOURCE_VIDEO;
            case RESOURCE_AUDIO:
                return WorkspaceNodeType.RESOURCE_AUDIO;
            case RESOURCE_LEXICAL:
                return WorkspaceNodeType.RESOURCE_WRITTEN;
            case RESOURCE_ANNOTATION:
                return WorkspaceNodeType.RESOURCE_WRITTEN;
            case RESOURCE_OTHER:
                return WorkspaceNodeType.RESOURCE_OTHER;
            case IMDICATALOGUE:
                return WorkspaceNodeType.METADATA;
            case IMDIINFO:
                return WorkspaceNodeType.RESOURCE_OTHER;
            default:
                return WorkspaceNodeType.UNKNOWN;
        }
    }

    /**
     * @see NodeUtil#convertMimetype(java.lang.String) 
     */
    @Override
    public WorkspaceNodeType convertMimetype(String mimetype) {
        
        if(mimetype == null) {
            return WorkspaceNodeType.UNKNOWN;
        }
        if(mimetype.equals("text/x-cmdi+xml")) {
            return WorkspaceNodeType.METADATA;
        }
        if(mimetype.equals("text/x-lmf-wrap+xml") || mimetype.startsWith("text/")) {
            return WorkspaceNodeType.RESOURCE_WRITTEN;
        }
        if(mimetype.startsWith("audio/")) {
            return WorkspaceNodeType.RESOURCE_AUDIO;
        }
        if(mimetype.startsWith("video/") || mimetype.equals("application/mxf")) {
            return WorkspaceNodeType.RESOURCE_VIDEO;
        }
        if(mimetype.startsWith("image/")) {
            return WorkspaceNodeType.RESOURCE_IMAGE;
        }
        if(mimetype.startsWith("application/")) { // word, pdf, mediatagger...
            return WorkspaceNodeType.RESOURCE_WRITTEN;
        }
        
        return WorkspaceNodeType.RESOURCE_OTHER;
        
    }

    /**
     * @see NodeUtil#isNodeMetadata(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public boolean isNodeMetadata(WorkspaceNode wsNode) {
        return isTypeMetadata(wsNode.getType());
    }

    /**
     * @see NodeUtil#isNodeInfoFile(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public boolean isNodeInfoFile(WorkspaceNode wsNode) {
        
        if(WorkspaceNodeType.RESOURCE_INFO.equals(wsNode.getType())) {
            return true;
        }
        
        return false;
    }
    
    /**
     * @see NodeUtil#isTypeMetadata(nl.mpi.lamus.workspace.model.WorkspaceNodeType)
     */
    @Override
    public boolean isTypeMetadata(WorkspaceNodeType wsNodeType) {
        
        if(WorkspaceNodeType.METADATA.equals(wsNodeType)) {
            return true;
        }
        
        return false;
    }

    /**
     * @see NodeUtil#isProfileLatCorpusOrSession(nl.mpi.lamus.cmdi.profile.CmdiProfile)
     */
    @Override
    public boolean isProfileLatCorpusOrSession(CmdiProfile profile) {
        
        if(profile == null) {
            return false;
        }
        return "lat-corpus".equals(profile.getName()) || "lat-session".equals(profile.getName());
    }
}
