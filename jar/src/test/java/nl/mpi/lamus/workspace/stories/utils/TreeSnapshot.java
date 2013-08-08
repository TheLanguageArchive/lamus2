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
package nl.mpi.lamus.workspace.stories.utils;

import java.util.List;

/**
 *
 * @author guisil
 */
public class TreeSnapshot {
    
    private NodeSnapshot topNodeSnapshot;
    private List<NodeSnapshot> otherNodesSnapshots;
    
    public TreeSnapshot(NodeSnapshot topNodeSnapshot, List<NodeSnapshot> otherNodesSnapshot) {
        this.topNodeSnapshot = topNodeSnapshot;
        this.otherNodesSnapshots = otherNodesSnapshot;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.topNodeSnapshot != null ? this.topNodeSnapshot.hashCode() : 0);
        hash = 53 * hash + (this.otherNodesSnapshots != null ? this.otherNodesSnapshots.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TreeSnapshot other = (TreeSnapshot) obj;
        if (this.topNodeSnapshot != other.topNodeSnapshot && (this.topNodeSnapshot == null || !this.topNodeSnapshot.equals(other.topNodeSnapshot))) {
            return false;
        }
        if (this.otherNodesSnapshots != other.otherNodesSnapshots && (this.otherNodesSnapshots == null || !this.otherNodesSnapshots.equals(other.otherNodesSnapshots))) {
            return false;
        }
        return true;
    }
    
    
}
