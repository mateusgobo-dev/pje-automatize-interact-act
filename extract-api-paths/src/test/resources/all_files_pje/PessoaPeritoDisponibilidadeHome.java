package br.com.infox.cliente.home;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.infox.cliente.component.suggest.PessoaPeritoSuggestBean;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaPerito;
import br.jus.pje.nucleo.entidades.PessoaPeritoDisponibilidade;
import br.jus.pje.nucleo.entidades.PessoaServidor;
import br.jus.pje.nucleo.enums.SemanaEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name("pessoaPeritoDisponibilidadeHome")
@BypassInterceptors
public class PessoaPeritoDisponibilidadeHome extends AbstractPessoaPeritoDisponibilidadeHome<PessoaPeritoDisponibilidade> {

	private static final long serialVersionUID = -5939399608014556230L;
	private PessoaPerito pps;
	private String pessoaPeritoEspecialidade;
	private Boolean formaAtendimento = Boolean.TRUE;

	public SemanaEnum[] getSemanaEnumValues() {
		return SemanaEnum.values();
	}

	public Boolean getFormaAtendimento() {
		return formaAtendimento;
	}

	public void setFormaAtendimento(Boolean formaAtendimento) {
		this.formaAtendimento = formaAtendimento;
	}

	@Override
	public void newInstance() {
		Contexts.removeFromAllContexts("pessoaPeritoSuggest");
		super.newInstance();
	}

	public PessoaPerito getPps() {
		if (Authenticator.isPapelPerito()) {
			pps = ((PessoaFisica) Authenticator.getPessoaLogada()).getPessoaPerito();
		}
		return pps;
	}
	
	public void setPps(PessoaPerito pps) {
		this.pps = pps;
	}

	public static PessoaPeritoDisponibilidadeHome instance() {
		return ComponentUtil.getComponent("pessoaPeritoDisponibilidadeHome");
	}

	public Boolean verificarServidor(Pessoa pessoa) {
		if (Pessoa.instanceOf(pessoa, PessoaServidor.class)) {
			return true;
		}
		return false;
	}

	public Boolean verificarPerito(Pessoa pessoa) {
		if (Pessoa.instanceOf(pessoa, PessoaPerito.class)) {
			return true;
		}
		return false;
	}

	private PessoaPeritoSuggestBean getPessoaPeritoSuggest() {
		PessoaPeritoSuggestBean pessoaPeritoSuggest = (PessoaPeritoSuggestBean) Component
				.getInstance("pessoaPeritoSuggest");
		return pessoaPeritoSuggest;
	}

	@SuppressWarnings("unchecked")
	public List<PessoaPeritoDisponibilidade> getDisponibilidadeDiaList(SemanaEnum dia) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT pd FROM PessoaPeritoDisponibilidade pd ");
		sb.append("WHERE pd.pessoaPeritoEspecialidade = :pessoaPeritoEspecialidade ");
		sb.append("AND pd.diaSemana = :diaSemana ");
		sb.append("AND pd.ativo = true ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("pessoaPeritoEspecialidade", getInstance().getPessoaPeritoEspecialidade());
		q.setParameter("diaSemana", dia);
		return q.getResultList();
	}

	/**
	 * Gravar os mesmos dados da instacia em resgistro diferentes, só alterando
	 * os dias da semana que foi marcado na ComboBox
	 */
	public void gravarVarios() {
		PessoaPeritoDisponibilidade ppDispo = getInstance();
		Integer listSize = ppDispo.getDiaSemanaList().size();
		List<String> diaList = ppDispo.getDiaSemanaList();
		SemanaEnum dia;
		boolean diasDisponiveis = true;
		if (isHoraValida() && isAtendimentoValido()) {
			for (int i = 0; i < listSize; i++) {
				dia = SemanaEnum.valueOf(diaList.get(i));
				diasDisponiveis = existeDisponibilidade(getDisponibilidadeDiaList(dia));
			}
			if (diasDisponiveis) {
				for (int i = 0; i < listSize; i++) {
					FacesMessages.instance().clear();
					getInstance().setPessoaPeritoEspecialidade(ppDispo.getPessoaPeritoEspecialidade());
					getInstance().setDiaSemana(SemanaEnum.valueOf(diaList.get(i)));
					getInstance().setHoraInicio(ppDispo.getHoraInicio());
					getInstance().setHoraFim(ppDispo.getHoraFim());
					getInstance().setQntAtendimento(ppDispo.getQntAtendimento());
					getInstance().setIgnoraFeriado(ppDispo.getIgnoraFeriado());
					getInstance().setIntervalo(ppDispo.getIntervalo());
					getInstance().setAtivo(ppDispo.getAtivo());
					persist(getInstance());
				}
			} else {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Horário indisponível.");
			}
		}
	}

