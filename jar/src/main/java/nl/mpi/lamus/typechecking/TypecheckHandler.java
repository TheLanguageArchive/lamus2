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

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface TypecheckHandler {
    
    /**
     * 
     * @param iStream
     * @param filename
     * @throws IOException 
     */
    public void typecheck(InputStream iStream, String filename) throws IOException;
    
    /**
     * 
     * @return 
     */
    public boolean isFileArchivable();
    
    /**
     * 
     * @return 
     */
    public String getTypecheckResult();
    
    /**
     * 
     * @return 
     */
    public TypecheckerJudgement getTypecheckJudgement();
    
    /**
     * 
     * @return 
     */
    public String getTypecheckMimetype();
}
