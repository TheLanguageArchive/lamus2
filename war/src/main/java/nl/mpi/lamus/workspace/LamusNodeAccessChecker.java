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
package nl.mpi.lamus.workspace;

import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.lamus.ams.AmsBridge;
import nl.mpi.lamus.dao.WorkspaceDao;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusNodeAccessChecker implements NodeAccessChecker {

    private final ArchiveObjectsDB archiveObjectsDB;
    private final AmsBridge amsBridge;
    private final WorkspaceDao workspaceDao;
    
    LamusNodeAccessChecker(ArchiveObjectsDB archiveObjectsDB, AmsBridge amsBridge, WorkspaceDao workspaceDao) {
        this.archiveObjectsDB = archiveObjectsDB;
        this.amsBridge = amsBridge;
        this.workspaceDao = workspaceDao;
    }

    public boolean canCreateWorkspace(String userID, int archiveNodeID) {
        
        if(!this.archiveObjectsDB.isOnSite(NodeIdUtils.TONODEID(archiveNodeID))) {
            return false;
        }
        if(!this.amsBridge.hasWriteAccess(userID, archiveNodeID)) {
            return false;
        }
        
        //TODO Should it take into account the "sessions" folders, where write access is always true?
        
        if(this.workspaceDao.isNodeLocked(archiveNodeID)) {
            return false;
        }
        
        //TODO Should it check now if any of the child nodes is locked??
            // maybe that's too much to compute for a large workspace...
            // on the other hand, it can also be a lot of waiting when creating the workspace, in case it ends up being locked
            // if there was a way of checking just the leave nodes, it would be a bit easier

        return true;
    }
    
}
