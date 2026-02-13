package br.com.infox.ibpm.component.tree;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.home.UsuarioLocalizacaoMagistradoServidorHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.tree.EntityNode;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.manager.LocalizacaoManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;

@Name("localizacaoEstruturaSearchTreeHandler")
@BypassInterceptors
public class LocalizacaoEstruturaSearchTreeHandler extends LocalizacaoEstruturaTreeHandler {

	private static final long serialVersionUID = 1L;
	
	private LocalizacaoManager localizacaoManager = ComponentUtil.getComponent(LocalizacaoManager.class);
	
	private UsuarioLocalizacaoMagistradoServidorHome usrLocalizacaoHome = 
			ComponentUtil.getComponent(UsuarioLocalizacaoMagistradoServidorHome.class);

	private boolean pesquisarApenasModelosLocalizacao = false;
	
	@Override
	protected String getQueryRoots() {
		ParametroService parametroService = (ParametroService)Component.getInstance("parametroService");
		String idLocalizacaoTribunal = parametroService.valueOf("idLocalizacaoTribunal");
		
		StringBuilder sb = new StringBuilder("select n from Localizacao n ");
		sb.append("where localizacaoPai is null ");
		sb.append("and (estrutura = true");

		if (!pesquisarApenasModelosLocalizacao && idLocalizacaoTribunal != null) {
			sb.append(" or idLocalizacao = " + idLocalizacaoTribunal);
		}
		
		sb.append(")");
		sb.append("order by faixaInferior");

		return sb.toString();
	}

	@Override
	protected EntityNode<Localizacao> createNode() {
		LocalizacaoNodeSearch node = new LocalizacaoNodeSearch(getQueryChildrenList());
		return node;
	}
	
	/**
	 * Método responsável por retornar o identificador do objeto {@link Localizacao}. 
	 * 
	 * @return Caso o usuário logado tenha o perfil de administrador do sistema E tenha solecionado um órgão julgador 
	 * (colegiado ou singular), será retornado o identificador da localização associada ao órgão julgador selecionado.
	 * Caso nenhum órgão julgador tenha sido selecionado, será retornado o valor cadastrado no parâmetro "idLocalizacaoTribunal".
	 * Caso o usuário logado não seja administrador do sistema, será retornado o identificador da localização ao qual este está associado.
	 */
	protected Localizacao getLocalizacaoFisica(){
		UsuarioLocalizacaoMagistradoServidor usuarioLocalizacaoMagistradoServidor = this.usrLocalizacaoHome.getInstance();
		
		if (Authenticator.isPermissaoCadastroTodosPapeis()) {			
			if (usuarioLocalizacaoMagistradoServidor.getOrgaoJulgador() != null) {
				return usuarioLocalizacaoMagistradoServidor.getOrgaoJulgador().getLocalizacao();
			}
			if(usuarioLocalizacaoMagistradoServidor.getOrgaoJulgadorColegiado() != null){
				return usuarioLocalizacaoMagistradoServidor.getOrgaoJulgadorColegiado().getLocalizacao();
			}
			return ParametroUtil.instance().getLocalizacaoTribunal();
		}else{
			if(usuarioLocalizacaoMagistradoServidor.getOrgaoJulgador() != null){
				if(Authenticator.getLocalizacaoAtual().getIdLocalizacao() == usuarioLocalizacaoMagistradoServidor.getOrgaoJulgador().getLocalizacao().getIdLocalizacao() ||
						localizacaoManager.isLocalizacaoDescendente(Authenticator.getLocalizacaoAtual(), usuarioLocalizacaoMagistradoServidor.getOrgaoJulgador().getLocalizacao())){
					return Authenticator.getLocalizacaoAtual();
				}else if(localizacaoManager.isLocalizacaoDescendente(usuarioLocalizacaoMagistradoServidor.getOrgaoJulgador().getLocalizacao(), Authenticator.getLocalizacaoAtual())){
					return usuarioLocalizacaoMagistradoServidor.getOrgaoJulgador().getLocalizacao();
				}
			}else if(usuarioLocalizacaoMagistradoServidor.getOrgaoJulgadorColegiado() != null){
				if(Authenticator.getLocalizacaoAtual().getIdLocalizacao() == usuarioLocalizacaoMagistradoServidor.getOrgaoJulgadorColegiado().getLocalizacao().getIdLocalizacao() ||
						localizacaoManager.isLocalizacaoDescendente(Authenticator.getLocalizacaoAtual(), usuarioLocalizacaoMagistradoServidor.getOrgaoJulgadorColegiado().getLocalizacao())){
					return Authenticator.getLocalizacaoAtual();
				}else if(localizacaoManager.isLocalizacaoDescendente(usuarioLocalizacaoMagistradoServidor.getOrgaoJulgadorColegiado().getLocalizacao(), Authenticator.getLocalizacaoAtual())){
					return usuarioLocalizacaoMagistradoServidor.getOrgaoJulgadorColegiado().getLocalizacao();
				}
			}
		}
		
		return null;
	}

