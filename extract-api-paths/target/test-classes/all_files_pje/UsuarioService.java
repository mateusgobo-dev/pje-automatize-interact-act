/**
 * 
 */
package br.jus.cnj.pje.nucleo.service;

import java.util.*;

import org.hibernate.Hibernate;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.component.ControleFiltros;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.certificado.VerificaCertificado;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.service.EmailService;
import br.com.infox.pje.manager.PessoaFisicaManager;
import br.com.infox.utils.Constantes;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.entidades.vo.ParametroEventoRegistroLoginVO;
import br.jus.cnj.pje.extensao.servico.ParametroService;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.DocumentoPessoaManager;
import br.jus.cnj.pje.nucleo.manager.LocalizacaoManager;
import br.jus.cnj.pje.nucleo.manager.LogAcessoManager;
import br.jus.cnj.pje.nucleo.manager.ModeloDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaLocalizacaoManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoVisibilidadeManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLoginManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioMobileManager;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.cnj.pje.webservice.client.keycloak.KeycloakServiceClient;
import br.jus.cnj.pje.webservice.json.InformacaoUsuarioSessao;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.DocumentoPessoa;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAssistenteAdvogadoLocal;
import br.jus.pje.nucleo.entidades.PessoaAssistenteProcuradoriaLocal;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaFisicaEspecializada;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.PessoaLocalizacao;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.PessoaServidor;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoVisibilidade;
import br.jus.pje.nucleo.entidades.UsuarioMobile;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.StatusSenhaEnum;
import br.jus.pje.nucleo.util.DateUtil;

/**
 * @author cristof
 *
 */
@Name("usuarioService")
public class UsuarioService extends BaseService {
	
	@Logger
	private Log logger;
	
	@In
	private EnderecoService enderecoService;

	@In
	private LocalizacaoManager localizacaoManager;
	
	@In
	private PessoaLocalizacaoManager pessoaLocalizacaoManager;
	
	@In(required=false)
	private Usuario usuarioLogado;
	
	@In(value="usuarioLogadoLocalizacaoAtual", required=false, scope=ScopeType.SESSION)
	private UsuarioLocalizacao localizacaoAtual;
	
	@In
	private UsuarioLocalizacaoManager usuarioLocalizacaoManager;
	
	@In
	private UsuarioManager usuarioManager;
	
	@In
	private UsuarioLoginManager usuarioLoginManager;
	
	@In(value="pje:usuario:localizacoesAtuais", required=false, scope=ScopeType.SESSION)
	private List<UsuarioLocalizacao> localizacoesAtuais;
	
	@In
	private DocumentoPessoaManager documentoPessoaManager;
	
	@In
	private ModeloDocumentoManager modeloDocumentoManager;
	
	@In
	private TipoProcessoDocumentoManager tipoProcessoDocumentoManager;
	
	@In
	private ParametroService parametroService;
	
	@In
	private PessoaFisicaManager pessoaFisicaManager;
	
	@In
	private UsuarioLocalizacaoVisibilidadeManager usuarioLocalizacaoVisibilidadeManager;
	
	@In
	private EmailService emailService;
	
	@In
	private UsuarioMobileManager usuarioMobileManager;

	@In
	private LogAcessoManager logAcessoManager;
	
	public Usuario getUsuarioLogado() {
		return Authenticator.getUsuarioLogado();
	}
	
	public Usuario getUsuarioSistema() throws PJeBusinessException{
		try {
			Usuario usuario = ParametroUtil.instance().getUsuarioSistema();
			if(usuario == null) {
				Integer idUsuarioSistema = Integer.parseInt(parametroService.valueOf(Parametros.ID_USUARIO_SISTEMA));
				usuario = usuarioManager.findById(idUsuarioSistema);
			}
			return usuario;
		} catch (PJeBusinessException e) {
			throw new PJeBusinessException("pje.usuario.error.recuperacaoPorId", e, usuarioLogado.getIdUsuario());
		}
	}
	
