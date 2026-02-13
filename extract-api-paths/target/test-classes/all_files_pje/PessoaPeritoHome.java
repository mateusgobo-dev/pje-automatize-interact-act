package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.component.suggest.PessoaPeritoMunicipioSuggestBean;
import br.com.infox.cliente.component.tree.EspecialidadeTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.PessoaPeritoManager;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.pje.nucleo.entidades.Especialidade;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaPerito;

@Name("pessoaPeritoHome")
@BypassInterceptors
public class PessoaPeritoHome extends AbstractPessoaPeritoHome<PessoaPerito> {

	private static final long serialVersionUID = 1L;
	private Estado estado;
	private List<Especialidade> especialidadeList = new ArrayList<Especialidade>(0);
	private Especialidade especialidade;
	private String oldCpf;	

	private PessoaPeritoManager pessoaPeritoManager;

	private PessoaPeritoMunicipioSuggestBean getPessoaPeritoMunicipioSuggest() {
		PessoaPeritoMunicipioSuggestBean pessoaPeritoMunicipioSuggest = (PessoaPeritoMunicipioSuggestBean) Component
				.getInstance("pessoaPeritoMunicipioSuggest");
		return pessoaPeritoMunicipioSuggest;
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
			getPessoaPeritoMunicipioSuggest().setInstance(getInstance().getMunicipioNascimento());
		}
		if (id == null) {
			getPessoaPeritoMunicipioSuggest().setInstance(null);
			estado = null;
		}
		if (getInstance() != null && oldCpf == null) {
			oldCpf = getInstance().getNumeroCPF();
		}
		if (getInstance() != null && oldCpf == null) {
			oldCpf = getInstance().getNumeroCPF();
		}
		
		setUsuarioAtivoInicial(instance.getAtivo());
		setPerfilAtivoInicial(instance.getPeritoAtivo());
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		instance.setMunicipioNascimento(getPessoaPeritoMunicipioSuggest().getInstance());
		return super.beforePersistOrUpdate();
	}

	@Override
	public void newInstance() {
		oldCpf = null;
		limparTree();
		Contexts.removeFromAllContexts("pessoaPeritoMunicipioSuggest");
		super.clearInstance(true);
		// Para o uso no cadastro de Localização que invoca o home
		PessoaHome.instance().newInstance();
	}

	@SuppressWarnings("unchecked")
	@Override
	public String persist() {
		String persist = null;
		pessoaPeritoManager = (PessoaPeritoManager)getComponent(PessoaPeritoManager.NAME);
		if(beforePersistOrUpdate()){
			if (!isCpfCadastrado()) {
				try {
					PessoaPerito pessoaPerito = getInstance();
					pessoaPeritoManager.persistAndFlush(pessoaPerito);
					setInstance(pessoaPerito);
					refreshGrid("cadastroPeritoGrid");
				} catch (PJeBusinessException e) {
					reportMessage(e);
				}
			}else{
				PessoaService pessoaService = (PessoaService) Component.getInstance("pessoaService");
				try {
					PessoaFisica pessoa = (PessoaFisica) pessoaService.especializa(getInstance().getPessoa(), PessoaPerito.class);
					pessoaService.persist(pessoa);
				} catch (PJeBusinessException e) {
					FacesMessages.instance().add(Severity.ERROR, "Erro ao tentar especializar o perito: {0}.", e.getLocalizedMessage());
				}
			}
			
			persist = afterPersistOrUpdate("persisted");
		}
		
		return persist;
	}

	public String removeCertificado() {
		getInstance().setCertChain(null);
		getInstance().setAssinatura(null);
		return update();
	}

	@Override
	public String update() {
		pessoaPeritoManager = (PessoaPeritoManager)getComponent(PessoaPeritoManager.NAME);
		if(beforePersistOrUpdate()){
			try {
				pessoaPeritoManager.persistAndFlush(getInstance());
				updatedMessage();
				
				EntityManager em = getEntityManager();
				for (PessoaDocumentoIdentificacao documento : getInstance().getPessoaDocumentoIdentificacaoList()) {
					em.persist(documento);
					EntityUtil.flush(em);
				}
				
				atualizarEspecializacao(instance.getPeritoAtivo(), instance.getPessoa(), PessoaPerito.class);
				getInstance().getPessoa().setNome(getInstance().getNome());
				PessoaHome.instance().atualizarNomeLocalizacao(getInstance().getPessoa());
				
				if (isCadastroAlterado()) {
					Authenticator.deslogar(instance.getPessoa(), "perfil.atualizar");
				}
				
				refreshGrid("pessoaDocumentoIdentificacaoCadastroGrid");
				return afterPersistOrUpdate("update");
			} catch (PJeBusinessException e) {
				reportMessage(e);
				FacesMessages.instance().clear();
				FacesMessages.instance().addFromResourceBundle(Severity.INFO, "perfil.erro");				
			}
		}
		
		return null;
	}	
	
	public void checkCPF() {
		Boolean cpfJaCadastrado = PessoaAdvogadoHome.instance().checkCPF(getInstance().getNumeroCPF(),
				getInstance().getIdUsuario());
	}

	private boolean isCpfCadastrado() {
		return PessoaAdvogadoHome.instance().checkCPF(getInstance().getNumeroCPF(), getInstance().getIdUsuario());
	}
	
	public static PessoaPeritoHome instance() {
		return ComponentUtil.getComponent("pessoaPeritoHome");
	}

	public Especialidade getEspecialidade() {
		return especialidade;
	}

	public void setEspecialidade(Especialidade especialidade) {
		this.especialidade = especialidade;
	}

	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		if (this.estado != null) {
			if ((estado == null) || (!this.estado.getEstado().equals(estado.getEstado()))) {
				getPessoaPeritoMunicipioSuggest().setInstance(null);
			}
		}
		this.estado = estado;
	}

	public List<Especialidade> getEspecialidadeList() {
		return especialidadeList;
	}

	public void setEspecialidadeList(List<Especialidade> especialidadeList) {
		this.especialidadeList = especialidadeList;
	}

	public void limparTree() {
		EspecialidadeTreeHandler ret1 = getComponent("especialidadeTree");
		ret1.clearTree();
	}

	@Override
	public String inactive(PessoaPerito instance) {
		try {
			pessoaPeritoManager.inactive(instance);
			FacesMessages.instance().add(StatusMessage.Severity.INFO, getInactiveSuccess());
			return "update";			
		} catch (PJeBusinessException e) {
			reportMessage(e);
		}

		return null;
	}
	
	public void gerarNovaSenha(){
		//setando como nulo, o manager inativa a senha atual, gera nova senha e hash de ativacao
		getInstance().setSenha(null);
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
}