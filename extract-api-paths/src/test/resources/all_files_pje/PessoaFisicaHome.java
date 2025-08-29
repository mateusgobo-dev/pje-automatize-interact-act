package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.access.home.PapelHome;
import br.com.infox.cliente.component.suggest.PessoaFisicaMunicipioSuggestBean;
import br.com.infox.cliente.component.suggest.ProfissaoSuggestBean;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.EnderecoHome;
import br.com.infox.ibpm.home.UsuarioLocalizacaoHome;
import br.com.infox.pje.manager.PessoaFisicaManager;
import br.com.infox.trf.webservice.ConsultaClienteReceitaPFCJF;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.manager.LogAcessoManager;
import br.jus.cnj.pje.nucleo.manager.MunicipioManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioManager;
import br.jus.cnj.pje.nucleo.manager.cache.ProcessoParteCache;
import br.jus.cnj.pje.nucleo.service.PapelService;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.cnj.pje.webservice.client.keycloak.KeycloakServiceClient;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaAssistenteAdvogado;
import br.jus.pje.nucleo.entidades.PessoaAssistenteProcuradoria;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.PessoaPerito;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.PessoaServidor;
import br.jus.pje.nucleo.entidades.Profissao;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.SexoEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaFisica;

@Name("pessoaFisicaHome")
@BypassInterceptors
public class PessoaFisicaHome extends AbstractPessoaFisicaHome<PessoaFisica> {

	private static final long serialVersionUID = 1L;
	private PessoaFisica pessoaConciliador = new PessoaFisica();
	private Endereco endereco;
	private Estado estado;
	private Localizacao localizacaoFisica;
	private DadosReceitaPessoaFisica dadosReceitaPessoa = new DadosReceitaPessoaFisica();
	private List<DadosReceitaPessoaFisica> listDadosReceitaPessoa = new ArrayList<DadosReceitaPessoaFisica>(0);
	private Date dataInicio;
	private Date dataFim;
	private Boolean obfuscateProfissao;
	private String oldCpf;
	private String numeroCPF;
	private PessoaFisicaManager pessoaFisicaManager;
	private List<Papel> papeis;
	private String email1;
	private Boolean informarNomeSocial;

	private ProcessoParteCache processoParteCache;

	private ProcessoParteCache getProcessoParteCache() {
		if (processoParteCache == null) {
			processoParteCache = (ProcessoParteCache) Component.getInstance(ProcessoParteCache.COMPONENT_NAME);
		}

		return processoParteCache;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public Date getDataFim() {
		return dataFim;
	}

	private EnderecoHome getEnderecoHome() {
		return getComponent("enderecoHome");
	}
	
	private CaracteristicaFisicaHome getCaracteristicaFisicaHome(){
		return getComponent("caracteristicaFisicaHome");
	}

	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		if (this.estado != null) {
			if ((estado == null) || (!this.estado.getEstado().equals(estado.getEstado()))) {
				PessoaFisicaMunicipioSuggestBean pessoaFisicaMunicipioSuggestBean = getPessoaFisicaMunicipioSuggestBean();
				pessoaFisicaMunicipioSuggestBean.setDefaultValue(null);
				pessoaFisicaMunicipioSuggestBean.setInstance(null);
			}
		}
		this.estado = estado;
	}

	public String getNumeroCPF() {
		return numeroCPF;
	}

	public void setNumeroCPF(String numeroCPF) {
		if(StringUtil.CPF_EMPTYMASK.equals(numeroCPF)){
			numeroCPF = "";
		}
		this.numeroCPF = numeroCPF;
	}

	@Override
	public void newInstance() {
		oldCpf = null;
		Contexts.removeFromAllContexts("cepSuggest");
		Contexts.removeFromAllContexts("pessoaFisicaMunicipioSuggest");
		refreshGrid("pessoaFisicaGrid");
		pessoaConciliador = new PessoaFisica();

		getInstance().getEnderecoList().clear();

		super.newInstance();
		getEnderecoHome().newInstance();
		endereco = null;
	}
	
	public void newInstanceUsuarioLocalizacaoConciliador() {
		this.newInstanceUsuarioLocalizacao();
		UsuarioLocalizacaoHome localizacaoHome = getComponent("usuarioLocalizacaoHome");
		PapelService papelService = getComponent("papelService");
		localizacaoHome.setPapel(papelService.findByCodeName(Papeis.CONCILIADOR));
	}

