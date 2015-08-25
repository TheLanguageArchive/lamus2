/*
 * Copyright (C) 2015 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.workspace.upload.implementation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import nl.mpi.lamus.workspace.importing.implementation.ImportProblem;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *
 * @author guisil
 */
public class ZipUploadResult {
    
    private final Collection<File> successfulUploads;
    private final Collection<ImportProblem> failedUploads;
    
    
    public ZipUploadResult() {
        successfulUploads = new ArrayList<>();
        failedUploads = new ArrayList<>();
    }
    
    
    public Collection<File> getSuccessfulUploads() {
        return successfulUploads;
    }
    
    public void addSuccessfulUpload(File file) {
        successfulUploads.add(file);
    }
    
    public Collection<ImportProblem> getFailedUploads() {
        return failedUploads;
    }
    
    public void addFailedUpload(ImportProblem problem) {
        failedUploads.add(problem);
    }
    
    
    @Override
    public int hashCode() {
        
        HashCodeBuilder hashCodeB = new HashCodeBuilder()
                .append(this.successfulUploads)
                .append(this.failedUploads);
        
        return hashCodeB.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        
        if(this == obj) {
            return true;
        }
        if(!(obj instanceof ZipUploadResult)) {
            return false;
        }
        ZipUploadResult other = (ZipUploadResult) obj;
        
        
        EqualsBuilder equalsB = new EqualsBuilder()
                .append(this.successfulUploads, other.getSuccessfulUploads())
                .append(this.failedUploads, other.getFailedUploads());
        
        return equalsB.isEquals();
    }
}
