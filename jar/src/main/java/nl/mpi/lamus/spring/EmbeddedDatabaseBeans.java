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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import javax.sql.DataSource;
import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.corpusstructure.ArchiveObjectsDBWrite;
import nl.mpi.corpusstructure.CorpusStructureDB;
import nl.mpi.corpusstructure.CorpusStructureDBWriteImpl;
import nl.mpi.lat.ams.authentication.impl.AmsDbAuthenticationSrv;
import nl.mpi.lat.ams.authentication.impl.IntegratedAuthenticationSrv;
import nl.mpi.lat.ams.authentication.impl.LdapAuthenticationSrv;
import nl.mpi.lat.ams.dao.*;
import nl.mpi.lat.ams.service.LicenseService;
import nl.mpi.lat.ams.service.RuleService;
import nl.mpi.lat.ams.service.impl.*;
import nl.mpi.lat.auth.authentication.AuthenticationService;
import nl.mpi.lat.auth.authentication.EncryptionService;
import nl.mpi.lat.auth.authorization.AdvAuthorizationService;
import nl.mpi.lat.auth.authorization.AuthorizationService;
import nl.mpi.lat.auth.federation.DamLrService;
import nl.mpi.lat.auth.principal.PrincipalService;
import nl.mpi.lat.fabric.FabricService;
import nl.mpi.latimpl.auth.authentication.UnixCryptSrv;
import nl.mpi.latimpl.fabric.FabricSrv;
import org.delaman.ldap.ArchiveUserAuthImpl;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Configuration
@Profile("testing")
public class EmbeddedDatabaseBeans {
    
    private final static Logger logger = LoggerFactory.getLogger(EmbeddedDatabaseBeans.class);
    
    
    private DataSource lamusDataSource;
    
    /**
     * Creates the 'lamus2' database, as an embedded database
     * 
     * @return DataSource to the 'lamus2' database, used by the WorkspaceDao bean
     */
    
//    @Bean
//    @Qualifier("lamusDataSource")
//    public DataSource dataSource() {
//        if(lamusDataSource == null) {
//            lamusDataSource = new EmbeddedDatabaseBuilder()
//                .setType(EmbeddedDatabaseType.HSQL)
//                .setName("lamus2")
//                .addScript("classpath:nl/mpi/lamus/dao/implementation/hsql_lamus2_drop.sql")
//                .addScript("classpath:nl/mpi/lamus/dao/implementation/hsql_lamus2_create.sql")
//                .build();
//        }
//        return lamusDataSource;
//    }
//    
//    private void createLamusDB() {
//        
//    }
    
    /**
     * @return ArchiveObjectsDB bean, which connects to the 'corpusstructure' database
     */
    @Bean
    @Qualifier("ArchiveObjectsDB")
    public ArchiveObjectsDBWrite archiveObjectsDB() {
        return corpusStructureDBWrite();
    }
    
    @Bean
    @Qualifier("CorpusStructureDB")
    public CorpusStructureDB corpusStructureDB() {
        return corpusStructureDBWrite();
    }
    
    private CorpusStructureDBWriteImpl csDBWrite;
    
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
        new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .setName("corpusstructure")
//                .addScript("classpath:hsql_corpusstructure_drop.sql")
//                .addScript("classpath:hsql_corpusstructure_create.sql")
                .build();
    }
    
//    private PrincipalSrv pcplSrv;
//    
//    @Bean
//    public PrincipalService principalSrv() throws Exception {
//        if(pcplSrv == null) {
//            pcplSrv = new PrincipalSrv();
//            pcplSrv.setPcplDao(principalDao());
//            pcplSrv.setUserDao(userDao());
//            pcplSrv.setGroupDao(groupDao());
//            pcplSrv.setEncryptionSrv(encryptionSrv());
//        }
//        return pcplSrv;
//    }
    
//    private PrincipalDao pcplDao;
//    
//    private PrincipalDao principalDao() throws Exception {
//        if(pcplDao == null) {
//            pcplDao = new PrincipalDao();
//            pcplDao.setSessionFactory(sessionFactory());
//        }
//        return pcplDao;
//    }
    
//    private UserDao uDao;
//    
//    private UserDao userDao() throws Exception {
//        if(uDao == null) {
//            uDao = new UserDao();
//            uDao.setSessionFactory(sessionFactory());
//        }
//        return uDao;
//    }
    