	public void newInstanceUsuarioLocalizacao() {
		UsuarioLocalizacaoHome localizacaoHome = getComponent("usuarioLocalizacaoHome");
		OficialJusticaCentralMandadoHome oficialCentralHome = getComponent(OficialJusticaCentralMandadoHome.NAME);
		localizacaoHome.newInstance();
		localizacaoHome.getInstance().setUsuario(PessoaHome.instance().getInstance());
		oficialCentralHome.newInstance();
		oficialCentralHome.getInstance().setUsuarioLocalizacao(localizacaoHome.getInstance());
	}

	public static PessoaFisicaHome instance() {
		return ComponentUtil.getComponent("pessoaFisicaHome");
	}

	public String removeCertificado() {
		getInstance().setCertChain(null);
		getInstance().setAssinatura(null);
		return update();
	}

	public PessoaFisicaMunicipioSuggestBean getPessoaFisicaMunicipioSuggestBean() {
		PessoaFisicaMunicipioSuggestBean pessoaFisicaMunicipioSuggest = 
				(PessoaFisicaMunicipioSuggestBean) Component.getInstance(PessoaFisicaMunicipioSuggestBean.NAME);
		return pessoaFisicaMunicipioSuggest;
	}

	@Override
	public String update() {
		try {
			if(beforePersistOrUpdate()){
				pessoaFisicaManager = (PessoaFisicaManager)getComponent(PessoaFisicaManager.NAME);
				pessoaFisicaManager.persistAndFlush(getInstance());
				
				for (PessoaDocumentoIdentificacao documento : getInstance().getPessoaDocumentoIdentificacaoList()) {
					getEntityManager().persist(documento);
					EntityUtil.flush();
				}
				
				refreshGrid("pessoaDocumentoIdentificacaoCadastroGrid");
				PessoaHome.instance().setInstance(getInstance());
				PessoaHome.instance().atualizarNomeLocalizacao(getInstance());
				
				updatedMessage();
				
				if (isCadastroAlterado()) {
					Authenticator.deslogar(instance, "perfil.atualizar");
				}

				getProcessoParteCache().refreshProcessoParteByProcessoTrfEPessoaCache(
						ProcessoTrfHome.instance().getInstance().getIdProcessoTrf(), getInstance().getIdPessoa());

				return afterPersistOrUpdate("persisted");
			}
		} catch (PJeBusinessException e) {
			reportMessage(e);			
		}
		
		return null;
	}

	@Override
	public String persist() {
		if(beforePersistOrUpdate()){
			pessoaConciliador = getInstance();
			if (!Objects.isNull(endereco)) {
				pessoaConciliador.getEnderecoList().add(endereco);
			}
			try {
				pessoaFisicaManager = (PessoaFisicaManager)getComponent(PessoaFisicaManager.NAME);
				pessoaFisicaManager.persistAndFlush(pessoaConciliador);
				setInstance(pessoaConciliador);
				endereco = null;
				createdMessage();
				return afterPersistOrUpdate("persisted");
			} catch (PJeBusinessException e) {
				reportMessage(e);
			}
		}

		return null;
	}

	public String persistConciliador() {
		if(beforePersistOrUpdate()){
			getInstance().getEnderecoList().clear();

			pessoaConciliador = getInstance();
			
			pessoaFisicaManager = (PessoaFisicaManager)getComponent(PessoaFisicaManager.NAME);
			
			try {
				pessoaFisicaManager.persistAndFlush(pessoaConciliador);
			} catch (PJeBusinessException e) {
				reportMessage(e);
			}
			
			setInstance(pessoaConciliador);
			String persist = afterPersistOrUpdate("persisted");
			
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Registro inserido com sucesso");
			
			return persist;			
		}
		
		return null;
	}
	
	/**
	 * metodo responsavel por gerenciar a persistencia da localizacao do conciliador e 
	 * exclusao do usuario push da pessoa conciliador, caso exista.
	 */
	public void salvarLocalizacaoConciliador() {
		try {
			persistConciliadorLocalizacao();
			desabilitarUsuarioPush();
		} catch (Exception ex) {
			ex.printStackTrace();
			FacesMessages.instance().add(Severity.ERROR, ex.getMessage());
		}
	}

	private void desabilitarUsuarioPush() throws PJeBusinessException {
		PessoaService pessoaService = getComponent(PessoaService.NAME);
		pessoaService.desabilitarUsuarioPush(getInstance());
	}

