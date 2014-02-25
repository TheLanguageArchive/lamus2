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
package nl.mpi.lamus.workspace.stories.config;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sql.DataSource;
//import nl.mpi.annot.search.lib.SearchClient;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProviderFactory;
import nl.mpi.bcarchive.typecheck.FileType;
//import nl.mpi.corpusstructure.ArchiveObjectsDBWrite;
//import nl.mpi.corpusstructure.CorpusStructureDBWrite;
//import nl.mpi.corpusstructure.CorpusStructureDBWriteImpl;
import nl.mpi.lamus.ams.Ams2Bridge;
import nl.mpi.lamus.ams.AmsBridge;
import nl.mpi.lamus.workspace.stories.utils.WorkspaceStepsCorpusStructureProviderFactory;
//import nl.mpi.lat.ams.authentication.impl.AmsDbAuthenticationSrv;
//import nl.mpi.lat.ams.authentication.impl.IntegratedAuthenticationSrv;
//import nl.mpi.lat.ams.authentication.impl.LdapAuthenticationSrv;
//import nl.mpi.lat.ams.dao.*;
//import nl.mpi.lat.ams.service.LicenseService;
//import nl.mpi.lat.ams.service.RuleService;
//import nl.mpi.lat.ams.service.impl.*;
//import nl.mpi.lat.auth.authentication.AuthenticationService;
//import nl.mpi.lat.auth.authentication.EncryptionService;
//import nl.mpi.lat.auth.authorization.AdvAuthorizationService;
//import nl.mpi.lat.auth.federation.DamLrService;
//import nl.mpi.lat.auth.principal.PrincipalService;
//import nl.mpi.lat.fabric.FabricService;
//import nl.mpi.latimpl.auth.authentication.UnixCryptSrv;
//import nl.mpi.latimpl.fabric.FabricSrv;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.cmdi.api.CMDIApi;
import nl.mpi.metadata.identifierresolver.IdentifierResolver;
import nl.mpi.metadata.identifierresolver.URLResolver;
//import nl.mpi.versioning.manager.VersioningAPI;
import org.apache.commons.io.FileUtils;
//import org.delaman.ldap.ArchiveUserAuthImpl;
import org.hibernate.SessionFactory;
import org.jbehave.core.configuration.spring.SpringStoryReporterBuilder;
import static org.jbehave.core.reporters.Format.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Configuration
@ComponentScan(basePackages = {"nl.mpi.lamus"})
@Profile("acceptance")
public class WorkspaceStoriesConfig {

//    public TemporaryFolder testFolder = new TemporaryFolder();
    private File temporaryDirectory;
    
    private EmbeddedDatabase corpusstructureDataSource;
    private EmbeddedDatabase amsDataSource;
    private EmbeddedDatabase lamusDataSource;
    
//    @Bean
//    public SpringStoryReporterBuilder springStoryReporterBuilder() {
//        return (SpringStoryReporterBuilder) new SpringStoryReporterBuilder().withDefaultFormats().withFormats(HTML);
//    }
    
    private void createTemporaryDirectory() throws IOException {
        temporaryDirectory = new File("/tmp/lamusStoriesTestDirectory/");
        
        FileUtils.forceMkdir(temporaryDirectory);
    }
    
    @Bean
    @Qualifier("archiveFolder")
    public File archiveFolder() throws IOException {
//        testFolder.create();
        if(temporaryDirectory == null || !temporaryDirectory.exists()) {
            createTemporaryDirectory();
        }
//        File archiveFolder = testFolder.newFolder("archiveFolder");
        File archiveFolder = new File(temporaryDirectory, "archiveFolder");
        archiveFolder.mkdirs();
        
        FileUtils.forceDeleteOnExit(archiveFolder);
        
        return archiveFolder;
    }
    
    
    private WorkspaceStepsCorpusStructureProviderFactory csProviderFactory;
    private CorpusStructureProvider csProvider;
    
    private void corpusStructureProviderFactory() {
        if(csProviderFactory == null) {
            csProviderFactory = new WorkspaceStepsCorpusStructureProviderFactory("jdbc:hsqldb:mem:corpusstructure");
        }
    }
    
    @Bean
    public CorpusStructureProvider corpusStructureProvider() {
        corpusStructureProviderFactory();
        if(csProvider == null) {
            corpusstructureDataSource();
            csProvider = csProviderFactory.createCorpusStructureProvider();
        }
        return csProvider;
    }
    
