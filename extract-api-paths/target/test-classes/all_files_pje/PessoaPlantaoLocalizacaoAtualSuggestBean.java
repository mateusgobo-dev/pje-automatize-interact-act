/* $Id: PessoaPlantaoSuggestBean.java 10746 2010-08-12 23:23:46Z jplacerda $ */

package br.com.infox.cliente.component.suggest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.home.PlantaoOficialJusticaHome;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.Plantao;

@Name("pessoaPlantaoLocalizacaoAtualSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class PessoaPlantaoLocalizacaoAtualSuggestBean extends AbstractSuggestBean<Pessoa> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		String ejbql = "select o from Pessoa o inner join o.usuarioLocalizacaoList ul "
				+ "where ul.localizacaoFisica.idLocalizacao = '"
				+ Authenticator.getIdLocalizacaoFisicaAtual()
				+ "' and lower(TO_ASCII(o.nome)) like " + "lower(concat('%', TO_ASCII(:" + INPUT_PARAMETER
				+ "), '%')) " + "order by o.nome";
		return ejbql;
	}

	protected Plantao getSearch() {
		PlantaoOficialJusticaHome ph = (PlantaoOficialJusticaHome) Component.getInstance("plantaoOficialJusticaHome");
		if (ph.getTab().equals("form")) {
			return ph.getInstance();
		}
		return (Plantao) Component.getInstance("plantaoSearch");
	}

	@Override
	protected String getEventSelected() {
		return "pessoaPlantaoSuggEvent";
	}

}
