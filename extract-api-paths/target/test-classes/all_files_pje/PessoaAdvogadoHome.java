package br.com.infox.cliente.home;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.Util;
import br.com.infox.cliente.component.suggest.PessoaAdvogadoMunicipioSuggestBean;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.core.certificado.util.VerificaCertificadoPessoa;
import br.com.infox.ibpm.component.suggest.CepSuggestBean;
import br.com.infox.ibpm.entity.log.LogUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.EnderecoHome;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.pje.manager.PessoaFisicaManager;
import br.com.infox.trf.webservice.ConsultaClienteOAB;
import br.com.infox.trf.webservice.ConsultaClienteWebService;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.LocalizacaoUtil;
import br.jus.cnj.certificado.SigningUtilities;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.DocumentoPessoaManager;
import br.jus.cnj.pje.nucleo.manager.EnderecoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaAdvogadoManager;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.DocumentoPessoa;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Escolaridade;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaAssistenteAdvogado;
import br.jus.pje.nucleo.entidades.PessoaAssistenteProcuradoria;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaFisicaEspecializada;
import br.jus.pje.nucleo.entidades.PessoaLocalizacao;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.PessoaServidor;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoPush;
import br.jus.pje.nucleo.entidades.Profissao;
import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.PessoaAdvogadoTipoInscricaoEnum;
import br.jus.pje.nucleo.enums.SexoEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.ws.externo.cna.entidades.DadosAdvogadoOAB;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaFisica;

@Name(PessoaAdvogadoHome.NAME)
@BypassInterceptors
public class PessoaAdvogadoHome extends AbstractPessoaAdvogadoHome<PessoaAdvogado> implements ArquivoAssinadoUploader{

	private static final LogProvider log = Logging.getLogProvider(PessoaAdvogadoHome.class);
	private static final long serialVersionUID = 1L;
	public static final String NAME = "pessoaAdvogadoHome"; 
	
	private boolean cpfJaCadastrado = true;
	private boolean loginJaCadastrado = true;
	private Integer ultimoIdInserido;
	private Date dataInicio;
	private Date dataFim;
	private Endereco endereco = new Endereco();
	private boolean existeInconsistencia = false;
	private boolean veracidadeCadastro = false;
	private boolean exibePopUpInconsistencia = false;
	private final StringBuffer inconsistenciasOabReceita = new StringBuffer();
	private boolean verificarNome = true;
	private boolean verificarCPF = true;
	private boolean verificarDadosCertificado = true;
	private DocumentoPessoa documentoPessoa;
	private String email1;
	private String email2;
	private String email3;
	private boolean inicializado = false;
	private String certChain;
	private String signature;
	private Boolean radioResetCertificado = Boolean.FALSE;
	private Estado estado;
	private List<DadosAdvogadoOAB> listDadosAdv = new ArrayList<DadosAdvogadoOAB>(0);
	private Pessoa pessoaLogada;
	private Boolean advogado = Boolean.FALSE;
	private String oldCpf;
	private String cnpj = "0";
	private List<ProcessoParte> participacoesProcesso = null;
	private List<Pessoa> pessoasRepresentadas = null;
	
	private Boolean ocorreuErroWsReceita = false;
	private Boolean ocorreuErroWsOAB = false;
	private String erroOAB = "";
	
	private Boolean possuiComprovanteCadastro = false;
	private DocumentoPessoa termoConfirmacaoCadastro;
	private ArrayList<ParAssinatura> assinaturas;
	
	private PessoaAdvogadoTipoInscricaoEnum tipoInscricao;	
	
	public void addEscritorioAdvogado(PessoaAdvogado pessoaAdvogado, Localizacao localizacao) {
		UsuarioLocalizacao usuarioLocalizacao = new UsuarioLocalizacao();
		if (usuarioLocalizacao != null) {
			usuarioLocalizacao.setLocalizacaoFisica(localizacao);
			usuarioLocalizacao.setUsuario(pessoaAdvogado.getPessoa());
			usuarioLocalizacao.setPapel(ParametroUtil.instance().getPapelAdvogado());
			usuarioLocalizacao.setResponsavelLocalizacao(Boolean.FALSE);
			this.getEntityManager().persist(usuarioLocalizacao);
			this.getEntityManager().flush();
			pessoaAdvogado.getUsuarioLocalizacaoList().add(usuarioLocalizacao);
		}

		FacesMessages.instance().clear();
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		this.getInstance().setMunicipioNascimento(this.getPessoaAdvogadoMunicipioSuggest().getInstance());
		this.getInstance().setEmail(this.montarEmail());
		if (this.isManaged())
			if (!this.oldCpf.equals(this.getInstance().getNumeroCPF())) {
				this.instance.setAssinatura(null);
				this.instance.setCertChain(null);
			}
		return true;
	}

	@SuppressWarnings("unchecked")
	public void carregarParticipacoesProcessos() {

		String consultaProcessosProprios = "SELECT DISTINCT pp FROM ProcessoParte pp WHERE pp.pessoa = :pessoa";
		String consultaProcessosRepresentados = "SELECT DISTINCT pp " + "	FROM ProcessoParte pp "
				+ "	INNER JOIN pp.processoParteRepresentanteList advs " + "   INNER JOIN advs.representante rep "
				+ "	WHERE rep = :pessoa " + "	AND advs.tipoRepresentante = :tipoAdvogado";
		Pessoa pessoaLogada = (Pessoa) ProcessoHome.instance().getUsuarioLogado();
		List<ProcessoParte> listaProcessosParte = new ArrayList<ProcessoParte>(0);
		Query q = getEntityManager().createQuery(consultaProcessosProprios);
		q.setParameter("pessoa", pessoaLogada);
		listaProcessosParte.addAll(q.getResultList());
		q = getEntityManager().createQuery(consultaProcessosRepresentados);
		q.setParameter("pessoa", pessoaLogada);
		q.setParameter("tipoAdvogado", ParametroUtil.instance().getTipoParteAdvogado());
		listaProcessosParte.addAll(q.getResultList());

		if (Pessoa.instanceOf(pessoaLogada, PessoaProcurador.class)) {
			String consultaProcessosParteProcuradoria = "SELECT DISTINCT ppp.pessoaProcuradoriaEntidade.pessoa.processoParteList "
					+ "	FROM PessoaProcuradorProcuradoria ppp " + "	WHERE ppp.pessoaProcurador = :pessoa";
			q = getEntityManager().createQuery(consultaProcessosParteProcuradoria);
			q.setParameter("pessoa", pessoaLogada);
			listaProcessosParte.addAll(q.getResultList());
		}

		participacoesProcesso = listaProcessosParte;

	}

	public boolean checkCPF() {
		this.cpfJaCadastrado = this.checkCPF(this.getInstance().getNumeroCPF(), this.getInstance().getIdUsuario());
		return this.cpfJaCadastrado;
	}
	
