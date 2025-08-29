package br.com.infox.cliente.util;

import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

public class ProcessoJbpmUtil {

	private ProcessoJbpmUtil() {
	}

	public static ProcessoTrf getProcessoTrf() {
		Integer idProcesso = JbpmUtil.getProcessVariable("processo");
		return EntityUtil.find(ProcessoTrf.class, idProcesso);
	}

}
