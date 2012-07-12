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

import javax.sql.DataSource;
import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.corpusstructure.ArchiveObjectsDBImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Configuration
//@Profile("production")
public class JndiDatabaseBeans {
    
    //TODO add these properties to some configuration file and load them here
    //TODO for testing load a dummy one? take care of closing the connection
//    @Bean
//    public DataSource dataSource() {
//        return new DriverManagerDataSource("url", "user", "pass");
//    }
//    
//    //TODO add these properties to some configuration file and load them here
//    //TODO for testing load a dummy one? take care of closing the connection
//    @Bean
//    @Qualifier("ArchiveObjectsDB")
//    public ArchiveObjectsDB archiveObjectsDB() {
//        return new ArchiveObjectsDBImpl("url", "user", "pass");
//    }
    
}
