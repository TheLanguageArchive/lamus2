/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
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
package nl.mpi.archiving.tree;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface ArchiveNode {
    
    /**
     * 
     * @param index index of child to get
     * @return the child at the specified index
     */
    ArchiveNode getChild(int index);
    
    /**
     * 
     * @return Number of children this node has
     */
    int getChildCount();
    
    /**
     * 
     * @param child child to get index for
     * @return index of child, -1 if child does not exist
     */
    int getIndexOfChild(ArchiveNode child); 
}
