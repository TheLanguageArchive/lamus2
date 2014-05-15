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
package nl.mpi.lamus.exporting;

import java.io.File;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;

/**
 *
 * @author guisil
 */
public interface AddedNodeExportHelper {
    
    
    //TODO METHODS WHERE THE COMMON CODE FROM AddedNodeExporter SHOULD BE MOVED
        // SO IT CAN BE SHARED BETWEEN THAT EXPORTER AND THE "ADD TOP NODE" FUNCTIONALITY
    
    //TODO IF IT MAKES SENSE, SEVERAL METHODS CAN BE COMBINED
    
    
    
    public File retrieveAndUpdateNewArchivePath();
    
    public MetadataDocument retrieveMetadataDocument();
    
    public void assignAndUpdateNewHandle();
    
    public void updateSelfHandle();
    
    public void moveFileIntoArchive();
    
    public ReferencingMetadataDocument retrieveReferencingMetadataDocument();
    
    public void updateReferenceInParent();
    
    
    //TODO SOMETHING ELSE?
    
}
