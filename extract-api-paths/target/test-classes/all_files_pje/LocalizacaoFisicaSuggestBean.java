package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.Localizacao;

@Name("localizacaoFisicaSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class LocalizacaoFisicaSuggestBean extends AbstractSuggestBean<Localizacao> {	
    private static final long serialVersionUID = 4376637547740125114L;

    @Override
	public String getEjbql() {
    	Integer idLocalizacaoFisica = Authenticator.getIdLocalizacaoFisicaAtual();
		StringBuilder sb = new StringBuilder();
		sb.append("select o from Localizacao o, Localizacao p ");
		sb.append(" WHERE p.faixaInferior IS NOT NULL AND o.faixaInferior IS NOT NULL ");
		sb.append(" AND p.faixaInferior <= o.faixaInferior ");
		sb.append(" AND p.faixaSuperior >= o.faixaSuperior ");
		sb.append(" AND p.idLocalizacao = " + idLocalizacaoFisica);
		sb.append(" AND o.ativo = TRUE ");
		sb.append(" AND LOWER(o.localizacao) LIKE LOWER(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) ");
		sb.append(" ORDER BY o.localizacao");
		return sb.toString();
	}

}