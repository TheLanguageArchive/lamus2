/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.spring;

import java.sql.SQLException;
import nl.mpi.annot.search.lib.SearchClient;
import nl.mpi.corpusstructure.ArchiveObjectsDBWrite;
import nl.mpi.corpusstructure.CorpusStructureDB;
import nl.mpi.corpusstructure.CorpusStructureDBWriteImpl;
import nl.mpi.versioning.manager.VersioningAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

/**
 * Configuration class containing some beans related with databases.
 * To be used in testing, therefore the databases are embedded.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Configuration
@Profile("testing")
public class EmbeddedDatabaseBeans {
    
    private final static Logger logger = LoggerFactory.getLogger(EmbeddedDatabaseBeans.class);
    private CorpusStructureDBWriteImpl csDBWrite;
    
    private SearchClient sClient;
    
    /**
     * @return ArchiveObjectsDB bean, which connects to the 'corpusstructure' database
     */
    @Bean
    @Qualifier("ArchiveObjectsDB")
    public ArchiveObjectsDBWrite archiveObjectsDB() {
        return corpusStructureDBWrite();
    }

    /**
     * @return  CorpusStructureDB bean, which connects to the 'corpusstructure' database
     */
    @Bean
    @Qualifier("CorpusStructureDB")
    public CorpusStructureDB corpusStructureDB() {
        return corpusStructureDBWrite();
    }
    
    @Bean
    public VersioningAPI versioningAPI() {
        corpusStructureDBWrite();
        return new VersioningAPI("jdbc:hsqldb:mem:corpusstructure", "sa", "");
    }
    
    @Bean
    public SearchClient searchClient() throws SQLException {
        if(sClient == null) {
            corpusStructureDBWrite();
            createAnnexDB();
            sClient = new SearchClient("jdbc:hsqldb:mem:corpusstructure", "sa", "", null, "jdbc:hsqldb:mem:annex", "sa", "");
        }
        return sClient;
    }
    
    /**
     * Creates the connection to the 'corpusstructure' database, in case it hasn't been done before.
     * @return CorpusStructureDBWriteImpl object, which will be used for the ArchiveObjectsDB and CorpusStructureDB beans
     */
    private CorpusStructureDBWriteImpl corpusStructureDBWrite() {
        if(csDBWrite == null) {
            createCorpusstructureDB();
            csDBWrite = new CorpusStructureDBWriteImpl("jdbc:hsqldb:mem:corpusstructure", true, "sa", "");
        }
        return csDBWrite;
    }
    
    /**
     * Creates the 'corpusstructure' database, as an embedded database
     */
    private void createCorpusstructureDB() {
        logger.debug("Creating connection to the corpusstructure database.");
        new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .setName("corpusstructure")
//                .addScript("classpath:hsql_corpusstructure_drop.sql")
//                .addScript("classpath:hsql_corpusstructure_create.sql")
                .build();
    }
    
    private void createAnnexDB() {
        logger.debug("Creating connection to the annex database");
        new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .setName("annex")
                //TODO Run scripts with proper database structure
                .build();
    }
}
