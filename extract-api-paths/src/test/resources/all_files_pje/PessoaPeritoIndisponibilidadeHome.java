package br.com.infox.cliente.home;

import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.component.suggest.PessoaPeritoSuggestBean;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaPerito;
import br.jus.pje.nucleo.entidades.PessoaPeritoEspecialidade;
import br.jus.pje.nucleo.entidades.PessoaPeritoIndisponibilidade;
import br.jus.pje.nucleo.util.DateUtil;

@Name("pessoaPeritoIndisponibilidadeHome")
@BypassInterceptors
public class PessoaPeritoIndisponibilidadeHome extends AbstractPessoaPeritoIndisponibilidadeHome<PessoaPeritoIndisponibilidade> {

	private static final long serialVersionUID = 1L;
	private String pessoaPeritoEspecialidade;
	private boolean verificaIndisponivel = false;
	private PessoaPerito pps;
	private boolean especialidade = false;
	private String msg;

	@Override
	public String update() {
		if (!isDatasValidas() || !isHorasValidas()) {
			return null;
		}
		if (isPericiaMarcada()) {
			verificaIndisponivel = true;
		} else if (isIndisponibilidadeMarcada()) {
			FacesMessages.instance().add(Severity.ERROR, "Indisponibilidade já cadastrada");
		} else {
			return super.update();
		}
		getEntityManager().refresh(instance);
		return null;
	}

