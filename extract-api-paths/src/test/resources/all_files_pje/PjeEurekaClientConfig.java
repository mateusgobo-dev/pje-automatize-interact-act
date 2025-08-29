package br.jus.cnj.pje.webservice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.netflix.discovery.DefaultEurekaClientConfig;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;

public class PjeEurekaClientConfig extends DefaultEurekaClientConfig{
	
	@Override
	public List<String> getEurekaServerServiceUrls(String myZone) {
        String serviceUrls = ConfiguracaoIntegracaoCloud.getUrlEurekaServer();
        
        if (serviceUrls != null) {
            return Arrays.asList(serviceUrls.split(","));
        }

        return new ArrayList<String>();
	}
}
