package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorColegiadoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfLogDistribuicao;
import br.jus.pje.nucleo.enums.TipoDistribuicaoEnum;

@Name(ProcessoTrfLogDistribuicaoHome.NAME)
@Scope(ScopeType.PAGE)
@BypassInterceptors
public class ProcessoTrfLogDistribuicaoHome extends AbstractHome<ProcessoTrfLogDistribuicao> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "processoTrfLogDistribuicaoHome";
	private OrgaoJulgadorManager orgaoJulgadorManager;
	private OrgaoJulgadorColegiadoManager orgaoJulgadorColegiadoManager;
	
	private List<OrgaoJulgador> orgaosJulgadores;
	private List<OrgaoJulgadorColegiado> orgaosJulgadoresColegiados;
	
	public static ProcessoTrfLogDistribuicaoHome instance() {
		return (ProcessoTrfLogDistribuicaoHome) Component.getInstance(NAME);
	}

	public void setarId(ProcessoTrfLogDistribuicao obj) {
		setId(obj.getIdProcessoTrfLog());
		setTab("abaItemLog");
	}

	@SuppressWarnings("unchecked")
	private List<OrgaoJulgador> orgaoJulgadorItens() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from OrgaoJulgador o where o.ativo = true ");
		sb.append("order by o.orgaoJulgadorOrdemAlfabetica");
		Query q = getEntityManager().createQuery(sb.toString());
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgadorCargo> orgaoJulgadorCargoItens() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from OrgaoJulgadorCargo o where o.ativo = true ");
		Query q = getEntityManager().createQuery(sb.toString());
		return q.getResultList();
	}

	public TipoDistribuicaoEnum[] tipoDistribuicaoItens() {
		return TipoDistribuicaoEnum.values();
	}

	@SuppressWarnings("unchecked")
	public ProcessoTrfLogDistribuicao recuperarPorProcesso(ProcessoTrf processoTrf) {
		StringBuilder sb = new StringBuilder();
		sb.append(" select p from ProcessoTrfLogDistribuicao p");
		sb.append(" where p.processoTrf = :processoTrf");
		sb.append(" and p.orgaoJulgador = :orgaoJulgador");
		sb.append(" order by dt_log desc");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setMaxResults(1);
		q.setParameter("processoTrf", processoTrf);
		q.setParameter("orgaoJulgador", processoTrf.getOrgaoJulgador());
		
		ProcessoTrfLogDistribuicao newCall = null;
		List<ProcessoTrfLogDistribuicao> results = q.getResultList();
		if (!results.isEmpty()) {
			newCall = results.get(0);
		}
		return newCall;
	}
	
	/**
	 * Retorna os orgaos julgadores ativos e vigentes de acordo com o colegiado passado por parâmetro.
	 * @param	colegiado
	 * @return	List<OrgaoJulgador> retorna uma lista de órgãos julgadores ativos e vigentes vinculados ao 
	 * 			orgão julgador colegiado passado por parâmetro.
	 */
	private List<OrgaoJulgador> obterOrgaosJulgadoresVinculados(OrgaoJulgadorColegiado colegiado){
		List<OrgaoJulgador> orgaosJulgadores = new ArrayList<OrgaoJulgador>();
		orgaoJulgadorManager = ComponentUtil.getComponent(OrgaoJulgadorManager.NAME);
		orgaosJulgadores = orgaoJulgadorManager.obterOrgaosJulgadoresPorColegiado(colegiado);
		return orgaosJulgadores;
	}
	
	/**
	 * Recupera uma lista de órgãos julgadores colegiados ativos
	 * @return retorna uma lista de órgãos julgadores colegiados ativos
	 */
	public List<OrgaoJulgadorColegiado> obterOrgaosJulgadoresColegiadosItems(){
		List<OrgaoJulgadorColegiado> colegiados = new ArrayList<OrgaoJulgadorColegiado>();
		orgaoJulgadorColegiadoManager = ComponentUtil.getComponent(OrgaoJulgadorColegiadoManager.NAME);
		colegiados = orgaoJulgadorColegiadoManager.getOrgaoJulgadorColegiadoItems();
		return colegiados;
	}

	/**
	 * Método utilizado na view Log da Distribuição, afim de carregar a combo de Órgão Julgador, sempre que a combo
	 * de Órgão Julgador Colegiado tiver seu valor alterado. 
	 * @param colegiado
	 */
	public void orgaosJulgadoresVinculados(OrgaoJulgadorColegiado colegiado){
		orgaosJulgadores = obterOrgaosJulgadoresVinculados(colegiado);
	}
	
	public void limpar(){
		orgaosJulgadores = null;
	}
	
	public List<OrgaoJulgador> getOrgaosJulgadores() {
		if (orgaosJulgadores == null) {
			orgaosJulgadores = orgaoJulgadorItens();
		}
		return orgaosJulgadores;
	}
	
	public List<OrgaoJulgadorColegiado> getOrgaosJulgadoresColegiados() {
		if (orgaosJulgadoresColegiados == null) {
			orgaosJulgadoresColegiados = obterOrgaosJulgadoresColegiadosItems();
		}
		return orgaosJulgadoresColegiados;
	}
}