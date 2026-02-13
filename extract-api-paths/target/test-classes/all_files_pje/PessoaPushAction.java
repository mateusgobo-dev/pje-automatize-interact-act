package br.jus.cnj.pje.view;
 
 import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.international.Messages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.component.suggest.PessoaPushCepSuggestBean;
import br.com.infox.pje.action.EnviarRedefinirSenhaPushAction;
import br.com.infox.pje.manager.PessoaPushManager;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.CadastroTempPushManager;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.pje.nucleo.entidades.CadastroTempPush;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.PessoaPush;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.SexoEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;
 
 /**
  * Componente de controle da tela de pesquisa de {@link PessoaPush}.
  */
 @Name(PessoaPushAction.NAME)
 @Scope(ScopeType.PAGE)
 public class PessoaPushAction extends BaseAction<PessoaPush> {
 
 	public static final String NAME = "pessoaPushAction";
 	public static final String CPF = "CPF";
 	public static final String CPJ = "CPJ";
 	
 	private static final long serialVersionUID = 1L;
 
 	private String tab;
 	private EntityDataModel<PessoaPush> model;
 
 	// Componentes da tela de pesquisa
 	private String nome;
 	private String numeroCPF;
 	private String numeroCNPJ;
 	private Boolean confirmado = Boolean.TRUE;
 	private Boolean ativo = Boolean.TRUE;
 	private boolean documentoCPF = false;
 
 	@RequestParameter(value = "pushGridCount")
 	private Integer pushGridCount;
 
 	@In
 	private PessoaPushManager pessoaPushManager;
 
 	@In
 	private CadastroTempPushManager cadastroTempPushManager;
 	
 	@In
 	private UsuarioService usuarioService;
 
 	@Create
 	public void init() {
 		pesquisar();
 		setTab("search");
 	}
 
 	/**
 	 * Método responsável por realizar a pesquisa dos usuários Push de acordo os
 	 * critérios escritos pelo usuário.
 	 */
 	public void pesquisar() {
 		try {
 			List<Criteria> criterios = new ArrayList<Criteria>(0);
 			criterios.addAll(getCriteriosTelaPesquisa());
 
 			model = new EntityDataModel<PessoaPush>(PessoaPush.class, super.facesContext, getRetriever());
 			model.setCriterias(criterios);
 			model.addOrder("nome", Order.ASC);
 		} catch (Exception e) {
 			facesMessages.addFromResourceBundle(Severity.ERROR, "pessoaPush.pesquisa.erro" + e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Método responsável por adicionar os critrios da tela de pesquisa.
 	 * 
 	 * @return <code>List</code> de <code>Criteria</code> com os filtros da
 	 *         pesquisa.
 	 */
 	private List<Criteria> getCriteriosTelaPesquisa() {
 		List<Criteria> criterios = new ArrayList<Criteria>(0);
 
 		if (StringUtil.isNotEmpty(this.nome)) {
 			criterios.add(Criteria.contains("nome", this.nome));
 		}
 
 		if (StringUtil.isNotEmpty(this.numeroCPF) && !StringUtil.CPF_EMPTYMASK.equalsIgnoreCase(this.numeroCPF)) {
 			criterios.add(Criteria.equals("tipoDocumentoIdentificacao.codTipo", CPF));
 			criterios.add(Criteria.startsWith("nrDocumento", this.numeroCPF));
 		}
 
 		if (StringUtil.isNotEmpty(this.numeroCNPJ) && !StringUtil.CNPJ_EMPTYMASK.equalsIgnoreCase(this.numeroCNPJ)) {
 			criterios.add(Criteria.equals("tipoDocumentoIdentificacao.codTipo", CPJ));
 			criterios.add(Criteria.startsWith("nrDocumento", this.numeroCNPJ));
 		}
 
 		return criterios;
 	}
 
 	/**
 	 * Método responsável por definir o objeto que ser utilizado para edição do
 	 * cadastro.
 	 * 
 	 * @param pessoaPush
 	 *            a pessoa que se deseja editar
 	 */
 	public void editar(PessoaPush pessoaPush) {
 		super.setInstance(pessoaPush);
 		setTab("form");
 	}
 
 	/**
 	 * Método responsável por atualizar os dados do endereço.
 	 */
 	public void atualizarDadosEndereco() {
 		PessoaPushCepSuggestBean cepSuggestBean = ComponentUtil.getComponent(PessoaPushCepSuggestBean.class);
 		Cep cep = cepSuggestBean.getInstance();
 		instance.setCep(cep.getNumeroCep());
 		instance.setMunicipio(cep.getMunicipio());
 		instance.setEndereco(cep.getNomeLogradouro());
 		instance.setBairro(cep.getNomeBairro());
 		instance.setComplemento(cep.getComplemento());
 		instance.setNumeroEndereco(cep.getNumeroEndereco());
 	}
 	
 	/**
	 * metodo responsavel por verificar se o cep já está inserido
	 * @return
	 */
	public boolean isCepNuloOuVazio() {
		return StringUtils.isBlank(this.instance.getCep());
	}
 
 	/**
 	 * Método responsável por gravar as alterações feitas no formulário.
 	 */
 	public void gravar() {
 		facesMessages.clear();
 		try {
 			getManager().mergeAndFlush(getInstance());
 			facesMessages.addFromResourceBundle(Severity.INFO, "PessoaPush_updated");
 		} catch (Exception e) {
 			facesMessages.addFromResourceBundle(Severity.ERROR, e.getLocalizedMessage());
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Método responsável por criar um novo usuário Push.
 	 */
 	public void novo() {
 		facesMessages.clear();
 		if (!isCadastroExistente()) {
	 		try {
	 			CadastroTempPush cadastroTempPush = criarCadastroTempPush();
	 			PessoaPush novaPessoaPush = getInstance();
	 			novaPessoaPush.setSenha(InscricaoMFUtil.retiraMascara(novaPessoaPush.getNrDocumento()));
	 			novaPessoaPush.setTipoDocumentoIdentificacao(tipoDocumentoIdentificacao());
	 			novaPessoaPush.setCadastroTempPush(cadastroTempPush);
	 			pessoaPushManager.persistAndFlush(novaPessoaPush);
	 			facesMessages.addFromResourceBundle(Severity.INFO, "PessoaPush_created");
	 		} catch (Exception e) {
	 			facesMessages.clear();
	 			if (e instanceof PJeBusinessException) {
	 				facesMessages.addFromResourceBundle(Severity.ERROR, e.getLocalizedMessage());
	 			} else if (e instanceof PJeDAOException) {
	 				facesMessages.addFromResourceBundle(Severity.ERROR, "pessoaPush.novoCadastro.erro", identificarErro(e));
	 			} else {
	 				facesMessages.addFromResourceBundle(Severity.ERROR, "pessoaPush.atualizacaoCadastral.erro", e.getLocalizedMessage());
	 			}
	 			e.printStackTrace();
	 		}
 		} else {
 			facesMessages.addFromResourceBundle(Severity.ERROR, "pessoaPush.novoCadastro.existente.erro");
 		}
 	}
 	
 	/**
	 * Método responsável por identificar o erro de <i>constraint</i> no novo
	 * cadastro do usuário.
	 * 
	 * @param e
	 *            a exceção que se deseja identificar o erro de
	 *            <i>constraint</i>
	 * @return <code>String</code>, caso a mensagem de erro contenha "email" em
	 *         sua mensagem, o erro é dado por
	 *         "<i>E-mail já cadastrado na base.</i>", caso seja "documento" o
	 *         erro informará: "<i>Documento já cadastrado na base.</i>"
	 */
 	private String identificarErro(Exception e) {
 		String exception = StringUtils.substringAfter(e.toString(), "Detail:");
 		if (exception.contains("email")) {
 			return Messages.instance().get("pessoaPush.novoCadastro.emailJaCadastrado.erro");
 		} else if (exception.contains("documento")) {
 			return Messages.instance().get("pessoaPush.novoCadastro.documentoJaCadastrado.erro");
 		}
 		return "";
 	}
 
 	/**
 	 * Método responsável por criar um cadastro temporário Push que armazenar o
 	 * hash do usuário para que seja possível a redefinição da senha
 	 * posteriormente.
 	 * 
 	 * @return <code>CadastroTempPush</code> o cadastro temporário do usuário
 	 * @throws PJeBusinessException
 	 */
 	private CadastroTempPush criarCadastroTempPush() throws PJeBusinessException {
 		CadastroTempPush cadastroTempPush = cadastroTempPushManager.criarNovoCadastro(instance.getEmail(), instance.getNrDocumento(), tipoDocumentoIdentificacao());
 		cadastroTempPush.setConfirmado(true);
 		cadastroTempPushManager.persistAndFlush(cadastroTempPush);
 		return cadastroTempPush;
 	}
 
 	/**
 	 * Método responsável por tornar o usuário Push, de acordo com seu
 	 * {@link PessoaPush#getNrDocumento()}, em {@link PessoaFisica} ou
 	 * {@link PessoaJuridica}.
 	 */
 	public void tornar() {
 		try {
 			pessoaPushManager.tornarPessoaFisicaJuridica(getInstance());
 			facesMessages.addFromResourceBundle(Severity.INFO, "pessoaPush.atualizacaoCadastral.sucesso");
 			init();
 		} catch (Exception e) {
 			facesMessages.clear();
 			if (e instanceof PJeBusinessException) {
 				facesMessages.addFromResourceBundle(Severity.ERROR, "{0}", e.getLocalizedMessage());
 			} else {
 				facesMessages.addFromResourceBundle(Severity.ERROR, "pessoaPush.atualizacaoCadastral.erro", e.getLocalizedMessage());
 			}
 			e.printStackTrace();
 		}
 	}
 	
 	/**
	 * Método responsável por remover o registro do usurio Push.
	 * 
	 * @param pessoaPush
	 *            o usuário que se deseja remover.
	 */
 	public void remover(PessoaPush pessoaPush) {
 		if (pessoaPush != null) {
 			facesMessages.clear();
 			try {
 				removerCadastros(pessoaPush);
 				pesquisar();
 				facesMessages.addFromResourceBundle(Severity.INFO, "PessoaPush_deleted");
 			} catch (PJeBusinessException e) {
 				facesMessages.addFromResourceBundle(Severity.ERROR, "pessoaPush.remover.erro", e.getLocalizedMessage());
 				e.printStackTrace();
 			}			
 		}
 	}
 	
 	/**
	 * Método responsável por remover os cadastros relacionados ao Push
	 * 
	 * @param pessoaPush
	 *            o usuário Push
	 * 
	 * @throws PJeBusinessException
	 */
 	private void removerCadastros(PessoaPush pessoaPush) throws PJeBusinessException {
 		pessoaPushManager.remove(pessoaPush);
 		pessoaPushManager.flush();
 	}
 	
	/**
	 * Método responsável por identificar se o novo cadastro já existe na base
	 * de dados
	 * 
	 * @return <code>Boolean</code>, <code>true</code> caso exista
	 */
 	public boolean isCadastroExistente() {
 		PessoaPush pessoaPush = pessoaPushManager.recuperarPessoaPushByLogin(InscricaoMFUtil.retiraMascara(instance.getNrDocumento()));
 		CadastroTempPush cadastroTempPush = cadastroTempPushManager.recuperarCadastroTempPushByLogin(instance.getNrDocumento());
 		Usuario usuario = usuarioService.findByLogin(instance.getNrDocumento());
 		
 		Boolean cadastroConfirmado = null;			
 		if (cadastroTempPush != null) {
 			cadastroConfirmado = cadastroTempPush.getConfirmado();
		}
 		
 		return (pessoaPush != null) || (usuario != null) 
 				|| (cadastroTempPush != null && cadastroConfirmado.equals(Boolean.TRUE)); 
 	}
 
 	/**
 	 * Método responsável por verificar se o tamanho documento do usuário Push 
 	 * o mesmo tamanho do CPF
 	 * 
 	 * @return <code>Boolean</code>, <code>true</code> se o documento do usuário
 	 *         tiver o mesmo tamanho de um CPF, 11 dígitos sem máscara.
 	 */
 	public boolean isTamanhoDocCPF() {
 		return (InscricaoMFUtil.retiraMascara(instance.getNrDocumento()).length() == InscricaoMFUtil.TAMANHO_CPF);
 	}
 
 	/**
	 * Método responsável pelo título do botão para tornar {@link PessoaFisica}
	 * ou {@link PessoaJuridica}
	 * 
	 * @return <code>String</code>, caso o método {@link #isTamanhoDocCPF()}
	 *         retorne <code>true</code> o título do botão ser <i>
	 *         "Tornar pessoa física"</i>, caso <code>false</code> ser <i>
	 *         "Tornar pessoa jurídica"</i>.
	 */
 	public String labelBotaoTornar() {
 		return isTamanhoDocCPF() ? Messages.instance().get("pessoaPush.btnTornarPF")
 				: Messages.instance().get("pessoaPush.btnTornarPJ");
 	}
 
 	/**
 	 * Método responsável por enviar um email com redefinição da senha do usuário Push.
 	 */
 	public void enviarEmail() {
 		EnviarRedefinirSenhaPushAction enviarRedefinirSenha = ComponentUtil.getComponent(EnviarRedefinirSenhaPushAction.NAME);
 		enviarRedefinirSenha.redefinir(instance.getNrDocumento(), instance.getEmail());
 	}
 	
 	/**
 	 * Método responsável por retornar o {@link TipoDocumentoIdentificacao}
 	 * baseado no tamanho do documento.
 	 * 
 	 * @return {@link TipoDocumentoIdentificacao} baseado no tamanho do
 	 *         documento.
 	 */
 	public TipoDocumentoIdentificacao tipoDocumentoIdentificacao() {
 		return (isTamanhoDocCPF() ? new TipoDocumentoIdentificacao(CPF) : new TipoDocumentoIdentificacao(CPJ));
 	}
 	
 	/**
 	 * Método responsável por limpar as pesquisas feita pelo usuário.
 	 */
 	public void limparPesquisa() {
 		setNome(null);
 		limparCpfCnpj();
 	}
 	
 	/**
 	 * Método responsável por limpar o campo de CPF/CNPJ na pesquisa.
 	 */
 	public void limparCpfCnpj() {
 		setNumeroCPF(null);
 		setNumeroCNPJ(null);
 	}
 	
 	/**
	 * Método responsável por recuperar o <i>suggest bean</i>.
	 * 
	 * @return {@link PessoaPushCepSuggestBean}
	 */
 	private PessoaPushCepSuggestBean getPessoaPushSuggestBean() {
 		return ComponentUtil.getComponent(PessoaPushCepSuggestBean.class);
 	}
 	
 	@Override
 	protected DataRetriever<PessoaPush> getRetriever() {
 		final PessoaPushManager manager = (PessoaPushManager) getManager();
 		final Integer tableCount = pushGridCount;
 		DataRetriever<PessoaPush> retriever = new DataRetriever<PessoaPush>() {
 			@Override
 			public PessoaPush findById(Object id) throws Exception {
 				try {
 					return manager.findById(id);
 				} catch (PJeBusinessException e) {
 					throw new Exception(e);
 				}
 			}
 
 			@Override
 			public List<PessoaPush> list(Search search) {
 				return manager.list(search);
 			}
 
 			@Override
 			public long count(Search search) {
 				if (tableCount != null && tableCount >= 0) {
 					return tableCount;
 				}
 
 				return manager.count(search);
 			}
 
 			@Override
 			public Object getId(PessoaPush obj) {
 				return manager.getId(obj);
 			}
 		};
 		return retriever;
 	}
 	
 	@Override
 	public boolean isManaged() {
 		return super.isManaged() && getInstance().getIdPessoaPush() != null;
 	}
 
 	@Override
 	public void onClickSearchTab() {
 		super.onClickSearchTab();
 		pesquisar();
 	}
 	
 	@Override
 	public void onClickFormTab() {
 		super.onClickFormTab();
 		getPessoaPushSuggestBean().setDefaultValue(null);
 		newInstance();
 	}
 
 	@Override
 	protected BaseManager<PessoaPush> getManager() {
 		return pessoaPushManager;
 	}
 
 	@Override
 	public EntityDataModel<PessoaPush> getModel() {
 		return this.model;
 	}
 
 	public String getTab() {
 		return tab;
 	}
 
 	public void setTab(String tab) {
 		this.tab = tab;
 	}
 
 	public String getNome() {
 		return nome;
 	}
 
 	public void setNome(String nome) {
 		this.nome = nome;
 	}
 
 	public String getNumeroCPF() {
 		return numeroCPF;
 	}
 
 	public void setNumeroCPF(String numeroCPF) {
 		this.numeroCPF = numeroCPF;
 	}
 
 	public String getNumeroCNPJ() {
 		return numeroCNPJ;
 	}
 
 	public void setNumeroCNPJ(String numeroCNPJ) {
 		this.numeroCNPJ = numeroCNPJ;
 	}
 
 	public Boolean getConfirmado() {
 		return confirmado;
 	}
 
 	public void setConfirmado(Boolean confirmado) {
 		this.confirmado = confirmado;
 	}
 
 	public Boolean getAtivo() {
 		return ativo;
 	}
 
 	public void setAtivo(Boolean ativo) {
 		this.ativo = ativo;
 	}
 
 	public boolean isDocumentoCPF() {
 		return documentoCPF;
 	}
 
 	public void setDocumentoCPF(boolean documentoCPF) {
 		this.documentoCPF = documentoCPF;
 	}
 
 	public SexoEnum[] getSexoValues() {
 		return SexoEnum.values();
 	}
 }