package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.list.PessoaAssistenteProcuradoriaLocalList;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.PessoaAssistenteProcuradoriaManager;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAssistenteProcuradoria;
import br.jus.pje.nucleo.entidades.PessoaAssistenteProcuradoriaLocal;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.enums.PessoaAssistenteProcuradorEnum;

@Name("pessoaAssistenteProcuradoriaHome")
@BypassInterceptors
public class PessoaAssistenteProcuradoriaHome extends AbstractHome<PessoaAssistenteProcuradoria> {

	private static final long serialVersionUID = 1L;	

	private PessoaAssistenteProcuradoriaManager pessoaAssistenteProcuradoriaManager;
	

	private boolean isCpfCadastrado() {
		pessoaAssistenteProcuradoriaManager = (PessoaAssistenteProcuradoriaManager)getComponent(PessoaAssistenteProcuradoriaManager.NAME);
		return !pessoaAssistenteProcuradoriaManager.checkCPF(getInstance().getNumeroCPF(), getInstance().getIdUsuario());
	}

	private Estado estado;
	
	@Override
	public void newInstance() {
		super.clearInstance(true);
	}

	@Override
	public String persist() {
		if(beforePersistOrUpdate()){
			if (!isCpfCadastrado()) {
				try {
					pessoaAssistenteProcuradoriaManager = (PessoaAssistenteProcuradoriaManager)getComponent(PessoaAssistenteProcuradoriaManager.NAME);
					pessoaAssistenteProcuradoriaManager.persistAndFlush(getInstance());
					
					if (Pessoa.instanceOf(Authenticator.getUsuarioLogado(), PessoaProcurador.class)
							|| Pessoa.instanceOf(Authenticator.getUsuarioLogado(), PessoaAssistenteProcuradoria.class)) {
						gravarPrcuradoriaInicial();
					}
					
					FacesMessages.instance().clear();
					FacesMessages.instance().add(StatusMessage.Severity.INFO, FacesUtil.getMessage("entity_messages", "PessoaProcuradorProcuradoria_created"));
					return afterPersistOrUpdate("persisted");
				} catch (PJeBusinessException e) {
					reportMessage(e);
				}
			}
		}
		
		return null;
	}

	/**
	 * Grava a localização do procurador logado para assitente
	 */
	public void gravarPrcuradoriaInicial() {
		PessoaAssistenteProcuradoriaLocal assistenteProcuradoriaLocal = new PessoaAssistenteProcuradoriaLocal();
		PessoaAssistenteProcuradoriaLocalHome procLocalHome = PessoaAssistenteProcuradoriaLocalHome.instance();
		assistenteProcuradoriaLocal.setUsuario(getInstance().getPessoa());
		assistenteProcuradoriaLocal.setResponsavelLocalizacao(false);
		Localizacao localizacao = Authenticator.getLocalizacaoAtual();
		assistenteProcuradoriaLocal.setLocalizacaoFisica(localizacao);
		Procuradoria procuradoria = procLocalHome.getProcuradoria(localizacao);
		assistenteProcuradoriaLocal.setProcuradoria(procuradoria);
		assistenteProcuradoriaLocal.setPapel(ParametroUtil.instance().getPapelAssistenteProcuradoria());
		EntityManager em = EntityUtil.getEntityManager();
		em.persist(assistenteProcuradoriaLocal);
		em.flush();
	}

	public static PessoaAssistenteProcuradoriaHome instance() {
		return ComponentUtil.getComponent("pessoaAssistenteProcuradoriaHome");
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		instance.setMunicipioNascimento(PessoaFisicaHome.instance().getPessoaFisicaMunicipioSuggestBean().getInstance());
		return super.beforePersistOrUpdate();
	}

	public Estado getEstado() {
		return this.estado;
	}
	
	@Override
	public String update() {
		List<PessoaAssistenteProcuradoriaLocal> list = PessoaAssistenteProcuradoriaLocalList.instance().list();
		
		if (list.size() == 0) {
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "assistenteProcuradoria.erro.cadastrar");
			return null;
		}
		