//    private GroupDao gDao;
//    
//    private GroupDao groupDao() throws Exception {
//        if(gDao == null) {
//            gDao = new GroupDao();
//            gDao.setSessionFactory(sessionFactory());
//        }
//        return gDao;
//    }
    
//    private EncryptionService encSrv;
//    
//    private EncryptionService encryptionSrv() {
//        if(encSrv == null) {
//            encSrv = new UnixCryptSrv();
//            encSrv.setAutoPrefix(true);
//        }
//        return encSrv;
//    }
    
//    private SessionFactory sesFactory;
//    
//    private SessionFactory sessionFactory() throws Exception {
//        if(sesFactory == null) {
//            
//            LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
//            sessionFactoryBean.setDataSource(amsDataSource());
//            
//            String [] mappingResources = {
//              "orm/principal.hbm.xml",
//                "orm/nodeauth.hbm.xml",
//                "orm/rule.hbm.xml",
//                "orm/license.hbm.xml" 
//            };
//            
//            Properties hibernateProperties = new Properties();
//            hibernateProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
//            hibernateProperties.setProperty("hibernate.cache.use_query_cache", "true");
//            hibernateProperties.setProperty("hibernate.cache.use_second_level_cache", "true");
//            hibernateProperties.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.EhCacheProvider");
//            hibernateProperties.setProperty("hibernate.cache.provider_configuration_file_resource_path", "/ehcache.xml");
//            
//            sessionFactoryBean.setMappingResources(mappingResources);
//            sessionFactoryBean.setHibernateProperties(hibernateProperties);
//            
//            sessionFactoryBean.afterPropertiesSet();
//            
//            sesFactory = (SessionFactory) sessionFactoryBean.getObject();
//        }
//        return sesFactory;
//    }
    
//    private DataSource amsDS;
//    
////    @Bean
//    public DataSource amsDataSource() {
//        if(amsDS == null) {
//            amsDS = new EmbeddedDatabaseBuilder()
//                .setType(EmbeddedDatabaseType.HSQL)
//                .setName("ams2")
////                .addScript("classpath:hsql_ams2_drop.sql")
////                .addScript("classpath:hsql_ams2_create.sql")
//                .build();
//        }
//        return amsDS;
//    }
    
//    private IntegratedAuthenticationSrv autheSrv;
//    
//    @Bean
//    public AuthenticationService authenticationSrv() throws Exception {
//        if(autheSrv == null) {
//            autheSrv = new IntegratedAuthenticationSrv();
//            List<AuthenticationService> authenticationServices = new ArrayList<AuthenticationService>();
//            authenticationServices.add(ldapAuthenticationSrv());
//            authenticationServices.add(amsAuthenticationSrv());
//            autheSrv.setServices(authenticationServices);
//        }
//        return autheSrv;
//    }
    
//    private LdapAuthenticationSrv ldapAutheSrv;
//    
//    private AuthenticationService ldapAuthenticationSrv() {
//        if(ldapAutheSrv == null) {
//            ldapAutheSrv = new LdapAuthenticationSrv();
//            ldapAutheSrv.setUseFederateID(true);
//            ldapAutheSrv.setDamlrSrv(damlrSrv());
//            ldapAutheSrv.setDamlrLdapApi(damlrLdapApi());
//        }
//        return ldapAutheSrv;
//    }
    
//    private AmsDbAuthenticationSrv amsAutheSrv;
//    
//    private AuthenticationService amsAuthenticationSrv() throws Exception {
//        if(amsAutheSrv == null) {
//            amsAutheSrv = new AmsDbAuthenticationSrv();
//            amsAutheSrv.setUseFederateID(true);
//            amsAutheSrv.setPrincipalSrv(principalSrv());
//            amsAutheSrv.setEncryptionSrv(encryptionSrv());
//            amsAutheSrv.setDamlrSrv(damlrSrv());
//        }
//        return amsAutheSrv;
//    }
    