	public List<Pessoa> consultaAdvogados(String cpf, Integer idUsuario){
		PessoaFisica pf = ComponentUtil.getComponent(PessoaFisicaManager.class).findByCPF(cpf);
		if(pf.getIdPessoa() != idUsuario){
			List<Pessoa> result = new ArrayList<Pessoa>(0);
			result.add(pf);
			return result;
		}
		
		return null;
	}

	// Valida se o cpf já existe no TRF
	public boolean checkCPF(String cpf, Integer idUsuario) {
		Boolean isCadastrado = ComponentUtil.getComponent(PessoaAdvogadoManager.class).checkCPF(cpf, idUsuario);

		if (isCadastrado) {
			FacesMessages.instance().addToControl("numeroCPFCpf", StatusMessage.Severity.ERROR, "CPF já cadastrado!");
			
			this.limparCpf();
			this.cpfJaCadastrado = true;
		} else {
			this.cpfJaCadastrado = false;
		}

		return this.cpfJaCadastrado;
	}

	public boolean checkLogin() {
		Boolean loginJaCadastrado = ComponentUtil.getComponent(PessoaAdvogadoManager.class).checkLogin(getInstance().getLogin(), getInstance().getIdUsuario());
		if(!loginJaCadastrado){
			FacesMessages.instance().addToControl("loginLogin", StatusMessage.Severity.ERROR, "Login ja cadastrado");
			getInstance().setLogin("");
			return false;
		}
		
		return true;
	}

	public void confirmarCadastro() {
		this.getInstance().setValidado(Boolean.TRUE);
		this.getInstance().setAtivo(Boolean.TRUE);
		this.getInstance().setDataValidacao(new Date());
		if (getInstance().getDataCadastro() == null) {
			getInstance().setDataCadastro(new Date());
		}
		update();
		FacesMessages.instance().clear();
		FacesMessages.instance().add(StatusMessage.Severity.INFO, "Usuário validado com sucesso.");
	}

	public void consultaDados(String cpf) throws Exception {
		cpf = cpf.replace(".", "").replace("-", "");
		List<DadosAdvogadoOAB> dados = ConsultaClienteOAB.instance().consultaDadosBase(cpf);
		if (dados.size() != 0) {
			this.setListDadosAdv(dados);
		}
	}

	public void consultaDadosOABWebService(String cpf) throws Exception {
		String cpfConsulta = StringUtil.removeNaoNumericos(cpf);

		try {
			ConsultaClienteOAB consultaClienteOAB = new ConsultaClienteOAB();
			consultaClienteOAB.consultaDados(cpfConsulta, true);
			List<DadosAdvogadoOAB> dadosAdvogadoList = consultaClienteOAB.getDadosAdvogadoList();
			if (dadosAdvogadoList == null || dadosAdvogadoList.size() == 0) {
				FacesMessages.instance().add(Severity.ERROR, "A OAB não retornou dados para este advogado.");
			} else {
				FacesMessages.instance().add(Severity.INFO, "Dados atualizados com sucesso.");
			}
		} catch (Exception e) {
			ocorreuErroWsOAB = true;
			erroOAB = "Erro ao consultar a OAB: " + e.getMessage();
		}
	}

	public void consultaDadosReceitaWebService(String cpfConsulta) throws Exception {		
		try {

			DadosReceitaPessoaFisica dados = (DadosReceitaPessoaFisica) ConsultaClienteWebService.instance()
					.consultaDados(TipoPessoaEnum.F, cpfConsulta, true);

			if (dados == null) {
				FacesMessages.instance()
						.add(Severity.ERROR, "A Receita Federal não retornou dados para este advogado.");
			} else {
				FacesMessages.instance().add(Severity.INFO, "Dados atualizados com sucesso.");
			}
		} catch (Exception e) {
			ocorreuErroWsReceita = true;
		}
	}

	// Valida se existe Pauta de Julgamento
	public boolean existePautadeJulgamento() {
		EntityManager em = this.getEntityManager();

		String sql = "select o from ProcessoExpediente o WHERE o.idProcessoExpediente = :idTipoProcesso";

		String idTipoProcesso = ParametroUtil.getFromContext("idTipoProcessoDocumentoIntimacaoPauta", true);

		Query query = em.createQuery(sql);
		query.setParameter("idTipoProcesso", Integer.parseInt(idTipoProcesso));

		int result = query.getResultList().size();

		return (result > 0);
	}

	public Boolean exists(String idUsuario) {
		int id = Integer.parseInt(idUsuario);
		UsuarioLogin usuarioLogin = EntityUtil.find(UsuarioLogin.class, id);
		return (usuarioLogin != null);
	}

	public Boolean getAdvogado() {
		return this.advogado;
	}

	public String getCamposDivergencia() {
		return this.inconsistenciasOabReceita.toString();
	}

	public String getCertChain() {
		return this.certChain;
	}

	public String getCnpj() {
		return this.cnpj;
	}

	public boolean getCpfJaCadastrado() {
		return this.cpfJaCadastrado;
	}

	public Date getDataFim() {
		return this.dataFim;
	}

	public Date getDataInicio() {
		return this.dataInicio;
	}

	public DocumentoPessoa getDocumentoPessoa() {
		return this.documentoPessoa;
	}

	public String getEmail1() {
		return this.email1;
	}

	public String getEmail2() {
		return this.email2;
	}

	public String getEmail3() {
		return this.email3;
	}

	public Endereco getEndereco() {
		return this.endereco;
	}

	private EnderecoHome getEnderecoHome() {
		return ComponentUtil.getComponent(EnderecoHome.class);
	}

	public Estado getEstado() {
		return this.estado;
	}

	public boolean getExibePopUpInconsistencia() {
		return this.exibePopUpInconsistencia;
	}

	public List<DadosAdvogadoOAB> getListDadosAdv() {
		return this.listDadosAdv;
	}

	public boolean getLoginJaCadastrado() {
		return this.loginJaCadastrado;
	}

	private PessoaAdvogadoMunicipioSuggestBean getPessoaAdvogadoMunicipioSuggest() {
		return ComponentUtil.getComponent(PessoaAdvogadoMunicipioSuggestBean.class);
	}

	@SuppressWarnings("unchecked")
	public List<Pessoa> getPessoaAdvogadoProcurador() {
		List<Pessoa> listaPessoas = new ArrayList<Pessoa>(0);
		Pessoa pessoaLogada = Authenticator.getPessoaLogada();
		listaPessoas.add(pessoaLogada);

		if (Pessoa.instanceOf(pessoaLogada, PessoaProcurador.class)) {
			String sql = "select o.pessoaProcuradoriaEntidade.pessoa from PessoaProcuradorProcuradoria o "
					+ "where o.pessoaProcurador.idUsuario = :idUsuario";
			Query q = this.getEntityManager().createQuery(sql);
			q.setParameter("idUsuario", pessoaLogada.getIdUsuario());
			listaPessoas.addAll(q.getResultList());
		} else if (Pessoa.instanceOf(pessoaLogada, PessoaAssistenteProcuradoria.class)) {
			StringBuilder sb = new StringBuilder();
			sb.append("select o.usuario from PessoaAssistenteProcuradoriaLocal o where ");
			sb.append("o.localizacaoFisica = :localizacao");
			Query q = getEntityManager().createQuery(sb.toString());
			q.setParameter("localizacao", Authenticator.getLocalizacaoFisicaAtual());
			listaPessoas.addAll(q.getResultList());
		} else if (Pessoa.instanceOf(pessoaLogada, PessoaAssistenteAdvogado.class)) {
			StringBuilder sb = new StringBuilder();
			sb.append("select o.usuario from PessoaAssistenteAdvogadoLocal o where ");
			sb.append("o.localizacaoFisica = :localizacao");
			Query q = getEntityManager().createQuery(sb.toString());
			q.setParameter("localizacao", Authenticator.getLocalizacaoFisicaAtual());
			listaPessoas.addAll(q.getResultList());
		}

		return listaPessoas;
	}

