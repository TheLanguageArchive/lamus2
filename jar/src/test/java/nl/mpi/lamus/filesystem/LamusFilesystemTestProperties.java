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
package nl.mpi.lamus.filesystem;

import java.io.File;
import java.io.IOException;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Configuration
@Profile("testing")
public class LamusFilesystemTestProperties {
    
    @Bean
    @Qualifier("workspaceBaseDirectory")
    public File workspaceBaseDirectory() throws IOException {
        TemporaryFolder testFolder = new TemporaryFolder();
        testFolder.create();
        File baseDirectory = testFolder.newFolder("workspace_base_directory");
        return baseDirectory;
    }
}
