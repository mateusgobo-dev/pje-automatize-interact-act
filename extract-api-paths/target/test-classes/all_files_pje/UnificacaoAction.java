package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.ScopeType;

import br.com.infox.pje.manager.PessoaFisicaManager;
import br.com.infox.pje.manager.PessoaJuridicaManager;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.FacesUtil;
import br.com.jt.pje.manager.SessaoManager;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.CaixaRepresentanteManager;
import br.jus.cnj.pje.nucleo.manager.CaracteristicaFisicaManager;
import br.jus.cnj.pje.nucleo.manager.EntityLogManager;
import br.jus.cnj.pje.nucleo.manager.LembreteManager;
import br.jus.cnj.pje.nucleo.manager.LembretePermissaoManager;
import br.jus.cnj.pje.nucleo.manager.LogAcessoManager;
import br.jus.cnj.pje.nucleo.manager.LogHistoricoMovimentacaoManager;
import br.jus.cnj.pje.nucleo.manager.MeioContatoManager;
import br.jus.cnj.pje.nucleo.manager.ModeloProclamacaoJulgamentoManager;
import br.jus.cnj.pje.nucleo.manager.NotaSessaoJulgamentoManager;
import br.jus.cnj.pje.nucleo.manager.ParametroManager;
import br.jus.cnj.pje.nucleo.manager.PessoaAutoridadeManager;
import br.jus.cnj.pje.nucleo.manager.PessoaDocumentoIdentificacaoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaManager;
import br.jus.cnj.pje.nucleo.manager.PessoaNomeAlternativoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoFavoritoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteHistoricoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteSigiloManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoSegredoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoTagManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoTrfConexaoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoTrfRedistribuicaoManager;
import br.jus.cnj.pje.nucleo.manager.QuadroAvisoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoEnteExternoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.SolicitacaoNoDesvioManager;
import br.jus.cnj.pje.nucleo.manager.TipoPessoaManager;
import br.jus.cnj.pje.nucleo.manager.UnificacaoManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLoginManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioManager;
import br.jus.cnj.pje.nucleo.manager.VisibilidadePessoaDocumentoIdentificacaoManager;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.pje.nucleo.entidades.CaixaRepresentante;
import br.jus.pje.nucleo.entidades.CaracteristicaFisica;
import br.jus.pje.nucleo.entidades.Lembrete;
import br.jus.pje.nucleo.entidades.LembretePermissao;
import br.jus.pje.nucleo.entidades.LogHistoricoMovimentacao;
import br.jus.pje.nucleo.entidades.MeioContato;
import br.jus.pje.nucleo.entidades.ModeloProclamacaoJulgamento;
import br.jus.pje.nucleo.entidades.NotaSessaoJulgamento;
import br.jus.pje.nucleo.entidades.Parametro;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAutoridade;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.PessoaNomeAlternativo;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoFavorito;
import br.jus.pje.nucleo.entidades.ProcessoParteHistorico;
import br.jus.pje.nucleo.entidades.ProcessoParteSigilo;
import br.jus.pje.nucleo.entidades.ProcessoSegredo;
import br.jus.pje.nucleo.entidades.ProcessoTag;
import br.jus.pje.nucleo.entidades.ProcessoTrfConexao;
import br.jus.pje.nucleo.entidades.ProcessoTrfRedistribuicao;
import br.jus.pje.nucleo.entidades.QuadroAviso;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoEnteExterno;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SolicitacaoNoDesvio;
import br.jus.pje.nucleo.entidades.UnificacaoFiltroVO;
import br.jus.pje.nucleo.entidades.UnificacaoVO;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.VisibilidadePessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.identidade.LogAcesso;
import br.jus.pje.nucleo.entidades.log.EntityLog;
import br.jus.pje.nucleo.enums.FasesUnificacaoEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Componente de controle da tela de unificaçao de pessoas no PJe.
 * @author luiz.mendes
 *
 */
@Name("unificacaoAction")
@Scope(ScopeType.CONVERSATION)
public class UnificacaoAction extends BaseAction<Pessoa> implements Serializable  {
	
	private static final long serialVersionUID = 1L;
	
	private Stack<FasesUnificacaoEnum> faseUnificacao = new Stack<FasesUnificacaoEnum>();
	private String activeTab = "FASE_01_SELECAO_PESSOAS";
	private static final String IDENTIFICADOR_FASE_SELECAO_PESSOAS = "1";
	private static final String IDENTIFICADOR_FASE_RESUMO_UNIFICACAO = "-1";
	private static final String IDENTIFICADOR_FASE_CONCLUSAO_UNIFICACAO = "-2";
	private UnificacaoFiltroVO filtro = null;
	private UnificacaoVO unificacaoVO = null;
	private EntityDataModel<Pessoa> model;
	@RequestParameter(value="pessoaGridCount")
	private Integer pessoaGridCount;
	
