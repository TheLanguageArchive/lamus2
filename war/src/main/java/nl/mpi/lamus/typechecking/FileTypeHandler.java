/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.typechecking;

import java.net.URL;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface FileTypeHandler {
    
    //TODO to be based on the 'ResourceConVoc' class from the old Lamus
    //TODO WS types and link types must be adjusted for CMDI
    
    /**
     * Mimetype getter
     * @return the current mimetype string, for example &quot;image/png&quot;
     */
    public String getFormat();
    
    /**
     * Type getter
     * @return the current type category string, for example &quot;audio&quot;
     */
    public String getType();
    
    /**
     * Analysis summary getter
     * @return a description of the mimetype decision result, for example
     *  a String describing why the file is seen as okay or why it is not.
     */
    public String getAnalysis();
    
    /**
     * Node type getter
     * @return nodetype, one of the WSNodeType constants
     */
    public WorkspaceNodeType getNodeType();
    
    /**
     * Sets the Format (mimetype) and Type (class / category) fields for a given file.
     * Use getFormat and getType to fetch the results.
     * @param resourceURL the url of the resource which should be tested, if null,
     *   only the filename is used for a rough decision
     * @param filename the filename of the resource which should be tested
     *   If given, filename (-extension) is checked for match with mimetype as
     *   detected based on file contents. If no resourceURL is given and only
     *   a filename is given, rough mimetype detection will be done based on name.
     * @param nodetype one of the WSNodeType constants for format hinting: if
     *   nodetype is WSNodeType.RESOURCE_WR, more exact document classes are used
     *   If nodetype is WSNodeType.UNKNOWN, type is autodetected (less accurate)
     *   Nodetype hinting does not influence mimetype checks.
     * @param mimetype suggested format, if already known: mimetype or MPI type
     *   NOTE: only if NO mimetype is given, calculateCV will check the file
     *   content or (as fallback) the file name, to determine the file format.
     *   NOTE: if mimetype IS given, filename and resourceURL will be ignored!
     */
    public void calculateCV(URL resourceURL, String filename, WorkspaceNodeType nodetype, String mimetype);
    
    /**
     * store the outcome of calculateCV, or override it, as done by DataMoverIn
     * @param format a mimetype or Unknown or Unspecified
     * @param type a type such as audio or 'Primary Text'
     * @param nodetype one of the WSNodeType constants
     */
    public void setValues(String format, String type, WorkspaceNodeType nodetype);
}
