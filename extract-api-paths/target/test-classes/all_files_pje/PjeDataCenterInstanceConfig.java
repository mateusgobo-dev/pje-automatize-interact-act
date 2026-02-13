package br.jus.cnj.pje.webservice;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

import javax.inject.Singleton;

import org.jboss.seam.contexts.ServletLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.ProvidedBy;
import com.netflix.appinfo.AbstractInstanceConfig;
import com.netflix.appinfo.DataCenterInfo;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.PropertiesInstanceConfig;
import com.netflix.appinfo.providers.MyDataCenterInstanceConfigProvider;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.pje.nucleo.util.StringUtil;

@Singleton
@ProvidedBy(MyDataCenterInstanceConfigProvider.class)
public class PjeDataCenterInstanceConfig extends PropertiesInstanceConfig implements EurekaInstanceConfig {

    private static final Logger logger = LoggerFactory.getLogger(AbstractInstanceConfig.class);
    
    private String instanceId;
	
    public PjeDataCenterInstanceConfig() {
    }

    public PjeDataCenterInstanceConfig(String namespace) {
        super(namespace);
    }

    public PjeDataCenterInstanceConfig(String namespace, DataCenterInfo dataCenterInfo) {
        super(namespace, dataCenterInfo);
    }
    
    @Override
    public String getInstanceId() {
    	return this.instanceId;
    }
    
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	
	@Override
	public String getAppname() {
		return ConfiguracaoIntegracaoCloud.getAppName();
	}
	
	@Override
	public String getVirtualHostName() {
		return ConfiguracaoIntegracaoCloud.getAppName();
	}
	
	@Override
	public String getSecureVirtualHostName() {
		return ConfiguracaoIntegracaoCloud.getAppName();
	}
	
	@Override
	public boolean isNonSecurePortEnabled() {
		return ConfiguracaoIntegracaoCloud.isEurekaClientNonSecurePortEnabled();
	}
	
	@Override
	public boolean getSecurePortEnabled() {
		return ConfiguracaoIntegracaoCloud.isEurekaClientSecurePortEnabled();
	}

	@Override
	public int getSecurePort() {
		return Integer.parseInt(ConfiguracaoIntegracaoCloud.getEurekaClientSecurePort());
	}
	
	@Override
	public int getNonSecurePort() {
		return Integer.parseInt(ConfiguracaoIntegracaoCloud.getEurekaClientNonSecurePort());
	}
	
	@Override
	public String getHomePageUrl() {
		StringBuilder sb = new StringBuilder("");

		sb.append(this.getProtocol());
		sb.append("://");
		sb.append(this.getHostName(false));
		if(this.getPort() >= 0) {
			sb.append(":");
			sb.append(this.getPort());
		}
		sb.append(this.getHomePageUrlPath());

		return sb.toString();
	}
	
	@Override
	public String getHomePageUrlPath() {
		return ServletLifecycle.getCurrentServletContext().getContextPath();
	}
	
	@Override
	public String getHealthCheckUrlPath() {
		return "/seam/resource/rest/pje-legacy/status/health";
	}
	
	@Override
	public String getHealthCheckUrl() {
		return this.getHomePageUrl() + this.getHealthCheckUrlPath();
	}
	
	@Override
	public String getStatusPageUrlPath() {
		return "/seam/resource/rest/pje-legacy/status/info";
	}
	
	@Override
	public String getStatusPageUrl() {
		return this.getHomePageUrl() + this.getStatusPageUrlPath();
	}
	
    @Override
    public String getHostName(boolean refresh) {
		String ret = new String("");
		
		if(!StringUtil.isEmpty(ConfiguracaoIntegracaoCloud.getEurekaClientHostname())) {
			ret = ConfiguracaoIntegracaoCloud.getEurekaClientHostname();
		} else {
			Boolean useIp = ConfiguracaoIntegracaoCloud.isRegistraComIp();
			try {
				if (!useIp) {
					ret = super.getHostName(refresh);
				} else {
					ret = this.findFirstNonLoopbackAddress().getHostAddress();
				}
			} catch (Exception e) {
				logger.error("Cannot get host info", e);
			}			
		}

		return ret;
    }
    
    @Override
    public String getIpAddress() {
    	String ret = "";
    	try {
	    	if(!StringUtil.isEmpty(ConfiguracaoIntegracaoCloud.getEurekaClientHostname())) {
				InetAddress address = InetAddress.getByName(this.getHostName(false));
				ret = address.getHostAddress();
	    	} else {
	    		ret = super.getIpAddress();
	    	}
    	} catch (UnknownHostException e) {
    		ret = super.getIpAddress();
    	}
    	return ret;
    }
    
	public InetAddress findFirstNonLoopbackAddress() {
		InetAddress result = null;
		try {
			int lowest = Integer.MAX_VALUE;
			for (Enumeration<NetworkInterface> nics = NetworkInterface
					.getNetworkInterfaces(); nics.hasMoreElements();) {
				NetworkInterface ifc = nics.nextElement();
				if (ifc.isUp()) {
					logger.trace("Testing interface: " + ifc.getDisplayName());
					if (ifc.getIndex() < lowest || result == null) {
						lowest = ifc.getIndex();
					}
					else if (result != null) {
						continue;
					}

					for (Enumeration<InetAddress> addrs = ifc
							.getInetAddresses(); addrs.hasMoreElements();) {
						InetAddress address = addrs.nextElement();
						if (address instanceof Inet4Address
								&& !address.isLoopbackAddress()) {
							logger.trace("Found non-loopback interface: "
									+ ifc.getDisplayName());
							result = address;
						}
					}
				}
			}
		}
		catch (IOException ex) {
			logger.error("Cannot get first non-loopback address", ex);
		}

		if (result != null) {
			return result;
		}

		try {
			return InetAddress.getLocalHost();
		}
		catch (UnknownHostException e) {
			logger.warn("Unable to retrieve localhost");
		}

		return null;
	} 
	
	private String getProtocol() {
		String protocol = "http";
		
		if(this.getSecurePortEnabled()) {
			protocol = "https";
		}
		
		return protocol;
	}
	
	private Integer getPort() {
		Integer port = (Integer)this.getNonSecurePort() != null ? this.getNonSecurePort() : 8080; 
		
		if(this.getSecurePortEnabled()) {
			port = (Integer)this.getSecurePort() != null ? this.getSecurePort() : 443;
		}
		
		return port;		
	}

}