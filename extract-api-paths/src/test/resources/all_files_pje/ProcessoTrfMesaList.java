package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.component.NumeroProcesso;
import br.com.infox.cliente.component.tree.AssuntoTrfTreeHandler;
import br.com.infox.cliente.component.tree.ClasseJudicialTreeHandler;
import br.com.infox.cliente.home.SessaoHome;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(ProcessoTrfMesaList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class ProcessoTrfMesaList extends EntityList<ProcessoTrf> {

	private String processoParte;
	private String relator;
	private AssuntoTrf assuntoTrf;
	private NumeroProcesso numeroProcesso = new NumeroProcesso();

	public static final String NAME = "processoTrfMesaList";
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "o.idProcessoTrf";

	private static final String R1 = "o.classeJudicial = #{processoTrfMesaList.entity.classeJudicial}";
	private static final String R2 = "o.assuntoTrf = #{processoTrfMesaList.entity.assuntoTrf}";
	private static final String R3 = "exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "where pp.processoTrf.idProcessoTrf = o.idProcessoTrf " + "and lower(to_ascii(pp.pessoa.nome)) like "
			+ "'%' || lower(to_ascii(#{processoTrfMesaList.processoParte})) || '%')";
	private static final String R4 = "exists (select ojc.orgaoJulgador from OrgaoJulgadorCargo ojc, UsuarioLocalizacaoMagistradoServidor pm "
			+ "where ojc.idOrgaoJulgadorCargo = pm.orgaoJulgadorCargo.idOrgaoJulgadorCargo "
			+ "and ojc.orgaoJulgador = o.orgaoJulgador "
			+ "and ojc.cargo = o.cargo "
			+ "and lower(to_ascii(pm.usuarioLocalizacao.usuario.nome)) like "
			+ "'%' || lower(to_ascii(#{processoTrfMesaList.relator})) || '%')";
	/**
	 * Correção de bugs - PJE-JT
	 * 
	 * @author rodrigo -- 15/11/2011 -- [PJE-888][PJE-881] A HQL foi alterada.
	 *         Troquei as chamadas à lista assuntoTrfList por
	 *         processoAssuntoList, pois a lista assuntoTrfList é um atributo
	 *         transiente de ProcessoTrf.
	 */
	private static final String R5 = "o.idProcessoTrf in (select distinct p.idProcessoTrf from ProcessoTrf p "
			+ "inner join p.processoAssuntoList assuntoList "
			+ "where p = o and assuntoList.assuntoTrf = #{processoTrfMesaList.assuntoTrf})";
	/**
	 * PJE-JT: Fim
	 */

	private static final String R6 = "o.numeroSequencia = #{processoTrfMesaList.numeroProcesso.numeroSequencia}";
	private static final String R7 = "o.numeroDigitoVerificador = #{processoTrfMesaList.numeroProcesso.numeroDigitoVerificador}";
	private static final String R8 = "o.ano = #{processoTrfMesaList.numeroProcesso.ano}";
	private static final String R9 = "o.numeroOrigem = #{processoTrfMesaList.numeroProcesso.numeroOrigem}";
	private static final String R10 = "o.orgaoJulgador = #{processoTrfMesaList.entity.orgaoJulgador}";

	@Override
	protected void addSearchFields() {
		addSearchField("classeJudicial", SearchCriteria.igual, R1);
		addSearchField("assuntoTrf", SearchCriteria.igual, R2);
		addSearchField("idProcessoTrf", SearchCriteria.igual, R3);
		addSearchField("relator", SearchCriteria.igual, R4);
		addSearchField("assuntoTrf", SearchCriteria.igual, R5);
		addSearchField("numeroSequencia", SearchCriteria.contendo, R6);
		addSearchField("numeroDigitoVerificador", SearchCriteria.contendo, R7);
		addSearchField("ano", SearchCriteria.contendo, R8);
		addSearchField("numeroOrigem", SearchCriteria.contendo, R9);
		addSearchField("orgaoJulgador", SearchCriteria.igual, R10);
	}

	/**
	 * Exibe processos que tenham:
	 * - a classe não exige pauta
	 * - uma movimentação de conclusão
	 * - sejam do mesmo OJC da sessão
	 * - tenham sido selecionados para julgamento
	 * - não estejam pautados em uma sessão ainda não finalizada
	 * - se o usuário logado é de um OJ, mostra apenas processos de relatoria ou de revisão desse OJ
	 */
	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoTrf o ");
		sb.append("WHERE o.classeJudicial.pauta = false ");
		sb.append(" AND o.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = #{sessaoHome.instance.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado} ");
		sb.append(" AND o.idProcessoTrf in (select c.processo.idProcesso from ProcessoEvento c where c.ativo = true and c.evento = #{parametroUtil.eventoConclusao}) ");
		sb.append(" AND o.selecionadoJulgamento = true ");
		sb.append(" AND o.orgaoJulgador.instancia in ('2','3') ");
		sb.append(" AND not exists ");
		sb.append("	   (select b.processoTrf from SessaoPautaProcessoTrf b ");
		sb.append("		where b.processoTrf = o and b.dataExclusaoProcessoTrf is null and b.sessao.dataRealizacaoSessao IS NULL ");
		sb.append("		and b.sessao.dataExclusao IS NULL) ");
		
		if(SessaoHome.instance().getInstance().getContinua()){
			sb.append(" AND o.classeJudicial.sessaoContinua = true ");
		}

		OrgaoJulgador orgaoJulgadorAtual = Authenticator.getOrgaoJulgadorAtual();
		OrgaoJulgadorColegiado orgaoJulgadorColegiadoAtual = Authenticator.getOrgaoJulgadorColegiadoAtual();
		if (orgaoJulgadorColegiadoAtual != null) {
			sb.append(" AND o.orgaoJulgadorColegiado = #{authenticator.getOrgaoJulgadorColegiadoAtual()} ");
		}
		if (orgaoJulgadorAtual != null) {
			sb.append(" AND (o.orgaoJulgador.idOrgaoJulgador = #{authenticator.getIdOrgaoJulgadorAtual()} ");
			sb.append(" OR ( exists (select rpt.processoTrf from ");
			sb.append("		RevisorProcessoTrf rpt ");
			sb.append("		where rpt.processoTrf = o ");
			sb.append("		and rpt.orgaoJulgadorRevisor = #{orgaoJulgadorAtual} ");
			sb.append("		and rpt.dataFinal is null) and o.revisado = true) ");
			sb.append(" OR (o.orgaoJulgadorRevisor.idOrgaoJulgador = #{authenticator.getIdOrgaoJulgadorAtual()} AND o.exigeRevisor = true )) ");
		}
		return sb.toString();
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("classeJudicial", "o.classeJudicial.classeJudicial");
		map.put("orgaoJulgador", "o.orgaoJulgador.orgaoJulgador");
		return map;
	}

	public void setProcessoParte(String processoParte) {
		this.processoParte = processoParte;
	}

	public String getProcessoParte() {
		return processoParte;
	}

	public void setRelator(String relator) {
		this.relator = relator;
	}

	public String getRelator() {
		return relator;
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setNumeroProcesso(NumeroProcesso numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public NumeroProcesso getNumeroProcesso() {
		return numeroProcesso;
	}

	private void limparTrees() {
		ClasseJudicialTreeHandler treeClasse = (ClasseJudicialTreeHandler) Component.getInstance("classeJudicialTree");
		AssuntoTrfTreeHandler assuntoTree = (AssuntoTrfTreeHandler) Component.getInstance("assuntoTrfTree");
		treeClasse.clearTree();
		assuntoTree.clearTree();
	}

	@Override
	public void newInstance() {
		setNumeroProcesso(new NumeroProcesso());
		setRelator(new String());
		setProcessoParte(new String());
		setAssuntoTrf(null);
		limparTrees();
		super.newInstance();
	}

}
