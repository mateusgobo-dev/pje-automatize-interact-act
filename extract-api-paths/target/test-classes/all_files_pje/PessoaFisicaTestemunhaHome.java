package br.com.infox.cliente.home;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.cliente.component.suggest.TestemunhaMunicipioSuggestBean;
import br.com.infox.ibpm.home.EnderecoHome;
import br.com.infox.pje.manager.PessoaFisicaManager;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.enums.SexoEnum;

@Name("pessoaFisicaTestemunhaHome")
@BypassInterceptors
public class PessoaFisicaTestemunhaHome extends AbstractPessoaFisicaHome<PessoaFisica> {
	
	@In
	private PessoaFisicaManager pessoaFisicaManager;

	private static final long serialVersionUID = 1L;
	private Estado estado;

	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		if (this.estado != null) {
			if ((estado == null) || (!this.estado.getEstado().equals(estado.getEstado()))) {
				getTestemunhaMunicipioSuggestBean().setInstance(null);
			}
		}
		this.estado = estado;
	}

	@Override
	public void newInstance() {
		Contexts.removeFromAllContexts("cepSuggest");
		Contexts.removeFromAllContexts("pessoaFisicaMunicipioSuggest");
		refreshGrid("pessoaFisicaGrid");
		super.newInstance();
	}

	public static PessoaFisicaTestemunhaHome instance() {
		return ComponentUtil.getComponent("pessoaFisicaTestemunhaHome");
	}

	private TestemunhaMunicipioSuggestBean getTestemunhaMunicipioSuggestBean() {
		TestemunhaMunicipioSuggestBean testemunhaMunicipioSuggest = (TestemunhaMunicipioSuggestBean) Component
				.getInstance("testemunhaMunicipioSuggest");
		return testemunhaMunicipioSuggest;
	}

	@Override
	protected PessoaFisica createInstance() {
		instance = super.createInstance();
		return instance;
	}

	@Override
	public String persist() {
		try {
			
			if(beforePersistOrUpdate()){
				pessoaFisicaManager.persistAndFlush(getInstance());
				setInstance(getInstance());
				return afterPersistOrUpdate("persisted");
			}
			
			return null;
		} catch (PJeBusinessException e) {
			reportMessage(e);
			return null;
		}
	}

	@Override
	public String update() {
		EntityManager em = getEntityManager();
		for (PessoaDocumentoIdentificacao documento : getInstance().getPessoaDocumentoIdentificacaoList()) {
			em.persist(documento);
			EntityUtil.flush(em);
		}
		refreshGrid("pessoaDocumentoIdentificacaoCadastroGrid");
		
		try {
			if(beforePersistOrUpdate()){
				pessoaFisicaManager.persistAndFlush(getInstance());
				setInstance(getInstance());
				return afterPersistOrUpdate("persisted");
			}
		} catch (PJeBusinessException e) {
			reportMessage(e);
		}
		
		return null;
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		PessoaHome.instance().setId(id);
		if (changed) {
			if (getInstance().getMunicipioNascimento() != null) {
				estado = getInstance().getMunicipioNascimento().getEstado();
			}
			getTestemunhaMunicipioSuggestBean().setInstance(getInstance().getMunicipioNascimento());
		}
		if (id == null) {
			getTestemunhaMunicipioSuggestBean().setInstance(null);
			estado = null;
		}
		/*
		 * if (getInstance().getMunicipioNascimento() != null) { if ((!changed)
		 * && (id != null) && (estado != getInstance().getMunicipioNascimento()
		 * .getEstado())) { estado =
		 * getInstance().getMunicipioNascimento().getEstado();
		 * getTestemunhaMunicipioSuggestBean().setInstance(
		 * getInstance().getMunicipioNascimento()); } }
		 */
	}

	/*
	 * Método chamado pelo botão cadastroPessoas. Encontra-se na página de
	 * inserção de partes de um processo, tanto na consulta de um processo
	 * quanto no cadastro.
	 */
	public void inserirAba() {
		this.persist();
		inserirAbaParteTestemunha();
	}

	/**
	 * Removido - PESSOANEW Testemunha é incluída como representante ou como
	 * participante na nova modelagem
	 * 
	 */
	public void inserirAbaParteTestemunha() {

		/*
		 * if (ProcessoTrfHome.instance().getInstance().getClasseJudicial() !=
		 * null) { if (ProcessoParteHome.instance().getTipoParte() != null) {
		 * ProcessoParteHome.instance().inserir(getInstance());
		 * 
		 * refreshGrid("cadastroPartesGrid");
		 * refreshGrid("processoPoloAtivoGrid");
		 * refreshGrid("processoPoloPassivoGrid");
		 * refreshGrid("cadastroPartesGrid");
		 * 
		 * ProcessoParteHome.instance().setTipoParte(null);
		 * 
		 * FacesMessages.instance().clear(); FacesMessages.instance().add(
		 * "Pessoa inserida no Processo com sucesso"); } if
		 * (ProcessoAudienciaPessoaHome.instance().getInstance()
		 * .getPessoaRepresentante() != null) {
		 * ProcessoAudienciaPessoaHome.instance().inserir(getInstance());
		 * refreshGrid("processoPoloAtivoTestemunhaGrid");
		 * refreshGrid("processoPoloPassivoTestemunhaGrid");
		 * refreshGrid("cadastroTestemunhasGrid");
		 * 
		 * ProcessoAudienciaPessoaHome.instance().getInstance()
		 * .setPessoaRepresentante(null);
		 * 
		 * FacesMessages.instance().clear(); FacesMessages.instance().add(
		 * "Testemunha inserida no Processo com sucesso"); } newInstance(); }
		 */

	}

	public void checkCPF() {
		Boolean cpfJaCadastrado = PessoaAdvogadoHome.instance().checkCPF(getInstance().getNumeroCPF(),getInstance().getIdUsuario());
//		REMOVIDO IF QUE NAO FAZIA NADA.
		
	}

	public SexoEnum[] getSexoValues() {
		return SexoEnum.values();
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		instance.setMunicipioNascimento(getTestemunhaMunicipioSuggestBean().getInstance());
		return super.beforePersistOrUpdate();
	}

	public String gravarEndereco() {
		EnderecoHome.instance().getInstance().setUsuario(instance);
		String output = EnderecoHome.instance().persist();
		refreshGrid("enderecoTestemunhaGrid");
		return output;
	}

	public void removeEndereco(Endereco obj) {
		EnderecoHome.instance().remove(obj);
		refreshGrid("enderecoTestemunhaGrid");
	}

}