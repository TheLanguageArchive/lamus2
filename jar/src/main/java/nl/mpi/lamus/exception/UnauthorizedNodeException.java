/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
public class UnauthorizedNodeException extends NodeAccessException {
    
    private static String messagePart1 = "Node with URI '";
    private static String messagePart2 = "' is not writeable by user ";
    
    private String userID;
    
    public UnauthorizedNodeException(URI nodeURI, String userID) {
        super(getConstructedMessage(nodeURI.toString(), userID), nodeURI, null);
        this.userID = userID;
    }
    
    public UnauthorizedNodeException(String message, URI nodeURI, String userID) {
        super(message, nodeURI, null);
        this.userID = userID;
        
    }
    
    private static String getConstructedMessage(String nodeUriStr, String userID) {
        return new StringBuilder().append(messagePart1).append(nodeUriStr).append(messagePart2).append(userID).toString();
    }
    
    public String getUserID() {
        return this.userID;
    }
}
