package br.com.infox.cliente.home;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.PessoaAssistenteAdvogadoManager;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.PessoaAssistenteAdvogado;
import br.jus.pje.nucleo.entidades.PessoaAssistenteAdvogadoLocal;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.enums.PessoaAssistenteAdvogadoEnum;

/**
 * Classe para operações com "Pessoa Assistente Advogado"
 * 
 */

@Name("pessoaAssistenteAdvogadoHome")
@BypassInterceptors
public class PessoaAssistenteAdvogadoHome extends AbstractHome<PessoaAssistenteAdvogado> {

	private static final long serialVersionUID = 1L;
	
	private PessoaAssistenteAdvogadoManager pessoaAssistenteAdvogadoManager;

	private boolean isCpfCadastrado() {
		pessoaAssistenteAdvogadoManager = (PessoaAssistenteAdvogadoManager)getComponent(PessoaAssistenteAdvogadoManager.NAME);
		return !pessoaAssistenteAdvogadoManager.checkCPF(getInstance().getNumeroCPF(), getInstance().getIdUsuario());
	}
	
	@Override
	public void newInstance() {
		super.clearInstance(true);
	}

	@Override
	public String persist() {
		String persist = null;
		if (!isCpfCadastrado()) {
			if(beforePersistOrUpdate()){
				try {
					pessoaAssistenteAdvogadoManager = (PessoaAssistenteAdvogadoManager)getComponent(PessoaAssistenteAdvogadoManager.NAME);
					pessoaAssistenteAdvogadoManager.persistAndFlush(getInstance());
					setInstance(getInstance());
					persist = afterPersistOrUpdate("persisted");					
				} catch (PJeBusinessException e) {
					reportMessage(e);
				}
				
				PessoaFisica pf = getInstance().getPessoa();
				UsuarioLocalizacao ul = new UsuarioLocalizacao();
				ul.setUsuario(pf);
				ul.setResponsavelLocalizacao(false);
				ul.setLocalizacaoFisica(Authenticator.getLocalizacaoAtual());
				ul.setPapel(ParametroUtil.instance().getPapelAssistenteAdvogado());
				getEntityManager().merge(ul);
				getEntityManager().flush();
				FacesMessages.instance().clear();
				FacesMessages.instance().add(StatusMessage.Severity.INFO, FacesUtil.getMessage("entity_messages", "PessoaAdvogado_created"));
			}
		}
		
		return persist;
	}

	public PessoaAssistenteAdvogadoEnum[] getPessoaAssistenteAdvogadoValues() {
		return PessoaAssistenteAdvogadoEnum.values();
	}

	public static PessoaAssistenteAdvogadoHome instance() {
		return ComponentUtil.getComponent("pessoaAssistenteAdvogadoHome");
	}

	@Override
	public void setInstance(PessoaAssistenteAdvogado instance) {
		if(instance == null){
			PessoaHome.instance().setInstance(null);
		}else{
			PessoaHome.instance().setInstance(instance.getPessoa());
		}
		super.setInstance(instance);
	}

	@Override
	public String update() {
		String ret = null;
		if(beforePersistOrUpdate()){		
			try {
				pessoaAssistenteAdvogadoManager = (PessoaAssistenteAdvogadoManager)getComponent(PessoaAssistenteAdvogadoManager.NAME);
				pessoaAssistenteAdvogadoManager.persistAndFlush(getInstance());
				ret = afterPersistOrUpdate("update");
				updatedMessage();
				
				if (Authenticator.getPapelAtual().getIdentificador().equalsIgnoreCase("advogado") && !existeLocalizacao()) {
					PessoaAssistenteAdvogadoLocal paal = new PessoaAssistenteAdvogadoLocal();
					paal.setLocalizacaoFisica(Authenticator.getLocalizacaoAtual());
					paal.setPapel(ParametroUtil.instance().getPapelAssistenteAdvogado());
					paal.setResponsavelLocalizacao(Boolean.FALSE);
					paal.setUsuario(getInstance().getPessoa());
					EntityUtil.getEntityManager().persist(paal);
					EntityUtil.getEntityManager().flush();
				}
				
				atualizarEspecializacao(instance.getAssistenteAdvogadoAtivo(), instance.getPessoa(), PessoaAssistenteAdvogado.class);
				getInstance().getPessoa().setNome(getInstance().getNome());
				PessoaHome.instance().atualizarNomeLocalizacao(getInstance().getPessoa());
				if (isCadastroAlterado()) {
					Authenticator.deslogar(instance.getPessoa(), "perfil.atualizar");
				}
				
			} catch (PJeBusinessException e) {
				reportMessage(e);
				FacesMessages.instance().clear();
				FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "perfil.erro");				
			} catch(Exception e1){
				FacesMessages.instance().add(Severity.ERROR, "Nao possivel salvar os dados. ERRO: "+e1.getLocalizedMessage());
			}
		}

