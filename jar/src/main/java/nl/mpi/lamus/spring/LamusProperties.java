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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.context.ServletContextAware;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Configuration
@PropertySource(value="classpath:application.properties")
@Profile(value = {"production", "cmdi-adapter-csdb", "demoserver"})
public class LamusProperties implements ServletContextAware {
    
    private ServletContext servletContext;
    
    
    @Override
    public void setServletContext(ServletContext sc) {
        servletContext = sc;
    }
    
    
    // otherwise the properties don't get automatically injected with the Value annotations
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
    
    
    @Value("${default_max_storage_space_in_megabytes}")
    private long defaultMaxStorageSpaceInMegabytes;
    @Bean
    @Qualifier("defaultMaxStorageSpaceInBytes")
    public long defaultMaxStorageSpaceInBytes() {
        return defaultMaxStorageSpaceInMegabytes * 1024 * 1024;
    }
    
    @Value("${days_of_inactivity_allowed_since_last_session}")
    private int numberOfDaysOfInactivityAllowedSinceLastSession;
    @Bean
    @Qualifier("numberOfDaysOfInactivityAllowedSinceLastSession")
    public int numberOfDaysOfInactivityAllowedSinceLastSession() {
        return numberOfDaysOfInactivityAllowedSinceLastSession;
    }
    
    @Value("${total_number_of_days_allowed_until_expiry}")
    private int totalNumberOfDaysAllowedUntilExpiry;
    @Bean
    @Qualifier("totalNumberOfDaysAllowedUntilExpiry")
    public int totalNumberOfDaysAllowedUntilExpiry() {
        return totalNumberOfDaysAllowedUntilExpiry;
    }
    
    @Value("${number_of_days_of_inactivity_allowed_since_last_warning_email}")
    private int numberOfDaysOfInactivityAllowedSinceLastWarningEmail;
    @Bean
    @Qualifier("numberOfDaysOfInactivityAllowedSinceLastWarningEmail")
    public int numberOfDaysOfInactivityAllowedSinceLastWarningEmail() {
        return numberOfDaysOfInactivityAllowedSinceLastWarningEmail;
    }
    
    @Value("${type_recheck_size_limit_in_megabytes}")
    private long typeRecheckSizeLimitInMegabytes;
    @Bean
    @Qualifier("typeRecheckSizeLimitInBytes")
    public long typeRecheckSizeLimitInBytes() {
        return typeRecheckSizeLimitInMegabytes * 1024 * 1024;
    }
    
    @Value("${max_directory_name_length}")
    private int maxDirectoryNameLength;
    @Bean
    @Qualifier("maxDirectoryNameLength")
    public int maxDirectoryNameLength() {
        return maxDirectoryNameLength;
    }
    
    @Value("${corpus_directory_base_name}")
    private String corpusDirectoryBaseName;
    @Bean
    @Qualifier("corpusDirectoryBaseName")
    public String corpusDirectoryBaseName() {
        return corpusDirectoryBaseName;
    }
    
    @Value("${orphans_directory_base_name}")
    private String orphansDirectoryBaseName;
    @Bean
    @Qualifier("orphansDirectoryBaseName")
    public String orphansDirectoryBaseName() {
        return orphansDirectoryBaseName;
    }
    
    
    @Value("${custom_typechecker_config_files_and_folders}")
    private String customTypecheckerFoldersAndConfigFiles;
    @Bean
    @Qualifier("customTypecheckerFolderToConfigFileMap")
    public Map<String, String> customTypecheckerFolderToConfigFileMap() {
        
        //TODO Check the validity of the string (with regular expressions, for instance)
        
        Map<String, String> mapToReturn = new HashMap<>();
        
        String[] foldersAndConfigFilesArray = customTypecheckerFoldersAndConfigFiles.split(";");
        for(String foldersAndConfigFile : foldersAndConfigFilesArray) {
            String[] foldersAndConfigFileSeparated = foldersAndConfigFile.split("=");
            if(foldersAndConfigFileSeparated.length == 2) {
                String configFileValue = foldersAndConfigFileSeparated[1];
                String[] foldersKey = foldersAndConfigFileSeparated[0].split(",");
                if(foldersKey.length > 0) {
                    for(String folderKey : foldersKey) {
                        mapToReturn.put(folderKey, configFileValue);
                    }
                }
            }
        }
        
        return mapToReturn;
    }
    