	/**
	 * [PJEIII-3199] Criação do método para ser utilizada a service do Usuario e recuperar o Usuario através do login.
	 * @param login
	 * @return Usuario
	 */
	public Usuario findByLogin(String login) {
		login = InscricaoMFUtil.retiraMascara(login);
		return usuarioManager.findByLogin(login);
	}
	
	/**
	 * Acrescenta, para uma dada pessoa e papel, a {@link Localizacao},
	 * a {@link UsuarioLocalizacao} e a {@link PessoaLocalizacao} pertinentes.
	 * 
	 * A localização será vinculada ao endereço mais recentemente
	 * atualizado da pessoa.
	 * 
	 * @param nomeLocalizacao o nome da localização a ser criada
	 * @param pessoa a pessoa quem se pretende acrescentar uma localização pessoal.
	 * @param papel o papel a ser atribuído.
	 * @throws PJeBusinessException
	 */
	public void acrescentaLocalizacaoPessoal(String nomeLocalizacao, Pessoa pessoa, Papel papel) throws PJeBusinessException{
		Localizacao loc = localizacaoManager.getLocalizacao(nomeLocalizacao);
		Endereco end = enderecoService.recuperaEnderecoRecente(pessoa);
		if(end == null && pessoa.getEnderecoList().size() > 0){
			end = pessoa.getEnderecoList().get(pessoa.getEnderecoList().size() -1);
		}
		loc.setEndereco(end);
		localizacaoManager.persist(loc);
		
		List<UsuarioLocalizacao> listaLocalizacao = usuarioLocalizacaoManager.getLocalizacoesAtuais(pessoa, papel, loc);
		UsuarioLocalizacao ul = null;
		if( listaLocalizacao.size() == 0) {
			ul = new UsuarioLocalizacao();
			ul.setLocalizacaoFisica(loc);
			ul.setUsuario(pessoa);
			ul.setPapel(papel);
			ul.setResponsavelLocalizacao(true);
			usuarioLocalizacaoManager.persist(ul);
		}
		PessoaLocalizacao pl = pessoaLocalizacaoManager.recuperaUnivoca(pessoa, loc);
		if(pl == null){
			pl = new PessoaLocalizacao();
			pl.setLocalizacao(loc);
			pl.setPessoa(pessoa);
			pessoaLocalizacaoManager.persist(pl);
		}
		if( ul != null) {
			pessoa.getUsuarioLocalizacaoList().add(ul);
		}
		pessoa.getPapelSet().add(papel);
	}
	
	public void atualizaLocalizacoesPessoais(String novoNome, Pessoa pessoa, Papel papel){
		
	}
	
	/**
	 * Recupera as localizações vinculadas a uma dada pessoa a partir de uma específica posição e até uma quantidade máxima.
	 *  
	 * @param p a pessoa a respeito da qual se está pesquisando a localização.
	 * @param first o primeiro resultado esperado da lista. Use null para recuperar a partir da primeira posição.
	 * @param maxResults o máximo de resultados esperado. Use null para suprimir a existência de limites
	 * @return a lista de localizações da pessoa, limitada ao máximo indicado e partir de uma dada posição
	 * @throws PJeBusinessException 
	 */
	public List<Localizacao> getLocalizacoes(Pessoa p, Integer first, Integer maxResults) throws PJeBusinessException{
		return localizacaoManager.getLocalizacoesPessoais(p, first, maxResults);
	}
	
	/**
	 * Recupera todas as localizações vinculadas a uma dada pessoa.
	 * 
	 * @param p a pessoa a respeito da qual se pretende recuperar as localizações
	 * @return a lista de localizações da pessoa
	 * @throws PJeBusinessException 
	 */
	public List<Localizacao> getLocalizacoes(Pessoa p) throws PJeBusinessException{
		return getLocalizacoes(p, null, null);
	}
	
	public List<UsuarioLocalizacao> getLocalizacoesAtivas(Usuario usuario){
		return usuarioLocalizacaoManager.getLocalizacoesAtuais(usuario);
	}
	
