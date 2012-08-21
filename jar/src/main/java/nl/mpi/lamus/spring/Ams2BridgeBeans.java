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

import nl.mpi.common.util.spring.SpringContextLoader;
import nl.mpi.lat.ams.Constants;
import nl.mpi.lat.ams.service.LicenseService;
import nl.mpi.lat.ams.service.RuleService;
import nl.mpi.lat.auth.authentication.AuthenticationService;
import nl.mpi.lat.auth.authorization.AdvAuthorizationService;
import nl.mpi.lat.auth.principal.PrincipalService;
import nl.mpi.lat.fabric.FabricService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration class containing the beans needed by AMS2.
 * The beans are retrieved from the configuration files provided by AMS2
 * and injected into the Lamus2 context.
 * To be used in production environment.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Configuration
@Profile("production")
public class Ams2BridgeBeans {
    
    private SpringContextLoader contextLoader;
    
    private PrincipalService pcplSrv;
    private AuthenticationService autheSrv;
    private AdvAuthorizationService authoSrv;
    private FabricService fabSrv;
    private LicenseService licSrv;
    private RuleService ruleSrv;
    
    /**
     * Initialises the context loader that will retrieve the beans from the
     * configuration provided by AMS2.
     */
    private void initialiseContextLoader() {
        this.contextLoader = new SpringContextLoader();
        this.contextLoader.init("spring-ams2-core.xml");
    }
    
    /**
     * @return PrincipalService bean, to be used by the connection to AMS2
     */
    @Bean
    public PrincipalService principalService() {
        
        if(this.contextLoader == null) {
            initialiseContextLoader();
        }
        
        if(this.pcplSrv == null) {
            this.pcplSrv = (PrincipalService) this.contextLoader.getBean(Constants.BEAN_PRINCIPAL_SRV);
        }
        return this.pcplSrv;
    }
    
    /**
     * @return AuthenticationService bean, to be used by the connection to AMS2
     */
    @Bean
    @Qualifier("integratedAuthenticationSrv")
    public AuthenticationService authenticationService() {
        
        if(this.contextLoader == null) {
            initialiseContextLoader();
        }
        if(this.autheSrv == null) {
            this.autheSrv = (AuthenticationService) this.contextLoader.getBean(Constants.BEAN_INTEGRATED_AUTHENTICATION_SRV);
        }
        return this.autheSrv;
    }
    
    /**
     * @return AuthorizationService bean, to be used by the connection to AMS2
     */
    @Bean
    public AdvAuthorizationService authorizationService() {
        
        if(this.contextLoader == null) {
            initialiseContextLoader();
        }
        if(this.authoSrv == null) {
            this.authoSrv = (AdvAuthorizationService) contextLoader.getBean(Constants.BEAN_AUTHORIZATION_SRV);
        }
        return this.authoSrv;
    }
    
    /**
     * @return FabricService bean, to be used by the connection to AMS2
     */
    @Bean
    public FabricService fabricService() {
        
        if(this.contextLoader == null) {
            initialiseContextLoader();
        }
        if(this.fabSrv == null) {
            this.fabSrv = (FabricService) contextLoader.getBean(Constants.BEAN_FABRIC_SRV);
        }
        return this.fabSrv;
    }
    
    /**
     * @return LicenseService bean, to be used by the connection to AMS2
     */
    @Bean
    public LicenseService licenseService() {
        
        if(this.contextLoader == null) {
            initialiseContextLoader();
        }
        if(this.licSrv == null) {
            this.licSrv = (LicenseService) contextLoader.getBean(Constants.BEAN_LICENSE_SRV);
        }
        return this.licSrv;
    }
    
    /**
     * @return RuleService bean, to be used by the connection to AMS2
     */
    @Bean
    public RuleService ruleService() {
        
        if(this.contextLoader == null) {
            initialiseContextLoader();
        }
        if(this.ruleSrv == null) {
            this.ruleSrv = (RuleService) contextLoader.getBean(Constants.BEAN_RULE_SRV);
        }
        return this.ruleSrv;
    }
}
