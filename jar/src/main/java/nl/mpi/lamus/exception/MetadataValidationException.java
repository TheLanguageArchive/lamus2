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
package nl.mpi.lamus.exception;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import nl.mpi.lamus.metadata.validation.implementation.MetadataValidationIssue;

/**
 *
 * @author guisil
 */
public class MetadataValidationException extends WorkspaceException {
    
    private final Collection<MetadataValidationIssue> validationIssues;
 
    public MetadataValidationException(String message, int workspaceID, Throwable cause) {
        super(message, workspaceID, cause);
        validationIssues = new ArrayList<>();
    }
    
    
    public Collection<MetadataValidationIssue> getValidationIssues() {
        return Collections.unmodifiableCollection(validationIssues);
    }
    
    public void addValidationIssue(MetadataValidationIssue issue) {
        validationIssues.add(issue);
    }
    
    public void addValidationIssues(Collection<MetadataValidationIssue> issues) {
        validationIssues.addAll(issues);
    }
}
