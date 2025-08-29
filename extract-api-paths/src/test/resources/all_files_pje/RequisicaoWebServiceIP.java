package br.jus.cnj.pje.integracao;

public class RequisicaoWebServiceIP {
	
	private String ip;
	
	public RequisicaoWebServiceIP(String ip) {
		this.ip = ip;
	}
	
	public boolean validar(String... ips){
		//for (String ipPermitido : ips) {
		//	if (StringUtil.isIPv4valido(ipPermitido) && ipPermitido.equals(this.ip)){
				return true;
		//	}
		//}
		//return false;
	}

}
