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
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ConsultaProcessoAdiadoVista;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;

@Name(ProcessoAdiadoVistaPVList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ProcessoAdiadoVistaPVList extends EntityList<ConsultaProcessoAdiadoVista> {

	public static final String NAME = "processoAdiadoVistaPVList";

	private static final long serialVersionUID = 1L;

	private AssuntoTrf assuntoTrf;
	private ClasseJudicial classeJudicial;
	private OrgaoJulgador orgaoJulgadorCombo;
	private String nomeParte;
	private String relator;
	private NumeroProcesso numeroProcesso = new NumeroProcesso();
	private Boolean continua;

	private static final String DEFAULT_ORDER = "processoTrf.idProcessoTrf";

	private static final String R1 = "o.processoTrf.classeJudicial = #{processoAdiadoVistaPVList.classeJudicial}";

	private static final String R2 = "o.processoTrf.idProcessoTrf in (select distinct p.idProcessoTrf from ProcessoTrf p "
			+ "inner join p.processoAssuntoList a "
			+ "where o = p and "
			+ "a.assuntoTrf = #{processoAdiadoVistaPVList.assuntoTrf})";

	private static final String R3 = "exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "where pp.processoTrf.idProcessoTrf = o.idProcessoTrf " + "and lower(to_ascii(pp.pessoa.nome)) like "
			+ "'%' || lower(to_ascii(#{processoAdiadoVistaPVList.nomeParte})) || '%')";

	private static final String R4 = " o.processoTrf.orgaoJulgador in (select ojc.orgaoJulgador from OrgaoJulgadorCargo ojc, UsuarioLocalizacaoMagistradoServidor pm "
			+ "where ojc.idOrgaoJulgadorCargo = pm.orgaoJulgadorCargo.idOrgaoJulgadorCargo "
			+ "and ojc.orgaoJulgador = o.processoTrf.orgaoJulgador "
			+ "and ojc.cargo = o.processoTrf.cargo "
			+ "and lower(to_ascii(pm.usuarioLocalizacao.usuario.nome)) like "
			+ "'%' || lower(to_ascii(#{processoAdiadoVistaPVList.relator})) || '%')";

	private static final String R5 = "o.processoTrf.numeroSequencia = #{processoAdiadoVistaPVList.numeroProcesso.numeroSequencia}";

	private static final String R6 = "o.processoTrf.numeroDigitoVerificador = #{processoAdiadoVistaPVList.numeroProcesso.numeroDigitoVerificador}";

	private static final String R7 = "o.processoTrf.ano = #{processoAdiadoVistaPVList.numeroProcesso.ano}";

	private static final String R8 = "o.processoTrf.numeroOrigem = #{processoAdiadoVistaPVList.numeroProcesso.numeroOrigem}";

	private static final String R9 = "o.processoTrf.numeroOrgaoJustica = #{processoAdiadoVistaPVList.numeroProcesso.numeroOrgaoJustica}";

	private static final String R10 = "o.processoTrf.orgaoJulgador = #{processoAdiadoVistaPVList.orgaoJulgadorCombo}";
	
	private static final String R11 = "o.sessaoPautaProcessoTrf.sessao.continua = #{processoAdiadoVistaPVList.continua}";

	@Override
	protected void addSearchFields() {
		addSearchField("processoTrf.classeJudicial", SearchCriteria.igual, R1);
		addSearchField("processoTrf.assuntoTrf", SearchCriteria.contendo, R2);
		addSearchField("processoTrf.nomeParte", SearchCriteria.contendo, R3);
		addSearchField("sessaoPautaProcessoTrf.processoTrf.nomeParte", SearchCriteria.igual, R4);
		addSearchField("numeroSequencia", SearchCriteria.contendo, R5);
		addSearchField("numeroDigitoVerificador", SearchCriteria.contendo, R6);
		addSearchField("ano", SearchCriteria.contendo, R7);
		addSearchField("numeroOrigem", SearchCriteria.contendo, R8);
		addSearchField("numeroOrgaoJustica", SearchCriteria.igual, R9);
		addSearchField("processoTrf.orgaoJulgador", SearchCriteria.igual, R10);
		addSearchField("sessaoPautaProcessoTrf.sessao.continua", SearchCriteria.igual, R11);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("numeroProcesso", "sessaoPautaProcessoTrf.processoTrf.processo.numeroProcesso");
		map.put("classeJudicial", "sessaoPautaProcessoTrf.processoTrf.classeJudicial");
		map.put("orgaoJulgadorPedidoVista", "sessaoPautaProcessoTrf.orgaoJulgadorPedidoVista");
		return map;
	}

	@Override
	protected String getDefaultEjbql() {
		OrgaoJulgadorColegiado orgaoJulgadorColegiadoAtual = Authenticator.getOrgaoJulgadorColegiadoAtual();
		OrgaoJulgador orgaoJulgadorAtual = Authenticator.getOrgaoJulgadorAtual();
		
		StringBuilder squery = new StringBuilder();
		squery.append("SELECT o FROM ConsultaProcessoAdiadoVista o ");
		squery.append("WHERE o.sessaoPautaProcessoTrf.orgaoJulgadorPedidoVista IS NOT NULL ");
		
		// Aba Pedido de vista: Processos com situação de Pedido de vista nas sessões de julgamento na situação Realizada ou Finalizada;
		squery.append("AND EXISTS (SELECT s.processoTrf FROM SessaoPautaProcessoTrf s ");
		squery.append("WHERE s.processoTrf = o.processoTrf AND s.dataExclusaoProcessoTrf IS NULL ");
		squery.append("AND s.julgamentoFinalizado is true or (s.sessao.dataRealizacaoSessao IS NOT NULL ");
		squery.append("AND (s.sessao.dataRegistroEvento is not null OR s.sessao.dataFechamentoSessao is not null))) ");
		
		if(orgaoJulgadorColegiadoAtual != null){
			squery.append("AND o.processoTrf.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = ");
			squery.append(orgaoJulgadorColegiadoAtual.getIdOrgaoJulgadorColegiado());
		} else if (orgaoJulgadorAtual != null){
			squery.append("AND o.sessaoPautaProcessoTrf.orgaoJulgadorPedidoVista.idOrgaoJulgador = ");
			squery.append(orgaoJulgadorAtual.getIdOrgaoJulgador());
		} 
		squery.append(" AND o.adiadoVista = 'PV' ");
		limitaProcessosPorTipoSessao(squery);
		return squery.toString();
	}
	
	private void limitaProcessosPorTipoSessao(StringBuilder query){
		if(SessaoHome.instance().getInstance().getContinua()){
			query.append("AND o.sessaoPautaProcessoTrf.sessao.continua IS true ");
		}else{
			query.append("AND o.sessaoPautaProcessoTrf.sessao.continua IS false ");
		}
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setRelator(String relator) {
		this.relator = relator;
	}

	public String getRelator() {
		return relator;
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
		setNomeParte(new String());
		setRelator(new String());
		setOrgaoJulgadorCombo(null);
		setClasseJudicial(null);
		setAssuntoTrf(null);
		limparTrees();
		super.newInstance();
	}

	public void setOrgaoJulgadorCombo(OrgaoJulgador orgaoJulgadorCombo) {
		this.orgaoJulgadorCombo = orgaoJulgadorCombo;
	}

	public OrgaoJulgador getOrgaoJulgadorCombo() {
		return orgaoJulgadorCombo;
	}

	public Boolean getContinua() {
		return continua;
	}

	public void setContinua(Boolean continua) {
		this.continua = continua;
	}
}