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
package nl.mpi.lamus.web.spring.production;

import nl.mpi.archiving.corpusstructure.core.UnknownNodeException;
import nl.mpi.archiving.corpusstructure.core.database.dao.ArchiveDao;
import nl.mpi.archiving.corpusstructure.core.database.dao.ArchiveObjectsDao;
import nl.mpi.archiving.corpusstructure.core.database.dao.CorpusStructureDao;
import nl.mpi.archiving.corpusstructure.core.database.dao.impl.ArchiveDaoImpl;
import nl.mpi.archiving.corpusstructure.core.database.dao.impl.ArchiveObjectsDaoImpl;
import nl.mpi.archiving.corpusstructure.core.database.dao.impl.CorpusStructureDaoImpl;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProviderFactory;
import nl.mpi.archiving.tree.corpusstructure.CorpusStructureTreeModelProvider;
import nl.mpi.lamus.cmdi.providers.ProductionCorpusStructureProviderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author guisil
 */
@Configuration
@Profile(value = { "production", "cmdi-production", "cmdi-production-providers", "cmdi-adapter-csdb" })
public class ProvidersBeans {
    
//    @Autowired
//    private CorpusStructureProviderFactory csdbFactory;
//    
//    @Bean
//    @Scope("prototype")
//    @Qualifier("createWorkspaceTreeProvider")
//    public CorpusStructureTreeModelProvider createWorkspaceTreeProvider() throws UnknownNodeException {
//        return new CorpusStructureTreeModelProvider(csdbFactory);
//    }
    
    
    @Bean
    public CorpusStructureProviderFactory corpusStructureProviderFactory() {
        return new ProductionCorpusStructureProviderFactory(archiveObjectsDao(), archiveDao(), corpusStructureDao());
    }
    
    @Bean
    @Scope("prototype")
    @Qualifier("createWorkspaceTreeProvider")
    public CorpusStructureTreeModelProvider createWorkspaceTreeProvider() throws UnknownNodeException {
//        return new CorpusStructureTreeModelProvider(corpusStructureDbFactory());
        return new CorpusStructureTreeModelProvider(corpusStructureProviderFactory().createCorpusStructureProvider());
    }
    
//    @Bean
//    public CorpusStructureProviderFactory corpusStructureDbFactory() {
//        return new CorpusStructureAPIProviderFactory("java:comp/env/jdbc/CSDB");
//    }
    
    
    @Bean
    public ArchiveDao archiveDao() {
        return new ArchiveDaoImpl();
    }
    
    @Bean
    public ArchiveObjectsDao archiveObjectsDao() {
        return new ArchiveObjectsDaoImpl();
    }
    
    @Bean
    public CorpusStructureDao corpusStructureDao() {
        return new CorpusStructureDaoImpl();
    }
}
