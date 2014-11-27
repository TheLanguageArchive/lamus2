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
package nl.mpi.lamus.typechecking.implementation;

import nl.mpi.lamus.typechecking.TypecheckedResults;
import nl.mpi.lamus.typechecking.TypecheckerJudgement;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @see TypecheckedResults
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusTypecheckedResults implements TypecheckedResults {

    private String checkedMimetype;
    private String analysis;
    private TypecheckerJudgement typecheckerJudgement;
    
    public LamusTypecheckedResults(String mimetype, String analysis, TypecheckerJudgement judgement) {
        this.checkedMimetype = mimetype;
        this.analysis = analysis;
        this.typecheckerJudgement = judgement;
    }
    
    /**
     * @see TypecheckedResults#getCheckedMimetype()
     */
    @Override
    public String getCheckedMimetype() {
        return checkedMimetype;
    }
    
    @Override
    public String getCompleteAnalysis() {
        return analysis;
    }
    
    /**
     * @see TypecheckedResults#getAnalysis()
     */
    @Override
    public String getAnalysis() {
        if (analysis.startsWith("true ")) {
            return analysis.substring(5);
        }
        if (analysis.startsWith("false ")) {
            return analysis.substring(6);
        }
        return analysis;
    }

    /**
     * @see TypecheckedResults#getTypecheckerJudgement()
     */
    @Override
    public TypecheckerJudgement getTypecheckerJudgement() {
        return typecheckerJudgement;
    }
    
    /**
     * @see TypecheckedResults#isTypeUnspecified()
     */
    @Override
    public boolean isTypeUnspecified() {
        if(checkedMimetype.startsWith("Un")) { //TODO use a better way to identify these cases
            return true;
        }
        return false;
    }
    
    
    @Override
    public int hashCode() {
        
        HashCodeBuilder hashCodeB = new HashCodeBuilder()
                .append(this.checkedMimetype)
                .append(this.analysis)
                .append(this.typecheckerJudgement);
        
        return hashCodeB.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        
        if(this == obj) {
            return true;
        }
        if(!(obj instanceof LamusTypecheckedResults)) {
            return false;
        }
        LamusTypecheckedResults other = (LamusTypecheckedResults) obj;
        
        
        EqualsBuilder equalsB = new EqualsBuilder()
                .append(this.checkedMimetype, other.getCheckedMimetype())
                .append(this.analysis, other.getCompleteAnalysis())
                .append(this.typecheckerJudgement, other.getTypecheckerJudgement());
        
        return equalsB.isEquals();
    }
    
    @Override
    public String toString() {
        
        String stringResult = "Checked mimetype: " + this.checkedMimetype + ", Analysis: " + this.analysis +
                ", Typechecker Judgement: " + this.typecheckerJudgement;
        
        return stringResult;
    }
}
