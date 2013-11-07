package nl.mpi.lamus.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import nl.mpi.archiving.corpusstructure.core.AccessInfo;
import nl.mpi.archiving.corpusstructure.core.AccessLevel;
import nl.mpi.archiving.corpusstructure.core.ArchiveUser;

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
/**
 * Mock implementation of the AccessInfo interface
 *
 * @author guisil
 */
public class MockAccessInfo implements AccessInfo {

    private AccessLevel accessLevel = AccessLevel.ACCESS_LEVEL_UNKNOWN;
    private Collection<ArchiveUser> readRights = Collections.emptyList();
    private Collection<ArchiveUser> writeRights = Collections.emptyList();
    private List<String> readUsers = new ArrayList<String>();
    private List<String> writeUsers = new ArrayList<String>();

    public MockAccessInfo() {
    }

    @Override
    public AccessLevel getAccessLevel() {
	return this.accessLevel;
    }

    @Override
    public Collection<ArchiveUser> getReadRights() {
	return this.readRights;
    }

    @Override
    public Collection<ArchiveUser> getWriteRights() {
	return writeRights;
    }

    @Override
    public boolean hasReadAccess(String username) {
	return readUsers.contains(username);
    }

    @Override
    public boolean hasWriteAccess(String username) {
	return writeUsers.contains(username);
    }

    public void setAccessLevel(AccessLevel accessLevel) {
	this.accessLevel = accessLevel;
    }

    public void setReadRights(Collection<ArchiveUser> privs) {
        this.readRights = privs;
    }

    public void setReadRule(String privs) {
	// do nothing
    }

    public void setReadUsers(List<String> users) {
	this.readUsers = users;
	readRights = new ArrayList<ArchiveUser>(users.size());
	for (final String user : users) {
	    readRights.add(new ArchiveUser() {
		@Override
		public String getUid() {
		    return user;
		}
	    });
	}
    }
    
    public void setWriteRights(Collection<ArchiveUser> privs) {
        this.writeRights = privs;
    }

    public void setWriteUsers(List<String> users) {
	this.writeUsers = users;
	writeRights = new ArrayList<ArchiveUser>(users.size());
	for (final String user : users) {
	    writeRights.add(new ArchiveUser() {
		@Override
		public String getUid() {
		    return user;
		}
	    });
	}
    }
}
