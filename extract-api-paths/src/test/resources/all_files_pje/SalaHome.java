package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import br.jus.pje.nucleo.entidades.Competencia;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.component.ControleFiltros;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.DiaSemana;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Sala;
import br.jus.pje.nucleo.entidades.SalaHorario;
import br.jus.pje.nucleo.entidades.TipoAudiencia;
import br.jus.pje.nucleo.enums.SalaEnum;

@Name("salaHome")
@BypassInterceptors
public class SalaHome extends AbstractHome<Sala> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1302489035700112134L;
	private Boolean tipoOrgaoJulgador;
	private TipoAudiencia tipoAudiencia;
	private Boolean isPrimeiraSalaAudienciaOrgao = null;
	private boolean todasCompetencias;

	public SalaEnum[] getSalaEnumValues() {
		return SalaEnum.values();
	}

	public void setSalaIdSala(Integer id) {
		setId(id);
	}

	public Integer getSalaIdSala() {
		return (Integer) getId();
	}

	public static SalaHome instance() {
		return ComponentUtil.getComponent("salaHome");
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		if (instance.getTipoSala().equals(SalaEnum.S)) {
			instance.setOrgaoJulgador(null);

			String sql = "select o.idSala from Sala o where o.orgaoJulgadorColegiado = :orgaoJulgadorColegiado and o.sala = :sala";
			if (isManaged()) {
				sql += " and o.idSala <> :id";
			}
			Query query = getEntityManager().createQuery(sql);
			query.setParameter("orgaoJulgadorColegiado", instance.getOrgaoJulgadorColegiado());
			query.setParameter("sala", instance.getSala());
			if (isManaged()) {
				query.setParameter("id", instance.getIdSala());
			}
			Object result = EntityUtil.getSingleResult(query);
			if (result != null) {
				FacesMessages.instance().add(Severity.ERROR, "Sala já cadastrada para este Orgão Julgador Colegiado");
				return false;
			}
		} else {
			instance.setOrgaoJulgadorColegiado(null);
			String sql = "select o.idSala from Sala o where o.orgaoJulgador = :orgaoJulgador and o.sala = :sala";
			if (isManaged()) {
				sql += " and o.idSala <> :id";
			}
			Query query = getEntityManager().createQuery(sql);
			query.setParameter("orgaoJulgador", instance.getOrgaoJulgador());
			query.setParameter("sala", instance.getSala());
			if (isManaged()) {
				query.setParameter("id", instance.getIdSala());
			}
			Object result = EntityUtil.getSingleResult(query);
			if (result != null) {
				FacesMessages.instance().add(Severity.ERROR, "Sala já cadastrada para este Orgão Julgador");
				return false;
			}
		}

		return super.beforePersistOrUpdate();
	}

	@Override
	public String persist() {
		if (instance.getOrgaoJulgadorColegiado() == null && instance.getOrgaoJulgador() == null) {
			if (ParametroUtil.instance().isPrimeiroGrau()) {
				instance.setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());
				instance.setTipoSala(SalaEnum.A);
			} else {
				instance.setOrgaoJulgadorColegiado(Authenticator.getOrgaoJulgadorColegiadoAtual());
				instance.setTipoSala(SalaEnum.S);
			}
		}

		if (ParametroUtil.instance().isAssociaCompetenciaSalaAudiencia()) {
			getInstance().setCompetenciaList(new ArrayList<>());
			getInstance().setCompetenciaList(getCompetenciaAtivas());
		}

		getInstance().setAtivo(true);
		return super.persist();
	}

	public void visualizarOrgao() {
		boolean primeiroGrau = ParametroUtil.instance().isPrimeiroGrau()
				&& Authenticator.getOrgaoJulgadorAtual() != null;
		boolean colegiado = !primeiroGrau 
				&& Authenticator.getOrgaoJulgadorColegiadoAtual() != null;
		if (Authenticator.isPapelAdministrador()) {
			getInstance().setTipoSala(ParametroUtil.instance().isPrimeiroGrau() ? SalaEnum.A : SalaEnum.S);
		} else if (!(primeiroGrau || colegiado)) {
			ControleFiltros.lancarErro("Usuário sem configuração para realizar esta operação");
		}
	}

	@SuppressWarnings("unchecked")
	public List<Date> horaInicialList(Sala obj) {
		List<Date> hora = new ArrayList<Date>(0);
		StringBuilder sb = new StringBuilder();
		sb.append("select o from SalaHorario o where o.sala = :sala");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("sala", obj);
		List<SalaHorario> orgao = q.getResultList();
		for (int i = 0; i < orgao.size(); i++) {
			hora.add(orgao.get(i).getHoraInicial());
		}
		return hora;
	}

	@SuppressWarnings("unchecked")
	public List<Date> horaFinalList(Sala obj) {
		List<Date> hora = new ArrayList<Date>(0);
		StringBuilder sb = new StringBuilder();
		sb.append("select o from SalaHorario o where o.sala = :sala");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("sala", obj);
		List<SalaHorario> orgao = q.getResultList();
		for (int i = 0; i < orgao.size(); i++) {
			hora.add(orgao.get(i).getHoraFinal());
		}
		return hora;
	}

	@SuppressWarnings("unchecked")
	public List<DiaSemana> diaSemanaList(Sala obj) {
		List<DiaSemana> dia = new ArrayList<DiaSemana>(0);
		StringBuilder sb = new StringBuilder();
		sb.append("select o from SalaHorario o where o.sala = :sala");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("sala", obj);
		List<SalaHorario> orgao = q.getResultList();
		for (int i = 0; i < orgao.size(); i++) {
			dia.add(orgao.get(i).getDiaSemana());
		}
		return dia;
	}

	@SuppressWarnings("unchecked")
	public List<String> ativoList(Sala obj) {
		List<String> ativo = new ArrayList<String>(0);
		StringBuilder sb = new StringBuilder();
		sb.append("select o from SalaHorario o where o.sala = :sala");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("sala", obj);
		List<SalaHorario> orgao = q.getResultList();
		for (int i = 0; i < orgao.size(); i++) {
			if (orgao.get(i).getAtivo()) {
				ativo.add("Ativo");
			} else {
				ativo.add("Inativo");
			}
		}
		return ativo;
	}

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgadorColegiado> getOrgaoJulgadorColegiadoItems() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from OrgaoJulgadorColegiado o ");
		sb.append("where o.ativo = true");
		return getEntityManager().createQuery(sb.toString()).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> getOrgaoJulgadorItems() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from OrgaoJulgador o ");
		sb.append("where o.ativo = true ");
		if (Authenticator.getOrgaoJulgadorAtual() != null) {
			sb.append("and o.idOrgaoJulgador = " + Authenticator.getOrgaoJulgadorAtual().getIdOrgaoJulgador() + " ");
		}
		sb.append("order by o.orgaoJulgadorOrdemAlfabetica");
		return getEntityManager().createQuery(sb.toString()).getResultList();
	}

	public void inativar(Sala obj) {
		obj.setAtivo(false);
		SalaHorarioHome.instance().inativar(obj);
		getEntityManager().persist(obj);
		getEntityManager().flush();
		
		FacesMessages.instance().clear();
		FacesMessages.instance().add(StatusMessage.Severity.INFO, "Registro inativado com sucesso!");
	}
	
	public void setTipoOrgaoJulgador(Boolean tipoOrgaoJulgador) {
		this.tipoOrgaoJulgador = tipoOrgaoJulgador;
	}

	public Boolean getTipoOrgaoJulgador() {
		return tipoOrgaoJulgador;
	}

	public OrgaoJulgador getOrgaoJulgadorAtualSala() {
		if (Authenticator.getOrgaoJulgadorColegiadoAtual() == null) {
			return Authenticator.getOrgaoJulgadorAtual();
		}
		return null;
	}

	public Boolean getIsPrimeiraSalaAudienciaOrgao() {

		if (isPrimeiraSalaAudienciaOrgao != null)
			return isPrimeiraSalaAudienciaOrgao;

		Sala sala = getInstance();

		if (sala.getOrgaoJulgador() == null && sala.getOrgaoJulgadorColegiado() == null)
			return true;

		int numSalas = getEntityManager()
				.createQuery(
						"from Sala o where o.tipoSala = :tipoSala AND "
								+ "(o.orgaoJulgador = :orgaoJulgador or o.orgaoJulgadorColegiado = :orgaoJulgadorColegiado)")
				.setParameter("tipoSala", SalaEnum.A).setParameter("orgaoJulgador", sala.getOrgaoJulgador())
				.setParameter("orgaoJulgadorColegiado", sala.getOrgaoJulgadorColegiado()).getResultList().size();

		isPrimeiraSalaAudienciaOrgao = numSalas <= 1;
		return isPrimeiraSalaAudienciaOrgao;

	}

	public void associarTipoAudiencia() {

		if (getInstance().getTipoAudienciaList().contains(tipoAudiencia)) {
			FacesMessages.instance().add(Severity.ERROR, "Tipo de audiência já vinculado anteriormente");
			return;
		}

		getInstance().getTipoAudienciaList().add(tipoAudiencia);
		update();
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Registro inserido com sucesso!");
		tipoAudiencia = null;
	}

	public void removerTipoAudienciaAssociada(TipoAudiencia tipoAudiencia) {
		getInstance().getTipoAudienciaList().remove(tipoAudiencia);
		update();
	}

	@SuppressWarnings("unchecked")
	public List<TipoAudiencia> getTipoAudienciaItens() {
		Query query = getEntityManager().createQuery("from TipoAudiencia o where o.ativo = true");
		return query.getResultList();
	}

	@Override
	public void newInstance() {
		isPrimeiraSalaAudienciaOrgao = null;
		super.newInstance();
	}

	public void defineTipoSala() {
		if (ParametroUtil.instance().isPrimeiroGrau()) {
			getInstance().setTipoSala(SalaEnum.A);
		} else {
			getInstance().setTipoSala(SalaEnum.S);
		}
	}

	public List<Competencia> getCompetenciaItens() {

		String competenciaPorOrgaoJulgador = "select distinct o.competencia from OrgaoJulgadorCompetencia o ";
		StringBuilder filtroPorOrgaoJulgador = new StringBuilder();

		boolean isPrimeiroGrau = ParametroUtil.instance().isPrimeiroGrau();
		boolean naoPrimeiroGrau =  !ParametroUtil.instance().isPrimeiroGrau();
		OrgaoJulgador orgaoJulgador = getInstance().getOrgaoJulgador();

		if (isPrimeiroGrau) {

			// CEJUSC
			if (orgaoJulgador.getPostoAvancado()) {

				StringBuilder sqlCejusc =new StringBuilder("select distinct comp.*  ").
						append(" from  tb_posto_avancado tpa").
						append(" inner join tb_org_julg_competencia tojc").
						append(" on (tpa.id_oj_vara_atendida = tojc.id_orgao_julgador) ").
						append(" inner join tb_competencia comp  ").
						append(" on (tojc.id_competencia = comp.id_competencia) ").
						append("  where ").
						append( " tpa.id_oj_posto = :orgaoJulgadorAtual " ).
						append(" order by comp.ds_competencia asc ");

				Query queryCejusc =getEntityManager().createNativeQuery(sqlCejusc.toString(),Competencia.class) ;
				queryCejusc.setParameter("orgaoJulgadorAtual", orgaoJulgador.getIdOrgaoJulgador());

				return queryCejusc.getResultList();
			}
			else{
				filtroPorOrgaoJulgador.append(" where o.orgaoJulgador.idOrgaoJulgador = :orgaoJulgadorAtual and o.competencia.competencia not like '%4.0%'   ");
			}
		}
		else if(naoPrimeiroGrau) {
			filtroPorOrgaoJulgador.append(" where o.orgaoJulgadorColegiado.idOrgaoJulgador = :orgaoJulgadorColegiadoAtual ");
		}

		filtroPorOrgaoJulgador.append(" order by o.competencia.competencia asc");

		String competenciaQuery = competenciaPorOrgaoJulgador.concat(" ").concat(filtroPorOrgaoJulgador.toString());
		Query query = getEntityManager().createQuery(competenciaQuery) ;

		if(isPrimeiroGrau){
			query.setParameter("orgaoJulgadorAtual", orgaoJulgador.getIdOrgaoJulgador());
		}
		if(naoPrimeiroGrau){
			query.setParameter(" orgaoJulgadorColegiadoAtual",Authenticator.getOrgaoJulgadorColegiadoAtual());
		}

		return query.getResultList();
	}

	public List<Competencia> getCompetenciaAtivas() {

		String competenciaPorOrgaoJulgador = "select distinct o.competencia from OrgaoJulgadorCompetencia o ";
		StringBuilder filtroPorOrgaoJulgador = new StringBuilder();

		boolean isPrimeiroGrau = ParametroUtil.instance().isPrimeiroGrau();
		boolean naoPrimeiroGrau =  !ParametroUtil.instance().isPrimeiroGrau();
		OrgaoJulgador orgaoJulgador = getInstance().getOrgaoJulgador();

		if (isPrimeiroGrau) {

			// CEJUSC
			if (orgaoJulgador.getPostoAvancado()) {

				StringBuilder sqlCejusc =new StringBuilder("select distinct comp.*  ").
						append(" from  tb_posto_avancado tpa").
						append(" inner join tb_org_julg_competencia tojc").
						append(" on (tpa.id_oj_vara_atendida = tojc.id_orgao_julgador) ").
						append(" inner join tb_competencia comp  ").
						append(" on (tojc.id_competencia = comp.id_competencia) ").
						append("  where ").
						append( " tpa.id_oj_posto = :orgaoJulgadorAtual " ).
						append(" and tojc.dt_fim is null ").
						append(" order by comp.ds_competencia asc ");

				Query queryCejusc =getEntityManager().createNativeQuery(sqlCejusc.toString(),Competencia.class) ;
				queryCejusc.setParameter("orgaoJulgadorAtual", orgaoJulgador.getIdOrgaoJulgador());

				return queryCejusc.getResultList();
			}
			else{
				filtroPorOrgaoJulgador.append(" where o.orgaoJulgador.idOrgaoJulgador = :orgaoJulgadorAtual and o.competencia.competencia not like '%4.0%'   ");
				filtroPorOrgaoJulgador.append(" and o.dataFim is null ");
			}
		}
		else if(naoPrimeiroGrau) {
			filtroPorOrgaoJulgador.append(" where o.orgaoJulgadorColegiado.idOrgaoJulgador = :orgaoJulgadorColegiadoAtual and o.dataFim is null ");
		}

		filtroPorOrgaoJulgador.append(" order by o.competencia.competencia asc");

		String competenciaQuery = competenciaPorOrgaoJulgador.concat(" ").concat(filtroPorOrgaoJulgador.toString());
		Query query = getEntityManager().createQuery(competenciaQuery) ;

		if(isPrimeiroGrau){
			query.setParameter("orgaoJulgadorAtual", orgaoJulgador.getIdOrgaoJulgador());
		}
		if(naoPrimeiroGrau){
			query.setParameter(" orgaoJulgadorColegiadoAtual",Authenticator.getOrgaoJulgadorColegiadoAtual());
		}

		return query.getResultList();
	}



	public List<Competencia> getCompetenciasDisponiveis() {

		List<Competencia> resultList = getCompetenciaItens();

		List<Competencia> competenciasSala = getInstance().getCompetenciaList();

		if (competenciasSala != null && !competenciasSala.isEmpty()) {

			if (competenciasSala.size() == resultList.size()) {
				setTodasCompetencias(Boolean.TRUE);
			} else {
				setTodasCompetencias(Boolean.FALSE);
			}
		} else {
			setTodasCompetencias(Boolean.FALSE);
		}

		return resultList;
	}

	public void associarCompetencia() {
		update();
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Registro inserido com sucesso!");
	}

	public void selecionarTodasCompetencias(){
		if (todasCompetencias){
			getInstance().getCompetenciaList().addAll(getCompetenciaItens());
		}else{
			getInstance().getCompetenciaList().removeAll(getCompetenciaItens());
		}
	}

	public void alterarTodasCompetencias(){
		this.setTodasCompetencias(false);
	}

	public boolean isTodasCompetencias() {
		return todasCompetencias;
	}

	public void setTodasCompetencias(boolean todasCompetencias) {
		this.todasCompetencias = todasCompetencias;
	}

	@Override
	protected String afterPersistOrUpdate(String ret) {
		return "true";
	}

	public TipoAudiencia getTipoAudiencia() {
		return tipoAudiencia;
	}

	public void setTipoAudiencia(TipoAudiencia tipoAudiencia) {
		this.tipoAudiencia = tipoAudiencia;
	}

	public void setIsPrimeiraSalaAudienciaOrgao(Boolean isPrimeiraSalaAudienciaOrgao) {
		this.isPrimeiraSalaAudienciaOrgao = isPrimeiraSalaAudienciaOrgao;
	}

}
