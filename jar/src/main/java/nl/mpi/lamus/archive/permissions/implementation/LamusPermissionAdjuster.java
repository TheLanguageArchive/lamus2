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
package nl.mpi.lamus.archive.permissions.implementation;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.archive.permissions.PermissionAdjuster;
import nl.mpi.lamus.archive.permissions.PermissionAdjusterHelper;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Adapted from the old LAMUS.
 * @see PermissionAdjuster
 * @author guisil
 */
@Component
public class LamusPermissionAdjuster implements PermissionAdjuster {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusPermissionAdjuster.class);
    
    private final WorkspaceDao workspaceDao;
    private final PermissionAdjusterHelper permissionAdjusterHelper;
    private final ArchiveFileLocationProvider archiveFileLocationProvider;
    
    @Autowired
    public LamusPermissionAdjuster(WorkspaceDao wsDao,
        PermissionAdjusterHelper permAdjustHelper, ArchiveFileLocationProvider fileLocationProvider) {
        workspaceDao = wsDao;
        permissionAdjusterHelper = permAdjustHelper;
        archiveFileLocationProvider = fileLocationProvider;
    }

    /**
     * @see PermissionAdjuster#adjustPermissions(int, nl.mpi.lamus.archive.permissions.implementation.PermissionAdjusterScope)
     */
    @Override
    public void adjustPermissions(int workspaceID, PermissionAdjusterScope scope) {
        
        try {
            permissionAdjusterHelper.loadConfiguredPermissions();
        } catch (IOException ex) {
            logger.warn("Couldn't load permissions configuration file. Won't change file permissions.", ex);
            return;
        }

        if(PermissionAdjusterScope.ALL_NODES.equals(scope)) {
            logger.info("Adjusting permissions for all nodes in workspace " + workspaceID);
            Collection<WorkspaceNode> allNodes = workspaceDao.getNodesForWorkspace(workspaceID);
            adjustPermissionsForNodes(allNodes);
            return;
        }
        if(PermissionAdjusterScope.UNLINKED_NODES_ONLY.equals(scope)) {
            logger.info("Adjusting permissions for unlinked nodes in workpsace " + workspaceID);
            Collection<WorkspaceNode> unlinkedNodesAndDescendants = workspaceDao.getUnlinkedNodesAndDescendants(workspaceID);
            adjustPermissionsForNodes(unlinkedNodesAndDescendants);
            return;
        }
        logger.info("The scope indicated for adjusting permissions in the workspace is not handled. Skipping permission adjustment.");
    }
    
    private void adjustPermissionsForNodes(Collection<WorkspaceNode> nodes) {
        
        int n = 0;
        int changed = 0;
        for(WorkspaceNode node : nodes) {
            if(node.isExternal()) {
                continue;
            } // skip remote files
            n++;
            
            URL nodeURL = node.getArchiveURL();
            
            if(nodeURL == null) {
                //could be a node that was never in the archive and won't be (uploaded and deleted)
                // but could also be an unlinked node which was saved in the orphans folder, for which case we want to continue
                nodeURL = node.getWorkspaceURL();
            }
            
            File nodeFile;
            try {
                nodeFile = new File(nodeURL.toURI());
            } catch (URISyntaxException ex) {
                logger.info("Could not proceed with permission change for location " + node.getArchiveURL(), ex);
                continue;
            }
            
            if(node.getArchiveURL() == null && !archiveFileLocationProvider.isFileInOrphansDirectory(nodeFile)) {
                //in this case we don't need to adjust any permissions; only if the node was in the orphans folder
                continue;
            }
            
            ApaPermission currentPermissions = permissionAdjusterHelper.getCurrentPermissionsForPath(nodeFile.getAbsolutePath());
            ApaPermission desiredPermissions = permissionAdjusterHelper.getDesiredPermissionsForPath(nodeFile.getAbsolutePath());

            if(permissionAdjusterHelper.checkAndRepairFile(nodeFile.getAbsolutePath(), currentPermissions, desiredPermissions)) {
                changed++;
            }
        }
        logger.info("Checked " + n + " files, changed " + changed + " permissions");
    }
}
