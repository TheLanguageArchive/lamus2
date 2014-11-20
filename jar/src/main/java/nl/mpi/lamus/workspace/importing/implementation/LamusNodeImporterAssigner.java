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

import nl.mpi.lamus.workspace.importing.NodeImporter;
import nl.mpi.lamus.workspace.importing.NodeImporterAssigner;
import nl.mpi.metadata.api.model.MetadataReference;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ResourceReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see NodeImporterAssigner
 * @author guisil
 */
@Component
public class LamusNodeImporterAssigner implements NodeImporterAssigner {
    
    @Autowired
    private MetadataNodeImporter metadataNodeImporter;
    @Autowired
    private ResourceNodeImporter resourceNodeImporter;

    /**
     * @see NodeImporterAssigner#getImporterForReference(nl.mpi.metadata.api.model.Reference)
     */
    @Override
    public NodeImporter getImporterForReference(Reference reference) {
        if(reference instanceof ResourceReference) {
            return resourceNodeImporter;
        } else if(reference instanceof MetadataReference) {
            return metadataNodeImporter;
        }
        throw new IllegalArgumentException("Unexpected reference type");
    }
}
