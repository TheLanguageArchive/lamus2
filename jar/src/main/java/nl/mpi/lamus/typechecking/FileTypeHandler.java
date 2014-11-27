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

import java.io.InputStream;
import nl.mpi.lamus.exception.TypeCheckerException;

/**
 * Class used to retrieved and analyse the results from the typechecker.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface FileTypeHandler {
    
    //TODO to be based on the 'ResourceConVoc' class from the old Lamus
    //TODO WS types and link types must be adjusted for CMDI
    
    /**
     * Given a filename and its input stream, performs a typecheck
     * and retrieves the result.
     * @param resourceInputStream InputStream of the resource
     * @param resourceFilename Filename of the resource
     * @return TypecheckedResults object representing the result of the check
     */
    public TypecheckedResults checkType(InputStream resourceInputStream, String resourceFilename) throws TypeCheckerException;
    
    /**
     * Checks if the resource is archivable by comparing a given acceptable judgement
     * for the archive location where it would be archived and the previously typecheck results.
     * @param typecheckedResults Result of the typecheck to compare with the acceptable judgement
     * @param acceptableJudgementForNode Acceptable judgement for the archive location
     * @param message Message to be built according to the results of the method
     * @return true if the judgement for the given resource is within the acceptable values
     */
    public boolean isCheckedResourceArchivable(TypecheckedResults typecheckedResults, TypecheckerJudgement acceptableJudgementForNode, StringBuilder message);
}
