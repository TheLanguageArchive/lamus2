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
package nl.mpi.lamus.spring.production;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 *
 * @author guisil
 */
@Configuration
@PropertySource(value="classpath:crawler.properties")
@Profile(value = {"production"})
public class CrawlerProperties {
    
    // otherwise the properties don't get automatically injected with the Value annotations
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
    
    
    @Value("${hostname}")
    private String hostname;
    @Bean
    @Qualifier("crawler_hostname")
    public String hostname() {
        return hostname;
    }
    
    @Value("${domainname}")
    private String domainname;
    @Bean
    @Qualifier("crawler_domainname")
    public String domainname() {
        return domainname;
    }
    
    @Value("${prefixes}")
    private String prefixes;
    @Bean
    @Qualifier("crawler_prefixes")
    public String prefixes() {
        return prefixes;
    }
    
    @Value("${amsurl}")
    private String amsurl;
    @Bean
    @Qualifier("crawler_amsurl")
    public String amsurl() {
        return amsurl;
    }
    
    @Value("${mdsurl}")
    private String mdsurl;
    @Bean
    @Qualifier("crawler_mdsurl")
    public String mdsurl() {
        return mdsurl;
    }
    
    @Value("${httproot}")
    private String httproot;
    @Bean
    @Qualifier("crawler_httproot")
    public String httproot() {
        return httproot;
    }
    
    @Value("${localroot}")
    private String localroot;
    @Bean
    @Qualifier("crawler_localroot")
    public String localroot() {
        return localroot;
    }

    
    @Value("${dbdriverclassname}")
    private String dbdriverclassname;
    @Bean
    @Qualifier("crawler_dbdriverclassname")
    public String dbdriverclassname() {
        return dbdriverclassname;
    }
    
    @Value("${dburl}")
    private String dburl;
    @Bean
    @Qualifier("crawler_dburl")
    public String dburl() {
        return dburl;
    }
    
    @Value("${dbmaxactive}")
    private String dbmaxactive;
    @Bean
    @Qualifier("crawler_dbmaxactive")
    public String dbmaxactive() {
        return dbmaxactive;
    }
    
    @Value("${dbmaxwait}")
    private String dbmaxwait;
    @Bean
    @Qualifier("crawler_dbmaxwait")
    public String dbmaxwait() {
        return dbmaxwait;
    }
    
    @Value("${dbtestonborrow}")
    private String dbtestonborrow;
    @Bean
    @Qualifier("crawler_dbtestonborrow")
    public String dbtestonborrow() {
        return dbtestonborrow;
    }
    
    @Value("${dbusername}")
    private String dbusername;
    @Bean
    @Qualifier("crawler_dbusername")
    public String dbusername() {
        return dbusername;
    }
    
    @Value("${dbpassword}")
    private String dbpassword;
    @Bean
    @Qualifier("crawler_dbpassword")
    public String dbpassword() {
        return dbpassword;
    }
    
    @Value("${connectiondrivername}")
    private String connectiondrivername;
    @Bean
    @Qualifier("crawler_connectiondrivername")
    public String connectiondrivername() {
        return connectiondrivername;
    }
    
    @Value("${hdlproxydomain}")
    private String hdlproxydomain;
    @Bean
    @Qualifier("crawler_hdlproxydomain")
    public String hdlproxydomain() {
        return hdlproxydomain;
    }
}
