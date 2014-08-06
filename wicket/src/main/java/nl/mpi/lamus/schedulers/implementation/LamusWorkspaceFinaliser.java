package nl.mpi.lamus.schedulers.implementation;



import nl.mpi.lamus.exception.CrawlerStateRetrievalException;
import nl.mpi.lamus.workspace.exporting.WorkspaceCrawlerChecker;
import nl.mpi.lamus.schedulers.WorkspaceFinaliser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

/**
 * @see WorkspaceFinaliser
 * @author guisil
 */
@Component
public class LamusWorkspaceFinaliser implements WorkspaceFinaliser {

    private final WorkspaceCrawlerChecker workspaceCrawlerChecker;
    
    @Autowired
    public LamusWorkspaceFinaliser(WorkspaceCrawlerChecker wsCrawlerChecker) {
        workspaceCrawlerChecker = wsCrawlerChecker;
    }
    
    /**
     * This method is supposed to run periodically in order
     * to check and finalise workspaces as needed.
     * 
     * @see WorkspaceFinaliser#checkAndFinaliseWorkspaces()
     */
    @Override
    @Scheduled(fixedDelay = 60000)
    public void checkAndFinaliseWorkspaces() throws CrawlerStateRetrievalException {
        workspaceCrawlerChecker.checkCrawlersForSubmittedWorkspaces();
    }
    
}
