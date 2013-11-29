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

import freemarker.core.Environment;
import java.util.Properties;
import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import nl.mpi.annot.search.lib.SearchClient;
import nl.mpi.versioning.manager.VersioningAPI;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

/**
 * Configuration class containing some beans related with databases. To be used
 * in production, connecting to the real databases.
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Configuration
@Profile("production")
public class JndiDatabaseBeans {
    
    
//    @Resource
//    private Environment env;
    

    //TODO add these properties to some configuration file and load them here
    //TODO for testing load a dummy one? take care of closing the connection

    private VersioningAPI versioningAPI;
    private SearchClient searchClient;

    
    @Bean
    @Qualifier("corpusStructureDataSource")
    public DataSource corpusStructureDataSource() throws NamingException {
        Context ctx = new InitialContext();
        return (DataSource) ctx.lookup("java:comp/env/jdbc/CSDB2");
    }
    
    @Bean  
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws NamingException {
            return new LocalContainerEntityManagerFactoryBean() {{
                
                setPackagesToScan("nl.mpi.archiving.corpusstructure.core.database.pojo");
                setDataSource(corpusStructureDataSource());
                setJpaVendorAdapter(
                        new HibernateJpaVendorAdapter() {{
                            setGenerateDdl(true);
                        }});
                setPersistenceUnitName(null);
                setJpaProperties(hibProperties());
            }};
    }
    
    private Properties hibProperties() {
        return new Properties() {{
            put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            put("hibernate.show_sql", "false");
            put("hibernate.hbm2ddl.auto", "update");
        }};
    }
    
    @Bean  
    public JpaTransactionManager transactionManager() throws NamingException {
        return new JpaTransactionManager() {{
            setEntityManagerFactory(entityManagerFactory().getObject());
        }};
    }
    
    
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
