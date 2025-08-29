package br.com.infox.list;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.security.Identity;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.PessoaServidor;
import br.jus.pje.nucleo.entidades.ProcessoAudiencia;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;


@Name(ProcessoAudienciaList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class ProcessoAudienciaList extends EntityList<ProcessoAudiencia> {

	public static final String NAME = "processoAudienciaList";
	
	private static final String PAPEL_ASSIST_ADVOGADO = "assistAdvogado";

	private static final long serialVersionUID = 1L;
	private OrgaoJulgador orgaoJulgador;
	private String nomeRealizador;
	private String nomeConciliador;
	private String parte;
	private String nomeAdvogado;
	private ClasseJudicial classeJudicial;
	private AssuntoTrf assuntoTrf;
	private Integer numeroSequencia;
	private Integer numeroDigitoVerificador;
	private Integer ano;
	private Integer numeroOrigemProcesso;
	private boolean executarCount = false;
	
	private static final String DEFAULT_ORDER = "o.idProcessoAudiencia";
	
		
	private static final String R1 = "(#{processoAudienciaHome.orgaoJulgador.idOrgaoJulgador} = o.processoTrf.orgaoJulgador.idOrgaoJulgador)";
	private static final String R2 = "lower(to_ascii(o.pessoaRealizador.nome)) like '%' || lower(to_ascii(#{processoAudienciaList.nomeRealizador})) || '%'";
	private static final String R3 = "lower(to_ascii(o.pessoaConciliador.nome)) like '%' || lower(to_ascii(#{processoAudienciaList.nomeConciliador})) || '%'";
 	private static final String R4 = "exists (select o.processoTrf from ProcessoParte pp where pp.processoTrf=o.processoTrf and pp.tipoParte.idTipoParte not in ("
 			+ParametroUtil.instance().getTipoParteProcurador().getIdTipoParte()
 			+","
 			+ParametroUtil.instance().getTipoParteAdvogado().getIdTipoParte()
 			+")"
 			+" and lower(to_ascii(pp.pessoa.nome)) like '%' || lower(to_ascii(#{processoAudienciaList.parte})) || '%'"
			+" and (pp.parteSigilosa = false or " +
			"      (pp.parteSigilosa = true and "
			+"          ("+Identity.instance().hasRole(Papeis.VISUALIZA_SIGILOSO)+" = true or "+Identity.instance().hasRole(Papeis.MANIPULA_SIGILOSO)+" = true)"
	        +"     )"
	        +" )"
 			+")";

 	private static final String R5 = "exists (select o.processoTrf from ProcessoParte pp where pp.processoTrf=o.processoTrf and pp.tipoParte.idTipoParte in ("
			+ParametroUtil.instance().getTipoParteProcurador().getIdTipoParte()
			+","
			+ParametroUtil.instance().getTipoParteAdvogado().getIdTipoParte()
			+")"
			+" and lower(to_ascii(pp.pessoa.nome)) like '%' || lower(to_ascii(#{processoAudienciaList.nomeAdvogado})) || '%'"
			+" and (pp.parteSigilosa = false or " +
			"      (pp.parteSigilosa = true and "
			+"          ("+Identity.instance().hasRole(Papeis.VISUALIZA_SIGILOSO)+" = true or "+Identity.instance().hasRole(Papeis.MANIPULA_SIGILOSO)+" = true)"
	        +"     )"
	        +" )"
 			+")";
	private static final String R6 = "o.processoTrf.classeJudicial = #{processoAudienciaList.classeJudicial}";
	private static final String R7 = "exists (select distinct pa from ProcessoAssunto pa "
			+ "where pa.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf and "
			+ "pa.assuntoTrf.idAssuntoTrf = #{processoAudienciaList.assuntoTrf.idAssuntoTrf}" + ")";
	private static final String R8 = "o.processoTrf.numeroSequencia = #{processoAudienciaList.numeroSequencia}";
	private static final String R9 = "o.processoTrf.numeroDigitoVerificador = #{processoAudienciaList.numeroDigitoVerificador}";
	private static final String R10 = "o.processoTrf.ano = #{processoAudienciaList.ano}";
	private static final String R11 = "o.processoTrf.numeroOrigem = #{processoAudienciaList.numeroOrigemProcesso}";

	private static final String R12 = "cast(o.dtInicio as date) >= cast(#{processoAudienciaList.entity.dtInicio==null?'" + 
			(new SimpleDateFormat("yyyy-MM-dd")).format(new Date())
			+ "':processoAudienciaList.entity.dtInicio} as date)";

	private static final String R13 = "cast(o.dtFim as date) <= cast(#{processoAudienciaList.entity.dtFim} as date)";
	private static final String R15 = "o.salaAudiencia.idSala = #{processoAudienciaList.entity.salaAudiencia.idSala}";
	private static final String R16 = "o.tipoAudiencia.idTipoAudiencia = #{processoAudienciaHome.tipoAudiencia.idTipoAudiencia}";
	private static final String R17 = "o.salaAudiencia.orgaoJulgador.jurisdicao.idJurisdicao = #{processoAudienciaHome.jurisdicao.idJurisdicao}";
	private static final String R18 = "o.statusAudiencia in( #{processoAudienciaHome.statusAudienciaList})";
	
	@Override
	protected void addSearchFields() {
		addSearchField("processoTrf.orgaoJulgador", SearchCriteria.igual, R1);
		addSearchField("pessoaRealizador", SearchCriteria.contendo, R2);
		addSearchField("pessoaConciliador", SearchCriteria.contendo, R3);
		addSearchField("parte", SearchCriteria.contendo, R4);
		addSearchField("nomeAdvogado", SearchCriteria.contendo, R5);
		addSearchField("processoTrf.classeJudicial", SearchCriteria.contendo, R6);
		addSearchField("processoTrf.assuntoTrf.assuntoTrf", SearchCriteria.igual, R7);
		addSearchField("processoTrf.numeroSequencia", SearchCriteria.contendo, R8);
		addSearchField("processoTrf.numeroDigitoVerificador", SearchCriteria.contendo, R9);
		addSearchField("processoTrf.ano", SearchCriteria.contendo, R10);
		addSearchField("processoTrf.numeroOrigem", SearchCriteria.contendo, R11);
		addSearchField("dtInicio", SearchCriteria.maior, R12);
		addSearchField("dtFim", SearchCriteria.menor, R13);
		addSearchField("tipoAudiencia", SearchCriteria.igual, R16);
		addSearchField("salaAudiencia", SearchCriteria.igual, R15);
		addSearchField("jurisdicao", SearchCriteria.igual, R17);
		addSearchField("statusAudiencia", SearchCriteria.contendo,R18);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("dtInicio", "o.dtInicio");
        map.put("orgaoJulgador", "o.processoTrf.orgaoJulgador.orgaoJulgador, o.processoTrf.orgaoJulgador.orgaoJulgadorOrdemAlfabetica");
        map.put("magistrado", "o.pessoaRealizador");
        map.put("statusAudiencia", "o.statusAudiencia");
        map.put("conciliador", "o.pessoaConciliador");
        map.put("salaAudiencia.sala", "o.salaAudiencia.sala");
        map.put("tipoAudiencia", "o.tipoAudiencia");
        map.put("processoTrf", "o.processoTrf");
        map.put("classeJudicial", "o.processoTrf.classeJudicial.classeJudicial");
		return map;
	}

	/**
	 * O EJBQL está sendo passado diretamente neste método, pois da outra forma,
	 * o EJBQL tinha que ser passado em uma variável statica, impossibilitando
	 * que o método getOrgaoJulgadorEjbql() fosse instanciado mais de uma vez.
	 */
	@Override
	protected String getDefaultEjbql() {

		Pessoa pessoaLogada = Authenticator.getPessoaLogada();
		UsuarioLocalizacao usuarioLocalizacao = Authenticator.getUsuarioLocalizacaoAtual();
		Integer idUsuarioLogado = pessoaLogada.getIdUsuario();

		boolean isConciliador = usuarioLocalizacao.getPapel().getIdentificador().equalsIgnoreCase("conciliador");
		boolean isAdvogado = usuarioLocalizacao.getPapel().getIdentificador().equalsIgnoreCase("advogado");
		boolean isAssistAdvogado = usuarioLocalizacao.getPapel().getIdentificador().equalsIgnoreCase(PAPEL_ASSIST_ADVOGADO);
		boolean isProcurador = usuarioLocalizacao.getPapel().getIdentificador().equalsIgnoreCase("procurador")
				|| usuarioLocalizacao.getPapel().getIdentificador().equalsIgnoreCase("procuradorMP")
				|| usuarioLocalizacao.getPapel().getIdentificador().equalsIgnoreCase("procChefe")
				|| usuarioLocalizacao.getPapel().getIdentificador().equalsIgnoreCase("procChefeMP");

		boolean isUsuarioDoOrgaoJulgador = false;
		if (Pessoa.instanceOf(pessoaLogada, PessoaServidor.class) || Pessoa.instanceOf(pessoaLogada, PessoaMagistrado.class)) {
			isUsuarioDoOrgaoJulgador = true;
		}

		StringBuilder sql = new StringBuilder();
		sql.append("select o from ProcessoAudiencia o ");
		sql.append("join o.processoTrf proc ");
		sql.append("where (");
		sql.append("	proc.segredoJustica = false");
		sql.append("	or (");
		sql.append("		proc.segredoJustica = true ");
		sql.append("		and (");
		sql.append("			exists (");
		sql.append("				select 1 from ProcessoVisibilidadeSegredo pvs");
		sql.append("				where pvs.processo.idProcesso = proc.idProcessoTrf");
		sql.append("				and  (");
		sql.append("					pvs.idPessoa = #{authenticator.getIdUsuarioLogado()} ");
		
		if(Authenticator.getIdProcuradoriaAtualUsuarioLogado() != null){
			sql.append("			                or exists (");
			sql.append("		   				select 1 from ProcessoParte pp ");
			sql.append("		   				where pp.processoTrf.idProcessoTrf = proc.idProcessoTrf ");
			sql.append("		   				and pp.idPessoa = pvs.idPessoa ");
			sql.append("		   				and pp.inSituacao = 'A' ");
			sql.append("		   				and pp.procuradoria.idProcuradoria = #{authenticator.getIdProcuradoriaAtualUsuarioLogado()}");
			sql.append("					)");
		}

		sql.append("				)");
		sql.append("			)");
		sql.append("			or (#{authenticator.isMagistrado()} = true or (#{org.jboss.seam.security.identity.loggedIn} = true and (#{authenticator.isVisualizaSigiloso()} = true)))");
		sql.append("		)");
		sql.append("	)");
		sql.append(") ");
		
		if (isAdvogado) {
			sql.append(" and exists (select ppa.pessoa.idUsuario from ProcessoParte ppa "
					+ "where ppa.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf and ppa.inSituacao = 'A' "
					+ "and ppa.tipoParte.idTipoParte = "
					+ ParametroUtil.instance().getTipoParteAdvogado().getIdTipoParte() + " and ppa.pessoa.idUsuario = "
					+ idUsuarioLogado + ")");
		}
		
		/* 
		 * Filtro para o assistente ter acesso à pauta de audiências do advogado, para, assim, 
		 * realizar as operações de controle.
		 */
		if (isAssistAdvogado) {
			// obtem localizacao do usuario atual, ou seja, do assistente de advogado.
			Localizacao localizacao = Authenticator.getLocalizacaoAtual();
			// obtem usuarioLocalizacao a partir da localização do usuário atual.
			UsuarioLocalizacao usuarioLocal = Authenticator.getUsuarioLocalizacaoPorIdLocalizacao(localizacao);
			sql.append("and exists (select ppa.pessoa.idUsuario from ProcessoParte ppa "
					+ "where ppa.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf and ppa.inSituacao = 'A'"
					+ "and ppa.tipoParte.idTipoParte = "
					+ ParametroUtil.instance().getTipoParteAdvogado().getIdTipoParte() +
					" and ppa.pessoa.idUsuario = "
					+ usuarioLocal.getUsuario().getIdUsuario() + ") ");
		}

		/*
		 * Adição de filtro para usuário com papel procurador, de forma a
		 * incluir, nos resultados da busca, as audiências de processos em que
		 * uma das entidades parte é representada pelo procurador. 
		 */
		if (isProcurador) {
			sql.append("and exists (");
			sql.append("	select 1 from ProcessoParte pp ");
			sql.append("	where pp.processoTrf.idProcessoTrf = proc.idProcessoTrf ");
			sql.append("	and pp.inSituacao = 'A' ");
			sql.append(" and pp.procuradoria.idProcuradoria = #{authenticator.getIdProcuradoriaAtualUsuarioLogado()}) ");
		}
		
		if (isConciliador) {
			sql.append("and ((o.pessoaConciliador.idUsuario = " + idUsuarioLogado
					+ " or o.pessoaRealizador.idUsuario = " + idUsuarioLogado + ") or o.statusAudiencia = 'M') ");
		}


		if (!isUsuarioDoOrgaoJulgador && !isAdvogado && !isAssistAdvogado && !isConciliador && !isProcurador) {
			sql.append("and exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
					+ "where pp.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf "
					+ "and pp.pessoa.idUsuario = " + idUsuarioLogado + ") ");

		}

		return sql.toString();

	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setNomeRealizador(String nomeRealizador) {
		this.nomeRealizador = nomeRealizador;
	}

	public String getNomeRealizador() {
		return nomeRealizador;
	}

	public void setNomeConciliador(String nomeConciliador) {
		this.nomeConciliador = nomeConciliador;
	}

	public String getNomeConciliador() {
		return nomeConciliador;
	}

	public void setParte(String parte) {
		this.parte = parte;
	}

	public String getParte() {
		return parte;
	}

	public void setNomeAdvogado(String nomeAdvogado) {
		this.nomeAdvogado = nomeAdvogado;
	}

	public String getNomeAdvogado() {
		return nomeAdvogado;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setNumeroSequencia(Integer numeroSequencia) {
		this.numeroSequencia = numeroSequencia;
	}

	public Integer getNumeroSequencia() {
		return numeroSequencia;
	}

	public void setNumeroDigitoVerificador(Integer numeroDigitoVerificador) {
		this.numeroDigitoVerificador = numeroDigitoVerificador;
	}

	public Integer getNumeroDigitoVerificador() {
		return numeroDigitoVerificador;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	public Integer getAno() {
		return ano;
	}

	public void setNumeroOrigemProcesso(Integer numeroOrigemProcesso) {
		this.numeroOrigemProcesso = numeroOrigemProcesso;
	}

	public Integer getNumeroOrigemProcesso() {
		return numeroOrigemProcesso;
	}

	@Override
	public Long getResultCount() {
		setGroupBy("o.idProcessoAudiencia");
		Long count = 0L;
		if(executarCount) {
			count = super.getResultCount();
		}
		setGroupBy(null);
		return count;
	}
	
	@Override
	public void actionList(int maxResult){
		executarCount = true;
		super.actionList(maxResult);
	}
	@Override
	public List<ProcessoAudiencia> list(int maxResult) {
		return executarCount ? super.list(maxResult) : new ArrayList<>(0);
	}
}