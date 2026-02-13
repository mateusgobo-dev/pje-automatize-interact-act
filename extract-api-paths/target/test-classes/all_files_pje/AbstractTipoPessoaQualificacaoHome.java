package br.com.infox.cliente.home;

import java.util.List;

import org.jboss.seam.Component;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.TipoPessoaQualificacao;
import br.jus.pje.nucleo.entidades.TipoPessoaQualificacaoId;

public abstract class AbstractTipoPessoaQualificacaoHome<T> extends AbstractHome<TipoPessoaQualificacao> {

	private static final long serialVersionUID = 1L;

	public void setTipoPessoaQualificacaoId(TipoPessoaQualificacaoId id) {
		setId(id);
	}

	public TipoPessoaQualificacaoId getTipoPessoaQualificacaoId() {
		return (TipoPessoaQualificacaoId) getId();
	}

	@Override
	public boolean isIdDefined() {
		if (getTipoPessoaQualificacaoId().getIdTipoPessoa() == 0)
			return false;
		if (getTipoPessoaQualificacaoId().getIdQualificacao() == 0)
			return false;
		return true;
	}

	public void setCompositeId(String id) {
		if (id == null || id.indexOf("-") == -1) {
			return;
		}
		TipoPessoaQualificacaoId compositeId = new TipoPessoaQualificacaoId();
		compositeId.setIdTipoPessoa(new Integer(id.split("-")[1]));
		compositeId.setIdQualificacao(new Integer(id.split("-")[1]));
		setId(compositeId);
	}

	public String getCompositeId() {
		if (!isManaged()) {
			return null;
		}
		return instance.getId().getIdTipoPessoa() + "-" + instance.getId().getIdQualificacao();
	}

	@Override
	protected TipoPessoaQualificacao createInstance() {
		TipoPessoaQualificacao tipoPessoaQualificacao = new TipoPessoaQualificacao();
		TipoPessoaHome tipoPessoaHome = (TipoPessoaHome) Component.getInstance("tipoPessoaHome", false);
		if (tipoPessoaHome != null) {
			tipoPessoaQualificacao.setTipoPessoa(tipoPessoaHome.getDefinedInstance());
		}
		QualificacaoHome qualificacaoHome = (QualificacaoHome) Component.getInstance("qualificacaoHome", false);
		if (qualificacaoHome != null) {
			tipoPessoaQualificacao.setQualificacao(qualificacaoHome.getDefinedInstance());
		}
		return tipoPessoaQualificacao;
	}

	@Override
	public String remove() {
		TipoPessoaHome tipoPessoa = (TipoPessoaHome) Component.getInstance("tipoPessoaHome", false);
		if (tipoPessoa != null) {
			tipoPessoa.getInstance().getTipoPessoaQualificacaoList().remove(instance);
		}
		QualificacaoHome qualificacao = (QualificacaoHome) Component.getInstance("qualificacaoHome", false);
		if (qualificacao != null) {
			qualificacao.getInstance().getTipoPessoaQualificacaoList().remove(instance);
		}
		return super.remove();
	}

	@Override
	public String remove(TipoPessoaQualificacao obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("tipoPessoaQualificacaoGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		if (getInstance().getTipoPessoa() != null) {
			List<TipoPessoaQualificacao> tipoPessoaList = getInstance().getTipoPessoa().getTipoPessoaQualificacaoList();
			if (!tipoPessoaList.contains(instance)) {
				getEntityManager().refresh(getInstance().getTipoPessoa());
			}
		}
		if (getInstance().getQualificacao() != null) {
			List<TipoPessoaQualificacao> qualificacaoList = getInstance().getQualificacao()
					.getTipoPessoaQualificacaoList();
			if (!qualificacaoList.contains(instance)) {
				getEntityManager().refresh(getInstance().getQualificacao());
			}
		}
		newInstance();
		return action;
	}

	@Override
	public String update() {
		TipoPessoaQualificacaoId id = new TipoPessoaQualificacaoId();
		if (instance != null) {
			id.setIdQualificacao(instance.getQualificacao().getIdQualificacao());
			if (!id.equals(instance.getId())) {
				remove();
				instance.setId(id);
				return persist();
			}
			return super.update();
		} else {
			return null;
		}
	}

}