package br.com.infox.trf.distribuicao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.pje.nucleo.entidades.Cargo;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

public class DistribuicaoHome implements Serializable {

	private static final long serialVersionUID = 1L;

	private static EntityManager getEntityManager() {
		return EntityUtil.getEntityManager();
	}

	public static VaraTitulacao executarSorteio(ProcessoTrf processoTrf, Competencia competencia) throws Exception {
		if (!ParametroUtil.instance().isPrimeiroGrau() && competencia == null) {
			if (ProcessoTrfHome.instance().getCompetencias(processoTrf).size() > 1) {
				ProcessoTrfHome.instance().inserirProcessoNoFluxo(processoTrf.getIdProcessoTrf(),
						ParametroUtil.instance().getFluxoDistribuicao());
				FacesMessages.instance().add(StatusMessage.Severity.INFO,
						"O processo esta em mais de uma competência e foi para o fluxo de distribuição.");
				return null;
			}
		}
		List<VaraTitulacao> listaParaSorteio = null;
		if (processoTrf.getProcessoOriginario() != null) {
			listaParaSorteio = new ArrayList<VaraTitulacao>();
			if (processoTrf.getProcessoOriginario().getOrgaoJulgadorColegiado() != null) {
				listaParaSorteio.add(getVaraTitulacao(processoTrf.getOrgaoJulgador(), processoTrf.getCargo(),
						processoTrf.getClasseJudicial(), processoTrf.getProcessoOriginario()
								.getOrgaoJulgadorColegiado()));
			} else {
				listaParaSorteio.add(getVaraTitulacao(processoTrf.getOrgaoJulgador(), processoTrf.getCargo(),
						processoTrf.getClasseJudicial(), null));
			}
		} else {
			listaParaSorteio = obterListaParaSorteio(processoTrf, competencia);
		}
		if (listaParaSorteio.size() == 0) {
			throw new Exception(
					"Escolha outra Seção/Subseção. Nesta existe somente um Órgão Julgador ativo, ao qual o Processo já está vinculado.");
		}
		SorteioVaraTitulacao sorteioVaraTitulacao = new SorteioVaraTitulacao();
		sorteioVaraTitulacao.setListVaraCargo(listaParaSorteio);
		sorteioVaraTitulacao.calcPesos();

		Sorteio<VaraTitulacao> sorteio = new Sorteio<VaraTitulacao>(
				sorteioVaraTitulacao.getListVaraCargoAsElementoList());
		Elemento<VaraTitulacao> elementoSorteado = sorteio.sortearElemento();
		return elementoSorteado.getObjeto();
	}

	public static List<VaraTitulacao> obterListaParaSorteio(ProcessoTrf processoTrf, Competencia competencia) throws Exception {
		List<VaraTitulacao> list = new ArrayList<VaraTitulacao>();
		ClasseJudicial classe = processoTrf.getClasseJudicial();
		if (!ParametroUtil.instance().isPrimeiroGrau()) {
			for (OrgaoJulgadorColegiado orgaoJulgadorColegiado : getOrgaoJulgadorColegiadoList(processoTrf, competencia)) {
				for (OrgaoJulgador orgaoJulgador : getOrgaoJulgadorListColegiado(processoTrf, orgaoJulgadorColegiado,
						competencia)) {
					for (Cargo cargo : getCargoList(orgaoJulgador)) {
						if (!cargo.equals(processoTrf.getCargo())
								|| !orgaoJulgador.equals(processoTrf.getOrgaoJulgador())) {
							list.add(getVaraTitulacao(orgaoJulgador, cargo, classe, orgaoJulgadorColegiado));
						}
					}
				}
			}
		} else {
			for (OrgaoJulgador orgaoJulgador : getOrgaoJulgadorList(processoTrf)) {
				for (Cargo cargo : getCargoList(orgaoJulgador)) {
					if (!cargo.equals(processoTrf.getCargo()) || !orgaoJulgador.equals(processoTrf.getOrgaoJulgador())) {
						list.add(getVaraTitulacao(orgaoJulgador, cargo, classe, null));
					}
				}
			}
		}
		return list;
	}

	public static int getQuantidadeProcesso(OrgaoJulgador orgaoJulgador, Cargo cargo, ClasseJudicial classe) {
		String hql = "select count(o) from ProcessoTrf o " + "where o.orgaoJulgador = :orgaoJulgador ";
		if (cargo != null) {
			hql += " and o.cargo = :cargo ";
		}
		if (classe != null) {
			hql += " and o.classeJudicial = :classe";
		}
		Query query = getEntityManager().createQuery(hql);
		query.setParameter("orgaoJulgador", orgaoJulgador);
		if (classe != null) {
			query.setParameter("classe", classe);
		}
		if (cargo != null) {
			query.setParameter("cargo", cargo);
		}
		long resultado = (Long) query.getSingleResult();
		return (int) resultado;
	}

