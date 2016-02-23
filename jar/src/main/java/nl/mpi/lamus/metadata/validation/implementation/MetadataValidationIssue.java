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
package nl.mpi.lamus.metadata.validation.implementation;

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
    private String message;
    private MetadataValidationIssueSeverity severity;
    
    public MetadataValidationIssue(File file, String message, String severity) {
        this.metadataFile = file;
        this.message = message;
        if("error".equalsIgnoreCase(severity) || "fatal".equalsIgnoreCase(severity)) {
            this.severity = MetadataValidationIssueSeverity.ERROR;
        }
        if("warn".equalsIgnoreCase(severity) || "warning".equalsIgnoreCase(severity)) {
            this.severity = MetadataValidationIssueSeverity.WARN;
        }
        if("info".equalsIgnoreCase(severity) || "information".equalsIgnoreCase(severity)) {
            this.severity = MetadataValidationIssueSeverity.INFO;
        }
    }
    
    public MetadataValidationIssue(File file, String assertionTest, String message, String severity) {
        this(file, message, severity);
        this.assertionTest = assertionTest;
    }
    
    
    public File getMetadataFile() {
        return metadataFile;
    }
    
    public String getAssertionTest() {
        return assertionTest;
    }
    
    public String getMessage() {
        return message;
    }
    
    public MetadataValidationIssueSeverity getSeverity() {
        return severity;
    }

    @Override
    public int hashCode() {
        
        HashCodeBuilder hashCodeB = new HashCodeBuilder()
                .append(this.metadataFile)
                .append(this.assertionTest)
                .append(this.message)
                .append(this.severity);
        
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
                .append(this.message, other.getMessage())
                .append(this.severity, other.getSeverity());
        
        return equalsB.isEquals();
    }

    @Override
    public String toString() {
        return "'" + metadataFile.getName() + "' - " + severity.toString() + ": " + message;
    }
}