	/**
	 * Método responsável por persistir a localizacao do Conciliador.
	 * 
	 * @throws Exception
	 */
	private void persistConciliadorLocalizacao() throws Exception {
		try {
			PapelService papelService = getComponent("papelService");
			Papel papelConciliador = papelService.findByCodeName(Papeis.CONCILIADOR);

			UsuarioLocalizacaoHome usuarioLocalizacaoHome = UsuarioLocalizacaoHome.instance();
			usuarioLocalizacaoHome.getInstance().setPapel(papelConciliador);
			usuarioLocalizacaoHome.getInstance().setUsuario(getInstance());
			if(usuarioLocalizacaoHome.getLocalizacaoFisica() != null){
				usuarioLocalizacaoHome.getInstance().setLocalizacaoFisica(usuarioLocalizacaoHome.getLocalizacaoFisica());
			} else {
				usuarioLocalizacaoHome.getInstance().setLocalizacaoFisica(this.localizacaoFisica);
			}
			usuarioLocalizacaoHome.persist();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Erro ao persistir a localização do conciliador");
		}
	}
	
	public void verificarTipoPessoa() {
		Context session = Contexts.getSessionContext();
		Pessoa pessoaLogada = ((Pessoa) session.get("usuarioLogado"));
		if (pessoaLogada instanceof PessoaFisica) {
			this.setId(pessoaLogada.getIdUsuario());
		}
	}

	@Override
	public void setId(Object id) {
		Contexts.removeFromAllContexts("pessoaFisicaForm");
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if((instance == null || instance.getNome() == null ) && id != null) {
			try {
				instance = ComponentUtil.getPessoaFisicaManager().findById(id);
			} catch (PJeBusinessException e) {
				FacesMessages.instance().add(Severity.ERROR, e.getMessage()); 
			}
		}
		PessoaHome.instance().setId(id);
		if(PessoaHome.instance().getInstance() != null && PessoaHome.instance().getInstance().getInTipoPessoa() == null){
			PessoaHome.instance().getInstance().setInTipoPessoa(TipoPessoaEnum.F);
		}

		if (changed) {
			if (getInstance().getMunicipioNascimento() != null) {
				estado = getInstance().getMunicipioNascimento().getEstado();
			} else {
				MunicipioManager municipioManager = getComponent(MunicipioManager.NAME);
				
				Municipio municipio = municipioManager.getMunicipioByIdPessoa(id);
				if (municipio != null) {
					this.estado = municipio.getEstado();
					getPessoaFisicaMunicipioSuggestBean().setDefaultValue(municipio.getMunicipio());
				}
			}
			getPessoaFisicaMunicipioSuggestBean().setInstance(getInstance().getMunicipioNascimento());
		}
		if (id == null) {
			getPessoaFisicaMunicipioSuggestBean().setInstance(null);
			estado = null;
		}

		if (getInstance() != null && oldCpf == null) {
			oldCpf = getInstance().getNumeroCPF();
		}
		
		setUsuarioAtivoInicial(instance.getAtivo());
	}

	/**
	 * Método chamado pelo botão cadastroPessoas. Encontra-se na página de
	 * inserção de partes de um processo, tanto na consulta de um processo
	 * quanto no cadastro.
	 */
	public void inserirAba() {
		beforePersistOrUpdate();
		persist();
		afterPersistOrUpdate("persisted");
	}

	public boolean checkLogin() {
		if(getInstance().getLogin() != null && !getInstance().getLogin().trim().isEmpty()){
			pessoaFisicaManager = (PessoaFisicaManager)getComponent(PessoaFisicaManager.NAME);
			Boolean disponivel = pessoaFisicaManager.checkLogin(getInstance().getLogin(), getInstance().getIdPessoa());
			if(!disponivel){
				FacesMessages.instance().addToControl("loginLogin", StatusMessage.Severity.ERROR, "Login já cadastrado!");
				getInstance().setLogin("");
				return true;
			}
		}
		return false;
	}

	public SexoEnum[] getSexoValues() {
		return SexoEnum.values();
	}

	public Endereco getEndereco() {
		return this.endereco;
	}

