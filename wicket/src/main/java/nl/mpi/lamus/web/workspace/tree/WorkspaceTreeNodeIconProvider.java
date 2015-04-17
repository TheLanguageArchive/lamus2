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
package nl.mpi.lamus.web.workspace.tree;

import nl.mpi.archiving.tree.wicket.components.ArchiveTreeNodeIconProvider;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implementation of the icon provider for the trees in Lamus.
 * @see ArchiveTreeNodeIconProvider
 * @author guisil
 */
@Component
public class WorkspaceTreeNodeIconProvider implements ArchiveTreeNodeIconProvider<WorkspaceTreeNode> {
    
    @Autowired
    private WorkspaceTreeNodeProfileIconMapper profileIconMapper;

    
    /**
     * @see ArchiveTreeNodeIconProvider#getNodeIcon(nl.mpi.archiving.tree.GenericTreeNode)
     */
    @Override
    public ResourceReference getNodeIcon(WorkspaceTreeNode contentNode) {

        if(contentNode.isExternal()) {
            return new PackageResourceReference(WorkspaceTreeNodeIconProvider.class, "al_circle_black.png");
        }
        if(contentNode.isProtected()) {
            return new PackageResourceReference(WorkspaceTreeNodeIconProvider.class, "al_circle_red.png");
        }
        
        WorkspaceNodeType nodeType = contentNode.getType();
        
        if(nodeType == null) {
            return new PackageResourceReference(WorkspaceTreeNodeIconProvider.class, "unknown.png");
        }
        
        switch(nodeType) {
            case METADATA:
                return new PackageResourceReference(WorkspaceTreeNodeIconProvider.class, profileIconMapper.matchProfileIdWithIconName(contentNode.getProfileSchemaURI()));
            case RESOURCE_AUDIO:
                return new PackageResourceReference(WorkspaceTreeNodeIconProvider.class, "audio.gif");
            case RESOURCE_IMAGE:
                return new PackageResourceReference(WorkspaceTreeNodeIconProvider.class, "image.gif");
            case RESOURCE_VIDEO:
                return new PackageResourceReference(WorkspaceTreeNodeIconProvider.class, "video.gif");
            case RESOURCE_WRITTEN:
                return new PackageResourceReference(WorkspaceTreeNodeIconProvider.class, "written.gif");
            case RESOURCE_OTHER:
                return new PackageResourceReference(WorkspaceTreeNodeIconProvider.class, "file.gif");
            default:
                return new PackageResourceReference(WorkspaceTreeNodeIconProvider.class, "unknown.png");
        }
    }
}