	private Boolean isPericiaMarcada() {
		String query = "select count(o) from ProcessoPericia o " + "inner join o.pessoaPerito pp "
				+ "where cast(o.dataMarcacao as date) between cast(:dataInicio as date) and cast(:dataFim as date) "
				+ "and pp.idUsuario = :nome and o.especialidade.especialidade = :especialidade";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("dataInicio", getInstance().getDtInicio());
		q.setParameter("dataFim", getInstance().getDtFim());
		q.setParameter("nome", getInstance().getPessoaPeritoEspecialidade().getPessoaPerito().getIdUsuario());
		q.setParameter("especialidade", getInstance().getPessoaPeritoEspecialidade().getEspecialidade().getEspecialidade());
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	private Boolean isIndisponibilidadeMarcada() {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from PessoaPeritoIndisponibilidade o where ");
		sb.append(" (((cast(:dataInicio as date) between o.dtInicio and o.dtFim and ");
		sb.append(" ((cast(:horaInicio as time) between o.horaInicio and o.horaFim ");
		sb.append(" or cast(:horaFim as time) between o.horaInicio and o.horaFim ) or ");
		sb.append(" (o.horaInicio between cast(:horaInicio as time) and cast(:horaFim as time) or ");
		sb.append(" o.horaFim between cast(:horaInicio as time) and cast(:horaFim as time)))) or ");
		sb.append(" (cast(:dataFim as date) between o.dtInicio and o.dtFim and ");
		sb.append(" ((cast(:horaInicio as time) between o.horaInicio and o.horaFim ");
		sb.append(" or cast(:horaFim as time) between o.horaInicio and o.horaFim ) or ");
		sb.append(" (o.horaInicio between cast(:horaInicio as time) and cast(:horaFim as time) or ");
		sb.append(" o.horaFim between cast(:horaInicio as time) and cast(:horaFim as time))))) ");
		sb.append(" or (o.dtInicio >= :dataInicio and o.dtFim <= :dataFim)) ");
		sb.append(" and o.pessoaPeritoEspecialidade.pessoaPerito.idUsuario = :idUsuario ");
		sb.append(" and o.pessoaPeritoEspecialidade.especialidade.especialidade = :especialidade ");
		sb.append(" and o.ativo = true ");
		if (isManaged()) {
			sb.append(" and o.idPeritoIndisponibilidade <> :id");
		}
		Query query2 = getEntityManager().createQuery(sb.toString());
		query2.setParameter("dataInicio", getInstance().getDtInicio());
		query2.setParameter("dataFim", getInstance().getDtFim());
		query2.setParameter("horaInicio", getInstance().getHoraInicio());
		query2.setParameter("horaFim", getInstance().getHoraFim());
		query2.setParameter("idUsuario", getInstance().getPessoaPeritoEspecialidade().getPessoaPerito().getIdUsuario());
		query2.setParameter("especialidade", getInstance().getPessoaPeritoEspecialidade().getEspecialidade().getEspecialidade());
		if (isManaged()) {
			query2.setParameter("id", getInstance().getIdPeritoIndisponibilidade());
		}
		try {
			Long retorno = (Long) query2.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	@SuppressWarnings("unchecked")
	public void validarIndisponibilidade() {
		FacesMessages.instance().clear();
		if (getInstance().getDtInicio() != null) {
			if (!isDatasValidas() || !isHorasValidas()) {
				return;
			}
			if (especialidade) {
				String query = "select o from PessoaPeritoEspecialidade o where o.pessoaPerito = :pps";
				Query q = getEntityManager().createQuery(query);
				q.setParameter("pps", getPps());
				List<PessoaPeritoEspecialidade> ppe = q.getResultList();
				PessoaPeritoIndisponibilidade obj = getInstance();
				boolean msgIndisponibilidadeMarcada = false;
				for (int i = 0; i < ppe.size(); i++) {
					String query2 = "select count(o) from ProcessoPericia o "
							+ "inner join o.pessoaPerito pp "
							+ "where cast(o.dataMarcacao as date) between cast(:dataInicio as date) and cast(:dataFim as date) "
							+ "and pp.idUsuario = :nome and o.especialidade.especialidade = :especialidade";
					Query q2 = getEntityManager().createQuery(query2);
					q2.setParameter("dataInicio", obj.getDtInicio());
					q2.setParameter("dataFim", obj.getDtFim());
					q2.setParameter("nome", ppe.get(i).getPessoaPerito().getIdUsuario());
					q2.setParameter("especialidade", ppe.get(i).getEspecialidade().getEspecialidade());
					Long retorno = 0L;
					try {
						retorno = (Long) q2.getSingleResult();
					} catch (NoResultException no) {							
					}
					if (retorno > 0) {
						FacesMessages
								.instance()
								.add(StatusMessage.Severity.ERROR,
										"Existem perícias marcadas para este período. Entre em contato com a Seção Judiciaria mais próxima.");
					} else {
						getInstance().setPessoaPeritoEspecialidade(ppe.get(i));
						getInstance().setIndisponibilidade(obj.getIndisponibilidade());
						getInstance().setDtInicio(obj.getDtInicio());
						getInstance().setDtFim(obj.getDtFim());
						getInstance().setHoraInicio(obj.getHoraInicio());
						getInstance().setHoraFim(obj.getHoraFim());
						getInstance().setDtCadastro(new Date());
						getInstance().setAtivo(obj.getAtivo());
						
						if (isIndisponibilidadeMarcada()) {
							if (msgIndisponibilidadeMarcada==false){
								FacesMessages.instance().add(Severity.ERROR, "Indisponibilidade já cadastrada");
								msgIndisponibilidadeMarcada=true;
							}
						} else {
							FacesMessages.instance().clear();
							if (msg == null) {
								msg = "Indisponibilidade Cadastrada para a(s) seguinte(s) Especialildade(s): \n";
							}
							msg += ppe.get(i).getEspecialidade().getEspecialidade() + ".\n ";
							persist(getInstance());
							msgIndisponibilidadeMarcada=true;
						}
					}
				}
			} else {
				String query = "select count(o) from ProcessoPericia o " + "inner join o.pessoaPerito pp "
						+ "where o.dataMarcacao between :dataInicio and :dataFim "
						+ "and pp.idUsuario = :nome and o.especialidade.especialidade = :especialidade";
				Query q = getEntityManager().createQuery(query);
				q.setParameter("dataInicio", getInstance().getDtInicio());
				q.setParameter("dataFim", getInstance().getDtFim());
				q.setParameter("nome", getInstance().getPessoaPeritoEspecialidade().getPessoaPerito()
						.getIdUsuario());
				q.setParameter("especialidade", getInstance().getPessoaPeritoEspecialidade().getEspecialidade().getEspecialidade());
				Long retorno = 0L;
				try {
					retorno = (Long) q.getSingleResult();
				} catch (NoResultException no) {
				}
				if (retorno > 0) {
					FacesMessages
							.instance()
							.add(StatusMessage.Severity.ERROR,
									"Existem perícias marcadas para este período. Entre em contato com a Seção Judiciaria mais próxima.");
				} else {
					persist();
				}
			}
		}
	}

	@Override
	public void newInstance() {
		Contexts.removeFromAllContexts("pessoaPeritoSuggest");
		super.newInstance();
	}

	public static PessoaPeritoIndisponibilidadeHome instance() {
		return ComponentUtil.getComponent("pessoaPeritoIndisponibilidadeHome");
	}

	private PessoaPeritoSuggestBean getPessoaPeritoSuggest() {
		return (PessoaPeritoSuggestBean) Component.getInstance("pessoaPeritoSuggest");
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		// Para o uso no cadastro de Localização que invoca o home
		PessoaHome.instance().setId(id);
		if (changed && getInstance().getPessoaPeritoEspecialidade() != null) {
			pps = getInstance().getPessoaPeritoEspecialidade().getPessoaPerito();
			getPessoaPeritoSuggest().setInstance(getInstance().getPessoaPeritoEspecialidade().getPessoaPerito());
		}
		if (id == null) {
			getPessoaPeritoSuggest().setInstance(null);
			pps = null;
		}
	}

	public String getEspecialidadePerito() {
		String result = "";
		if (getInstance().getPessoaPeritoEspecialidade() != null) {
			result = getInstance().getPessoaPeritoEspecialidade().getEspecialidade().getEspecialidade();
		}
		return result;
	}

	public Integer idPeritoI() {
		Integer result = 0;
		if (getInstance() != null) {
			result = getInstance().getIdPeritoIndisponibilidade();
		}
		return result;
	}

	public Integer getIdPerito() {
		Integer result = 0;
		if (getInstance().getPessoaPeritoEspecialidade() != null) {
			result = getInstance().getPessoaPeritoEspecialidade().getPessoaPerito().getIdUsuario();
		}
		return result;
	}

	/**
	 * Método responsável por verificar se a data informada no cadastro está de acordo com as regras.
	 * 
	 * @return Verdadeiro se:
	 * <ul>
	 * 		<li>A data inicial é posterior a data final.</li>
	 * 		<li>A data final é posterior a data atual, caso seja uma alteração.</li>
	 * </ul>
	 * Falso, caso contrário.
	 */
	private boolean isDatasValidas() {
		boolean retorno = true;
		
		PessoaPeritoIndisponibilidade ppi = this.getInstance();
		if (ppi.getDtFim().compareTo(ppi.getDtInicio()) < 0) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"Data final anterior à data de início. Escolha uma data final posterior ou igual à data inicial.");
			
			retorno = false;
		}
		
		if (isManaged()) {
			if (ppi.getDtFim().compareTo(DateUtil.getDataAtual()) < 0) {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, 
						"Data final anterior à data de hoje. Escolha uma data final posterior ou igual à data de hoje.");
				
				retorno = false;
			}
		}
		return retorno;
	}
	
