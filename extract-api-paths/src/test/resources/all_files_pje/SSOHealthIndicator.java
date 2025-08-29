package br.jus.cnj.pje.status;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;

public class SSOHealthIndicator extends AbstractHealthIndicator{

	protected Client client;
	
	protected WebTarget webTarget;

	private static boolean isDisabled = false;

	private static LocalDateTime lastHealthCheck = null;

	private static final Logger logger = LoggerFactory.getLogger(SSOHealthIndicator.class);

	@Override
	public Health doHealthCheck() {

		if (ConfiguracaoIntegracaoCloud.getSSOAuthenticationEnabled()) {
			if (lastHealthCheck != null) {
				long minutosSSODesabilitado = ChronoUnit.MINUTES.between(lastHealthCheck, LocalDateTime.now());

				minutosSSODesabilitado = minutosSSODesabilitado < 0 ? 0 : minutosSSODesabilitado;

				if (isDisabled
						&& minutosSSODesabilitado < ConfiguracaoIntegracaoCloud.getSSOFalhaDesabilitadoMinutos()) {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

					final String errorMessage = "Problema de comunicação entre o pje-legacy e o serviço SSO. Desativando o uso do SSO por '"
							+ ConfiguracaoIntegracaoCloud.getSSOFalhaDesabilitadoMinutos() + "' minuto(s) até "
							+ lastHealthCheck.plusMinutes(ConfiguracaoIntegracaoCloud.getSSOFalhaDesabilitadoMinutos())
									.format(formatter)
							+ ".";
					this.getDetails().put("error", errorMessage);
					this.setHealth(new Health(Status.DOWN, this.getDetails()));
					logger.error(errorMessage);
					return this.getHealth();
				}
			}

			lastHealthCheck = LocalDateTime.now();

			CloseableHttpClient httpclient = HttpClients.custom().disableAutomaticRetries().build();
			HttpGet httpget = new HttpGet(this.getSSOPath());
			CloseableHttpResponse resp = null;
			
			try{
				resp = httpclient.execute(httpget);
				if(resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
					this.getDetails().put("success", "O pje-legacy consegue se comunicar com o serviço SSO.");
					this.setHealth(new Health(Status.UP, this.getDetails()));
					isDisabled = false;
				} else {
					this.getDetails().put("error", "Problema de comunicação entre o pje-legacy e o serviço SSO.");
					this.setHealth(new Health(Status.DOWN, this.getDetails()));
					isDisabled = true;
				}
			} catch (Exception e) {
				this.getDetails().put("error", "Problema de comunicação entre o pje-legacy e o serviço SSO.");
				this.setHealth(new Health(Status.DOWN, this.getDetails()));
				isDisabled = true;
			} finally {
				try {
					if(resp != null) {
						resp.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}			
		} else {
			this.getDetails().put("out of service", "O pje-legacy não possui integração com o SSO habilitada.");
			this.setHealth(new Health(Status.OUT_OF_SERVICE, this.getDetails()));
		}
	
		return this.getHealth();
	}
	
	private String getSSOPath(){
		return ConfiguracaoIntegracaoCloud.getSSOUrl();
	}

}
