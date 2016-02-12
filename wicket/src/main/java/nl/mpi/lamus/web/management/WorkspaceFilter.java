/*
 * Copyright (C) 2016 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.web.management;

import java.io.Serializable;

/**
 * Filter class to use for filtering workspaces in a data table.
 * @author guisil
 */
public class WorkspaceFilter implements Serializable {
    
    private String workspaceID;
    private String userID;
    private String topNodeURI;
    private String startDate;
    private String status;
    
    public String getWorkspaceID() {
        return workspaceID;
    }
    public void setWorkspaceID(String wsID) {
        workspaceID = wsID;
    }
    
    public String getUserID() {
        return userID;
    }
    public void setUserID(String uID) {
        userID = uID;
    }
    
    public String getTopNodeURI() {
        return topNodeURI;
    }
    public void setTopNodeURI(String uri) {
        topNodeURI = uri;
    }
    
    public String getStartDate() {
        return startDate;
    }
    public void setStartDate(String sDate) {
        startDate = sDate;
    }
    
    public String getStatus() {
        return status;
    }
    public void setStatus(String stt) {
        status = stt;
    }
}
