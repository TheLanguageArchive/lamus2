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

import javax.servlet.ServletContext;
import nl.mpi.lat.ams.Constants;
import nl.mpi.lat.ams.export.RecalcTriggerService;
import nl.mpi.lat.ams.service.LicenseService;
import nl.mpi.lat.ams.service.RuleService;
import nl.mpi.lat.auth.authorization.AdvAuthorizationService;
import nl.mpi.lat.auth.authorization.export.AuthorizationExportService;
import nl.mpi.lat.auth.principal.PrincipalService;
import nl.mpi.lat.fabric.FabricService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.ServletContextAware;

/**
 * Configuration class containing the beans needed by AMS2.
 * The beans are retrieved from the configuration files provided by AMS2
 * and injected into the Lamus2 context.
 * To be used in production environment.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Configuration
@Profile(value = {"production","cmdi-adapter-csdb","ams-hybrid"})
public class AmsBeans implements ServletContextAware {
    
    private ServletContext servletContext;
    private ClassPathXmlApplicationContext context;
    
    private PrincipalService pcplSrv;
    private AdvAuthorizationService authoSrv;
    private FabricService fabSrv;
    private LicenseService licSrv;
    private RuleService ruleSrv;
    
    private RecalcTriggerService recalcTrSrv;
    private AuthorizationExportService integratedExpSrv;
    private AuthorizationExportService cachedCsDbExpSrv;
    private AuthorizationExportService webserverExpSrv;
    
    
    @Override
    public void setServletContext(ServletContext sc) {
        servletContext = sc;
    }
    
    /**
     * @return PrincipalService bean, to be used by the connection to AMS2
     */
    @Bean
    public PrincipalService principalService() {
        
        initialiseContext();
        if(pcplSrv == null) {
            pcplSrv = context.getBean(Constants.BEAN_PRINCIPAL_SRV, PrincipalService.class);
        }
        return pcplSrv;
    }
    
    /**
     * @return AuthorizationService bean, to be used by the connection to AMS2
     */
    @Bean
    public AdvAuthorizationService authorizationService() {
        
        initialiseContext();
        if(authoSrv == null) {
           authoSrv = context.getBean(Constants.BEAN_AUTHORIZATION_SRV, AdvAuthorizationService.class);
        }
        return authoSrv;
    }
    
    /**
     * @return FabricService bean, to be used by the connection to AMS2
     */
    @Bean
    public FabricService fabricService() {
        
        initialiseContext();
        if(fabSrv == null) {
           fabSrv = context.getBean(Constants.BEAN_FABRIC_SRV, FabricService.class);
        }
        return fabSrv;
    }
    
    /**
     * @return LicenseService bean, to be used by the connection to AMS2
     */
    @Bean
    public LicenseService licenseService() {
        
        initialiseContext();
        if(licSrv == null) {
            licSrv = context.getBean(Constants.BEAN_LICENSE_SRV, LicenseService.class);
        }
        return licSrv;
    }
    
    /**
     * @return RuleService bean, to be used by the connection to AMS2
     */
    @Bean
    public RuleService ruleService() {
        
        initialiseContext();
        if(ruleSrv == null) {
            ruleSrv = context.getBean(Constants.BEAN_RULE_SRV, RuleService.class);
        }
        return ruleSrv;
    }
    
    /**
     * @return RecalcTriggerService bean, to be used by the connection to AMS2
     */
    @Bean
    public RecalcTriggerService recalcTriggerService() {
        
        initialiseContext();
        if(recalcTrSrv == null) {
            recalcTrSrv = context.getBean(Constants.BEAN_RECALC_TRIGGER_SRV, RecalcTriggerService.class);
        }
        return recalcTrSrv;
    }
    
    /**
     * @return IntegratedAuthExportSrv bean, to be used by the connection to AMS2
     */
    @Qualifier("integratedExportSrv")
    @Bean
    public AuthorizationExportService integratedExportService() {
        
        initialiseContext();
        if(integratedExpSrv == null) {
            integratedExpSrv = context.getBean(Constants.BEAN_INTEGRATED_EXPORT_SRV, AuthorizationExportService.class);
        }
        return integratedExpSrv;
    }
    
    /**
     * @return CachedCorpusDbExportSrv bean, to be used by the connection to AMS2
     */
    @Qualifier("cachedCorpusDbExportSrv")
    @Bean
    public AuthorizationExportService cachedCorpusDbExportService() {
        
        initialiseContext();
        if(cachedCsDbExpSrv == null) {
            cachedCsDbExpSrv = context.getBean(Constants.BEAN_CACHED_CORPUS_DB_EXPORT_SRV, AuthorizationExportService.class);
        }
        return cachedCsDbExpSrv;
    }
    
    /**
     * @return ApacheAclExportSrv bean, to be used by the connection to AMS2
     */
    @Qualifier("webserverExportSrv")
    @Bean
    public AuthorizationExportService webserverExportService() {
        
        initialiseContext();
        if(webserverExpSrv == null) {
            webserverExpSrv = context.getBean(Constants.BEAN_WEBSERVER_EXPORT_SRV, AuthorizationExportService.class);
        }
        return webserverExpSrv;
    }
    
    
    private void initialiseContext() {
        
        if(context == null) {
            context = new ClassPathXmlApplicationContext();
            
            String activeProfilesString = servletContext.getInitParameter("spring.profiles.active");
            String[] activeProfilesArray = activeProfilesString.split(",");
            for(int i = 0; i < activeProfilesArray.length; i++) {
                activeProfilesArray[i] = activeProfilesArray[i].trim();
            }
            
            context.getEnvironment().setActiveProfiles(activeProfilesArray);
            context.setConfigLocation("spring-ams2-core.xml");
            context.refresh();
        }
    }
}
