package br.com.infox.pje.list;

import java.util.Map;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;

public abstract class AbstractProcessosConclusosList<T> extends EntityList<T> {

	private static final long serialVersionUID = 1L;

	private static final String R1 = "o.secaoJudiciaria = #{estatisticaProcessosConclusosAction.getSecaoJudiciaria().getCdSecaoJudiciaria()}";
	private static final String R2 = "o.orgaoJulgador = #{estatisticaProcessosConclusosAction.getOrgaoJulgador()}";
	private static final String R3 = "o.classeJudicial in (#{estatisticaProcessosConclusosAction.classeJudicialList})";
	private static final String R4 = "o.pessoaMagistrado = #{estatisticaProcessosConclusosAction.juiz}";
	private static final String R5 = "to_char(o.dtEvento,'yyyy-MM-dd') >= #{estatisticaProcessosConclusosAction.dataInicioStr} ";
	private static final String R6 = "to_char(o.dtEvento,'yyyy-MM-dd') <= #{estatisticaProcessosConclusosAction.dataFimStr} ";
	private static final String R7 = "o.codEvento = #{estatisticaProcessosConclusosAction.getMotivo().getCodEvento()}";
	private static final String R8 = "o.codEvento = #{estatisticaProcessosConclusosAction.getCodEvento()}";
	private static final String R9 = "o.pessoaMagistrado = #{estatisticaProcessosConclusosAction.pessoaMagistrado}";

	@Override
	protected void addSearchFields() {
		addSearchField("secaoJudiciaria", SearchCriteria.igual, R1);
		addSearchField("orgaoJulgador", SearchCriteria.igual, R2);
		addSearchField("classeJudicial", SearchCriteria.igual, R3);
		addSearchField("juiz", SearchCriteria.igual, R4);
		addSearchField("dataInicio", SearchCriteria.igual, R5);
		addSearchField("dataFim", SearchCriteria.igual, R6);
		addSearchField("motivo", SearchCriteria.igual, R7);
		addSearchField("evento", SearchCriteria.igual, R8);
		addSearchField("pessoaMagistrado", SearchCriteria.igual, R9);
	}

	protected String getRetornarEventoConclusaoCaminhoCompleto() {
		StringBuilder sb = new StringBuilder();
		sb.append("and o.secaoJudiciaria = #{estatisticaProcessosConclusosAction.getSecaoJudiciaria().getCdSecaoJudiciaria()} ");
		sb.append("and o.orgaoJulgador = #{estatisticaProcessosConclusosAction.getOrgaoJulgador()} ");
		sb.append("and to_char(o.dtEvento,'yyyy-MM-dd') >= #{estatisticaProcessosConclusosAction.dataInicioStr} ");
		sb.append("and to_char(o.dtEvento,'yyyy-MM-dd') <= #{estatisticaProcessosConclusosAction.dataFimStr} ");
		sb.append("and o.codEvento IN (select ep0.codEvento from Evento ep0 ");
		sb.append("                    where ep0.caminhoCompleto like concat(#{parametroUtil.eventoConclusao.caminhoCompleto}, '%')) ");
		sb.append("and o.dtEvento = (select max(epj0.dtEvento) from EstatisticaProcessoJusticaFederal epj0 ");
		sb.append("                  where epj0.codEvento IN (select ep1.codEvento from Evento ep1 ");
		sb.append("                                           where ep1.caminhoCompleto like concat(#{parametroUtil.eventoConclusao.caminhoCompleto}, '%')) ");
		sb.append("                                           and epj0.processoTrf = o.processoTrf) ");
		sb.append("and not exists (select epj1 from EstatisticaProcessoJusticaFederal epj1 ");
		sb.append("                where epj1.codEvento IN (select e.codEvento from Evento e ");
		sb.append("                                          where e.caminhoCompleto like concat(#{parametroUtil.eventoConclusao.caminhoCompleto}, '%')) ");
		sb.append("                and epj1.dtEvento > (select max(epj2.dtEvento) from EstatisticaProcessoJusticaFederal epj2 ");
		sb.append("                                     where epj2.codEvento IN (select ep2.codEvento from Evento ep2 ");
		sb.append("                                                              where ep2.caminhoCompleto like concat(#{parametroUtil.eventoConclusao.caminhoCompleto}, '%')) ");
		sb.append("                                                              and epj2.processoTrf = epj1.processoTrf) ");
		sb.append("                and to_char(o.dtEvento,'yyyy-MM-dd') <= #{estatisticaProcessosConclusosAction.dataFimStr} ");
		sb.append("                and epj1.processoTrf = o.processoTrf) ");
		return sb.toString();
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}
}