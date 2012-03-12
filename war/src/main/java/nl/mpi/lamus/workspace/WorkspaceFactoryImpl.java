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

import nl.mpi.lamus.Configuration;
import nl.mpi.lamus.ams.AmsBridge;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class WorkspaceFactoryImpl implements WorkspaceFactory {
    
    private AmsBridge amsBridge;
    
    /**
     * 
     */
    WorkspaceFactoryImpl(AmsBridge amsBridge) {
        this.amsBridge = amsBridge;
    }
    
    
    /**
     * 
     * @return 
     */
    public Workspace getNewWorkspace(String userID, int archiveTopNodeID) {
        
        long usedStorageSpace = this.amsBridge.getUsedStorageSpace(userID, archiveTopNodeID);
        long maxStorageSpace = this.amsBridge.getMaxStorageSpace(userID, archiveTopNodeID);
        
        if(usedStorageSpace == -1) {
            usedStorageSpace = 0;
        }
        if(maxStorageSpace == -1) {
            maxStorageSpace = Configuration.getInstance().getDefaultMaxStorageSpace();
        }
        
        Workspace workspace = new LamusWorkspace(userID, usedStorageSpace, maxStorageSpace);
        //TODO set more values?
        
        return workspace;
        
    }
    
}
