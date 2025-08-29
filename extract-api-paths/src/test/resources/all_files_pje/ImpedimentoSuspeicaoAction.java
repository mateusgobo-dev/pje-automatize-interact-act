package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.event.ValueChangeEvent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.pje.manager.PessoaFisicaManager;
import br.com.infox.pje.manager.PessoaJuridicaManager;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.MunicipioManager;
import br.jus.cnj.pje.nucleo.manager.PessoaAdvogadoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaAutoridadeManager;
import br.jus.cnj.pje.nucleo.service.ImpedimentoSuspeicaoService;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.je.pje.manager.EleicaoManager;
import br.jus.pje.je.entidades.Eleicao;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.ImpedimentoSuspeicao;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaAutoridade;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.enums.RegraImpedimentoSuspeicaoEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Classe responsavel pelo controller da aba impedimento/suspeicao no cadastro do processo com o perfil de Administrador de autuacao.
 * 
 */
@Name(ImpedimentoSuspeicaoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class ImpedimentoSuspeicaoAction extends BaseAction<ImpedimentoSuspeicao> {
	
	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 1009452071728722555L;

	public static final String NAME = "impedimentoSuspeicaoAction";
	
	private List<RegraImpedimentoSuspeicaoEnum> listaRegras;
	
	private RegraImpedimentoSuspeicaoEnum regra;
	
	private Boolean canEdit = Boolean.FALSE;
	
	private ImpedimentoSuspeicao impedimentoSuspeicao;
	
	private Pessoa pessoaParte;
	
	private PessoaAdvogado pessoaAdvogado;
	
	private List<Estado> listaEstados;
	
	private List<Municipio> listaMunicipios;
	
	private Estado estado;
	
	private List<Eleicao> listaEleicoesAtivas;
	
	private EntityDataModel<ImpedimentoSuspeicao> listaResultadoPesquisa;
	
	private TipoPessoaEnum inTipoPessoa = TipoPessoaEnum.F;
	
	private List<TipoPessoaEnum> listaTipoPessoa;
	
	private List<PessoaAutoridade> listaEnteAutoridade;
	
	/**
	 * Inicializa as variaveis na criacao da instancia da classe.
	 */
	@Create
	public void init() {
		limpar();
	}

	/**
	 * Limpa os campos do formulario.
	 */
	public void limpar() {
		canEdit = false;
		regra = null;
		listaResultadoPesquisa = null;
		if (CollectionUtils.isEmpty(listaRegras)) {
			listaRegras = recuperarListaRegras();
		}
		inicializarImpedimentoSuspeicao();
		inicializarPessoaParteAdvogado();
	}
	
	/**
	 * Inicializa a variavel pessoaParteAdvogado. 
	 */
	public void inicializarPessoaParteAdvogado() {
		inTipoPessoa = TipoPessoaEnum.F;
		pessoaParte = instanciarPessoaParte();
		pessoaAdvogado = new PessoaAdvogado();
	}
	
	private Pessoa instanciarPessoaParte() {
		if (TipoPessoaEnum.J.equals(inTipoPessoa)) {
			return new PessoaJuridica();
		}
		if (TipoPessoaEnum.A.equals(inTipoPessoa)) {
			return new PessoaAutoridade();
		}
		return new PessoaFisica();
	}
	
	/**
	 * Inicializa o objeto impedimentoSuspeicao.
	 */
	private void inicializarImpedimentoSuspeicao() {
		impedimentoSuspeicao = new ImpedimentoSuspeicao();
		impedimentoSuspeicao.setPoloAtivo(Boolean.FALSE);
		impedimentoSuspeicao.setPoloPassivo(Boolean.FALSE);
		impedimentoSuspeicao.setPoloAtivo(Boolean.TRUE);
		inicializarMunicipio();
	}
	
	/**
	 * Inicializa o objeto da classe Municipio.
	 */
	private void inicializarMunicipio() {
		if (impedimentoSuspeicao.getRegraImpedimentoSuspeicaoEnum() != null 
				&& impedimentoSuspeicao.getRegraImpedimentoSuspeicaoEnum().equals(RegraImpedimentoSuspeicaoEnum.U)) {
			Municipio municipio = new Municipio();
			municipio.setMunicipio("");
			impedimentoSuspeicao.setMunicipio(municipio);
		}
	}
	
	public void preencherMunicipio(Municipio municipio) {
		if (municipio != null) {
			impedimentoSuspeicao.setMunicipio(municipio);
		} else {
			impedimentoSuspeicao.setMunicipio(new Municipio());
		}
	}
	
	public void preencherPessoaAutoridade(PessoaAutoridade pessoaAutoridade) {
		if (pessoaAutoridade != null) {
			impedimentoSuspeicao.setPessoaParteAdvogado(pessoaAutoridade);
			pessoaParte = pessoaAutoridade;
		}
	}
	
	/**
	 * Recupera a lista de regras para preenchimento da combo regra.
	 * 
	 * @return List<RegraImpedimentoSuspeicaoEnum> lista com as regras.
	 */
	private List<RegraImpedimentoSuspeicaoEnum> recuperarListaRegras() {
		List<RegraImpedimentoSuspeicaoEnum> listaRegrasLocal = new ArrayList<>();
		listaRegrasLocal.add(RegraImpedimentoSuspeicaoEnum.A);
		listaRegrasLocal.add(RegraImpedimentoSuspeicaoEnum.P);
		if (ParametroJtUtil.instance().justicaEleitoral()) {
			listaRegrasLocal.add(RegraImpedimentoSuspeicaoEnum.E);
			listaRegrasLocal.add(RegraImpedimentoSuspeicaoEnum.U);
		}
		return listaRegrasLocal;
	}
	
	/**
	 * Metodo responsavel pela pesquisa de impedimento suspeicao.
	 */
	public void pesquisar() {
		
		listaResultadoPesquisa = new EntityDataModel<>(ImpedimentoSuspeicao.class, super.facesContext, getRetriever());
		
		List<Criteria> criterios = new ArrayList<>(0);
		criterios.addAll(getCriteriosTelaPesquisa());
		try {
			listaResultadoPesquisa.setCriterias(criterios);
		} catch (NoSuchFieldException e) {
			logger.error("Erro ao criar os filtros para a pesquisa: ", e.getMessage());
		}
		listaResultadoPesquisa.addOrder("id", Order.ASC);
		
	}
	
	/**
	 * Cria a lista de filtros vindo da tela de pesquisa.
	 * 
	 * @return
	 */
	public List<Criteria> getCriteriosTelaPesquisa() {
		
		List<Criteria> criterios = new ArrayList<>(0);
		/*if (regra != null) {
			criterios.add(Criteria.equals("regraImpedimentoSuspeicaoEnum", regra));
		}*/
		if (impedimentoSuspeicao.getPessoaMagistrado() != null) {
			criterios.add(Criteria.equals("pessoaMagistrado.idUsuario", impedimentoSuspeicao.getPessoaMagistrado().getIdUsuario()));
		}
		return criterios;
		
	}
	
	/**
	 * Metodo responsavel pela exclusao do registro selecionado.
	 * 
	 * @param impedimentoSuspeicao ImpedimentoSuspeicao registro a ser excluido.
	 */
	public void remover(ImpedimentoSuspeicao impedimentoSuspeicao) {
		ImpedimentoSuspeicaoService impedimentoSuspeicaoService = ComponentUtil.getComponent(ImpedimentoSuspeicaoService.class);
		impedimentoSuspeicaoService.remover(impedimentoSuspeicao);
		canEdit = false;
		inicializarImpedimentoSuspeicao();
		FacesMessages.instance().add("Registro excluído com sucesso");
		FacesUtil.refreshFacesMessages();
	}
	
	/**
	 * No momento da edicao e feito a validacao do tipo de regra para preenchimento dos dados a serem utilizados pela view.
	 */
	public void validarTipoRegra() {
		if (impedimentoSuspeicao != null && impedimentoSuspeicao.getRegraImpedimentoSuspeicaoEnum() != null) {
			RegraImpedimentoSuspeicaoEnum regraImpedimentoSuspeicao = impedimentoSuspeicao.getRegraImpedimentoSuspeicaoEnum();
			// POR ELEICAO
			if (regraImpedimentoSuspeicao.equals(RegraImpedimentoSuspeicaoEnum.E)) {
				preencherDadosAnoEleicao();
			}
			// POR ESTADO
			if (regraImpedimentoSuspeicao.equals(RegraImpedimentoSuspeicaoEnum.U)) {
				preencherMunicipio(impedimentoSuspeicao.getMunicipio());
			}
		}
	}
	
	/**
	 * Limpa a descricao do motivo.
	 */
	public void limparDescricaoMotivo() {
		impedimentoSuspeicao.setDescricaoMotivo(null);
	}
	
	/**
	 * Metodo responsavel por carregar os dados do registro selecionado.
	 * 
	 * @param impedimentoSuspeicao ImpedimentoSuspeicao registro selecionado.
	 */
	public void editar(ImpedimentoSuspeicao impedimentoSuspeicao) {
		preencherDadosCadastro();
		try {
			this.impedimentoSuspeicao = impedimentoSuspeicao.clone();
		} catch (CloneNotSupportedException e) {
			lancarMsg(Severity.ERROR, "Erro ao editar o impedimento/suspeição.");
			return;
		}
		this.pessoaParte = impedimentoSuspeicao.getPessoaParteAdvogado();
		if (impedimentoSuspeicao.getPessoaParteAdvogado() != null) {
			this.inTipoPessoa = impedimentoSuspeicao.getPessoaParteAdvogado().getInTipoPessoa();
		}
		impedimentoSuspeicao.getRegraImpedimentoSuspeicaoEnum();
		preencherParteAdvogado();
		validarTipoRegra();
	}
	
	/**
	 * Preenche os dados da div ELEICAO.
	 */
	private void preencherDadosAnoEleicao() {
		if (CollectionUtils.isEmpty(listaEleicoesAtivas)) {
			listaEleicoesAtivas = ComponentUtil.getComponent(EleicaoManager.class).findEleicoes(true);
		}
	}
	
 	/**
 	 * Realiza a pesquisa da pessoa de acordo com o CPF informado.
 	 */
	public void pesquisarParteAdvogado() {
		if (TipoPessoaEnum.F.equals(inTipoPessoa)) {
			pesquisarPessoaFisicaParteAdvogado();
		} 
		if (TipoPessoaEnum.J.equals(inTipoPessoa)) {
			pesquisarPessoaJuridicaParte();
		}
	}
	
	/**
	 * Se a regra escolhida for parte e a radio for pessoa juridica pesquisa de acordo com o CNPJ informado.
	 */
	private void pesquisarPessoaJuridicaParte() {
		if (pessoaParte != null) {
			PessoaJuridicaManager pessoaJuridicaManager = ComponentUtil.getPessoaJuridicaManager();
			String numeroCNPJ = ((PessoaJuridica)pessoaParte).getNumeroCNPJ();
			if (StringUtils.isNotEmpty(numeroCNPJ)) {
				String numeroCNPJSemMascara = InscricaoMFUtil.retiraMascara(numeroCNPJ);
				if (!InscricaoMFUtil.validarCpfCnpj(numeroCNPJSemMascara)) {
					FacesMessages.instance().add(Severity.ERROR, FacesUtil.getMessage("impedimentoSuspeicao.cnpjInvalido"));
					FacesUtil.refreshFacesMessages();
				} else {
					PessoaJuridica pessoaJuridica = pessoaJuridicaManager.findByCNPJ(numeroCNPJSemMascara);
					if (pessoaJuridica != null) {
						pessoaParte = pessoaJuridica;
						pessoaAdvogado = new PessoaAdvogado();
					} else {
						pessoaParte.setNome(null);
						pessoaAdvogado = new PessoaAdvogado();
						FacesMessages.instance().add(Severity.ERROR, FacesUtil.getMessage("impedimentoSuspeicao.buscarPessoaFisicaJuridica"));
						FacesUtil.refreshFacesMessages();
					}
				}
			} 
		}
	}
	
	/**
	 * Se a regra escolhida for parte e a radio for pessoa fisica pesquisa de acordo com o CPF informado.
	 */
	private void pesquisarPessoaFisicaParteAdvogado() {
		if (pessoaParte != null) {
			PessoaFisicaManager pessoaFisicaManager = ComponentUtil.getPessoaFisicaManager();
			String numeroCPF = ((PessoaFisica)pessoaParte).getNumeroCPF();
			if (StringUtils.isNotEmpty(numeroCPF)) {
				String numeroCPFSemMascara = InscricaoMFUtil.retiraMascara(numeroCPF);
				if (!InscricaoMFUtil.validarCpfCnpj(numeroCPFSemMascara)) {
					FacesMessages.instance().add(Severity.ERROR, FacesUtil.getMessage("impedimentoSuspeicao.cpfInvalido"));
					FacesUtil.refreshFacesMessages();
				} else {
					PessoaFisica pessoaFisica = pessoaFisicaManager.findByCPF(numeroCPFSemMascara);
					if (pessoaFisica != null) {
						pessoaParte = pessoaFisica;
						pessoaAdvogado = new PessoaAdvogado();
						if(Pessoa.instanceOf(pessoaFisica, PessoaAdvogado.class)) {
							try {
								pessoaAdvogado = ComponentUtil.getComponent(PessoaAdvogadoManager.class).findById(pessoaFisica.getIdPessoa());
							} catch (PJeBusinessException e) {
								pessoaAdvogado = pessoaFisica.getPessoaAdvogado();
								e.printStackTrace();
								
							}
						}
					} else {
						pessoaParte.setNome(null);
						pessoaAdvogado = new PessoaAdvogado();
						FacesMessages.instance().add(Severity.ERROR, FacesUtil.getMessage("impedimentoSuspeicao.buscarPessoaFisicaJuridica"));
						FacesUtil.refreshFacesMessages();
					}
				}
			} else {
				pesquisarParteAdvogadoOAB();
			}
		}
	}
	
	/**
	 * Pesquisa o advogado estado OAB, numero OAB e letraOAB.
	 * 
	 * @throws PJeBusinessException
	 */
	private void pesquisarParteAdvogadoOAB() {
		if (pessoaAdvogado != null) {	
			PessoaAdvogadoManager pessoaAdvogadoManager = ComponentUtil.getComponent(PessoaAdvogadoManager.class);
			try { 
				if (pessoaAdvogado.getUfOAB() != null) {
					PessoaAdvogado pessoaAdvogadoRecuperado = pessoaAdvogadoManager.recuperarAdvogado(pessoaAdvogado.getUfOAB(), pessoaAdvogado.getNumeroOAB(), pessoaAdvogado.getLetraOAB());
					if (pessoaAdvogadoRecuperado != null) {
						pessoaParte = pessoaAdvogadoRecuperado.getPessoa();
						pessoaAdvogado = pessoaAdvogadoRecuperado;
					} else {
						FacesMessages.instance().add(Severity.ERROR, "Advogado não encontrado.");
			 			FacesUtil.refreshFacesMessages();
					}
				}
			} catch (PJeBusinessException be) {
				FacesMessages.instance().add(Severity.ERROR, "Houve um erro ao pesquisar o advogado. {0}", be.getLocalizedMessage());
	 			FacesUtil.refreshFacesMessages();
			}
			if (pessoaParte != null) {
				impedimentoSuspeicao.setPessoaParteAdvogado(pessoaParte);
			}
		}
	}
	
	public void preencherParteAdvogado() {
		if (impedimentoSuspeicao != null && impedimentoSuspeicao.getPessoaParteAdvogado() != null) {
			Pessoa pessoa = impedimentoSuspeicao.getPessoaParteAdvogado();
			if (TipoPessoaEnum.F.equals(pessoa.getInTipoPessoa())) {
				recuperarDadosPessoaFisica(pessoa);
			}
			if (TipoPessoaEnum.J.equals(pessoa.getInTipoPessoa())) {
				recuperarDadosPessoaJuridica(pessoa);
			}
		} 
	}
	
	private void recuperarDadosPessoaFisica(Pessoa pessoa) {
		PessoaFisicaManager pessoaFisicaManager = ComponentUtil.getPessoaFisicaManager();
		PessoaFisica pessoaFisica = pessoaFisicaManager.encontraPessoaFisicaPorPessoa(pessoa);  
		if (pessoaFisica.getPessoaAdvogado() != null) {
			pessoaAdvogado = pessoaFisica.getPessoaAdvogado(); 
		} 
		pessoaParte = pessoaFisica;
		inTipoPessoa = TipoPessoaEnum.F;
	}
	
	private void recuperarDadosPessoaJuridica(Pessoa pessoa) {
		PessoaJuridicaManager pessoaJuridicaManager = ComponentUtil.getPessoaJuridicaManager();
		PessoaJuridica pessoaJuridica = null;
		try {
			pessoaJuridica = pessoaJuridicaManager.findById(pessoa.getIdPessoa());  
		} catch (PJeBusinessException be) {
			FacesMessages.instance().add(Severity.ERROR, FacesUtil.getMessage("impedimentoSuspeicao.pesquisarPessoaJuridica") + "{0}", be.getLocalizedMessage());
			FacesUtil.refreshFacesMessages();
			logger.error(FacesUtil.getMessage("impedimentoSuspeicao.pesquisarPessoaJuridica") + be.getMessage());
		}
		pessoaParte = pessoaJuridica;
		inTipoPessoa = TipoPessoaEnum.J;
	}
	
	/**
	 * Metodo responsavel pela validacao de obrigatoriedade e chamada do servico de negocio para salvar/alterar o impedimentoSuspeicao.
	 */
	public void salvar() throws PJeException {
		ImpedimentoSuspeicaoService impedimentoSuspeicaoService = ComponentUtil.getComponent(ImpedimentoSuspeicaoService.class);
		boolean isValido = validarObrigatoriedade();
		if (isValido) {
			try {
				inicializaMunicipio();
				preencherPessoaParteAdvogado();
				impedimentoSuspeicaoService.salvar(getImpedimentoSuspeicao());
			} catch (PJeBusinessException e) {
				lancarMsg(Severity.INFO, e.getCode());
		        logger.info(e.getMessage());
		        return;
			} catch (PJeDAOException de) {
				lancarMsg(Severity.ERROR, de.getCode());
		        logger.error(de.getMessage());
		        return;
			} catch (Exception e) {
				lancarMsg(Severity.ERROR, e.getMessage());
		        logger.error("Erro ao inserir impedimento/suspeição");
		        return;
			}
			lancarMsg(Severity.INFO, "Registro inserido com sucesso");
			inicializarImpedimentoSuspeicao();
			inicializarPessoaParteAdvogado();
		}
	}
	
	/**
	 * Preenche a entidade pessoaParteadvogado.
	 */
	private void preencherPessoaParteAdvogado() {
		if (pessoaParte != null				
					&& pessoaParte.getIdPessoa() != null) {
			impedimentoSuspeicao.setPessoaParteAdvogado(pessoaParte);
		}
	}
	
	/**
	 * Inicializa a entidade Municipio.
	 */
	private void inicializaMunicipio() {
		if (impedimentoSuspeicao.getMunicipio() != null && impedimentoSuspeicao.getMunicipio().getIdMunicipio() == 0) {
			impedimentoSuspeicao.setMunicipio(null);
		}
	}
	
	/**
	 * Valida a obrigatoriedade dos campos.
	 * @return boolean Booleano com o resultado da validacao.
	 */
	private boolean validarObrigatoriedade() {
		RegraImpedimentoSuspeicaoEnum regraImpedimentoSuspeicao = impedimentoSuspeicao.getRegraImpedimentoSuspeicaoEnum();
		// Validacao obrigatoriedade regra
		if (regraImpedimentoSuspeicao == null) {
			lancarMsgObrigatoriedadeAsResourceBundle("impedimentoSuspeicao.obrigatoriedade.regra");
			return Boolean.FALSE;
		}
		// Validacao obrigatoriedade motivo
		if (impedimentoSuspeicao.getDescricaoMotivo() == null || "".equals(impedimentoSuspeicao.getDescricaoMotivo().trim())) {
			lancarMsgObrigatoriedadeAsResourceBundle("impedimentoSuspeicao.obrigatoriedade.motivo");
			return Boolean.FALSE;
		}
		if (regraImpedimentoSuspeicao.equals(RegraImpedimentoSuspeicaoEnum.P)) {
			return validarObrigatoriedadePorParte();
		} 
		if (regraImpedimentoSuspeicao.equals(RegraImpedimentoSuspeicaoEnum.U)) {
			return validarObrigatoriedadePorEstado();
		} 
		if (regraImpedimentoSuspeicao.equals(RegraImpedimentoSuspeicaoEnum.E)) {
			return validarObrigatoriedadePorAnoEleicao();
		} 
		if (regraImpedimentoSuspeicao.equals(RegraImpedimentoSuspeicaoEnum.A)) {
			return validarObrigatoriedadePorAdvogado();
		}
		return Boolean.TRUE;
	}
	
	/**
	 * Valida a obrigatoriedade dos campos da RegraImpedimentoSuspeicao por advogado.
	 * @return boolean Booleano com o resultado da validacao. 
	 */
	private boolean validarObrigatoriedadePorAdvogado() {
		boolean validacaoParte = Boolean.TRUE;
		if (impedimentoSuspeicao != null && !validarPreenchimentoNumeroCPFCNPJ()) {
			validacaoParte = Boolean.FALSE;
		}
		if (!validacaoParte && pessoaAdvogado != null
				&& (StringUtils.isEmpty(pessoaAdvogado.getNumeroOAB())
						&& (pessoaAdvogado.getUfOAB() == null))) {
			lancarMsgObrigatoriedadeAsResourceBundle("impedimentoSuspeicao.obrigatoriedade.parteOuNumeroOAB");
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}
	
	/**
	 * Verifica o preenchimento do número CPF.
	 * @return
	 */
	private boolean validarPreenchimentoNumeroCPFCNPJ() {
		if (pessoaParte != null && TipoPessoaEnum.F.equals(inTipoPessoa)) {
			return StringUtils.isNotEmpty(((PessoaFisica)pessoaParte).getNumeroCPF());
		}
		if (pessoaParte != null && TipoPessoaEnum.J.equals(inTipoPessoa)) {
			return StringUtils.isNotEmpty(((PessoaJuridica)pessoaParte).getNumeroCNPJ());
		}
		return true;
	}
	
	/**
	 * Valida a obrigatoriedade dos campos da RegraImpedimentoSuspeicao por ano eleicao.
	 * @return boolean Booleano com o resultado da validacao. 
	 */
	private boolean validarObrigatoriedadePorAnoEleicao() {
		boolean retorno = Boolean.TRUE;
		if (impedimentoSuspeicao != null && impedimentoSuspeicao.getEleicao() == null) {
			lancarMsgObrigatoriedadeAsResourceBundle("impedimentoSuspeicao.obrigatoriedade.anoEleicao.eleicao");
			retorno = Boolean.FALSE;
		}
		return retorno;
	}
	
	/**
	 * Valida a obrigatoriedade dos campos da RegraImpedimentoSuspeicao por estado.
	 * @return boolean Booleano com o resultado da validacao. 
	 */
	private boolean validarObrigatoriedadePorEstado() {
		boolean retornoValidacao = Boolean.TRUE;
		if (impedimentoSuspeicao != null && impedimentoSuspeicao.getEstado() == null) {
			lancarMsgObrigatoriedadeAsResourceBundle("impedimentoSuspeicao.obrigatoriedade.estado.estado");
			retornoValidacao = Boolean.FALSE;
		}
		return retornoValidacao;
	}
	
	/**
	 * Valida a obrigatoriedade dos campos da RegraImpedimentoSuspeicao por parte.
	 * @return boolean Booleano com o resultado da validacao. 
	 */
	private boolean validarObrigatoriedadePorParte() {
		if (impedimentoSuspeicao != null) {
			if (!validarPreenchimentoNumeroCPFCNPJ()) {
				lancarMsgObrigatoriedadeAsResourceBundle("impedimentoSuspeicao.obrigatoriedade.parte.numeroCpfParteAdvogado");
				return Boolean.FALSE;
			}
			if (!isPessoaAutoridade() && (pessoaParte == null || StringUtils.isEmpty(pessoaParte.getNome()))) {
				lancarMsgObrigatoriedadeAsResourceBundle("impedimentoSuspeicao.obrigatoriedade.nome");
				return Boolean.FALSE;
			}
			if (!impedimentoSuspeicao.getPoloAtivo() && !impedimentoSuspeicao.getPoloPassivo() && !impedimentoSuspeicao.getPoloIndefinido()) {
				lancarMsgObrigatoriedadeAsResourceBundle("impedimentoSuspeicao.obrigatoriedade.polo");
				return Boolean.FALSE;
			}
			if (!validarPreenchimentoPessoaAutoridade()) {
				lancarMsgObrigatoriedadeAsResourceBundle("impedimentoSuspeicao.obrigatoriedade.parte.pessoaAutoridade");
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}
	
	private boolean isPessoaAutoridade() {
		return impedimentoSuspeicao.getPessoaParteAdvogado() != null && TipoPessoaEnum.A.equals(inTipoPessoa);
	}

	private boolean validarPreenchimentoPessoaAutoridade() {
		boolean retorno = Boolean.TRUE;
		if (isPessoaAutoridade()) {
			PessoaAutoridadeManager pessoaAutoridadeManager = ComponentUtil.getPessoaAutoridadeManager();
			PessoaAutoridade pessoaAutoridade = pessoaAutoridadeManager.encontraPessoaAutoridadePorPessoa(impedimentoSuspeicao.getPessoaParteAdvogado());
			retorno = StringUtils.isNotEmpty(pessoaAutoridade.getNome());
		}
		return retorno;
	}
	
	/**
	 * Ao selecionar a combo de estado e chamado esse metodo para preencher o objeto estado para ser utilizado no
	 * autocomplete de municipios.
	 * 
	 * @param event ValueChangeEvent evento lancado.
	 */
	public void preencherEstado(ValueChangeEvent event) {
		if (event != null && event.getNewValue() instanceof Estado) {
			Estado estadoSelecionado = (Estado)event.getNewValue();
			impedimentoSuspeicao.setEstado(estadoSelecionado);
			inicializarMunicipio();
			limparDescricaoMotivo();
		} else {
			impedimentoSuspeicao.setEstado(null);
		}
	}
	
	/**
	 * Metodo utilizado no autocomplete de municipios.
	 * 
	 * @return List<Municipio> lista com os municipios.
	 */
	public List<Municipio> filtrarMunicipios(Object nomeMunicipio) {
		listaMunicipios = new ArrayList<>();
		if (getImpedimentoSuspeicao().getEstado() != null && StringUtils.isNotEmpty((String) nomeMunicipio)) {
			listaMunicipios = ComponentUtil.getComponent(MunicipioManager.class).filtrarMunicipios((String) nomeMunicipio, getImpedimentoSuspeicao().getEstado().getIdEstado());
		}
		return listaMunicipios;
	}
	
	/**
	 * Metodo utilizado no autocomplete de ente ou autoridade.
	 * 
	 * @return List<PessoaAutoridade> lista com os ente ou autoridade.
	 */
	public List<PessoaAutoridade> filtrarPessoaAutoridade(Object nomeEnteAutoridade) {
		listaEnteAutoridade = new ArrayList<>();
		String nome = (String)nomeEnteAutoridade;
		if (StringUtils.isNotEmpty(nome)) {
			listaEnteAutoridade = ComponentUtil.getPessoaAutoridadeManager().filtrarPessoaAutoridade(nome);
		}
		return listaEnteAutoridade;
	}
	
	/**
	 * Lança a mensagem e realiza o refresh.
	 * 
	 * @param severidade Severity severidade.
	 * @param msg String mensagem.
	 */
	private void lancarMsg(Severity severidade, String msg) {
		FacesMessages.instance().add(severidade, msg);
		FacesUtil.refreshFacesMessages();
	}
	
	/**
	 * Lanca a mensagem e realiza o refresh.
	 * 
	 * @param severidade Severity severidade.
	 * @param msg String mensagem.
	 */
	private void lancarMsgObrigatoriedadeAsResourceBundle(String keyMsg) {
		FacesMessages.instance().addFromResourceBundle(keyMsg, FacesUtil.getMessage("impedimentoSuspeicao.campoObrigatorio"));
		FacesUtil.refreshFacesMessages();
	}
	
	/**
	 * Preenche os dados para a tela de cadastro.
	 */
	public void preencherDadosCadastro() {
		canEdit = true;
		inicializarImpedimentoSuspeicao();
		limparCamposParteAdvogado();
		pessoaParte = new PessoaFisica();
		inTipoPessoa = TipoPessoaEnum.F;
	}
	
	/**
	 * Metodo responsavel por limpar os campos quando na combo de parte selecionado o tipoParte Advogado.
	 */
	public void limparCamposParteAdvogado() {
		pessoaParte = instanciarPessoaParte();
		pessoaAdvogado = new PessoaAdvogado();
		impedimentoSuspeicao.setPessoaParteAdvogado(null);
	}
	
	/**
	 * Inicializa a variavel municipio ao ser alterado o estado na combo de estado.
	 */
	public void limparMunicipio() {
		inicializarMunicipio();
	}
	
	@Override
	protected DataRetriever<ImpedimentoSuspeicao> getRetriever() {
		final ImpedimentoSuspeicaoService service = ComponentUtil.getComponent(ImpedimentoSuspeicaoService.class);
		return new DataRetriever<ImpedimentoSuspeicao>() {
			@Override
			public ImpedimentoSuspeicao findById(Object id) throws Exception {
				return service.findById(id);
			}
			@Override
			public List<ImpedimentoSuspeicao> list(Search search) {
				List<ImpedimentoSuspeicao> listaRetorno = new ArrayList<>();
				try {
					listaRetorno = service.pesquisar(regra, impedimentoSuspeicao.getPessoaMagistrado(), search);
				} catch (PJeBusinessException be) {
					lancarMsg(Severity.INFO, be.getCode());
			        logger.info(be.getMessage());
				}
				return listaRetorno;
			}
			@Override
			public long count(Search search) {
				long count = 0L;
				try {
					count = (service.pesquisar(regra, impedimentoSuspeicao.getPessoaMagistrado(), search)).size();
				} catch (PJeBusinessException be) {
					lancarMsg(Severity.INFO, be.getCode());
			        logger.info(be.getMessage());
				}
				return count;
			}
			@Override
			public Long getId(ImpedimentoSuspeicao impedimentoSuspeicao){
				return impedimentoSuspeicao.getId();
			}
		};
	}
	
	@Override
	protected BaseManager<ImpedimentoSuspeicao> getManager() {
		return null;
	}
	
	@Override
	public EntityDataModel<ImpedimentoSuspeicao> getModel() {
		return null;
	}
	
	/**
	 * @return the listaRegras
	 */
	public List<RegraImpedimentoSuspeicaoEnum> getListaRegras() {
		return listaRegras;
	}
	
	/**
	 * @param listaRegras the listaRegras to set
	 */
	public void setListaRegras(List<RegraImpedimentoSuspeicaoEnum> listaRegras) {
		this.listaRegras = listaRegras;
	}
	
	/**
	 * @return the regra
	 */
	public RegraImpedimentoSuspeicaoEnum getRegra() {
		return regra;
	}
	
	/**
	 * @param regra the regra to set
	 */
	public void setRegra(RegraImpedimentoSuspeicaoEnum regra) {
		this.regra = regra;
	}
	
	/**
	 * @return the canEdit
	 */
	public Boolean getCanEdit() {
		return canEdit;
	}
	
	/**
	 * @param canEdit the canEdit to set
	 */
	public void setCanEdit(Boolean canEdit) {
		this.canEdit = canEdit;
	}
	
	/**
	 * @return the impedimentoSuspeicao
	 */
	public ImpedimentoSuspeicao getImpedimentoSuspeicao() {
		return impedimentoSuspeicao;
	}
	
	/**
	 * @return the estado
	 */
	public Estado getEstado() {
		return estado;
	}
	
	/**
	 * @param estado the estado to set
	 */
	public void setEstado(Estado estado) {
		this.estado = estado;
	}
	
	/**
	 * @param impedimentoSuspeicao the impedimentoSuspeicao to set
	 */
	public void setImpedimentoSuspeicao(ImpedimentoSuspeicao impedimentoSuspeicao) {
		this.impedimentoSuspeicao = impedimentoSuspeicao;
	}
	
	/**
	 * @return the listaEstados
	 */
	public List<Estado> getListaEstados() {
		return listaEstados;
	}
	
	/**
	 * @return the listaMunicipios
	 */
	public List<Municipio> getListaMunicipios() {
		return listaMunicipios;
	}
	
	/**
	 * @param listaMunicipios the listaMunicipios to set
	 */
	public void setListaMunicipios(List<Municipio> listaMunicipios) {
		this.listaMunicipios = listaMunicipios;
	}
	
	/**
	 * @param listaEstados the listaEstados to set
	 */
	public void setListaEstados(List<Estado> listaEstados) {
		this.listaEstados = listaEstados;
	}
	
	/**
	 * @return the listaEleicoesAtivas
	 */
	public List<Eleicao> getListaEleicoesAtivas() {
		return listaEleicoesAtivas;
	}
	
	/**
	 * @param listaEleicoesAtivas the listaEleicoesAtivas to set
	 */
	public void setListaEleicoesAtivas(List<Eleicao> listaEleicoesAtivas) {
		this.listaEleicoesAtivas = listaEleicoesAtivas;
	}
	
	/**
	 * @return the listaResultadoPesquisa
	 */
	public EntityDataModel<ImpedimentoSuspeicao> getListaResultadoPesquisa() {
		return listaResultadoPesquisa;
	}
	
	/**
	 * @param listaResultadoPesquisa the listaResultadoPesquisa to set
	 */
	public void setListaResultadoPesquisa(EntityDataModel<ImpedimentoSuspeicao> listaResultadoPesquisa) {
		this.listaResultadoPesquisa = listaResultadoPesquisa;
	}
	
	/**
	 * @return the pessoaParte
	 */
	public Pessoa getPessoaParte() {
		return pessoaParte;
	}
	
	/**
	 * @param pessoaParte the pessoaParte to set
	 */
	public void setPessoaParte(Pessoa pessoaParte) {
		this.pessoaParte = pessoaParte;
	}
	
	/**
	 * @return the pessoaAdvogado
	 */
	public PessoaAdvogado getPessoaAdvogado() {
		return pessoaAdvogado;
	}
	
	/**
	 * @param pessoaAdvogado the pessoaAdvogado to set
	 */
	public void setPessoaAdvogado(PessoaAdvogado pessoaAdvogado) {
		this.pessoaAdvogado = pessoaAdvogado;
	}
	
	/**
	 * @return the inTipoPessoa
	 */
	public TipoPessoaEnum getInTipoPessoa() {
		return inTipoPessoa;
	}
	
	/**
	 * @param inTipoPessoa the inTipoPessoa to set
	 */
	public void setInTipoPessoa(TipoPessoaEnum inTipoPessoa) {
		this.inTipoPessoa = inTipoPessoa;
	}
	
	/**
	 * @return the listaTipoPessoa
	 */
	public List<TipoPessoaEnum> getListaTipoPessoa() {
		if (CollectionUtils.isEmpty(listaTipoPessoa)) {
			listaTipoPessoa = Arrays.asList(TipoPessoaEnum.values());
		}
		return listaTipoPessoa;
	}
	
	/**
	 * @return the listaEnteAutoridade
	 */
	public List<PessoaAutoridade> getListaEnteAutoridade() {
		return listaEnteAutoridade;
	}
	
	/**
	 * @param listaEnteAutoridade the listaEnteAutoridade to set
	 */
	public void setListaEnteAutoridade(List<PessoaAutoridade> listaEnteAutoridade) {
		this.listaEnteAutoridade = listaEnteAutoridade;
	}
	
	public void marcaAmbos() {
		impedimentoSuspeicao.setPoloAtivo(false);
		impedimentoSuspeicao.setPoloPassivo(false);
	}

	public void marcaPolo() {
		impedimentoSuspeicao.setPoloIndefinido(false);
	}

}