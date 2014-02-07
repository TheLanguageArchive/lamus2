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
@Profile(value = {"production", "cmdi-adapter-csdb"})
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
    
    @Value("${crawler_hostname}")
    private String crawlerHostName;
    @Bean
    @Qualifier("crawlerHostName")
    public String crawlerHostName() {
        return crawlerHostName;
    }
    
    @Value("${crawler_domainname}")
    private String crawlerDomainName;
    @Bean
    @Qualifier("crawlerDomainName")
    public String crawlerDomainName() {
        return crawlerDomainName;
    }
    
    @Value("${crawler_amsurl}")
    private String crawlerAmsUrl;
    @Bean
    @Qualifier("crawlerAmsUrl")
    public String crawlerAmsUrl() {
        return crawlerAmsUrl;
    }
    
    @Value("${crawler_mdsurl}")
    private String crawlerMdsUrl;
    @Bean
    @Qualifier("crawlerMdsUrl")
    public String crawlerMdsUrl() {
        return crawlerMdsUrl;
    }
    
    @Value("${crawler_dbdriverclassname}")
    private String crawlerDbDriverClassName;
    @Bean
    @Qualifier("crawlerDbDriverClassName")
    public String crawlerDbDriverClassName() {
        return crawlerDbDriverClassName;
    }
    
    @Value("${crawler_dburl}")
    private String crawlerDbUrl;
    @Bean
    @Qualifier("crawlerDbUrl")
    public String crawlerDbUrl() {
        return crawlerDbUrl;
    }
    
    @Value("${crawler_dbmaxactive}")
    private String crawlerDbMaxActive;
    @Bean
    @Qualifier("crawlerDbMaxActive")
    public String crawlerDbMaxActive() {
        return crawlerDbMaxActive;
    }
    
    @Value("${crawler_dbmaxwait}")
    private String crawlerDbMaxWait;
    @Bean
    @Qualifier("crawlerDbMaxWait")
    public String crawlerDbMaxWait() {
        return crawlerDbMaxWait;
    }
    
    @Value("${crawler_dbtestonborrow}")
    private String crawlerDbTestOnBorrow;
    @Bean
    @Qualifier("crawlerDbTestOnBorrow")
    public String crawlerDbTestOnBorrow() {
        return crawlerDbTestOnBorrow;
    }
    
    @Value("${crawler_dbusername}")
    private String crawlerDbUsername;
    @Bean
    @Qualifier("crawlerDbUsername")
    public String crawlerDbUsername() {
        return crawlerDbUsername;
    }
    
    @Value("${crawler_dbpassword}")
    private String crawlerDbPassword;
    @Bean
    @Qualifier("crawlerDbPassword")
    public String crawlerDbPassword() {
        return crawlerDbPassword;
    }
    
    @Value("${crawler_connectiondrivername}")
    private String crawlerConnectionDriverName;
    @Bean
    @Qualifier("crawlerConnectionDriverName")
    public String crawlerConnectionDriverName() {
        return crawlerConnectionDriverName;
    }
    
    @Value("${crawler_hdlproxydomain}")
    private String crawlerHdlProxyDomain;
    @Bean
    @Qualifier("crawlerHdlProxyDomain")
    public String crawlerHdlProxyDomain() {
        return crawlerHdlProxyDomain;
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
    
}
