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
package nl.mpi.lamus.workspace.importing.implementation;

import nl.mpi.lamus.workspace.importing.FileImporter;
import nl.mpi.metadata.api.model.ResourceReference;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class ResourceFileImporter implements FileImporter<ResourceReference> {

    public void importFile(ResourceReference reference, int nodeArchiveID) {
        
        //TODO if onsite and not in orphans folder: typechecker - gettype()
        
        //TODO etc...
        
        //TODO needsProtection?
        
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
