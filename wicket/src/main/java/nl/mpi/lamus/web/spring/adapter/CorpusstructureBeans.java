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
import nl.mpi.archiving.corpusstructure.adapter.NodeUriUtils;
import nl.mpi.archiving.corpusstructure.adapter.TranslationService;
import nl.mpi.archiving.corpusstructure.adapter.VersioningAPIImplFactory;
import nl.mpi.archiving.corpusstructure.adapter.utils.FilePathTranslatorAdapter;
import nl.mpi.archiving.corpusstructure.adapter.servlet.ThreadLocalCSDBContainer;
import nl.mpi.archiving.corpusstructure.adapter.db.proxy.ArchiveObjectsDBFactory;
import nl.mpi.archiving.corpusstructure.adapter.db.proxy.ArchiveObjectsDBProxy;
import nl.mpi.archiving.corpusstructure.adapter.db.proxy.CSDBContainer;
import nl.mpi.archiving.corpusstructure.adapter.db.proxy.CorpusStructureDBFactory;
import nl.mpi.archiving.corpusstructure.adapter.db.proxy.CorpusStructureDBProxy;
import nl.mpi.archiving.corpusstructure.adapter.db.proxy.VersioningAPIFactory;
import nl.mpi.archiving.corpusstructure.adapter.db.proxy.VersioningAPIProxy;
import nl.mpi.archiving.corpusstructure.adapter.servlet.CSDBConnectionFilter;
import nl.mpi.archiving.corpusstructure.core.handle.HandleResolver;
import nl.mpi.archiving.corpusstructure.core.handle.HttpHandleResolver;
import nl.mpi.archiving.corpusstructure.core.service.FilePathTranslator;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.AccessInfoProvider;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.corpusstructure.CorpusStructureDB;
import nl.mpi.versioning.manager.VersioningAPI;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    @Autowired
    @Qualifier("translationServiceLocation")
    private String translationServiceLocation;
    
    @Bean
    public CorpusStructureDBFactory csdbImplFactory() {
        return new CorpusStructureDBImplFactory("java:comp/env/jdbc/CSDB_HYBRID");
    }
    
    @Bean
    public CSDBContainer csdbContainer() {
        return new ThreadLocalCSDBContainer(csdbImplFactory(), aoImplFactory(), versioningApiFactory());
    }
    
    @Bean(name = "adapterCSDB")
    @Qualifier("adapterCSDB")
    public CorpusStructureDB csdbProxy() {
        return new CorpusStructureDBProxy(csdbContainer());
    }
    
    @Bean
    public ArchiveObjectsDBFactory aoImplFactory() {
        return new ArchiveObjectsDBImplFactory("java:comp/env/jdbc/CSDB_HYBRID");
    }
    
    @Bean(name = "adapterAO")
    @Qualifier("adapterAO")
    public ArchiveObjectsDB aoProxy() {
        return new ArchiveObjectsDBProxy(csdbContainer());
    }
    
    @Bean
    public VersioningAPIFactory versioningApiFactory() {
        return new VersioningAPIImplFactory("java:comp/env/jdbc/CSDB_HYBRID");
    }
    
    @Bean
    public VersioningAPI versioningProxy() {
        return new VersioningAPIProxy(csdbContainer());
    }
    
    @Bean
    public HandleResolver handleResolver() {
        return new HttpHandleResolver();
    }
    
    @Bean
    public NodeUriUtils nodeUriUtils() {
        return new NodeUriUtils(aoProxy());
    }
    
    @Bean
    public CorpusStructureAPIProviderFactory corpusStructureProviderFactory() {
        return new CorpusStructureAPIProviderFactory(csdbProxy(), aoProxy(), versioningProxy(), filePathTranslator(), nodeUriUtils(), translationServiceLocation);
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
    public TranslationService translationService() {
        return new TranslationService(aoProxy(), translationServiceLocation);
    }
    
    @Bean
    public FilePathTranslator filePathTranslator() {
        return new FilePathTranslatorAdapter(aoProxy(), handleResolver());
    }
    
    @Bean
    public CSDBConnectionFilter springDataSourceConnectionFilter() {
        return new CSDBConnectionFilter();
    }
}
