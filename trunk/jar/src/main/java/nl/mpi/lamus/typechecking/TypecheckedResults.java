/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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

/**
 * Container for the results of the typechecker regarding a resource.
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface TypecheckedResults {
    
    /**
     * @return mimetype of the checked resource
     */
    public String getCheckedMimetype();
    
    /**
     * @return String containing the analysis of the checked resource,
     * including the beginning of the string (true or false)
     */
    public String getCompleteAnalysis();
    
    /**
     * @return String containing the analysis of the checked resource
     */
    public String getAnalysis();
    
    /**
     * @return judgement of the checked resource
     */
    public TypecheckerJudgement getTypecheckerJudgement();
    
    /**
     * @return true if the type of the checked resource is unspecified
     */
    public boolean isTypeUnspecified();
}
