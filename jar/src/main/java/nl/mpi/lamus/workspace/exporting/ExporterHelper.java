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
package nl.mpi.lamus.workspace.exporting;

import nl.mpi.lamus.workspace.model.WorkspaceNode;

/**
 * Class containing helper methods for Exporters.
 * @author guisil
 */
public interface ExporterHelper {
    
    /**
     * Determines the path (comprised of ancestor node names) to be used
     * for a specific exporter, given the parent path.
     * @param currentNode Current WorkspaceNode
     * @param parentNode Parent WorkspaceNode
     * @param parentCorpusNamePathToClosestTopNode Name Path (to the top node) for the parent node
     * @param acceptNullPath true if receiving a null path is acceptable
     * @param exporterType Type of the exporter that called the method
     * @return Final path to be used
     */
    public String getNamePathToUseForThisExporter(
            WorkspaceNode currentNode, WorkspaceNode parentNode,
            String parentCorpusNamePathToClosestTopNode,
            boolean acceptNullPath, Class exporterType);
    
}
