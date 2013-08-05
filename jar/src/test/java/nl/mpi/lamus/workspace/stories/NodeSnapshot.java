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
package nl.mpi.lamus.workspace.stories;

import java.sql.Timestamp;

/**
 *
 * @author guisil
 */
public class NodeSnapshot {
    
    private String id;
    private Timestamp timestamp;
    private String checksum;
    
    public NodeSnapshot(String id, Timestamp timestamp, String checksum) {
        this.id = id;
        this.timestamp = timestamp;
        this.checksum = checksum;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 97 * hash + (this.timestamp != null ? this.timestamp.hashCode() : 0);
        hash = 97 * hash + (this.checksum != null ? this.checksum.hashCode() : 0);
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
        final NodeSnapshot other = (NodeSnapshot) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        if (this.timestamp != other.timestamp && (this.timestamp == null || !this.timestamp.equals(other.timestamp))) {
            return false;
        }
        if ((this.checksum == null) ? (other.checksum != null) : !this.checksum.equals(other.checksum)) {
            return false;
        }
        return true;
    }
}
