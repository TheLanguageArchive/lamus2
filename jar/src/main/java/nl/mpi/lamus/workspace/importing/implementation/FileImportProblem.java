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

import java.io.File;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *
 * @author guisil
 */
public class FileImportProblem extends ImportProblem {
    
    private final File problematicFile;
    
    public FileImportProblem(File problematicFile, String errorMessage, Exception exception) {
        super(errorMessage, exception);
        this.problematicFile = problematicFile;
    }
    
    public File getProblematicFile() {
        return problematicFile;
    }
    
    
    @Override
    public int hashCode() {
        
        HashCodeBuilder hashCodeB = new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(this.problematicFile);
        
        return hashCodeB.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        
        if(this == obj) {
            return true;
        }
        if(!(obj instanceof FileImportProblem)) {
            return false;
        }
        FileImportProblem other = (FileImportProblem) obj;
        
        
        EqualsBuilder equalsB = new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.problematicFile, other.getProblematicFile());
        
        return equalsB.isEquals();
    }
}