    private NodeResolver nodeResolver;
    
    @Bean
    public NodeResolver nodeResolver() {
        corpusStructureProviderFactory();
        if(nodeResolver == null) {
            corpusstructureDataSource();
            nodeResolver = csProviderFactory.createNodeResolver();
        }
        return nodeResolver;
    }
    
//    @Bean
//    @Qualifier("ArchiveObjectsDB")
//    public CorpusStructureDBWriteImpl archiveObjectsDB() {
//        return corpusStructureDBWrite();
//    }

//    private CorpusStructureDBWriteImpl csDBWrite;

//    private CorpusStructureDBWriteImpl corpusStructureDBWrite() {
//        if(csDBWrite == null) {
//            corpusstructureDataSource();
//            csDBWrite = new CorpusStructureDBWriteImpl("jdbc:hsqldb:mem:corpusstructure", false, "sa", "");
//        }
//        return csDBWrite;
//    }

    @Bean
    @Qualifier("corpusstructureDataSource")
    public DataSource corpusstructureDataSource() {
        if(corpusstructureDataSource == null) {
            createCorpusstructureDB();
        }
        return corpusstructureDataSource;
    }
    
    private void createCorpusstructureDB() {
        corpusstructureDataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .setName("corpusstructure")
                .addScript("classpath:nl/mpi/lamus/workspace/stories/config/hsql_corpusstructure_drop.sql")
                .addScript("classpath:nl/mpi/lamus/workspace/stories/config/hsql_corpusstructure_create.sql")
                .addScript("classpath:nl/mpi/lamus/workspace/stories/config/hsql_corpusstructure_insert_basic.sql")
                .build();
    }
    
//    private VersioningAPI versioningAPI;
    
//    @Bean
//    public VersioningAPI versioningAPI() {
//        corpusStructureDBWrite();
//        if(versioningAPI == null) {
//            versioningAPI = new VersioningAPI("jdbc:hsqldb:mem:corpusstructure");
//        }
//        return versioningAPI;
//    }
    
//    private SearchClient searchClient;
    
//    @Bean
//    public SearchClient searchClient() throws SQLException {
//        if(searchClient == null) {
//            corpusStructureDBWrite();
//            createAnnexDB();
//            searchClient = new SearchClient("jdbc:hsqldb:mem:corpusstructure", "sa", "", null, "jdbc:hsqldb:mem:annex", "sa", "");
//        }
//        return searchClient;
//    }
    
    private void createAnnexDB() {
        new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .setName("annex")
                //TODO Run scripts with proper database structure
                .build();
    }
    
    @Bean
    @Qualifier
    public DataSource amsDataSource() {
        if(amsDataSource == null) {
            createAmsDB();
        }
        return amsDataSource;
    }
    
    private void createAmsDB() {
        amsDataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .setName("ams2")
                .addScript("classpath:nl/mpi/lamus/workspace/stories/config/hsql_ams2_drop.sql")
                .addScript("classpath:nl/mpi/lamus/workspace/stories/config/hsql_ams2_create.sql")
                .addScript("classpath:nl/mpi/lamus/workspace/stories/config/hsql_ams2_insert_basic.sql")
                .build();
    }
    
    @Bean
    @Qualifier("lamusDataSource")
    public DataSource lamusDataSource() {
        if(lamusDataSource == null) {
            createLamusDB();
        }
        return lamusDataSource;
    }
    
