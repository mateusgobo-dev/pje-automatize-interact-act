package br.com.infox.cliente.component.suggest;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.service.LocalizacaoService;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaOficialJustica;

@Name("oficialJusticaSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class OficialJusticaSuggestBean extends AbstractSuggestBean<PessoaOficialJustica> {

	private static final long serialVersionUID = 3150192197019918759L;

	@Override
	public String getEjbql() {
		StringBuilder query = new StringBuilder();
		
		query.append("SELECT DISTINCT pessoaOfJus FROM PessoaOficialJustica pessoaOfJus ");
		query.append(" INNER JOIN pessoaOfJus.pessoa pf ");
		query.append(" INNER JOIN pessoaOfJus.usuarioLocalizacaoList ul WHERE ");
		query.append(" LOWER(TO_ASCII(pf.nome)) LIKE ");
		query.append("		LOWER(concat('%', TO_ASCII(:");
		query.append( 		INPUT_PARAMETER );
		query.append("	), '%')) ");
		query.append(" AND bitwise_and(pf.especializacoes, ");
		query.append(	PessoaFisica.OFJ );
		query.append( ") = ");
		query.append( PessoaFisica.OFJ );
		if (!Authenticator.isPapelAdministrador()) {
			query.append("AND ul.localizacaoFisica.idLocalizacao IN (");
			query.append(obterIdsLocalizacoesTodosOsNiveis());
			query.append(")");
		}
		query.append(" ORDER BY pessoaOfJus.nome");
		return query.toString();
	}

	/**
	 * Metodo que recupera os ID's das localizacoes filhas e ancestrais da localizacao atual e retornar uma string dos ID's concatenados por
	 * virgulas (','). 
	 * @return String contendo os ID's das localizacoes concatenados.
	 */
	private String obterIdsLocalizacoesTodosOsNiveis() {
		StringBuilder localizacoesIdsConcatenadas = new StringBuilder();
		List<Integer> idsLocalizacoes = new ArrayList<Integer>();
		LocalizacaoService localizacaoService = ComponentUtil.getComponent("localizacaoService");
		
		idsLocalizacoes.addAll(localizacaoService.getTreeIdsList(Authenticator.getUsuarioLocalizacaoAtual().getLocalizacaoFisica()));
		idsLocalizacoes.addAll(localizacaoService.obterIdsAncestrais(Authenticator.getUsuarioLocalizacaoAtual().getLocalizacaoFisica().getIdLocalizacao()));
		
		localizacoesIdsConcatenadas.append(StringUtils.join(idsLocalizacoes,","));
		return localizacoesIdsConcatenadas.toString();
	}

}