//    private DamLrSrv damlrSrv;
//    
//    private DamLrService damlrSrv() {
//        if(damlrSrv == null) {
//            damlrSrv = new DamLrSrv();
//            damlrSrv.setDefaultHostingInstitute("MPINLA");
//            damlrSrv.setDefaultHostingService("ams2/ldap");
//            damlrSrv.setDefaultFedID("mpi.nl");
//            damlrSrv.setDefaultDelimiter("@");
//            damlrSrv.setUseFederateID(true);
//            damlrSrv.setDamlrLdapApi(damlrLdapApi());
//            damlrSrv.setLdapEnabled(false);
//
//        //TODO use an embedded LDAP for testing
//        }
//        
//        return damlrSrv;
//    }
    
//    private ArchiveUserAuthImpl damlrLdapApi;
//    
//    private ArchiveUserAuthImpl damlrLdapApi() {
//        if(damlrLdapApi == null) {
//            damlrLdapApi = new ArchiveUserAuthImpl();
//            damlrLdapApi.setEncryptionSrv(encryptionSrv());
////        damlrLdapApi.setLdapJndiName(null);
////        damlrLdapApi.setKerberosJndiName(null);
//        
//        //TODO use an embedded LDAP for testing
//        }
//        
//        return damlrLdapApi;
//    }
    
//    private AmsAuthorizationSrv authoSrv;
//    
//    @Bean
//    public AdvAuthorizationService authorizationSrv() throws Exception {
//        if(authoSrv == null) {
//            authoSrv = new AmsAuthorizationSrv();
//            authoSrv.setNodeAuthDao(nodeAuthDao());
//            authoSrv.setFabricSrv(fabricSrv());
//            authoSrv.setLicenseSrv(licenseSrv());
//            authoSrv.setRuleSrv(ruleSrv());
//            authoSrv.setRuleEvaluationSrv(corpusDbAuthSrv());
//        }
//        return authoSrv;
//    }
    
//    private NodeAuthorizationDao nAuthDao;
//    
//    private NodeAuthorizationDao nodeAuthDao() throws Exception {
//        if(nAuthDao == null) {
//            nAuthDao = new NodeAuthorizationDao();
//            nAuthDao.setSessionFactory(sessionFactory());
//        }
//        return nAuthDao;
//    }
    
//    private CsDbAuthSrv csdbAuthSrv;
//    
//    private CsDbAuthSrv corpusDbAuthSrv() throws Exception {
//        if(csdbAuthSrv == null) {
//            csdbAuthSrv = new CsDbAuthSrv();
//            csdbAuthSrv.setFabricSrv(fabricSrv());
//            csdbAuthSrv.setPrincipalSrv(principalSrv());
//        }
//        return csdbAuthSrv;
//    }
    
//    private FabricSrv fSrv;
//    
//    @Bean
//    public FabricService fabricSrv() {
//        if(fSrv == null) {
//            fSrv = new FabricSrv();
//            fSrv.setStructDB(corpusStructureDB());
//            fSrv.setArchObjDB(corpusStructureDBWrite());
//        }
//        return fSrv;
//    }
    
//    private LicenseSrv lSrv;
//    
//    @Bean
//    public LicenseService licenseSrv() throws Exception {
//        if(lSrv == null) {
//            lSrv = new LicenseSrv();
//            lSrv.setLicenseDao(licenseDao());
//            lSrv.setNodeLicenseDao(nodeLicenseDao());
//        }
//        return lSrv;
//    }
    
//    private LicenseDao lDao;
//    
//    private LicenseDao licenseDao() throws Exception {
//        if(lDao == null) {
//            lDao = new LicenseDao();
//            lDao.setSessionFactory(sessionFactory());
//        }
//        return lDao;
//    }
//    
//    private NodeLicenseDao nLDao;
//    
//    private NodeLicenseDao nodeLicenseDao() throws Exception {
//        if(nLDao == null) {
//            nLDao = new NodeLicenseDao();
//            nLDao.setSessionFactory(sessionFactory());
//        }
//        return nLDao;
//    }
    
//    private RuleSrv rSrv;
//    
//    @Bean
//    public RuleService ruleSrv() throws Exception {
//        if(rSrv == null) {
//            rSrv = new RuleSrv();
//            rSrv.setRuleDao(ruleDao());
//        }
//        return rSrv;
//    }
    
//    private RuleDao rDao;
//    
//    private RuleDao ruleDao() throws Exception {
//        if(rDao == null) {
//            rDao = new RuleDao();
//            rDao.setSessionFactory(sessionFactory());
//        }
//        return rDao;
//    }
}