    private void createLamusDB() {
        lamusDataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .setName("lamus2")
                .addScript("classpath:nl/mpi/lamus/dao/implementation/hsql_lamus2_drop.sql")
                .addScript("classpath:nl/mpi/lamus/dao/implementation/hsql_lamus2_create.sql")
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
//
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

    private SessionFactory sesFactory;

    private SessionFactory sessionFactory() throws Exception {
        if(sesFactory == null) {

            LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
            sessionFactoryBean.setDataSource(amsDataSource);

            String [] mappingResources = {
            "orm/principal.hbm.xml",
                "orm/nodeauth.hbm.xml",
                "orm/rule.hbm.xml",
                "orm/license.hbm.xml" 
            };

            Properties hibernateProperties = new Properties();
            hibernateProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
            hibernateProperties.setProperty("hibernate.cache.use_query_cache", "true");
            hibernateProperties.setProperty("hibernate.cache.use_second_level_cache", "true");
            hibernateProperties.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.EhCacheProvider");
            hibernateProperties.setProperty("hibernate.cache.provider_configuration_file_resource_path", "/ehcache.xml");

            sessionFactoryBean.setMappingResources(mappingResources);
            sessionFactoryBean.setHibernateProperties(hibernateProperties);

            sessionFactoryBean.afterPropertiesSet();

            sesFactory = (SessionFactory) sessionFactoryBean.getObject();
        }
        return sesFactory;
    }
    
//    private IntegratedAuthenticationSrv autheSrv;
//    
//    @Bean
//    @Qualifier("integratedAuthenticationSrv")
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
//            fSrv.setStructDB(archiveObjectsDB());
//            fSrv.setArchObjDB(archiveObjectsDB());
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
    
    @Bean
    public ExecutorService executorService() {
        return Executors.newSingleThreadExecutor();
    }
    
    //TODO change properties to initialise API
    @Bean
    public MetadataAPI metadataAPI() {
        return new CMDIApi();
    }
    
    @Bean
    public IdentifierResolver identifierResolver() {
        return new URLResolver();
    }
    
    @Bean
    public FileType typeChecker() {
        return new FileType();
    }
    
    
    // PROPERTIES
    
    
    @Bean
    @Qualifier("defaultMaxStorageSpaceInBytes")
    public long defaultMaxStorageSpaceInBytes() {
        return 10240 * 1024 * 1024;
    }
    
    @Bean
    @Qualifier("numberOfDaysOfInactivityAllowedSinceLastSession")
    public int numberOfDaysOfInactivityAllowedSinceLastSession() {
        return 60;
    }

    @Bean
    @Qualifier("totalNumberOfDaysAllowedUntilExpiry")
    public int totalNumberOfDaysAllowedUntilExpiry() {
        return 180;
    }
    
    @Bean
    @Qualifier("numberOfDaysOfInactivityAllowedSinceLastWarningEmail")
    public int numberOfDaysOfInactivityAllowedSinceLastWarningEmail() {
        return 30;
    }
    
    @Bean
    @Qualifier("typeRecheckSizeLimitInBytes")
    public long typeRecheckSizeLimitInBytes() {
        return 8 * 1024 * 1024;
    }
    
    @Bean
    @Qualifier("maxDirectoryNameLength")
    public int maxDirectoryNameLength() {
        return 100;
    }
    
    @Bean
    @Qualifier("corpusDirectoryBaseName")
    public String corpusDirectoryBaseName() {
        return "Corpusstructure";
    }
    
    @Bean
    @Qualifier("orphansDirectoryBaseName")
    public String orphansDirectoryBaseName() {
        return "sessions";
    }
    
    @Bean
    @Qualifier("workspaceBaseDirectory")
    public File workspaceBaseDirectory() throws IOException {
//        testFolder.create();
        if(temporaryDirectory == null || !temporaryDirectory.exists()) {
            createTemporaryDirectory();
        }
//        File workspaceBaseFolder = testFolder.newFolder("workspaceFolders");
        File workspaceBaseFolder = new File(temporaryDirectory, "workspaceFolders");
        workspaceBaseFolder.mkdirs();
        
        FileUtils.forceDeleteOnExit(workspaceBaseFolder);
        
        return workspaceBaseFolder;
    }
    
    @Bean
    @Qualifier("trashCanBaseDirectory")
    public File trashCanBaseDirectory() throws IOException {
//        testFolder.create();
        if(temporaryDirectory == null || !temporaryDirectory.exists()) {
            createTemporaryDirectory();
        }
//        File trashcanBaseFolder = testFolder.newFolder("trashcanFolder");
        File trashcanBaseFolder = new File(temporaryDirectory, "trashcanFolder");
        trashcanBaseFolder.mkdirs();
        
        FileUtils.forceDeleteOnExit(trashcanBaseFolder);
        
        return trashcanBaseFolder;
    }
    
    @Bean
    @Qualifier("workspaceUploadDirectoryName")
    public String workspaceUploadDirectoryName() {
        return "upload";
    }
    
    @Bean
    @Qualifier("customTypecheckerFolderToConfigFileMap")
    public Map<String, String> customTypecheckerFolderToConfigFileMap() {
        
        Map<String, String> mapToReturn = new HashMap<String, String>();
        mapToReturn.put("dummyLocation", "dummyConfigFile.txt");
        
        return mapToReturn;
    }
}
