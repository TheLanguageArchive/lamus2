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
package nl.mpi.lamus.workspace.model;

import java.net.URI;

/**
 * Represents a node replacement within a workspace.
 * @author guisil
 */
public interface WorkspaceNodeReplacement {
    
    public URI getOldNodeURI();
    
    public void setOldNodeURI(URI oldNodeURI);
    
    public URI getNewNodeURI();
    
    public void setNewNodeURI(URI newNodeURI);
    
    public String getReplacementStatus();
    
    public void setReplacementStatus(String replacementStatus);
    
    public String getReplacementError();
    
    public void setReplacementError(String replacementError);
}
