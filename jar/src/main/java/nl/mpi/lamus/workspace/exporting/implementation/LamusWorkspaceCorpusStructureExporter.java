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
package nl.mpi.lamus.workspace.exporting.implementation;

import nl.mpi.lamus.archive.CorpusStructureServiceBridge;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.CrawlerInvocationException;
import nl.mpi.lamus.workspace.exporting.WorkspaceCorpusStructureExporter;
import nl.mpi.lamus.workspace.model.Workspace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceCorpusStructureExporter
 * @author guisil
 */
@Component
public class LamusWorkspaceCorpusStructureExporter implements WorkspaceCorpusStructureExporter {
    
    private final CorpusStructureServiceBridge corpusStructureServiceBridge;
    private final WorkspaceDao workspaceDao;
    
    @Autowired
    public LamusWorkspaceCorpusStructureExporter(
            CorpusStructureServiceBridge csServiceBridge,
            WorkspaceDao wsDao) {
        corpusStructureServiceBridge = csServiceBridge;
        workspaceDao = wsDao;
    }

    /**
     * @see WorkspaceCorpusStructureExporter#triggerWorkspaceCrawl(nl.mpi.lamus.workspace.model.Workspace)
     */
    @Override
    public void triggerWorkspaceCrawl(Workspace workspace) throws CrawlerInvocationException {
        
        String crawlerID = corpusStructureServiceBridge.callCrawler(workspace.getTopNodeArchiveURI());
        workspace.setCrawlerID(crawlerID);
        workspaceDao.updateWorkspaceCrawlerID(workspace);
    }
}
