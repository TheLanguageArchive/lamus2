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
package nl.mpi.lamus.workspace.importing.implementation;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Representation of problems that can happen during workspace import.
 * 
 * @author guisil
 */
public class ImportProblem {
    
    private final String errorMessage;
    private final Exception exception;
    
    public ImportProblem(String errorMessage, Exception exception) {
        this.errorMessage = errorMessage;
        this.exception = exception;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public Exception getException() {
        return exception;
    }
    
    
    @Override
    public int hashCode() {
        
        HashCodeBuilder hashCodeB = new HashCodeBuilder()
                .append(this.errorMessage)
                .append(this.exception);
        
        return hashCodeB.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        
        if(this == obj) {
            return true;
        }
        if(!(obj instanceof ImportProblem)) {
            return false;
        }
        ImportProblem other = (ImportProblem) obj;
        
        
        EqualsBuilder equalsB = new EqualsBuilder()
                .append(this.errorMessage, other.getErrorMessage())
                .append(this.exception, other.getException());
        
        return equalsB.isEquals();
    }
}