	@SuppressWarnings("static-access")
	public boolean existeDisponibilidade(List<PessoaPeritoDisponibilidade> diaList, PessoaPeritoDisponibilidade instance) {
		for (PessoaPeritoDisponibilidade pp : diaList) {
			if ((pp.getDiaSemana().equals(instance.getDiaSemana().valueOf(pp.getDiaSemana().toString()))
					&& ((pp.getHoraInicio().equals(instance.getHoraInicio())
							|| pp.getHoraInicio().equals(instance.getHoraFim())
							|| pp.getHoraFim().equals(instance.getHoraInicio()) || pp.getHoraFim().equals(
							instance.getHoraFim()))
							|| (DateUtil.isBetweenHours(pp.getHoraInicio(), instance.getHoraInicio(),
									instance.getHoraFim()) || DateUtil.isBetweenHours(pp.getHoraFim(),
									instance.getHoraInicio(), instance.getHoraFim())) || (DateUtil.isBetweenHours(
							instance.getHoraInicio(), pp.getHoraInicio(), pp.getHoraFim()) || DateUtil.isBetweenHours(
							instance.getHoraFim(), pp.getHoraInicio(), pp.getHoraFim()))) && (instance
					.getPessoaPeritoEspecialidade().getPessoaPerito().equals(pp.getPessoaPeritoEspecialidade()
					.getPessoaPerito())))) {
				return false;
			}
		}
		return true;
	}

	public boolean existeDisponibilidade(List<PessoaPeritoDisponibilidade> diaList) {
		for (PessoaPeritoDisponibilidade pp : diaList) {
			Date horaFim = getInstance().getHoraFim();
			Date horaInicio = getInstance().getHoraInicio();

			Date horaInicioPP = pp.getHoraInicio();
			Date horaFimPP = pp.getHoraFim();

			if ((horaInicio.equals(horaInicioPP) && horaFim.equals(horaFimPP))
					|| (horaInicio.before(horaInicioPP) && horaFim.after(horaInicioPP))
					|| (horaInicio.after(horaInicioPP) && horaFim.before(horaFimPP))
					|| (horaInicio.before(horaFimPP) && horaFim.after(horaFimPP))) {
				return false;
			}
		}
		return true;
	}

	private boolean isHoraValida() {
		if (getInstance().getHoraInicio() != null && getInstance().getHoraFim() != null
				&& getInstance().getHoraInicio().after(getInstance().getHoraFim())) {

			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "A hora final tem que ser maior que a hora inicial");
			return false;
		}
		return true;
	}

	private boolean isAtendimentoValido() {
		if (instance.getQntAtendimento() != null && instance.getQntAtendimento() <= 0) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "A quantidade de perícias tem que ser maior que 0");
			return false;
		} else if (instance.getIntervalo() != null && (instance.getMinutos() <= 0 || 
			DateUtil.convertToMinutes(instance.getIntervalo()) > DateUtil.convertToMinutes(instance.getHoraInicio(), instance.getHoraFim()))) {
			
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, String.format(
				"A duração média da perícia tem que ser maior que 0 e menor ou igual a %s", 
				DateUtil.convertToMinutes(instance.getHoraInicio(), instance.getHoraFim())));
			
			return false;
		}
		return true;
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		if (isHoraValida() && isAtendimentoValido()) {
			if (formaAtendimento) {
				getInstance().setIntervalo(null);
			} else {
				getInstance().setQntAtendimento(null);
			}
			return super.beforePersistOrUpdate();	
		}
		return false;
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (changed) {
			if (getInstance().getPessoaPeritoEspecialidade() != null) {
				pps = getInstance().getPessoaPeritoEspecialidade().getPessoaPerito();
				getPessoaPeritoSuggest().setInstance(getInstance().getPessoaPeritoEspecialidade().getPessoaPerito());
			}

			if (getInstance().getQntAtendimento() != null) {
				formaAtendimento = Boolean.TRUE;
			} else if (getInstance().getIntervalo() != null) {
				formaAtendimento = Boolean.FALSE;
			}
		}
		if (id == null) {
			getPessoaPeritoSuggest().setInstance(null);
			pps = null;
		}
	}

	public void setPessoaPeritoEspecialidade(String pessoaPeritoEspecialidade) {
		this.pessoaPeritoEspecialidade = pessoaPeritoEspecialidade;
	}

	public String getPessoaPeritoEspecialidade() {
		return pessoaPeritoEspecialidade;
	}

}