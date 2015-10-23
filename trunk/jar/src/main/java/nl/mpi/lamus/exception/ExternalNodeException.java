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
public class ExternalNodeException extends NodeAccessException {
    
    private static String messagePart1 = "Node with URI '";
    private static String messagePart2 = "' is external";
    
    public ExternalNodeException(URI nodeURI) {
        super(getConstructedMessage(nodeURI.toString()), nodeURI, null);
    }
    
    public ExternalNodeException(String message, URI nodeURI) {
        super(message, nodeURI, null);
    }
    
    private static String getConstructedMessage(String nodeUriStr) {
        return new StringBuilder().append(messagePart1).append(nodeUriStr).append(messagePart2).toString();
    }
}
