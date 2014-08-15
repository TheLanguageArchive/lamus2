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

import java.net.URI;
import nl.mpi.lamus.ams.AmsServiceBridge;
import nl.mpi.lamus.workspace.factory.WorkspaceFactory;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceFactory
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusWorkspaceFactory implements WorkspaceFactory {
    
    private final AmsServiceBridge amsBridge;
    
    @Autowired
    @Qualifier("defaultMaxStorageSpaceInBytes")
    private long defaultMaxStorageSpaceInBytes;
    
    @Autowired
    public LamusWorkspaceFactory(AmsServiceBridge amsBridge) {
        this.amsBridge = amsBridge;
    }
    
    
    /**
     * @see WorkspaceFactory#getNewWorkspace(java.lang.String, java.net.URI)
     */
    @Override
    public Workspace getNewWorkspace(String userID, URI archiveTopNodeURI) {
        
        long usedStorageSpace = this.amsBridge.getUsedStorageSpace(userID, archiveTopNodeURI);
        long maxStorageSpace = this.amsBridge.getMaxStorageSpace(userID, archiveTopNodeURI);
        
        if(usedStorageSpace == -1) {
            usedStorageSpace = 0;
        }
        if(maxStorageSpace == -1) {
            maxStorageSpace = defaultMaxStorageSpaceInBytes;
        }
        
        Workspace workspace = new LamusWorkspace(userID, usedStorageSpace, maxStorageSpace);
        //TODO set more values?
        
        //SET TOPNODEID?????
        
        return workspace;
        
    }
    
}
