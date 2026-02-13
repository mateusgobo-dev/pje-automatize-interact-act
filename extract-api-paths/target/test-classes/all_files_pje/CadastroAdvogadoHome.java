/* $Id: CadastroAdvogadoHome.java 10978 2010-08-19 13:48:05Z danielsilva $ */

package br.com.infox.cliente.home;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.component.suggest.CadastroAdvogadoMunicipioSuggestBean;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.certificado.Certificado;
import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.core.certificado.CertificadoLog;
import br.com.infox.core.certificado.DadosCertificado;
import br.com.infox.core.certificado.VerificaCertificado;
import br.com.infox.core.certificado.util.VerificaCertificadoPessoa;
import br.com.infox.ibpm.entity.log.LogUtil;
import br.com.infox.ibpm.home.EnderecoHome;
import br.com.infox.trf.webservice.ConsultaClienteOAB;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.LocalizacaoUtil;
import br.jus.cnj.pje.business.dao.EstadoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.PessoaAdvogadoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaDocumentoIdentificacaoManager;
import br.jus.cnj.pje.nucleo.manager.TipoDocumentoIdentificacaoManager;
import br.jus.cnj.pje.nucleo.service.CepService;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.webservice.client.ConsultaClienteReceitaPFCNJ;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.DocumentoPessoa;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Escolaridade;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaLocalizacao;
import br.jus.pje.nucleo.entidades.Profissao;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.enums.PessoaAdvogadoTipoInscricaoEnum;
import br.jus.pje.nucleo.enums.SexoEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.ws.externo.cna.entidades.DadosAdvogadoOAB;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaFisica;

@Name(CadastroAdvogadoHome.NAME)
@BypassInterceptors
public class CadastroAdvogadoHome extends AbstractPessoaAdvogadoHome<PessoaAdvogado> {

	public static final String NAME = "cadastroAdvogadoHome";
	private static final LogProvider log = Logging.getLogProvider(CadastroAdvogadoHome.class);
    private static final boolean VALIDACAO_OAB = true;//false;
    
	private static final long serialVersionUID = 1L;

	private Integer ultimoIdInserido;
	private Date dataInicio;
	private Date dataFim;
	private Endereco endereco = new Endereco();
	private boolean existeInconsistencia = false;
	private boolean veracidadeCadastro = false;
	private boolean exibePopUpInconsistencia = false;
	private StringBuffer inconsistenciasOab;
	private StringBuffer inconsistenciasReceita;
	private StringBuffer inconsistenciasCertificado;
	private boolean verificarNome = true;
	private boolean verificarCPF = true;
	private boolean verificarDadosCertificado = true;
	private DocumentoPessoa documentoPessoa;
	private String email1;
	private String email2;
	private String email3;
	private String certChain;
	private Estado estado;
	private String termoLido;
	private String certChainStringLog;
	private List<DadosAdvogadoOAB> dadosOAB = new ArrayList<DadosAdvogadoOAB>(0);
	private PessoaAdvogadoTipoInscricaoEnum tipoInscricao;
	private boolean dadosCarregadoReceitaOAB;
	
	@Logger
	private Log logger;
	
	@In
	private PessoaService pessoaService;
	
	@In
	private PessoaDocumentoIdentificacaoManager pessoaDocumentoIdentificacaoManager;
	
	@In
	private PessoaAdvogadoManager pessoaAdvogadoManager;
	
	
	public static CadastroAdvogadoHome instance() {
		return ComponentUtil.getComponent(CadastroAdvogadoHome.NAME);
	}

	private CadastroAdvogadoMunicipioSuggestBean getCadastroAdvogadoMunicipioSuggest() {
		return (CadastroAdvogadoMunicipioSuggestBean) Component.getInstance("cadastroAdvogadoMunicipioSuggest");
	}

	private EnderecoHome getEnderecoHome() {
		return getComponent("enderecoHome");
	}

