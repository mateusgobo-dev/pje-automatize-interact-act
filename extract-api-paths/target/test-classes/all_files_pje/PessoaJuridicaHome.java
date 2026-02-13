package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.pje.manager.PessoaJuridicaManager;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.LogAcessoManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioManager;
import br.jus.cnj.pje.nucleo.manager.cache.ProcessoParteCache;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.PessoaLocalizacao;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.TipoOrgaoPublicoEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.nucleo.util.StringUtil;

@Name("pessoaJuridicaHome")
@BypassInterceptors
public class PessoaJuridicaHome extends AbstractPessoaJuridicaHome<PessoaJuridica> {

	private static final long serialVersionUID = 1L;
	private Endereco endereco;
	private Date dataInicio;
	private Date dataFim;
	private String numeroCNPJ;
	
	private PessoaJuridicaManager pessoaJuridicaManager;

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

	@Override
	protected boolean beforePersistOrUpdate() {
		if (instance.getNome() != null) {
			instance.setNome(StringUtil.limparCaracteresEntreStrings(instance.getNome()));
		}
		
		if(instance.getNumeroCNPJ() != null && !InscricaoMFUtil.verificaCNPJ(instance.getNumeroCNPJ())){
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "CNPJ inválido");
			return false;
		}
		
		if (instance instanceof PessoaJuridica) {
			PessoaJuridica pj = (PessoaJuridica) instance;
			if (pj.getDataAbertura() != null && pj.getDataFimAtividade() != null) {
				if (pj.getDataAbertura().after(pj.getDataFimAtividade())) {
					FacesMessages.instance().add(Severity.ERROR, "Data de encerramento não pode ser anterior à data de abertura.");
					return false;
				}
			}
		}