	public List<UsuarioLocalizacao> getUsuarioLocalizacaoServidor(PessoaServidor servidor){
		return usuarioLocalizacaoManager.getUsuarioLocalizacaoServidor(servidor);
	}
	
	/**
	 * Consulta as localizações onde o magistrado é atuante, ou seja, serão retornadas todas as 
	 * localizações onde o cargo é diferente de nulo, pois se o cargo é diferente de nulo então 
	 * a localização foi atribuída devido ao cadastro do magistrado no Orgão Julgador.
	 * 
	 * @param magistrado PessoaMagistrado
	 * @return localizações.
	 */
	public List<UsuarioLocalizacao> consultarLocalizacoesDeMagistradoAtuante(PessoaMagistrado magistrado){
		return usuarioLocalizacaoManager.consultarLocalizacoesDeMagistradoAtuante(magistrado);
	}
	
	/**
	 * Recupera a lista de localizações ativas da pessoa indicada, quando vinculadas a um dado papel.
	 * 
	 * @param pessoa a pessoa cujas localizações se pretende obter
	 * @param papel o papel de interesse
	 * @return a lista de localizações
	 */
	public List<UsuarioLocalizacao> getLocalizacoesAtivas(Pessoa pessoa, Papel papel){
		return usuarioLocalizacaoManager.getLocalizacoesAtuais(pessoa, papel);
	}
	
	public List<UsuarioLocalizacao> getLocalizacoesAtivas(Pessoa pessoa, Papel papel,Localizacao l){
		return usuarioLocalizacaoManager.getLocalizacoesAtuais(pessoa, papel, l);
	}
	
	/**
	 * Método responsável por obter uma lista de {@link UsuarioLocalizacao} de
	 * uma determinada {@link Pessoa} a partir da sua lista de papéis.
	 * 
	 * @param pessoa
	 *            a pessoa cujas localizações se pretende obter
	 * @param papeis
	 *            os papéis de interesse
	 * @return <code>List<code>, de localizações
	 */
	public List<UsuarioLocalizacao> getLocalizacoesAtivas(Pessoa pessoa, List<Papel> papeis){
		return usuarioLocalizacaoManager.getLocalizacoesAtuais(pessoa, papeis);
	}
	
	public List<UsuarioLocalizacao> getLocalizacoesAtuais(){
		if(localizacoesAtuais != null){
			return localizacoesAtuais;
		}else{
			return Collections.emptyList();
		}
	}
	
	public UsuarioLocalizacao getLocalizacaoAtual() throws PJeBusinessException{
		if(localizacaoAtual != null && localizacaoAtual.getIdUsuarioLocalizacao() != 0){
			return usuarioLocalizacaoManager.findById(localizacaoAtual.getIdUsuarioLocalizacao());
		}
		return null;
	}
	