		return ret;
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		instance.setMunicipioNascimento(PessoaFisicaHome.instance().getPessoaFisicaMunicipioSuggestBean().getInstance());
		return super.beforePersistOrUpdate();
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
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
		if (getInstance().getMunicipioNascimento() != null) {
			if ((!changed)
					&& (id != null)
					&& (PessoaFisicaHome.instance().getPessoaFisicaMunicipioSuggestBean().getInstance() != null)
					&& (PessoaFisicaHome.instance().getEstado() != PessoaFisicaHome.instance()
							.getPessoaFisicaMunicipioSuggestBean().getInstance().getEstado())) {
				PessoaFisicaHome.instance().setEstado(getInstance().getMunicipioNascimento().getEstado());
				PessoaFisicaHome.instance().getPessoaFisicaMunicipioSuggestBean()
						.setInstance(getInstance().getMunicipioNascimento());
			}
		}
		
		setUsuarioAtivoInicial(instance.getAtivo());
		setPerfilAtivoInicial(instance.getAssistenteAdvogadoAtivo());
	}

	private Boolean existeLocalizacao() {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from PessoaAssistenteAdvogadoLocal o where ");
		sb.append("o.usuario.idUsuario = :idUsuario ");
		sb.append("and o.localizacaoFisica = :localizacao ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idUsuario", instance.getIdUsuario());
		q.setParameter("localizacao", Authenticator.getLocalizacaoAtual());
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
		
	}
	
	public Boolean isAssistenteAdvogadoProcesso(ProcessoTrf processoTrf) {		
		// Obtém localização do usuário atual, ou seja, do assistente de advogado.
		Localizacao localizacao = Authenticator.getLocalizacaoAtual();
		// Obtém usuário localização a partir da localização do usuário atual.
		UsuarioLocalizacao usuarioLocal = Authenticator.getUsuarioLocalizacaoPorIdLocalizacao(localizacao);
		if((usuarioLocal == null)
				|| (usuarioLocal.getUsuario() == null)) {
			return Boolean.FALSE;
		}
		Integer idTipoParte = ParametroUtil.instance().getTipoParteAdvogado().getIdTipoParte();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select ppa.pessoa.idUsuario from ProcessoParte ppa ");
		sb.append("where ppa.processoTrf.idProcessoTrf = :idProcessoTrf ");
		sb.append("and ppa.tipoParte.idTipoParte = :idTipoParte ");
		sb.append("and ppa.pessoa.idUsuario = :idUsuario ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idProcessoTrf", processoTrf.getIdProcessoTrf());
		q.setParameter("idTipoParte", idTipoParte);
		q.setParameter("idUsuario", usuarioLocal.getUsuario().getIdUsuario());
		try {
			Integer retorno = (Integer) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public String inactive(PessoaAssistenteAdvogado instance) {
		PessoaService pessoaService = ComponentUtil.getComponent("pessoaService");
		try {
			PessoaFisica pessoaFisica = (PessoaFisica) pessoaService.desespecializa(instance.getPessoa(), PessoaAssistenteAdvogado.class);
			instance = pessoaFisica.getPessoaAssistenteAdvogado();
			instance.setAssistenteAdvogadoAtivo(Boolean.FALSE);
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "pessoaAssistenteAdvogado.perfil.inativado");
			Authenticator.deslogar(instance.getPessoa(), "perfil.atualizar");
		} catch (PJeBusinessException e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "pessoaAssistenteAdvogado.erro.visibilidade");
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
	public String ativarAssistenteAdvogado(PessoaAssistenteAdvogado instance) {
		PessoaService pessoaService = ComponentUtil.getComponent(PessoaService.NAME);
		try {
			PessoaFisica pessoaFisica = (PessoaFisica)pessoaService.especializa(instance.getPessoa(), PessoaAssistenteAdvogado.class);
			instance = pessoaFisica.getPessoaAssistenteAdvogado();
			instance.setAssistenteAdvogadoAtivo(Boolean.TRUE);
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "pessoaAssistenteAdvogado.perfil.ativado");
		} catch (PJeBusinessException e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "perfil.erro");
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
	
	@SuppressWarnings("unchecked")
	public List<PessoaAssistenteAdvogadoLocal> getLocalizacoes(PessoaAssistenteAdvogado pessoa){
		Query q = getEntityManager().createQuery(
				"SELECT o.localizacaoFisica FROM PessoaAssistenteAdvogadoLocal o WHERE o.usuario.idUsuario = :idUsuario");
		
		q.setParameter("idUsuario", pessoa.getIdUsuario());
		return q.getResultList();
	}
	
	public boolean isPermiteAlterarEmail() {
		return Authenticator.isPermiteAlterarEmail(getInstance().getPessoa());
	}
}
