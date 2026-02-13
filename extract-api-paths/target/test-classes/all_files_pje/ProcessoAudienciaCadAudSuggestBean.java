package br.com.infox.cliente.component.suggest;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * Suggest utilizado no cadastro de processoAudiência
 * 
 * @author MarlonAssis
 * 
 */
@Name("processoAudienciaCadAudSuggest")
@BypassInterceptors
public class ProcessoAudienciaCadAudSuggestBean extends AbstractSuggestBean<ProcessoTrf> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {

		int idLocalizacao = Authenticator.getIdLocalizacaoFisicaAtual();

		return "select o from ProcessoTrf o "
				+ "where lower(TO_ASCII(o.processo.numeroProcesso)) like lower(concat('%',TO_ASCII(:" + INPUT_PARAMETER
				+ "), '%')) and " + "o.orgaoJulgador.localizacao.idLocalizacao = " + idLocalizacao
				+ " order by o.processo.numeroProcesso";
	}

}