	/**
	 * Prepara o termo de compromisso do usuário do sistema.
	 * 
	 * @param pessoa a pessoa que será cadastrada como usuario
	 * @param advogado marca indicativa de que se trata de usuário advogado
	 * @return o documento preparado para assinatura
	 * @throws PJeBusinessException caso tenha havido algum erro na elaboração do termo
	 * @see Parametros#ID_MODELO_CADASTRO_ADVOGADO
	 * @see Parametros#ID_MODELO_CADASTRO_JUSPOSTULANDI
	 * @see Parametros#ID_TIPO_DOCUMENTO_CADASTRO_ADVOGADO
	 * @see Parametros#ID_TIPO_DOCUMENTO_CADASTRO_JUSPOSTULANDI
	 */
	public DocumentoPessoa preparaTermo(Pessoa pessoa, boolean advogado) throws PJeBusinessException {
		String parametroModelo = null;
		String parametroTipo = null;
		if(advogado){
			parametroModelo = Parametros.ID_MODELO_CADASTRO_ADVOGADO;
			parametroTipo = Parametros.ID_TIPO_DOCUMENTO_CADASTRO_ADVOGADO;
		}else{
			parametroModelo = Parametros.ID_MODELO_CADASTRO_JUSPOSTULANDI;
			parametroTipo = Parametros.ID_TIPO_DOCUMENTO_CADASTRO_JUSPOSTULANDI;
		}
		String idModelo = parametroService.valueOf(parametroModelo);
		if(idModelo == null || idModelo.isEmpty()){
			String msg = String.format("Não foi possível recuperar o valor do parâmetro do modelo de documento [%s].", parametroModelo);
			logger.warn(msg);
			throw new PJeBusinessException(msg);
		}
		String idTipo = parametroService.valueOf(parametroTipo);
		if(idTipo == null || idTipo.isEmpty()){
			String msg = String.format("Não foi possível recuperar o valor do parâmetro do tipo de documento [%s].", parametroModelo);
			logger.warn(msg);
			throw new PJeBusinessException(msg);
		}
		ModeloDocumento md = modeloDocumentoManager.findById(Integer.parseInt(idModelo));
		if(md == null){
			logger.warn("Erro ao tentar recuperar o modelo de comprovante de cadastramento de advogado. Verifique o parâmetro [{0}]", Parametros.ID_MODELO_CADASTRO_ADVOGADO);
			throw new PJeBusinessException("Erro ao recuperar o identificador do modelo de comprovante de cadastramento de advogado.");
		}
		Contexts.getEventContext().set("cadastrante", pessoa);
		String conteudo = modeloDocumentoManager.obtemConteudo(md);
		Contexts.getEventContext().remove("cadastrante");
		TipoProcessoDocumento td = tipoProcessoDocumentoManager.findById(Integer.parseInt(idTipo));
		if(td == null){
			logger.warn("Erro ao tentar recuperar o tipo de documento do comprovante de cadastramento. Verifique o parâmetro [{0}]", Parametros.ID_MODELO_CADASTRO_ADVOGADO);
			throw new PJeBusinessException("Erro ao recuperar o tipo de documento do comprovante de cadastramento.");
		}
		DocumentoPessoa termo = documentoPessoaManager.getDocumentoPessoal();
		termo.setDocumentoHtml(conteudo);
		termo.setPessoa(pessoa);
		termo.setTipoProcessoDocumento(td);
		return termo;
	}
	
	public boolean verificaTermo(Pessoa pessoa, DocumentoPessoa termo) throws Exception{
		return documentoPessoaManager.validaDocumento(pessoa, termo);
	}
	
	public boolean validaDadosCadastrais(Pessoa p, Date dataReferencia){
		if(VerificaCertificado.instance().isModoTesteCertificado() || ConsultaClienteReceitaPFMock.class.isAssignableFrom(Component.getInstance("consultaClienteReceitaPFCNJ").getClass()) || ConfiguracaoIntegracaoCloud.getSSOAuthenticationEnabled()){
			return true;
		}
		if(PessoaFisica.class.isAssignableFrom(p.getClass())){
			return DateUtil.getBeginningOfDay(((PessoaFisica) p).getDataNascimento()).compareTo(DateUtil.getBeginningOfDay(dataReferencia)) == 0;
		}else if(PessoaJuridica.class.isAssignableFrom(p.getClass())){
			PessoaJuridica pj = (PessoaJuridica) p;
			if(pj.getDataAbertura() != null){
				return DateUtil.getBeginningOfDay(pj.getDataAbertura()).compareTo(DateUtil.getBeginningOfDay(dataReferencia)) == 0;
			}else{
				return true;
			}
		}else{
			return true;
		}
	}

	public DocumentoPessoa acrescentaTermo(Pessoa pessoa, DocumentoPessoa termo) throws Exception {
		if(verificaTermo(pessoa, termo)){
			termo.setPessoa(pessoa);
			termo.setUsuarioCadastro(pessoa);
			termo.setSignature(termo.getAssinatura());
			return documentoPessoaManager.persist(termo);
		}else{
			throw new PJeBusinessException("Não foi possível validar o termo de compromisso.");
		}
	}