	protected Localizacao getLocalizacaoModeloRaiz(){
		UsuarioLocalizacaoMagistradoServidor usuarioLocalizacaoMagistradoServidor = this.usrLocalizacaoHome.getInstance();
		
		if(usuarioLocalizacaoMagistradoServidor != null) {
			Localizacao localizacaoFisica = null;
			if(usuarioLocalizacaoMagistradoServidor.getOrgaoJulgador() != null) {
				localizacaoFisica = usuarioLocalizacaoMagistradoServidor.getOrgaoJulgador().getLocalizacao();
			}else if(usuarioLocalizacaoMagistradoServidor.getOrgaoJulgadorColegiado() != null) {
				localizacaoFisica = usuarioLocalizacaoMagistradoServidor.getOrgaoJulgadorColegiado().getLocalizacao();
			}else {
				localizacaoFisica = usuarioLocalizacaoMagistradoServidor.getLocalizacaoFisica();
			}
			if(localizacaoFisica != null) {
				return localizacaoFisica.getEstruturaFilho();
			}
		}
		return null;
	}

		
	/**
	 * Método responsável por retornar o identificador do objeto {@link Localizacao}.  
	 * 
	 * @return Caso o usuário logado tenha o perfil de administrador do sistema E tenha solecionado um órgão julgador 
	 * (colegiado ou singular), será retornado o identificador da localização pai associada ao órgão julgador selecionado. 
	 * Caso contrário, será retornado NULL.
	 */
	protected Integer getIdLocalizacaoPai() {
		UsuarioLocalizacaoMagistradoServidor usuarioLocalizacaoMagistradoServidor = this.usrLocalizacaoHome.getInstance();
		
		if (Authenticator.isPermissaoCadastroTodosPapeis() && usuarioLocalizacaoMagistradoServidor != null) {
			if (usuarioLocalizacaoMagistradoServidor.getOrgaoJulgador() != null) {
				return usuarioLocalizacaoMagistradoServidor.getOrgaoJulgador().getLocalizacao().getLocalizacaoPai().getIdLocalizacao();
			}
			if(usuarioLocalizacaoMagistradoServidor.getOrgaoJulgadorColegiado() != null){
				return usuarioLocalizacaoMagistradoServidor.getOrgaoJulgadorColegiado().getLocalizacao().getLocalizacaoPai().getIdLocalizacao();
			}
		}
		
		return null;
	}
	
	

	public LocalizacaoManager getLocalizacaoManager() {
		return localizacaoManager;
	}

	public UsuarioLocalizacaoMagistradoServidorHome getUsrLocalizacaoHome() {
		return usrLocalizacaoHome;
	}
	
	public boolean isPesquisarApenasModelosLocalizacao() {
		return pesquisarApenasModelosLocalizacao;
	}
	
	public void setPesquisarApenasModelosLocalizacao(boolean pesquisarApenasModelosLocalizacao) {
		this.pesquisarApenasModelosLocalizacao = pesquisarApenasModelosLocalizacao;
	}
}