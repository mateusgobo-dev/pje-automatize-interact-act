package br.jus.csjt.pje.commons.util;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.utils.Constantes.TIPO_JUSTICA;
import br.com.itx.util.ComponentUtil;

@Name(ParametroJtUtil.NAME)
public class ParametroJtUtil {

	public static final String NAME = "parametroJtUtil";
	
	private String tipoJustica = ParametroUtil.instance().getTipoJustica();
	
	public static ParametroJtUtil instance() {
		return ComponentUtil.getComponent(ParametroJtUtil.NAME);
	}

	@Factory(value = "justicaTrabalho", scope = ScopeType.EVENT)
	public boolean justicaTrabalho() {
		return TIPO_JUSTICA.TRABALHO.equals(tipoJustica) || TIPO_JUSTICA.CONSELHO_SUPERIOR_TRABALHO.equals(tipoJustica);
	}

	@Factory(value = "justicaEleitoral", scope = ScopeType.EVENT)
	public boolean justicaEleitoral() {
		return TIPO_JUSTICA.ELEITORAL.equals(tipoJustica);
	}

	@Factory(value = "justicaMilitar", scope = ScopeType.EVENT)
	public boolean justicaMilitar() {
		return TIPO_JUSTICA.MILITAR_ESTADUAL.equals(tipoJustica);
	}

	@Factory(value = "justicaFederal", scope = ScopeType.EVENT)
	public boolean justicaFederal() {
		return TIPO_JUSTICA.FEDERAL.equals(tipoJustica);
	}

	@Factory(value = "justicaComum", scope = ScopeType.EVENT)
	public boolean justicaComum() {
		return TIPO_JUSTICA.COMUM.equals(tipoJustica);
	}
	
	@Factory(value = "csjt", scope = ScopeType.EVENT)
	public boolean csjt() {
		return TIPO_JUSTICA.CONSELHO_SUPERIOR_TRABALHO.equals(tipoJustica);
	}
	
 	@Factory(value = "cnj", scope = ScopeType.EVENT)
 	public boolean cnj() {
 		return TIPO_JUSTICA.CONSELHO_NACIONAL_JUSTICA.equals(tipoJustica);
 	}

	public String getTipoJustica() {
		return tipoJustica;
	}
	
}