	@Observer("cepChangedEvent")
	public void setEndereco(Cep cep) {
		endereco = null;
		if (cep == null) {
			Contexts.removeFromAllContexts("cepSuggest");
		} else {
			endereco = new Endereco();
			endereco.setCep(cep);
			if(cep.getMunicipio() != null) {
				endereco.setNomeEstado(cep.getMunicipio().getEstado().getEstado());
				endereco.setNomeCidade(cep.getMunicipio().getMunicipio());
			}
			endereco.setNomeLogradouro(cep.getNomeLogradouro());
			endereco.setNomeBairro(cep.getNomeBairro());
			endereco.setNumeroEndereco(EnderecoHome.instance().getInstance().getNumeroEndereco());
			endereco.setNomeCidade(endereco.getNomeCidade());
			endereco.setUsuario(getInstance());
		}
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		if (instance.getNome() != null) {
			instance.setNome(StringUtil.limparCaracteresEntreStrings(instance.getNome()));
		}
		
		if (instance.getAtivo() == null) {
			instance.setAtivo(Boolean.FALSE);
		}
		
		if (instance.getDataNascimento() != null && instance.getDataObito() != null ) {
			if (instance.getDataNascimento().after(instance.getDataObito())) {
				FacesMessages.instance().add(Severity.ERROR, "Data de óbito não pode ser anterior à data de nascimento.");
				return false;
			}
		}
		
		if (endereco != null && EnderecoHome.instance().getInstance().getNumeroEndereco() != null) {
			endereco.setNumeroEndereco(EnderecoHome.instance().getInstance().getNumeroEndereco());
			instance.getEnderecoList().add(endereco);
		}
		
		getInstance().setInTipoPessoa(TipoPessoaEnum.F);
		getInstance().setTipoPessoa(ParametroUtil.instance().getTipoPessoaFisica());
		
		if(getPessoaFisicaMunicipioSuggestBean().getInstance() != null) {
			instance.setMunicipioNascimento(getPessoaFisicaMunicipioSuggestBean().getInstance());
		}
		
		if (isManaged()) {
			if (oldCpf != null) {
				if (!oldCpf.equals(getInstance().getNumeroCPF())) {
					instance.setAssinatura(null);
					instance.setCertChain(null);
				}
			}
		}
		return super.beforePersistOrUpdate();
	}
	
	public Localizacao getLocalizacaoFisica() {
		return localizacaoFisica;
	}

	public void setLocalizacaoFisica(Localizacao localizacaoFisica) {
		this.localizacaoFisica = localizacaoFisica;
	}

	public DadosReceitaPessoaFisica getDadosReceitaPessoa() {
		return dadosReceitaPessoa;
	}

	public void setDadosReceitaPessoa(DadosReceitaPessoaFisica dadosReceitaPessoa) {
		this.dadosReceitaPessoa = dadosReceitaPessoa;
	}

	public List<DadosReceitaPessoaFisica> getListDadosReceitaPessoa() {
		this.listDadosReceitaPessoa = new ArrayList<DadosReceitaPessoaFisica>(0);
		if (dadosReceitaPessoa != null) {
			this.listDadosReceitaPessoa.add(dadosReceitaPessoa);
		}
		return listDadosReceitaPessoa;
	}

	public void setListDadosReceitaPessoa(List<DadosReceitaPessoaFisica> listDadosReceitaPessoa) {
		this.listDadosReceitaPessoa = listDadosReceitaPessoa;
	}

	public void consultaDados(String cpf) throws Exception {
		cpf = cpf.replace(".", "").replace("-", "");	
		dadosReceitaPessoa = ConsultaClienteReceitaPFCJF.instance().consultaDadosBase(cpf);
	}

	@Override
	protected String afterPersistOrUpdate(String ret) {
		refreshGrid("pessoaDocumentoIdentificacaoCadastroGrid");
		return super.afterPersistOrUpdate(ret);
	}

	public boolean ehEspecializada() {
		PessoaFisica p = getInstance();
		if (Pessoa.instanceOf(p, PessoaAdvogado.class) || Pessoa.instanceOf(p, PessoaServidor.class) || Pessoa.instanceOf(p, PessoaMagistrado.class)
				|| Pessoa.instanceOf(p, PessoaPerito.class) || Pessoa.instanceOf(p, PessoaProcurador.class) || Pessoa.instanceOf(p, PessoaAssistenteAdvogado.class) 
				|| Pessoa.instanceOf(p, PessoaAssistenteProcuradoria.class)) {
			return true;
		}
		return false;
	}

	@Override
	public void onClickSearchTab() {
		UsuarioLocalizacaoHome.instance().newInstance();
		super.onClickSearchTab();
	}

	public String ataulizaPessoaFisica() {
		return super.update();
	}

	public void verificaProfissao() {
		if (getInstance().getProfissao() != null) {
			setObfuscateProfissao(Boolean.TRUE);
		} else {
			setObfuscateProfissao(Boolean.FALSE);
		}
	}