    @Value("${disallowed_folder_names_workspace}")
    private String disallowedFolderNamesWorkspace;
    @Bean
    @Qualifier("disallowedFolderNamesWorkspace")
    public Collection<String> disallowedFolderNamesWorkspace() {
        return splitStringIntoCollectionOfStrings(disallowedFolderNamesWorkspace);
    }
    
    @Value("${disallowed_folder_names_archive}")
    private String disallowedFolderNamesArchive;
    @Bean
    @Qualifier("disallowedFolderNamesArchive")
    public Collection<String> disallowedFolderNamesArchive() {
        return splitStringIntoCollectionOfStrings(disallowedFolderNamesArchive);
    }
    
    private Collection<String> splitStringIntoCollectionOfStrings(String stringToSplit) {
        Collection<String> collectionOfStrings = new ArrayList<>();
        
        String[] stringsArray = disallowedFolderNamesWorkspace.split(",");
        for(String string : stringsArray) {
            if(!string.isEmpty()) {
                collectionOfStrings.add(string);
            }
        }
        return collectionOfStrings;
    }

    
    // Properties loaded from the web server context
    
    @Bean
    @Qualifier("registerUrl")
    public String registerUrl() {
        return servletContext.getInitParameter("nl.mpi.rrsUrl") + servletContext.getInitParameter("nl.mpi.rrsRegister");
    }
    
    @Bean
    @Qualifier("manualUrl")
    public String manualUrl() {
        return servletContext.getInitParameter("nl.mpi.lamus.lamus2_manualUrl");
    }
    
    @Bean
    @Qualifier("workspaceBaseDirectory")
    public File workspaceBaseDirectory() {
        return new File(servletContext.getInitParameter("nl.mpi.lamus.workspace_base_directory"));
    }
    
    @Bean
    @Qualifier("workspaceUploadDirectoryName")
    public String workspaceUploadDirectoryName() {
        return servletContext.getInitParameter("nl.mpi.lamus.workspace_upload_directory_name");
    }
    
    @Bean
    @Qualifier("metadataDirectoryName")
    public String metadataDirectoryName() {
        return servletContext.getInitParameter("nl.mpi.lamus.metadata_directory_name");
    }
    
    @Bean
    @Qualifier("resourcesDirectoryName")
    public String resourcesDirectoryName() {
        return servletContext.getInitParameter("nl.mpi.lamus.resources_directory_name");
    }
    
    @Bean
    @Qualifier("trashCanBaseDirectory")
    public File trashCanBaseDirectory() {
        return new File(servletContext.getInitParameter("nl.mpi.lamus.trashcan_base_directory"));
    }
    
    @Bean
    @Qualifier("versioningBaseDirectory")
    public File versioningBaseDirectory() {
        return new File(servletContext.getInitParameter("nl.mpi.lamus.versioning_base_directory"));
    }
    
    
    @Bean
    @Qualifier("dbHttpRoot")
    public String dbHttpRoot() {
        return servletContext.getInitParameter("nl.mpi.lamus.db.httproot");
    }

    @Bean
    @Qualifier("dbHttpsRoot")
    public String dbHttpsRoot() {
        return servletContext.getInitParameter("nl.mpi.lamus.db.httpsroot");
    }

    @Bean
    @Qualifier("dbLocalRoot")
    public String dbLocalRoot() {
        return servletContext.getInitParameter("nl.mpi.lamus.db.localroot");
    }
    
    
    @Bean
    @Qualifier("handlePrefix")
    public String handlePrefix() {
        return servletContext.getInitParameter("nl.mpi.lamus.handle.prefix");
    }
    
    @Bean
    @Qualifier("handleProxy")
    public String handleProxy() {
        return servletContext.getInitParameter("nl.mpi.lamus.handle.proxy");
    }
    
    @Bean
    @Qualifier("handleAdminKeyFile")
    public String handleAdminKeyFile() {
        return servletContext.getInitParameter("nl.mpi.lamus.handle.admin_key_file");
    }
    
    @Bean
    @Qualifier("handleAdminUserHandle")
    public String handleAdminUserHandle() {
        return servletContext.getInitParameter("nl.mpi.lamus.handle.admin_user_handle");
    }
    
    @Bean
    @Qualifier("handleAdminUserHandleIndex")
    public String handleAdminUserHandleIndex() {
        return servletContext.getInitParameter("nl.mpi.lamus.handle.admin_user_handle_index");
    }
    
