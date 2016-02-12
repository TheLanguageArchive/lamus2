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
import nl.mpi.lamus.workspace.exporting.NodeExporterFactory;
import nl.mpi.lamus.workspace.exporting.UnlinkedAndDeletedNodesExportHandler;
import nl.mpi.lamus.workspace.exporting.WorkspaceCorpusStructureExporter;
import nl.mpi.lamus.workspace.exporting.WorkspaceExportRunnerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceExportRunnerFactory
 * @author guisil
 */
@Component
public class LamusWorkspaceExportRunnerFactory implements WorkspaceExportRunnerFactory {

    private final WorkspaceDao workspaceDao;
    private final NodeExporterFactory nodeExporterFactory;
    private final UnlinkedAndDeletedNodesExportHandler unlinkedAndDeletedNodesExportHandler;
    private final CorpusStructureServiceBridge corpusStructureServiceBridge;
    private final WorkspaceCorpusStructureExporter workspaceCorpusStructureExporter;
    
    @Autowired
    public LamusWorkspaceExportRunnerFactory(WorkspaceDao wsDao,
            NodeExporterFactory nExporterFactory,
            UnlinkedAndDeletedNodesExportHandler udNodesExportHandler,
            CorpusStructureServiceBridge csServiceBridge,
            WorkspaceCorpusStructureExporter wsCsExporter) {
        
        this.workspaceDao = wsDao;
        this.nodeExporterFactory = nExporterFactory;
        this.unlinkedAndDeletedNodesExportHandler = udNodesExportHandler;
        this.corpusStructureServiceBridge = csServiceBridge;
        this.workspaceCorpusStructureExporter = wsCsExporter;
    }
    
    /**
     * @see WorkspaceExportRunnerFactory#getNewExportRunner()
     */
    @Override
    public WorkspaceExportRunner getNewExportRunner() {
        return new WorkspaceExportRunner(workspaceDao, nodeExporterFactory,
                unlinkedAndDeletedNodesExportHandler, corpusStructureServiceBridge,
                workspaceCorpusStructureExporter);
    }
    
}
