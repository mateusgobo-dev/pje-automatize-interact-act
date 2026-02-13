package br.com.infox.utils;

import br.com.infox.cliente.home.ProcessoTrfHome;

public class ExpressionsUtil {

	
	public static void processoTrfHomeExpressions(Integer idProcesso) {
		
		ProcessoTrfHome.instance().setId(idProcesso);
		
	}
}
