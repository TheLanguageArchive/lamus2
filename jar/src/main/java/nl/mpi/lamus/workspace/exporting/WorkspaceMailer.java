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
package nl.mpi.lamus.workspace.exporting;

import nl.mpi.lamus.workspace.model.Workspace;

/**
 * Class that handles workspace related emails.
 * @author guisil
 */
public interface WorkspaceMailer {
    
    /**
     * Sends a message when the workspace submission is finished.
     * 
     * @param workspace Workspace about which the message is concerned
     * @param crawlerWasSuccessful true if the crawling process was successful
     * @param versioningWasSuccessful true if the version creation process was successful
     */
    public void sendWorkspaceFinalMessage(Workspace workspace, boolean crawlerWasSuccessful, boolean versioningWasSuccessful);
    
}
