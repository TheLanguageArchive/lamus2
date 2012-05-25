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
package nl.mpi.lamus.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Configuration
@PropertySource(value="classpath:application.properties")
public class LamusProperties {
    
    // otherwise the properties don't get automatically injected with the Value annotations
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
    
    
    @Value("${default_max_storage_space_in_megabytes}")
    private long defaultMaxStorageSpaceInMegabytes;
    @Bean
    public long defaultMaxStorageSpaceInBytes() {
        return defaultMaxStorageSpaceInMegabytes * 1024 * 1024;
    }
    
    @Value("${days_of_inactivity_allowed_since_last_session}")
    private int numberOfDaysOfInactivityAllowedSinceLastSession;
    @Bean
    public int numberOfDaysOfInactivityAllowedSinceLastSession() {
        return numberOfDaysOfInactivityAllowedSinceLastSession;
    }
    
    @Value("${total_number_of_days_allowed_until_expiry}")
    private int totalNumberOfDaysAllowedUntilExpiry;
    @Bean
    public int totalNumberOfDaysAllowedUntilExpiry() {
        return totalNumberOfDaysAllowedUntilExpiry;
    }
    
    @Value("${number_of_days_of_inactivity_allowed_since_last_warning_email}")
    private int numberOfDaysOfInactivityAllowedSinceLastWarningEmail;
    @Bean
    public int numberOfDaysOfInactivityAllowedSinceLastWarningEmail() {
        return numberOfDaysOfInactivityAllowedSinceLastWarningEmail;
    }
    
    @Value("${type_recheck_size_limit_in_megabytes}")
    private long typeRecheckSizeLimitInMegabytes;
    @Bean
    public long typeRecheckSizeLimitInBytes() {
        return typeRecheckSizeLimitInMegabytes * 1024 * 1024;
    }
    
    @Value("${max_directory_name_length}")
    private int maxDirectoryNameLength;
    @Bean
    public int maxDirectoryNameLength() {
        return maxDirectoryNameLength;
    }
    
    @Value("${corpus_directory_base_name}")
    private String corpusDirectoryBaseName;
    @Bean
    public String corpusDirectoryBaseName() {
        return corpusDirectoryBaseName;
    }
    
    @Value("${orphans_directory_base_name}")
    private String orphansDirectoryBaseName;
    @Bean
    public String orphansDirectoryBaseName() {
        return orphansDirectoryBaseName;
    }
    
    @Value("${workspace_base_directory}")
    private String workspaceBaseDirectory;
    @Bean
    public File workspaceBaseDirectory() {
        return new File(workspaceBaseDirectory);
    }
    
    @Value("${custom_typechecker_config_files_and_folders}")
    private String customTypecheckerFoldersAndConfigFiles;
    @Bean
    public Map<File, File> customTypecheckerFolderToConfigFileMap() {
        
        //TODO Check the validity of the string (with regular expressions, for instance)
        
        Map<File, File> mapToReturn = new HashMap<File, File>();
        
        String[] foldersAndConfigFilesArray = customTypecheckerFoldersAndConfigFiles.split(";");
        for(String foldersAndConfigFile : foldersAndConfigFilesArray) {
            String[] foldersAndConfigFileSeparated = foldersAndConfigFile.split("=");
            if(foldersAndConfigFileSeparated.length == 2) {
                String configFileValue = foldersAndConfigFileSeparated[1];
                File configFile = new File(configFileValue);
                String[] foldersKey = foldersAndConfigFileSeparated[0].split(",");
                if(foldersKey.length > 0) {
                    for(String folderKey : foldersKey) {
                        File folder = new File(folderKey);
                        mapToReturn.put(folder, configFile);
                    }
                }
            }
        }
        
        return mapToReturn;
    }
}
