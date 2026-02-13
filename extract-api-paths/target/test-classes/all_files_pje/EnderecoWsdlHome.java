/*
 IBPM - Ferramenta de produtividade Java
 Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda.

 Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo 
 sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela 
 Free Software Foundation; versão 2 da Licença.
 Este programa é distribuído na expectativa de que seja útil, porém, SEM 
 NENHUMA GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU 
 ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA.
 
 Consulte a GNU GPL para mais detalhes.
 Você deve ter recebido uma cópia da GNU GPL junto com este programa; se não, 
 veja em http://www.gnu.org/licenses/   
 */
package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.itx.component.AbstractHome;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.intercomunicacao.dto.ConsultarProcessoRespostaDTO;
import br.jus.cnj.pje.intercomunicacao.service.MNIMediatorService;
import br.jus.cnj.pje.intercomunicacao.service.MNIMediatorServiceAbstract;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.AplicacaoClasseManager;
import br.jus.cnj.pje.nucleo.manager.PessoaManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoManager;
import br.jus.pje.nucleo.entidades.AplicacaoClasse;
import br.jus.pje.nucleo.entidades.EnderecoWsdl;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;

/**
 * Classe para operações com "EnderecoWsdl"
 * 
 */

@Name("enderecoWsdlHome")
@BypassInterceptors
public class EnderecoWsdlHome extends AbstractHome<EnderecoWsdl> {

	private static final long serialVersionUID = 1L;
	
	private AplicacaoClasseManager aplicacaoClasseManager;
	
	private List<AplicacaoClasse> aplicacoesClasse = new ArrayList<AplicacaoClasse>(); 
	
	private static final LogProvider log = Logging.getLogProvider(EnderecoWsdl.class);
	
	@Override
	public void create() {
		super.create();
		recuperarAplicacoesClasse();
	}
	
	private void recuperarAplicacoesClasse() {
		try {
			List<AplicacaoClasse> aplicacaoes = getAplicacaoClasseManager().findAll();
			if(aplicacaoes != null && !aplicacaoes.isEmpty()){
				aplicacoesClasse.addAll(aplicacaoes);
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			log.error("Erro ao tentar recuperar aplicações da classe: "+e.getMessage());
		} 		
	}
	public static EnderecoWsdlHome instance() {
		return ComponentUtil.getComponent("enderecoWsdlHome");
	}

	public List<AplicacaoClasse> getAplicacoesClasse() {
		return aplicacoesClasse;
	}

	/**
	 * Inativa um EndereçoWsdl.
	 * 
	 * @param enderecoWsdl
	 */
	public void inativar(EnderecoWsdl enderecoWsdl) {
		enderecoWsdl.setAtivo(false);
		setId(enderecoWsdl.getIdEnderecoWsdl());
		update();
		FacesUtil.adicionarMensagemError(true, "Registro inativado com sucesso.");
		FacesMessages.instance().clear();
	}

	/**
	 * Método responsável por retornar uma manager de aplicacao manager.
	 * @return Manager de processo judicial
	 */
	public AplicacaoClasseManager getAplicacaoClasseManager() {
		if(aplicacaoClasseManager == null){
			aplicacaoClasseManager = (AplicacaoClasseManager) Component.getInstance(AplicacaoClasseManager.NAME);		
		}
		return aplicacaoClasseManager;
	}
	
	/**
	 * Verifica o acesso ao endpoint (wsdl, usuário e senha)
	 */
	public void verificarAcessoEndpoint() {
		EnderecoWsdl enderecoWsdl = getInstance();
		
		verificarAcessoEndpoint(enderecoWsdl, enderecoWsdl.getLogin(), enderecoWsdl.getSenha());
	}
	
	/**
	 * Verifica o acesso ao endpoint (wsdl, usuário e senha)
	 * 
	 * @param wsdl
	 * @param login
	 * @param senha
	 */
	public void verificarAcessoEndpoint(EnderecoWsdl enderecoWsdl, String login, String senha) {
		
		if (ProjetoUtil.isNaoVazio(enderecoWsdl.getWsdlIntercomunicacao(), login, senha)) {
			MNIMediatorService mediator = MNIMediatorServiceAbstract.instance(enderecoWsdl);
			ConsultarProcessoRespostaDTO resposta = mediator.login(login, senha);
			
			if (resposta.getSucesso()) {
				FacesUtil.adicionarMensagemInfo(true, "Conexão com o endpoint OK.");
			} else {
				FacesUtil.adicionarMensagemError(true, resposta.getMensagem());
			}
		} else {
			FacesUtil.adicionarMensagemError(true, "Falha na conexão com o endpoint. Erro: WSDL/Usuário/Senha null!");
		}
	}
	
	/**
	 * Verifica o acesso ao endpoint (wsdl, usuário e senha)
	 * 
	 * @param enderecoWsdl
	 * @param login
	 * @param senha
	 */
	public void verificarAcessoEndpoint(EnderecoWsdl enderecoWsdl, UsuarioLogin login) {
		if (enderecoWsdl != null && login != null) {
			verificarAcessoEndpoint(enderecoWsdl, login.getLogin(), login.getSenha());
		} else {
			FacesUtil.adicionarMensagemError(true, "Falha na conexão com o endpoint. Erro: WSDL/Usuário/Senha null!");
		}
	}
}