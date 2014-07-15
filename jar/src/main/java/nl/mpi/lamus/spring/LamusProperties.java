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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Configuration
@PropertySource(value="classpath:application.properties")
@Profile(value = {"production", "cmdi-adapter-csdb", "demoserver"})
public class LamusProperties {
    
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
    
    @Value("${workspace_base_directory}")
    private String workspaceBaseDirectory;
    @Bean
    @Qualifier("workspaceBaseDirectory")
    public File workspaceBaseDirectory() {
        return new File(workspaceBaseDirectory);
    }
    
    @Value("${workspace_upload_directory_name}")
    private String workspaceUploadDirectoryName;
    @Bean
    @Qualifier("workspaceUploadDirectoryName")
    public String workspaceUploadDirectoryName() {
        return workspaceUploadDirectoryName;
    }
//    @Bean
//    @Qualifier("workspaceUploadDirectory")
//    public File workspaceUploadDirectory() {
//        return new File(workspaceBaseDirectory, workspaceUploadDirectoryName);
//    }

    @Value("${metadata_directory_name}")
    private String metadataDirectoryName;
    @Bean
    @Qualifier("metadataDirectoryName")
    public String metadataDirectoryName() {
        return metadataDirectoryName;
    }
    
    @Value("${resources_directory_name}")
    private String resourcesDirectoryName;
    @Bean
    @Qualifier("resourcesDirectoryName")
    public String resourcesDirectoryName() {
        return resourcesDirectoryName;
    }
    
    
    @Value("${trashcan_base_directory}")
    private String trashCanBaseDirectory;
    @Bean
    @Qualifier("trashCanBaseDirectory")
    public File trashCanBaseDirectory() {
        return new File(trashCanBaseDirectory);
    }
    
    @Value("${versioning_base_directory}")
    private String versioningBaseDirectory;
    @Bean
    @Qualifier("versioningBaseDirectory")
    public File versioningBaseDirectory() {
        return new File(versioningBaseDirectory);
    }
    
    @Value("${custom_typechecker_config_files_and_folders}")
    private String customTypecheckerFoldersAndConfigFiles;
    @Bean
    @Qualifier("customTypecheckerFolderToConfigFileMap")
    public Map<String, String> customTypecheckerFolderToConfigFileMap() {
        
        //TODO Check the validity of the string (with regular expressions, for instance)
        
        Map<String, String> mapToReturn = new HashMap<String, String>();
        
        String[] foldersAndConfigFilesArray = customTypecheckerFoldersAndConfigFiles.split(";");
        for(String foldersAndConfigFile : foldersAndConfigFilesArray) {
            String[] foldersAndConfigFileSeparated = foldersAndConfigFile.split("=");
            if(foldersAndConfigFileSeparated.length == 2) {
                String configFileValue = foldersAndConfigFileSeparated[1];
//                File configFile = new File(configFileValue);
                String[] foldersKey = foldersAndConfigFileSeparated[0].split(",");
                if(foldersKey.length > 0) {
                    for(String folderKey : foldersKey) {
//                        File folder = new File(folderKey);
//                        mapToReturn.put(folder, configFile);
                        mapToReturn.put(folderKey, configFileValue);
                    }
                }
            }
        }
        
        return mapToReturn;
    }

        
    // crawler properties
    
    @Value("${db_httproot}")
    private String dbHttpRoot;
    @Bean
    @Qualifier("dbHttpRoot")
    public String dbHttpRoot() {
        return dbHttpRoot;
    }
    
    @Value("${db_localroot}")
    private String dbLocalRoot;
    @Bean
    @Qualifier("dbLocalRoot")
    public String dbLocalRoot() {
        return dbLocalRoot;
    }
    
    
    //handle properties
    
    @Value("${handle_prefix}")
    private String handlePrefix;
    @Bean
    @Qualifier("handlePrefix")
    public String handlePrefix() {
        return handlePrefix;
    }
    
