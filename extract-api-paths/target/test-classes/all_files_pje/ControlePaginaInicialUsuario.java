package br.com.infox.ibpm.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.component.ControleFiltros;
import br.com.infox.cliente.home.QuadroAvisoHome;
import br.com.infox.cliente.home.SessaoHome;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.list.QuadroAvisoPapelMensagemList;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.extensao.AssinadorA1;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.identity.PjeSessionCache;
import br.jus.cnj.pje.nucleo.manager.DocumentoPessoaManager;
import br.jus.cnj.pje.view.CadastroUsuarioAction;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.pje.nucleo.entidades.DocumentoPessoa;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.QuadroAviso;
import br.jus.pje.nucleo.enums.StatusSenhaEnum;


@Name(ControlePaginaInicialUsuario.NAME)
@Scope(ScopeType.EVENT)
public class ControlePaginaInicialUsuario implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String NAME = "controlePaginaInicialUsuario";
    @In(create = true, required = false)
	private AssinadorA1 assinadorA1;
    @In
    private DocumentoPessoaManager documentoPessoaManager;
    private String tarefaInicial = "";    

    @End(beforeRedirect = true)
    public void redirectToPainel() {
        Context conversationContext = Contexts.getConversationContext();
        String[] names = conversationContext.getNames();

        for (String name : names) {
            if (name.toLowerCase().indexOf("grid") >= 0) {
                conversationContext.remove(name);
            }
        }
        
        PjeSessionCache pjeSessionCache = (PjeSessionCache)Component.getInstance(PjeSessionCache.class);
        pjeSessionCache.removeAll();

        Boolean exibeQuadroAviso = false;
        List<QuadroAviso> quadroAvisos = new ArrayList<QuadroAviso>(0);
        if (Authenticator.getPessoaPushLogada() == null) {  // Para pessoa push o quadro de avisos não é exibido.
            QuadroAvisoPapelMensagemList quadroAvisoPapelMensagemList = new QuadroAvisoPapelMensagemList();
            quadroAvisos = quadroAvisoPapelMensagemList.list();
            for (QuadroAviso quadroAviso : quadroAvisos) {
                if (QuadroAvisoHome.instance().exibeAviso(quadroAviso)) {
                	exibeQuadroAviso = true;
                	break;
                }
            }
        }
        getPaginas(quadroAvisos.size() > 0 && exibeQuadroAviso);
    }
    
    @End(beforeRedirect = true)
    public void redirectToSSOCadastroCallback() {
    	Redirect redirect = Redirect.instance();
    	redirect.setViewId("/publico/usuario/ssoCadastroCallback.xhtml");
        redirect.execute();
    }
    
    @End(beforeRedirect = true)
    public void redirectToSSOCallback() {
    	Redirect redirect = Redirect.instance();
    	redirect.setViewId("/publico/usuario/ssoCallback.seam");
        redirect.execute();
    }    

    public void getPaginas(boolean exibeQuadroAvisos) {
    	Authenticator.instance().setPossuiPendenciaCadastro(false);
        Pessoa pessoaLogada = Authenticator.getPessoaLogada();
        ControleFiltros.instance().iniciarFiltro();
        Redirect redirect = Redirect.instance();

        if (assinadorA1!=null && !Authenticator.isLogouComCertificado() && !Authenticator.isTokenValido() && !Authenticator.isCancelouToken() && Authenticator.isUsuarioMobile()) {
        	redirect.setViewId("/publico/usuario/token.seam");
            redirect.execute();
        } else if (Authenticator.isAdvogado()) {
        	PessoaAdvogado adv = ((PessoaFisica) pessoaLogada).getPessoaAdvogado();
        	redirecionarAdvogado(adv, exibeQuadroAvisos);
        } else if (exibeQuadroAvisos) {
            redirect.setViewId("/QuadroAviso/listViewQuadroAvisoMensagem.seam");
            redirect.execute();
        } else if ((pessoaLogada == null) || Authenticator.isPapelAdministrador()) {
            redirect.setViewId("/home.seam");
            redirect.execute();
            redirect.setViewId(null);
        } else if (Authenticator.isPapelAtualMagistrado()) {
       		redirect.setConversationPropagationEnabled(false);
            redirect.setViewId(recuperaPainelUsuario());
            redirect.execute();
            pessoaLogada.setUltimoPainelSelecionado("painel_usuario/Painel_Usuario_Magistrado/listView.seam");
        } else if (Authenticator.isProcurador()) {
            if (ParametroJtUtil.instance().justicaFederal() && 
            		SessaoHome.instance().verificaExitenciaSessaoAbertaVinculada(Authenticator.getUsuarioLogado())) {
            	
            	redirect.setConversationPropagationEnabled(false);
                redirect.setViewId("/Painel/ProcuradorMP/sessaoAbertaProcuradorMP.seam");
                redirect.execute();
            } else {
            	redirect.setConversationPropagationEnabled(false);
                redirect.setViewId("/Painel/painel_usuario/advogado.seam");
                redirect.execute();
            }
        } else if (Authenticator.isAssistenteAdvogado() || Authenticator.isAssistenteProcurador()) {
        	redirect.setViewId("/Painel/painel_usuario/advogado.seam");
            redirect.execute();
        } else if (Authenticator.isPapelAssessor()) {
        	redirect.setConversationPropagationEnabled(false);
            redirect.setViewId(recuperaPainelUsuario());
            redirect.execute();
        } else if (Authenticator.isPapelAtual(Papeis.PJE_SERVIDOR_MALOTE)) {
            redirect.setViewId("/Painel/painel_usuario/usuarioSetorMalote.seam");
            redirect.execute();
        } else if (Authenticator.isPapelOficialJustica() || Authenticator.isPapelOficialJusticaDistribuidor()) {
            redirect.setViewId("/Painel/painel_usuario/Paniel_Usuario_Oficial_Justica/listView.seam");
            redirect.execute();
        } else if (Authenticator.isPerito()) {
            redirect.setViewId("/Painel/Perito/listView.seam");
            redirect.execute();
        } else if (Authenticator.isSecretarioSessao()) {
            redirect.setViewId("/Painel/SecretarioSessao/painelSecretarioSessao.seam");
            redirect.execute();
        } else if (Authenticator.isJusPostulandi()) {
        	redirecionarJusPostulandi(pessoaLogada, exibeQuadroAvisos);
        } else if (Authenticator.isUsuarioInterno()) {
        	redirect.setConversationPropagationEnabled(false);
        	redirect.setViewId(recuperaPainelUsuario());
        	redirect.execute();
        	pessoaLogada.setUltimoPainelSelecionado("/ng2/dev.seam#/painel-usuario-interno");
    	} else {
            redirect.setViewId("/home.seam");
            redirect.execute();
        }
    }

    /**
	 * Método verifica se advogado já assinou o termo de compromisso 
	 * antes de encaminhá-lo para o painel do usuário.  
	 *   
	 * @param pessoaLogada Pessoa logado no sistma
	 */
	public void redirecionarAdvogado(PessoaAdvogado pessoaLogada, boolean exibeQuadroAvisos) {
		DocumentoPessoa docTermoCompromisso = obterTermoCompromissoAdvogado(pessoaLogada);					

		boolean usuarioMigrado = StatusSenhaEnum.M.equals(pessoaLogada.getStatusSenha());

		if (usuarioMigrado || docTermoCompromisso == null
				|| !documentoPessoaManager.termoCompromissoAssinado(docTermoCompromisso)) {
			Authenticator.instance().setPossuiPendenciaCadastro(true);
			if(Authenticator.isLogouComCertificado()) {
				redirecionarAdvogadoParaAssinarTermo();
			} else {
				redirecionarAvisoNaoAcesso();
			}
		} else if (exibeQuadroAvisos) {
			redirecionarAdvogadoParaQuadroAviso();
		} else {
			redirecionarAdvogadoParaPainel();
		}
	}

    /**
	 * Método verifica se jusPostulandi já assinou o termo de compromisso 
	 * antes de encaminhá-lo para o painel do usuário.  
	 *   
	 * @param pessoaLogada Pessoa logado no sistema
	 */
	public void redirecionarJusPostulandi(Pessoa pessoaLogada, boolean exibeQuadroAvisos) {
		DocumentoPessoa docTermoCompromisso = documentoPessoaManager.getUltimoTermoCompromissoJusPostulandi(pessoaLogada);					

		boolean usuarioMigrado = StatusSenhaEnum.M.equals(pessoaLogada.getStatusSenha());

		if (usuarioMigrado || docTermoCompromisso == null
				|| !documentoPessoaManager.termoCompromissoAssinado(docTermoCompromisso)) {
			Authenticator.instance().setPossuiPendenciaCadastro(true);
			if(Authenticator.isLogouComCertificado()) {
				redirecionarAdvogadoParaAssinarTermo();
			} else {
				redirecionarAvisoNaoAcesso();
			}
		} else {
			redirecionarAdvogadoParaPainel();
		}
	}
	
	/**
	 * Obtem o termo de compromisso do advogado logado
	 * ou cria um se ainda não existir
	 * 
	 * @param pessoaLogada Pessoa logada no sistma
	 * @return documento com termo de compromisso
	 */
	private DocumentoPessoa obterTermoCompromissoAdvogado(PessoaAdvogado pessoaLogada) {
		
		DocumentoPessoa docTermoCompromisso = documentoPessoaManager.getUltimoTermoCompromisso(pessoaLogada);
		if (docTermoCompromisso == null) {				
			docTermoCompromisso = gerarTermoDeCompromisso(pessoaLogada);
		}
		return docTermoCompromisso;
	}

	/**
	 * Gerar termo de compromisso do advogado passado como parâmetro
	 * @param pessoaLogada advogado para quem será gerado o termo 
	 * @return documento do termo de compromisso gerado
	 */
	private DocumentoPessoa gerarTermoDeCompromisso(PessoaAdvogado pessoaLogada) {
		incluirVariaveisContexto(pessoaLogada);
		return documentoPessoaManager.gerarTermoDeCompromisso(pessoaLogada);
	}
	
	/**
	 * Carreaga variáveis no contexto pois a chamada gerarTermoDeCompromisso utiliza estas variáveis
	 * @param pessoaLogada
	 */
	private void incluirVariaveisContexto(PessoaAdvogado pessoaLogada) {
		Contexts.getConversationContext().set(Variaveis.DATA_NASCIMENTO_CADASTRO, pessoaLogada.getDataNascimento());
		Contexts.getConversationContext().set(Variaveis.INSCRICAO_MF_CADASTRO, pessoaLogada.getNumeroCPF());
	}

	/**
	 * Método redireciona advogado para o painel do advogado.
	 */
	private void redirecionarAdvogadoParaPainel() {
		Redirect redirect = Redirect.instance();
		redirect.setConversationPropagationEnabled(false);
		redirect.setViewId("/Painel/painel_usuario/advogado.seam");
		redirect.execute();
	}

	/**
	 * Método redireciona advogado para assinatura do termo de compromisso.
	 */
	private void redirecionarAdvogadoParaAssinarTermo() {
		CadastroUsuarioAction.redirectParaCadastro(Authenticator.getPessoaLogada());
	}
	
	/**
	 * Método redireciona advogado para o quadro de avisos.
	 */
	private void redirecionarAdvogadoParaQuadroAviso() {
		Redirect redirect = Redirect.instance();
        redirect.setViewId("/QuadroAviso/listViewQuadroAvisoMensagem.seam");
		FacesMessages.instance().clear();
		redirect.execute();
	}

	/**
	 * Método redireciona usuário externo (advogado e jusPostulandi) para aviso de não acesso ao sistema.
	 */
	private void redirecionarAvisoNaoAcesso() {
		CadastroUsuarioAction.carregarVariaveisDeSessao(Authenticator.getPessoaLogada().getDocumentoCpfCnpj(), null, null);
		Redirect redirect = Redirect.instance();
        redirect.setViewId("/publico/usuario/semTermo.seam");
		FacesMessages.instance().clear();
		redirect.execute();
	}
        
    /**
     * Método retorna verdadeiro se usuario possuir alguma
     * pendência na assinatura do termo de compromisso
     * 
     * @return
     */
    public boolean possuiPendenciaCadastro(){    	
    	return Authenticator.instance().isPossuiPendenciaCadastro();
    }
            
	public static ControlePaginaInicialUsuario instance() {
        return ComponentUtil.getComponent(NAME, ScopeType.EVENT);
    }

	private String recuperaPainelUsuario(){
		Identity identity = Identity.instance();
		String pagePrefix = "/pages";
		String painelPrefix = "/ng2/dev.seam#/painel-usuario-interno";
		if(identity != null) {
//			if (!tarefaInicial.isEmpty()){
//				return painelPrefix + "/3/"+tarefaInicial+"/0";
//			} else if (identity.hasRole(pagePrefix+ painelPrefix + "/1")) {
//				return painelPrefix + "/1";
//			} else if (identity.hasRole(pagePrefix + painelPrefix + "/2")) {
//				return painelPrefix + "/2";
//			} else if (identity.hasRole(pagePrefix + painelPrefix + "/3")) {
//				return painelPrefix + "/3";
//			} else {
				return painelPrefix;
//			}
		}
		return "/home.seam";
	}

	public String getTarefaInicial() {
		return tarefaInicial;
	}

	public void setTarefaInicial(String tarefaInicial) {
		this.tarefaInicial = tarefaInicial;
	}

	public void redirectToSSOLoginCallback(String mensagemErroLogin) {
    	Redirect redirect = Redirect.instance();
    	redirect.setViewId("/publico/usuario/ssoCallbackLogin.xhtml");
    	redirect.setParameter("mensagem-erro-login", mensagemErroLogin);
        redirect.execute();
	}

}