		if(getInstance().getTipoOrgaoPublico() == null){ 
			getInstance().setValorLimiteRpv(null);
		} else if(getInstance().getTipoOrgaoPublico().equals(TipoOrgaoPublicoEnum.F)){
			getInstance().setValorLimiteRpv(null);
		}
		return super.beforePersistOrUpdate();
	}

	@Override
	public String persist() {
		PessoaJuridica pessoaJuridica = getInstance();
		pessoaJuridica.setTipoPessoa(ParametroUtil.instance().getTipoPessoaJuridica());
		String persist = null;
		
		try {
			if (beforePersistOrUpdate()) {
				pessoaJuridicaManager = (PessoaJuridicaManager)getComponent(PessoaJuridicaManager.NAME);
				pessoaJuridicaManager.persistAndFlush(pessoaJuridica);
				createdMessage();
				persist = afterPersistOrUpdate("persisted");
			}
		} catch (PJeBusinessException e) {
			reportMessage(e);
		}		
		
		return persist;
	}
	
	@Override
	public String update() {
		try {
			if (beforePersistOrUpdate()) {
				pessoaJuridicaManager = (PessoaJuridicaManager)getComponent(PessoaJuridicaManager.NAME);
				pessoaJuridicaManager.merge(getInstance());
				pessoaJuridicaManager.flush();
				setInstance(getInstance());
				PessoaHome.instance().atualizarNomeLocalizacao(getInstance());
				updatedMessage();

				getProcessoParteCache().refreshProcessoParteByProcessoTrfEPessoaCache(
						ProcessoTrfHome.instance().getInstance().getIdProcessoTrf(), getInstance().getIdPessoa());

				return afterPersistOrUpdate("update");
			}
		} catch (PJeBusinessException e) {
			reportMessage(e);
		}
		
		return null;
	}

	public void inserirEntidade() {
		PessoaJuridica pessoaJuridica = getInstance();
		persist();

		PessoaLocalizacao pessoaLocalizacao = new PessoaLocalizacao();

		Localizacao localizacao = new Localizacao();
		localizacao.setAtivo(Boolean.TRUE);
		getEntityManager().persist(endereco);
		localizacao.setEndereco(endereco);
		localizacao.setLocalizacao(pessoaJuridica.getNome() + " (" + pessoaJuridica.getNumeroCNPJ() + ")"); 
		localizacao.setLocalizacao(pessoaJuridica.getNome());
		getEntityManager().persist(localizacao);

		pessoaLocalizacao.setLocalizacao(localizacao);
		pessoaLocalizacao.setPessoa(pessoaJuridica);
		getEntityManager().persist(pessoaLocalizacao);

		refreshGrid("entidadeGrid");
		refreshGrid("entidadeLocalizacaoGrid");
	}

	public Boolean verificaDisponibilidadeLocalizacao(String nome) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from Localizacao o ");
		sb.append("where o.localizacao = :nome");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("nome", nome);

		try {
			Long retorno = (Long) q.getSingleResult();
			if (retorno > 0){
				return Boolean.FALSE;
			}
			return Boolean.TRUE;
		} catch (NoResultException no) {
			return Boolean.TRUE;
		}
	}

	public void inserirEscritorioAdvocacia() {
		try{
			if (beforePersistOrUpdate()) {
				getInstance().setTipoPessoa(ParametroUtil.instance().getTipoPessoaEscritorioAdvocacia());
				PessoaJuridica pessoaJuridica = getInstance();
				for (PessoaDocumentoIdentificacao pdi : pessoaJuridica.getPessoaDocumentoIdentificacaoList()) {
					if (pdi.getNome() == null) {
						pdi.setNome(pessoaJuridica.getNome());
					}
				}

				persist();
				afterPersistOrUpdate("persisted");

				UsuarioLocalizacao usuarioLocalizacao = new UsuarioLocalizacao();
				PessoaLocalizacao pessoaLocalizacao = new PessoaLocalizacao();

				Localizacao localizacao = new Localizacao();
				localizacao.setAtivo(Boolean.TRUE);
				getEntityManager().persist(endereco);
				localizacao.setEndereco(endereco);
				localizacao.setLocalizacao(pessoaJuridica.getNome() + " (" + pessoaJuridica.getNumeroCNPJ() + ")");
				localizacao.setLocalizacao(pessoaJuridica.getNome());

				getEntityManager().persist(localizacao);

				usuarioLocalizacao.setUsuario(PessoaAdvogadoHome.instance().getInstance().getPessoa());
				usuarioLocalizacao.setLocalizacaoFisica(localizacao);
				usuarioLocalizacao.setResponsavelLocalizacao(Boolean.TRUE);
				usuarioLocalizacao.setPapel(ParametroUtil.instance().getPapelAdvogado());
				getEntityManager().persist(usuarioLocalizacao);

				getEntityManager().merge(pessoaJuridica);

				pessoaLocalizacao.setLocalizacao(localizacao);
				pessoaLocalizacao.setPessoa(EntityUtil.find(PessoaJuridica.class, pessoaJuridica.getIdUsuario()));

				getEntityManager().persist(pessoaLocalizacao);

				PessoaAdvogadoHome.instance().getInstance().getUsuarioLocalizacaoList().add(usuarioLocalizacao);

				EntityUtil.flush();

				refreshGrid("escritorioAdvogadoGrid");

				newInstance();
			}
			
		}catch (Exception e) {			
			FacesMessages.instance().add(Severity.INFO, e.getMessage());
		}		
	}

	/**
	 * Método chamado pelo botão cadastroPessoas. Encontra-se na página de
	 * inserção de partes de um processo, tanto na consulta de um processo
	 * quanto no cadastro.
	 */
	public void inserirAba() {
		this.persist();
		if (ProcessoTrfHome.instance().getInstance().getClasseJudicial() != null) {
			refreshGrid("cadastroPartesGrid");
			refreshGrid("cadastroPartesAdvGrid");
			refreshGrid("processoPoloAtivoGrid");
			refreshGrid("processoPoloPassivoGrid");
			refreshGrid("cadastroPartesGrid");
			newInstance();
			FacesMessages.instance().clear();
			FacesMessages.instance().add("Pessoa inserida no Processo com sucesso");
		}
	}
	
	public String getNumeroCNPJ() {
		return numeroCNPJ;
	}

	public void setNumeroCNPJ(String numeroCNPJ) {
		if(StringUtil.CNPJ_EMPTYMASK.equals(numeroCNPJ)){
			numeroCNPJ = "";
		}
		this.numeroCNPJ = numeroCNPJ;
	}

	public boolean checkCNPJ(String cnpj, Integer idUsuario) {
		pessoaJuridicaManager = (PessoaJuridicaManager)getComponent(PessoaJuridicaManager.NAME);
		PessoaJuridica pessoaJuridica = pessoaJuridicaManager.findByCNPJ(cnpj);
		
		if(pessoaJuridica != null && pessoaJuridica.getIdPessoa().equals(idUsuario)){
			FacesMessages.instance().addToControl("messages_valida", StatusMessage.Severity.ERROR,
					"CNPJ já cadastrado!");
			limparCnpj();
			return true;
		}
		
		return false;
	}

	public void limparCnpj() {
		getInstance().setNumeroCNPJ("");
	}

	public boolean checkCNPJ() {
		if (!InscricaoMFUtil.validarCpfCnpj(getInstance().getNumeroCNPJ())) {
			FacesMessages.instance().add(Severity.ERROR, "CNPJ em formato inválido. Favor verificar.");
			return false;
		}
		
		return checkCNPJ(getInstance().getNumeroCNPJ(), getInstance().getIdUsuario());
	}

	public boolean checkLogin() {
		pessoaJuridicaManager = (PessoaJuridicaManager)getComponent(PessoaJuridicaManager.NAME);
		Boolean loginDisponivel = pessoaJuridicaManager.checkLogin(getInstance().getLogin(), getInstance().getIdUsuario());
		if(!loginDisponivel){
			FacesMessages.instance().addToControl("loginLogin", StatusMessage.Severity.ERROR, "Login já cadastrado!");
			getInstance().setLogin("");
			return true;
		}
		
		return false;
	}

	public static PessoaJuridicaHome instance() {
		return ComponentUtil.getComponent("pessoaJuridicaHome");
	}

	private PessoaAdvogadoHome getPessoaAdvogadoHome() {
		return getComponent("pessoaAdvogadoHome");
	}

	@Override
	public void newInstance() {
		if (getInstance() != null && getInstance().getEnderecoList() != null) getInstance().getEnderecoList().clear();
		super.newInstance();
		getPessoaAdvogadoHome().setEndereco(null);
		endereco = new Endereco();
		Contexts.removeFromAllContexts("cepSuggestLocalizacao");
	}

	@Override
	public void setId(Object id) {
		super.setId(id);
		PessoaHome.instance().setId(id);
		if(PessoaHome.instance().getInstance() != null && PessoaHome.instance().getInstance().getInTipoPessoa() == null){
			PessoaHome.instance().getInstance().setInTipoPessoa(TipoPessoaEnum.J);
		}
		getInstance().setAssociarPapelParaRemessa(getAssociarPapelParaRemessa());
	}

	@Observer("cepLocalizacaoChangedEvent")
	public void setEndereco(Cep cep) {
		endereco = new Endereco();
		if (cep == null) {
			Contexts.removeFromAllContexts("cepSuggestLocalizacao");
		} else {
			endereco.setCep(cep);
			endereco.setNomeEstado(cep.getMunicipio().getEstado().getEstado());
			endereco.setNomeCidade(cep.getMunicipio().getMunicipio());
			endereco.setNomeLogradouro(cep.getNomeLogradouro());
			endereco.setNomeBairro(cep.getNomeBairro());
		}
	}

	public Endereco getEndereco() {
		return endereco;
	}

	public String getCNPJ(PessoaJuridica obj) {
		String query = "SELECT id.numeroDocumento FROM PessoaDocumentoIdentificacao id " +
				"	WHERE id.ativo = true " +
				"	AND id.usadoFalsamente = false " +
				"	AND id.pessoa = :pessoa " +
				"	AND id.tipoDocumento.codTipo = :tipoDocumento " +
				"	AND id.numeroDocumento = :numeroDocumento ";
		Query q = EntityUtil.createQuery(query);
		q.setParameter("pessoa", obj);
		q.setParameter("tipoDocumento", "CPJ");
		q.setParameter("numeroDocumento", obj.getNumeroCNPJ());
		q.setFirstResult(0);
		q.setMaxResults(1);
		try{
			return (String) q.getSingleResult();
		}catch (NoResultException e){
			return null;
		}
	}

	public String localizacaoCNPJ(Localizacao obj) {
		Criteria criteria = HibernateUtil.getSession().createCriteria(Pessoa.class);
		criteria.add(Restrictions.eq("nome", obj.getLocalizacao()));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		Pessoa pessoa = (Pessoa)criteria.uniqueResult();
		if (pessoa != null) {
			return pessoa.getDocumentoCpfCnpj();
		}
		return null;
	}
	
	public List<SelectItem> getPrazoExpedienteAutomaticoList(){
		List<SelectItem> sil = new ArrayList<SelectItem>();
		sil.add(new SelectItem(null, "Selecione"));
		sil.add(new SelectItem(1, "Simples"));
		sil.add(new SelectItem(2, "Dobro"));
		sil.add(new SelectItem(4, "Quádruplo"));
		return sil;
	}
	
	/**
	 * Metodo para gerar uma nova senha ao usuario. 
	 * A alteracao no metodo consiste em que apenas o campo senha sofra atualizacao no banco, preservando assim os outros 
	 * campos do objeto PessoaJuridica, ao executar o flush, evitando que outras alteracoes sejam persistidas indevidamente. 
	 * CNJ abrira uma issue para uma nova implementacao na funcionalidade, posteriormente.
	 */
	public void gerarNovaSenha(){
		if (getInstance().getEmail() == null) {
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

	@Override
	protected String afterPersistOrUpdate(String ret) {
		UsuarioLocalizacaoManager manager = UsuarioLocalizacaoManager.instance();
		manager.associarLocalizacaoParaRemessa(getInstance(), true);
		
		return ret;
	}
	
	
	/**
	 * Retorna true se existir localização de remessa para a pessoa jurídica da classe Home.
	 * 
	 * @return booleano.
	 */
	public Boolean getAssociarPapelParaRemessa() {
		UsuarioLocalizacaoManager manager = UsuarioLocalizacaoManager.instance();
		
		return manager.isExisteLocalizacaoParaRemessa(getInstance());
	}
}