	private static VaraTitulacao getVaraTitulacao(OrgaoJulgador orgaoJulgador, Cargo cargo, ClasseJudicial classe,
			OrgaoJulgadorColegiado orgaoJulgadorColegiado) throws Exception {
		
		if(cargo == null) {
			List<Cargo> cargos = getCargoList(orgaoJulgador);
			cargo = cargos.get(0);
		}

		VaraTitulacao vt = new VaraTitulacao(orgaoJulgador, cargo, getQuantidadeProcesso(orgaoJulgador, cargo, classe),
				getQuantidadeProcesso(orgaoJulgador, cargo, null), orgaoJulgadorColegiado);
		return vt;
	}

	@SuppressWarnings("unchecked")
	private static List<OrgaoJulgador> getOrgaoJulgadorList(ProcessoTrf processoTrf) {
		Competencia competencia = ProcessoTrfHome.instance().getCompetencia(processoTrf);
		if (competencia != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("select o from OrgaoJulgador o ");
			sb.append("inner join o.orgaoJulgadorCompetenciaList orgComp ");
			sb.append("where orgComp.competencia = :competencia ");
			sb.append("and o.ativo = true ");
			sb.append("and orgComp.dataInicio <= current_date ");
			sb.append("and (orgComp.dataFim >= current_date or orgComp.dataFim is null) ");
			sb.append("and o.jurisdicao = :jurisdicao");
			Query query = getEntityManager().createQuery(sb.toString());
			query.setParameter("competencia", competencia);
			query.setParameter("jurisdicao", processoTrf.getJurisdicao());
			return query.getResultList();
		} else {
			throw new IllegalArgumentException("Não foi encontrado assunto vinculado a este Processo");
		}
	}

	@SuppressWarnings("unchecked")
	private static List<OrgaoJulgador> getOrgaoJulgadorListColegiado(ProcessoTrf processoTrf,
			OrgaoJulgadorColegiado orgaoJulgadorColegiado, Competencia competencia) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ojc.orgaoJulgador from OrgaoJulgadorColegiadoOrgaoJulgador ojc ");
		sb.append("where ojc.orgaoJulgador in (");
		sb.append("select o from OrgaoJulgador o ");
		sb.append("inner join o.orgaoJulgadorCompetenciaList orgComp ");
		sb.append("where orgComp.competencia = :competencia ");
		sb.append("and o.ativo = true ");
		sb.append("and orgComp.dataInicio <= current_date ");
		sb.append("and (orgComp.dataFim >= current_date or orgComp.dataFim is null)) ");
		sb.append("and ojc.orgaoJulgadorColegiado = :orgaoJulgadorColegiado ");
		sb.append("and ojc.dataInicial <= current_date ");
		sb.append("and (ojc.dataFinal is null or ojc.dataFinal >= current_date)");
		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("competencia", competencia == null ? ProcessoTrfHome.instance().getCompetencia(processoTrf)
				: competencia);
		query.setParameter("orgaoJulgadorColegiado", orgaoJulgadorColegiado);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	private static List<OrgaoJulgadorColegiado> getOrgaoJulgadorColegiadoList(ProcessoTrf processoTrf,
			Competencia competencia) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from OrgaoJulgadorColegiado o ");
		sb.append("inner join o.orgaoJulgadorColegiadoCompetenciaList orgComp ");
		sb.append("where orgComp.competencia = :competencia ");
		sb.append("and o.ativo = true ");
		sb.append("and orgComp.dataInicio <= current_date ");
		sb.append("and (orgComp.dataFim >= current_date or orgComp.dataFim is null) ");
		sb.append("and o.jurisdicao = :jurisdicao");
		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("competencia", competencia == null ? ProcessoTrfHome.instance().getCompetencia(processoTrf)
				: competencia);
		query.setParameter("jurisdicao", processoTrf.getJurisdicao());
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	private static List<Cargo> getCargoList(OrgaoJulgador orgaoJulgador) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o.cargo from OrgaoJulgadorCargo o ");
		sb.append("where o.recebeDistribuicao = true and ");
		sb.append("o.cargo.ativo = true and ");
		sb.append("o.orgaoJulgador = :orgaoJulgador");
		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("orgaoJulgador", orgaoJulgador);
		return query.getResultList();
	}

}