	private UnificacaoManager unificacaoManager = ComponentUtil.getComponent("unificacaoManager");
	private PessoaManager pessoaManager = ComponentUtil.getComponent("pessoaManager");
	private PessoaJuridicaManager pessoaJuridicaManager = ComponentUtil.getComponent("pessoaJuridicaManager");
	private PessoaFisicaManager pessoaFisicaManager = ComponentUtil.getComponent("pessoaFisicaManager");
	private PessoaAutoridadeManager pessoaAutoridadeManager = ComponentUtil.getComponent("pessoaAutoridadeManager");
	private PessoaDocumentoIdentificacaoManager pessoaDocumentoIdentificacaoManager = ComponentUtil.getComponent("pessoaDocumentoIdentificacaoManager");
	private TipoPessoaManager tipoPessoaManager = ComponentUtil.getComponent("tipoPessoaManager");
	private LogAcessoManager logAcessoManager = ComponentUtil.getComponent("logAcessoManager");
	private CaracteristicaFisicaManager caracteristicaFisicaManager = ComponentUtil.getComponent("caracteristicaFisicaManager");
	private MeioContatoManager meioContatoManager = ComponentUtil.getComponent("meioContatoManager");
	private UsuarioManager usuarioManager = ComponentUtil.getComponent("usuarioManager");
	private UsuarioLoginManager usuarioLoginManager = ComponentUtil.getComponent("usuarioLoginManager");
	private PessoaNomeAlternativoManager nomeAlternativoManager = ComponentUtil.getComponent("pessoaNomeAlternativoManager");
	private ProcessoTrfConexaoManager conexaoPrevencaoManager = ComponentUtil.getComponent("processoTrfConexaoManager");
	private ProcessoSegredoManager processoSegredoManager = ComponentUtil.getComponent("processoSegredoManager");
	private ProcessoParteSigiloManager processoParteSigiloManager = ComponentUtil.getComponent("processoParteSigiloManager");
	private CaixaRepresentanteManager caixaRepresentanteManager = ComponentUtil.getComponent("caixaRepresentanteManager");
	private SessaoEnteExternoManager sessaoEnteExternoManager = ComponentUtil.getComponent("sessaoEnteExternoManager");
	private ProcessoTrfRedistribuicaoManager processoTrfRedistribuicaoManager = ComponentUtil.getComponent("processoTrfRedistribuicaoManager");
	private ProcessoParteHistoricoManager processoParteHistoricoManager = ComponentUtil.getComponent("processoParteHistoricoManager");
	private ProcessoTagManager processoTagManager = ComponentUtil.getComponent("processoTagManager");
	private LembreteManager lembreteManager = ComponentUtil.getComponent("lembreteManager");
	private LembretePermissaoManager lembretePermissaoManager = ComponentUtil.getComponent("lembretePermissaoManager");
	private ProcessoManager processoManager = ComponentUtil.getComponent("processoManager");
	private ParametroManager parametroManager = ComponentUtil.getComponent("parametroManager");
	private EntityLogManager entityLogManager = ComponentUtil.getComponent("entityLogManager");
	private SolicitacaoNoDesvioManager solicitacaoNoDesvioManager = ComponentUtil.getComponent("solicitacaoNoDesvioManager");
	private SessaoPautaProcessoTrfManager sessaoPautaProcessoManager = ComponentUtil.getComponent("sessaoPautaProcessoTrfManager");
	private SessaoManager sessaoManager = ComponentUtil.getComponent("sessaoManager");
	private QuadroAvisoManager quadroAvisoManager = ComponentUtil.getComponent("quadroAvisoManager");
	private ProcessoDocumentoFavoritoManager processoDocumentoFavoritoManager = ComponentUtil.getComponent("processoDocumentoFavoritoManager");
	private NotaSessaoJulgamentoManager notaSessaoJulgamentoManager = ComponentUtil.getComponent("notaSessaoJulgamentoManager");
	private ModeloProclamacaoJulgamentoManager modeloProclamacaoJulgamentoManager = ComponentUtil.getComponent("modeloProclamacaoJulgamentoManager");
	private LogHistoricoMovimentacaoManager logHistoricoMovimentacaoManager = ComponentUtil.getComponent("logHistoricoMovimentacaoManager");
	private VisibilidadePessoaDocumentoIdentificacaoManager visibilidadePessoaDocumentoIdentificacaoManager = ComponentUtil.getComponent("visibilidadePessoaDocumentoIdentificacaoManager");
	
	public UnificacaoAction() {}	

