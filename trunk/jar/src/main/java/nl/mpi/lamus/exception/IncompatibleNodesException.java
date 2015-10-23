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
package nl.mpi.lamus.exception;

/**
 *
 * @author guisil
 */
public class IncompatibleNodesException extends WorkspaceException {
    
    private final int oldNodeID;
    private final int newNodeID;
    
    public IncompatibleNodesException(String message, int workspaceID, int oldNodeID, int newNodeID, Throwable cause) {
        super(message, workspaceID, cause);
        this.oldNodeID = oldNodeID;
        this.newNodeID = newNodeID;
    }
    
    
    public int getOldNodeID() {
        return oldNodeID;
    }
    
    public int getNewNodeID() {
        return newNodeID;
    }
}
