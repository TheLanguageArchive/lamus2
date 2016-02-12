/*
 * Copyright (C) 2016 Max Planck Institute for Psycholinguistics
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

import nl.mpi.lamus.exception.CrawlerInvocationException;
import nl.mpi.lamus.workspace.model.Workspace;

/**
 * Separating the invocation of the crawler service, so that it can be reused.
 * @author guisil
 */
public interface WorkspaceCorpusStructureExporter {
   
    /**
     * Triggers the crawler service for the branch changed by the given workspace.
     * @param workspace Workspace for which to trigger the crawler.
     */
    public void triggerWorkspaceCrawl(Workspace workspace)
            throws CrawlerInvocationException;
}
