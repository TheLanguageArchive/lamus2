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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Interface that provides a bridge to interact with the typechecker.
 * @author guisil
 */
public interface TypecheckHandler {
    
    /**
     * Passes a stream to the typechecker and fills in the result.
     * @param iStream input stream of the file to check
     * @param filename name of the file to check
     * @return typecheck result
     * @throws IOException if some issue happens with the typechecker
     */
    public String typecheck(InputStream iStream, String filename) throws IOException;
    
    /**
     * Passes a stream to the typechecker and fills in the result of a deep typecheck.
     * 
     * @param fileUrl actual location of the file to check, 
     * should be file: URL for faster check, often is a workspace URL 
     * @param filename
     * @return intended name of the file to check, 
     * has to have right file name extension, often != fileUrl 
     */
    public String deepTypecheck(URL fileUrl, String filename) throws IOException;
    
    /**
     * Checks if, given the typechecker result, the file is archivable.
     * @param typecheckResult result from which to retrieve the mimetype
     * @return true if the file is considered archivable
     */
    public boolean isFileArchivable(String typecheckResult);
    
    /**
     * Gets the message returned by the typechecker for the most recent result
     * and converts it into the corresponding TypecheckerJudgement value.
     * @param typecheckResult result from which to retrieve the mimetype
     * @return the judgement of the typechecker for the most recent result
     */
    public TypecheckerJudgement getTypecheckJudgement(String typecheckResult);
    
    /**
     * Retrieves the mimetype corresponding to the given typecheck result.
     * @param typecheckResult result from which to retrieve the mimetype
     * @return the file format corresponding to the most recent result
     */
    public String getTypecheckMimetype(String typecheckResult);
}
