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

@Name(ProcessoAdiadoVistaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ProcessoAdiadoVistaList extends EntityList<ConsultaProcessoAdiadoVista> {

	public static final String NAME = "processoAdiadoVistaList";

	private static final long serialVersionUID = 1L;

	private AssuntoTrf assuntoTrf;
	private ClasseJudicial classeJudicial;
	private OrgaoJulgador orgaoJulgadorCombo;
	private String nomeParte;
	private String relator;
	private NumeroProcesso numeroProcesso = new NumeroProcesso();
	private Boolean continua;

	private static final String DEFAULT_ORDER = "idProcessoTrf";

	private static final String R1 = "o.processoTrf.classeJudicial = #{processoAdiadoVistaList.classeJudicial}";

	private static final String R2 = "o.processoTrf.idProcessoTrf in (select distinct p.idProcessoTrf from ProcessoTrf p "
			+ "inner join p.processoAssuntoList a "
			+ "where o = p and "
			+ "a.assuntoTrf = #{processoAdiadoVistaList.assuntoTrf})";

	private static final String R3 = "exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "where pp.processoTrf.idProcessoTrf = o.idProcessoTrf " + "and lower(to_ascii(pp.pessoa.nome)) like "
			+ "'%' || lower(to_ascii(#{processoAdiadoVistaList.nomeParte})) || '%')";
	
	private static final String R4 = " o.processoTrf.orgaoJulgador in (select ojc.orgaoJulgador from OrgaoJulgadorCargo ojc, UsuarioLocalizacaoMagistradoServidor pm "
			+ "where ojc.idOrgaoJulgadorCargo = pm.orgaoJulgadorCargo.idOrgaoJulgadorCargo "
			+ "and ojc.orgaoJulgador = o.processoTrf.orgaoJulgador "
			+ "and ojc.cargo = o.processoTrf.cargo "
			+ "and lower(to_ascii(pm.usuarioLocalizacao.usuario.nome)) like "
			+ "'%' || lower(to_ascii(#{processoAdiadoVistaList.relator})) || '%')";
	
	private static final String R5 = "o.processoTrf.numeroSequencia = #{processoAdiadoVistaList.numeroProcesso.numeroSequencia}";
	private static final String R6 = "o.processoTrf.ano = #{processoAdiadoVistaList.numeroProcesso.ano}";
	private static final String R7 = "o.processoTrf.numeroDigitoVerificador = #{processoAdiadoVistaList.numeroProcesso.numeroDigitoVerificador}";
	private static final String R8 = "o.processoTrf.numeroOrgaoJustica = #{processoAdiadoVistaList.numeroProcesso.numeroOrgaoJustica}";
	private static final String R9 = "o.processoTrf.numeroOrigem = #{processoAdiadoVistaList.numeroProcesso.numeroOrigem}";
	private static final String R10 = "o.processoTrf.orgaoJulgador = #{processoAdiadoVistaList.orgaoJulgadorCombo}";
	private static final String R11 = "o.sessaoPautaProcessoTrf.sessao.continua = #{processoAdiadoVistaList.continua}";

	@Override
	protected void addSearchFields() {
		addSearchField("processoTrf.classeJudicial", SearchCriteria.igual, R1);
		addSearchField("processoTrf.assuntoTrf", SearchCriteria.contendo, R2);
		addSearchField("processoTrf.nomeParte", SearchCriteria.igual, R3);
		addSearchField("sessaoPautaProcessoTrf.processoTrf.nomeParte", SearchCriteria.igual, R4);
		addSearchField("processoTrf.numeroSequencia", SearchCriteria.igual, R5);
		addSearchField("processoTrf.ano", SearchCriteria.igual, R6);
		addSearchField("processoTrf.numeroDigitoVerificador", SearchCriteria.igual, R7);
		addSearchField("processoTrf.numeroOrgaoJustica", SearchCriteria.igual, R8);
		addSearchField("processoTrf.numeroOrigem", SearchCriteria.igual, R9);
		addSearchField("processoTrf.orgaoJulgador", SearchCriteria.igual, R10);
		addSearchField("sessaoPautaProcessoTrf.sessao.continua", SearchCriteria.igual, R11);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("numeroProcesso", "processoTrf.processo.numeroProcesso");
		map.put("classeJudicial", "processoTrf.classeJudicial");
		map.put("orgaoJulgador", "processoTrf.orgaoJulgador");
		return map;
	}

	/**
	 * O EJBQL está sendo passado diretamente neste método, pois da outra forma,
	 * o EJBQL tinha que ser passado em uma variável statica, impossibilitando
	 * que o método getOrgaoJulgador() fosse instanciado mais de uma vez.
	 */
	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		OrgaoJulgador orgaoJulgadorAtual = Authenticator.getOrgaoJulgadorAtual();

		sb.append("select o from ConsultaProcessoAdiadoVista o where o.adiadoVista = 'AD' ");
		if (Authenticator.getOrgaoJulgadorColegiadoAtual() != null) {
			sb.append("and o.processoTrf.orgaoJulgadorColegiado = #{authenticator.getOrgaoJulgadorColegiadoAtual() } ");
		}
		sb.append("and not exists (select s.processoTrf from SessaoPautaProcessoTrf s ");
		sb.append("where s.processoTrf = o.processoTrf and s.dataExclusaoProcessoTrf is null ");
		sb.append("and s.julgamentoFinalizado is false and s.sessao.dataRealizacaoSessao is null ");
		sb.append(" and (s.sessao.dataRegistroEvento is null or s.sessao.dataFechamentoSessao is not null)) "); 
		if(SessaoHome.instance().getInstance().getContinua()){
			sb.append("and o.sessaoPautaProcessoTrf.sessao.continua is true ");
			if(!Authenticator.getUsuarioLocalizacaoAtual().getPapel().getIdentificador().equals("idSecretarioSessao")){
				sb.append("and o.sessaoPautaProcessoTrf.orgaoJulgadorRetiradaJulgamento = o.processoTrf.orgaoJulgador ");
			}
		}else{
			sb.append("and (o.sessaoPautaProcessoTrf.sessao.continua is false ");
			sb.append("		or (o.sessaoPautaProcessoTrf.sessao.continua is true ");
			sb.append("			and o.sessaoPautaProcessoTrf.orgaoJulgadorRetiradaJulgamento != o.processoTrf.orgaoJulgador)) ");
		}
		
		
		if (Authenticator.isPapelPermissaoSecretarioSessao() || 
				orgaoJulgadorAtual == null) {
			
			sb.append(" and ("
					+ " o.processoTrf.selecionadoJulgamento = true or o.processoTrf.selecionadoPauta = true"
					+ " ) ");
			
			sb.append(" and	o.processoTrf.orgaoJulgadorColegiado = #{sessaoHome.instance.orgaoJulgadorColegiado} ");
			
		} else {
			sb.append("and (o.processoTrf.orgaoJulgador.idOrgaoJulgador = " + orgaoJulgadorAtual.getIdOrgaoJulgador() +" or ");
			sb.append("exists ");
			sb.append("	(select rpt.processoTrf from RevisorProcessoTrf rpt ");
			sb.append("		 where rpt.orgaoJulgadorRevisor.idOrgaoJulgador = " + orgaoJulgadorAtual.getIdOrgaoJulgador() );
			sb.append("		 and rpt.processoTrf = o.processoTrf ");
			sb.append("      and rpt.dataFinal is null )");
			sb.append("OR (o.processoTrf.orgaoJulgadorRevisor.idOrgaoJulgador = ").append(orgaoJulgadorAtual.getIdOrgaoJulgador()).append(" AND o.processoTrf.exigeRevisor = true )) ");
		}
		return sb.toString();
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

	public NumeroProcesso getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(NumeroProcesso numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
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
		setNomeParte(new String());
		setOrgaoJulgadorCombo(null);
		setClasseJudicial(null);
		setAssuntoTrf(null);
		limparTrees();
		setContinua(null);
		super.newInstance();
	}

	public void setOrgaoJulgadorCombo(OrgaoJulgador orgaoJulgadorCombo) {
		this.orgaoJulgadorCombo = orgaoJulgadorCombo;
	}

	public OrgaoJulgador getOrgaoJulgadorCombo() {
		return orgaoJulgadorCombo;
	}
	
	public void setOrderedColumn(String order) {
		setOrder(order);
	}
	
	public Boolean getContinua() {
		return continua;
	}

	public void setContinua(Boolean continua) {
		this.continua = continua;
	}
}