	@Override
	public void newInstance() {
		Contexts.removeFromAllContexts("cepSuggest");
		Contexts.removeFromAllContexts("cadastroAdvogadoMunicipioSuggest");
		this.email1 = "";
		this.email2 = "";
		this.email3 = "";

		super.newInstance();

		Profissao profissao = ParametroUtil.instance().getProfissaoAdvogado();
		Escolaridade escolaridade = ParametroUtil.instance().getEscolaridadeEnsinoSuperior();
		TipoPessoa tipoAdvogado = ParametroUtil.instance().getTipoAdvogado();
		if (tipoAdvogado == null || profissao == null || escolaridade == null) {
			return;
		}
		getInstance().setTipoPessoa(tipoAdvogado);
		getInstance().setValidado(false);
		getInstance().setProfissao(profissao);
		getInstance().setEscolaridade(escolaridade);
		endereco = new Endereco();
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		getInstance().setMunicipioNascimento(getCadastroAdvogadoMunicipioSuggest().getInstance());
		getInstance().setEmail(montarEmail());
		return true;
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

    /*

     * PJE-JT: Ricardo Scholz : PJE-1223 - 2012-01-20 Alteracoes feitas pela JT.
     * Refactoring do método. Modificamos dos nomes de algumas variáveis para
     * facilitar leitura. Modificação da lógica de validação para número da OAB.
     * Nova lógica:
     * 1) Checa se o advogado possui registro no UF informado;
     * 2) Recupera código da OAB e remove zeros à esquerda e caracteres não 
     * alfa-numéricos;
     * 3) Recupera número da interface, remove caracteres não numéricos e concatena
     * com a letra também recuperada da interface, se houver;
     * 4) Realiza a comparação do código da OAB (item 1) com os dados inseridos na
     * interface (item 2), ignorando diferenças de maiúsculo/minúsculo (ignore case).
     */
	public boolean validaOAB() {

		if (!CadastroAdvogadoHome.VALIDACAO_OAB)
			return true;

		boolean validouOAB = true;

		ConsultaClienteOAB consultaClienteOAB = new ConsultaClienteOAB();

		try {

			consultaClienteOAB.consultaDados(getInstance().getNumeroCPF(), false);

			String nomeOAB = "";
			String codOAB = "";

			if (consultaClienteOAB.getDadosAdvogadoList().size() > 0) {
				boolean ufEncontrado = false;

				for (DadosAdvogadoOAB dadosOAB : consultaClienteOAB.getDadosAdvogadoList()) {

					if (dadosOAB.getUf().equalsIgnoreCase(getInstance().getUfOAB().getCodEstado())) {
						nomeOAB = dadosOAB.getNome();
						codOAB = StringUtil.removeNaoAlphaNumericos(dadosOAB.getNumInscricao());
						codOAB = StringUtil.retiraZerosEsquerda(codOAB);
						ufEncontrado = true;
						break;
					}
				}

				if (!ufEncontrado) {
					if (inconsistenciasOab.length() > 0) {
						inconsistenciasOab.append(", ");
					}

					inconsistenciasOab.append("UF OAB");
					validouOAB = false;

				} else {

					String codOABSistema = StringUtil.removeNaoNumericos(getInstance().getNumeroOAB());
					codOABSistema = StringUtil.retiraZerosEsquerda(codOABSistema);

					if (getInstance().getLetraOAB() != null && getInstance().getLetraOAB().trim().length() > 0) {
						codOABSistema = codOABSistema + getInstance().getLetraOAB();
					}

					if (!codOAB.equalsIgnoreCase(codOABSistema)) {
						if (inconsistenciasOab.length() > 0) {
							inconsistenciasOab.append(", ");
						}

						inconsistenciasOab.append("Número OAB");
						validouOAB = false;
					}

					if (!StringUtil.getUsAscii(nomeOAB).equalsIgnoreCase(StringUtil.getUsAscii(getInstance().getNome()))) {
						if (inconsistenciasOab.length() > 0) {
							inconsistenciasOab.append(", ");
						}

						inconsistenciasOab.append("Nome");
						this.verificarNome = false;
						validouOAB = false;
					}
				}
			} else {
				inconsistenciasOab.append("Não foi encontrado nenhum registro para o CPF informado");
				validouOAB = false;
			}

		} catch (Exception e) {
			FacesMessages.instance().add(Severity.WARN, e.getMessage());
			log.warn("Erro no webservice OAB: " + e.getMessage(), e);
			inconsistenciasOab.append("Sem conexão com a OAB");
			verificarCPF = false;

			return false;
		}

		return validouOAB;
	}

	/*
	 * PJE-JT: Fim.
	 */

	public boolean validaReceita() {
		boolean validou = true;
		ConsultaClienteReceitaPFCNJ clienteReceitaPF = new ConsultaClienteReceitaPFCNJ();
		DadosReceitaPessoaFisica receitaPessoaFisica = null;

		if (!verificarCPF) {
			return false;
		}
		
		try {
			receitaPessoaFisica = clienteReceitaPF.consultaDadosSemLogin(getInstance().getNumeroCPF(), false);

			/*
			 * if (this.verificarNome) { if
			 * (!StringUtil.getUsAscii(receitaPessoaFisica
			 * .getNome()).equalsIgnoreCase(
			 * StringUtil.getUsAscii(getInstance().getNome()))){ if
			 * (inconsistenciasReceita.length() > 0) {
			 * inconsistenciasReceita.append(", "); }
			 * inconsistenciasReceita.append("Nome"); validou = false; } }
			 */
			if (!receitaPessoaFisica.getDataNascimento().equals(getInstance().getDataNascimento())) {
				if (inconsistenciasReceita.length() > 0) {
					inconsistenciasReceita.append(", ");
				}
				inconsistenciasReceita.append("Data de Nascimento");
				validou = false;
			}
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.WARN, e.getMessage());
			log.warn("Erro no webservice receita: " + e.getMessage(), e);

			if (inconsistenciasReceita.length() > 0) {
				inconsistenciasReceita.append(", ");
			}
			inconsistenciasReceita.append("Sem conexão com a Receita Federal");
			return false;
		}
		return validou;
	}

