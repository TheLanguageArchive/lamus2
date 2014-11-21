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
package nl.mpi.lamus.util.implementation;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author guisil
 */
public class MockableURL {
    
    private URL url;
    
    public MockableURL(URL url) {
        this.url = url;
    }
    
    public URLConnection openConnection() throws IOException {
        return url.openConnection();
    }
    
    public URL getURL() {
        return url;
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return url.equals(((MockableURL)obj).getURL());
    }
}
