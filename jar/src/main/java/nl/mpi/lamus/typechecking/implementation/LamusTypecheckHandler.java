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
package nl.mpi.lamus.typechecking.implementation;

import java.io.IOException;
import java.io.InputStream;
import nl.mpi.bcarchive.typecheck.FileType;
import nl.mpi.lamus.typechecking.TypecheckHandler;
import nl.mpi.lamus.typechecking.TypecheckerJudgement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusTypecheckHandler implements TypecheckHandler {
    
    private FileType typechecker;
    private String typecheckResult;
    private String typecheckJudgementMessage;
    
    @Autowired
    public LamusTypecheckHandler(FileType typechecker) {
        this.typechecker = typechecker;
    }

    /**
     * Passes a stream to the typechecker and fills in the result.
     * 
     * @param iStream input stream of the file to check
     * @param filename name of the file to check
     * @throws IOException 
     */
    public void typecheck(InputStream iStream, String filename) throws IOException {
        typecheckResult = typechecker.checkStream( iStream, filename.toLowerCase() );
    }
    
    /**
     * Checks if, given the typechecker result, the file is archivable.
     * 
     * @return true if the file is considered archivable
     */
    public boolean isFileArchivable() {
        return FileType.resultToBoolean(typecheckResult);
    }
    
    /**
     * @return the most recent type check result
     */
    public String getTypecheckResult() {
        return this.typecheckResult;
    }
    
    /**
     * Gets the message returned by the typechecker for the most recent result
     * and converts it into the corresponding TypecheckerJudgement value.
     * 
     * @return the judgement of the typechecker for the most recent result
     */
    public TypecheckerJudgement getTypecheckJudgement() {
        typecheckJudgementMessage = FileType.resultToJudgement(typecheckResult);
        
        if(typecheckJudgementMessage.startsWith("GOOD")) {
            return TypecheckerJudgement.ARCHIVABLE_LONGTERM;
        } else if(typecheckJudgementMessage.startsWith("OKAYFORAWHILE")) {
            return TypecheckerJudgement.ARCHIVABLE_SHORTTERM;
        } else {
            return TypecheckerJudgement.UNARCHIVABLE;
        }
    }
    
    /**
     * @return the file format corresponding to the most recent result
     */
    public String getTypecheckMimetype() {
        return FileType.resultToMPIType(typecheckResult);
    }
}
