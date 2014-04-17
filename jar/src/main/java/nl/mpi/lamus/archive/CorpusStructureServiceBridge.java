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
package nl.mpi.lamus.archive;

import java.util.Collection;
import nl.mpi.lamus.exception.VersionCreationException;
import nl.mpi.lamus.workspace.model.WorkspaceNodeReplacement;

/**
 * Interface providing methods which interact with the
 * available corpus structure services.
 * @author guisil
 */
public interface CorpusStructureServiceBridge {
    
    /**
     * Triggers the creation of versions in the archive.
     * @param nodeReplacements Collection containing the node replacements that should be translated into version entries in the corpusstructure database
     */
    public void createVersions(Collection<WorkspaceNodeReplacement> nodeReplacements) throws VersionCreationException;
}
