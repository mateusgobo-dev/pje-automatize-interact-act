package br.com.infox.pje.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.component.NumeroProcesso;
import br.com.infox.cliente.home.ConsultaProcessoHome;
import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.nucleo.enums.SessaoResultadoVotacaoEnum;
import br.jus.pje.nucleo.enums.SituacaoProcessoSessaoEnum;
import br.jus.pje.nucleo.enums.TipoInclusaoEnum;

@Name(SessaoPautaSecretarioProcessoNuloList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
@Deprecated
public class SessaoPautaSecretarioProcessoNuloList extends EntityList<SessaoPautaProcessoTrf> {
	/**
	 * este componente nao é mais utilizado na tela do secretario da sessao, tendo sido completamente substituido.
	 * verificar possibilidade de exclusao desta tela
	 */

	private NumeroProcesso numeroProcesso = new NumeroProcesso();
	private OrgaoJulgador orgaoJulgador;
	private ClasseJudicial classeJudicial;
	private AssuntoTrf assuntoTrf;
	private TipoPessoa tipoPessoa;
	private String processoParte;
	private Boolean cpf = Boolean.TRUE;
	private String numCpf;
	private String numCnpj;
	private TipoInclusaoEnum tipoInclusao;
	private SituacaoProcessoSessaoEnum situacaoProcEnum;
	private Integer numeroProcessos = 0;
	private Integer numeroOrdem;
	private Boolean possuiProclamacaoAntecipada;
	private List<SessaoPautaProcessoTrf> listaProcessosEmPauta = new ArrayList<SessaoPautaProcessoTrf>(0);
	private TipoVoto tipoVoto;
	private SessaoResultadoVotacaoEnum sessaoResultadoVotacaoEnum;

	/**
	 * metodo criado para evitar carregamentos desnecessarios da lista
	 * @return
	 */
	public List<SessaoPautaProcessoTrf> getProcessosEmPauta() {
		if(listaProcessosEmPauta.isEmpty()) {
			listaProcessosEmPauta.addAll(list(numeroProcessos));
		}
		return listaProcessosEmPauta;
	}
	

	public void clearCpfCnpj() {
		setNumCpf(null);
		setNumCnpj(null);
	}

	public static final String NAME = "sessaoPautaSecretarioProcessoNuloList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "o.numeroOrdem";
	private static final String R1 = "o.sessao = #{sessaoHome.instance}";

	private static final String R2 = "o.processoTrf.numeroSequencia = #{sessaoPautaSecretarioProcessoNuloList.numeroProcesso.numeroSequencia}";
	private static final String R3 = "o.processoTrf.ano = #{sessaoPautaSecretarioProcessoNuloList.numeroProcesso.ano}";
	private static final String R4 = "o.processoTrf.numeroDigitoVerificador = #{sessaoPautaSecretarioProcessoNuloList.numeroProcesso.numeroDigitoVerificador}";
	private static final String R5 = "o.processoTrf.numeroOrgaoJustica = #{sessaoPautaSecretarioProcessoNuloList.numeroProcesso.numeroOrgaoJustica}";
	private static final String R6 = "o.processoTrf.numeroOrigem = #{sessaoPautaSecretarioProcessoNuloList.numeroProcesso.numeroOrigem}";

	private static final String R7 = "o.processoTrf.orgaoJulgador = #{sessaoPautaSecretarioProcessoNuloList.orgaoJulgador}";

	private static final String R8 = "o.processoTrf.classeJudicial = #{sessaoPautaSecretarioProcessoNuloList.classeJudicial}";

	private static final String R9 = "o.processoTrf.idProcessoTrf in (select distinct p.idProcessoTrf from ProcessoTrf p "
			+ "inner join p.assuntoTrfList a "
			+ "where o.processoTrf = p and "
			+ "a = #{sessaoPautaSecretarioProcessoNuloList.assuntoTrf})";

	private static final String R10 = " exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "where pp.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf "
			+ "and pp.pessoa.tipoPessoa = #{sessaoPautaSecretarioProcessoNuloList.tipoPessoa})";

	private static final String R11 = "exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp "
			+ "where pp.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf "
			+ "and lower(to_ascii(pp.pessoa.nome)) like "
			+ "'%' || lower(to_ascii(#{sessaoPautaSecretarioProcessoNuloList.processoParte})) || '%')";

	private static final String R12 = "o.processoTrf IN (select pp.processoTrf from ProcessoParte pp "
			+ "where pp.processoTrf = o.processoTrf and pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi "
			+ "where pdi.tipoDocumento.codTipo = 'CPF' and pdi.numeroDocumento like concat('%', #{consultaProcessoHome.instance.numeroCPF} ,'%')))";
	private static final String R13 = "o.processoTrf IN (select pp.processoTrf from ProcessoParte pp "
			+ "where pp.processoTrf = o.processoTrf and pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi "
			+ "where pdi.tipoDocumento.codTipo = 'CPJ' and pdi.numeroDocumento like concat('%', #{consultaProcessoHome.instance.numeroCNPJ} ,'%')))";
	private static final String R14 = "o.tipoInclusao = #{sessaoPautaSecretarioProcessoNuloList.tipoInclusao}";
	private static final String R15 = "o.numeroOrdem = #{sessaoPautaSecretarioProcessoNuloList.numeroOrdem}";
	private static final String R16 = "exists (select 1 from SessaoProcessoDocumentoVoto spdv "
															+" where spdv.sessao = o.sessao "
															+"   and processoTrf = o.processoTrf "
															+"   and orgaoJulgador = o.processoTrf.orgaoJulgador "
															+"   and tipoVoto      =  #{sessaoPautaSecretarioProcessoNuloList.tipoVoto})";

	@Override
	protected void addSearchFields() {
		addSearchField("sessao", SearchCriteria.igual, R1);

		addSearchField("processoTrf.numeroSequencia", SearchCriteria.igual, R2);
		addSearchField("processoTrf.ano", SearchCriteria.igual, R3);
		addSearchField("processoTrf.numeroDigitoVerificador", SearchCriteria.igual, R4);
		addSearchField("processoTrf.numeroOrgaoJustica", SearchCriteria.igual, R5);
		addSearchField("processoTrf.numeroOrigem", SearchCriteria.igual, R6);

		addSearchField("processoTrf.orgaoJulgador", SearchCriteria.igual, R7);
		addSearchField("processoTrf.classeJudicial", SearchCriteria.igual, R8);
		addSearchField("processoTrf.assuntoTrfList", SearchCriteria.igual, R9);
		addSearchField("processoTrf.tipoPessoa", SearchCriteria.igual, R10);
		addSearchField("processoTrf.pessoaMarcouPauta", SearchCriteria.contendo, R11);
		addSearchField("processoTrf.orgaoJulgador.orgaoJulgador", SearchCriteria.contendo, R12);
		addSearchField("processoTrf.orgaoJulgador.localizacao", SearchCriteria.contendo, R13);
		addSearchField("tipoInclusao", SearchCriteria.igual, R14);
		addSearchField("numeroOrdem", SearchCriteria.igual, R15);
		addSearchField("tipoVoto", SearchCriteria.igual, R16);
	
	}

	@Override
	protected String getDefaultEjbql() {
		return "select o from SessaoPautaProcessoTrf o join fetch o.consultaProcessoTrf cons where o.dataExclusaoProcessoTrf is null "
				+ hqlFiltroSituacaoProcSessao() + hqlFiltroPossuiProclamacaoAntecipada() + hqlFiltroSessaoResultadoVotacao();
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("classeJudicial", "processoTrf.classeJudicial.classeJudicial");
		return map;
	}

	private String hqlFiltroSessaoResultadoVotacao() {
		String retorno = " ";

		if(sessaoResultadoVotacaoEnum != null){

			switch (sessaoResultadoVotacaoEnum) {
				case UN:
					retorno  += " and not exists (select s.idSessaoProcessoDocumento from SessaoProcessoDocumentoVoto s  "
							  + "							where o.sessao = s.sessao and s.processoTrf = o.processoTrf "
							  + "							  and s.ojAcompanhado != o.processoTrf.orgaoJulgador"
							  + "							  and s.liberacao = true) "
							  + " and exists (select s.idSessaoProcessoDocumento from SessaoProcessoDocumentoVoto s  "
							  + "							where o.sessao = s.sessao and s.processoTrf = o.processoTrf "
							  + "							  and s.ojAcompanhado = o.processoTrf.orgaoJulgador"
							  + "							  and s.liberacao = true) ";

					break;
				case NU:
					retorno += " and exists (select s.idSessaoProcessoDocumento from SessaoProcessoDocumentoVoto s  "
							 +"							where o.sessao = s.sessao and s.processoTrf = o.processoTrf "
							 +"							  and s.ojAcompanhado != o.processoTrf.orgaoJulgador " 
							 + "							  and s.liberacao = true) "
							 
							 +" and   (select count(s1.idSessaoProcessoDocumento) from SessaoProcessoDocumentoVoto s1  "
							 +"							where o.sessao = s1.sessao and s1.processoTrf = o.processoTrf "
							 +"							  and s1.ojAcompanhado = o.processoTrf.orgaoJulgador "
							 + "							  and s1.liberacao = true) > "
							 
							 +" 		(select count(s2.idSessaoProcessoDocumento) from SessaoProcessoDocumentoVoto s2 "  
							 +"							where o.sessao = s2.sessao and s2.processoTrf = o.processoTrf "
							 +"							  and s2.ojAcompanhado != o.processoTrf.orgaoJulgador "
							 + "							  and s2.liberacao = true) ";
					break;
				case NR:
					retorno += " and   (select count(s1.idSessaoProcessoDocumento) from SessaoProcessoDocumentoVoto s1  "
							 +"							where o.sessao = s1.sessao and s1.processoTrf = o.processoTrf "
							 +"							  and s1.ojAcompanhado != o.processoTrf.orgaoJulgador "
							 + "							  and s1.liberacao = true) > "
							 +" 		(select count(s2.idSessaoProcessoDocumento) from SessaoProcessoDocumentoVoto s2 "  
							 +"							where o.sessao = s2.sessao and s2.processoTrf = o.processoTrf "
							 +"							 and s2.ojAcompanhado = o.processoTrf.orgaoJulgador "
							 + "							  and s2.liberacao = true) ";
					break;
			}
		}
		return retorno;
	}
	
	private String hqlFiltroSituacaoProcSessao() {
		String retorno = " ";
		if (null != situacaoProcEnum) {
			switch (situacaoProcEnum) {
			case AD:
				retorno = "and o.adiadoVista = 'AD' ";
				break;
			case AJ:
				retorno = "and o.situacaoJulgamento = 'AJ' ";
				break;
			case AN:
				retorno = "AND EXISTS (SELECT 1 FROM NotaSessaoJulgamento AS n  WHERE n.processoTrf = o.processoTrf AND n.ativo = true AND n.sessao = o.sessao) ";
				break;
			case EJ:
				retorno = "and o.situacaoJulgamento = 'EJ' ";
				break;
			case JG:
				retorno = "and o.situacaoJulgamento = 'JG' ";
				break;
			case PR:
				retorno = "and o.preferencia = true ";
				break;
			case PV:
				retorno = "and o.adiadoVista = 'PV' ";
				break;
			case RJ:
				retorno = "and o.situacaoJulgamento = 'NJ' ";
				break;
			case SO:
				retorno = "and o.sustentacaoOral = true ";
				break;
			case JC:
				retorno = "and o.maioriaDetectada = true ";
				break;	
			case DD:
				retorno = "and exists (select 1 from SessaoProcessoDocumentoVoto spdv where spdv.sessao = o.sessao " +
						  "and spdv.processoTrf = o.processoTrf and spdv.destaqueSessao = true) ";
				break;
			}
		}
		return retorno;
	}

	private String hqlFiltroPossuiProclamacaoAntecipada() {
		String retorno = " ";
		
		if(possuiProclamacaoAntecipada != null) {
			String not = possuiProclamacaoAntecipada ? " " : " not ";
			retorno = " and " +  not + " exists (select 1 from SessaoProcessoDocumentoVoto spdv where spdv.sessao = o.sessao " +
					  " and spdv.processoTrf = o.processoTrf and spdv.textoProclamacaoJulgamento is not null) ";
		}
		return retorno;
	}
	
	@Override
	public List<SessaoPautaProcessoTrf> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}

	public void setNumeroProcesso(NumeroProcesso numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public NumeroProcesso getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
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

	public void setTipoPessoa(TipoPessoa tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}

	public TipoPessoa getTipoPessoa() {
		return tipoPessoa;
	}

	public void setProcessoParte(String processoParte) {
		this.processoParte = processoParte;
	}

	public String getProcessoParte() {
		return processoParte;
	}

	public void setCpf(Boolean cpf) {
		this.cpf = cpf;
	}

	public Boolean getCpf() {
		return cpf;
	}

	public void setNumCpf(String numCpf) {
		this.numCpf = numCpf;
	}

	public String getNumCpf() {
		return numCpf;
	}

	public void setNumCnpj(String numCnpj) {
		this.numCnpj = numCnpj;
	}

	public String getNumCnpj() {
		return numCnpj;
	}

	public TipoInclusaoEnum getTipoInclusao() {
		return tipoInclusao;
	}

	public void setTipoInclusao(TipoInclusaoEnum tipoInclusao) {
		this.tipoInclusao = tipoInclusao;
	}

	@Override
	public void newInstance() {
		numeroProcesso = new NumeroProcesso();
		orgaoJulgador = null;
		classeJudicial = null;
		assuntoTrf = null;
		tipoPessoa = null;
		processoParte = null;
		cpf = Boolean.TRUE;
		numCpf = null;
		numCnpj = null;
		tipoInclusao = null;
		situacaoProcEnum = null;
		numeroOrdem = null;
		possuiProclamacaoAntecipada = null;
		tipoVoto = null;
		sessaoResultadoVotacaoEnum = null;
		if (ConsultaProcessoHome.instance() != null) {
			ConsultaProcessoHome.instance().newInstance();
		}
		
		limparSelecaoArvore("classeJudicialTree");
		limparSelecaoArvore("assuntoTrfTree");
		limparSelecaoArvore("tipoPessoaTree");
		
	}
	
	/**
	 * Realiza a limpeza do item selecionado em determinada árvore.
	 */
	private void limparSelecaoArvore(String idComponente) {
		@SuppressWarnings("rawtypes")
		AbstractTreeHandler tree = ComponentUtil.getComponent(idComponente);
		if (tree != null) {
			tree.clearTree();
		}
	}
	
	public SituacaoProcessoSessaoEnum getSituacaoProcEnum() {
		return situacaoProcEnum;
	}

	public void setSituacaoProcEnum(SituacaoProcessoSessaoEnum situacaoProcessoSessaoEnum) {
		this.situacaoProcEnum = situacaoProcessoSessaoEnum;
	}

	public Integer getNumeroProcessos() {
		return numeroProcessos;
	}

	public void setNumeroProcessos(Integer numeroProcessos) {
		this.numeroProcessos = numeroProcessos;
	}
	@Override
	public List<SessaoPautaProcessoTrf> list(int maxResult) {
		if(maxResult == 0){
			return super.list();
		}
		return super.list(maxResult);
	}

	public Integer getNumeroOrdem() {
		return numeroOrdem;
	}

	public void setNumeroOrdem(Integer numeroOrdem) {
		this.numeroOrdem = numeroOrdem;
	}

	public Boolean getPossuiProclamacaoAntecipada() {
		return possuiProclamacaoAntecipada;
	}

	public void setPossuiProclamacaoAntecipada(
			Boolean possuiProclamacaoAntecipada) {
		this.possuiProclamacaoAntecipada = possuiProclamacaoAntecipada;
	}

	public TipoVoto getTipoVoto() {
		return tipoVoto;
	}

	public void setTipoVoto(TipoVoto tipoVoto) {
		this.tipoVoto = tipoVoto;
	}

	public SessaoResultadoVotacaoEnum getSessaoResultadoVotacaoEnum() {
		return sessaoResultadoVotacaoEnum;
	}

	public void setSessaoResultadoVotacaoEnum(SessaoResultadoVotacaoEnum sessaoResultadoVotacaoEnum) {
		this.sessaoResultadoVotacaoEnum = sessaoResultadoVotacaoEnum;
	}
	
	
}