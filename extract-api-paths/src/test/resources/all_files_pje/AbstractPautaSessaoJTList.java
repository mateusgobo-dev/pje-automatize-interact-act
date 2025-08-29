package br.com.infox.pje.list;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.component.NumeroProcesso;
import br.com.infox.cliente.component.tree.AssuntoTrfTreeHandler;
import br.com.infox.cliente.component.tree.ClasseJudicialTreeHandler;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.jus.pje.jt.entidades.PautaSessao;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoSessao;
import br.jus.pje.nucleo.entidades.Usuario;


public abstract class AbstractPautaSessaoJTList <T extends PautaSessao> extends EntityList<PautaSessao> {
	
	private static final long serialVersionUID = 1L;
	
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from PautaSessao o ");
		return sb.toString();
	}
	
	private static final String DEFAULT_ORDER = "o";
	
	private NumeroProcesso numeroProcesso = new NumeroProcesso();
	private ClasseJudicial classeJudicial;
	private AssuntoTrf assuntoTrf;
	private String nomeParte;
	private Boolean relatorio = null;
	private Date dtDistribuicaoInicio;
	private Date dtDistribuicaoFim;
	private OrgaoJulgador orgaoJulgador;
	private TipoSessao tipoSessao;
	private Map<String, AbstractTreeHandler<?>> treePainelAdvogado;

	private static final String R1 = "o.processoTrf.numeroSequencia = #{sessaoPautaProcessoTrfIntimacoesJTList.numeroProcesso.numeroSequencia}";
	private static final String R2 = "o.processoTrf.numeroDigitoVerificador = #{sessaoPautaProcessoTrfIntimacoesJTList.numeroProcesso.numeroDigitoVerificador}";
	private static final String R3 = "o.processoTrf.ano = #{sessaoPautaProcessoTrfIntimacoesJTList.numeroProcesso.ano}";
	private static final String R4 = "o.processoTrf.numeroOrigem = #{sessaoPautaProcessoTrfIntimacoesJTList.numeroProcesso.numeroOrigem}";
	private static final String R5 = "o.processoTrf.numeroOrgaoJustica = #{sessaoPautaProcessoTrfIntimacoesJTList.numeroProcesso.numeroOrgaoJustica}";
	private static final String R6 = "o.processoTrf.orgaoJulgador = #{sessaoPautaProcessoTrfIntimacoesJTList.orgaoJulgador}";
	private static final String R7 = "o.processoTrf.classeJudicial = #{sessaoPautaProcessoTrfIntimacoesJTList.classeJudicial}";
	private static final String R8 = "o.processoTrf in (select pa.processoTrf from ProcessoAssunto pa " +
	 								 				   "where pa.assuntoTrf = #{sessaoPautaProcessoTrfIntimacoesJTList.assuntoTrf}) ";
	private static final String R9 = "o.processoTrf in (select pp.processoTrf from ProcessoParte pp " +
													   "where pp.processoTrf = o.processoTrf " +
													   "and lower(to_ascii(pp.pessoa.nome)) like '%' || lower(to_ascii(#{sessaoPautaProcessoTrfIntimacoesJTList.nomeParte})) || '%')";  
	private static final String R10 = "o.processoTrf in (select pp.processoTrf from ProcessoParte pp " +
														"where pp.processoTrf = o.processoTrf " +
												        "and pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi " +
																			   		"where pdi.tipoDocumento.codTipo = 'CPF' and pdi.numeroDocumento like concat('%', #{consultaProcessoHome.instance.numeroCPF} ,'%')))";
	private static final String R11 = "o.processoTrf in (select pp.processoTrf from ProcessoParte pp " +
												        "where pp.processoTrf = o.processoTrf and pp.pessoa.idUsuario IN (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi " +
												        																 "where pdi.tipoDocumento.codTipo = 'CPJ' and pdi.numeroDocumento like concat('%', #{consultaProcessoHome.instance.numeroCNPJ} ,'%')))";
	private static final String R12 = "o.sessao.tipoSessao =  #{sessaoPautaProcessoTrfIntimacoesJTList.tipoSessao}";
	private static final String R13 = "cast(o.processoTrf.dataDistribuicao as date) >= #{sessaoPautaProcessoTrfIntimacoesJTList.dtDistribuicaoInicio}";
	private static final String R14 = "cast(o.processoTrf.dataDistribuicao as date) <= #{sessaoPautaProcessoTrfIntimacoesJTList.dtDistribuicaoFim}";
	
	protected void addSearchFields() {
		addSearchField("processoTrf.numeroSequencia", SearchCriteria.contendo, R1);
		addSearchField("processoTrf.numeroDigitoVerificador", SearchCriteria.contendo, R2);
		addSearchField("processoTrf.ano", SearchCriteria.contendo, R3);
		addSearchField("processoTrf.numeroOrigem", SearchCriteria.contendo, R4);
		addSearchField("processoTrf.numeroOrgaoJustica", SearchCriteria.igual, R5);
		addSearchField("processoTrf.orgaoJulgador", SearchCriteria.igual, R6);
		addSearchField("processoTrf.classeJudicial", SearchCriteria.igual, R7);
		addSearchField("processoTrf.assuntoTrf", SearchCriteria.contendo, R8);
		addSearchField("processoTrf.nomeParte", SearchCriteria.contendo, R9);
		addSearchField("processoTrf.jurisdicao", SearchCriteria.igual, R10);
		addSearchField("processoTrf.valorCausa", SearchCriteria.igual, R11);
		addSearchField("sessao.tipoSessao", SearchCriteria.igual, R12);
		addSearchField("processoTrf.listaPartePassivo", SearchCriteria.igual, R13);
		addSearchField("processoTrf.dataDistribuicao", SearchCriteria.igual, R14);
	}

	@SuppressWarnings("unchecked")
	public String getListaPoloAtivo(PautaSessao pautaSessao) {
		String retorno = null;
		if(pautaSessao != null)
		{
			ProcessoTrf processoTrf = pautaSessao.getProcessoTrf();
			StringBuilder sb = new StringBuilder();
			sb.append("select o from ProcessoParte o ");
			sb.append("where o.processoTrf = :processoTrf and inParticipacao='A'");
			
			Query query = getEntityManager().createQuery(sb.toString());
			query.setParameter("processoTrf", processoTrf);		
			
			List<ProcessoParte> listPP =  query.getResultList();
			
			if (listPP.size() == 1){
				retorno = listPP.get(0).getPessoa().getDocumentoCpfCnpj() != null ? 
						listPP.get(0).getPessoa().getNome() + " - " + listPP.get(0).getPessoa().getDocumentoCpfCnpj() : 
						listPP.get(0).getPessoa().getNome();
				return retorno;
			}
			else {
				if (listPP.size() > 1){
					retorno = listPP.get(0).getPessoa().getDocumentoCpfCnpj() != null ?
					listPP.get(0).getPessoa().getNome() + " e outros - " + listPP.get(0).getPessoa().getDocumentoCpfCnpj() :
					listPP.get(0).getPessoa().getNome() + " e outros ";
							
					return retorno;
				}
			}
		}		
		return retorno;
	}	
	
	@SuppressWarnings("unchecked")
	public String getListaPoloPassivo(PautaSessao pautaSessao) {
		
		String retorno = null;
		
		if(pautaSessao != null)
		{
			ProcessoTrf processoTrf = pautaSessao.getProcessoTrf();
			StringBuilder sb = new StringBuilder();
			sb.append("select o from ProcessoParte o ");
			sb.append("where o.processoTrf = :processoTrf and inParticipacao='P'");
			
			Query query = getEntityManager().createQuery(sb.toString());
			query.setParameter("processoTrf", processoTrf);		
			
			List<ProcessoParte> listPP =  query.getResultList();
						
			if (listPP.size() == 1){
				retorno = listPP.get(0).getPessoa().getDocumentoCpfCnpj() != null ? 
						listPP.get(0).getPessoa().getNome() + " - " + listPP.get(0).getPessoa().getDocumentoCpfCnpj() :  
						listPP.get(0).getPessoa().getNome();
						
				return retorno;
			}
			else {
				if (listPP.size() > 1){
					retorno = listPP.get(0).getPessoa().getDocumentoCpfCnpj() != null ?  
						
					listPP.get(0).getPessoa().getNome() + " e outros - " + listPP.get(0).getPessoa().getDocumentoCpfCnpj() : 
					listPP.get(0).getPessoa().getNome()  + " e outros ";
					return retorno;
				}
				
			}
		}		
		return retorno;
	}
	
	public String getIdProcessoParteExpedienteUsuarioLogado(PautaSessao pautaSessao) {
		ProcessoTrf processoTrf = pautaSessao.getProcessoTrf();
		Usuario usuario = Authenticator.getUsuarioLogado();
		String sql = "select o from ProcessoParteExpediente o "+
		         "where o.processoJudicial = :processoTrf "+
		         "and o.pessoaParte = :pessoa " +
		         "and o.processoExpediente.meioExpedicaoExpediente = 'E' " +
		         "and o.processoExpediente.tipoProcessoDocumento.idTipoProcessoDocumento = :parametroTipoProcessoDocumentoIntimacaoPauta " +
		         "and o.processoExpediente.dtCriacao > o.processoJudicial.dataAutuacao";
		Query query = EntityUtil.createQuery(sql);
		query.setParameter("processoTrf", processoTrf);
		query.setParameter("pessoa", usuario);
		query.setParameter("parametroTipoProcessoDocumentoIntimacaoPauta", Integer.valueOf(ParametroUtil.getParametro("idTipoProcessoDocumentoIntimacaoPauta")));
		ProcessoParteExpediente ppe = EntityUtil.getSingleResult(query);
		if(ppe != null){
			return Integer.toString(ppe.getIdProcessoParteExpediente());
		}
		return null;
	}
	
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}
	
	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}


	public void setNumeroProcesso(NumeroProcesso numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}


	public NumeroProcesso getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}


	public String getNomeParte() {
		return nomeParte;
	}


	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}


	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	public Boolean getRelatorio() {
		return relatorio;
	}
	
	public void setRelatorio(Boolean relatorio) {
		this.relatorio = relatorio;
	}

	public void setDtDistribuicaoInicio(Date dtDistribuicaoInicio) {
		this.dtDistribuicaoInicio = dtDistribuicaoInicio;
	}


	public Date getDtDistribuicaoInicio() {
		return dtDistribuicaoInicio;
	}


	public void setDtDistribuicaoFim(Date dtDistribuicaoFim) {
		this.dtDistribuicaoFim = dtDistribuicaoFim;
	}


	public Date getDtDistribuicaoFim() {
		return dtDistribuicaoFim;
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
	
	 @Override
	 public void newInstance() {
	  setOrgaoJulgador(null);
	  setRelatorio(null);
	  super.newInstance();
	 }

	 public TipoSessao getTipoSessao() {
		 return tipoSessao;
	 }
	public void setTipoSessao(TipoSessao tipoSessao) {
		this.tipoSessao = tipoSessao;
	}

	public Map<String, AbstractTreeHandler<?>> getTreePainelAdvogado() {
	if (treePainelAdvogado == null) {
		treePainelAdvogado = new HashMap<String, AbstractTreeHandler<?>>();
		treePainelAdvogado.put("classeJudicialTree1", new ClasseJudicialTreeHandler());
		treePainelAdvogado.put("assuntoTrfTree1", new AssuntoTrfTreeHandler());
	}
	return treePainelAdvogado;
	}
	
	public void setTreePainelAdvogado(Map<String, AbstractTreeHandler<?>> treePainelAdvogado) {
		this.treePainelAdvogado = treePainelAdvogado;
	}


}