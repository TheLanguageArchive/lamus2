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

import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.lamus.ams.AmsBridge;
import nl.mpi.lamus.configuration.Configuration;
import nl.mpi.lamus.workspace.LamusWorkspace;
import nl.mpi.lamus.workspace.Workspace;
import nl.mpi.lamus.workspace.factory.WorkspaceFactory;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceFactory implements WorkspaceFactory {
    
    private final AmsBridge amsBridge;
    private final Configuration configuration;
    
    /**
     * 
     */
    LamusWorkspaceFactory(AmsBridge amsBridge, Configuration configuration) {
        this.amsBridge = amsBridge;
        this.configuration = configuration;
    }
    
    
    /**
     * 
     * @return 
     */
    public Workspace getNewWorkspace(String userID, int archiveTopNodeID) {
        
        long usedStorageSpace = this.amsBridge.getUsedStorageSpace(userID, NodeIdUtils.TONODEID(archiveTopNodeID));
        long maxStorageSpace = this.amsBridge.getMaxStorageSpace(userID, NodeIdUtils.TONODEID(archiveTopNodeID));
        
        if(usedStorageSpace == -1) {
            usedStorageSpace = 0;
        }
        if(maxStorageSpace == -1) {
            maxStorageSpace = this.configuration.getDefaultMaxStorageSpace();
        }
        
        Workspace workspace = new LamusWorkspace(userID, usedStorageSpace, maxStorageSpace);
        //TODO set more values?
        
        return workspace;
        
    }
    
}
