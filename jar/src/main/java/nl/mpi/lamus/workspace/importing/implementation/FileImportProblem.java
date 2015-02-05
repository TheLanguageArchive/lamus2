/*
 * Copyright (C) 2014 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.workspace.importing.implementation;

import nl.mpi.lamus.workspace.importing.implementation.ImportProblem;
import java.io.File;

/**
 *
 * @author guisil
 */
public class FileImportProblem extends ImportProblem {
    
    private File problematicFile;
    
    public FileImportProblem(File problematicFile, String errorMessage, Exception exception) {
        super(errorMessage, exception);
        this.problematicFile = problematicFile;
    }
    
    public File getProblematicFile() {
        return problematicFile;
    }
}
