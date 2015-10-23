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
package nl.mpi.lamus.typechecking.implementation;

import java.io.File;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Class representing an issue with the validation of a metadata file.
 * @author guisil
 */
public class MetadataValidationIssue {
    
    private File metadataFile;
    private String assertionTest;
    private String assertionErrorMessage;
    private MetadataValidationIssueLevel assertionErrorLevel;
    
    public MetadataValidationIssue(File file, String assertionTest, String errorMessage, String errorLevel) {
        
        this.metadataFile = file;
        this.assertionTest = assertionTest;
        this.assertionErrorMessage = errorMessage;
        
        if("error".equalsIgnoreCase(errorLevel) || "fatal".equalsIgnoreCase(errorLevel)) {
            this.assertionErrorLevel = MetadataValidationIssueLevel.ERROR;
        }
        if("warn".equalsIgnoreCase(errorLevel) || "warning".equalsIgnoreCase(errorLevel)) {
            this.assertionErrorLevel = MetadataValidationIssueLevel.WARN;
        }
        if("info".equalsIgnoreCase(errorLevel) || "information".equalsIgnoreCase(errorLevel)) {
            this.assertionErrorLevel = MetadataValidationIssueLevel.INFO;
        }
    }
    
    
    public File getMetadataFile() {
        return metadataFile;
    }
    
    public String getAssertionTest() {
        return assertionTest;
    }
    
    public String getAssertionErrorMessage() {
        return assertionErrorMessage;
    }
    
    public MetadataValidationIssueLevel getAssertionErrorLevel() {
        return assertionErrorLevel;
    }

    @Override
    public int hashCode() {
        
        HashCodeBuilder hashCodeB = new HashCodeBuilder()
                .append(this.metadataFile)
                .append(this.assertionTest)
                .append(this.assertionErrorMessage)
                .append(this.assertionErrorLevel);
        
        return hashCodeB.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        
        if(this == obj) {
            return true;
        }
        if(!(obj instanceof MetadataValidationIssue)) {
            return false;
        }
        MetadataValidationIssue other = (MetadataValidationIssue) obj;
        
        EqualsBuilder equalsB = new EqualsBuilder()
                .append(this.metadataFile, other.getMetadataFile())
                .append(this.assertionTest, other.getAssertionTest())
                .append(this.assertionErrorMessage, other.getAssertionErrorMessage())
                .append(this.assertionErrorLevel, other.getAssertionErrorLevel());
        
        return equalsB.isEquals();
    }

    @Override
    public String toString() {
        return "Validation issue for file '" + metadataFile.getName() + "' - " + assertionErrorLevel.toString() + ": " + assertionErrorMessage + ".";
    }
}