	public void setObfuscateProfissao(Boolean obfuscateProfissao) {
		this.obfuscateProfissao = obfuscateProfissao;
	}

	public Boolean getObfuscateProfissao() {
		return obfuscateProfissao;
	}

	@Observer("profissaoChangedEvent")
	public void setProfissao(Profissao profissao) {
		if (profissao == null) {
			Contexts.removeFromAllContexts("profissaoSuggest");
		} else {
			ProfissaoSuggestBean profissaoSuggest = ComponentUtil.getComponent("profissaoSuggest");
			profissaoSuggest.setDefaultValue(profissao.getProfissao());
			getInstance().setProfissao(profissao);
		}
	}

	/**
	 * Verifica se a instancia contem o papel jus postulandi
	 * 
	 * @author rodrigo / athos
	 * @category PJE-JT
	 * @since 1.4.2
	 * @return
	 */
	public boolean ehJusPostulandi() {
		boolean jusPostulandi = false;
		Papel papelJusPostulandi = ParametroUtil.instance().getPapelJusPostulandi();
		List<UsuarioLocalizacao> usuarioLocalizacaoList = ComponentUtil.getComponent(UsuarioLocalizacaoManager.class).getLocalizacoesAtuais(getInstance());
		for (UsuarioLocalizacao usuarioLocalizacao : usuarioLocalizacaoList) {
			Papel papel = usuarioLocalizacao.getPapel();
			if (papel.getIdPapel() == papelJusPostulandi.getIdPapel()) {
				jusPostulandi = true;
				break;
			}
		}
		return jusPostulandi;
	}
	
	@Override
	public void setInstance(PessoaFisica instance) {	
		super.setInstance(instance);		
		getCaracteristicaFisicaHome().newInstance();
	}
	
	/**
	 * metodo responsavel por gerar o hash para inserçao da nova senha.
	 * o hash ser enviado para o usuario via e-mail, logo é essencial que o usuario tenha um email cadastrado.
	 * o usuario deverá acessar a tela enviada por e-mail e cadastrar a senha.
	
	 */
	public void gerarNovaSenha(){
		if(getInstance().getEmail() == null) {
			reportMessage("pje.usuarioService.error.informeEmailUsuario", null, getInstance().getNome());		
			return;
		}
		getInstance().setHashAtivacaoSenha(PjeUtil.instance().gerarHashAtivacao(getInstance().getLogin()));
		persist();
		enviarNovaSenha();
	}
	
	/**
	 * Metodo responsavel por enviar a nova senha por email.
	 */
	private void enviarNovaSenha() {
		try {
			UsuarioService usuarioService = getComponent("usuarioService");
			usuarioService.revokeSSOPassword(this.getInstance().getLogin());
			usuarioService.enviarEmailSenha(getInstance());
			reportMessage("pje.pessoaFisicaHome.info.emailEnviadoComSucesso", null, getInstance().getEmail());
		} catch (PJeBusinessException e) {
			reportMessage(e);
		}
	}

