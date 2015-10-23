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
package nl.mpi.lamus.workspace.exporting;

import nl.mpi.lamus.exception.CrawlerStateRetrievalException;

/**
 * Interface used to check if any of the workspaces recently submitted
 * have had their associated crawler finished. If so, the status of
 * the workspace will be set accordingly and an email will be sent
 * to its creator.
 * 
 * @author guisil
 */
public interface WorkspaceCrawlerChecker {

    /**
     * Checks if there are submitted workspaces and finalises
     * the ones for which the crawler has finished.
     */
    public void checkCrawlersForSubmittedWorkspaces() throws CrawlerStateRetrievalException;
}
