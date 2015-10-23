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
package nl.mpi.lamus.schedulers;

import nl.mpi.lamus.exception.CrawlerStateRetrievalException;

/**
 * Provides a way to trigger the workspace check and finalisation.
 * @author guisil
 */
public interface UnfinishedWorkspacesChecker {
    
    /**
     * Checks if there are workspaces which were submitted already
     * but not finalised yet. If everything is ready, it triggers
     * the finalisation of the workspace.
     */
    public void checkAndFinaliseWorkspaces() throws CrawlerStateRetrievalException;
}
