/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.web.spring;

import nl.mpi.lamus.web.LamusWicketApplication;
import nl.mpi.lamus.web.session.LamusSessionFactory;
import org.apache.wicket.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author guisil
 */
@Configuration
public class WicketBeans {
    
    @Autowired
    private LamusSessionFactory sessionFactory;
    
    private LamusWicketApplication wicketApplication;
    
    @Bean
    public Application wicketApplication() {
        if(wicketApplication == null) {
            wicketApplication = new LamusWicketApplication(sessionFactory);
        }
        return wicketApplication;
    }
}
