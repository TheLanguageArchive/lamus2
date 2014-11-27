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
import nl.mpi.lamus.typechecking.FileTypeHandler;
import nl.mpi.lamus.typechecking.TypecheckHandler;
import nl.mpi.lamus.typechecking.TypecheckedResults;
import nl.mpi.lamus.typechecking.TypecheckerJudgement;
import nl.mpi.lamus.exception.TypeCheckerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see FileTypeHandler
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusFileTypeHandler implements FileTypeHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusFileTypeHandler.class);
    
    private TypecheckHandler typecheckHandler;
    

    @Autowired
    public LamusFileTypeHandler(TypecheckHandler typecheckHandler) {
        this.typecheckHandler = typecheckHandler;
    }

    
    /**
     * @see FileTypeHandler#checkType(java.io.InputStream, java.lang.String, java.lang.String)
     */
    @Override
    public TypecheckedResults checkType(InputStream resourceInputStream, String resourceFilename) throws TypeCheckerException {
        
        String checkedMimetype;
        String checkedAnalysis;
        String typecheckStringResult = null;
        
        try {

            typecheckStringResult = typecheckHandler.typecheck(resourceInputStream, resourceFilename.toLowerCase());
            checkedMimetype = typecheckHandler.getTypecheckMimetype(typecheckStringResult);

            if (checkedMimetype == null) {
                logger.warn("LamusFileTypeHandler.checkType: No archivable file type for FILE: " + resourceFilename + "; result: " +
                        typecheckStringResult);
                checkedMimetype = "Unknown";
                checkedAnalysis = typecheckStringResult;
            } else {
                checkedAnalysis = "okay (content, name)";
            }
        } catch (IOException ioe) {
            String errorMessage = "LamusFileTypeHandler.checkType: File type checker could not access file: " + resourceFilename;
            logger.warn(errorMessage);
            checkedMimetype = "Unknown";
            checkedAnalysis = "Read error for " + resourceFilename + " - " + ioe.getMessage();
            TypecheckedResults results = new LamusTypecheckedResults(checkedMimetype, checkedAnalysis, null);
            throw new TypeCheckerException(results, errorMessage, ioe);
        }
        
        TypecheckedResults resultsToReturn = new LamusTypecheckedResults(checkedMimetype, checkedAnalysis, typecheckHandler.getTypecheckJudgement(typecheckStringResult));
        
        return resultsToReturn;
    }

    /**
     * @see FileTypeHandler#isCheckedResourceArchivable(nl.mpi.lamus.typechecking.TypecheckedResults, nl.mpi.lamus.typechecking.TypecheckerJudgement, java.lang.StringBuilder)
     */
    @Override
    public boolean isCheckedResourceArchivable(TypecheckedResults typecheckedResults, TypecheckerJudgement acceptableJudgementForCorpus, StringBuilder message) {
        
        boolean isArchivable = false;
        TypecheckerJudgement actualJudgement = typecheckedResults.getTypecheckerJudgement();
        
        if(actualJudgement.compareTo(acceptableJudgementForCorpus) >= 0) {
            isArchivable = true;
            message.append("Resource is archivable. Judgement '").
                    append(actualJudgement).
                    append("' acceptable.");
        } else {
            message.append("Resource is not archivable. Judgement '").
                    append(actualJudgement).
                    append("' not acceptable - minimum is '").
                    append(acceptableJudgementForCorpus).
                    append("'.");
        }
        return isArchivable;
    }
}
