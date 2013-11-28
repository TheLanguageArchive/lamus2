/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.workspace.stories.utils;

import java.io.Serializable;
import nl.mpi.archiving.corpusstructure.adapter.CorpusStructureAPIAdapter;
import nl.mpi.archiving.corpusstructure.adapter.CorpusStructureAPIProviderFactory;
import nl.mpi.archiving.corpusstructure.adapter.proxy.ArchiveObjectsDBFactory;
import nl.mpi.archiving.corpusstructure.adapter.proxy.ArchiveObjectsDBProxy;
import nl.mpi.archiving.corpusstructure.adapter.proxy.CorpusStructureDBFactory;
import nl.mpi.archiving.corpusstructure.adapter.proxy.CorpusStructureDBProxy;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProviderFactory;
import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.corpusstructure.ArchiveObjectsDBImpl;
import nl.mpi.corpusstructure.CorpusStructureDB;
import nl.mpi.corpusstructure.CorpusStructureDBImpl;

/**
 * Factory for {@link CorpusStructureProvider} and {@link NodeResolver}, to be used in the workspace stories,
 * based on {@link CorpusStructureAPIProviderFactory}
 * @author guisil
 */
public class WorkspaceStepsCorpusStructureProviderFactory implements CorpusStructureProviderFactory, Serializable {
    
    private final String dbname;
    private CorpusStructureDBProxy corpusStructureDBProxy;
    private ArchiveObjectsDBProxy archiveObjectsDBProxy;

    /**
     *
     * @param dbname Database name used to to initialize a connection with the corpus structure database. This can be a JNDI named
     * datasource string, e.g. "java:comp/env/jdbc/CSDB"
     */
    public WorkspaceStepsCorpusStructureProviderFactory(String dbname) {
	this.dbname = dbname;
	this.corpusStructureDBProxy = new CorpusStructureDBProxy(new CorpusStructureDBImplFactory());
	this.archiveObjectsDBProxy = new ArchiveObjectsDBProxy(new ArchiveObjectsDBImplFactory());
    }

    @Override
    public CorpusStructureProvider createCorpusStructureProvider() {
	return new CorpusStructureAPIAdapter(corpusStructureDBProxy, archiveObjectsDBProxy);
    }

    public NodeResolver createNodeResolver() {
	return new WorkspaceStepsNodeResolver(archiveObjectsDBProxy);
    }

    private class CorpusStructureDBImplFactory implements CorpusStructureDBFactory, Serializable {

	@Override
	public CorpusStructureDB newCSDB() {
	    return new CorpusStructureDBImpl(dbname);
	}
    }

    private class ArchiveObjectsDBImplFactory implements ArchiveObjectsDBFactory, Serializable {

	@Override
	public ArchiveObjectsDB newAO() {
	    return new ArchiveObjectsDBImpl(dbname);
	}
    }
}
