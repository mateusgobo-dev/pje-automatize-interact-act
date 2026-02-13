package br.jus.cnj.pje.nucleo;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ConfiguracaoIntegracaoCloudTest {
	
	@Test
	public void test1_appNameNotNull() {
		String appName = ConfiguracaoIntegracaoCloud.getAppName();
		
		assertNotNull(appName);
	}
}
