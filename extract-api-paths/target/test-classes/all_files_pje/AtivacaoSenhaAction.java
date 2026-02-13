package br.jus.cnj.pje.view;

import java.io.IOException;
import java.io.Serializable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.core.SeamResourceBundle;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.component.Util;
import br.jus.cnj.pje.nucleo.Constants;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.UsuarioManager;
import br.jus.pje.nucleo.entidades.Usuario;

@Name(AtivacaoSenhaAction.NAME)
@Scope(ScopeType.PAGE)
public class AtivacaoSenhaAction implements Serializable{	

	private static final long serialVersionUID = -3289406935152231640L;
	

	public static final String NAME = "ativacaoSenhaAction";	
	
	@RequestParameter("hashCodigoAtivacao")
	String hashCodigoAtivacao;	
	
	@RequestParameter("login")
	String login;
	
	private String novaSenha1;
	private String novaSenha2;
	private Usuario usuario;
	private boolean permiteEdicao;
	
	private boolean sucessoGerarSenha;

	@Create
	public void init(){
		if(login != null && hashCodigoAtivacao != null){
			UsuarioManager usuarioManager = (UsuarioManager) Component.getInstance(UsuarioManager.NAME);
			usuario = usuarioManager.findUsuarioHashAtivacao(login, hashCodigoAtivacao);
		}else{		
			usuario = null;
		}
	}
	
	public void cadastrarNovaSenha(){
		if (usuario == null){
			FacesMessages.instance().add(Severity.ERROR, "Nao foi possivel encontrar o usuario com o login e o codigo de ativacao especificados");
			return;
		}
		
		if (getNovaSenha1() == null || getNovaSenha1().trim().equals("")){
			FacesMessages.instance().add(Severity.WARN, "Informe a senha a ser cadastrada");
			return;
		}
		
		if (getNovaSenha2() == null || getNovaSenha2().trim().equals("")){
			FacesMessages.instance().add(Severity.WARN, "Digite a senha novamente para confirmacao");
			return;
		}
		
		if (!getNovaSenha1().equals(getNovaSenha2())){
			FacesMessages.instance().add(Severity.WARN, "As senhas não são coincidentes");
			return;
		}
		
		try {
			UsuarioManager usuarioManager = (UsuarioManager) Component.getInstance(UsuarioManager.NAME);
			usuarioManager.ativarSenha(usuario.getLogin(), usuario.getHashAtivacaoSenha(), getNovaSenha1());
			Util.commitTransction();
			FacesMessages.instance().add(Severity.INFO, "Senha ativada com sucesso.");
			sucessoGerarSenha = true;
		} catch (PJeBusinessException e) {
			reportMessage(e);
		} catch(Exception e2){
			FacesMessages.instance().add(Severity.ERROR, "Erro ao cadastrar a senha. "+e2.getLocalizedMessage());
		}
	}
	
	private void reportMessage(PJeBusinessException ex){
		ResourceBundle bundle = SeamResourceBundle.getBundle();
		String message = null;
		try{
			message = bundle.getString(ex.getCode());
		} catch (MissingResourceException e){
			e.printStackTrace();
			message = ex.getCode();
		}

		if (!FacesMessages.instance().getCurrentMessages().contains(message)
			&& !FacesMessages.instance().getCurrentGlobalMessages()
					.contains(message)){
			
			if (ex.getCode().contains(Constants.PREFIXO_ERROR)){
				FacesMessages.instance().addFromResourceBundle(Severity.ERROR, ex.getCode(), ex.getParams());
			}
			else if (ex.getCode().contains(Constants.PREFIXO_INFO)){
				FacesMessages.instance().addFromResourceBundle(Severity.INFO, ex.getCode(), ex.getParams());
			}
			else if (ex.getCode().contains(Constants.PREFIXO_WARN)){
				FacesMessages.instance().addFromResourceBundle(Severity.WARN, ex.getCode(), ex.getParams());
			}
			else if (ex.getCode().contains(Constants.PREFIXO_FATAL)){
				FacesMessages.instance().addFromResourceBundle(Severity.FATAL, ex.getCode(), ex.getParams());
			}
			else{
				FacesMessages.instance().addFromResourceBundle(Severity.WARN, ex.getCode(), ex.getParams());
			}
		}
	}
	
	public String getLogin() {
		return login;
	}
	
	public void setLogin(String login) {
		this.login = login;
	}
	
	public String getNovaSenha1() {
		return novaSenha1;
	}
	
	public void setNovaSenha1(String novaSenha1) {
		this.novaSenha1 = novaSenha1;
	}
	
	public String getNovaSenha2() {
		return novaSenha2;
	}
	
	public void setNovaSenha2(String novaSenha2) {
		this.novaSenha2 = novaSenha2;
	}
	
	public Usuario getUsuario() {
		return usuario;
	}
	
	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
	
	public boolean isPermiteEdicao() {
		return permiteEdicao;
	}
	
	public void setPermiteEdicao(boolean permiteEdicao) {
		this.permiteEdicao = permiteEdicao;
	}
	
	public String getLoginUsuario(){
		return (usuario != null ? usuario.getLogin() : "nao encontrado");
	}
	
	public String getNomeUsuario(){
		return (usuario != null ? usuario.getNome() : "");		
	}
	
	public String getOrientacoesUsuario(){
		if(usuario != null){
			return "Prezado(a) "+getNomeUsuario()+
					", para ativar seu cadastro, insira uma nova senha contendo letras, numeros e tamanho entre 8 e 64 caracteres.";
		}
		
		return "Usuario nao encontrado";
	}

	public boolean isSucessoGerarSenha() {
		return sucessoGerarSenha;
	}

	public void redirectPje() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
		try {
			response.sendRedirect(new Util().getUrlProject());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			facesContext.responseComplete();
		}
	}
}
