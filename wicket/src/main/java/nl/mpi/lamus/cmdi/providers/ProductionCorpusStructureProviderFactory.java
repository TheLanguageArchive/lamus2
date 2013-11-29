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
package nl.mpi.lamus.cmdi.providers;

import java.io.Serializable;
import nl.mpi.archiving.corpusstructure.core.database.dao.ArchiveDao;
import nl.mpi.archiving.corpusstructure.core.database.dao.ArchiveObjectsDao;
import nl.mpi.archiving.corpusstructure.core.database.dao.CorpusStructureDao;
import nl.mpi.archiving.corpusstructure.provider.AccessInfoProvider;
import nl.mpi.archiving.corpusstructure.provider.AccessInfoProviderFactory;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProviderFactory;
import nl.mpi.archiving.corpusstructure.provider.db.AccessInfoProviderImpl;
import nl.mpi.archiving.corpusstructure.provider.db.CorpusStructureProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * COPY FROM THE METADATA BROWSER FOR TESTING PURPOSES...
 */
@Transactional
public class ProductionCorpusStructureProviderFactory implements CorpusStructureProviderFactory, AccessInfoProviderFactory, Serializable {

    private final static Logger logger = LoggerFactory.getLogger(ProductionCorpusStructureProviderFactory.class);
    private final ArchiveObjectsDao aoDao;
    private final ArchiveDao archiveDao;
    private final CorpusStructureDao csDao;

    @Autowired
    public ProductionCorpusStructureProviderFactory(ArchiveObjectsDao aoDao, ArchiveDao archiveDao, CorpusStructureDao csDao) {
	logger.debug("Creating cs provider factory with {}, {}, {}", aoDao, archiveDao, csDao);
	this.aoDao = aoDao;
	this.archiveDao = archiveDao;
	this.csDao = csDao;
    }

    @Override
    public CorpusStructureProvider createCorpusStructureProvider() {
	logger.debug("Constructing new CorpusStructureProviderImpl");
	return new CorpusStructureProviderImpl(archiveDao, aoDao, csDao);
    }

    @Override
    public AccessInfoProvider createAccessInfoProvider() {
	logger.debug("Constructing new CorpusStructureProviderImpl");
	return new AccessInfoProviderImpl(aoDao);
    }
}