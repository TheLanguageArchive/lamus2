package nl.mpi.lamus.mock;


import java.util.ArrayList;
import java.util.List;
import nl.mpi.archiving.corpusstructure.core.AccessInfo;

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
 * @author guisil
 */
public class MockAccessInfo implements AccessInfo {

    private int accessLevel = -1;
    private String readRights = "NOBODY";
    private String writeRights = "NOBODY";
    private List<String> readUsers = new ArrayList<String>();
    private List<String> writeUsers = new ArrayList<String>();
    
    public MockAccessInfo() {
        
    }
    
    @Override
    public int getAccessLevel() {
        return this.accessLevel;
    }

    @Override
    public String getReadRights() {
        return this.readRights;
    }

    @Override
    public String getWriteRights() {
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

    @Override
    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }

    @Override
    public void setReadRights(String privs) {
        this.readRights = privs;
    }

    @Override
    public void setReadRule(String privs) {
        // do nothing
    }

    @Override
    public void setReadUsers(List users) {
        this.readUsers = users;
        for(String user : (List<String>)users) {
            if(!this.readRights.isEmpty()) {
                this.readRights = this.readRights.concat(",");
            }
            this.readRights = this.readRights.concat(user);
        }
    }

    @Override
    public void setWriteRights(String privs) {
        this.writeRights = privs;
    }

    @Override
    public void setWriteRule(String privs) {
        // do nothing
    }

    @Override
    public void setWriteUsers(List users) {
        this.writeUsers = users;
        for(String user : (List<String>)users) {
            if(!this.writeRights.isEmpty()) {
                this.writeRights = this.writeRights.concat(",");
            }
            this.writeRights = this.writeRights.concat(user);
        }
    }
    
}
