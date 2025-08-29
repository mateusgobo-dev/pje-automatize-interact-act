package br.com.infox.cliente.component.suggest;

import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.component.ControleFiltros;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.filters.ProcessoTrfFilter;

@Name("associacaoProcessoSuggest")
@Scope(ScopeType.CONVERSATION)
public class AssociacaoProcessoSuggestBean extends AbstractSuggestBean<ProcessoTrf> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoTrf o where o.processoStatus = 'D'");
		sb.append(" and o.idProcessoTrf != '");
		sb.append(ProcessoTrfHome.instance().getInstance().getIdProcessoTrf());
		sb.append("' ");
		Object variavel = ComponentUtil.getTramitacaoProcessualService().recuperaVariavelTarefa(Variaveis.PJE_FLUXO_ASSOCIACAO_PROCESSOS_BLOQUEAR_OJS_DIFERENTES);
		if (variavel != null && Boolean.parseBoolean((variavel).toString())) {
			sb.append(" and o.orgaoJulgador.idOrgaoJulgador = ");
			sb.append(ProcessoTrfHome.instance().getInstance().getOrgaoJulgador().getIdOrgaoJulgador());
		}

		sb.append(" and o.processo.numeroProcesso like concat(:");
		sb.append(INPUT_PARAMETER);
		sb.append(", '%')) order by o.processo.numeroProcesso");

		return sb.toString();
	}

	@Override
	public List<ProcessoTrf> suggestList(Object typed) {
		ControleFiltros controleFiltros = (ControleFiltros) Component.getInstance(ControleFiltros.class, true);

		HibernateUtil.disableFilters(ProcessoTrfFilter.FILTER_LOCALIZACAO_SERVIDOR);
		HibernateUtil.disableFilters(ProcessoTrfFilter.FILTER_ORGAO_JULGADOR_COLEGIADO);

		UsuarioLocalizacaoMagistradoServidor usrLocMagistrado = EntityUtil.getEntityManager().find(UsuarioLocalizacaoMagistradoServidor.class, Authenticator.getIdUsuarioLocalizacaoAtual());
		Integer idOrgaoJulgadorColegiado = null;
		Boolean visualizaSigiloso = Authenticator.isVisualizaSigiloso();
		List<Integer> idsLocalizacoesFisicasList = Authenticator.getIdsLocalizacoesFilhasAtuaisList();
		if(usrLocMagistrado != null){
			idOrgaoJulgadorColegiado = usrLocMagistrado.getOrgaoJulgadorColegiado() != null ? usrLocMagistrado.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado() : 0;
		}else{
			idOrgaoJulgadorColegiado = 0;
		}
		
		
		boolean isServidorExclusivoOJC = Authenticator.isServidorExclusivoColegiado();		
		controleFiltros.setarFiltroSegredoSigiloEntidade(ProcessoTrfFilter.FILTER_SEGREDO_JUSTICA, 
				Authenticator.getIdUsuarioLogado(), visualizaSigiloso, idsLocalizacoesFisicasList, idOrgaoJulgadorColegiado, isServidorExclusivoOJC);
		
		List<ProcessoTrf> list = super.suggestList(typed);
		
		controleFiltros.setarFiltroLocalizacaoServidorEntidade(ProcessoTrfFilter.FILTER_LOCALIZACAO_SERVIDOR, 
				idsLocalizacoesFisicasList, idOrgaoJulgadorColegiado, isServidorExclusivoOJC);
		controleFiltros.setarFiltroOrgaoJulgadorColegiado(ProcessoTrfFilter.FILTER_ORGAO_JULGADOR_COLEGIADO, idOrgaoJulgadorColegiado);

		return list;
	}

}
