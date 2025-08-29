package br.jus.pje.nucleo.entidades.filters;

public interface ProcessoParteExpedienteFilter extends Filter {
	
	public static final String FILTER_ORGAO_JULGADOR_CARGO = "orgaoJulgadorCargoProcessoParteExpediente";

	public static final String CONDITION_ORGAO_JULGADOR_CARGO = "exists ( select 1 from tb_usu_local_visibilidade ulv"
			+ " inner join tb_processo_trf ptrf on ptrf.id_processo_trf = id_processo_trf"
			+ " where ulv.id_usu_local_mgstrado_servidor = :idUsuarioLocalizacao"
			+ " and ulv.dt_inicio <= :dataAtual"
			+ " and (ulv.dt_final is null or (ulv.dt_final >= :dataAtual))"
			+ " and ulv.id_org_julg_cargo_visibilidade = ptrf.id_orgao_julgador_cargo)";
}