    @Value("${handle_proxy}")
    private String handleProxy;
    @Bean
    @Qualifier("handleProxy")
    public String handleProxy() {
        return handleProxy;
    }
    
    @Value("${handle_admin_key_file}")
    private String handleAdminKeyFile;
    @Bean
    @Qualifier("handleAdminKeyFile")
    public String handleAdminKeyFile() {
        return handleAdminKeyFile;
    }
    
    @Value("${handle_admin_user_handle}")
    private String handleAdminUserHandle;
    @Bean
    @Qualifier("handleAdminUserHandle")
    public String handleAdminUserHandle() {
        return handleAdminUserHandle;
    }
    
    @Value("${handle_admin_user_handle_index}")
    private String handleAdminUserHandleIndex;
    @Bean
    @Qualifier("handleAdminUserHandleIndex")
    public String handleAdminUserHandleIndex() {
        return handleAdminUserHandleIndex;
    }
    
    @Value("${handle_admin_handle_password}")
    private String handleAdminHandlePassword;
    @Bean
    @Qualifier("handleAdminHandlePassword")
    public String handleAdminHandlePassword() {
        return handleAdminHandlePassword;
    }
    
    @Value("${corpusstructure_service_location}")
    private String corpusStructureServiceLocation;
    @Bean
    @Qualifier("corpusStructureServiceLocation")
    public String corpusStructureServiceLocation() {
        return corpusStructureServiceLocation;
    }
    
    @Value("${corpusstructure_service_versioning_path}")
    private String corpusStructureServiceVersioningPath;
    @Bean
    @Qualifier("corpusStructureServiceVersioningPath")
    public String corpusStructureServiceVersioningPath() {
        return corpusStructureServiceVersioningPath;
    }
    
    @Value("${corpusstructure_service_version_creation_path}")
    private String corpusStructureServiceVersionCreationPath;
    @Bean
    @Qualifier("corpusStructureServiceVersionCreationPath")
    public String corpusStructureServiceVersionCreationPath() {
        return corpusStructureServiceVersionCreationPath;
    }
    
    @Value("${corpusstructure_service_crawler_path}")
    private String corpusStructureServiceCrawlerPath;
    @Bean
    @Qualifier("corpusStructureServiceCrawlerPath")
    public String corpusStructureServiceCrawlerPath() {
        return corpusStructureServiceCrawlerPath;
    }
    
    @Value("${corpusstructure_service_crawler_start_path}")
    private String corpusStructureServiceCrawlerStartPath;
    @Bean
    @Qualifier("corpusStructureServiceCrawlerStartPath")
    public String corpusStructureServiceCrawlerStartPath() {
        return corpusStructureServiceCrawlerStartPath;
    }
    
    @Value("${corpusstructure_service_crawler_details_path}")
    private String corpusStructureServiceCrawlerDetailsPath;
    @Bean
    @Qualifier("corpusStructureServiceCrawlerDetailsPath")
    public String corpusStructureServiceCrawlerDetailsPath() {
        return corpusStructureServiceCrawlerDetailsPath;
    }
    
    @Value("${mail_server}")
    private String mailServer;
    @Bean
    @Qualifier("mailServer")
    public String mailServer() {
        return mailServer;
    }
    
    @Value("${mail_from_address}")
    private String mailFromAddress;
    @Bean
    @Qualifier("mailFromAddress")
    public String mailFromAddress() {
        return mailFromAddress;
    }
    
    @Value("${manager_users}")
    private String managerUsers;
    @Bean
    @Qualifier("managerUsers")
    public Collection<String> managerUsers() {
        
        Collection<String> collectionToReturn = new ArrayList<String>();
        
        String[] usernames = managerUsers.split(",");
        if(usernames.length > 0) {
            for(String username : usernames) {
                collectionToReturn.add(username);
            }
        }
        
        return collectionToReturn;
    }
}