	public boolean validaCertificado() {
		boolean validou = true;

		try {
			DadosCertificado dadosCertificado = DadosCertificado.parse(new Certificado(getInstance().getCertChain()));

			String dataNascimento = dadosCertificado.getValor(DadosCertificado.DATA_NASCIMENTO);

			SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");

			if (!getInstance().getDataNascimento().equals(format.parse(dataNascimento))) {
				if (inconsistenciasCertificado.length() > 0) {
					inconsistenciasCertificado.append(", ");
				}
				inconsistenciasCertificado.append("Data de Nascimento");
				validou = false;
			}

		} catch (Exception e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"Erro de validação do certificado: " + e.getMessage());
			return false;
		}

		return validou;
	}
	
	private void validarCadastroTeste(){
		if (!veracidadeCadastro) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"O campo que confirma veracidade das informações é obrigatório.");
			return;
		}
		ModeloDocumento md = ParametroUtil.instance().getModeloComprovanteCadastroAdvogado();
		PessoaAdvogado advogado = getInstance();
		String documentoHtml = md.getModeloDocumento();
		documentoHtml = ProcessoDocumentoHome.processarModelo(documentoHtml);
		getInstance().setValidado(Boolean.TRUE);
		getInstance().setAtivo(Boolean.TRUE);
		getInstance().setDataValidacao(new Date());

		String msgPersist = persist();
		if (Strings.isEmpty(msgPersist)) {
			return;
		}
		// System.out.println(getEndereco());arg1
		DocumentoPessoa docPessoa = gerarTermodeCompromisso(advogado, documentoHtml);
		confirmarCadastro();
		Redirect.instance().setViewId("/PessoaAdvogado/termoCompromissoHTML.seam");
		Redirect.instance().setParameter("id", docPessoa.getIdDocumentoPessoa());
		Redirect.instance().execute();
		this.existeInconsistencia=false;
	}

	public void validarCadastro() {
		DadosAdvogadoOAB oab = null;
		
		for(DadosAdvogadoOAB dado : dadosOAB) {
			if(dado.getOabSelecionado() != null && dado.getOabSelecionado() == true) {
				oab = dado;
				break;
			}
		}
		
		if(oab == null) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"É obrigatório a seleção de uma inscrição na OAB.");
			return;
		}
		
		String uf = oab.getUf();
		EstadoDAO estadoDao = ComponentUtil.getComponent("estadoDAO");
		List<Estado> estadoOAB = estadoDao.findByUf(uf);
		
		PessoaAdvogadoTipoInscricaoEnum tipoInscricao = null;
		
		if(oab.getTipoInscricao().equals("ADVOGADO")) {
			tipoInscricao = PessoaAdvogadoTipoInscricaoEnum.A;
		} else if(oab.getTipoInscricao().equals("SUPLEMENTAR")) {
			tipoInscricao = PessoaAdvogadoTipoInscricaoEnum.S;
		} else if(oab.getTipoInscricao().equals("ESTAGIARIO")) {
			tipoInscricao = PessoaAdvogadoTipoInscricaoEnum.E;
		}
		
		getInstance().setUfOAB(estadoOAB.get(0));
		getInstance().setNumeroOAB(oab.getNumInscricao());
		getInstance().setTipoInscricao(tipoInscricao);
		getInstance().setDataExpedicaoOAB(oab.getDataCadastro());
		getInstance().setLetraOAB(oab.getLetra());
		
		if(VerificaCertificado.instance().isModoTesteCertificado()){
			validarCadastroTeste();
			return;
		}
		if (!isEnderecoValido()) {
			return;
		}
		existeInconsistencia = false;
		verificarDadosCertificado = true;
		exibePopUpInconsistencia = false;
		if (!veracidadeCadastro) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"O campo que confirma veracidade das informações é obrigatório.");
			return;
		}

		// TODO - CNJ - Descomentar linhas abaixo para produção
		if (Strings.isEmpty(getInstance().getAssinatura())) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "É obrigado assinar o cadastro do advogado.");
			return;
		}

		// TODO - CNJ - Descomentar linhas abaixo para produção

		try {
			VerificaCertificadoPessoa.verificaCertificadoPessoa(getInstance().getCertChain(), getInstance().getPessoa());
		} catch (CertificadoException e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"Erro de validação do certificado: " + e.getMessage());
			return;
		}

		inconsistenciasOab = new StringBuffer();
		inconsistenciasReceita = new StringBuffer();
		inconsistenciasCertificado = new StringBuffer();
		// TODO - CNJ - Modificar linhas abaixo para produção
		if (validaOabReceitaCertificado()) {
			// if(validaReceita()){
			// if(true){

			try {

				ModeloDocumento md = ParametroUtil.instance().getModeloComprovanteCadastroAdvogado();				
				String documentoHtml = md.getModeloDocumento();
				documentoHtml = ProcessoDocumentoHome.processarModelo(documentoHtml);

				getInstance().setValidado(Boolean.TRUE);
				getInstance().setAtivo(Boolean.TRUE);
				getInstance().setDataValidacao(new Date());

				String msgPersist = persist();
				if (Strings.isEmpty(msgPersist)) {
					return;
				}
				// System.out.println(getEndereco());arg1
				PessoaAdvogado advogado = getInstance();
				DocumentoPessoa docPessoa = gerarTermodeCompromisso(advogado, documentoHtml);

				// confirmarCadastro();

				Redirect.instance().setViewId("/PessoaAdvogado/termoCompromissoHTML.seam");
				Redirect.instance().setParameter("id", docPessoa.getIdDocumentoPessoa());
				Redirect.instance().execute();

				this.existeInconsistencia = false;
			} catch (Exception e) {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR,
						"Erro ao cadastrar advogado: " + e.getMessage());
			}

		} else {
			if (verificarDadosCertificado)
				this.existeInconsistencia = true;
		}
	}

	private boolean validaOabReceitaCertificado() {
		// Garantir que os tres serão executados
		// PJEII-3648 - Não há mais validações
//		boolean isValidoOab = validaOAB();
//		boolean isValidoReceita = validaReceita();
//		boolean isValidoCertificado = validaCertificado();
//		return isValidoOab && isValidoReceita && isValidoCertificado;
		return true;
	}

	public DocumentoPessoa gerarTermodeCompromisso(PessoaAdvogado advogado, String documentoHtml) {
		DocumentoPessoaHome documentoPessoaHome = DocumentoPessoaHome.instance();
		documentoPessoaHome.newInstance();
		DocumentoPessoa docPessoa = documentoPessoaHome.getInstance();
		docPessoa.setDocumentoHtml(documentoHtml);
		docPessoa.setAtivo(Boolean.TRUE);
		docPessoa.setDataInclusao(new Date());
		docPessoa.setPessoa(advogado.getPessoa());
		docPessoa.setUsuarioCadastro(advogado.getPessoa());
		TipoProcessoDocumento termoCompromisso = ParametroUtil.instance().getTipoProcessoDocumentoTermoCompromisso();
		docPessoa.setTipoProcessoDocumento(termoCompromisso);
		String msg = documentoPessoaHome.persist();
		if (msg != null) {
			return docPessoa;
		} else {
			return null;
		}
	}

	public void validarComInconsistencias() {
		TipoProcessoDocumento tipoProcessoDocumentoInconsistencia = ParametroUtil.instance()
				.getTipoProcessoDocumentoInconsistencia();
		if (tipoProcessoDocumentoInconsistencia == null) {
			FacesMessages fc = FacesMessages.instance();
			fc.clear();
			fc.add(Severity.INFO, "Parametro nao cadastrado");
		}
		ModeloDocumento md = ParametroUtil.instance().getModeloDocumentoInconsistencia();
		PessoaAdvogado advogado = getInstance();
		String documentoHtml = md.getModeloDocumento();
		documentoHtml = ProcessoDocumentoHome.processarModelo(documentoHtml);
		DocumentoPessoaHome documentoPessoaHome = DocumentoPessoaHome.instance();

		getInstance().setValidado(Boolean.FALSE);
		getInstance().setAtivo(Boolean.FALSE);

		String msgPersist = persist();
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
		docPessoa.setTipoProcessoDocumento(tipoProcessoDocumentoInconsistencia);
		// TODO usar em.persist(
		documentoPessoaHome.persist();
		this.documentoPessoa = docPessoa;
		this.exibePopUpInconsistencia = true;
		this.existeInconsistencia = false;
		FacesMessages fc = FacesMessages.instance();
		fc.clear();
		fc.add(Severity.INFO, "Cadastro realizado com sucesso. O Advogado deverá se dirigir a Sessão / Subseção"
				+ " Judiciária mais próxima para sanar inconsistências no cadastro.");
	}
	
	public void loadAdvogado(PessoaAdvogado advogado) {
		
		advogado.setNome(getInstance().getNome());
		advogado.setLogin(getInstance().getLogin());
		advogado.setSexo(getInstance().getSexo());
		advogado.setDataNascimento(getInstance().getDataNascimento());
		advogado.setNomeGenitora(getInstance().getNomeGenitora());
		advogado.getEnderecoList().clear();
		advogado.getEnderecoList().addAll(getInstance().getEnderecoList());
		advogado.setAssinatura(getInstance().getAssinatura());
		advogado.setCertChain(getInstance().getCertChain());
		PessoaAdvogadoHome.instance().setInstance(advogado);		
		
	}

	public String persist(boolean ignoraAusenciaEndereco) {
		String persist = null;
		if (!checkCPF() && beforePersistOrUpdate() && (ignoraAusenciaEndereco | isEnderecoValido())) {
			PessoaAdvogado advogado = null;
			List<Pessoa> listAdvogado = PessoaAdvogadoHome.instance().consultaAdvogados(getInstance().getNumeroCPF(), null);
		
			if(listAdvogado.size() > 0){
				advogado = PessoaAdvogadoHome.instance().obterPessoaAdvogado(listAdvogado.get(0));
				loadAdvogado(advogado);
				CadastroAdvogadoHome.instance().setInstance(advogado);
			}
			
			advogado = getInstance();
			advogado.setDataCadastro(new Date());
			advogado.setTipoPessoa(ParametroUtil.instance().getTipoAdvogado());
			
			TipoDocumentoIdentificacao tipo = null;
			try{
				tipo = ((TipoDocumentoIdentificacaoManager) Component.getInstance("tipoDocumentoIdentificacao")).findById("OAB");
				for (DadosAdvogadoOAB d : dadosOAB) {
					PessoaDocumentoIdentificacao doc = pessoaDocumentoIdentificacaoManager.recuperaDocumento(d.getNumInscricao(), tipo);
					if(doc == null){
						try {
							pessoaService.adicionaInscricaoOAB(advogado.getPessoa(), d);
						} catch (PJeBusinessException e) {
							logger.error("Não foi possível acrescentar a OAB {0}.", d.getNumInscricao());
						}
					}
				}
			}catch(PJeBusinessException e){
				logger.error("Erro ao tentar recuperar o tipo de documento advogado.");
			}			
			
			try {
				pessoaAdvogadoManager.persist(advogado);
			} catch (PJeBusinessException e1) {
				reportMessage(e1);
				return null;
			}
			logGravar(advogado);
			boolean cadastraEndereco = true;
			if (!isEnderecoValido()) {
				if (advogado.getEnderecoList().size() > 0) {
					this.endereco = advogado.getEnderecoList().get(0);
					this.endereco.setUsuario(advogado.getPessoa());
				} else {
					cadastraEndereco = false;
				}
			}

			ultimoIdInserido = advogado.getIdUsuario();

			FacesMessages fm = FacesMessages.instance();

			EntityManager em = getEntityManager();
			if (cadastraEndereco) {
				endereco.setDataAlteracao(new Date());
				endereco.setUsuario(advogado.getPessoa());
				em.persist(endereco);
				advogado.getEnderecoList().add(endereco);
				logGravar(endereco);

				try {
					pessoaAdvogadoManager.persist(advogado);
				} catch (PJeBusinessException e) {
					reportMessage(e);
					return null;
				}
			}
			UsuarioLocalizacao usuarioLocalizacao = new UsuarioLocalizacao();
			PessoaLocalizacao pessoaLocalizacao = new PessoaLocalizacao();

			Localizacao localizacao = new Localizacao();
			localizacao.setAtivo(Boolean.TRUE);
			if (endereco != null && endereco.getIdEndereco() != 0)
				localizacao.setEndereco(endereco);
			localizacao.setLocalizacao(LocalizacaoUtil.formataLocalizacaoPessoaFisicaEspecializada(advogado));

			em.persist(localizacao);
			logGravar(localizacao);

			usuarioLocalizacao.setUsuario(advogado.getPessoa());
			usuarioLocalizacao.setLocalizacaoFisica(localizacao);
			usuarioLocalizacao.setResponsavelLocalizacao(Boolean.TRUE);
			usuarioLocalizacao.setPapel(ParametroUtil.instance().getPapelAdvogado());
			em.persist(usuarioLocalizacao);
			logGravar(usuarioLocalizacao);

			pessoaLocalizacao.setLocalizacao(localizacao);
			pessoaLocalizacao.setPessoa(advogado.getPessoa());
			em.persist(pessoaLocalizacao);
			logGravar(pessoaLocalizacao);

			EntityUtil.flush(em);
			
			setInstance(advogado);
			persist = "persisted";
			fm.add(Severity.INFO, "Advogado cadastrado com sucesso");
			
		}
		return persist;
	}

	private Estado buscaEstado(String uf) {
		return (Estado) getEntityManager().createQuery("select e from Estado e where e.codEstado = :sigla")
				.setParameter("sigla", uf).getSingleResult();
	}

	@Override
	public String persist() {
		return this.persist(false);
	}

	private void logGravar(Object obj) {
		log.info("Gravando: " + LogUtil.toStringFields(obj));
	}

	private boolean isEnderecoValido() {
		FacesMessages fm = FacesMessages.instance();

		if (endereco == null || endereco.getCep() == null || Strings.isEmpty(endereco.getNomeEstado())) {
			String msg = "Endereço inválido";
			fm.add(Severity.ERROR, msg);
			log.info("isEnderecoValido():" + msg);
			return false;
		}
		return true;
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		List<Endereco> endList = null;
		if (getInstance() != null) {
			endList = getInstance().getEnderecoList();
		}
		if (isManaged() && endList.size() > 0) {
			getEnderecoHome().setId(endList.get(0).getIdEndereco());
		} else if ((changed && endList.size() > 0) || id == null) {
			getEnderecoHome().newInstance();
		}

		if (!Strings.isEmpty(getInstance().getEmail())) {
			String[] s = getInstance().getEmail().split("\\,");
			this.email1 = s[0];
			if (s.length > 1) {
				this.email2 = s[1];
			}
			if (s.length > 2) {
				this.email3 = s[2];
			}
		}

		if (changed) {
			getCadastroAdvogadoMunicipioSuggest().setInstance(getInstance().getMunicipioNascimento());
		}
		if (id == null) {
			getCadastroAdvogadoMunicipioSuggest().setInstance(null);
		}
	}

	public boolean checkCPF() {
		return PessoaAdvogadoHome.instance().checkCPF(getInstance().getNumeroCPF(), null);
	}

	public boolean checkLogin() {
		Boolean loginJaCadastrado = pessoaAdvogadoManager.checkLogin(getInstance().getLogin(), getInstance().getIdUsuario());
		if (!loginJaCadastrado) {
			FacesMessages.instance().addToControl("loginLogin", StatusMessage.Severity.ERROR, "Login já cadastrado!");
			getInstance().setLogin("");
		}
		
		return loginJaCadastrado;
	}

	public void limparLogin() {
		getInstance().setLogin("");
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
		}
	}

	public Endereco getEndereco() {
		return this.endereco;
	}

	public void setUltimoIdInserido(Integer ultimoIdInserido) {
		this.ultimoIdInserido = ultimoIdInserido;
	}

	public Integer getUltimoIdInserido() {
		return ultimoIdInserido;
	}

	public void confirmarCadastro() {
		getInstance().setValidado(Boolean.TRUE);
		getInstance().setAtivo(Boolean.TRUE);
		getInstance().setDataValidacao(new Date());
		
		try {
			pessoaAdvogadoManager.persistAndFlush(getInstance());
			setInstance(getInstance());
			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.INFO, "Usuário validado com sucesso.");			
		} catch (PJeBusinessException e) {
			reportMessage(e);
		}
	}

	public boolean canNewInstance() {
		return !(new br.com.itx.component.Util().isAjaxRequest()) && !isManaged();
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

	public void setTab() {
		super.setTab("Confirmacao");
	}

	public PessoaAdvogadoTipoInscricaoEnum[] getTipoInscricaoValues() {
		return PessoaAdvogadoTipoInscricaoEnum.values();
	}

	public SexoEnum[] getSexoValues() {
		return SexoEnum.values();
	}

	public void setExisteInconsistencia(boolean existeInconsistencia) {
		this.existeInconsistencia = existeInconsistencia;
	}

	public boolean getExisteInconsistencia() {
		return existeInconsistencia;
	}

	public void setVeracidadeCadastro(boolean veracidadeCadastro) {
		this.veracidadeCadastro = veracidadeCadastro;
	}

	public boolean getVeracidadeCadastro() {
		return veracidadeCadastro;
	}

	public void setExibePopUpInconsistencia(boolean exibePopUpInconsistencia) {
		this.exibePopUpInconsistencia = exibePopUpInconsistencia;
	}

	public boolean getExibePopUpInconsistencia() {
		return exibePopUpInconsistencia;
	}

	public void setDocumentoPessoa(DocumentoPessoa documentoPessoa) {
		this.documentoPessoa = documentoPessoa;
	}

	public DocumentoPessoa getDocumentoPessoa() {
		return documentoPessoa;
	}

	public void setEmail1(String email1) {
		this.email1 = email1;
	}

	public String getEmail1() {
		return email1;
	}

	public void setEmail2(String email2) {
		this.email2 = email2;
	}

	public String getEmail2() {
		return email2;
	}

	public void setEmail3(String email3) {
		this.email3 = email3;
	}

	public String getEmail3() {
		return email3;
	}

	public String getCamposDivergencia() {
		StringBuilder sb = new StringBuilder();

		sb.append(getCamposDivergenciaOab());

		sb.append(getCamposDivergenciaReceita());

		sb.append(getCamposDivergenciaCertificado());

		return sb.toString();
	}

	public String getCamposDivergenciaReceita() {
		StringBuilder sb = new StringBuilder();
		if (inconsistenciasReceita != null && inconsistenciasReceita.length() > 0) {
			sb.append("Receita Federal: ");
			sb.append(inconsistenciasReceita.toString());
			sb.append(".");
		}
		return sb.toString();
	}

	public String getCamposDivergenciaOab() {
		StringBuilder sb = new StringBuilder();
		if (inconsistenciasOab != null && inconsistenciasOab.length() > 0) {
			sb.append("OAB: ");
			sb.append(inconsistenciasOab.toString());
			sb.append(".");
		}
		return sb.toString();
	}

	public String getCamposDivergenciaCertificado() {
		StringBuilder sb = new StringBuilder();
		if (inconsistenciasCertificado != null && inconsistenciasCertificado.length() > 0) {
			sb.append("Certificado Digital: ");
			sb.append(inconsistenciasCertificado.toString());
			sb.append(".");
		}
		return sb.toString();
	}

	public void setCertChain(String certChain) {
		this.certChain = certChain;
	}

	public String getCertChain() {
		return certChain;
	}

	public void setEstado(Estado estado) {
		this.estado = estado;
	}

	public Estado getEstado() {
		return estado;
	}

	public void setTermoLido(String termoLido) {
		this.termoLido = termoLido;
	}

	public String getTermoLido() {
		return termoLido;
	}

	public void setCertChainStringLog(String certChainStringLog) {
		this.certChainStringLog = certChainStringLog;
	}

	public String getCertChainStringLog() {
		return certChainStringLog;
	}

	public void executeLogCertificadoInvalido() {
		String msg = "User: " + LogUtil.toStringForLog(getInstance()) + " certChainStringLog: " + certChainStringLog;
		log.warn(msg);
		CertificadoLog.executeLog(msg);
	}

	public boolean getLogradouroDisabled() {
		/*
		 * PJE-JT: Ricardo Scholz : PJEII-6339 - 2013-03-19 Alteracoes feitas pela JT. 
		 * Inclusão de código defensivo para evitar 'NullPointerException' quando 'endereco' estiver nulo.
		 */
		if (endereco == null || endereco.getCep() == null || endereco.getCep().getNumeroCep() == null) {
		//PJE-JT: Fim.
			return true;
		}
		/**
		 * [PJE-1178] Caso o CEP termine com 000, permite alterar o logradouro
		 */
		/*
		 * PJE-JT: Ricardo Scholz : PJEII-6339 - 2013-03-19 Alteracoes feitas pela JT. 
		 * Modificação da checagem do final do CEP, para evitar ArrayIndexOutOfBounds quando o CEP
		 * tinha menos de 6 caracteres. Chamada 'substring(5).equalsIgnoreCase("-000")' substituida
		 * por 'endsWith("-000")'.
		 */
		else if((endereco.getNomeLogradouro() == null) || (endereco.getCep().getNumeroCep().endsWith("-000"))){
		//PJE-JT: Fim.
			return false;
		} else {
			return true;
		}
	}
	
	public boolean getBairroDisabled() {
		/*
		 * PJE-JT: Ricardo Scholz : PJEII-4890 - 2013-01-31 Alteracoes feitas pela JT. 
		 * Inclusão de código defensivo para evitar 'NullPointerException' quando 'endereco' estiver nulo.
		 */
		if (endereco == null || endereco.getCep() == null || endereco.getCep().getNumeroCep() == null) {
			return true;
		}
		/*
		 * PJE-JT: Fim.
		 */
		/**
		 * [PJEII-2457] Caso o CEP termine com 000, permite alterar o bairro
		 */
		/*
		 * PJE-JT: Ricardo Scholz : PJEII-5705 - 2013-02-25 Alteracoes feitas pela JT. 
		 * Modificação da checagem do final do CEP, para evitar ArrayIndexOutOfBounds quando o CEP
		 * tinha menos de 6 caracteres. Chamada 'substring(5).equalsIgnoreCase("-000")' substituida
		 * por 'endsWith("-000")'.
		 */
		else if((endereco.getNomeBairro() == null) || (endereco.getCep().getNumeroCep().endsWith("-000"))){
		// PJE-JT: Fim.
			return false;
		} else {
			return true;
		}
	}
	
	public PessoaAdvogadoTipoInscricaoEnum getTipoInscricao() {
		this.tipoInscricao = instance.getTipoInscricao();
		return this.tipoInscricao;
	}
	
	public void setTipoInscricao(PessoaAdvogadoTipoInscricaoEnum tipoInscricao) {
		this.tipoInscricao = tipoInscricao;
		
		if(tipoInscricao != null){
			getInstance().setTipoInscricao(tipoInscricao);
			/*
			 * [PJEII-2423] PJE-JT: Sérgio Ricardo : PJE-1.4.4 
			 * O trecho abaixo foi retirado por setar indevidamente o atributo letraOAB com o mesmo valor do atributo tipoInscricao. Por esse motivo
			 * a validação do advogado no ws da OAB estava sendo rejeitada
			 */					
			/*		
			if(this.tipoInscricao.getLabel().equals("Advogado")){
				getInstance().setLetraOAB("A");
			}
			else if(tipoInscricao.getLabel().equals("Estagiario")){
				getInstance().setLetraOAB("E");
			} 
			else if(tipoInscricao.getLabel().equals("Suplementar")){
				getInstance().setLetraOAB("S");
			}
			*/
		}
		
	}
	
	public PessoaAdvogadoTipoInscricaoEnum[] getPessoaAdvogadoTipoInscricaoEnumValues(){
		return PessoaAdvogadoTipoInscricaoEnum.values();
	}
	
	public void limparCamposBrasileiro() {
		if (instance.getBrasileiro() == true) {
			instance.setNumeroPassaporte(null);
		} else {
			setEstado(null);
		}
	}
	
	public String carregarDadosAdvogado() {
		String cpfAdvogado = (String) Contexts.getConversationContext().get("cpfAdvogado");
		String retorno = load(cpfAdvogado);
		
		if(retorno != null && retorno.contains("erro")) {
			return "erro";
		}
		
		return "cadastrar";
	}
	

	public String load(String cpf) {
		newInstance();
		
		cpf = StringUtil.removeNaoNumericos(cpf);

		ConsultaClienteReceitaPFCNJ pf = ConsultaClienteReceitaPFCNJ.instance();

		DadosReceitaPessoaFisica dpf = null;

		try {
			dpf = pf.getDadosReceitaPessoaFisicaSemAtualizarBaseDeDados(cpf);
		} catch (Exception e) {
			e.printStackTrace();

			getStatusMessages()
					.add(Severity.ERROR,
							"Problemas na comunicação com a Receita Federal, por favor tente novamente mais tarde.");

			return "erro-consulta-receita";
		}

		ConsultaClienteOAB oab = ConsultaClienteOAB.instance();

		try {
			oab.consultaDados(cpf, true);
		} catch (Exception e) {
			e.printStackTrace();

			getStatusMessages()
					.add(Severity.ERROR,
							"Problemas na comunicação com a Ordem dos Advogados, por favor tente novamente mais tarde.");

			return "erro-consulta-oab";
		}

		dadosOAB = oab.getDadosAdvogadoList();
		Iterator<DadosAdvogadoOAB> it = dadosOAB.iterator();
		
		while(it.hasNext()) {
			DadosAdvogadoOAB d = it.next();
			
			if (!"REGULAR".equalsIgnoreCase(d.getSituacaoInscricao())) {
				// retira os nao regulares da lista.
				it.remove();
			}
			
			String inscricao = d.getNumInscricao();
            
	        if(inscricao != null && inscricao != ""){
	       
	            int tamanho = inscricao.length() - 1;
	            String letra = "";
	           
	            char c = inscricao.charAt(tamanho);
	           
	            if(Character.isLetter(c)){
	                letra = inscricao.substring(tamanho);
	                inscricao = inscricao.replace(letra, "");
	                inscricao = inscricao.replace("-", "");
	            }
	            
	            d.setLetra(letra);
	        }
	        
	        d.setNumInscricao(inscricao);
		}

		if (dadosOAB == null || dadosOAB.isEmpty()) {
			getStatusMessages()
					.add(Severity.ERROR,
							"Pesquisa na base de dados da Ordem dos Advogados do Brasil não retornou registros de OAB em situação regular para o CPF associado ao cartão de identidade digital. Não será possível prosseguir com o credenciamento. Procure sua Seccional da OAB para solução do problema.");

			return "erro-sem-oab";
		}
		
		//orderna a lista dos dados do OAB pelo Advogado (Principal), depois suplementares e por último estagiários.
		List<DadosAdvogadoOAB> dadosOABAdvogado = new ArrayList<DadosAdvogadoOAB>(0);
		List<DadosAdvogadoOAB> dadosOABSuplementar = new ArrayList<DadosAdvogadoOAB>(0);
		List<DadosAdvogadoOAB> dadosOABEstagiario = new ArrayList<DadosAdvogadoOAB>(0);
		it = null;
		it = dadosOAB.iterator();
		
		while(it.hasNext()) {
			DadosAdvogadoOAB d = it.next();
			if(d.getTipoInscricao().equals("ADVOGADO")) {
				dadosOABAdvogado.add(d);
			} else if(d.getTipoInscricao().equals("SUPLEMENTAR")) {
				dadosOABSuplementar.add(d);
			} else if(d.getTipoInscricao().equals("ESTAGIARIO")) {
				dadosOABEstagiario.add(d);
			}			
		}
		
		dadosOAB = new ArrayList<DadosAdvogadoOAB>(0);
		dadosOAB.addAll(dadosOABAdvogado);
		dadosOAB.addAll(dadosOABSuplementar);
		dadosOAB.addAll(dadosOABEstagiario);
		dadosOAB.get(0).setOabSelecionado(true);

		info("Registro OAB: " + dadosOAB.size());

		// carregar objetos de edicao.
		getInstance().setNumeroCPF(cpf);
		
		getInstance().setNome(dpf.getNome());

		getInstance().setLogin(cpf);

		getInstance().setSexo("1".equals(dpf.getSexo()) ? SexoEnum.M : SexoEnum.F);

		getInstance().setDataNascimento(dpf.getDataNascimento());

		getInstance().setNomeGenitora(dpf.getNomeMae());

		CepService cepService = ComponentUtil.getComponent("cepService");
		Cep cep = cepService.findByCodigo(dpf.getNumCEP());

		EnderecoHome.instance().carregaEndereco(cep, dpf.getBairro(),
				dpf.getLogradouro(), dpf.getNumLogradouro(),
				dpf.getComplemento());

		Endereco endereco = EnderecoHome.instance().getInstance();

		List<Endereco> enderecoList = new ArrayList<Endereco>();

		enderecoList.add(endereco);

		getInstance().setEnderecoList(enderecoList);

		getStatusMessages()
		.addToControl("aviso", Severity.INFO,
				"O formulário foi preenchido de forma automática com informações colhidas na base de dados da Ordem dos Advogados do Brasil e/ou Receita Federal. Caso deseje, atualize os  referidos dados  e clique em \"Prosseguir\".");
		
		info("instance", getInstance());

		info("endereco", endereco);

		dadosCarregadoReceitaOAB = true;
		
		return "cadastrar";
	}

	public boolean isDadosCarregadoReceitaOAB() {
		return dadosCarregadoReceitaOAB;
	}

	public void setDadosCarregadoReceitaOAB(boolean dadosCarregadoReceitaOAB) {
		this.dadosCarregadoReceitaOAB = dadosCarregadoReceitaOAB;
	}

	/**
	 * PJEII-3673 PJEII-3648 - Alterar layout de telas
	 * Método criado para registrar o OAB do radiobutton selecionado
	 * @param dadosOABSelecionado
	 * @since 1.4.6
	 * @author Thiago Oliveira
	 */
	public void setSelectedRowOab(DadosAdvogadoOAB dadosOABSelecionado){
		for(DadosAdvogadoOAB dado : dadosOAB){
			if(dado.equals(dadosOABSelecionado)){
				dado.setOabSelecionado(true);
			}else{
				dado.setOabSelecionado(false);
			}			
		}	
	}

	public List<DadosAdvogadoOAB> getDadosOAB() {
		return dadosOAB;
	}

	public void setDadosOAB(List<DadosAdvogadoOAB> dadosOAB) {
		this.dadosOAB = dadosOAB;
	}
}