	/**
	 * Método responsável por verificar se a hora informada no cadastro está de acordo com as regras.
	 * 
	 * @return Verdadeiro se a hora inicial é anterior a hora final.
	 */
	private boolean isHorasValidas() {
		boolean retorno = true;
		
		PessoaPeritoIndisponibilidade ppi = this.getInstance();
		if (ppi.getHoraInicio() != null && ppi.getHoraFim() != null && ppi.getHoraFim().before(ppi.getHoraInicio())) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, 
					"A hora inicial deve ser anterior à hora final.");
			
			retorno = false;
		}
		return retorno;
	}
	
	public PessoaPerito getPps() {
		if (Authenticator.isPapelPerito()) {
			pps = ((PessoaFisica) Authenticator.getPessoaLogada()).getPessoaPerito();
		}
		return pps;
	}

	public void setPps(PessoaPerito pp) {
		this.pps = pp;
	}
	
	public void setPessoaPeritoEspecialidade(String pessoaPeritoEspecialidade) {
		this.pessoaPeritoEspecialidade = pessoaPeritoEspecialidade;
	}

	public String getPessoaPeritoEspecialidade() {
		return pessoaPeritoEspecialidade;
	}
	
	public boolean getEspecialidade() {
		return especialidade;
	}

	public void setEspecialidade(boolean especialidade) {
		this.especialidade = especialidade;
	}

	public boolean getVerificaIndisponivel() {
		return verificaIndisponivel;
	}

	public void setVerificaIndisponivel(boolean verificaIndisponivel) {
		this.verificaIndisponivel = verificaIndisponivel;
	}
	
	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}
