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
package nl.mpi.lamus.web.spring.adapter;

import nl.mpi.archiving.corpusstructure.adapter.ArchiveObjectsDBImplFactory;
import nl.mpi.archiving.corpusstructure.adapter.CorpusStructureAPIProviderFactory;
import nl.mpi.archiving.corpusstructure.adapter.CorpusStructureDBImplFactory;
import nl.mpi.archiving.corpusstructure.adapter.proxy.ArchiveObjectsDBFactory;
import nl.mpi.archiving.corpusstructure.adapter.proxy.ArchiveObjectsDBProxy;
import nl.mpi.archiving.corpusstructure.adapter.proxy.CorpusStructureDBFactory;
import nl.mpi.archiving.corpusstructure.adapter.proxy.CorpusStructureDBProxy;
import nl.mpi.archiving.corpusstructure.core.service.FilePathTranslator;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.AccessInfoProvider;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.corpusstructure.CorpusStructureDB;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 *
 * @author guisil
 */
@Configuration
@Profile("cmdi-adapter-csdb")
public class CorpusstructureBeans {
    
    @Bean
    public CorpusStructureDBFactory csdbImplFactory() {
        return new CorpusStructureDBImplFactory("java:comp/env/jdbc/CSDB3");
    }
    
    @Bean(name = "adapterCSDB")
    @Qualifier("adapterCSDB")
    public CorpusStructureDB csdbProxy() {
        return new CorpusStructureDBProxy();
    }
    
    @Bean
    public ArchiveObjectsDBFactory aoImplFactory() {
        return new ArchiveObjectsDBImplFactory("java:comp/env/jdbc/CSDB3");
    }
    
    @Bean(name = "adapterAO")
    @Qualifier("adapterAO")
    public ArchiveObjectsDB aoProxy() {
        return new ArchiveObjectsDBProxy();
    }
    
    @Bean
    public CorpusStructureAPIProviderFactory corpusStructureProviderFactory() {
        return new CorpusStructureAPIProviderFactory(csdbProxy(), aoProxy(), "https://lux16.mpi.nl/ds/TranslationService");
    }
    
    @Bean
    public CorpusStructureProvider corpusStructureProvider() {
        return corpusStructureProviderFactory().createCorpusStructureProvider();
    }
    
    @Bean
    public AccessInfoProvider accessInfoProvider() {
        return corpusStructureProviderFactory().createAccessInfoProvider();
    }
    
    @Bean
    public NodeResolver nodeResolver() {
        return corpusStructureProviderFactory().createNodeResolver();
    }
    
    @Bean
    public FilePathTranslator translator() {
        return corpusStructureProviderFactory().createFilePathTranslator();
    }
}