    @Bean
    @Qualifier("handleAdminHandlePassword")
    public String handleAdminHandlePassword() {
        return servletContext.getInitParameter("nl.mpi.lamus.handle.admin_handle_password");
    }
    
    
    @Bean
    @Qualifier("corpusStructureServiceLocation")
    public String corpusStructureServiceLocation() {
        return servletContext.getInitParameter("nl.mpi.lamus.corpusstructure.service_location");
    }
    
    @Bean
    @Qualifier("corpusStructureServiceVersioningPath")
    public String corpusStructureServiceVersioningPath() {
        return servletContext.getInitParameter("nl.mpi.lamus.corpusstructure.service_versioning_path");
    }
    
    @Bean
    @Qualifier("corpusStructureServiceVersionCreationPath")
    public String corpusStructureServiceVersionCreationPath() {
        return servletContext.getInitParameter("nl.mpi.lamus.corpusstructure.service_version_creation_path");
    }
    
    @Bean
    @Qualifier("corpusStructureServiceCrawlerPath")
    public String corpusStructureServiceCrawlerPath() {
        return servletContext.getInitParameter("nl.mpi.lamus.corpusstructure.service_crawler_path");
    }
    
    @Bean
    @Qualifier("corpusStructureServiceCrawlerStartPath")
    public String corpusStructureServiceCrawlerStartPath() {
        return servletContext.getInitParameter("nl.mpi.lamus.corpusstructure.service_crawler_start_path");
    }
    
    @Bean
    @Qualifier("corpusStructureServiceCrawlerDetailsPath")
    public String corpusStructureServiceCrawlerDetailsPath() {
        return servletContext.getInitParameter("nl.mpi.lamus.corpusstructure.service_crawler_details_path");
    }
    
    
    @Bean
    @Qualifier("mailServer")
    public String mailServer() {
        return servletContext.getInitParameter("nl.mpi.lamus.mail.server");
    }
    
    @Bean
    @Qualifier("mailFromAddress")
    public String mailFromAddress() {
        return servletContext.getInitParameter("nl.mpi.lamus.mail.from_address");
    }
    
    @Bean
    @Qualifier("mailBccAddress")
    public String mailBccAddress() {
        return servletContext.getInitParameter("nl.mpi.lamus.mailAddress");
    }
    
    @Bean
    @Qualifier("managerUsers")
    public Collection<String> managerUsers() {
        
        String mUsers = servletContext.getInitParameter("nl.mpi.lamus.manager.users");
        
        Collection<String> collectionToReturn = new ArrayList<>();
        
        String[] usernames = mUsers.split(",");
        if(usernames.length > 0) {
            collectionToReturn.addAll(Arrays.asList(usernames));
        }
        
        return collectionToReturn;
    }
    
    //AMS2
    
    @Bean
    @Qualifier("authBaseUrl")
    public String authBaseUrl() {
        return servletContext.getInitParameter("nl.mpi.auth.cmdi.baseurl");
    }
    
    @Bean
    @Qualifier("authRecalcUrl")
    public String authRecalcUrl() {
        return servletContext.getInitParameter("nl.mpi.auth.recalc.url");
    }
    
    @Bean
    @Qualifier("authRecalcCsdbUrl")
    public String authRecalcCsdbUrl() {
        return servletContext.getInitParameter("nl.mpi.auth.recalc_csdb.url");
    }
    
    @Bean
    @Qualifier("authRecalcWebserverUrl")
    public String authRecalcWebserverUrl() {
        return servletContext.getInitParameter("nl.mpi.auth.recalc_webserver.url");
    }
    
    @Bean
    @Qualifier("authRecalcParam")
    public String authRecalcParam() {
        return servletContext.getInitParameter("nl.mpi.auth.recalc.param");
    }
    
    // Translation Service
    
    @Bean
    @Qualifier("translationServiceLocation")
    public String translationServiceLocation() {
        return servletContext.getInitParameter("nl.mpi.lamus.translation_service.location");
    }
    
    // Filesystem permissions adjustment
    
    @Bean
    @Qualifier("permissionConfigFile")
    public File permissionConfigFile() {
        return new File(servletContext.getInitParameter("nl.mpi.lamus.permissionConfigFile_CMDI"));
    }
    
    
    // Metadata checker
    
    @Bean
    @Qualifier("schematronFile")
    public File schematronFile() {
        return new File(servletContext.getInitParameter("nl.mpi.lamus.schematronValidationFile"));
    }
}