	/**
	 * Exclui a localização com o nome dado da lista de localizações pessoais da pessoa indicada, assim como o 
	 * papel específico, se existente.
	 * 
	 * @param nomeLocalizacao o nome da localização a ser suprimida
	 * @param pessoa a pessoa de quem as localizações serão suprimidas
	 * @param papel o papel pretensamente associado à localização. 
	 * @return
	 * @throws PJeBusinessException
	 */
	@SuppressWarnings("deprecation")
	public boolean excluiLocalizacaoPessoal(String nomeLocalizacao, PessoaFisica pessoa, Papel papel) throws PJeBusinessException {
		Localizacao loc = localizacaoManager.getLocalizacaoExistente(nomeLocalizacao);
		if(loc == null){
			return false;
		}
		PessoaLocalizacao pl = pessoaLocalizacaoManager.recuperaUnivoca(pessoa, loc);
		if(pl != null){
			pessoaLocalizacaoManager.remove(pl);
		}
		UsuarioLocalizacao ul = usuarioLocalizacaoManager.getLocalizacao(pessoa, loc, papel);
		if(ul != null){
			Session sessaoTemp = HibernateUtil.getSession();
			sessaoTemp.lock(pessoa, LockMode.NONE);
			Hibernate.initialize(pessoa.getUsuarioLocalizacaoList());
			
			usuarioLocalizacaoManager.remove(ul);
			pessoa.getUsuarioLocalizacaoList().remove(ul);
			if(!usuarioLocalizacaoManager.mantemPapel(pessoa, papel)){
				pessoa.getPapelSet().remove(papel);
			}
		}
		return true;
	}
	
