package br.com.infox.cliente.home;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.bean.PreCadastroPessoaBean;
import br.com.infox.ibpm.entity.log.LogUtil;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.MeioContato;
import br.jus.pje.nucleo.entidades.Usuario;

@Name("meioContatoHome")
@BypassInterceptors
public class MeioContatoHome extends AbstractMeioContatoHome<MeioContato> {

	private static final long serialVersionUID = 1L;

	@Override
	public String persist() {
		Usuario pessoaLogada = (Usuario) Contexts.getSessionContext().get("usuarioLogado");
		getInstance().setUsuarioCadastrador(pessoaLogada);
		String persist = "";

		persist = super.persist();

		if (persist != null) {
			EntityUtil.getEntityManager().refresh(instance);
			instance.getPessoa();
			instance.getPessoa().getMeioContatoList().add(instance);
		}

		refreshGrid("meioContatoGrid");
		refreshGrid("processoParteVinculoPessoaMeioContatoGrid");
		return persist;
	}

	public void persistInTestemunha() {
		instance.setPessoa(PessoaFisicaHome.instance().getInstance());
		persistInPessoa();
	}

	public static MeioContatoHome instance() {
		return ComponentUtil.getComponent("meioContatoHome");
	}

	public String persistInPessoa() {
		String ret = null;
		if (instance.getPessoa() == null) {
			PreCadastroPessoaBean pbean = getPreCadastroPessoaBean();
			if (pbean.getPessoa() == null) {
				instance.setPessoa(PessoaHome.instance().getInstance());
			} else {
				instance.setPessoa(pbean.getPessoa());
			}
		}

		MeioContatoHome.instance().getInstance().setTipoContato(instance.getTipoContato());
		MeioContatoHome.instance().getInstance().setValorMeioContato(instance.getValorMeioContato());

		StringBuilder sb = new StringBuilder();
		sb.append("select mc from MeioContato mc ");
		sb.append("where mc.tipoContato = :tipoContato ");
		sb.append("and mc.valorMeioContato = :valorMeioContato ");
		sb.append("and mc.pessoa = :pessoa ");
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter("tipoContato", getInstance().getTipoContato());
		q.setParameter("valorMeioContato", getInstance().getValorMeioContato());
		q.setParameter("pessoa", getInstance().getPessoa());
		MeioContato meioContatoTemp = (MeioContato) EntityUtil.getSingleResult(q);

		if (meioContatoTemp != null) {
			FacesMessages.instance().add(Severity.INFO, "Meio de contato já cadastrado.");
			return ret;
		}

		if (!valida(getInstance())) {
			FacesMessages.instance().add(Severity.INFO, "Valor inválido para o tipo " + getInstance().getTipoContato().getTipoContato());
			return ret;
		}

		ret = MeioContatoHome.instance().persist();

		Usuario pessoaLogada = (Usuario) Contexts.getSessionContext().get("usuarioLogado");

		// setando o usuário que alterou/incluiu o meio de contato
		getInstance().setUsuarioCadastrador(pessoaLogada);

		EntityUtil.getEntityManager().flush();
		refreshGrid("meioContatoPessoaGrid");
		refreshGrid("processoParteVinculoPessoaMeioContatoGrid");
		setId(null);
		instance = createInstance();
		return ret;
	}

	@Override
	public String remove(MeioContato obj) {
		return removeInPessoa(obj);
	}

	public String removeInPessoa(MeioContato meio) {
		setInstance(meio);
		PreCadastroPessoaBean pbean = getPreCadastroPessoaBean();
		if (instance.getPessoa() == null) {
			instance.setPessoa(pbean.getPessoa());
		}
		instance.getPessoa().getMeioContatoList().remove(meio);
		EntityUtil.flush();
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Registro removido com Sucesso.");
		refreshGrid("meioContatoPessoaGrid");
		refreshGrid("processoParteVinculoPessoaMeioContatoGrid");
		setId(null);
		instance = createInstance();
		return "removed";
	}

	@Override
	public String update() {
		String ret = null;
		if (!valida(getInstance())) {
			FacesMessages.instance().add(Severity.INFO, "Valor inválido para o tipo " + getInstance().getTipoContato().getTipoContato());
			EntityUtil.getEntityManager().refresh(instance);
			return ret;
		}
		refreshGrid("meioContatoPessoaGrid");
		ret = super.update();
		newInstance();
		return ret;
	}

	/**
	 * Valida o meio de contato de acordo com seu tipo.
	 * 
	 * @param meioContato
	 * @return
	 */
	private boolean valida(MeioContato meioContato) {
		String valorMeioContato = meioContato.getValorMeioContato();
		String regex = meioContato.getTipoContato().getRegexValidacao();

		boolean validado = true;
		if (regex != null) {
			validado = valorMeioContato.matches(regex);
		}
		return validado;
	}
	
	private PreCadastroPessoaBean getPreCadastroPessoaBean() {
		return (PreCadastroPessoaBean) ComponentUtil.getComponent("preCadastroPessoaBean");
	}
	
	public Integer getIdUsuario() {
		if (getInstance().getPessoa() == null) {
			PreCadastroPessoaBean preCadastroPessoaBean = getPreCadastroPessoaBean();
			if (preCadastroPessoaBean.getPessoa() == null) {
				return PessoaHome.instance().getInstance().getIdPessoa();
			} else {
				return preCadastroPessoaBean.getPessoa().getIdPessoa();
			}
		}
		return getInstance().getPessoa().getIdPessoa();
	}

	/**
	 * Método responsável por verificar se o meio de contato possui uma máscara cadastrada.
	 * 
	 * @return Verdadeiro se o meio de contato possui uma máscara cadastrada. Falso, caso contrário.
	 */
	public boolean temMascara() {
		return StringUtils.isNotBlank(obterMascara());
	}
	
	/**
	 * Método responsável por obter a máscara do meio de contato.
	 *  
	 * @return A máscara do meio de contato.
	 */
	public String obterMascara() {
		return (instance != null && instance.getTipoContato() != null) ? instance.getTipoContato().getMascara() : StringUtils.EMPTY;
	}
	
}