	public Pessoa getPessoaLogada() {
		return this.pessoaLogada;
	}

	public Boolean getRadioResetCertificado() {
		return this.radioResetCertificado;
	}

	public SexoEnum[] getSexoValues() {
		return SexoEnum.values();
	}

	public PessoaAdvogadoTipoInscricaoEnum[] getTipoInscricaoValues() {
		return PessoaAdvogadoTipoInscricaoEnum.values();
	}

	public Integer getUltimoIdInserido() {
		return this.ultimoIdInserido;
	}

	public boolean getVeracidadeCadastro() {
		return this.veracidadeCadastro;
	}

	@Override
	public boolean isEditable() {
		return ParametroUtil.instance().getPermitirCadastrosBasicos();
	}

	private boolean isEnderecoValido() {
		FacesMessages fm = FacesMessages.instance();
		PessoaAdvogado pa = getInstance();
		if (pa.getEnderecoList().size() > 0) {
			this.endereco = pa.getEnderecoList().get(0);
		}
		if ((this.endereco == null) || (this.endereco.getCep() == null)
				|| Strings.isEmpty(this.endereco.getNomeEstado())) {
			String msg = "Endereço inválido";
			fm.add(Severity.ERROR, msg);
			log.info("isEnderecoValido():" + msg);
			return false;
		}
		if (ComponentUtil.getComponent(CepSuggestBean.class).getInstance() == null) {
			String msg = "Cep inválido";
			fm.add(Severity.ERROR, msg);
			log.info("isEnderecoValido():" + msg);
			return false;
		}
		return true;
	}

	public boolean isExisteInconsistencia() {
		return this.existeInconsistencia;
	}

	public boolean isInicializado() {
		return this.inicializado;
	}

	public void limparCpf() {
		this.getInstance().setNumeroCPF("");
	}

	public void limparLogin() {
		this.getInstance().setLogin("");
	}

	private void logGravar(Object obj) {
		log.info("Gravando: " + LogUtil.toStringFields(obj));
	}

	private String montarEmail() {
		StringBuffer buffer = new StringBuffer();
		if (!Strings.isEmpty(this.email1)) {
			buffer.append(this.email1);
		}

		if (!Strings.isEmpty(this.email2)) {
			if (buffer.length() > 0) {
				buffer.append(",");
			}
			buffer.append(this.email2);
		}

		if (!Strings.isEmpty(this.email3)) {
			if (buffer.length() > 0) {
				buffer.append(",");
			}
			buffer.append(this.email3);
		}
		return buffer.toString();
	}

	@Override
	public void newInstance() {
		this.oldCpf = null;
		Contexts.removeFromAllContexts("cepSuggest");
		Contexts.removeFromAllContexts("pessoaAdvogadoMunicipioSuggest");
		this.email1 = "";
		this.email2 = "";
		this.email3 = "";
		this.inicializado = true;

		super.clearInstance(true);
		this.getEnderecoHome().newInstance();
		this.endereco = new Endereco();

		Profissao profissao = ParametroUtil.instance().getProfissaoAdvogado();
		Escolaridade escolaridade = ParametroUtil.instance().getEscolaridadeEnsinoSuperior();
		TipoPessoa tipoAdvogado = ParametroUtil.instance().getTipoAdvogado();
		if ((tipoAdvogado == null) || (profissao == null) || (escolaridade == null)) {
			return;
		}
		this.getInstance().setTipoPessoa(tipoAdvogado);
		this.getInstance().setValidado(false);
		this.getInstance().setProfissao(profissao);
		this.getInstance().setEscolaridade(escolaridade);
		this.endereco = new Endereco();
	}

	@Override
	public String persist() {
		return this.persist(false);
	}

	public String persist(boolean preCadastro) {
		String persist = null;
		if (isEnderecoValido()) {
			System.out.println("Não há endereço para o advogado [" + getInstance().getNome() + "].");
		}
		if (this.beforePersistOrUpdate() && this.isEnderecoValido() || preCadastro) {
			this.inicializado = false;
			PessoaAdvogado advogado = this.getInstance();
			try {
				ComponentUtil.getComponent(PessoaAdvogadoManager.class).persistAndFlush(advogado);
				this.logGravar(advogado);
				setInstance(advogado);
				afterPersistOrUpdate("persisted");
			} catch (PJeBusinessException e) {
				reportMessage(e);
			}
			
			this.ultimoIdInserido = advogado.getIdUsuario();

			FacesMessages fm = FacesMessages.instance();
			if (isEnderecoValido()) {
				this.endereco.setComplemento(this.getEnderecoHome().getInstance().getComplemento());
				this.endereco.setNumeroEndereco(this.getEnderecoHome().getInstance().getNumeroEndereco());
				this.endereco.setDataAlteracao(new Date());
				this.endereco.setNomeCidade(this.getEnderecoHome().getInstance().getNomeCidade());
				this.endereco.setNomeEstado(this.getEnderecoHome().getInstance().getNomeEstado());
				advogado.getEnderecoList().add(endereco);
				endereco.setUsuario(advogado.getPessoa());
				getEntityManager().persist(endereco);
				logGravar(endereco);
			}
			UsuarioLocalizacao usuarioLocalizacao = new UsuarioLocalizacao();
			PessoaLocalizacao pessoaLocalizacao = new PessoaLocalizacao();

			boolean localizacaoPendente = true;
			for (Localizacao l : advogado.getLocalizacoes()) {
				if (l.getLocalizacao().equalsIgnoreCase(LocalizacaoUtil.formataLocalizacaoPessoaFisicaEspecializada(advogado))) {
					localizacaoPendente = false;
					break;
				}
			}
			if (localizacaoPendente) {
				Localizacao localizacao = new Localizacao();
				localizacao.setAtivo(Boolean.TRUE);
				localizacao.setEndereco(this.endereco);
				localizacao.setLocalizacao(LocalizacaoUtil.formataLocalizacaoPessoaFisicaEspecializada(advogado));

				this.getEntityManager().persist(localizacao);
				this.logGravar(localizacao);

				usuarioLocalizacao.setUsuario(advogado.getPessoa());
				usuarioLocalizacao.setLocalizacaoFisica(localizacao);
				usuarioLocalizacao.setResponsavelLocalizacao(Boolean.TRUE);
				usuarioLocalizacao.setPapel(ParametroUtil.instance().getPapelAdvogado());
				this.getEntityManager().persist(usuarioLocalizacao);
				this.logGravar(usuarioLocalizacao);

				pessoaLocalizacao.setLocalizacao(localizacao);
				pessoaLocalizacao.setPessoa(advogado.getPessoa());
				this.getEntityManager().persist(pessoaLocalizacao);
				this.logGravar(pessoaLocalizacao);
			}
			this.getEntityManager().flush();
			persist = "persisted";
			fm.add(Severity.INFO, "Advogado cadastrado com sucesso");
		}
		return persist;
	}