		try{
			pessoaAssistenteProcuradoriaManager = (PessoaAssistenteProcuradoriaManager)getComponent(PessoaAssistenteProcuradoriaManager.NAME);
			pessoaAssistenteProcuradoriaManager.persistAndFlush(getInstance());
			updatedMessage();
			
			atualizarEspecializacao(instance.getAssistenteProcuradoriaAtivo(), instance.getPessoa(), PessoaAssistenteProcuradoria.class);
			getInstance().getPessoa().setNome(getInstance().getNome());
			PessoaHome.instance().atualizarNomeLocalizacao(getInstance().getPessoa());

			if (isCadastroAlterado()) {
				Authenticator.deslogar(instance.getPessoa(), "perfil.atualizar");
			}
			
			return afterPersistOrUpdate("update");
		} catch (PJeBusinessException e) {
			reportMessage(e);
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "perfil.erro");
		}
		
		return null;
	}

	public Localizacao getLocalizacaoAtual() {
		if (Authenticator.isPapelAdministrador())
			return null;
		return Authenticator.getLocalizacaoAtual();
	}

	public PessoaAssistenteProcuradorEnum[] getPessoaAssistenteProcuradorValues() {
		return PessoaAssistenteProcuradorEnum.values();
	}

	public void setEstado(Estado estado) {
		this.estado = estado;
	}

	@Override
	public void setId(Object id) {
		boolean changed = (id != null) && !id.equals(this.getId());
		super.setId(id);
		PessoaHome.instance().setId(id);
		if (changed) {
			if (getInstance().getMunicipioNascimento() != null) {
				PessoaFisicaHome.instance().setEstado(getInstance().getMunicipioNascimento().getEstado());
			}
			PessoaFisicaHome.instance().getPessoaFisicaMunicipioSuggestBean()
					.setInstance(getInstance().getMunicipioNascimento());
		}
		if (id == null) {
			PessoaFisicaHome.instance().getPessoaFisicaMunicipioSuggestBean().setInstance(null);
			PessoaFisicaHome.instance().setEstado(null);
		}
		if ((getInstance().getMunicipioNascimento() != null) && (!changed) && (id != null)
				&& (PessoaFisicaHome.instance().getPessoaFisicaMunicipioSuggestBean().getInstance() != null)
				&& (PessoaFisicaHome.instance().getEstado() != getInstance().getMunicipioNascimento().getEstado())) {
			PessoaFisicaHome.instance().setEstado(getInstance().getMunicipioNascimento().getEstado());
			PessoaFisicaHome.instance().getPessoaFisicaMunicipioSuggestBean()
					.setInstance(getInstance().getMunicipioNascimento());
		}
		
		setUsuarioAtivoInicial(instance.getAtivo());
		setPerfilAtivoInicial(instance.getAssistenteProcuradoriaAtivo());
	}

	@Override
	public void setInstance(PessoaAssistenteProcuradoria instance) {
		if(instance == null){
			PessoaHome.instance().setInstance(null);
		}else{
			PessoaHome.instance().setInstance(instance.getPessoa());
		}
		super.setInstance(instance);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public String inactive(PessoaAssistenteProcuradoria instance) {
		PessoaService pessoaService = ComponentUtil.getComponent("pessoaService");
		try {
			PessoaFisica pessoaFisica = (PessoaFisica) pessoaService.desespecializa(instance.getPessoa(), PessoaAssistenteProcuradoria.class);
			instance = pessoaFisica.getPessoaAssistenteProcuradoria();
			instance.setAssistenteProcuradoriaAtivo(Boolean.FALSE);
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "assistenteProcuradoria.perfil.inativado");
			Authenticator.deslogar(instance.getPessoa(), "perfil.atualizar");
		} catch (PJeBusinessException e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "assistenteProcuradoria.erro.visibilidade");
			e.printStackTrace();
		}
		return "update";
	}
	
	/**
	 * Este método reativa o assistente de advogado.
	 * @param pessoaServidor
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String ativarAssistenteProcuradoria(PessoaAssistenteProcuradoria instance) {
		PessoaService pessoaService = ComponentUtil.getComponent(PessoaService.NAME);
		try {
			PessoaFisica pessoaFisica = (PessoaFisica)pessoaService.especializa(instance.getPessoa(), PessoaAssistenteProcuradoria.class);
			instance = pessoaFisica.getPessoaAssistenteProcuradoria();
			instance.setAssistenteProcuradoriaAtivo(Boolean.TRUE);
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "assistenteProcuradoria.perfil.ativado");
		} catch (PJeBusinessException e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "perfil.erro");
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
	
	//PJEII-19201 recuperar localizações relacionadas à assistente de procuradoria.
	public List<Localizacao> getLocalizacoes(Pessoa pessoa){
		List<Localizacao> returnValue = new ArrayList<Localizacao>(0);
		
		for(UsuarioLocalizacao usuarioLocalizacao: pessoa.getUsuarioLocalizacaoList()){
			if(usuarioLocalizacao instanceof PessoaAssistenteProcuradoriaLocal){
				returnValue.add(usuarioLocalizacao.getLocalizacaoFisica());
			}
		}
		
		return returnValue;
	}
	
	@Override
	public boolean isEditable() {
		return Authenticator.isAdministradorProcuradoriadefensoria() || Authenticator.isRepresentanteGestor() || Authenticator.isAssistenteGestorProcurador();
	}

	public boolean isPermiteAlterarEmail() {
		return Authenticator.isPermiteAlterarEmail(getInstance().getPessoa());
	}

	public boolean isPossuiPermissaoAtivarDesativar() {
		return Authenticator.isAdministradorProcuradoriadefensoria();
	}
	
}