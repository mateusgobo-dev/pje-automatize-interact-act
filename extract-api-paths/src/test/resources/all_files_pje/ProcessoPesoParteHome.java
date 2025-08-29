package br.com.infox.cliente.home;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.ProcessoPesoParte;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

@Name("processoPesoParteHome")
@BypassInterceptors
public class ProcessoPesoParteHome extends AbstractHome<ProcessoPesoParte> {

	private static final long serialVersionUID = 1L;

	public static ProcessoPesoParteHome instance() {
		return ComponentUtil.getComponent("processoPesoParteHome");
	}

	public void setProcessoPesoParteIdProcessoPesoParte(Integer id) {
		setId(id);
	}

	public Integer getProcessoPesoParteIdProcessoPesoParte() {
		return (Integer) getId();
	}

	public ProcessoParteParticipacaoEnum[] getPoloEnumValues() {
		return ProcessoParteParticipacaoEnum.values();
	}

	@Override
	public String persist() {
		String ret = super.persist();
		refreshGrid("processoPesoParteGrid");
		return ret;
	}

	@Override
	public String inactive(ProcessoPesoParte obj) {
		setInstance(obj);
		String ret = super.remove();
		refreshGrid("processoPesoParteGrid");
		return ret;
	}

	@Override
	public String update() {
		String ret = super.update();
		if (ret != null) {
			refreshGrid("processoPesoParteGrid");
		}
		return ret;
	}

	@Override
	public String remove(ProcessoPesoParte obj) {
		setInstance(obj);
		String ret = super.remove(obj);
		refreshGrid("processoPesoParteGrid");
		newInstance();
		return ret;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected boolean beforePersistOrUpdate() {
		if (instance.getNumeroPartesFinal() != null
				&& instance.getNumeroPartesFinal() <= instance.getNumeroPartesInicial()) {
			FacesMessages.instance().add(Severity.ERROR,
					"A quantidade de partes final não pode ser menor que a quantidade inicial.");
			return false;
		}

		String query = "select o from ProcessoPesoParte o "
				+ "where ( :numeroInicial between numeroPartesInicial and numeroPartesFinal "
				+ "or :numeroFinal between o.numeroPartesInicial and o.numeroPartesFinal "
				+ "or (:numeroInicial >= o.numeroPartesInicial and o.numeroPartesFinal is null) "
				+ "or (:numeroFinal >= o.numeroPartesInicial and o.numeroPartesFinal is null) "
				+ "or (:numeroFinal is null and o.numeroPartesFinal is null)) "
				+ "and (o.inPolo = :inPolo or o.inPolo = 'T' or :inPolo = 'T') "
				+ "and o.idProcessoPesoParte <> :idProcessoPesoParte";

		Query q = getEntityManager().createQuery(query);
		q.setParameter("numeroInicial", instance.getNumeroPartesInicial());
		q.setParameter("numeroFinal", instance.getNumeroPartesFinal());
		q.setParameter("idProcessoPesoParte", instance.getIdProcessoPesoParte());
		q.setParameter("inPolo", instance.getInPolo());
		List<ProcessoPesoParte> list = q.getResultList();
		if (list.size() > 0) {
			FacesMessages.instance().add(Severity.ERROR,
					"O intervalo informado já está contido em um ou mais intervalos previamente cadastrados.");
			return false;
		}

		if (instance.getNumeroPartesInicial() != 0 && instance.getNumeroPartesInicial() != 1) {
			query = "select o from ProcessoPesoParte o " + "where :numeroInicial - o.numeroPartesFinal = 1 "
					+ "and (o.inPolo = :inPolo or o.inPolo = 'T' or :inPolo = 'T') "
					+ "and o.idProcessoPesoParte <> :idProcessoPesoParte";

			Query q2 = getEntityManager().createQuery(query);
			q2.setParameter("numeroInicial", instance.getNumeroPartesInicial());
			q2.setParameter("idProcessoPesoParte", instance.getIdProcessoPesoParte());
			q2.setParameter("inPolo", instance.getInPolo());
			list = q2.getResultList();
			if (list.size() == 0) {
				FacesMessages.instance().add(
						Severity.ERROR,
						String.format("Não existe intervalo já cadastrado com quantidade de partes final igual a %d.",
								instance.getNumeroPartesInicial() - 1));
				return false;
			}
		}

		return super.beforePersistOrUpdate();
	}

}
