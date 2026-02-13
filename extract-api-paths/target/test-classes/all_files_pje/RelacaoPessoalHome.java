package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.component.suggest.PessoaRepresentadaSuggestBean;
import br.com.infox.cliente.component.suggest.PessoaRepresentanteSuggestBean;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.RelacaoPessoal;

@Name("relacaoPessoalHome")
@BypassInterceptors
public class RelacaoPessoalHome extends AbstractRelacaoPessoalHome<RelacaoPessoal> {

	private static final long serialVersionUID = 1L;

	private PessoaRepresentadaSuggestBean getPessoaRepresentadaSuggest() {
		PessoaRepresentadaSuggestBean pessoaRepresentadaSuggestBean = (PessoaRepresentadaSuggestBean) Component
				.getInstance(PessoaRepresentadaSuggestBean.NAME);
		return pessoaRepresentadaSuggestBean;
	}

	public PessoaRepresentanteSuggestBean getPessoaRepresentanteSuggest() {
		PessoaRepresentanteSuggestBean pessoaRepresentanteSuggestBean = (PessoaRepresentanteSuggestBean) Component
				.getInstance(PessoaRepresentanteSuggestBean.NAME);
		return pessoaRepresentanteSuggestBean;
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		Pessoa pessoaRepresentadaInstance = getInstance().getPessoaRepresentada();
		Pessoa pessoaRepresentanteInstance = getInstance().getPessoaRepresentante();

		if (changed) {
			getPessoaRepresentadaSuggest().setInstance(pessoaRepresentadaInstance);
			getPessoaRepresentanteSuggest().setInstance(pessoaRepresentanteInstance);
		}
		if (id == null) {
			getPessoaRepresentadaSuggest().setInstance(null);
			getPessoaRepresentanteSuggest().setInstance(null);
		}
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		if (!validate()) {
			return false;
		}
		// instance.setPessoaRepresentada(getPessoaRepresentadaSuggest().getInstance());
		// instance.setPessoaRepresentante(getPessoaRepresentanteSuggest().getInstance());
		return super.beforePersistOrUpdate();
	}

	@Override
	public String persist() {
		instance.setPessoaRepresentada(getPessoaRepresentadaSuggest().getInstance());
		instance.setPessoaRepresentante(getPessoaRepresentanteSuggest().getInstance());
		if (verifica()) {
			String ret = super.persist();
			refreshGrid("relacaoPessoalGrid");
			return ret;
		} else {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Data indisponível.");
			return null;
		}

	}

	@Override
	public String update() {
		instance.setPessoaRepresentada(getPessoaRepresentadaSuggest().getInstance());
		instance.setPessoaRepresentante(getPessoaRepresentanteSuggest().getInstance());
		if (verifica()) {
			String ret = super.update();
			refreshGrid("relacaoPessoalGrid");
			return ret;
		} else {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Data indisponível.");
			return null;
		}

	}

	@Override
	public String remove(RelacaoPessoal obj) {
		obj.setAtivo(Boolean.FALSE);
		setInstance(obj);
		String ret = super.update();
		newInstance();
		return ret;
	}

	@Override
	public void newInstance() {
		Contexts.removeFromAllContexts(PessoaRepresentadaSuggestBean.NAME);
		Contexts.removeFromAllContexts(PessoaRepresentanteSuggestBean.NAME);
		super.newInstance();
	}

	private boolean validate() {
		// verifica se a data inicial da relação é menor que a data final
		if (instance.getDataFimRelacao() != null) {
			if (instance.getDataFimRelacao().before(instance.getDataInicioRelacao())) {
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.ERROR,
						"A data final da relação não pode ser menor que a data inicial.");
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public List<RelacaoPessoal> getRelacaoPessoalList() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT rp FROM RelacaoPessoal rp ");
		sb.append("WHERE rp.tipoRelacaoPessoal = :tipoRelacaoPessoal ");
		sb.append("AND rp.pessoaRepresentante = :pessoaRepresentante ");
		sb.append("AND rp.pessoaRepresentada = :pessoaRepresentada ");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("tipoRelacaoPessoal", getInstance().getTipoRelacaoPessoal());
		q.setParameter("pessoaRepresentante", getInstance().getPessoaRepresentante());
		q.setParameter("pessoaRepresentada", getInstance().getPessoaRepresentada());

		return q.getResultList();
	}

	/**
	 * Retorna dados do getRelacaoPessoalList(), verificando o periodo de datas
	 * para poder inserir a Relacao Pessoal
	 */
	public boolean verifica() {
		boolean flag = true;
		if (validate()) {
			List<RelacaoPessoal> relacaoPessoalList = getRelacaoPessoalList();
			List<RelacaoPessoal> listSec = new ArrayList<RelacaoPessoal>();
			listSec.addAll(relacaoPessoalList);
			for (RelacaoPessoal rp : relacaoPessoalList) {
				if (getInstance().getId() == rp.getId()) {
					listSec.remove(rp);
				}
			}
			for (RelacaoPessoal relacaoPessoal : listSec) {
				if (relacaoPessoal.getDataFimRelacao() == null) {
					flag = false;
					FacesMessages.instance().add(Severity.ERROR,
							"Já existe uma relação deste tipo sem data final definida.");
					return flag;
				}
				if (relacaoPessoal.getDataFimRelacao() == null && getInstance().getDataFimRelacao() != null
						&& getInstance().getDataFimRelacao().after(relacaoPessoal.getDataInicioRelacao())) {
					flag = false;
				}

				if (getInstance().getDataFimRelacao() == null && relacaoPessoal.getDataFimRelacao() != null
						&& relacaoPessoal.getDataFimRelacao().after(getInstance().getDataInicioRelacao()))
					flag = false;

				if ((getInstance().getDataFimRelacao() != null)
						&& !(getInstance().getDataFimRelacao().before(relacaoPessoal.getDataInicioRelacao()) || getInstance()
								.getDataInicioRelacao().after(relacaoPessoal.getDataFimRelacao())))
					flag = false;
			}
		}
		return flag;
	}
}
