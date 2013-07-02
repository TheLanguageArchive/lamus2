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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import nl.mpi.annot.search.lib.SearchClient;
import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.corpusstructure.ArchiveObjectsDBImpl;
import nl.mpi.corpusstructure.CorpusStructureDBWriteImpl;
import nl.mpi.versioning.manager.VersioningAPI;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Configuration class containing some beans related with databases. To be used
 * in production, connecting to the real databases.
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Configuration
@Profile("production")
public class JndiDatabaseBeans {

    //TODO add these properties to some configuration file and load them here
    //TODO for testing load a dummy one? take care of closing the connection
    private CorpusStructureDBWriteImpl csDBWrite;
    private DataSource lamusDataSource;
    private VersioningAPI versioningAPI;
    private SearchClient searchClient;

    /**
     * @return ArchiveObjectsDB bean, which connects to the 'corpusstructure'
     * database
     */
    @Bean
    @Qualifier("ArchiveObjectsDB")
    public CorpusStructureDBWriteImpl archiveObjectsDB() {
        return corpusStructureDBWrite();
    }

    /**
     * Creates the connection to the 'corpusstructure' database, in case it
     * hasn't been done before.
     *
     * @return CorpusStructureDBWriteImpl object, which will be used for the
     * ArchiveObjectsDB bean
     */
    private CorpusStructureDBWriteImpl corpusStructureDBWrite() {
        if (csDBWrite == null) {

            //TODO How are the username and password injected here?

            csDBWrite = new CorpusStructureDBWriteImpl("java:comp/env/jdbc/CSDB", false, "", "");
        }
        return csDBWrite;
    }

    /**
     * @return DataSource bean corresponding to the Lamus2 database
     * @throws NamingException
     */
    @Bean
    @Qualifier("lamusDataSource")
    public DataSource lamusDataSource() throws NamingException {
        if (lamusDataSource == null) {
            Context ctx = new InitialContext();
            lamusDataSource = (DataSource) ctx.lookup("java:comp/env/jdbc/LAMUS2_DB");
        }
        return lamusDataSource;
    }

    @Bean
    public VersioningAPI versioningAPI() {
        if (versioningAPI == null) {
            versioningAPI = new VersioningAPI("java:comp/env/jdbc/CSDB", "", "");
        }
        return versioningAPI;
    }

    @Bean
    public SearchClient searchClient() {
        if (searchClient == null) {
            //TODO INITIALIZE SearchClient bean
        }
        return searchClient;
    }
}