	public void pesquisarEscritorio() {
		if (Strings.isEmpty(this.cnpj)) {
			this.cnpj = "0";
		}
	}

	public void removeDados(String idUsuario) throws Exception {
		if (this.exists(idUsuario)) {
			this.getInstance().setAssinatura(null);
			this.getInstance().setCertChain(null);
			super.update();
		}
	}

	public void removeEscritorioAdvogado(UsuarioLocalizacao usuarioLocalizacao) {

		if (usuarioLocalizacao.getResponsavelLocalizacao()) {
			FacesMessages.instance().add(Severity.ERROR, "O advogado não pode ser desassociado do próprio escritório.");
			return;
		}

		PessoaAdvogadoHome pessoaAHome = PessoaAdvogadoHome.instance();
		pessoaAHome.getInstance().getUsuarioLocalizacaoList().remove(usuarioLocalizacao);

		getEntityManager().remove(usuarioLocalizacao);
		getEntityManager().flush();
		FacesMessages.instance().clear();
	}

	// Retorna a pessoa que já possuir o CPF
	@SuppressWarnings("unchecked")
	public PessoaServidor returnByCPF(String cpf, Integer idUsuario) {
		EntityManager em = this.getEntityManager();

		StringBuilder sqlPes = new StringBuilder();
		sqlPes.append("select o from PessoaServidor o ");
		sqlPes.append("inner join o.pessoaDocumentoIdentificacaoList p ");
		sqlPes.append("where p.tipoDocumento.codTipo = 'CPF' ");
		sqlPes.append("and p.numeroDocumento = :cpf");

		sqlPes.append("and o.idUsuario in ");
		sqlPes.append("(select n.usuario.idUsuario from UsuarioLocalizacao n ");
		sqlPes.append("where trim(n.papel.identificador) like 'administrador' ");
		sqlPes.append(" or trim(n.papel.identificador) like 'secretaria') ");

		sqlPes.append("and o.idUsuario not in ");
		sqlPes.append("(select l.idUsuario from PessoaOficialJustica l) ");

		if ((idUsuario != null) && (idUsuario != 0)) {
			sqlPes.append(" and o.idUsuario <> :idUsuario");
		}

		Query query = em.createQuery(sqlPes.toString());
		query.setParameter("cpf", cpf);
		if (idUsuario != null) {
			query.setParameter("idUsuario", idUsuario);
		}
		List<PessoaServidor> listAdvogado = query.getResultList();

		if (listAdvogado.size() > 0) {
			FacesMessages.instance().addToControl("numeroCPFCpf", StatusMessage.Severity.ERROR, "CPF já cadastrado!");
			this.limparCpf();
			this.cpfJaCadastrado = true;
			return listAdvogado.get(0);
		} else {
			this.cpfJaCadastrado = false;
			return null;
		}
	}

	public void setAdvogado(Boolean advogado) {
		this.advogado = advogado;
	}

