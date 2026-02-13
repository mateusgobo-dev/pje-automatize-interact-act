package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.DiaSemana;
import br.jus.pje.nucleo.entidades.Sala;
import br.jus.pje.nucleo.entidades.SalaHorario;

@Name("salaHorarioHome")
@BypassInterceptors
public class SalaHorarioHome extends AbstractHome<SalaHorario> {

	private static final long serialVersionUID = 1L;

	public void setOrgaoJulgadorColegiadoSalaHorarioIdSalaHorario(Integer id) {
		setId(id);
	}

	public Integer getOrgaoJulgadorColegiadoSalaHorarioIdSalaHorario() {
		return (Integer) getId();
	}

	public static SalaHorarioHome instance() {
		return ComponentUtil.getComponent("salaHorarioHome");
	}

	public boolean isHoraValida() {
		if (instance.getHoraInicial() != null && instance.getHoraFinal() != null
				&& instance.getHoraFinal().after(instance.getHoraInicial())) {
			return true;
		} else {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"A hora final tem que ser maior que a hora inicial");
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public List<SalaHorario> getDisponibilidadeDiaList(DiaSemana dia) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o FROM SalaHorario o ");
		sb.append("WHERE o.sala = :sala ");
		sb.append("AND o.diaSemana = :diaSemana");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("sala", SalaHome.instance().getInstance());
		q.setParameter("diaSemana", dia);
		return q.getResultList();
	}

	public boolean existeDisponibilidade(List<SalaHorario> diaList) {
		for (SalaHorario pp : diaList) {
			if (!(instance.getHoraFinal().before(pp.getHoraInicial()) || instance.getHoraInicial().after(
					pp.getHoraFinal()))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String update() {
		String ret = null;
		if (isHoraValida()) {
			ret = super.update();
		}
		
		/**
		 * [PJEII-6879] Implementação de reativação automática da sala ao ativar um de seus horários.
		 * @author fernando.junior (10/04/2013)
		 */
		if ((SalaHome.instance().getInstance().getAtivo() == null || !SalaHome.instance().getInstance().getAtivo()) &&  
				(instance.getAtivo() != null && instance.getAtivo())) {
			SalaHome.instance().getInstance().setAtivo(true);
			SalaHome.instance().update();
		}
		return ret;
	}

	public List<DiaSemana> getDiaSemanaList(List<String> diaList) {
		List<DiaSemana> diaSemana = new ArrayList<DiaSemana>();
		for (int i = 0; i < diaList.size(); i++) {
			StringBuilder sb = new StringBuilder();
			sb.append("select o from DiaSemana o ");
			sb.append("where o.diaSemana = :diaSemana");
			Query q = getEntityManager().createQuery(sb.toString()).setParameter("diaSemana",
					diaList.get(i).replace('[', ' ').replace(']', ' ').trim());
			diaSemana.add((DiaSemana) q.getSingleResult());
		}
		return diaSemana;
	}

	@Override
	public String persist() {
		List<DiaSemana> diaList = getDiaSemanaList(instance.getDiaSemanaList());
		if (!isHoraValida()) {
			return null;
		}
		int e = 0;
		List<DiaSemana> listSec = new ArrayList<DiaSemana>();
		listSec.addAll(diaList);
		for (int i = 0; i < diaList.size(); i++) {
			DiaSemana dia = diaList.get(i);
			if (!existeDisponibilidade(getDisponibilidadeDiaList(dia))) {
				e++;
				listSec.remove(dia);
			}
		}
		diaList = listSec;
		String ret = null;
		for (int i = 0; i < diaList.size(); i++) {
			SalaHorario ojcsh = new SalaHorario();
			ojcsh.setAtivo(getInstance().getAtivo());
			ojcsh.setDiaSemana(diaList.get(i));
			ojcsh.setHoraFinal(getInstance().getHoraFinal());
			ojcsh.setHoraInicial(getInstance().getHoraInicial());
			ojcsh.setSala(SalaHome.instance().getInstance());
			ret = persist(ojcsh);
		}
		if (e != 0 && ret == null) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Horário indisponível.");
		}
		newInstance();
		return ret;
	}

	public String persist(SalaHorario obj) {
		setInstance(obj);
		FacesMessages.instance().clear();
		String ret = super.persist();
		return ret;
	}

	@SuppressWarnings("unchecked")
	public void inativar(Sala obj) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from SalaHorario o where o.sala = :sala");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("sala", obj);
		List<SalaHorario> orgao = q.getResultList();
		for (SalaHorario ojcsh : orgao) {
			ojcsh.setAtivo(false);
			getEntityManager().merge(ojcsh);
			EntityUtil.flush();
		}
//		FacesMessages.instance().clear();
//		FacesMessages.instance().add(StatusMessage.Severity.INFO, "Registro inativado com sucesso!");
	}

	public boolean exibeLixeira(Sala s) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from SalaHorario o where o.sala = :sala ");
		sb.append("and o.ativo = true ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("sala", s);
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	@Override
	public String remove(SalaHorario obj) {
		String ret = super.remove(obj);
		newInstance();
		return ret;
	}

}
