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
package nl.mpi.lamus.workspace.stories;

import java.io.*;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;
import javax.sql.DataSource;
import nl.mpi.corpusstructure.*;
import nl.mpi.lamus.ams.Ams2Bridge;
import nl.mpi.lamus.ams.AmsBridge;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.dao.implementation.LamusJdbcWorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.filesystem.implementation.LamusWorkspaceDirectoryHandler;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.service.implementation.LamusWorkspaceService;
import nl.mpi.lamus.workspace.factory.WorkspaceFactory;
import nl.mpi.lamus.workspace.factory.implementation.LamusWorkspaceFactory;
import nl.mpi.lamus.workspace.management.WorkspaceAccessChecker;
import nl.mpi.lamus.workspace.management.implementation.LamusWorkspaceAccessChecker;
import nl.mpi.lamus.workspace.management.implementation.LamusWorkspaceManager;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.lat.ams.dao.*;
import nl.mpi.lat.ams.service.LicenseService;
import nl.mpi.lat.ams.service.RuleService;
import nl.mpi.lat.ams.service.impl.*;
import nl.mpi.lat.auth.authentication.EncryptionService;
import nl.mpi.lat.auth.authorization.AdvAuthorizationService;
import nl.mpi.lat.auth.principal.PrincipalService;
import nl.mpi.lat.fabric.FabricService;
import nl.mpi.latimpl.auth.authentication.UnixCryptSrv;
import nl.mpi.latimpl.fabric.FabricSrv;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.FileUtils;
import org.hibernate.SessionFactory;
import org.jbehave.core.annotations.*;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class WorkspaceSteps {
    

    private static final Logger logger = LoggerFactory.getLogger(WorkspaceSteps.class);
    
    @Autowired
    @Qualifier("archiveFolder")
    private File archiveFolder;
    
    @Autowired
    @Qualifier("workspaceBaseDirectory")
    private File workspaceBaseDirectory;
    
    private URL topNodeURL;
    
    @Autowired
    @Qualifier("corpusstructureDataSource")
    private DataSource corpusstructureDataSource;
    @Autowired
    @Qualifier("amsDataSource")
    private DataSource amsDataSource;
    @Autowired
    @Qualifier("lamusDataSource")
    private DataSource lamusDataSource;

    @Autowired
    @Qualifier("ArchiveObjectsDB")
    private ArchiveObjectsDBWrite archiveObjectsDB;
    @Autowired
    @Qualifier("ArchiveObjectsDB")
    private CorpusStructureDBWrite corpusStructureDB;
    
    @Autowired
    private Ams2Bridge ams2Bridge;

    @Autowired
    private WorkspaceDao workspaceDao;
    
    @Autowired
    private WorkspaceService workspaceService;
 
    private int selectedNodeID;
    
    private String currentUserID;
    
    private Workspace createdWorkspace;
    
    private int workspaceIDToDelete;
    
    @BeforeStory
    public void beforeStory() throws IOException {
        cleanLamusDatabaseAndFilesystem();
    }
    
    
//    @BeforeClass
//    private static void setupArchiveFolder() throws IOException {
//        TemporaryFolder tempFolder = new TemporaryFolder();
//        File aFolder = tempFolder.newFile("archiveFolder");
//        if(!aFolder.mkdirs()) {
//            fail("archive folder could not be created");
//        }
//        archiveFolder = aFolder;
//    }
    
//    @BeforeScenario
//    public void createAndPopulateDatabases() throws Exception {
//     
////        if(archiveFolder == null) {
////            createArchiveFolder();
////        }
//        
////        if(corpusstructureDataSource == null) {
////            createCorpusstructureDB();
////        }
////        if(amsDataSource == null) {
////            createAmsDB();
////        }
////        if(lamusDataSource == null) {
////            createLamusDB();
////        }
//        
////        CorpusStructureDBWriteImpl csdbw = getInitialisedCSDB();
////        corpusStructureDB = csdbw;
////        archiveObjectsDB = csdbw;
//        //insertDataInCSDB();
//        
////        ams2Bridge = getInitialisedAmsBridge();
//        //insertDataInAmsDB();
//        
////        initialiseWorkspaceDao();
//        //initialiseWorkspaceService();
//        
////        assertNotNull("WORKSPACESERVICE NOT INJECTED", workspaceService);
//        
//    }
//    
////    private void createArchiveFolder() throws IOException {
////        tempFolder = new TemporaryFolder();
////        tempFolder.create();
////        archiveFolder = tempFolder.newFolder("archiveFolder");
////        archiveFolder.mkdirs();
////        copyTestFileToArchiveFolder();
////    }
//    
    private void copyTestFileToArchiveFolder() throws IOException {

        InputStream testIn = getClass().getClassLoader().getResourceAsStream("Node_CMDIfied_IMDI.cmdi");
        File testFile = new File(this.archiveFolder, "Node_CMDIfied_IMDI.cmdi");
        OutputStream testOut = new FileOutputStream(testFile);
        IOUtils.copy(testIn, testOut);
        
        this.topNodeURL = testFile.toURI().toURL();
    }
//    
//    private void createCorpusstructureDB() {
//        corpusstructureDataSource = new EmbeddedDatabaseBuilder()
//                .setType(EmbeddedDatabaseType.HSQL)
//                .setName("corpusstructure")
//                .addScript("classpath:hsql_corpusstructure_drop.sql")
//                .addScript("classpath:hsql_corpusstructure_create.sql")
//                .build();
//    }
//
//    private void createAmsDB() {
//        amsDataSource = new EmbeddedDatabaseBuilder()
//                .setType(EmbeddedDatabaseType.HSQL)
//                .setName("ams2")
//                .addScript("classpath:hsql_ams2_drop.sql")
//                .addScript("classpath:hsql_ams2_create.sql")
////                .addScript("classpath:hsql_ams2_insert.sql")
//                .build();
//    }
//    
//    private void createLamusDB() {
//        lamusDataSource = new EmbeddedDatabaseBuilder()
//                .setType(EmbeddedDatabaseType.HSQL)
//                .setName("lamus2")
//                .addScript("classpath:nl/mpi/lamus/dao/implementation/hsql_lamus2_drop.sql")
//                .addScript("classpath:nl/mpi/lamus/dao/implementation/hsql_lamus2_create.sql")
//                .build();
//    }
//    
//    
//    private CorpusStructureDBWriteImpl getInitialisedCSDB() {
//        return new CorpusStructureDBWriteImpl("jdbc:hsqldb:mem:corpusstructure", false, "sa", "");
//    }
//    
//    private Ams2Bridge getInitialisedAmsBridge() throws Exception {
//        return new Ams2Bridge(principalSrv(), null, authorizationSrv(), fabricSrv(), licenseSrv(), ruleSrv());
//    }
//    
//    private PrincipalSrv pcplSrv;
//
//    private PrincipalService principalSrv() throws Exception {
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
//
//    private PrincipalDao pcplDao;
//
//    private PrincipalDao principalDao() throws Exception {
//        if(pcplDao == null) {
//            pcplDao = new PrincipalDao();
//            pcplDao.setSessionFactory(sessionFactory());
//        }
//        return pcplDao;
//    }
//
//
//    private UserDao uDao;
//
//    private UserDao userDao() throws Exception {
//        if(uDao == null) {
//            uDao = new UserDao();
//            uDao.setSessionFactory(sessionFactory());
//        }
//        return uDao;
//    }
//
//    private GroupDao gDao;
//
//    private GroupDao groupDao() throws Exception {
//        if(gDao == null) {
//            gDao = new GroupDao();
//            gDao.setSessionFactory(sessionFactory());
//        }
//        return gDao;
//    }
//
//    private EncryptionService encSrv;
//
//    private EncryptionService encryptionSrv() {
//        if(encSrv == null) {
//            encSrv = new UnixCryptSrv();
//            encSrv.setAutoPrefix(true);
//        }
//        return encSrv;
//    }    
//
//    private SessionFactory sesFactory;
//
//    private SessionFactory sessionFactory() throws Exception {
//        if(sesFactory == null) {
//
//            LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
//            sessionFactoryBean.setDataSource(amsDataSource);
//
//            String [] mappingResources = {
//            "orm/principal.hbm.xml",
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
//
//    private AmsAuthorizationSrv authoSrv;
//
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
//
//    private NodeAuthorizationDao nAuthDao;
//
//    private NodeAuthorizationDao nodeAuthDao() throws Exception {
//        if(nAuthDao == null) {
//            nAuthDao = new NodeAuthorizationDao();
//            nAuthDao.setSessionFactory(sessionFactory());
//        }
//        return nAuthDao;
//    }
//
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
//
//    private FabricSrv fSrv;
//
//    public FabricService fabricSrv() {
//        if(fSrv == null) {
//            fSrv = new FabricSrv();
//            fSrv.setStructDB(corpusStructureDB);
//            fSrv.setArchObjDB(archiveObjectsDB);
//        }
//        return fSrv;
//    }
//
//    private LicenseSrv lSrv;
//
//    public LicenseService licenseSrv() throws Exception {
//        if(lSrv == null) {
//            lSrv = new LicenseSrv();
//            lSrv.setLicenseDao(licenseDao());
//            lSrv.setNodeLicenseDao(nodeLicenseDao());
//        }
//        return lSrv;
//    }
//
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
//
//    private RuleSrv rSrv;
//
//    public RuleService ruleSrv() throws Exception {
//        if(rSrv == null) {
//            rSrv = new RuleSrv();
//            rSrv.setRuleDao(ruleDao());
//        }
//        return rSrv;
//    }
//
//    private RuleDao rDao;
//
//    private RuleDao ruleDao() throws Exception {
//        if(rDao == null) {
//            rDao = new RuleDao();
//            rDao.setSessionFactory(sessionFactory());
//        }
//        return rDao;
//    }

    private void insertDataInCSDB() {
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(this.corpusstructureDataSource);

//            String insertString = "INSERT INTO imdiadmin VALUES (:name, :value);";
//            Map<String, String> map = new HashMap<String, String>();
//            map.put("name", "UNIX_ARCHIVE_ROOT");
//            map.put("value", archiveFolder.getAbsolutePath());
//            template.update(insertString, map);
//            
//            insertString = "INSERT INTO imdiadmin VALUES (:name, :value);";
//            map = new HashMap<String, String>();
//            map.put("name", "HTTP_ARCHIVE_ROOT");
//            map.put("value", "http://someserver.mpi.nl/corpora/");
//            template.update(insertString, map);

        String insertString = "INSERT INTO accessgroups VALUES (:md5, :aclstring);";
        Map<String, String> map = new HashMap<String, String>();
        map.put("md5", "everybody");
        map.put("aclstring", "everybody");
        template.update(insertString, map);
        
        insertString = "INSERT INTO archiveobjects (nodeid, url, crawltime, onsite, readrights, writerights, pid, accesslevel) "
                + "VALUES (:nodeid, :url, :crawltime, :onsite, :readrights, :writerights, :pid, :accesslevel);";
        map.clear();
        map.put("nodeid", "1");
        map.put("url", this.topNodeURL.toString()); //"http://someserver.mpi.nl/corpora/Node_CMDIfied_IMDI.cmdi");
        map.put("crawltime", new Timestamp(Calendar.getInstance().getTimeInMillis()).toString());
        map.put("onsite", Boolean.toString(true));
        map.put("readrights", "everybody");
        map.put("writerights", "everybody");
        map.put("pid", "hdl:SOMETHING/00-0000-0000-0000-0000-1");
        map.put("accesslevel", "1");
        template.update(insertString, map);

        insertString = "INSERT INTO corpusnodes (nodeid, nodetype, format, name, title) "
                + "VALUES (:nodeid, :nodetype, :format, :name, :title);";
        map.clear();
        map.put("nodeid", "1");
        map.put("nodetype", "2");
        map.put("format", "text/cmdi");
        map.put("name", "Node");
        map.put("title", "Node CMDIfied IMDI");
        template.update(insertString, map);

        insertString = "INSERT INTO corpusstructure (nodeid, canonical, valid, vpath0, vpath) "
                + "VALUES (:nodeid, :canonical, :valid, :vpath0, :vpath);";
        map.clear();
        map.put("nodeid", "1");
        map.put("canonical", "t");
        map.put("valid", "t");
        map.put("vpath0", "0");
        map.put("vpath", "MPI0#");
        template.update(insertString, map);
    }
    
    private void insertDataInAmsDB() {
        
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(this.amsDataSource);
        
        String insertString = "INSERT INTO \"principal\" (id, uid, name, nature, host_institute, host_srv, creator, created_on, lastmodifier, last_mod_on) "
                + "VALUES (:id, :uid, :name, :nature, :host_institute, :host_srv, :creator, :created_on, :creator, :created_on);";
        Map<String, String> map = new HashMap<String, String>();
        map.put("id", "1");
        map.put("uid", "testUser");
        map.put("name", "TestUser");
        map.put("nature", "USR");
        map.put("host_institute", "MPINLA");
        map.put("host_srv", "ams2/ldap");
        map.put("creator", "1");
        map.put("created_on", new Timestamp(Calendar.getInstance().getTimeInMillis()).toString());
        template.update(insertString, map);

        insertString = "INSERT INTO \"user\" "
                + "VALUES (:id, :firstname, :email, :organisation, :address, :passwd, :creator, :created_on, :creator, :created_on);";
        map.clear();
        map.put("id", "1");
        map.put("firstname", "Test");
        map.put("email", "testUser@mpi.nl");
        map.put("organisation", "mpi 4 psycholinguistics");
        map.put("address", "wundtlaan 1\n6525 xd nijmegen\nthe netherlands");
        map.put("passwd", "{CRYPT}.GDJxqz1Ipii6");
        map.put("creator", "1");
        map.put("created_on", new Timestamp(Calendar.getInstance().getTimeInMillis()).toString());
        template.update(insertString, map);
        
        insertString = "INSERT INTO \"principal\" (id, uid, name, nature, host_institute, host_srv, creator, created_on, lastmodifier, last_mod_on) "
                + "VALUES (:id, :uid, :name, :nature, :host_institute, :host_srv, :creator, :created_on, :creator, :created_on);";
        map.clear();
        map.put("id", "-1");
        map.put("uid", "everybody");
        map.put("name", "-== EVERYBODY ==-");
        map.put("nature", "ALL");
        map.put("host_institute", "MPINLA");
        map.put("host_srv", "ams2/ldap");
        map.put("creator", "1");
        map.put("created_on", new Timestamp(Calendar.getInstance().getTimeInMillis()).toString());
        template.update(insertString, map);
        
        insertString = "INSERT INTO \"principal\" (id, uid, name, nature, host_institute, host_srv, creator, created_on, lastmodifier, last_mod_on) "
                + "VALUES (:id, :uid, :name, :nature, :host_institute, :host_srv, :creator, :created_on, :creator, :created_on);";
        map.clear();
        map.put("id", "-2");
        map.put("uid", "anyAuthenticatedUser");
        map.put("name", "-== REGISTERED USERS ==-");
        map.put("nature", "ALL");
        map.put("host_institute", "MPINLA");
        map.put("host_srv", "ams2/ldap");
        map.put("creator", "1");
        map.put("created_on", new Timestamp(Calendar.getInstance().getTimeInMillis()).toString());
        template.update(insertString, map);
        
        insertString = "INSERT INTO \"node_principal\" (id, node_id, pcpl_id, creator, created_on, lastmodifier, last_mod_on) "
                + "VALUES (:id, :node_id, :pcpl_id, :creator, :created_on, :creator, :created_on);";
        map.clear();
        map.put("id", "1");
        map.put("node_id", "1");
        map.put("pcpl_id", "1");
        map.put("creator", "1");
        map.put("created_on", new Timestamp(Calendar.getInstance().getTimeInMillis()).toString());
        template.update(insertString, map);
        
        insertString = "INSERT INTO \"rule\" VALUES(:id, :species, :realm, :competence, :disposition, :name,  :creator, :created_on, :creator, :created_on);";
        map.clear();
        map.put("id", "120");
        map.put("species", "DE");
        map.put("realm", "0110");
        map.put("competence", "0010");
        map.put("disposition", "-40");
        map.put("name", "Domain Editor");
        map.put("creator", "1");
        map.put("created_on", new Timestamp(Calendar.getInstance().getTimeInMillis()).toString());
        template.update(insertString, map);

        insertString = "INSERT INTO \"nodepcpl_rule\" (id, node_pcpl_id, rule_id, nature, priority, max_storage_mb, used_storage_mb, creator, created_on, lastmodifier, last_mod_on) "
                + "VALUES (:id, :node_pcpl_id, :rule_id, :nature, :priority, :max_storage_mb, :used_storage_mb, :creator, :created_on, :creator, :created_on);";
        map.clear();
        map.put("id", "1");
        map.put("node_pcpl_id", "1");
        map.put("rule_id", "120");
        map.put("nature", "1");
        map.put("priority", "150");
        map.put("max_storage_mb", "50000");
        map.put("used_storage_mb", "10000");
        map.put("creator", "1");
        map.put("created_on", new Timestamp(Calendar.getInstance().getTimeInMillis()).toString());
        template.update(insertString, map);        
    }
        
//    private void initialiseWorkspaceDao() {
//        workspaceDao = new LamusJdbcWorkspaceDao();
//        workspaceDao.setDataSource(lamusDataSource);
//    }
    
    private void insertWorkspaceInDB(int workspaceID, String userID) throws FileNotFoundException, IOException {

        String currentDate = new Timestamp(Calendar.getInstance().getTimeInMillis()).toString();
        int topNodeID = 1;
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + workspaceID);
        File testFile = new File(workspaceDirectory, "Node_CMDIfied_IMDI.cmdi");
        
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(this.lamusDataSource);
        
        String insertString = "INSERT INTO workspace (workspace_id, user_id, start_date, session_start_date, status, message) "
                + "VALUES (:workspace_id, :user_id, :start_date, :session_start_date, :status, :message);";
        Map<String, String> map = new HashMap<String, String>();
        map.put("workspace_id", "" + workspaceID);
        map.put("user_id", userID);
        map.put("start_date", currentDate);
        map.put("session_start_date", currentDate);
        map.put("status", WorkspaceStatus.INITIALISED.toString());
        map.put("message", "some message");
        template.update(insertString, map);
        
        insertString = "INSERT INTO node (workspace_node_id, workspace_id, name, type, workspace_url, status, format) "
                + "VALUES (:workspace_node_id, :workspace_id, :name, :type, :workspace_url, :status, :format);";
        map.clear();
        map.put("workspace_node_id", "" + topNodeID);
        map.put("workspace_id", "" + workspaceID);
        map.put("name", "testNode");
        map.put("type", WorkspaceNodeType.METADATA.toString());
        map.put("workspace_url", testFile.toURI().toURL().toString());
        map.put("status", WorkspaceNodeStatus.NODE_ISCOPY.toString());
        map.put("format", "cmdi");
        template.update(insertString, map);
        
        insertString = "UPDATE workspace SET top_node_id = :top_node_id WHERE workspace_id = :workspace_id;";
        map.clear();
        map.put("top_node_id", "" + topNodeID);
        map.put("workspace_id", "" + workspaceID);
        template.update(insertString, map);
        
        workspaceDirectory.mkdirs();
        InputStream testIn = getClass().getClassLoader().getResourceAsStream("Node_CMDIfied_IMDI.cmdi");
        OutputStream testOut = new FileOutputStream(testFile);
        IOUtils.copy(testIn, testOut);
    }
    
    private void cleanLamusDatabaseAndFilesystem() throws IOException {
        
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(this.lamusDataSource);
        
        String deleteNodeLinkSql = "DELETE FROM node_link;";
        String deleteNodeSql = "DELETE FROM node;";
        String deleteWorkspaceSql = "DELETE FROM workspace;";
        
        Map<String, String> map = new HashMap<String, String>();
        
        template.update(deleteNodeLinkSql, map);
        template.update(deleteNodeSql, map);
        template.update(deleteWorkspaceSql, map);
        
        FileUtils.cleanDirectory(this.workspaceBaseDirectory);
        
        this.createdWorkspace = null;
        this.currentUserID = null;
        this.selectedNodeID = -1;
        this.workspaceIDToDelete = -1;
    }
    
    
    
    @Given("an archive")
    public void anArchive() throws IOException {
        
        assertNotNull("archiveFolder null, was not correctly injected", this.archiveFolder);
        assertTrue("archiveFolder does not exist, was not properly created", this.archiveFolder.exists());
        
        copyTestFileToArchiveFolder();
        
        assertNotNull("corpusstructureDataSource null, was not correctly injected", this.corpusstructureDataSource);
        assertNotNull("archiveObjectsDB null, was not correctly injected", this.archiveObjectsDB);
        assertNotNull("corpusStructureDB null, was not correctly injected", this.corpusStructureDB);
        assertTrue("corpusstructure database was not initialised", this.archiveObjectsDB.getStatus());
    }
    
    @Given("a node with ID $nodeID")
    public void aNodeWithID(@Named("nodeID") int nodeID) {
        
        insertDataInCSDB();
        
        Node node = this.corpusStructureDB.getNode(NodeIdUtils.TONODEID(nodeID));
        assertNotNull("Node with ID " + nodeID + " does not exist in the corpusstructure database", node);
        
        this.selectedNodeID = nodeID;
    }
    
    @Given("a user with ID $userID that has read and write access to the node with ID $nodeID")
    public void aUserWithID(String userID, int nodeID) {
        
        assertNotNull("amsDataSource null, was not correctly injected", this.amsDataSource);
        insertDataInAmsDB();
        
        assertNotNull("Principal with ID " + userID + " is null", this.ams2Bridge.getPrincipalSrv().getPrincipal(userID));
        assertTrue("Principal with ID " + userID + " has no write access to the node with ID " + nodeID, this.ams2Bridge.hasWriteAccess(userID, NodeIdUtils.TONODEID(nodeID)));
        
        this.currentUserID = userID;
    }
    
    @Given("a workspace with ID $workspaceID created by user with ID $userID")
    public void aWorkspaceWithIDCreatedByUserWithID(int workspaceID, String userID) throws FileNotFoundException, IOException {

        this.currentUserID = userID;
        this.workspaceIDToDelete = workspaceID;
        
        insertWorkspaceInDB(workspaceID, userID);

        //filesystem
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + workspaceID);
        assertTrue("Workspace directory for workspace " + workspaceID + " should have been created", workspaceDirectory.exists());
        
        // database
        Workspace retrievedWorkspace = this.workspaceDao.getWorkspace(workspaceID);
        assertNotNull("retrievedWorkspace null, was not properly created in the database", retrievedWorkspace);
    }
    
    @When("that user chooses to create a workspace in that node")
    public void thatUserChoosesToCreateAWorkspaceInThatNode() {

        assertNotNull("lamusDataSource null, was not correctly injected", this.lamusDataSource);
        assertNotNull("workspaceDao null, was not correctly injected", this.workspaceDao);

        this.createdWorkspace = workspaceService.createWorkspace(this.currentUserID, this.selectedNodeID);
        assertNotNull("createdWorkspace null just after 'createWorkspace' was called", this.createdWorkspace);
    }
    
    @When("that user chooses to delete the workspace")
    public void thatUserChoosesToDeleteTheWorkspace() {
        
        assertNotNull("lamusDataSource null, was not correctly injected", this.lamusDataSource);
        assertNotNull("workspaceDao null, was not correctly injected", this.workspaceDao);

        this.workspaceService.deleteWorkspace(this.currentUserID, this.workspaceIDToDelete);
    }
    
    @Then("a workspace is created in that node for that user")
    public void aWorkspaceIsCreatedInThatNodeForThatUser() throws InterruptedException {
        
        
        // filesystem
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + this.createdWorkspace.getWorkspaceID());
        assertTrue("Workspace directory for workspace " + this.createdWorkspace.getWorkspaceID() + " does not exist", workspaceDirectory.exists());
        
        // database
        Workspace retrievedWorkspace = this.workspaceDao.getWorkspace(this.createdWorkspace.getWorkspaceID());
        WorkspaceNode retrievedWorkspaceTopNode = this.workspaceDao.getWorkspaceNode(retrievedWorkspace.getTopNodeID());
        assertNotNull("retrievedWorkspace null, was not properly created in the database", retrievedWorkspace);
        assertEquals("currentUserID (" + this.currentUserID + ") does not match the user ID in the retrieved workspace (" + retrievedWorkspace.getUserID() + ")", this.currentUserID, retrievedWorkspace.getUserID());
        assertEquals("selectedNodeID (" + this.selectedNodeID + ") does not match the ID of the top node in the retrieved workspace (" + retrievedWorkspaceTopNode.getArchiveNodeID() + ")", this.selectedNodeID, retrievedWorkspaceTopNode.getArchiveNodeID());
        
        assertEquals("retrieved workspace does not have the expected status (expected = " + WorkspaceStatus.INITIALISED + "; retrieved = " + retrievedWorkspace.getStatus() + ")", WorkspaceStatus.INITIALISED, retrievedWorkspace.getStatus());
    }
    
    @Then("the workspace is deleted")
    public void theWorkspaceIsDeleted() {
        
        // filesystem
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + this.workspaceIDToDelete);
        assertFalse("Workspace directory for workspace " + this.workspaceIDToDelete + " still exists", workspaceDirectory.exists());
        
        // database
        Workspace retrievedWorkspace = this.workspaceDao.getWorkspace(this.workspaceIDToDelete);
        assertNull("retrievedWorkspace not null, was not properly deleted from the database", retrievedWorkspace);
        Collection<WorkspaceNode> retrievedNodes = this.workspaceDao.getNodesForWorkspace(this.workspaceIDToDelete);
        assertTrue("There should be no nodes associated with the deleted workspace", retrievedNodes.isEmpty());
    }
}
