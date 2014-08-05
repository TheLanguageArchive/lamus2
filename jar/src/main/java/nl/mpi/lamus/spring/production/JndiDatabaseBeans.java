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
package nl.mpi.lamus.spring.production;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import nl.mpi.archiving.corpusstructure.core.database.dao.ArchiveObjectDao;
import nl.mpi.archiving.corpusstructure.core.database.dao.ArchivePropertyDao;
import nl.mpi.archiving.corpusstructure.core.database.dao.CorpusStructureDao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

/**
 * Configuration class containing some beans related with databases. To be used
 * in production, connecting to the real databases.
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Configuration
//@EnableTransactionManagement
//@ComponentScan("nl.mpi.archiving.corpusstructure")
@Profile(value = {"production", "demoserver"})
@ImportResource("classpath:/config/production/csdb.xml")
public class JndiDatabaseBeans {
    
    
//    @Autowired
//    private CorpusStructureProviderFactory cspFactory;
    
    private ArchivePropertyDao archiveDao;
    private ArchiveObjectDao aoDao;
    private CorpusStructureDao csDao;
    
    
//    @Resource
//    private Environment env;
    

    //TODO add these properties to some configuration file and load them here
    //TODO for testing load a dummy one? take care of closing the connection

//    private VersioningAPI versioningAPI;
//    private SearchClient searchClient;

    
//    @Bean
//    @Qualifier("corpusStructureDataSource")
//    public DataSource corpusStructureDataSource() throws NamingException {
//        Context ctx = new InitialContext();
//        return (DataSource) ctx.lookup("java:comp/env/jdbc/CSDB2");
//    }
    
//    @Bean  
//    public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws NamingException {
//            return new LocalContainerEntityManagerFactoryBean() {{
//                
////                setPackagesToScan("nl.mpi.archiving.corpusstructure.core.database.pojo");
//                setDataSource(corpusStructureDataSource());
//                setJpaVendorAdapter(
//                        new HibernateJpaVendorAdapter() {{
//                        }});
//                
////                setPersistenceProviderClass(PersistenceProviderImpl.class);
//                setPersistenceUnitName("corpusstructure2-persistency");
//                
//                setJpaProperties(hibProperties());
//            }};
//    }
    
//    private Properties hibProperties() {
//        return new Properties() {{
//            put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
//            put("hibernate.show_sql", "false");
//            put("hibernate.hbm2ddl.auto", "update");
//        }};
//    }
    
//    @Bean  
//    public JpaTransactionManager transactionManager() throws NamingException {
//        return new JpaTransactionManager() {{
//            setEntityManagerFactory(entityManagerFactory().getObject());
//        }};
//    }
//    @Bean
//    public PlatformTransactionManager transactionManager() throws NamingException{
////        return new JpaTransactionManager() {{
////            setEntityManagerFactory(
////                    entityManagerFactory().getObject());
////        }};
//        
//        JpaTransactionManager transactionManager = new JpaTransactionManager();
//        transactionManager.setEntityManagerFactory(
//                entityManagerFactory().getObject());
// 
//        return transactionManager;
//    }
    
//    @Bean
//    public PersistenceExceptionTranslationPostProcessor exceptionTranslation(){
//        return new PersistenceExceptionTranslationPostProcessor();
//    }
    
    
//    @Bean
//    public ArchivePropertyDao archiveDao() {
////        if(archiveDao == null) {
////            archiveDao = new ArchivePropertyDaoImpl();
////        }
////        return archiveDao;
//        return new ArchivePropertyDaoImpl();
//    }
    
//    @Bean
//    public ArchiveObjectDao aoDao() {
////        if(aoDao == null) {
////            aoDao = new ArchiveObjectDaoImpl();
////        }
////        return aoDao;
//        return new ArchiveObjectDaoImpl();
//    }
    
//    @Bean
//    public CorpusStructureDao csDao() {
////        if(csDao == null) {
////            csDao = new CorpusStructureDaoImpl();
////        }
////        return csDao;
//        return new CorpusStructureDaoImpl();
//    }
    
//    @Bean
//    @Qualifier("corpusStructureProvider")
//    public CorpusStructureProvider corpusStructureProvider() {
//        CorpusStructureProvider csProvider = new CorpusStructureProviderImpl(archiveDao(), aoDao(), csDao());
//        csProvider.initialize();
//        return csProvider;
////        return cspFactory.createCorpusStructureProvider();
//    }
    
//    @Bean
//    @Qualifier("nodeResolver")
//    public NodeResolver nodeResolver() {
//        return new CorpusStructureProviderNodeResolver();
//    }
    
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
//        if (versioningAPI == null) {
//            versioningAPI = new VersioningAPI("java:comp/env/jdbc/CSDB", "", "");
//        }
//        return versioningAPI;
//    }

//    @Bean
//    public SearchClient searchClient() {
//        //TODO INITIALIZE SearchClient bean
//        return searchClient;
//    }
}
