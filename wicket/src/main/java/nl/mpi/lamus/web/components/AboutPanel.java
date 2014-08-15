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
package nl.mpi.lamus.web.components;

import java.io.IOException;
import java.util.Properties;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author guisil
 */
public class AboutPanel extends Panel {
    
    private static final Logger logger = LoggerFactory.getLogger(AboutPanel.class);
    
    public AboutPanel(String id) {
        super(id);
        
        String version = "";
        String build = "";
        String date = "";
        String year = "";
        
        try {
            Properties versionProps = new Properties();
            versionProps.load(this.getClass().getResource("/build.properties").openStream());

            version = versionProps.getProperty("version");
            build = versionProps.getProperty("build");
            date = versionProps.getProperty("date");
            year = versionProps.getProperty("year");
            
        } catch(IOException ex) {
            logger.error("Could not retrieve build properties", ex);
        }
        
        add(new Label("version", version));
        add(new Label("build", build));
        add(new Label("date", date));
        add(new Label("year", year));
    }
}
