package nl.mpi.lamus.archive.providers;

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


import nl.mpi.archiving.corpusstructure.core.database.dao.ArchivePropertyDao;
import nl.mpi.archiving.corpusstructure.core.database.dao.ArchiveObjectDao;
import nl.mpi.archiving.corpusstructure.core.database.dao.CorpusStructureDao;
import nl.mpi.archiving.corpusstructure.provider.AccessInfoProvider;
import nl.mpi.archiving.corpusstructure.provider.AccessInfoProviderFactory;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProviderFactory;
import nl.mpi.archiving.corpusstructure.provider.db.AccessInfoProviderImpl;
import nl.mpi.archiving.corpusstructure.provider.db.CorpusStructureProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CorpusStructureProviderFactoryImpl implements CorpusStructureProviderFactory, AccessInfoProviderFactory {

    private final static Logger logger = LoggerFactory.getLogger(CorpusStructureProviderFactoryImpl.class);
    private final ArchiveObjectDao aoDao;
    private final ArchivePropertyDao archiveDao;
    private final CorpusStructureDao csDao;

    public CorpusStructureProviderFactoryImpl(ArchiveObjectDao aoDao, ArchivePropertyDao archiveDao, CorpusStructureDao csDao) {
	this.aoDao = aoDao;
	this.archiveDao = archiveDao;
	this.csDao = csDao;
    }

    @Override
    public CorpusStructureProvider createCorpusStructureProvider() {
	logger.debug("Constructing new CorpusStructureProviderImpl");
	CorpusStructureProviderImpl provider =  new CorpusStructureProviderImpl(archiveDao, aoDao, csDao);
        provider.initialize();
        return provider;
    }

    @Override
    public AccessInfoProvider createAccessInfoProvider() {
	logger.debug("Constructing new CorpusStructureProviderImpl");
	return new AccessInfoProviderImpl(aoDao);
    }
}