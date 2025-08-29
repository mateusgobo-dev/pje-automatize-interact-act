package br.com.infox.ibpm.home;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioManager;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.pje.nucleo.entidades.BloqueioUsuario;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;

@Name("usuarioHome")
@BypassInterceptors
public class UsuarioHome extends AbstractUsuarioHome<Usuario> {

	public static final String AFTER_SET_USUARIO_LOCALIZACAO_ATUAL_EVENT = "br.com.infox.ibpm.home.UsuarioHome.afterSetLocalizacaoAtual";
	private static final long serialVersionUID = 1L;
	public static final String USUARIO_LOCALIZACAO_ATUAL = "usuarioLogadoLocalizacaoAtual";

	private String login;
	private String password;
	private String passwordConfirm;
	private String email;
	private BloqueioUsuario ultimoBloqueio;
	private BloqueioUsuario novoBloqueio = new BloqueioUsuario();

	public BloqueioUsuario getUltimoBloqueio() {
		return ultimoBloqueio;
	}

	public BloqueioUsuario getNovoBloqueio() {
		return novoBloqueio;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public void setPasswordConfirm(String passwordConfirm) {
		this.passwordConfirm = passwordConfirm;
	}

	public String getPasswordConfirm() {
		return passwordConfirm;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	private void validarBloqueio() {
		if (getInstance().getBloqueio()
				&& (novoBloqueio.getDataPrevisaoDesbloqueio() == null || novoBloqueio.getMotivoBloqueio().equals(""))) {
			getInstance().setBloqueio(false);
			this.novoBloqueio = new BloqueioUsuario();
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Campo bloqueio preenchido incorretamente");
		}
	}

	@Override
	public void newInstance() {
		super.newInstance();
		getInstance().setAtivo(true);
		getInstance().setBloqueio(false);
	}

	@Override
	protected Usuario createInstance() {
		Usuario usuario = super.createInstance();
		usuario.setAtivo(true);
		return usuario;
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (changed) {
			login = getInstance().getLogin();
		}
	}

	@Override
	public String update() {
		if(beforePersistOrUpdate()){
			validarBloqueio();
			
			try {
				ComponentUtil.getComponent(UsuarioManager.class).persistAndFlush(getInstance());
			} catch (PJeBusinessException e) {
				reportMessage(e);
			}
			
			return afterPersistOrUpdate("update");
		}
		
		return null;
	}

	@Override
	public String persist() {		
		if(beforePersistOrUpdate()){
			try {
				ComponentUtil.getComponent(UsuarioManager.class).persistAndFlush(getInstance());
				setInstance(getInstance());
				login = getInstance().getLogin();
				return afterPersistOrUpdate("persisted");
			} catch (PJeBusinessException e) {
				reportMessage(e);
			}			
		}
		
		return null;
	}

	/**
	 * Metodo que gera uma nova senha para usuário. Este metodo faz isso
	 * buscando na base do wiacs o usuário pelo login e email e retorna uma
	 * mensagem de erro caso não encontre. A partir do usuário do wiacs é dado
	 * um setId utilizando a 'identificacao'.
	 * 
	 * @throws LoginException
	 */
	public void requisitarNovaSenha() throws LoginException {
		FacesMessages fm = FacesMessages.instance();
		if (StringUtils.isEmpty(email) || StringUtils.isEmpty(login)) {
			fm.add("É preciso informar o CPF/CNPJ e o email usuário");
		} else {
			UsuarioManager usuarioManager = ComponentUtil.getComponent(UsuarioManager.class);
			
			Usuario usuario = usuarioManager.findByLogin(login);
			if (usuario == null) {
				fm.add("Usuário não encontrado");
			} else {
				if (StringUtils.isEmpty(usuario.getEmail())) {
					fm.add("Usuário não possui email cadastrado");
				} else if (usuario.getEmail().equals(email)) {
					try {
						ComponentUtil.getComponent(UsuarioService.class).revokeSSOPassword(login);
						usuario.setHashAtivacaoSenha(PjeUtil.instance().gerarHashAtivacao(usuario.getLogin()));
						usuarioManager.persistAndFlush(usuario);
						ComponentUtil.getComponent(UsuarioService.class).enviarEmailSenha(usuario);
						fm.add("Um link para alteração de senha foi enviado por email");
					} catch (PJeBusinessException e) {
						reportMessage(e);
					}
				} else {
					fm.add("Senha não gerada. O email informado é diferente daquele existente na base do PJE");
				}
			}
		}
	}

	public static UsuarioHome instance() {
		return ComponentUtil.getComponent("usuarioHome");
	}

	/**
	 * Atalho para a localização atual
	 * 
	 * @return a localização atual do usuário
	 */
	public static UsuarioLocalizacao getUsuarioLocalizacaoAtual() {
		UsuarioLocalizacao usuLoc = (UsuarioLocalizacao) Contexts.getSessionContext().get(USUARIO_LOCALIZACAO_ATUAL);
		if(usuLoc == null){
			return null;
		}else{
			return EntityUtil.getEntityManager().find(usuLoc.getClass(), usuLoc.getIdUsuarioLocalizacao());
		}
	}
	
	
	/**
	 * Método responsável por retornar o nome do usuário logado.
	 * 
	 * @param consideraOrgaoProcuradoria Caso verdadeiro, no retorno do método constará também o nome da procuradoria 
	 * do qual o usuário logado faz parte.
	 * @return Exemplo: <b>José da Silva - Representação do CNJ</b> ou apenas <b>José da Silva</b>.
	 * 
	 * @see <a href="http://www.cnj.jus.br/jira/browse/PJEII-19546">PJEII-19546</a>
	 * @deprecated utilizar a função getNomeUsuarioCompleto
	 */
	@Deprecated
	public String getNomeUsuarioLogado(boolean consideraOrgaoProcuradoria) {
		Usuario usuarioLogado = Authenticator.getPessoaLogada();
		StringBuilder nome = new StringBuilder(usuarioLogado.getNome());
		
		if (consideraOrgaoProcuradoria == true) {
			Procuradoria procuradoriaAtual = Authenticator.getProcuradoriaAtualUsuarioLogado();
			if (procuradoriaAtual != null) {
				nome.append(" - " + procuradoriaAtual.getNome());
			}			
		}
		
		return nome.toString();
	}
	
	/**
	 * Método responsável por retornar o nome do usuário com a informação da procuradoria se houver
	 * 
	 * @param usuario
	 * @param localizacao
	 * @param papel
	 * @return nomeUsuarioComReferencia
	 * 
	 * @see <a href="http://www.cnj.jus.br/jira/browse/PJEII-19546">PJEII-19546</a>
	 */
	public String getNomeUsuarioCompleto(Usuario usuario, Localizacao localizacao, Papel papel) {
		StringBuilder nome = new StringBuilder(usuario.getNome());
		
		if(Authenticator.isProcurador(papel)) {
			ProcuradoriaManager procuradoriaManager = ComponentUtil.getComponent(ProcuradoriaManager.class);
			Procuradoria procuradoriaUsuario = procuradoriaManager.recuperaPorLocalizacao(localizacao);
			if(procuradoriaUsuario != null) {
				nome.append(" - " + procuradoriaUsuario.getNome());
			}
		}
		
		return nome.toString();
	}
}