	/**
	 * Método responsável por gravar a localização inicial do usuário.
	 */
	public void gravarLocalizacaoInicial() {
		try {
			getEntityManager().persist(getInstance());
			getEntityManager().flush();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "localizacao.sucesso");
		} catch (Exception e) {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "localizacao.erro");
			e.printStackTrace();
		}
	}
	
	/**
	 * Recupera o último login do usuário no sistema.
	 * 
	 * @return Texto no formato HH:mm:ss dd/MM/YYYY. 
	 */
	public String getUltimoLogin() {
		LogAcessoManager logAcessoManager = ComponentUtil.getComponent(LogAcessoManager.NAME);
		String ultimoLogin = logAcessoManager.recuperarUltimoLogin(getInstance().getIdUsuario());
		if (StringUtils.isBlank(ultimoLogin)) {
			return FacesUtil.getMessage("entity_messages", "log.semAcesso");
		}
		return ultimoLogin;
	}

	/**
	 * Recupera a localização inicial do usuário.
	 * 
	 * @return A localização inicial do usuário.
	 */
	public String getLocalizacaoInicial() {
		UsuarioManager usuarioManager = ComponentUtil.getComponent(UsuarioManager.NAME);
		return usuarioManager.recuperarLocalizacaoInicial(getInstance().getIdUsuario());
	}
	
	/**
	 * Recupera todas as localizações do usuário.
	 * 
	 * @return As localizações do usuário.
	 */
	public List<UsuarioLocalizacao> getLocalizacoes() {
		UsuarioLocalizacaoManager usuarioLocalizacaoManager = ComponentUtil.getComponent("usuarioLocalizacaoManager");
		return usuarioLocalizacaoManager.recuperarLocalizacoes(getInstance().getIdUsuario());
	}
	
	public List<UsuarioLocalizacao> getLocalizacoesConciliador(PessoaFisica pessoaFisica) {
		List<UsuarioLocalizacao> usuarioLocalizacaoList = new ArrayList<UsuarioLocalizacao>();
		if(pessoaFisica != null) {
			PapelService papelService = getComponent("papelService");
			Papel papelConciliador = papelService.findByCodeName(Papeis.CONCILIADOR);
			UsuarioLocalizacaoManager usuarioLocalizacaoManager = ComponentUtil.getComponent("usuarioLocalizacaoManager");
			usuarioLocalizacaoList = usuarioLocalizacaoManager.getLocalizacoesAtuais(pessoaFisica, papelConciliador);
		}
		return usuarioLocalizacaoList;
	}
	
	/**
	 * Recupera os perfis do usuário.
	 * 
	 * @return Os perfis do usuário.
	 */
	public Map<Papel, List<UsuarioLocalizacao>> getPerfis() {
		Map<Papel, List<UsuarioLocalizacao>> perfis = new LinkedHashMap<Papel, List<UsuarioLocalizacao>>(0);	
		List<UsuarioLocalizacao> usuarioLocalizacoes = this.getLocalizacoes();
		for (UsuarioLocalizacao usuarioLocalizacao : usuarioLocalizacoes) {
			Papel papel = usuarioLocalizacao.getPapel();
			if (!perfis.containsKey(papel)) {
				perfis.put(papel, new ArrayList<UsuarioLocalizacao>(0));
			}
			perfis.get(papel).add(usuarioLocalizacao);
		}
		return perfis;
	}

	public void atribuirPapeis(String... idPapeis) {
		this.papeis = PapelHome.instance().getPapelList(Arrays.asList(idPapeis));
	}

	public List<Papel> getPapeis() {
		return this.papeis;
	}

	/**
	 * Este método inativa o conciliador informado
	 * 
	 * @param instance
	 *            A pessoa conciliador que se deseja inativar
	 * @return
	 */
	public void inativarConciliador(PessoaFisica instance) {
		pessoaFisicaManager = getComponent(PessoaFisicaManager.NAME);
		try {
			instance.setAtivo(Boolean.FALSE);
			pessoaFisicaManager.persistAndFlush(instance);
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "pessoaConciliador.perfil.inativado");
			refreshGrid("pessoaConciliadorGrid");
			Authenticator.deslogar(instance, "perfil.atualizar");
		} catch (Exception e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "perfil.erro");
		}
	}	

	/**
	 * Método responsável por remover a localização de conciliador da {@link PessoaFisica}.
	 */
	public void removerPerfilConciliador() {
		PessoaFisica conciliador = getInstance();
		for (UsuarioLocalizacao usuarioLocalizacao : getLocalizacoes()) {
			Papel papel = usuarioLocalizacao.getPapel();
			if (Authenticator.isPapelConciliador(papel)) {
				conciliador.getUsuarioLocalizacaoList().remove(usuarioLocalizacao);
				getEntityManager().remove(usuarioLocalizacao);
				break;
			}
		}
		getEntityManager().flush();
		update();
	}

	public void atualizarEmailSSO() {
		 KeycloakServiceClient.instance();
		 try {
			KeycloakServiceClient.instance().updateEmail(this.getInstance().getLogin(), this.getInstance().getEmail());
			
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "Registro alterado com sucesso.");
		} catch (PJeBusinessException e) {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR,  e.getCode());		
		}		 
	}	
	
	@Override
	public String inactive(PessoaFisica instance) {
		String inactive = super.inactive(instance);
        GridQuery gridQuery = ComponentUtil.getComponent("pessoaFisicaGrid");
        if (gridQuery != null) {
            gridQuery.refresh();
        }
		return inactive;
	}

	public String getEmail1() {
		return email1;
	}

	public void setEmail1(String email1) {
		this.email1 = email1;
	}
	
	public boolean podeCadastrarNomeSocial() {
		return ComponentUtil.getPessoaFisicaManager().podeCadastrarNomeSocial(getInstance());
	}

	public Boolean getInformarNomeSocial() {
		return informarNomeSocial;
	}

	public void setInformarNomeSocial(Boolean informarNomeSocial) {
		this.informarNomeSocial = informarNomeSocial;
	}
}