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
package nl.mpi.lamus.spring.adapter;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
//import nl.mpi.annot.search.lib.SearchClient;
//import nl.mpi.versioning.manager.VersioningAPI;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 *
 * @author guisil
 */
@Configuration
//@ComponentScan("nl.mpi.lamus")
@Profile(value = {"cmdi-adapter-csdb"})
public class JndiDatabaseBeans {
    
    /**
     * @return DataSource bean corresponding to the Lamus2 database
     * @throws NamingException
     */
    @Bean
    @Qualifier("lamusDataSource")
    public DataSource lamusDataSource() throws NamingException {
        Context ctx = new InitialContext();
        return (DataSource) ctx.lookup("java:comp/env/jdbc/LAMUS2_DB");
    }

//    @Bean
//    public VersioningAPI versioningAPI() {
//        return new VersioningAPI("java:comp/env/jdbc/CSDB", "", "");
//    }

//    @Bean
//    public SearchClient searchClient() {
//        //TODO INITIALIZE SearchClient bean
//        return null;
//    }
}
