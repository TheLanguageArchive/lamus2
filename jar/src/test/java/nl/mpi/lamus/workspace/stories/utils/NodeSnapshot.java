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

import java.net.URI;
import java.util.Date;

/**
 *
 * @author guisil
 */
public class NodeSnapshot {
    
    private URI uri;
    private Date date;
    private String checksum;
    
    public NodeSnapshot(URI uri, Date date, String checksum) {
        this.uri = uri;
        this.date = date;
        this.checksum = checksum;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.uri != null ? this.uri.hashCode() : 0);
        hash = 97 * hash + (this.date != null ? this.date.hashCode() : 0);
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
        if ((this.uri == null) ? (other.uri != null) : !this.uri.equals(other.uri)) {
            return false;
        }
        if (this.date != other.date && (this.date == null || !this.date.equals(other.date))) {
            return false;
        }
        if ((this.checksum == null) ? (other.checksum != null) : !this.checksum.equals(other.checksum)) {
            return false;
        }
        return true;
    }
}
