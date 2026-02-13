/**
 * 
 */
package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.Util;
import br.com.infox.cliente.home.PessoaAdvogadoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.trf.webservice.ConsultaClienteOAB;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.DocumentoPessoaManager;
import br.jus.cnj.pje.nucleo.manager.EnderecoManager;
import br.jus.cnj.pje.nucleo.manager.EstadoManager;
import br.jus.cnj.pje.nucleo.manager.ModeloDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaAdvogadoManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoManager;
import br.jus.cnj.pje.nucleo.service.CepService;
import br.jus.cnj.pje.nucleo.service.PapelService;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.DocumentoPessoa;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.PessoaAdvogadoTipoInscricaoEnum;
import br.jus.pje.nucleo.enums.StatusSenhaEnum;
import br.jus.pje.ws.externo.cna.entidades.DadosAdvogadoOAB;

/**
 * Componente de controle da tela de cadastro de usuários novos no PJe.
 * A tela em questão deve ser /publico/cadastro/cadastro.xhtml
 * 
 * @author cristof
 *
 */
@Name(CadastroUsuarioAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class CadastroUsuarioAction implements Serializable, ArquivoAssinadoUploader {

	private static final long serialVersionUID = -3527537500356632826L;
	
	public static final String NAME = "cadastroUsuarioAction";
	
	@Logger
	private Log logger;
	
	@In(value="pje:cadastro:inscricaoMF", scope=ScopeType.SESSION)
	private String inscricaoMF;
	
	@In(value="pje:cadastro:dataNascimento", scope=ScopeType.SESSION, required= false)
	private Date dataNascimento;
	
	@In(value="pje:cadastro:inscricaoConsulente", scope=ScopeType.SESSION, required= false)
	private String inscricaoConsulente;
	
	private boolean cadastroPJ = false;
	
	private Cep cep;
	
	private boolean cadastroAdvogado;
	
	private boolean termoPreparado = false;
	
	private boolean push = false;
	
	private Pessoa pessoa;
	
	private Endereco endereco;
	
	private List<DadosAdvogadoOAB> inscricoesOAB;
	
	private DadosAdvogadoOAB inscricaoEscolhida;
	
	private String numeroCEP;
	
	private List<String> mensagens = new ArrayList<String>();
	
	private DocumentoPessoa termo;

	private String signature;
	
	private String certChain;
	
	private boolean finalizado = false;
	
	private String ERRO_DATA_NASC = "Não foi possível confirmar os seus dados, por favor, entre em contato com o suporte do tribunal.";
	private String ERRO_CPF_RESPONSAVEL = "Não foi possível confirmar os dados do responsável pela pessoa jurídica, por favor, entre em contato com o suporte do tribunal.";
	
	@Create
	@Begin(join=true, flushMode=FlushModeType.COMMIT)
	public void init(){
		pessoa = null;
		if(inscricaoMF == null || inscricaoMF.isEmpty()){
			mensagens.add("Não foi informado o número de inscrição (CPF ou CNPJ) do potencial usuário no cadastro de pessoas.");
			return;
		}
		String inscricaoMFSemPontos = InscricaoMFUtil.retiraMascara(inscricaoMF);
		
		if(inscricaoMFSemPontos.length() == 14) {
			//pessoa juridica - jus postulandi
			if(inscricaoConsulente == null) {
				mensagens.add(ERRO_CPF_RESPONSAVEL);
				finalizado = true;
				termoPreparado= false;
				return;
			}
			cadastroPJ = true;
			carregarDadosPessoaJuridica();
		} else if(inscricaoMFSemPontos.length() == 11) {
			//pessoa fisica - jus postulandi / advogado
			if(dataNascimento != null) {
				carregarDadosPessoaFisica();
			} else {
				mensagens.add(ERRO_DATA_NASC);
				finalizado = true;
				termoPreparado= false;
				return;
			}
		}

		if(pessoa == null){
			finalizado = true;
			termoPreparado= false;
			return;
		}
		if(pessoa.getEnderecoList().size() > 0){
			endereco = pessoa.getEnderecoList().get(0);
			numeroCEP = endereco.getCep().getNumeroCep();
			cep = endereco.getCep();
		}else{
			numeroCEP = "";
		}
	}
	
	private void carregarDadosPessoaFisica(){
		try {
			pessoa = (PessoaFisica) getPessoaService().findByInscricaoMF(inscricaoMF, inscricaoMF);
			mensagens.add("Os dados abaixo foram recuperados da Receita Federal do Brasil.");
		} catch (PJeBusinessException e) {
			mensagens.add("Erro ao tentar recuperar as informações da pessoa com CPF ["+inscricaoMF+ "]. Por favor, dirija-se ao órgão judiciário para realizar seu cadastro.");
			mensagens.add(e.getLocalizedMessage());
			return;
		}
		if(!getUsuarioService().validaDadosCadastrais(pessoa, dataNascimento)){
			mensagens.clear();
			mensagens.add("Não será possível realizar o cadastro via internet: a data de nascimento que consta no certificado digital é diferente da data de nascimento recuperada da Receita Federal do Brasil.");
			mensagens.add("Por favor, retifique suas informações junto à Receita Federal do Brasil ou compareça ao órgão judiciário para finalizar seu cadastro.");
			finalizado = true;
			return;
		}
		try {
			ConsultaClienteOAB consultaClienteOAB = ComponentUtil.getComponent(ConsultaClienteOAB.class, true);
			consultaClienteOAB.consultaDados(inscricaoMF, true);
			inscricoesOAB = consultaClienteOAB.getDadosAdvogadoList();
			if(inscricoesOAB == null){
				inscricoesOAB = Collections.emptyList();
			}else{
				processaInscricoes();
				if(inscricoesOAB.size() == 0 || !isAdvogadoComInscricaoDiferenteDeEstagiario(inscricoesOAB)){
					mensagens.add("Embora tenham sido recuperadas informações do Cadastro Nacional de Advogados da Ordem dos Advogados do Brasil, não há registro de inscrição ativa. Você pode prosseguir seu cadastro como usuário simples do sistema e solicitar posteriormente, no tribunal de cadastro, a modificação de seu perfil para o de advogado.");
				}else{
					if(Authenticator.isJusPostulandi()) {
						cadastroAdvogado = false;
					}else {
						cadastroAdvogado = true;						
					}
				}
			}
		} catch (Exception e) {
			mensagens.add( "Houve um erro ao tentar contactar o CNA/OAB. Você pode continuar seu cadastro como usuário comum. Caso seja advogado, procure o órgão judiciário de cadastramento.");
		}
	}
	
	/**
     * Verificar se o advogado possui alguma inscrição diferente de estagiário.
     * 
	 * @param List<DadosAdvogadoOAB> inscricoesOAB
	 * @return Verdadeiro se o advogado possuir alguma inscrição diferente de estagiário. 
	 */
	private boolean isAdvogadoComInscricaoDiferenteDeEstagiario(List<DadosAdvogadoOAB> inscricoesOAB){
		for(DadosAdvogadoOAB dadosAdvogadoOAB: inscricoesOAB){
			if(!PessoaAdvogadoTipoInscricaoEnum.isEstagiario(dadosAdvogadoOAB.getTipoInscricao())){
				return true;
			}
		}
		
		return false;
	}
	
	private void carregarDadosPessoaJuridica(){
		try {
			pessoa = (PessoaJuridica) getPessoaService().findByInscricaoMF(inscricaoMF, inscricaoConsulente);
			mensagens.add("Os dados abaixo foram recuperados da Receita Federal do Brasil.");
		} catch (PJeBusinessException e) {
			mensagens.add("Erro ao tentar recuperar as informações da pessoa com CNPJ [{0}]. Por favor, dirija-se ao órgão judiciário para realizar seu cadastro.");
			return;
		}
	}
	
	/**
	 * Verifica as informações retornadas da Ordem dos Advogados do Brasil quanto à regularidade e validade e 
	 * ordena a lista {@link #inscricoesOAB} segundo o tipo (principal, suplementares e estagiários).
	 * 
	 */
	private void processaInscricoes(){
		List<DadosAdvogadoOAB> principais = new ArrayList<DadosAdvogadoOAB>(inscricoesOAB.size());
		List<DadosAdvogadoOAB> suplementares = new ArrayList<DadosAdvogadoOAB>(inscricoesOAB.size());
		List<DadosAdvogadoOAB> estagiarios = new ArrayList<DadosAdvogadoOAB>(inscricoesOAB.size());
		for (DadosAdvogadoOAB inscricao: inscricoesOAB) {
			if(inscricao.getSituacaoInscricao().equalsIgnoreCase("regular")){
				if(inscricao.getTipoInscricao().equalsIgnoreCase("advogado")){
					principais.add(inscricao);
				}else if(inscricao.getTipoInscricao().equalsIgnoreCase("suplementar")){
					suplementares.add(inscricao);
				}else if(inscricao.getTipoInscricao().equalsIgnoreCase("estagiario")){
					estagiarios.add(inscricao);
				}
			}
		}
		inscricoesOAB.clear();
		inscricoesOAB.addAll(principais);
		inscricoesOAB.addAll(suplementares);
		inscricoesOAB.addAll(estagiarios);
	}
	
	public void confirmarDados(){
		try {
			termo = getUsuarioService().preparaTermo(pessoa, cadastroAdvogado);
			termoPreparado = true;
			mensagens.clear();
			FacesMessages.instance().add(Severity.INFO, "Dados confirmados. Leia o termo e assine eletronicamente ao final.");
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao preparar o termo de cadastramento. Por favor, dirija-se ao órgão judiciário para realizar seu cadastro.");
		}
	}
	
	public void retificarDados(){
		termoPreparado = false;
		FacesMessages.instance().add(Severity.INFO, "Por favor, retifique as informações para confirmar o cadastro.");
	}
	
	@SuppressWarnings("unchecked")
	public void finalizarCadastro(){
		try{
			Conversation conversation = ComponentUtil.getComponent(Conversation.class);
			if(conversation.isLongRunning()){
				conversation.changeFlushMode(FlushModeType.COMMIT);
			}
			if(signature == null || signature.isEmpty()){
				FacesMessages.instance().add(Severity.ERROR, "Não foi possível obter a assinatura. Por favor, tente novamente ou procure o órgão judiciário da instalação para finalizar seu cadastro.");
			}
			if(certChain == null || certChain.isEmpty()){
				FacesMessages.instance().add(Severity.ERROR, "Não foi possível obter a assinatura. Por favor, tente novamente ou procure o órgão judiciário da instalação para finalizar seu cadastro.");
			}
			termo.setAssinatura(signature);
			termo.setCertChain(certChain);
			pessoa.setAssinatura(signature);
			pessoa.setCertChain(certChain);
			if(getUsuarioService().verificaTermo(pessoa, termo)){
				PessoaAdvogadoManager pessoaAdvogadoManager = getPessoaAdvogadoManager();
				if (cadastroAdvogado) {
					EstadoManager estadoManager = ComponentUtil.getComponent(EstadoManager.class);
					Estado estadoInscricao = estadoManager.findBySigla(inscricaoEscolhida.getUf());
					if(estadoInscricao == null){
						FacesMessages.instance().add(Severity.ERROR, "A informação oriunda do CNA/OAB quanto à unidade federativa da inscrição é inválida ({0}). Por favor, dirija-se ao órgão judiciário para realizar seu cadastro.", inscricaoEscolhida.getUf());
						return;
					}
					pessoa = (PessoaFisica) getPessoaService().persist(pessoa);
					pessoa = (PessoaFisica) getPessoaService().especializa(pessoa, inscricaoMF, PessoaAdvogado.class);
					for(DadosAdvogadoOAB insc: inscricoesOAB){
						if(estadoInscricao != null){
							getPessoaService().adicionaInscricaoOAB((PessoaFisica) pessoa, insc);
						}
					}
					PessoaAdvogado adv = EntityUtil.getEntityManager().getReference(PessoaAdvogado.class, pessoa.getIdPessoa());
					adv.setIncluirProcessoPush(push);
					pessoaAdvogadoManager.complementaCadastro(adv, estadoInscricao, inscricaoEscolhida.getNumInscricao());
				} else {
					pessoa = getPessoaService().persist(pessoa);
					pessoa.setSenha(PjeUtil.instance().gerarNovaSenha());
					if(!verificaPessoaJaPossuiPerfilJusPostulandi()) {
						getPessoaService().tornaJusPostulandi(pessoa);
					}
				}
				termo = getUsuarioService().acrescentaTermo(pessoa, termo);

				verificaUsuarioMigrado();

				pessoaAdvogadoManager.flush();
				conversation.end();
				Redirect redir = Redirect.instance();
				redir.setViewId("/publico/usuario/cadastroConcluido.xhtml");
				redir.execute();
			}else{
				FacesMessages.instance().add(Severity.ERROR, "Houve um erro ao tentar verificar a assinatura digital do documento. Por favor, tente novamente ou dirija-se ao órgão judiciário para realizar seu cadastro.");
			}
		}catch(Exception e){
			FacesMessages.instance().add(Severity.ERROR, "Não foi possível finalizar o cadastro. Por favor, tente novamente ou dirija-se ao órgão judiciário para realizar seu cadastro.");
		}
	}

	// Apos o usuario migrado assinar o termo, mudar o statusSenha de Migrado para
	// Bloqueado
	private void verificaUsuarioMigrado() throws PJeBusinessException {
		if (StatusSenhaEnum.M.equals(pessoa.getStatusSenha())) {
			pessoa.setStatusSenha(StatusSenhaEnum.B);
			getPessoaService().persist(pessoa);
		}
	}

	/**
	 * metodo responsavel por verificar no banco de dados se a pessoa já possui uma localizacao de jus postulandi
	 * @return true se ja possuir pelo menos uma localizacao de jus postulandi / false
	 */
	private boolean verificaPessoaJaPossuiPerfilJusPostulandi() {
		PapelService papelService = ComponentUtil.getComponent(PapelService.class);
		Papel papelJusPostulandi = papelService.findById(getParametroService().valueOf(Parametros.ID_PAPEL_JUSPOSTULANDI));
		UsuarioLocalizacaoManager usuarioLocalizacaoManager = ComponentUtil.getComponent(UsuarioLocalizacaoManager.class);
		List<UsuarioLocalizacao> localizacoesJusPostulandi = usuarioLocalizacaoManager.getLocalizacoesAtuais(pessoa, papelJusPostulandi);
		if(localizacoesJusPostulandi == null || localizacoesJusPostulandi.isEmpty()) {
			return false;
		}
		return true;
	}

	@End
	public void cancelarCadastro(){
		Redirect redir = Redirect.instance();
		redir.setViewId("/login.xhtml");
		redir.execute();
		return;
	}
	
	public void atualizarCep(){
		CepService cepService = ComponentUtil.getComponent(CepService.class);
		cep = cepService.findByCodigo(numeroCEP);
		if(cep != null){
			EnderecoManager enderecoManager = ComponentUtil.getComponent(EnderecoManager.class);
			endereco = enderecoManager.criaEndereco(cep);
			numeroCEP = cep.getNumeroCep();
		}else{
			endereco = new Endereco();
			FacesMessages.instance().add(Severity.ERROR, "CEP inexistente.");
		}
	}

	public List<String> getMensagens() {
		return mensagens;
	}

	public void setMensagens(List<String> mensagens) {
		this.mensagens = mensagens;
	}

	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}

	public Endereco getEndereco() {
		return endereco;
	}

	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
	}

	public DadosAdvogadoOAB getInscricaoEscolhida() {
		return inscricaoEscolhida;
	}

	public void setInscricaoEscolhida(DadosAdvogadoOAB inscricaoEscolhida) {
		this.inscricaoEscolhida = inscricaoEscolhida;
	}

	public String getNumeroCEP() {
		return numeroCEP;
	}

	public void setNumeroCEP(String numeroCEP) {
		this.numeroCEP = numeroCEP;
	}

	public List<DadosAdvogadoOAB> getInscricoesOAB() {
		return inscricoesOAB;
	}

	public boolean isCadastroAdvogado() {
		return cadastroAdvogado;
	}
	
	public boolean isTermoPreparado() {
		return termoPreparado;
	}
	
	public Cep getCep(){
		return cep;
	}
	
	public DocumentoPessoa getTermo() {
		return termo;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getCertChain() {
		return certChain;
	}

	public void setCertChain(String certChain) {
		this.certChain = certChain;
	}

	public boolean isPush() {
		return push;
	}

	public void setPush(boolean push) {
		this.push = push;
	}

	public boolean isFinalizado() {
		return finalizado;
	}
	
	public boolean isCadastroPJ() {
		return cadastroPJ;
	}
	
	/**
	 * Recupera a inscrição do advogado na Ordem dos Advogados do Brasil
	 * no formato UFNNNNNN-L seja no cadastro de novo usuário ou no credenciamento do advogado.
	 * 
	 * @return a inscrição formatada.
	 */
	@Transient
	public String getOabFormatado() {
		if (getInscricaoEscolhida() == null) {
			PessoaAdvogadoHome advogadoHome = PessoaAdvogadoHome.instance();
			
			StringBuilder bf = new StringBuilder();
			if (advogadoHome.getInstance().getUfOAB() != null) {
				bf.append(advogadoHome.getInstance().getUfOAB().getCodEstado());
			}
			bf.append(advogadoHome.getInstance().getNumeroOAB());
			if (advogadoHome.getInstance().getLetraOAB() != null && advogadoHome.getInstance().getLetraOAB().trim().length() > 0) {
				bf.append("-" + advogadoHome.getInstance().getLetraOAB());
			}
			return bf.toString();
			
		} else{
			StringBuilder bf = new StringBuilder();
			if (inscricaoEscolhida.getUf() != null) {
				bf.append(inscricaoEscolhida.getUf());
			}
			bf.append(getInscricaoEscolhida().getNumInscricao());
			if (inscricaoEscolhida.getLetra() != null && inscricaoEscolhida.getLetra().trim().length() > 0) {
				bf.append("-" + inscricaoEscolhida.getLetra());
			}
			return bf.toString();
		}
	}
	
	/**
	 * Metodo que altera a localizacao atual do usuário a localização de jus postulandi, caso exista
	 * Atualiza o combo de localizações e atualiza o painel. 
	 */
	public void alterarLocalizacaoAtualJusPostulandi() {
		try {
			DocumentoPessoaManager documentoPessoaManager = ComponentUtil.getComponent(DocumentoPessoaManager.class);
			if( !documentoPessoaManager.possuiPapelJusPostulandi(pessoa) ) {
				getPessoaService().tornaJusPostulandi(pessoa);
				PessoaAdvogadoManager pessoaAdvogadoManager = getPessoaAdvogadoManager();
				pessoaAdvogadoManager.flush();
				Redirect redir = Redirect.instance();
				redir.setViewId("/publico/usuario/cadastroConcluido.xhtml");
				redir.execute();
			} else {
				Papel papelJusPostulandi = ParametroUtil.instance().getPapelJusPostulandi();
				List<UsuarioLocalizacao> localizacoesUsuario = getUsuarioService().getLocalizacoesAtivas(pessoa, papelJusPostulandi);
				if( localizacoesUsuario != null && localizacoesUsuario.size() > 0 ) {
					Authenticator.instance().setLocalizacaoAtualCombo(localizacoesUsuario.get(0));
				}
			}
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(Severity.ERROR, "Não foi possível finalizar o cadastro. Por favor, tente novamente ou dirija-se ao tribunal para realizar seu cadastro.");
		}
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest request, ArquivoAssinadoHash arquivoAssinadoHash) {
		setSignature(arquivoAssinadoHash.getAssinatura());
		setCertChain(arquivoAssinadoHash.getCadeiaCertificado());
	}
	
	public String getUrlDocsField() {
		DocumentoJudicialService documentoJudicialService = ComponentUtil.getComponent(DocumentoJudicialService.class);
		return documentoJudicialService.getDownloadLink(getTermo().getDocumentoHtml());		
	}

	@Override
	public String getActionName() {
		return NAME;
	}
	
	/**
	 * Retorna o aviso de não acesso ao sistema
	 * 
	 * @return o documento de aviso para exibição
	 * @throws PJeBusinessException caso tenha havido algum erro na recuperação do modelo
	 */
	public String retornaAviso() throws PJeBusinessException {
		String conteudo = "Para ter acesso ao PJe é necessário assinar o termo de compromisso do sistema.<br><br>Caso possua o certificado digital, refaça o acesso, clicando no botão \"Certificado digital\" e assine o termo para prosseguir.<br><br>Para quem que não possui certificado digital é necessário dirigir-se pessoalmente a um posto de atendimento do tribunal levando os seguintes documentos: uma cópia do CPF, do RG (ou da OAB no caso de advogado) e um comprovante de residência. Neste caso, o servidor fará o seu cadastro e anexará o seu termo assinado.<br><br>Você pode ter acesso às informações públicas dos processos sem ter o termo assinado, pela consulta pública, clique na opção \"Consulta processual\" na página inicial do PJe.<br>";
		String parametroModelo = Parametros.ID_MODELO_CADASTRO_SEM_TERMO_COMPROMISSO;
		String idModelo = getParametroService().valueOf(parametroModelo);
		if(idModelo != null && !idModelo.isEmpty()){
			ModeloDocumentoManager modeloDocumentoManager = ComponentUtil.getComponent(ModeloDocumentoManager.class);					
			ModeloDocumento md = modeloDocumentoManager.findById(Integer.parseInt(idModelo));
			if(md != null){
				conteudo = modeloDocumentoManager.obtemConteudo(md);
			}
		}
		return conteudo;
	}
	
	/**
	 * metodo responsavel por verificar se o cep foi preenchido incorretamente (nulo ou em branco).
	 * controla a edicao de campos na tela.
	 * @return true se endereco.cep for nulo ou se o campo numeroCEP for nulo ou vazio.
	 */
	public boolean isCepNulo() {
		return Util.isEnderecoCepNulo(this.endereco);
	}
	
	private PessoaService getPessoaService() {
		return ComponentUtil.getComponent(PessoaService.class);
	}
	
	private UsuarioService getUsuarioService() {
		return ComponentUtil.getComponent(UsuarioService.class);
	}
	
	private ParametroService getParametroService() {
		return ComponentUtil.getComponent(ParametroService.class);
	}
	
	private PessoaAdvogadoManager getPessoaAdvogadoManager() {
		return ComponentUtil.getComponent(PessoaAdvogadoManager.class);
	}
	
	/**
	 * Redirect para a tela de cadastro.
	 * 
	 * @param pessoa Pessoa (física ou jurídica)
	 */
	public static void redirectParaCadastro(Pessoa pessoa) {
		carregarVariaveisDeSessao(pessoa);
		
		redirectParaCadastro();
	}
	
	/**
	 * Redirect para a tela de cadastro.
	 * 
	 * @param inscricaoMF
	 * @param dataNascimento
	 * @param inscricaoConsulente
	 */
	public static void redirectParaCadastro(String inscricaoMF, Date dataNascimento, String inscricaoConsulente) {
		carregarVariaveisDeSessao(inscricaoMF, dataNascimento, inscricaoConsulente);
		
		redirectParaCadastro();
	}
	
	/**
	 * Redirect para a tela de cadastro.
	 */
	public static void redirectParaCadastro() {
		FacesMessages.instance().clear();

		Redirect redirect = Redirect.instance();
		redirect.setViewId("/publico/usuario/cadastro.xhtml");
		redirect.execute();
	}
	
	/**
	 * Carrega as variáveis de sessão necessárias para o cadastro do usuário.
	 * As variáveis são: pje:cadastro:inscricaoMF, pje:cadastro:dataNascimento e pje:cadastro:inscricaoConsulente.
	 * 
	 * @param pessoa Pessoa (física ou jurídica)
	 */
	public static void carregarVariaveisDeSessao(Pessoa pessoa) {
		if (pessoa != null) {
			if (pessoa instanceof PessoaFisica) {
				PessoaFisica pf = (PessoaFisica) pessoa;
				carregarVariaveisDeSessao(pessoa.getLogin(), pf.getDataNascimento(), null);
			} else {
				PessoaJuridica pj = (PessoaJuridica) pessoa;
				carregarVariaveisDeSessao(pessoa.getLogin(), pj.getDataAbertura(), pj.getNumeroCpfResponsavel());
			}
		}
	}
	
	/**
	 * Carrega as variáveis de sessão necessárias para o cadastro do usuário.
	 * As variáveis são: pje:cadastro:inscricaoMF, pje:cadastro:dataNascimento e pje:cadastro:inscricaoConsulente.
	 * 
	 * @param inscricaoMF
	 * @param dataNascimento
	 * @param inscricaoConsulente
	 */
	public static void carregarVariaveisDeSessao(String inscricaoMF, Date dataNascimento, String inscricaoConsulente) {
		Context context = Contexts.getSessionContext();
		context.set(Variaveis.INSCRICAO_MF_CADASTRO, inscricaoMF);
		context.set(Variaveis.DATA_NASCIMENTO_CADASTRO, dataNascimento);
		context.set(Variaveis.INSCRICAO_CONSULENTE_CADASTRO, inscricaoConsulente);
	}
}
