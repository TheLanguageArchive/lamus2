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
package nl.mpi.lamus.exception;

import java.net.URI;

/**
 *
 * @author guisil
 */
public class ProtectedNodeException extends NodeAccessException {
    
    private static String messagePart1 = "Node with URI '";
    private static String messagePart2 = "' is protected and cannot be changed ";
    
    private int workspaceID;
    
    public ProtectedNodeException(URI nodeURI, int workspaceID) {
        super(getConstructedMessage(nodeURI.toString(), workspaceID), nodeURI, null);
        this.workspaceID = workspaceID;
    }
    
    public ProtectedNodeException(String message, URI nodeURI, int workspaceID) {
        super(message, nodeURI, null);
        this.workspaceID = workspaceID;
    }
    
    private static String getConstructedMessage(String nodeUriStr, int workspaceID) {
        StringBuilder builder = new StringBuilder().append(messagePart1).append(nodeUriStr).append(messagePart2);
        if(workspaceID > -1) {
            builder.append("[workspace ").append(workspaceID).append("]");
        }
        return builder.toString();
    }
    
    public int getWorkspaceID() {
        return this.workspaceID;
    }
}
