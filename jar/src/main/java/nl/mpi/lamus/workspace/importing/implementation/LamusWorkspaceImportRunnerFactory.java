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
package nl.mpi.lamus.workspace.importing.implementation;

import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.importing.OrphanNodesImportHandler;
import nl.mpi.lamus.workspace.importing.WorkspaceImportRunnerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceImportRunnerFactory
 * @author guisil
 */
@Component
public class LamusWorkspaceImportRunnerFactory implements WorkspaceImportRunnerFactory {
    
    private final WorkspaceDao workspaceDao;
    private final TopNodeImporter topNodeImporter;
    private final OrphanNodesImportHandler orphanNodesImportHandler;
    
    @Autowired
    public LamusWorkspaceImportRunnerFactory(WorkspaceDao wsDao,
            TopNodeImporter tnImporter, OrphanNodesImportHandler onImporterHandler) {
        
        this.workspaceDao = wsDao;
        this.topNodeImporter = tnImporter;
        this.orphanNodesImportHandler = onImporterHandler;
    }

    /**
     * @see WorkspaceImportRunnerFactory#getNewImportRunner()
     */
    @Override
    public WorkspaceImportRunner getNewImportRunner() {
        return new WorkspaceImportRunner(workspaceDao, topNodeImporter, orphanNodesImportHandler);
    }
    
}