	/**
	 * Este método efetua a exclusão de uma lista de localizações 
	 * @param usuarioLocalizacaoList
	 * @return
	 */
	public Boolean exlcuiLocalizacoesMagistradoServidor(List<UsuarioLocalizacao> usuarioLocalizacaoList){
		try {
			for(UsuarioLocalizacao usuLocList : usuarioLocalizacaoList){
					excluiVisibilidadesUsuarioLocalizacaoMagistradoServidor(usuLocList.
																			getUsuarioLocalizacaoMagistradoServidor().
																			getUsuarioLocalizacaoVisibilidadeList());
					usuarioLocalizacaoManager.remove(usuLocList);

			}
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Ocorreu um erro ao tentar remover as localizações do servidor.");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Este método executa a exclusão de uma lista de visibilidades
	 * @param visibilidades
	 * @return
	 */
	public Boolean excluiVisibilidadesUsuarioLocalizacaoMagistradoServidor(List<UsuarioLocalizacaoVisibilidade> visibilidades){
		try {
			for(UsuarioLocalizacaoVisibilidade usuLocVis : visibilidades){
					usuarioLocalizacaoVisibilidadeManager.remove(usuLocVis);
			}
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Ocorreu um erro ao tentar remover as visibilidades do servidor.");
			return false;
		}
		return true;		
	}

	public List<PessoaFisica> getServidores(OrgaoJulgador orgaoJulgador) throws PJeBusinessException {
		return pessoaFisicaManager.getServidores(orgaoJulgador);
	}
	
	public List<PessoaFisica> getServidores(OrgaoJulgadorColegiado orgaoJulgador) throws PJeBusinessException{
		return pessoaFisicaManager.getServidores(orgaoJulgador);
	}
	
	/**
	 * Verifica se usuário está logado no sistema com o papel de assistente (assistente de advogado, assistente de procurador)
	 * @return true: Possui perfil de assistente; false: Não possui perfil de assistente. 
	 */
	public boolean perfilAssistente() {
		UsuarioLocalizacao usuarioLocalizacao = Authenticator.getUsuarioLocalizacaoAtual();
		
		return usuarioLocalizacao instanceof PessoaAssistenteAdvogadoLocal ||
				usuarioLocalizacao instanceof PessoaAssistenteProcuradoriaLocal;
	}
	
	/**
	 * Verifica se o assistente pode assinar.
	 * @return true: Pode assinar; false: Não pode assinar.
	 */
	public boolean assistentePodeAssinar() {
		UsuarioLocalizacao usuarioLocalizacao = Authenticator.getUsuarioLocalizacaoAtual();
		
		if(usuarioLocalizacao instanceof PessoaAssistenteAdvogadoLocal){
			return ((PessoaAssistenteAdvogadoLocal) usuarioLocalizacao).getAssinadoDigitalmente();
		}
		
		return false;
	}
	
	/**
	 * Envia um email ao usuario com o link para cadastro de nova senha.
	 * @param UsuarioLogin: usuarioLogin
	 * @throws PJeBusinessException
	 */
	public void enviarEmailSenha(UsuarioLogin usuarioLogin) throws PJeBusinessException{
		if(usuarioLogin.getEmail() == null || usuarioLogin.getEmail().trim().isEmpty()){
			throw new PJeBusinessException("pje.usuarioService.error.informeEmailUsuario");
		}
		
		String idModeloEmailSenha = parametroService.valueOf("idModeloEMailMudancaSenha");
		if(idModeloEmailSenha == null || idModeloEmailSenha.trim().isEmpty()){
			throw new PJeBusinessException("pje.usuarioService.error.idModeloEMailMudancaSenhaNaoEncontrado");
		}		
		
		ModeloDocumento modeloDocumentoEmail = modeloDocumentoManager.findById(Integer.parseInt(idModeloEmailSenha));
		if(modeloDocumentoEmail == null){
			throw new PJeBusinessException("pje.usuarioService.error.modeloEmailNaoEncontrado");
		}
		
		String body = modeloDocumentoEmail.getModeloDocumento();
		String urlAtivacaoSenha = usuarioLogin.getUrlAtivacaoSenha(new Util().getUrlProject());
		Contexts.getEventContext().set("urlAtivacaoSenha", urlAtivacaoSenha);
		
		// processa o modelo avaliando as expressões
		body = (String) Expressions.instance().createValueExpression(body).getValue();

		emailService.enviarEmail(usuarioLogin, "Cadastro de Senha", body);
	}
	
	/**
	 * Envia um email a pessoa fisica especializada com o link para cadastro de nova senha.
	 * @param PessoaFisicaEspecializada: pessoaFisicaEspecializada
	 * @throws PJeBusinessException
	 */
	public void enviarEmailSenha(PessoaFisicaEspecializada pessoaFisicaEspecializada) throws PJeBusinessException{
		if(pessoaFisicaEspecializada.getEmail() == null || pessoaFisicaEspecializada.getEmail().trim().isEmpty()){
			throw new PJeBusinessException("pje.usuarioService.error.informeEmailUsuario");
		}
		
		String idModeloEmailSenha = parametroService.valueOf("idModeloEMailMudancaSenha");
		if(idModeloEmailSenha == null || idModeloEmailSenha.trim().isEmpty()){
			throw new PJeBusinessException("pje.usuarioService.error.idModeloEMailMudancaSenhaNaoEncontrado");
		}		
		
		ModeloDocumento modeloDocumentoEmail = modeloDocumentoManager.findById(Integer.parseInt(idModeloEmailSenha));
		if(modeloDocumentoEmail == null){
			throw new PJeBusinessException("pje.usuarioService.error.modeloEmailNaoEncontrado");
		}
		
		String body = modeloDocumentoEmail.getModeloDocumento();
		String urlAtivacaoSenha = pessoaFisicaEspecializada.getUrlAtivacaoSenha(new Util().getUrlProject());
		Contexts.getEventContext().set("urlAtivacaoSenha", urlAtivacaoSenha);
		
		// processa o modelo avaliando as expressões
		body = (String) Expressions.instance().createValueExpression(body).getValue();
		
		UsuarioLogin usuarioLogin = new UsuarioLogin();
		usuarioLogin.setNome(pessoaFisicaEspecializada.getNome());
		usuarioLogin.setEmail(pessoaFisicaEspecializada.getEmail());
		
		emailService.enviarEmail(usuarioLogin, "Cadastro de Senha", body);
	}
	
	public void revokeSSOPassword(String username) throws PJeBusinessException {
		if(PjeUtil.instance().isSSOAuthenticationEnabled()) {
			KeycloakServiceClient keycloakServiceClient = ComponentUtil.getComponent(KeycloakServiceClient.NAME);
			keycloakServiceClient.removePasswordCredentialsFrom(username);
		}
	}
	
	/**
	 * Retorna o papel relacionado à localização atual do usuário.
	 * @return papel
	 * @throws PJeBusinessException
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-22393
	 */
	public Papel getPapelLocalizacaoAtual() throws PJeBusinessException{
		UsuarioLocalizacao localizacaoAtual = getLocalizacaoAtual();
		if(localizacaoAtual != null){
			return localizacaoAtual.getPapel();	
		}else{
			return null;
		}
	}

	public void enviarEmailCodigoPareamento(UsuarioMobile usuarioMobile) throws PJeBusinessException, Exception{
		if(usuarioMobile.getUsuario().getEmail() == null || usuarioMobile.getUsuario().getEmail().trim().isEmpty()){
			throw new PJeBusinessException("pje.usuarioService.error.informeEmailUsuario");
		}
		
		String pass = null;
		try {
			pass = usuarioMobileManager.gerarTokenContador(usuarioMobile.getCodigoPareamento());
		} catch (Exception e) {
			throw new PJeBusinessException(e);
		}
		String body = "<p>"+pass+"</p>";
		
		UsuarioLogin usuarioLogin = new UsuarioLogin();
		usuarioLogin.setNome(usuarioMobile.getUsuario().getNome());
		usuarioLogin.setEmail(usuarioMobile.getUsuario().getEmail());

		emailService.enviarEmail(usuarioLogin, "Código de pareamento PJe Mobile", body);
	}
	
	@Observer({Eventos.EVENTO_LOGIN_SSO_REGISTRAR})
	public void registraLogonSSO(ParametroEventoRegistroLoginVO parametroVO) {
		try {
			if(parametroVO != null && parametroVO.getIdUsuario() != null){
				UsuarioLogin usuario = this.usuarioLoginManager.getReference(parametroVO.getIdUsuario());
				if(parametroVO.isInicializaFalhasAutenticacao()) {
					usuario.setFalhasSucessivas(0);
				}
				if(parametroVO.isDeveBloquearSenha()) {
					bloqueiaSenhaUsuario(usuario);
				}
				this.logAcessoManager.registrarTentativaLogon(usuario, true, parametroVO.getIp(), parametroVO.isLogouComCertificado());
				if (parametroVO.isLogouComCertificado() && !parametroVO.isTemCertificado()) {
					 this.usuarioLoginManager.registrarLogOnCertificado(usuario);
				}
			}
		} catch (PJeBusinessException e) {
			logger.error(e.getLocalizedMessage());
		}
	}
	
	public void registraFalhaLogon(UsuarioLogin usuario) throws PJeBusinessException {
		if (usuario != null) {
			if (usuario.getStatusSenha() != StatusSenhaEnum.B) {
				String idsUsuarioNaoBloquear = ParametroUtil.getParametro("pje:idsUsuario:naoBloquear");
				if (idsUsuarioNaoBloquear == null || idsUsuarioNaoBloquear.isEmpty() || !Arrays.asList(idsUsuarioNaoBloquear.split(",")).contains(usuario.getIdUsuario().toString())) {
					this.usuarioLoginManager.adicionaFalhasAutenticacao(usuario);
				}
			}
			this.logAcessoManager.registrarTentativaLogon(usuario, false);
			if (bloqueiaSenhaUsuario(usuario)) {
				throw new PJeBusinessException("A senha foi bloqueada. Solicite uma nova senha para efetuar o desbloqueio");
			}
		}
	}
	
	public void normalizaCadastroSSO(UsuarioLogin usuario) throws PJeBusinessException {
		if(usuario != null) {
			this.usuarioLoginManager.normalizaCadastroSSO(usuario);
		}
	}	
	
	private boolean bloqueiaSenhaUsuario(UsuarioLogin usuario) {
		boolean retorno = false;
		try {
			if (usuario.getStatusSenha() == StatusSenhaEnum.A && 
				usuario.getFalhasSucessivas() >= Constantes.MAXIMO_FALHAS_SUCESSIVAS_LOGIN) {
				
				this.usuarioLoginManager.bloquearSenhaUsuario(usuario);
				retorno = true;
			} else if (usuario.getStatusSenha() == StatusSenhaEnum.B) {
				retorno = true;
			}
		} catch (PJeBusinessException e) {
			logger.error(e.getLocalizedMessage());
		}
		return retorno;
	}
	
	public InformacaoUsuarioSessao recuperarInformacaoUsuarioLogado(){
 		InformacaoUsuarioSessao info = new InformacaoUsuarioSessao();
 		info.setIdsLocalizacoesFisicasFilhas(Authenticator.getIdsLocalizacoesFilhasAtuaisList());
		info.setIdLocalizacaoFisica(Authenticator.getLocalizacaoFisicaAtual().getIdLocalizacao());
		info.setIdLocalizacaoModelo(Authenticator.getIdLocalizacaoModeloAtual());
		if (Authenticator.getIdOrgaoJulgadorAtual() !=null){
			info.setIdOrgaoJulgador(Authenticator.getIdOrgaoJulgadorAtual());
		}
		if (Authenticator.getIdOrgaoJulgadorColegiadoAtual() !=null){
			info.setIdOrgaoJulgadorColegiado(Authenticator.getIdOrgaoJulgadorColegiadoAtual());
		}
		List<Integer> idsOrgaoJulgadorCargo = new ArrayList<Integer>();
		UsuarioLocalizacaoMagistradoServidor usu = Authenticator.getUsuarioLocalizacaoMagistradoServidorAtual();
		if(usu != null){
			info.setIdUsuarioLocalizacaoMagistradoServidor(usu.getIdUsuarioLocalizacaoMagistradoServidor());
			Date agora = new Date();
			for(UsuarioLocalizacaoVisibilidade vis : usu.getUsuarioLocalizacaoVisibilidadeList()){
				if(vis.getDtInicio().after(agora) || (vis.getDtFinal() != null && vis.getDtFinal().before(agora))){
					continue;
				}

				if(vis.getOrgaoJulgadorCargo() == null){
					idsOrgaoJulgadorCargo = new ArrayList<Integer>(0);
					break;
				}
				else{
					idsOrgaoJulgadorCargo.add(vis.getOrgaoJulgadorCargo().getIdOrgaoJulgadorCargo());
				}
			}

			info.setIdsOrgaoJulgadorCargoVisibilidade(idsOrgaoJulgadorCargo);

			if(Authenticator.isMagistrado()){
				if(usu.getOrgaoJulgadorCargo() != null && usu.getOrgaoJulgadorCargo().getAuxiliar()){
					info.setCargoAuxiliar(true);
				}
			}
		}

		info.setIdPapel(Authenticator.getIdPapelAtual());
		info.setIdUsuario(Authenticator.getIdUsuarioLogado());
		info.setVisualizaSigiloso(Authenticator.isVisualizaSigiloso());
		info.setNivelAcessoSigilo(Authenticator.recuperarNivelAcessoUsuarioLogado());
		Usuario u = (Usuario)Contexts.getSessionContext().get(Authenticator.USUARIO_LOGADO);
		info.setNomeUsuario(u.getNome());
		info.setLogin(u.getLogin());
		info.setPapelNaoFiltravel(ControleFiltros.isPapeisExternosNaoFiltraveis());
		return info;
	}	

	@Observer(Eventos.EVENTO_ATUALIZAR_CADASTRO_SSO_USUARIO)
	public void atualizarCadastroSSOUsuario(Integer idUsuario) {
		this.usuarioManager.marcarFlagAtualizaSSO(idUsuario);
	}
	
}
