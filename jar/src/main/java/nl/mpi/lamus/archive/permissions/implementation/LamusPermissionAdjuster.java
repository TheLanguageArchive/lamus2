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
import java.util.Collection;
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
    
    private WorkspaceDao workspaceDao;
    private PermissionAdjusterHelper permissionAdjusterHelper;
    
    @Autowired
    public LamusPermissionAdjuster(WorkspaceDao wsDao,
        PermissionAdjusterHelper permAdjustHelper) {
        workspaceDao = wsDao;
        permissionAdjusterHelper = permAdjustHelper;
    }

    /**
     * @see PermissionAdjuster#adjustPermissions(int)
     */
    @Override
    public void adjustPermissions(int workspaceID) {
        try {
            permissionAdjusterHelper.loadConfiguredPermissions();
        } catch (IOException ex) {
            logger.warn("Couldn't load permissions configuration file. Won't change file permissions.", ex);
            return;
        }

        Collection<WorkspaceNode> allNodes = workspaceDao.getNodesForWorkspace(workspaceID);
        
        int n = 0;
        int changed = 0;
        for(WorkspaceNode node : allNodes) {
            if(node.isExternal()) {
                continue;
            } // skip remote files
            n++;
            
            File nodeFile = null;
            try {
                nodeFile = new File(node.getArchiveURL().toURI());
            } catch (URISyntaxException ex) {
                logger.info("Could not proceed with permission change for location " + node.getArchiveURL(), ex);
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
