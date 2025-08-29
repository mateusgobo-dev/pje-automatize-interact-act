package br.com.infox.pje.list;

import java.util.Map;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;

public abstract class AbstractEstatisticaConclusaoProcessoList<T> extends EntityList<T> {

	private static final long serialVersionUID = 1L;

	private static final String R3 = "o.competencia = #{estatisticaConclusaoAction.competencia} ";
	private static final String R4 = "o.classeJudicial in (#{estatisticaConclusaoAction.classeJudicialList}) ";
	private static final String R5 = "o.cargo = #{estatisticaConclusaoAction.cargoJuiz} ";
	private static final String R6 = "exists(select ulms from UsuarioLocalizacaoMagistradoServidor ulms "
			+ "        where ulms.usuarioLocalizacao.usuario = #{estatisticaConclusaoAction.juiz} "
			+ "         and  ulms.orgaoJulgadorCargo.cargo = o.processoTrf.cargo "
			+ "         and  ulms.orgaoJulgador = o.processoTrf.orgaoJulgador "
			+ "         and  ulms.usuarioLocalizacao.usuario.idUsuario in (select pm.idUsuario from PessoaMagistrado pm) "
			+ "         and  ulms.dtInicio <= o.dtEvento "
			+ "         and (ulms.dtFinal is null or (ulms.dtFinal >= o.dtEvento))) ";

	@Override
	protected void addSearchFields() {
		addSearchField("competencia", SearchCriteria.igual, R3);
		addSearchField("classeJudicial", SearchCriteria.igual, R4);
		addSearchField("cargo", SearchCriteria.igual, R5);
		addSearchField("juiz", SearchCriteria.igual, R6);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}
}