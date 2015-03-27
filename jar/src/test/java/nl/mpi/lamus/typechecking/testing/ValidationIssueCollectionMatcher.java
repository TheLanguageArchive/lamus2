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
package nl.mpi.lamus.typechecking.testing;

import java.util.Collection;
import java.util.Iterator;
import nl.mpi.lamus.typechecking.implementation.MetadataValidationIssue;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 *
 * @author guisil
 */
public class ValidationIssueCollectionMatcher extends TypeSafeMatcher<Collection<MetadataValidationIssue>> {
    
    private Collection<MetadataValidationIssue> collection;

    public ValidationIssueCollectionMatcher(Collection<MetadataValidationIssue> collection) {
        this.collection = collection;
    }

    @Override
    public boolean matchesSafely(Collection<MetadataValidationIssue> c) {
 
        if(c.size() != collection.size()) {
            return Boolean.FALSE;
        }
        
        Iterator<MetadataValidationIssue> testIterator = c.iterator();
        Iterator<MetadataValidationIssue> thisIterator = collection.iterator();
        
        while(testIterator.hasNext()) {
            if(!testIterator.next().equals(thisIterator.next())) {
                return Boolean.FALSE;
            }
        }
        
        return Boolean.TRUE;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("collection ").appendValue(collection);
    }
}
