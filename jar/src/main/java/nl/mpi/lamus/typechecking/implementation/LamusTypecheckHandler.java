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
import java.net.URL;
import nl.mpi.bcarchive.typecheck.DeepFileType;
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
    
    private final FileType typechecker;
    // Deep typechecking is the default
    private final DeepFileType deepTypechecker;
    
    @Autowired
    public LamusTypecheckHandler(FileType typechecker, DeepFileType deepTypechecker) {
        this.typechecker = typechecker;
        this.deepTypechecker = deepTypechecker;
    }

    
    /**
     * @see TypecheckHandler#typecheck(java.io.InputStream, java.lang.String)
     */
    @Override
    public String typecheck(InputStream iStream, String filename) throws IOException {
        return typechecker.checkStream(iStream, filename.toLowerCase());
    }

    /**
     * @see TypecheckHandler#deepTypecheck(java.net.URL, java.lang.String)
     */
    @Override
    public String deepTypecheck(URL fileUrl, String filename) throws IOException {
        return deepTypechecker.checkURL(fileUrl, filename);
    }
    
    /**
     * @see TypecheckHandler#isFileArchivable(java.lang.String)
     */
    @Override
    public boolean isFileArchivable(String typecheckResult) {
        return FileType.resultToBoolean(typecheckResult);
    }
    
    /**
     * @see TypecheckHandler#getTypecheckJudgement(java.lang.String)
     */
    @Override
    public TypecheckerJudgement getTypecheckJudgement(String typecheckResult) {
        String typecheckJudgementMessage = FileType.resultToJudgement(typecheckResult);
        
        if(typecheckJudgementMessage.startsWith("GOOD")) {
            return TypecheckerJudgement.ARCHIVABLE_LONGTERM;
        } else if(typecheckJudgementMessage.startsWith("OKAYFORAWHILE")) {
            return TypecheckerJudgement.ARCHIVABLE_SHORTTERM;
        } else {
            return TypecheckerJudgement.UNARCHIVABLE;
        }
    }
    
    /**
     * @see TypecheckHandler#getTypecheckMimetype()
     */
    @Override
    public String getTypecheckMimetype(String typecheckResult) {
        return FileType.resultToMPIType(typecheckResult);
    }
}
