package br.com.infox.cliente.home;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.component.suggest.PessoaServidorMunicipioSuggestBean;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.UsuarioLocalizacaoHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorColegiadoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.PessoaServidorManager;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaServidor;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoVisibilidade;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.VisualizacaoProcessoEnum;

@Name("pessoaServidorHome")
@BypassInterceptors
public class PessoaServidorHome extends AbstractPessoaServidorHome<PessoaServidor> {

	private static final long serialVersionUID = 1L;
	private Estado estado;
	private Papel papel;
	private Localizacao localizacao;
	private String nomeServidor;
	private String oldCpf;
	
	private PessoaServidorManager pessoaServidorManager;

			
	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		if (this.estado != null) {
			if ((estado == null) || (!this.estado.getEstado().equals(estado.getEstado()))) {
				getPessoaServidorMunicipioSuggestBean().setInstance(null);
			}
		}
		this.estado = estado;
	}

	private PessoaServidorMunicipioSuggestBean getPessoaServidorMunicipioSuggestBean() {
		PessoaServidorMunicipioSuggestBean pessoaServidorMunicipioSuggest = 
				(PessoaServidorMunicipioSuggestBean) Component.getInstance(PessoaServidorMunicipioSuggestBean.NAME);
		return pessoaServidorMunicipioSuggest;
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);

		// Para o uso no cadastro de Localização que invoca o home
		PessoaHome.instance().setId(id);
		if (changed) {
			if (getInstance().getMunicipioNascimento() != null) {
				estado = getInstance().getMunicipioNascimento().getEstado();
			}
			getPessoaServidorMunicipioSuggestBean().setInstance(getInstance().getMunicipioNascimento());
		}
		if (id == null) {
			getPessoaServidorMunicipioSuggestBean().setInstance(null);
			estado = null;
		}

		if (getInstance() != null && oldCpf == null) {
			oldCpf = getInstance().getNumeroCPF();
		}
		
		setUsuarioAtivoInicial(instance.getAtivo());
		setPerfilAtivoInicial(instance.getServidorAtivo());
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		instance.setMunicipioNascimento(getPessoaServidorMunicipioSuggestBean().getInstance());
		return super.beforePersistOrUpdate();
	}

	@Override
	public void newInstance() {
		Contexts.removeFromAllContexts("pessoaServidorMunicipioSuggest");

		Integer usuarioId = null;
		if(instance != null){
			usuarioId = instance.getIdUsuario();
		}
		super.newInstance(usuarioId);
		// Para o uso no cadastro de Localização que invoca o home
		PessoaHome.instance().newInstance();
		oldCpf = null;
	}
	
	@Override
	public String persist() {
		
		if(beforePersistOrUpdate()){
			try {
				PessoaServidor servidor = getInstance();
				pessoaServidorManager = (PessoaServidorManager)getComponent(PessoaServidorManager.NAME);
				pessoaServidorManager.persistAndFlush(servidor);
				setInstance(servidor);
				createdMessage();
				return afterPersistOrUpdate("persisted");
			} catch (PJeBusinessException e) {
				reportMessage(e);
			}
		}
		
		return null;
	}

	@Override
	public String update() {
		if(beforePersistOrUpdate()){			
			try {
				pessoaServidorManager = (PessoaServidorManager)getComponent(PessoaServidorManager.NAME);
				pessoaServidorManager.persistAndFlush(getInstance());
				setInstance(getInstance());
				updatedMessage();
				
				EntityManager em = getEntityManager();
				for (PessoaDocumentoIdentificacao documento : getInstance().getPessoaDocumentoIdentificacaoList()) {
					em.merge(documento);
					EntityUtil.flush(em);
				}
				
				atualizarEspecializacao(instance.getServidorAtivo(), instance.getPessoa(), PessoaServidor.class);
				getInstance().getPessoa().setNome(getInstance().getNome());
				PessoaHome.instance().atualizarNomeLocalizacao(getInstance().getPessoa());

				refreshGrid("pessoaDocumentoIdentificacaoCadastroGrid");
				
				if (isCadastroAlterado()) {
					VisualizadoresSigiloHome.instance().ativarOuInativarVisualizador(getInstance());
					Authenticator.deslogar(instance.getPessoa(), "perfil.atualizar");
				}
			
				return afterPersistOrUpdate("update");
			} catch (PJeBusinessException e) {
				reportMessage(e);
				if (instance.getPessoa().getPessoaMagistrado() != null) {
					FacesMessages.instance().clear();
					FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "pessoaServidor.erro.magistrado");
				} else {
					FacesMessages.instance().clear();
					FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "perfil.erro");
				}
			}
		}
		
		return null;		
	}
	
	public boolean checkLogin() {
		pessoaServidorManager = getComponent(PessoaServidorManager.NAME);
		Boolean loginJaCadastrado = pessoaServidorManager.checkLogin(getInstance().getLogin(),getInstance().getIdUsuario());
		if (!loginJaCadastrado) {
			FacesMessages.instance().addToControl("loginLogin", StatusMessage.Severity.ERROR, "Login já cadastrado!");
			getInstance().setLogin("");
		}
		
		return loginJaCadastrado;
	}

	public static PessoaServidorHome instance() {
		return ComponentUtil.getComponent("pessoaServidorHome");
	}

	public String removeCertificado() {
		getInstance().setCertChain(null);
		getInstance().setAssinatura(null);
		return update();
	}

	public void setPapel(Papel papel) {
		this.papel = papel;
	}

	public Papel getPapel() {
		return papel;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
	}

	public Localizacao getLocalizacao() {
		return localizacao;
	}

	public VisualizacaoProcessoEnum[] getVisualizacaoProcessoEnumValues() {
		return VisualizacaoProcessoEnum.values();
	}

	public void newInstanceUsuarioLocalizacao() {
		UsuarioLocalizacaoHome localizacaoHome = getComponent("usuarioLocalizacaoHome");
		localizacaoHome.newInstance();
		localizacaoHome.getInstance().setUsuario(getInstance().getPessoa());
	}

	public String getNomeServidor() {
		return nomeServidor;
	}

	public void setNomeServidor(String nomeServidor) {
		this.nomeServidor = nomeServidor;
	}

	public void onClickSearchTab() {
		super.onClickSearchTab();
		UsuarioLocalizacaoHome.instance().newInstance();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public String inactive(PessoaServidor instance) {
		PessoaService pessoaService = ComponentUtil.getComponent("pessoaService");
		try {
			PessoaFisica pessoaFisica = (PessoaFisica) pessoaService.desespecializa(instance.getPessoa(), PessoaServidor.class);
			instance = pessoaFisica.getPessoaServidor();
			instance.setServidorAtivo(Boolean.FALSE);
			instance.getPessoa().setUsuarioLocalizacaoList(null);
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "pessoaServidor.perfil.inativado");
			Authenticator.deslogar(instance.getPessoa(), "perfil.atualizar");
		} catch (PJeBusinessException e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "pessoaServidor.erro.visibilidade");
			e.printStackTrace();
		}
		
		return "update";
	}
	
	/**
	 * Este método reativa o servidor.
	 * @param pessoaServidor
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String ativarServidor(PessoaServidor pessoaServidor){
		PessoaService pessoaService = ComponentUtil.getComponent("pessoaService");
		try {
			PessoaFisica pessoaFisica = (PessoaFisica)pessoaService.especializa(pessoaServidor.getPessoa(), PessoaServidor.class);
			pessoaServidor = pessoaFisica.getPessoaServidor();
			pessoaServidor.setServidorAtivo(Boolean.TRUE);
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "pessoaServidor.perfil.ativado");
		} catch (PJeBusinessException e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "perfil.erro");
			e.printStackTrace();
		}

		return "update";
	}
	
	public void gerarNovaSenha(){
		//setando como nulo, o manager inativa a senha atual, gera nova senha e hash de ativacao
		getInstance().setHashAtivacaoSenha(PjeUtil.instance().gerarHashAtivacao(getInstance().getLogin()));
		if(getInstance().getIdUsuario() == null){
			persist();
		}else{
			update();
		}
		
		try {
			UsuarioService usuarioService = getComponent("usuarioService");
			usuarioService.enviarEmailSenha(getInstance());
			reportMessage("pje.pessoaFisicaHome.info.emailEnviadoComSucesso", null, getInstance().getEmail());
		} catch (PJeBusinessException e) {
			reportMessage(e);
		}
	}
	
	public Boolean checkCPF(){
		pessoaServidorManager = (PessoaServidorManager)getComponent(PessoaServidorManager.NAME);
		return pessoaServidorManager.checkCPF(getInstance().getNumeroCPF(), getInstance().getIdUsuario());
	}	
	
	/**
	 * Método responsável por retornar a lista de localizações/visibilidade de um servidor.
	 * 
	 * @param pessoaServidor Pessoa servidor.
	 * @return A lista de localizações/visibilidade de um servidor.
	 */
	public String obterLocalizacoesVisibilidades(PessoaServidor pessoaServidor){
		StringBuilder sb = new StringBuilder();
		Papel papelMagistado = ParametroUtil.instance().getPapelMagistrado();
		List<UsuarioLocalizacaoVisibilidade> lista = pessoaServidor.getUsuarioLocalizacoesVisibilidades();
		if(lista != null){
			for(UsuarioLocalizacaoVisibilidade usuarioLocalizacaoVisibilidade : lista){
				if(usuarioLocalizacaoVisibilidade != null ) {
					if(usuarioLocalizacaoVisibilidade.getUsuarioLocalizacaoMagistradoServidor() != null &&
							usuarioLocalizacaoVisibilidade.getUsuarioLocalizacaoMagistradoServidor().getUsuarioLocalizacao() != null &&
							!usuarioLocalizacaoVisibilidade.getUsuarioLocalizacaoMagistradoServidor().getUsuarioLocalizacao().getPapel().equals(papelMagistado)){
						
						sb.append(usuarioLocalizacaoVisibilidade.getUsuarioLocalizacaoMagistradoServidor().getUsuarioLocalizacao());
						sb.append(" - ");
					}
					if(usuarioLocalizacaoVisibilidade.getOrgaoJulgadorCargo() != null && 
							usuarioLocalizacaoVisibilidade.getOrgaoJulgadorCargo().getCargo() != null){
						
						sb.append(usuarioLocalizacaoVisibilidade.getOrgaoJulgadorCargo().getCargo()).append("<br/>");
					} else {
						if(usuarioLocalizacaoVisibilidade.getUsuarioLocalizacaoMagistradoServidor() != null &&
								usuarioLocalizacaoVisibilidade.getUsuarioLocalizacaoMagistradoServidor().getUsuarioLocalizacao() != null &&
								!usuarioLocalizacaoVisibilidade.getUsuarioLocalizacaoMagistradoServidor().getUsuarioLocalizacao().getPapel().equals(papelMagistado)){
							
							sb.append("Todos <br/>");
						}
					}
				}
			}
		}
		return sb.toString();
	}
 	
  	public List<OrgaoJulgadorColegiado> getOrgaoJulgadorColegiadoItens(){
  		OrgaoJulgadorColegiadoManager orgaoJulgadorColegiadoManager = ComponentUtil.getComponent("orgaoJulgadorColegiadoManager");
  		return orgaoJulgadorColegiadoManager.obterOJColegiadosAtivosPorPerfilLogado();
  	}
  	
  	public List<OrgaoJulgador> getOrgaoJulgadorItens(OrgaoJulgadorColegiado orgaoJulgadorColegiado){
  		OrgaoJulgadorManager orgaoJulgadorManager = ComponentUtil.getComponent("orgaoJulgadorManager");
  		if (orgaoJulgadorColegiado != null) {
  			return orgaoJulgadorManager.obterOrgaosJulgadoresPorColegiadoEhPorLocalizacao(orgaoJulgadorColegiado, Authenticator.getLocalizacaoAtual());
  		}
  		return orgaoJulgadorManager.findAllbyLocalizacao(Authenticator.getLocalizacaoAtual());
	}
}