	public void pesquisar(){
		List<Criteria> criterios = new ArrayList<Criteria>(0);
		criterios.addAll(getCriteriosTelaPesquisa());
		
		try {			
			model = new EntityDataModel<Pessoa>(Pessoa.class, super.facesContext, getRetriever());
			model.setCriterias(criterios);
		} catch (Exception e) {
			mostraMensagem(true, null, "Ocorreu um erro ao executar a pesquisa: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/** 
	 * Este método adiciona os critérios de pesquisa preenchidos na tela.
	 * 
	 * @return Lista de critérios de pesquisa
	 */
	private List<Criteria> getCriteriosTelaPesquisa(){
		List<Criteria> criterios = new ArrayList<Criteria>(0);
		
		criterios.add(Criteria.equals("unificada", Boolean.FALSE));
		if(filtro.getInTipoPessoaSearch() != null){
			criterios.add(Criteria.equals("inTipoPessoa", filtro.getInTipoPessoaSearch()));
		}
		
		criterios.add(Criteria.notEquals("idPessoa", getPessoaPrincipal().getIdPessoa()));
		//		NOME DA PESSOA
		if(filtro.getNome() != null && !filtro.getNome().isEmpty()) {
			criterios.add(Criteria.contains("nome", filtro.getNome()));
		}
		//		CPF DA PESSOA
		if(filtro.getIdentificadorBuscaPessoa() != 0 && filtro.getCPFSearch() != null && !filtro.getCPFSearch().isEmpty()) {
			criterios.add(Criteria.equals("pessoaDocumentoIdentificacaoList.numeroDocumento", filtro.getCPFSearch()));
		}
		//		CNPJ DA PESSOA
		if(filtro.getIdentificadorBuscaPessoa() != 0 && filtro.getCNPJSearch() != null && !filtro.getCNPJSearch().isEmpty()) {
			criterios.add(Criteria.equals("pessoaDocumentoIdentificacaoList.numeroDocumento", filtro.getCNPJSearch()));
		}
		//		CNPJ DO ORGAO VINCULACAO DA PESSOA
		if(filtro.getIdentificadorBuscaPessoa() != 0 && filtro.getCNPJOVSearch() != null && !filtro.getCNPJOVSearch().isEmpty()) {
			Integer[] idPessoasAutoridadesComOrgaoVinculacao = obtemIDPessoaAutoridadeComOrgaoVinculacao(filtro.getCNPJOVSearch());
			if(idPessoasAutoridadesComOrgaoVinculacao == null) {
				idPessoasAutoridadesComOrgaoVinculacao = inviabilizaPesquisaPorIDPessoa();
			}
			criterios.add(Criteria.in("idPessoa", idPessoasAutoridadesComOrgaoVinculacao));
		}
		//		ID DA PESSOA
		if(filtro.getIdPessoa() != null && !filtro.getIdPessoa().isEmpty() 
				&& (!filtro.getIdPessoa().equals(String.valueOf(getPessoaPrincipal().getIdPessoa())))) {
			criterios.add(Criteria.equals("idPessoa", Integer.parseInt(filtro.getIdPessoa())));
		}
		//		NOME ALTERNATIVO
		if(filtro.getNomeAlternativo() != null && !filtro.getNomeAlternativo().isEmpty()) {
			Integer[] idPessoaComNomeAlternativoENomeFantasia = obtemIDPessoaComNomeAlternativoENomeFantasia(filtro.getNomeAlternativo());
			if(idPessoaComNomeAlternativoENomeFantasia == null) {
				idPessoaComNomeAlternativoENomeFantasia = inviabilizaPesquisaPorIDPessoa();
			}
			criterios.add(Criteria.in("idPessoa", idPessoaComNomeAlternativoENomeFantasia));

		}
		//		DATA NASCIMENTO
		if(filtro.isBuscaNascimento() && filtro.getDataNascimento() != null) {
			Integer[] idPessoasFisicasPorDataNascimento = obtemIDPessoaFisicaPorDataNascimento(filtro.getDataNascimento());
			if(idPessoasFisicasPorDataNascimento == null) {
				idPessoasFisicasPorDataNascimento = inviabilizaPesquisaPorIDPessoa();
			}
			criterios.add(Criteria.in("idPessoa", idPessoasFisicasPorDataNascimento));
		}
		//		DATA ABERTURA
		if(!filtro.isBuscaNascimento() && filtro.getDataAbertura() != null) {
			Integer[] idPessoasJuridicasPorDataAbertura = obtemIDPessoaJuridicaPorDataAbertura(filtro.getDataAbertura());
			if(idPessoasJuridicasPorDataAbertura == null) {
				idPessoasJuridicasPorDataAbertura = inviabilizaPesquisaPorIDPessoa();
			}
			criterios.add(Criteria.in("idPessoa", idPessoasJuridicasPorDataAbertura));				

		}
		
		//		ORGAO DE VINCULACAO
		if(filtro.getOrgaoVinculacao() != null) {
			Integer[] idPessoasEnteAutoridadePorOrgaoVinculacao = obtemIDPessoaEnteAutoridadePorOrgaoVinculacao(filtro.getOrgaoVinculacao());
			if(idPessoasEnteAutoridadePorOrgaoVinculacao == null) {
				idPessoasEnteAutoridadePorOrgaoVinculacao = inviabilizaPesquisaPorIDPessoa();
			}
			criterios.add(Criteria.in("idPessoa", idPessoasEnteAutoridadePorOrgaoVinculacao));
		}
		return criterios;
	}
	
	/**
	 * para evitar excluir do filtro de pessoas devido ao fato de nao existir pessoas com as caracteristicas passadas e evitar exibir todos os resultados
	 * mais uma vez, este metodo é responsavel por gerar um Integer[] com a id -1.
	 * @return Integer[] com valor -1
	 */
	private Integer[] inviabilizaPesquisaPorIDPessoa() {
		Integer[] retorno = new Integer[1];
		retorno[0] = -1;
		return retorno;
	}

	private Integer[] obtemIDPessoaEnteAutoridadePorOrgaoVinculacao(PessoaJuridica orgaoVinculacao) {
		Integer[] retorno = null;
		
		List<PessoaAutoridade> pessoasAutoridades = pessoaAutoridadeManager.findByOrgaoVinculacao(orgaoVinculacao);
		if(pessoasAutoridades != null && !pessoasAutoridades.isEmpty()) {
			retorno = new Integer[pessoasAutoridades.size()];
			int counter = 0;
			for (PessoaAutoridade pessoa : pessoasAutoridades) {
				retorno[counter] = pessoa.getIdPessoa();
				counter++;
			}
		}
		return retorno;
	}

	private Integer[] obtemIDPessoaJuridicaPorDataAbertura(Date dataAbertura) {
		Integer[] retorno = null;
		
		List<PessoaJuridica> pessoasJuridicas = pessoaJuridicaManager.recuperaPessoasJuridicasPorDataAbertura(dataAbertura);
		if(pessoasJuridicas != null && !pessoasJuridicas.isEmpty()) {
			retorno = new Integer[pessoasJuridicas.size()];
			int counter = 0;
			for (PessoaJuridica pessoa : pessoasJuridicas) {
				retorno[counter] = pessoa.getIdPessoa();
				counter++;
			}
		}
		return retorno;
	}

	private Integer[] obtemIDPessoaFisicaPorDataNascimento(Date dataNascimento) {
		Integer[] retorno = null;
		
		List<PessoaFisica> pessoasFisicas = pessoaFisicaManager.recuperaPessoasFisicasPorDataNascimento(dataNascimento);
		if(pessoasFisicas != null && !pessoasFisicas.isEmpty()) {
			retorno = new Integer[pessoasFisicas.size()];
			int counter = 0;
			for (PessoaFisica pessoa : pessoasFisicas) {
				retorno[counter] = pessoa.getIdPessoa();
				counter++;
			}
		}
		return retorno;
	}

	private Integer[] obtemIDPessoaComNomeAlternativoENomeFantasia(String nomeAltFant) {
		Integer[] retorno = null;
		
		List<PessoaJuridica> pessoasNomeFantasia =  pessoaJuridicaManager.recuperaPessoasJuridicasPorNomeFantasia(nomeAltFant);
		List<Pessoa> pessoasNomeAlternativo =  pessoaFisicaManager.recuperaPessoasFisicasPorNomeAlternativo(nomeAltFant);
		
		if((pessoasNomeFantasia != null && !pessoasNomeFantasia.isEmpty()) || pessoasNomeAlternativo != null && !pessoasNomeAlternativo.isEmpty()) {
			retorno = new Integer[pessoasNomeFantasia.size() + pessoasNomeAlternativo.size()];
			
			int counter = 0;
			for (Pessoa pessoaJuridica : pessoasNomeFantasia) {
				retorno[counter] = pessoaJuridica.getIdPessoa();
				counter++;
			}
			for (Pessoa pessoas : pessoasNomeAlternativo) {
				retorno[counter] = pessoas.getIdPessoa();
				counter++;
			}	
		}
		return retorno;
	}

	private Integer[] obtemIDPessoaAutoridadeComOrgaoVinculacao(String cnpjOrgaoVinculacao) {
		Integer[] retorno = null;
		
		List<PessoaDocumentoIdentificacao> cnpjs = pessoaDocumentoIdentificacaoManager.findByNumeroDocumento(cnpjOrgaoVinculacao);
		
		if(cnpjs != null && !cnpjs.isEmpty()) {
			List<Integer> idOrgaosvinculacao = new ArrayList<Integer>(0);
			
			for (PessoaDocumentoIdentificacao documento : cnpjs) {
				idOrgaosvinculacao.add(documento.getPessoa().getIdPessoa());
			}
			
			if(!idOrgaosvinculacao.isEmpty()) {
				List<PessoaJuridica> orgaosVinculacao = pessoaJuridicaManager.recuperarPessoasJuridicasPorListaID(idOrgaosvinculacao);
				
				if(!orgaosVinculacao.isEmpty()) {
					List<PessoaAutoridade> autoridades = new ArrayList<PessoaAutoridade>();
					
					for (PessoaJuridica orgaoVinculacao : orgaosVinculacao) {
						autoridades.addAll(pessoaAutoridadeManager.findByOrgaoVinculacao(orgaoVinculacao));
					}
					
					retorno = new Integer[autoridades.size()];
					
					int counter = 0;
					for (PessoaAutoridade autoridade : autoridades) {
						retorno[counter] = autoridade.getIdPessoa();
						counter++;
					}			
				}
			}
		}
		return retorno;
	}

	@Override
	protected DataRetriever<Pessoa> getRetriever() {
		final PessoaManager manager = (PessoaManager)getManager();
		final Integer tableCount = pessoaGridCount;
		DataRetriever<Pessoa> retriever = new DataRetriever<Pessoa>() {
			@Override
			public Pessoa findById(Object id) throws Exception {
				try {
					return manager.findById(id);
				} catch (PJeBusinessException e) {
					throw new Exception(e);
				}
			}
			
			@Override
			public List<Pessoa> list(Search search) {
				return manager.list(search);
			}
			@Override
			public long count(Search search) {
				if (tableCount != null && tableCount >=0){
					return tableCount;
				}

				return manager.count(search);
			}
			@Override
			public Object getId(Pessoa obj){
				return manager.getId(obj);
			}
		};
		return retriever;
	}
	
	/**
	 * Metodo utilizado para limpar os campos de pesquisa de pessoas secundarias da unificacao
	 */
	public void limparTodosCamposFiltros() {
		filtro.limparTodosCampos();
	}

	/** 
	 * Metodo responsavel por retirar a pessoa selecionada da lista de pessoas secundarias da unificacao
	 * @param Pessoa pessoaSelecionada
	 */
	public void removePessoaListaUnificacao(Pessoa pessoaSelecionada) {
		getPessoasSecundariasDaUnificacao().remove(pessoaSelecionada);
	}
	
	/**
	 * Metodo utilizado para limpar os campos e reiniciar a unificacao
	 * @param Pessoa pessoaPrincipal 
	 */
	public void resetUnificacao() {
		limparTodosCamposFiltros();
		resetFaseUnificacao();
		resetObjetosUnificacao();
	}
	
	private void resetObjetosUnificacao() {
		unificacaoVO.resetObjetosUnificacao();
	}
	
	/** 
	 * metodo auxiliar com funcao de reiniciar a pilha de controle de telas
	 */
	private void resetFaseUnificacao() {
		faseUnificacao = new Stack<FasesUnificacaoEnum>();
		faseUnificacao.add(FasesUnificacaoEnum.FASE_01_SELECAO_PESSOAS);
	}
	
	/**
	 * metodo que verifica os requisitos para prosseguir para a proxima fase.
	 * caso os requisitos para a proxima fase nao for atingido, procura a proxima fase que cumpre com os requisitos.
	 * @param deveParar - true para obrigar a parar na proxima fase.
	 */
	public void prossegueProximaFaseUnificacao() {
		switch (faseUnificacao.peek()) {

		case FASE_01_SELECAO_PESSOAS:
			try {
				procurarObjetosParaUnificacaoPessoasSecundarias();
				activeTab = "RESUMO_UNIFICACAO";
				faseUnificacao.add(FasesUnificacaoEnum.RESUMO_UNIFICACAO);
			}catch (Exception e) {
				e.printStackTrace();
				mostraMensagem(true, null, e.getCause().toString());
			}
			break;		
		default:
			break;
		}		
	}

	/**
	 * Metodo responsavel por retornar a pessoa principal da unificacao
	 * @return Pessoa pessoa principal
	 */
	public Pessoa getPessoaPrincipal() {
		return unificacaoVO.getPessoaPrincipal();
	}
	
	/**
	 * Metodo responsavel por retornar uma lista com a pessoa principal da unificacao para exibicao em tela
	 * @return List<Pessoa> pessoa principal
	 */
	public List<Pessoa> getPessoaPrincipalList() {
		List<Pessoa> retorno = new ArrayList<Pessoa>(0);
		retorno.add(getPessoaPrincipal());
		return retorno;
	}
	
	/**
	 * Metodo responsavel por retornar a lista de pessoas secundarias que serao unificadas à pessoa principal
	 * @return List<Pessoa> pessoas secundarias
	 */
	public List<Pessoa> getPessoasSecundariasDaUnificacao() {
		return unificacaoVO.getPessoasSecundariasUnificacao();
	}
	
	public UnificacaoFiltroVO getFiltro() {
		return filtro;
	}
	
	/**
	 * Metodo responsavel por verificar se as opcoes escolhidas na tela estao corretamente selecionadas e encaminha para unificacao de pessoas
	 * LOGICA:
	 * -> se nao existirem ProcessosParte em conflito(TODO)
	 * --> se existem documentos principais(TODO)
	 * ---> se houver problemas na configuracao dos documentos(TODO)
	 * ----> se sim, exibe mensagem de erro (TODO)
	 * --> se existem documentos secundarios (TODO)
	 * ---> se existir problemas com documentos secundarios (TODO) 
	 * ----> mensagem de erro (TODO)
	 * --> se tudo correto, prossegue para unificacao 
	 * -> se existirem processos parte em conflito, mensagem de Erro (TODO)
	 */
	@Transactional
	public void finalizarUnificacaoPessoas() {
		try{
			unificacaoManager.finalizarUnificacao(unificacaoVO);
			activeTab = "CONCLUSAO_UNIFICACAO";
			faseUnificacao.add(FasesUnificacaoEnum.CONCLUSAO_UNIFICACAO);
			mostraMensagem(false, "pje.unificacao.unificacaoConcluida", null);
		} catch (Exception e) {
			mostraMensagem(true, "pje.unificacao.unificacaoErro", e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Metodo responsavel por montar as mensagems para exibi ao na tela
	 * @param tipoMensagem - true se for uma mensagem de erro / false se for uma mensagem de informacao / null se for uma mensagem de warn
	 * @param localizacaoMensagem - localizacao da mensagem no arquivo entitymessages.properties
	 */
	private void mostraMensagem(Boolean tipoMensagem, String localizacaoMensagem, String complemento) {
		String complementoTemp = "";
		if(complemento != null) {
			complementoTemp = complemento;
		}
		if(localizacaoMensagem != null) {
			FacesMessages.instance().clear();
			if (tipoMensagem == null) {
				FacesMessages.instance().add(Severity.WARN, FacesUtil.getMessage("entity_messages", localizacaoMensagem, complementoTemp));
			}else  if (tipoMensagem) {
				FacesMessages.instance().add(Severity.ERROR, FacesUtil.getMessage("entity_messages", localizacaoMensagem, complementoTemp));
			} else {
				FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", localizacaoMensagem, complementoTemp));
			}	
		}else {
			FacesMessages.instance().add(Severity.ERROR, complementoTemp);
		}
	}
	
	/**
	 * metodo responsavel por retornar para a ultima fase da unificacao registrada.
	 */
	public void voltarUltimaFaseUnificacao() {
		faseUnificacao.pop();
	}
	
	/**
	 * metodo responsavel por aplicar as regras de visualizacao do botao 'prosseguir'
	 * @return true / false
	 */
	public boolean exibeBotaoProssegue() {
		if(getPessoasSecundariasDaUnificacao().size() > 0) {
			return true;
		}else {
			return false;
		}
	}
	
	/**
	 * metodo responsavel por aplicar as regras de visualizacao do botao 'voltar'
	 * @return true / false
	 */
	public boolean exibeBotaoVoltar() {
		if(faseUnificacao.peek().equals(FasesUnificacaoEnum.FASE_01_SELECAO_PESSOAS)) {
			return false;
		}
		return true;
	}

	/**
	 * retorna o manager da entidade passada como tipo na  extends BaseAction<Unificacao>
	 */
	@Override
	protected PessoaManager getManager() {
		return pessoaManager;
	}

	@Override
	public EntityDataModel<Pessoa> getModel() {
		return this.model;
	}
	
	/**
	 * Metodo utilizado para iniciar a unificacao, colocando a pessoa original (a qual todas vao ser unidas) e separando os documentos e processos parte desta.
	 * @param Pessoa pessoaPrincipal
	 */
	public void iniciarUnificacao(Pessoa pessoaPrincipal) {
		if(pessoaPrincipal != null) {
			unificacaoVO = new UnificacaoVO(pessoaPrincipal,(Usuario) Contexts.getSessionContext().get("usuarioLogado"), tipoPessoaManager.recuperaTipoPessoaPorExistenciaTabela(pessoaPrincipal));
			filtro = new UnificacaoFiltroVO(unificacaoVO.getTipoRealPessoaPrincipal());
			inserePessoaPorTipoPessoa();
			insereUsuarioPessoaPrincipal();
			insereUsuarioLoginPessoaPrincipal();
			resetFaseUnificacao();
			pesquisar();
			procurarObjetosParaUnificacaoPessoaPrincipal();
		}
	}

	public String getSelectedTab() {
		return activeTab;
	}
	
	public void setSelectedTab(String tab) {
		activeTab = tab;
	}
	
	/**
	 * metodo responsavel por converter a string passa por parametro, relativa à fase na qual se deseja comparar e encaminha para a comparacao com a fase atual.
	 * @return true se a fase atual for igual à fase passada em parametro.
	 */
	public boolean verificaFaseAtual(String fasePretendida) {
		boolean resultado = false;
		
		if (fasePretendida.equals(IDENTIFICADOR_FASE_SELECAO_PESSOAS)) {
			resultado = faseUnificacao.peek() == FasesUnificacaoEnum.FASE_01_SELECAO_PESSOAS;
		} else if (fasePretendida.equals(IDENTIFICADOR_FASE_RESUMO_UNIFICACAO)) {
			resultado = faseUnificacao.peek() == FasesUnificacaoEnum.RESUMO_UNIFICACAO;			
		} else if (fasePretendida.equals(IDENTIFICADOR_FASE_CONCLUSAO_UNIFICACAO)) {
			resultado = faseUnificacao.peek() == FasesUnificacaoEnum.CONCLUSAO_UNIFICACAO;			
		}
		return resultado;
	}
	
	/** 
	 * Metodo responsavel por verificar se a pessoa principal é uma pessoa fisica
	 * @return true se a pessoa principal for fisica / false
	 */
	public boolean isPessoaPrincipalTipoFisica() {
		return unificacaoVO.getTipoRealPessoaPrincipal().equals(TipoPessoaEnum.F);
	}
	
	/**
	 * Metodo responsavel por verificar se a pessoa principal é uma pessoa juridica
	 * @return true se a pessoa principal for juridica / false
	 */
	public boolean isPessoaPrincipalTipoJuridica() {
		return unificacaoVO.getTipoRealPessoaPrincipal().equals(TipoPessoaEnum.J);
	}
	
	/** NAO UTILIZADO AINDA
	 * Metodo responsavel por verificar se a pessoa principal é uma pessoa autoridade
	 * @return true se a pessoa principal for autoridade / false
	 */
	public boolean isPessoaPrincipalTipoAutoridade() {
		return unificacaoVO.getTipoRealPessoaPrincipal().equals(TipoPessoaEnum.A);
	}
	
	/**
	 * Metodo para limpar os campos equivalentes do search
	 */
	public void clearCpfCnpj(){
		filtro.limparCampoCpfCnpj();
	}
	
	/**
	 * metodo para limpar o campo oposto ao escolhido (data nascimento / data abertura), evitando assim a entrada do mesmo campo na query
	 */
	public void limparCamposDataAberturaDataNascimentoAlternados(){
		filtro.limparCamposDataAberturaDataNascimentoAlternados();
	}
	
	/**
	 * Metodo utilizado para adicionar pessoas selecionadas na lista de pessoas a serem unificadas
	 * @param Pessoa pessoaSelecionada - pessoa que sera unificada - pessoa secundaria da unificacao
	 */
	public void adicionarPessoaSecundariaUnificacao(Pessoa pessoaSelecionada) {
		if (!pessoaSelecionada.equals(getPessoaPrincipal()) && !getPessoasSecundariasDaUnificacao().contains(pessoaSelecionada)) {
			getPessoasSecundariasDaUnificacao().add(pessoaSelecionada);
		}
	}
	
	/**
	 * Metodo responsavel por verificar se a pessoa esta na lista de conflitos de partes.
	 * @param Pessoa pessoaUnificada
	 * @return String com aviso de conflito se pessoa estiver na lista de conflitos / String vazia se nao estiver
	 */
	public String verificaConflitoPartesPessoaUnificada(Pessoa pessoaSecundaria) {
		return "";
	}
	
	private void inserePessoaPorTipoPessoa() {
		if(unificacaoVO.getTipoRealPessoaPrincipal().equals(TipoPessoaEnum.F)) {
			unificacaoVO.setPessoaFisicaPessoaPrincipal(pessoaFisicaManager.encontraPessoaFisicaPorPessoa(unificacaoVO.getPessoaPrincipal()));
		}else if (unificacaoVO.getTipoRealPessoaPrincipal().equals(TipoPessoaEnum.J)) {
			unificacaoVO.setPessoaJuridicaPessoaPrincipal(pessoaJuridicaManager.encontraPessoaJuridicaPorPessoa(unificacaoVO.getPessoaPrincipal()));
		} else {
			unificacaoVO.setPessoaAutoridadePessoaPrincipal(pessoaAutoridadeManager.encontraPessoaAutoridadePorPessoa(unificacaoVO.getPessoaPrincipal()));
		}
	}
	
	private void insereUsuarioPessoaPrincipal() {
		unificacaoVO.setUsuarioPessoaPrincipal(usuarioManager.encontrarPorPessoa(unificacaoVO.getPessoaPrincipal()));
	}
	
	private void insereUsuarioLoginPessoaPrincipal() {
		unificacaoVO.setUsuarioLoginPessoaPrincipal(usuarioLoginManager.encontrarPorPessoa(unificacaoVO.getPessoaPrincipal()));
	}
	
	/**
	 * metodo responsavel por encaminhar para busca em banco de dados dos objetos da unificacao da pessoa principal
	*/
	private void procurarObjetosParaUnificacaoPessoaPrincipal() {
		//SOMENTE PESSOAS FISICAS OU PESSOAS JURIDICAS
		if((unificacaoVO.getTipoRealPessoaPrincipal().equals(TipoPessoaEnum.F)|| unificacaoVO.getTipoRealPessoaPrincipal().equals(TipoPessoaEnum.J))) {
			unificacaoVO.addMeioContatoProprietariaPessoaPrincipal(meioContatoManager.recuperaMeioContatoProprietarios(unificacaoVO.getPessoaPrincipal()));
			unificacaoVO.addNomeAlternativoProprietariaPessoaPrincipal(nomeAlternativoManager.recuperaNomesAlternativosProprietarios(unificacaoVO.getPessoaPrincipal()));
		}
		//SOMENTE PESSOAS PRINCIPAIS FISICAS
		if(unificacaoVO.getTipoRealPessoaPrincipal().equals(TipoPessoaEnum.F)) {
			unificacaoVO.addCaracteristicasFisicasPessoaPrincipal(caracteristicaFisicaManager.recuperaCaracteristicasFisicas(unificacaoVO.getPessoaPrincipal()));
			unificacaoVO.addCaixaRepresentantePessoaPrincipal(caixaRepresentanteManager.getCaixaRepresentanteByRepresentante(unificacaoVO.getPessoaPrincipal().getIdPessoa()));
		}	
	}
	
	/**
	 * metodo responsavel por encaminhar para busca em banco de dados dos objetos da unificacao das pessoas secundarias
	 * @throws Exception 
	 */
	private void procurarObjetosParaUnificacaoPessoasSecundarias() throws Exception{
		for (Pessoa pessoaSecundaria : unificacaoVO.getPessoasSecundariasUnificacao()) {
			unificacaoVO.addLogsAcesso(logAcessoManager.recuperaLogsAcesso(pessoaSecundaria));
			unificacaoVO.addMeiosContatoCadastrados(meioContatoManager.recuperaMeioContatoCadastrados(pessoaSecundaria));
			unificacaoVO.addNomesAlternativosCadastrados(nomeAlternativoManager.recuperaNomesAlternativosCadastrados(pessoaSecundaria));
			unificacaoVO.addSegredoProcessosCadastrados(processoSegredoManager.recuperaSegredoProcessosCadastrados(pessoaSecundaria));
			unificacaoVO.addSessaoEnteExterno(sessaoEnteExternoManager.recuperaSessaoEnteExterno(pessoaSecundaria));
			unificacaoVO.addRedistribuicaoProcesso(processoTrfRedistribuicaoManager.recuperaRedistribuicoesProcessos(pessoaSecundaria));
			unificacaoVO.addProcessoParteHistorico(processoParteHistoricoManager.recuperaProcessosParteHistoricos(pessoaSecundaria));
			unificacaoVO.addLembrete(lembreteManager.recuperarLembretes(pessoaSecundaria));
			unificacaoVO.addLembretePermissao(lembretePermissaoManager.recuperarLembretesPermissao(pessoaSecundaria));
			unificacaoVO.addProcesso(processoManager.recuperarProcessosProtocolados(pessoaSecundaria));
			unificacaoVO.addParametros(parametroManager.recuperarParametrosCadastrados(pessoaSecundaria));
			unificacaoVO.addEntityLogs(entityLogManager.recuperarEntityLogs(pessoaSecundaria));
			unificacaoVO.addSolicitacaoNoDesvio(solicitacaoNoDesvioManager.recuperarSolicitacoesNoDesvio(pessoaSecundaria));
			unificacaoVO.addSessaoPautaProcessoInclusora(sessaoPautaProcessoManager.recuperarSessaoPautaProcessoPessoaInclusora(pessoaSecundaria));
			unificacaoVO.addSessaoPautaProcessoExclusora(sessaoPautaProcessoManager.recuperarSessaoPautaProcessoPessoaExclusora(pessoaSecundaria));
			unificacaoVO.addSessaoInclusora(sessaoManager.recuperarSessaoPessoaInclusora(pessoaSecundaria));
			unificacaoVO.addSessaoExclusora(sessaoManager.recuperarSessaoPessoaExclusora(pessoaSecundaria));
			unificacaoVO.addQuadroAviso(quadroAvisoManager.recuperarAvisosQuadroAviso(pessoaSecundaria));
			unificacaoVO.addProcessoDocumentoFavorito(processoDocumentoFavoritoManager.recuperarProcessosDocumentosFavoritos(pessoaSecundaria));
			unificacaoVO.addNotaSessaoJulgamento(notaSessaoJulgamentoManager.recuperarNotasSessaoJulgamento(pessoaSecundaria));
			unificacaoVO.addModeloProclamacaoJulgamento(modeloProclamacaoJulgamentoManager.recuperarModelos(pessoaSecundaria));
			unificacaoVO.addLogHistMov(logHistoricoMovimentacaoManager.recuperarLogs(pessoaSecundaria));
			unificacaoVO.addVisibilidadeDocumentosIdentificacao(visibilidadePessoaDocumentoIdentificacaoManager.recuperarVisibilidades(pessoaSecundaria));
			
			//SE A PESSOA PRINCIPAL FOR FISICA OU JURIDICA
			if((unificacaoVO.getTipoRealPessoaPrincipal().equals(TipoPessoaEnum.F))|| (unificacaoVO.getTipoRealPessoaPrincipal().equals(TipoPessoaEnum.J))) {
				unificacaoVO.addMeiosContatoProprietarios(meioContatoManager.recuperaMeioContatoProprietarios(pessoaSecundaria));
				unificacaoVO.addNomeAlternativoProprietarios(nomeAlternativoManager.recuperaNomesAlternativosProprietarios(pessoaSecundaria));
			}
			
			//SE A PESSOA PRINCIPAL FOR FISICA -- dados somente serão unificados se a pessoa principal for fisica
			if(unificacaoVO.getTipoRealPessoaPrincipal().equals(TipoPessoaEnum.F)) {
				unificacaoVO.addCaracteristicasFisicasPessoaSecundaria(caracteristicaFisicaManager.recuperaCaracteristicasFisicas(pessoaSecundaria));
				unificacaoVO.addConexaoPrevencaoProcessoPessoaSecundaria(conexaoPrevencaoManager.recuperaConexoesPrevencoes(pessoaSecundaria));
				unificacaoVO.addProcessoParteSigiloPessoaSecundaria(processoParteSigiloManager.recuperaProcessoParteSigilo(pessoaSecundaria));
				unificacaoVO.addCaixasRepresentantesPessoaSecundaria(caixaRepresentanteManager.getCaixaRepresentanteByRepresentante(pessoaSecundaria.getIdPessoa()));
			}
		}	
	}
	
	/**
	 * Metodo responsavel por retornar a lista de logs de acesso das pessoas secundarias, que serão unificadas
	 * @return List<LogAcesso>
	 */
	public List<LogAcesso> getLogsAcesso() {
		return unificacaoVO.getLogsAcessoPessoaSecundarias();
	}
	
	/**
	 * Metodo responsavel por retornar a lista de caracteristicas fisicas da pessoa principal
	 * @return List<CaracteristicaFisica>
	 */
	public List<CaracteristicaFisica> getCaracteristicaFisicaPessoaPrincipal() {
		return unificacaoVO.getCaracteristicaFisicaPessoaPrincipal();
	}
	
	/**
	 * Metodo responsavel por retornar a lista de caracteristicas fisicas das pessoas secundarias, 
	 * que serão unificadas à pessoa principal
	 * @return List<CaracteristicaFisica>
	 */
	public List<CaracteristicaFisica> getCaracteristicaFisicaPessoaSecundaria() {
		return unificacaoVO.getCaracteristicaFisicaPessoaSecundaria();
	}
	
	/**
	 * Metodo responsavel por retornar a lista de caracteristicas fisicas das pessoas secundarias, 
	 * que estão em conflito com as caracteristicas fisicas da pessoa principal ou de pessoas secundarias adicionadas
	 * anteriormente.
	 * nao serao unficadas, permanecendo com a pessoa proprietaria.
	 * @return List<CaracteristicaFisica>
	 */
	public List<CaracteristicaFisica> getCaracteristicaFisicaConflito() {
		return unificacaoVO.getCaracteristicaFisicaConflito();
	}
	
	/**
	 * metodo responsavel por retornar os meios de contato cadastrados pelas pessoas secundarias.
	 * @return List<MeioContato>
	 */
	public List<MeioContato> getMeiosContatosCadastradosPessoaSecundarias() {
		return unificacaoVO.getMeiosContatosCadastradosPessoaSecundarias();
	}
	
	/**
	 * metodo resposnavel por retornar os meios de contato onde as pessoas secundarias sao as proprietarias e
	 * que serão unificados. 
	 * @return
	 */
	public List<MeioContato> getMeiosContatosProprietariasPessoasSecundarias() {
		return unificacaoVO.getMeiosContatosProprietariasPessoasSecundarias();
	}
	
	public List<MeioContato> getMeiosContatosConflito() {
		return unificacaoVO.getMeiosContatosConflito();
	}
	
	public List<PessoaNomeAlternativo> getNomesAlternativosCadastradosPessoasSecundarias() {
		return unificacaoVO.getNomesAlternativosCadastradosPessoasSecundarias();
	}
	
	public List<PessoaNomeAlternativo> getNomesAlternativosProprietariasPessoasSecundarias() {
		return unificacaoVO.getNomesAlternativosProprietariasPessoasSecundarias();
	}

	public List<PessoaNomeAlternativo> getNomesAlternativosConflito() {
		return unificacaoVO.getNomesAlternativosConflito();
	}
	
	public List<ProcessoTrfConexao> getConexoesPrevencaoPessoasSecundarias() {
		return unificacaoVO.getConexoesPrevencaoPessoasSecundarias();
	}
	
	public List<ProcessoSegredo> getProcessoSegredoCadastradosPessoaSecundarias() {
		return unificacaoVO.getProcessoSegredoCadastradosPessoaSecundarias();
	}
	
	public List<ProcessoParteSigilo> getProcessosParteSigiloPessoasSecundarias() {
		return unificacaoVO.getProcessosParteSigiloPessoasSecundarias();
	}

	public List<CaixaRepresentante> getCaixasRepresentantesPessoasSecundarias() {
		return unificacaoVO.getCaixasRepresentantesPessoasSecundarias();
	}
	
	public List<CaixaRepresentante> getCaixasRepresentantesConflito() {
		return unificacaoVO.getCaixasRepresentantesConflito();
	}
	
	public List<SessaoEnteExterno> getSessoesEntesExternosPessoaSecundarias() {
		return unificacaoVO.getSessoesEntesExternosPessoaSecundarias();
	}
	
	public List<ProcessoTrfRedistribuicao> getRedistribuicoesProcessosPessoaSecundarias() {
		return unificacaoVO.getRedistribuicoesProcessosPessoaSecundarias();
	}
	
	public List<ProcessoParteHistorico> getProcessosParteHistoricosPessoaSecundarias() {
		return unificacaoVO.getProcessosParteHistoricosPessoaSecundarias();
	}
	
	public List<ProcessoTag> getProcessosTagPessoaSecundarias() {
		return unificacaoVO.getProcessosTagPessoaSecundarias();
	}
	
	public List<Lembrete> getLembretesPessoaSecundarias() {
		return unificacaoVO.getLembretesPessoaSecundarias();
	}
	
	public List<LembretePermissao> getPermissoesLembretesPessoaSecundarias() {
		return unificacaoVO.getPermissoesLembretesPessoaSecundarias();
	}
	
	public List<Processo> getProcessosPessoaSecundarias() {
		return unificacaoVO.getProcessosPessoaSecundarias();
	}
	
	public List<Parametro> getParametrosPessoaSecundarias() {
		return unificacaoVO.getParametrosPessoaSecundarias();
	}
	
	public List<EntityLog> getEntityLogPessoaSecundarias() {
		return unificacaoVO.getEntityLogPessoaSecundarias();
	}
	
	public List<SolicitacaoNoDesvio> getSolicitacoesNoDesvioPessoaSecundarias() {
		return unificacaoVO.getSolicitacoesNoDesvioPessoaSecundarias();
	}
	
	public List<SessaoPautaProcessoTrf> getSessaoPautaProcessoPessoaSecundarias() {
		return unificacaoVO.getSessaoPautaProcessoPessoaSecundarias();
	}
	
	public List<SessaoPautaProcessoTrf> getSessaoPautaProcessoPessoaSecundariasExclusoras() {
		return unificacaoVO.getSessaoPautaProcessoPessoaSecundariasExclusoras();
	}
	
	public List<Sessao> getSessaoPessoaSecundariasInclusoras() {
		return unificacaoVO.getSessaoPessoaSecundariasInclusoras();
	}
	
	public List<Sessao> getSessaoPessoaSecundariasExclusoras() {
		return unificacaoVO.getSessaoPessoaSecundariasExclusoras();
	}
	
	public List<QuadroAviso> getAvisosPessoaSecundarias() {
		return unificacaoVO.getAvisosPessoaSecundarias();
	}
	
	public List<ProcessoDocumentoFavorito> getProcsDocFavoritosPessoaSecundarias() {
		return unificacaoVO.getProcsDocFavoritosPessoaSecundarias();
	}
	
	public List<NotaSessaoJulgamento> getNotasSessaoJulgamentoPessoaSecundarias() {
		return unificacaoVO.getNotasSessaoJulgamentoPessoaSecundarias();
	}
	
	public List<ModeloProclamacaoJulgamento> getModelosProclamacaoJulgamentoPessoasSecundarias() {
		return unificacaoVO.getModelosProclamacaoJulgamentoPessoasSecundarias();
	}
	
	public List<LogHistoricoMovimentacao> getLogsHistoricoMovimentacaoPessoasSecundarias() {
		return unificacaoVO.getLogsHistoricoMovimentacaoPessoasSecundarias();
	}
	
	public List<VisibilidadePessoaDocumentoIdentificacao> getVisibilidadesDocumentoIdentificacaoPessoasSecundarias() {
		return unificacaoVO.getVisibilidadesDocumentoIdentificacaoPessoasSecundarias();
	}
	
}