	public void setCertChain(String certChain) {
		this.certChain = certChain;
	}

	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}

	public void setCpfJaCadastrado(boolean cpfJaCadastrado) {
		this.cpfJaCadastrado = cpfJaCadastrado;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public void setDocumentoPessoa(DocumentoPessoa documentoPessoa) {
		this.documentoPessoa = documentoPessoa;
	}

	public void setEmail1(String email1) {
		this.email1 = email1;
	}

	public void setEmail2(String email2) {
		this.email2 = email2;
	}

	public void setEmail3(String email3) {
		this.email3 = email3;
	}

	@Observer("cepChangedEvent")
	public void setEndereco(Cep cep) {
		this.endereco = null;
		if (cep == null) {
			Contexts.removeFromAllContexts("cepSuggest");
		} else {
			this.endereco = new Endereco();
			this.endereco.setCep(cep);
			if(cep.getMunicipio() != null) {
				this.endereco.setNomeEstado(cep.getMunicipio().getEstado().getEstado());
				this.endereco.setNomeCidade(cep.getMunicipio().getMunicipio());
			}
			this.endereco.setNomeLogradouro(cep.getNomeLogradouro());
			this.endereco.setNomeBairro(cep.getNomeBairro());
		}
	}

	public void setEstado(Estado estado) {
		if (this.estado != null) {
			if ((estado == null) || (!this.estado.getEstado().equals(estado.getEstado()))) {
				this.getPessoaAdvogadoMunicipioSuggest().setInstance(null);
			}
		}
		this.estado = estado;
	}

	public void setExibePopUpInconsistencia(boolean exibePopUpInconsistencia) {
		this.exibePopUpInconsistencia = exibePopUpInconsistencia;
	}

	public void setExisteInconsistencia(boolean existeInconsistencia) {
		this.existeInconsistencia = existeInconsistencia;
	}

	@Override
	public void setId(Object id) {
		boolean changed = (id != null) && !id.equals(this.getId());
		super.setId(id);
		List<Endereco> endList = null;
		if (this.getInstance() != null) {
			endList = this.getInstance().getEnderecoList();
		}
		if (this.isManaged() && (endList.size() > 0)) {
			this.getEnderecoHome().setId(endList.get(0).getIdEndereco());
		} else if ((changed && (endList.size() > 0)) || (id == null)) {
			this.getEnderecoHome().newInstance();
		}

		if (!Strings.isEmpty(this.getInstance().getEmail())) {
			String[] s = this.getInstance().getEmail().split("\\,");
			this.email1 = s[0];
			if (s.length > 1) {
				this.email2 = s[1];
			}
			if (s.length > 2) {
				this.email3 = s[2];
			}
		}

		if (changed) {
			if (this.getInstance().getMunicipioNascimento() != null) {
				this.estado = this.getInstance().getMunicipioNascimento().getEstado();
			}
			this.getPessoaAdvogadoMunicipioSuggest().setInstance(this.getInstance().getMunicipioNascimento());
		}
		if (id == null) {
			this.getPessoaAdvogadoMunicipioSuggest().setInstance(null);
			this.estado = null;
		}
		if ((this.getInstance() != null) && (this.oldCpf == null)) {
			this.oldCpf = this.getInstance().getNumeroCPF();
		}
		
		setarPossuiComprovanteCadastro();
		
		setUsuarioAtivoInicial(instance.getAtivo());
		setPerfilAtivoInicial(instance.getAdvogadoAtivo());
	}

	public void setListDadosAdv(List<DadosAdvogadoOAB> listDadosAdv) {
		this.listDadosAdv = listDadosAdv;
	}

	public void setLoginJaCadastrado(boolean loginJaCadastrado) {
		this.loginJaCadastrado = loginJaCadastrado;
	}

	public List<Pessoa> getPessoasRepresentadas() {
		if (pessoasRepresentadas == null) {
			List<ProcessoParte> participacoes = getParticipacoesProcesso();
			pessoasRepresentadas = new ArrayList<Pessoa>(participacoes.size());
			for (ProcessoParte pp : participacoes) {
				pessoasRepresentadas.add(pp.getPessoa());
			}
		}
		return pessoasRepresentadas;
	}

	public void setPessoaLogada(Pessoa pessoaLogada) {
		this.pessoaLogada = pessoaLogada;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaLogada(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoaLogada(PessoaFisicaEspecializada pessoa){
		setPessoaLogada(pessoa != null ? pessoa.getPessoa() : (Pessoa) null);
	}

	public void setRadioResetCertificado(Boolean radioResetCertificado) {
		this.radioResetCertificado = radioResetCertificado;
	}

	public void setTab() {
		super.setTab("Confirmacao");
	}

	public void setUltimoIdInserido(Integer ultimoIdInserido) {
		this.ultimoIdInserido = ultimoIdInserido;
	}

	public void setVeracidadeCadastro(boolean veracidadeCadastro) {
		this.veracidadeCadastro = veracidadeCadastro;
	}

	public boolean temPapelProcurador(PessoaAdvogado p) {
		return p.getPapelSet().contains(ParametroUtil.instance().getPapelProcurador());
	}

	@SuppressWarnings("unchecked")
	public Endereco findEndereco(Integer idUsuario) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from Endereco o ");
		sb.append("where o.usuario.idUsuario = :id");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("id", idUsuario);
		List<Endereco> lista = new ArrayList<Endereco>();
		lista.addAll(q.getResultList());
		if (lista.size() > 0) {
			return lista.get(0);
		} 
		else {
			return new Endereco();
		}
	}

	@Override
	public String update() {
		this.getInstance().setMunicipioNascimento(this.getPessoaAdvogadoMunicipioSuggest().getInstance());
		this.getInstance().setEmail(this.montarEmail());
		
		if (this.radioResetCertificado) {
			this.getInstance().setCertChain(null);
			this.getInstance().setAssinatura(null);
		}
		
		try {
			PessoaAdvogado advogado = this.getInstance();
			ComponentUtil.getComponent(PessoaAdvogadoManager.class).persistAndFlush(advogado);
			setInstance(advogado);
			updatedMessage();
			
			gravarEndereco();
			
			if(getInstance().getIncluirProcessoPush() == null) {
				getInstance().setIncluirProcessoPush(Boolean.FALSE);
			}
			
			if(getInstance().getIncluirProcessoPush()){
				incluirProcessosNoPusH();
			}
			advogado.getPessoa().setNome(advogado.getNome());
			
			atualizarEspecializacao(instance.getAdvogadoAtivo(), instance.getPessoa(), PessoaAdvogado.class);
			
			String msg = afterPersistOrUpdate("update");
			PessoaHome.instance().atualizarNomeLocalizacao(advogado.getPessoa());
			if (msg != null) {
				this.radioResetCertificado = Boolean.FALSE;
			}
			
			if (isCadastroAlterado()) {
				Authenticator.deslogar(instance.getPessoa(), "perfil.atualizar");
			}
			
			return msg;
		} catch (PJeBusinessException e) {
			reportMessage(e);
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "perfil.erro");			
		}
		
		return null;
	}
	
	private void gravarEndereco() throws PJeBusinessException {
		Endereco endereco = getEnderecoHome().getInstance();
		if (endereco.getIdEndereco() == 0) {
			endereco.setUsuario(getInstance().getPessoa());
			endereco.setDataAlteracao(new Date());
			endereco.setUsuarioCadastrador(Authenticator.getUsuarioLogado());
			
			ComponentUtil.getComponent(EnderecoManager.class).persistAndFlush(endereco);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void incluirProcessosNoPusH(){
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoPush o ");
		sb.append("where o.pessoa=:pessoa ");
		sb.append("and o.dtExclusao != null ");
		Query q = EntityUtil.createQuery(sb.toString());
		q.setParameter("pessoa", getInstance().getPessoa());
		List<ProcessoPush> ppList = q.getResultList();
		for (ProcessoPush processoPush : ppList) {
			processoPush.setDtExclusao(null);
			EntityUtil.getEntityManager().merge(processoPush);
		}
		
		EntityUtil.flush();
		
		sb = new StringBuilder();
		sb.append("select new br.jus.pje.nucleo.entidades.ProcessoPush(o.pessoa,o.processoTrf) ");
		sb.append("from ProcessoParte o ");
		sb.append("where o.pessoa=:pessoa ");
		sb.append("and o.processoTrf.processoStatus != 'E' ");
		sb.append("and not exists(select o1 ");
		sb.append("				  from ProcessoPush o1 ");
		sb.append("				  where o1.processoTrf=o.processoTrf ");
		sb.append("				  and o1.pessoa=o.pessoa ");
		sb.append("				  and o1.dtExclusao=null) ");
		sb.append("				  group by o.pessoa,o.processoTrf ");
		q = EntityUtil.createQuery(sb.toString());
		q.setParameter("pessoa", getInstance().getPessoa());
		List<ProcessoPush> processoPushList = q.getResultList();
		for (ProcessoPush processoPush : processoPushList) {
			processoPush.setDtInclusao(new Date());
			EntityUtil.getEntityManager().persist(processoPush);
		}
		
		EntityUtil.flush();
	}

	public boolean validaOAB() {
		boolean validouOAB = true;
		ConsultaClienteOAB consultaClienteOAB = new ConsultaClienteOAB();
		try {
			consultaClienteOAB.consultaDados(this.getInstance().getNumeroCPF(), false);

			String nome = "";
			String numOAB = "";
			if (consultaClienteOAB.getDadosAdvogadoList().size() > 0) {

				for (DadosAdvogadoOAB dadosOAB : consultaClienteOAB.getDadosAdvogadoList()) {
					if (dadosOAB.getUf().equalsIgnoreCase(this.getInstance().getUfOAB().getCodEstado())) {
						nome = dadosOAB.getNome();
						numOAB = dadosOAB.getNumInscricao().replaceAll(" ", "").replaceAll("-", "");
						numOAB = StringUtil.retiraZerosEsquerda(numOAB);
						break;
					}
				}

				if ((this.getInstance().getLetraOAB() != null)
						&& (this.getInstance().getLetraOAB().trim().length() > 0)) {
					String oabLetra = this.getInstance().getNumeroOAB() + this.getInstance().getLetraOAB();
					if (!numOAB.equals(oabLetra)) {
						this.inconsistenciasOabReceita.append("Número OAB");
						validouOAB = false;
					}
				} else if (!numOAB.equals(StringUtil.retiraZerosEsquerda(this.getInstance().getNumeroOAB()))) {
					this.inconsistenciasOabReceita.append("Número OAB");
					validouOAB = false;
				}
				if (!StringUtil.getUsAscii(nome).equalsIgnoreCase(StringUtil.getUsAscii(this.getInstance().getNome()))) {
					if (this.inconsistenciasOabReceita.length() == 0) {
						this.inconsistenciasOabReceita.append("Nome");
					} else {
						this.inconsistenciasOabReceita.append(", ").append("Nome");
					}
					this.verificarNome = false;
					validouOAB = false;
				}
			} else {
				this.verificarDadosCertificado = false;
				FacesMessages.instance().addToControl("numeroCPFCpf", StatusMessage.Severity.ERROR,
						"Não foi encontrado nenhum registro na OAB para o CPF informado.");
				this.limparCpf();
			}
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.WARN, e.getMessage());
			log.warn("Erro no webservice OAB: " + e.getMessage(), e);
			this.inconsistenciasOabReceita.append("CPF");
			this.verificarCPF = false;
			return false;
		}
		return validouOAB;
	}

	public void validarCadastro() {
		if (!this.isEnderecoValido()) {
			return;
		}
		this.verificarDadosCertificado = true;
		this.exibePopUpInconsistencia = false;
		if (!this.veracidadeCadastro) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"O campo que confirma veracidade das informações é obrigatório.");
			return;
		} else if (Strings.isEmpty(this.getInstance().getAssinatura())) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "É obrigado assinar o cadastro do advogado.");
			return;
		}
		try {
			VerificaCertificadoPessoa.verificaCertificadoPessoa(this.getInstance().getCertChain(), this.getInstance().getPessoa());
		} catch (CertificadoException e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"Erro de validação do certificado: " + e.getMessage());
			return;
		}
		if (this.validaOAB() && this.validaReceita()) {
			ModeloDocumento md = ParametroUtil.instance().getModeloComprovanteCadastroAdvogado();
			PessoaAdvogado advogado = this.getInstance();
			String documentoHtml = md.getModeloDocumento();
			documentoHtml = ProcessoDocumentoHome.processarModelo(documentoHtml);
			String msgPersist = this.persist();
			if (Strings.isEmpty(msgPersist)) {
				return;
			}
			DocumentoPessoaHome documentoPessoaHome = DocumentoPessoaHome.instance();
			documentoPessoaHome.newInstance();
			DocumentoPessoa docPessoa = documentoPessoaHome.getInstance();
			docPessoa.setDocumentoHtml(documentoHtml);
			docPessoa.setAtivo(Boolean.TRUE);
			docPessoa.setDataInclusao(new Date());
			docPessoa.setPessoa(advogado.getPessoa());
			docPessoa.setUsuarioCadastro(advogado.getPessoa());
			TipoProcessoDocumento termoCompromisso = ParametroUtil.instance()
					.getTipoProcessoDocumentoTermoCompromisso();
			docPessoa.setTipoProcessoDocumento(termoCompromisso);
			documentoPessoaHome.persist();

			this.confirmarCadastro();

			Redirect.instance().setViewId("/PessoaAdvogado/termoCompromissoHTML.seam");
			Redirect.instance().setParameter("id", docPessoa.getIdDocumentoPessoa());
			Redirect.instance().execute();

			this.existeInconsistencia = false;

		} else if (this.verificarDadosCertificado) {
			this.existeInconsistencia = true;
		}
	}

	public void validarComInconsistencias() {
		ModeloDocumento md = ParametroUtil.instance().getModeloDocumentoInconsistencia();
		PessoaAdvogado advogado = this.getInstance();
		String documentoHtml = md.getModeloDocumento();
		documentoHtml = ProcessoDocumentoHome.processarModelo(documentoHtml);
		DocumentoPessoaHome documentoPessoaHome = DocumentoPessoaHome.instance();
		String msgPersist = this.persist();
		if (Strings.isEmpty(msgPersist)) {
			return;
		}
		documentoPessoaHome.newInstance();
		DocumentoPessoa docPessoa = documentoPessoaHome.getInstance();
		docPessoa.setDocumentoHtml(documentoHtml);
		docPessoa.setAtivo(Boolean.TRUE);
		docPessoa.setDataInclusao(new Date());
		docPessoa.setPessoa(advogado.getPessoa());
		docPessoa.setUsuarioCadastro(advogado.getPessoa());
		docPessoa.setTipoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoInconsistencia());
		documentoPessoaHome.persist();
		this.documentoPessoa = docPessoa;
		this.exibePopUpInconsistencia = true;
		this.existeInconsistencia = false;
		FacesMessages fc = FacesMessages.instance();
		fc.clear();
		fc.add(Severity.INFO, "Cadastro realizado com sucesso. O Advogado deverá se dirigir a Sessão / Subseção"
				+ " Judiciária mais próxima para sanar inconsistências no cadastro.");
	}

	public boolean validaReceita() {
		boolean validou = true;
		DadosReceitaPessoaFisica receitaPessoaFisica = null;
		
		if (!this.verificarCPF)
			return false;
		
		try {

			receitaPessoaFisica = (DadosReceitaPessoaFisica) ConsultaClienteWebService.instance().consultaDados(
					TipoPessoaEnum.F, this.getInstance().getNumeroCPF(), false);

			if (this.verificarNome) {
				if (!StringUtil.getUsAscii(receitaPessoaFisica.getNome()).equalsIgnoreCase(
						StringUtil.getUsAscii(this.getInstance().getNome()))) {
					if (this.inconsistenciasOabReceita.length() == 0) {
						this.inconsistenciasOabReceita.append("Nome");
					} else {
						this.inconsistenciasOabReceita.append(", ").append("Nome");
					}
					validou = false;
				}
			}
			if (!receitaPessoaFisica.getDataNascimento().equals(this.getInstance().getDataNascimento())) {
				if (this.inconsistenciasOabReceita.length() == 0) {
					this.inconsistenciasOabReceita.append("Data de Nascimento");
				} else {
					this.inconsistenciasOabReceita.append(", ").append("Data de Nascimento");
				}
				validou = false;
			}
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.WARN, e.getMessage());
			log.warn("Erro no webservice receita: " + e.getMessage(), e);

			if (this.inconsistenciasOabReceita.length() == 0)
				this.inconsistenciasOabReceita.append("CPF");
			else
				this.inconsistenciasOabReceita.append(", ").append("CPF");
			
			return false;			
		}
		return validou;
	}

	public void verificarTipoPessoa() {
		Context session = Contexts.getSessionContext();
		this.setPessoaLogada((Pessoa) session.get("usuarioLogado"));
		if (Pessoa.instanceOf(pessoaLogada, PessoaAdvogado.class)) {
			this.setId(this.pessoaLogada.getIdUsuario());
			this.setAdvogado(Boolean.TRUE);
		}
	}
	
	public PessoaAdvogado obterPessoaAdvogado(Pessoa pessoa) {
		try {
			return ComponentUtil.getComponent(PessoaAdvogadoManager.class).findById(pessoa.getIdUsuario());
		} catch (PJeBusinessException e) {
			reportMessage(e);
		}
		
		return null;
	}
	
	public List<String> getParticipacoesProcessoString(){
		List<String> lista = new ArrayList<String>(0);
		
		for(ProcessoParte p: getParticipacoesProcesso()){
			lista.add(p.getPessoa().getIdUsuario().toString() + "_"+p.getProcessoTrf().getIdProcessoTrf());
		}
		return lista;
	}

	public List<ProcessoParte> getParticipacoesProcesso() {
		if (participacoesProcesso == null) {
			carregarParticipacoesProcessos();
		}
		return participacoesProcesso;
	}

	public Integer getQtdParticipacoesProcesso() {
		if (participacoesProcesso.size() == 0)
			return -1;
		else
			return null;
	}

	public void setParticipacoesProcesso(List<ProcessoParte> participacoesProcesso) {
		this.participacoesProcesso = participacoesProcesso;
	}

	public String removeZerosOab(String numeroOab) {
		return StringUtil.retiraZerosEsquerda(numeroOab);
	}

	public String removeCertificado() {
		getInstance().setCertChain(null);
		getInstance().setAssinatura(null);
		return update();
	}
	
	public Boolean getOcorreuErroWsReceita(){
		return ocorreuErroWsReceita;
	}

	public void setOcorreuErroWsReceita(Boolean ocorreuErroWsReceita){
		this.ocorreuErroWsReceita = ocorreuErroWsReceita;
	}
	
	public void continuarCadastroReceita(){
		ocorreuErroWsReceita = false;
	}
	
	public void continuarCadastroOAB(){
		ocorreuErroWsOAB = false;
	}

	public Boolean getOcorreuErroWsOAB() {
		return ocorreuErroWsOAB;
	}

	public void setOcorreuErroWsOAB(Boolean ocorreuErroWsOAB) {
		this.ocorreuErroWsOAB = ocorreuErroWsOAB;
	}

	public String getErroOAB() {
		return erroOAB;
	}

	public void setErroOAB(String erroOAB) {
		this.erroOAB = erroOAB;
	}
	
	public Boolean isPossuiComprovanteCadastro(){
		return possuiComprovanteCadastro;
	}
	
	public void setPossuiComprovanteCadastro(Boolean possuiComprovanteCadastro) {
		this.possuiComprovanteCadastro = possuiComprovanteCadastro;
	}
	
	public DocumentoPessoa getTermoConfirmacaoCadastro() {
		return termoConfirmacaoCadastro;
	}
	
	public void setTermoConfirmacaoCadastro(
			DocumentoPessoa termoConfirmacaoCadastro) {
		this.termoConfirmacaoCadastro = termoConfirmacaoCadastro;
	}
	
	public List<ParAssinatura> getAssinaturas(){
		if(assinaturas == null && getTermoConfirmacaoCadastro() != null){
			String contents = null;
			
			try {
				contents = new String(SigningUtilities.base64Encode(getTermoConfirmacaoCadastro()
										.getDocumentoHtml().getBytes()));
			} catch (IOException e) {
				reportMessage(e);
			}
			
			ParAssinatura assinatura = new ParAssinatura();
			assinatura.setConteudo(contents);
			assinaturas = new ArrayList<ParAssinatura>(0);
			assinaturas.add(assinatura);
		}

		return assinaturas;
	}
	
	public String getEncodedCertChain(){
		return certChain;
	}

	public void setEncodedCertChain(String certChain){
		this.certChain = certChain;
	}

	public static PessoaAdvogadoHome instance() {
		return ComponentUtil.getComponent(PessoaAdvogadoHome.class);
	}
	
	
	public PessoaAdvogadoTipoInscricaoEnum getTipoInscricao() {
		this.tipoInscricao = instance.getTipoInscricao();
		return this.tipoInscricao;
	}
	
	public void setTipoInscricao(PessoaAdvogadoTipoInscricaoEnum tipoInscricao) {
		this.tipoInscricao = tipoInscricao;
		
		if(tipoInscricao != null){
			getInstance().setTipoInscricao(tipoInscricao);
		}
	}
	
	public PessoaAdvogadoTipoInscricaoEnum[] getPessoaAdvogadoTipoInscricaoEnumValues(){
		return PessoaAdvogadoTipoInscricaoEnum.values();
	}	
	
	/**
	 * metodo responsavel por verificar se o cep foi preenchido incorretamente (nulo ou em branco).
	 * controla a edicao de campos na tela.
	 * @return true se endereco.cep for nulo ou se o campo numeroCEP for nulo ou vazio.
	 */
	public boolean isCepNulo() {
		return Util.isEnderecoCepNulo(this.endereco);
	}
	
	public boolean isAdvogado(Integer id){
		EntityManager em = getEntityManager();
		
		PessoaAdvogado pa = em.find(PessoaAdvogado.class, id);
		
		return (pa != null);
	}
	
	public void limparCamposBrasileiro() {
		if (instance.getBrasileiro() == true) {
			instance.setNumeroPassaporte(null);
		} else {
			setEstado(null);
		}
	}
	
	/**
	 * Método que verifica se já existe um advogado com o CPF especificado
	 * @since 1.4.6
	 * @author Rafael Barros da Costa
	 * @category PJE-JT
	 * @return true / false
	 * ISSUE [PJEII-3674]
	 * Criado em 31/10/2012 
	 */
	@SuppressWarnings("unchecked")
	public boolean isCpfAdvogado(String cpf){
		
		EntityManager em = this.getEntityManager();		

		StringBuilder querySQL = new StringBuilder();
		querySQL.append("select o from PessoaDocumentoIdentificacao o, PessoaAdvogado a ");
		querySQL.append(" where o.pessoa.idUsuario = a.idUsuario ");
		querySQL.append("and o.tipoDocumento.codTipo = 'CPF' ");
		querySQL.append("and o.numeroDocumento = :cpf");
		
		Query query = em.createQuery(querySQL.toString());
		query.setParameter("cpf", cpf);		
		List<PessoaDocumentoIdentificacao> listaDocumentoId = query.getResultList(); //Lista de documentos CPF encontrados relacionados a advogados com o cpf informado
		
		if (listaDocumentoId.size() >  0){
			return true; //existe um CPF associado a advogado
		}
		else{
			return false; //Não existe um CPF associado a advogado
		}		
	}
	
	/**
	 * Este método reativa o advogado.
	 * @param pessoaAdvogado
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String ativarAdvogado(PessoaAdvogado pessoaAdvogado){
		PessoaService pessoaService = ComponentUtil.getComponent(PessoaService.class);
		try {
			PessoaFisica pessoaFisica = (PessoaFisica)pessoaService.especializa(pessoaAdvogado.getPessoa(), PessoaAdvogado.class);
			pessoaAdvogado = pessoaFisica.getPessoaAdvogado();
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Ocorreu um erro ao tentar ativar o advogado");
			e.printStackTrace();
		}

		return "update";
	}	
	
	@Override
	@SuppressWarnings("unchecked")
	public String inactive(PessoaAdvogado instance) {		
		PessoaService pessoaService = ComponentUtil.getComponent(PessoaService.class);
		try {
			PessoaFisica pessoaFisica = (PessoaFisica) pessoaService.desespecializa(instance.getPessoa(), PessoaAdvogado.class);
			instance = pessoaFisica.getPessoaAdvogado();
			instance.setAdvogadoAtivo(Boolean.TRUE);
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Ocorreu um erro ao tentar inativar o advogado.");
			e.printStackTrace();
		}

		FacesMessages.instance().add(StatusMessage.Severity.INFO, getInactiveSuccess());
		return "update";
	}
	
	public void criaESetaNovoAdvogado(Integer id) {
		instance = new PessoaAdvogado();
		super.setPessoaAdvogadoIdPessoaAdvogado(id);
		setTab();
	}
	
	public void gerarNovaSenha(){
		getInstance().setHashAtivacaoSenha(PjeUtil.instance().gerarHashAtivacao(getInstance().getLogin()));
		if(getInstance().getIdUsuario() == null){
			persist();
		}else{
			update();
		}
		
		try {
			ComponentUtil.getComponent(UsuarioService.class).enviarEmailSenha(getInstance());
			reportMessage("pje.pessoaFisicaHome.info.emailEnviadoComSucesso", null, getInstance().getEmail());
		} catch (PJeBusinessException e) {
			reportMessage(e);
		}
	}
	
    private void setarPossuiComprovanteCadastro(){
		TipoProcessoDocumento tipoModeloComprovante = ParametroUtil.instance().getTipoProcessoDocumentoTermoCompromisso();
    	if(tipoModeloComprovante != null){
	    	if(getInstance().getDocumentoPessoaList() != null){
	    		for(DocumentoPessoa dp : getInstance().getDocumentoPessoaList()){
	    			if(dp.getTipoProcessoDocumento().getIdTipoProcessoDocumento() == tipoModeloComprovante.getIdTipoProcessoDocumento() && 
	    					StringUtils.isNotEmpty(dp.getAssinatura())){
	    				
	    				possuiComprovanteCadastro = true;
	    				return;
	    			}
	    		}
	    	}
    	}
    	
    	possuiComprovanteCadastro = false;
    }
    
	public void preparaTermoConfirmacaoCadastro(){
		if(!isPossuiComprovanteCadastro()){
			if(getInstance().getLogin() != null && getInstance().getDataNascimento() != null
					&& getInstance().getNumeroOAB() != null	&& !StringUtils.isEmpty(getInstance().getNumeroOAB())){

				ModeloDocumento md = ParametroUtil.instance().getModeloComprovanteCadastroAdvogado();
				String documentoHtml = md.getModeloDocumento();			
				
				Contexts.getConversationContext().set("pje:cadastro:inscricaoMF", getInstance().getLogin());
				Contexts.getConversationContext().set("pje:cadastro:dataNascimento", getInstance().getDataNascimento());
				
				documentoHtml = ProcessoDocumentoHome.processarModelo(documentoHtml);
	
				termoConfirmacaoCadastro = new DocumentoPessoa();
				termoConfirmacaoCadastro.setPessoa(getInstance());
				termoConfirmacaoCadastro.setDocumentoHtml(documentoHtml);
				termoConfirmacaoCadastro.setAtivo(true);
				termoConfirmacaoCadastro.setDataInclusao(new Date());
				termoConfirmacaoCadastro.setUsuarioCadastro(Authenticator.getPessoaLogada());
				TipoProcessoDocumento termoCompromisso = ParametroUtil.instance().getTipoProcessoDocumentoTermoCompromisso();
				termoConfirmacaoCadastro.setTipoProcessoDocumento(termoCompromisso);
			}else{
				FacesMessages.instance().add(Severity.ERROR,
						"Informe o CPF, a data de nascimento, número da OAB e salve os dados antes de anexar o termo.");
			}
		}else{
			FacesMessages.instance().add(Severity.ERROR,
					"Advogado já possui termo de confirmação de cadastro.");
		}
	}
	
	public void finalizarMultiplos(){
		if(assinaturas != null){
			getTermoConfirmacaoCadastro().setAssinatura(assinaturas.get(0).getAssinatura());
			getTermoConfirmacaoCadastro().setCertChain(getEncodedCertChain());			
			try {
				ComponentUtil.getComponent(DocumentoPessoaManager.class).persistAndFlush(getTermoConfirmacaoCadastro());				
				setInstance(ComponentUtil.getComponent(PessoaAdvogadoManager.class).getPessoaAdvogado(getInstance().getIdUsuario()));
				refreshGrid("documentoPessoaGrid");
				possuiComprovanteCadastro = true;
				FacesMessages.instance().add(Severity.INFO, "Termo assinado com sucesso!");				
			} catch (PJeBusinessException e) {
				reportMessage(e);
			}
		}else{
			FacesMessages.instance().add(Severity.ERROR, "Nao foi possivel asinar termo. Assinaturas ainda nao preparadas.");
		}
	}
	
	public void finalizarPJeOfficeAssinador(){
		termoConfirmacaoCadastro.setAssinatura(getSignature());
		termoConfirmacaoCadastro.setCertChain(getCertChain());
		try {
			ComponentUtil.getComponent(DocumentoPessoaManager.class).persistAndFlush(getTermoConfirmacaoCadastro());			
			setInstance(ComponentUtil.getComponent(PessoaAdvogadoManager.class).getPessoaAdvogado(getInstance().getIdUsuario()));
			refreshGrid("documentoPessoaGrid");
			possuiComprovanteCadastro = true;
			FacesMessages.instance().add(Severity.INFO, "Termo assinado com sucesso!");				
		} catch (PJeBusinessException e) {
			reportMessage(e);
		}		

	}
	
	public class ParAssinatura{
		private String conteudo;
		private String assinatura;
		public String getConteudo() {
			return conteudo;
		}
		public void setConteudo(String conteudo) {
			this.conteudo = conteudo;
		}
		public String getAssinatura() {
			return assinatura;
		}
		public void setAssinatura(String assinatura) {
			this.assinatura = assinatura;
		}
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash)
			throws Exception {
		setCertChain(arquivoAssinadoHash.getCadeiaCertificado());
		setSignature(arquivoAssinadoHash.getAssinatura());
	}

	public String getUrlDocsField() {
		if(getTermoConfirmacaoCadastro() != null){
			return ComponentUtil.getComponent(DocumentoJudicialService.class).getDownloadLink(getTermoConfirmacaoCadastro().getDocumentoHtml());
		} else{
			return null;
		}
	}	
	
	@Override
	public String getActionName() {
		return NAME;
	}
	
	public String getSignature() {
		return signature;
	}
	
	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	public void setEmail(String email){
		this.email1 = email;
	}
	public String getEmail(){
		return email1;
	}

}
