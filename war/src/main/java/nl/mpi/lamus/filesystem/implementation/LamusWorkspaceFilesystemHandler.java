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
package nl.mpi.lamus.filesystem.implementation;

import java.io.File;
import nl.mpi.lamus.configuration.Configuration;
import nl.mpi.lamus.filesystem.WorkspaceFilesystemHandler;
import nl.mpi.lamus.workspace.Workspace;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceFilesystemHandler implements WorkspaceFilesystemHandler {

    private Configuration configuration;
    
    LamusWorkspaceFilesystemHandler(Configuration configuration) {
        this.configuration = configuration;
    }

    public File createWorkspaceDirectory(Workspace workspace) {
        
        File baseDirectory = new File(this.configuration.getWorkspaceBaseDirectory());
        File workspaceDirectory = new File(baseDirectory, "" + workspace.getWorkspaceID());
        
        if(workspaceDirectory.exists()) {
            //TODO log something
            return workspaceDirectory;
        } else {
            if(workspaceDirectory.mkdirs()) {
                //TODO log some successful message
                return workspaceDirectory;
            } else {
                //TODO log some error
                return null;
            }
        }
    }
    
}
