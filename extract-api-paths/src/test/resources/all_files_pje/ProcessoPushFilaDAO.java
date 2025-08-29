package br.jus.cnj.pje.business.dao;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.component.Util;
import br.jus.cnj.pje.servicos.DateService;
import br.jus.pje.nucleo.dto.ProcessoPushFilaDTO;
import br.jus.pje.nucleo.entidades.ProcessoPushFila;

@Name(ProcessoPushFilaDAO.NAME)
public class ProcessoPushFilaDAO extends BaseDAO<ProcessoPushFila> {
	
	public static final String NAME = "pushFilaDAO";	
	
	private static final Integer TAMANHO_LOTE_PROCESSOS = 300;

	private static final String SELECT = "select ";

	private static final String FROM = "from ";

	private static final String UPDATE_PROCESSO_PUSH_FILA = "update client.tb_processo_push_fila ";

	@Logger
	private Log log;
	
	private String querySegredoJustica(boolean valor) {
		StringBuilder sb = new StringBuilder();
		
		sb.append(SELECT);
		sb.append("pe.id_processo, ");
		sb.append("concat(to_char(pe.dt_atualizacao, 'DD/MM/YYYY HH24:MI'), ' - ', pe.ds_texto_final_externo) evento, ");
		sb.append("coalesce(pp.id_pessoa, ");
		sb.append("pp.id_pessoa_push) id_pessoa ");
		sb.append(FROM);
		sb.append("core.tb_processo_evento pe ");
		sb.append("inner join core.tb_evento e on ");
		sb.append("(e.id_evento = pe.id_evento) ");
		sb.append("inner join client.tb_processo_push pp on ");
		sb.append("(pp.id_processo_trf = pe.id_processo) ");
		sb.append("inner join client.tb_processo_trf proc on "); 
		sb.append("(proc.id_processo_trf = pp.id_processo_trf) "); 
		sb.append("where ");
		sb.append("pp.dt_exclusao is null ");
		
		if(valor) {
			sb.append("and proc.in_segredo_justica = true ");
			sb.append("and exists(select 1 from client.tb_proc_visibilida_segredo pvs where pvs.id_processo_trf = proc.id_processo_trf and pvs.id_pessoa = coalesce(pp.id_pessoa,pp.id_pessoa_push)) ");
		}else {
			sb.append("and proc.in_segredo_justica = false ");
		}
		
		sb.append("and pe.dt_atualizacao > :ultimaExecucao ");
		sb.append("and pe.dt_atualizacao <= :horarioExecucao ");
		sb.append("and e.in_segredo_justica = false ");
		sb.append("and e.in_visibilidade_externa = true ");
		sb.append("and pe.in_ativo = true ");
		sb.append("and pe.in_visibilidade_externa = true ");
		sb.append("and pe.id_processo_evento_excludente is null ");
		
		return sb.toString();
	}
	
	@Override
	public Integer getId(ProcessoPushFila p) {
		return p.getIdProcessoPushFila();
	}
	
	public Boolean existeItemEmProcessamento() {
			
		Boolean existeRegistro = false;
			
		String query = "select true where exists (select 1 from client.tb_processo_push_fila where in_em_processamento = true)";
			
		try {
			existeRegistro = (Boolean) getEntityManager().createNativeQuery(query).getSingleResult();
		}
		catch(NoResultException e) {
			existeRegistro = false;
		}
			
		return existeRegistro;
	}
	
	public Boolean existeItemParaProcessar() {
		
		Boolean existeRegistro = false;
		
		String query = "select true where exists (select 1 from client.tb_processo_push_fila where in_em_processamento = false)";
		
		try {
			existeRegistro = (Boolean) getEntityManager().createNativeQuery(query).getSingleResult();
		}
		catch(NoResultException e) {
			existeRegistro = false;
		}
		
		return existeRegistro;
	}	

	// Insere registros na fila para novas movimentações ou atualiza o registro 
	// caso já esteja na fila e não esteja em processamento por outra instância do job
	public void gerarFila() {
		
		Date horarioExecucao = DateService.instance().getDataHoraAtual();

		Util.beginAndJoinTransaction();

		// Busca o horário de última execução do push com 
		// lock no registro para atualizacao ao final do método.
		Date ultimaExecucao = buscarHorarioUltimaExecucaoPush();

		if (ultimaExecucao == null) {
			log.error("[Push] Não foi possível gerar nova fila de processos. O parâmetro \"pje:jobs:push:ultimaExecucao\" não foi encontrado ou está inativo.");
			Util.rollbackAndOpenJoinTransaction();
			return;
		}
		
		StringBuilder sb = new StringBuilder("with eventos_novos as (");
		sb.append(querySegredoJustica(false));
		sb.append("UNION ALL ");
		sb.append(querySegredoJustica(true));
		sb.append("), ");
		sb.append("lista_push as (");
		sb.append(SELECT);
		sb.append("e.id_processo, ");
		sb.append("array_to_string(array_agg(distinct coalesce(l.ds_email,ppu.ds_email)),'|') emails, ");
		sb.append("array_to_string(array_agg(distinct e.evento),'|') movimentos ");
		sb.append(FROM);
		sb.append("eventos_novos e ");
		sb.append("left join acl.tb_usuario_login l on ");
		sb.append("(e.id_pessoa = l.id_usuario) ");
		sb.append("left join client.tb_pessoa_push ppu on ");
		sb.append("(e.id_pessoa = ppu.id_pessoa_push) ");
		sb.append("group by ");
		sb.append("e.id_processo) ");
		sb.append("insert into ");
		sb.append("client.tb_processo_push_fila as fila (id_processo, ");
		sb.append("ds_lista_emails, ");
		sb.append("ds_movimentacao, ");
		sb.append("dt_atualizacao) ");
		sb.append("select id_processo, emails, movimentos, :horarioExecucao ");
		sb.append("from lista_push where trim(emails) != '' ");
		sb.append("on conflict on constraint tb_processo_push_fila_proc_un ");
		sb.append("do update set ");
		sb.append("ds_lista_emails = EXCLUDED.ds_lista_emails, ");
		sb.append("ds_movimentacao = EXCLUDED.ds_movimentacao, ");
		sb.append("dt_atualizacao = EXCLUDED.dt_atualizacao ");
		
		Query query = getEntityManager().createNativeQuery(sb.toString());
		query.setParameter("ultimaExecucao", ultimaExecucao);
		query.setParameter("horarioExecucao", horarioExecucao);
		query.executeUpdate();
		atualizarHorarioUltimaExecucaoPush(horarioExecucao);
		Util.commitAndOpenJoinTransaction();
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoPushFilaDTO> consumirFila() {
		
		List<ProcessoPushFilaDTO> listaPushFilaDTO = new ArrayList<>();
		
		String sb = "select id_processo_push_fila as idProcessoPushFila, id_processo as idProcesso, " +
				 "ds_lista_emails listaEmail, ds_movimentacao listaMovimentacao, " +
				 "nr_processo nrProcesso, dt_autuacao dtAutuacao, in_segredo_justica inSegredoJustica, " + 
				 "dt_distribuicao dtDistribuicao, ds_orgao_julgador orgaoJulgador, " +
			     "ds_orgao_julgador_colegiado orgaoJulgadorColegiado, " + 
				 "ds_classe_judicial classeJudicial, ds_assunto_principal assuntoPrincipal, " +
				 "nm_pessoa_autor nomeAutor, qt_autor qtAutor, nm_pessoa_reu nomeReu, qt_reu qtReu, " +
				 "(select dt_nascimento from client.tb_pessoa_fisica p where p.id_pessoa_fisica = id_pessoa_autor) data_nascimento_autor, " +
				 "(select dt_nascimento from client.tb_pessoa_fisica p where p.id_pessoa_fisica = id_pessoa_reu) data_nascimento_reu " +
				 "from client.tb_processo_push_fila as fila " +
				 "inner join client.tb_cabecalho_processo as cab " +
				 "on (cab.id_processo_trf = fila.id_processo) " +
				 "where fila.in_em_processamento = false " +
				 "for update of fila skip locked";
		
		Util.beginAndJoinTransaction();
		
		Query query = getEntityManager().createNativeQuery(sb);

		Integer tamanhoLoteProcessos = ParametroUtil.instance().getTamanhoLoteProcessosPush();

		if (tamanhoLoteProcessos == null || tamanhoLoteProcessos <= 0) {
			tamanhoLoteProcessos = TAMANHO_LOTE_PROCESSOS;
		}
			
		query.setMaxResults(tamanhoLoteProcessos);
			
		List<Object[]> dadosProcessos = query.getResultList();
		
		if(dadosProcessos != null && !dadosProcessos.isEmpty()) {
			listaPushFilaDTO = carregarProcessoPushFilaDTO(dadosProcessos);
			atualizarItensEmProcessamento(gerarListaIdPushFila(listaPushFilaDTO));
		}
		
		Util.commitAndOpenJoinTransaction();
		
		return listaPushFilaDTO;
	}

	private void atualizarItensEmProcessamento(List<Integer> pushFilaEmProcessamento) {
		
		if (!pushFilaEmProcessamento.isEmpty()) {

			String sb = UPDATE_PROCESSO_PUSH_FILA +
					"set in_em_processamento = true " +
					"where id_processo_push_fila in (?1)";

			Query query = getEntityManager().createNativeQuery(sb);
			
			query.setParameter(1, pushFilaEmProcessamento);

			query.executeUpdate();	

			Util.commitAndOpenJoinTransaction();
		}
	}

	public void removerItensProcessados(List<ProcessoPushFilaDTO> pushFilaProcessadosParaRemocao) {
		
		if (!pushFilaProcessadosParaRemocao.isEmpty()) {

			List<Integer> ids = gerarListaIdPushFila(pushFilaProcessadosParaRemocao);

			String sb = "delete from client.tb_processo_push_fila f" +
							" using (" +
							"select id_processo_push_fila " +
							"from client.tb_processo_push_fila " +
							"where id_processo_push_fila in (?1)" +
							"order by id_processo_push_fila " +
							"for update) remover " +
						    "where f.id_processo_push_fila = remover.id_processo_push_fila";

			Query query = getEntityManager().createNativeQuery(sb);
			
			query.setParameter(1, ids);
			
			query.executeUpdate();
			
			Util.commitAndOpenJoinTransaction();
		}
	}

	public void retornarTodosItensParaFila() {
		
		String sb = UPDATE_PROCESSO_PUSH_FILA +
				    "set in_em_processamento = false " +
				    "where in_em_processamento = true";
		
		Query query = getEntityManager().createNativeQuery(sb);
		
		query.executeUpdate();

		Util.commitAndOpenJoinTransaction();
	}

	public void retornarItensParaFila(List<ProcessoPushFilaDTO> retornarItensParaFila) {

		if (!retornarItensParaFila.isEmpty()) {

			List<Integer> ids = gerarListaIdPushFila(retornarItensParaFila);

			String sb = UPDATE_PROCESSO_PUSH_FILA +
						"set in_em_processamento = false " +
						"where in_em_processamento = true " +
						"and id_processo_push_fila in (?1)";
			
			Query query = getEntityManager().createNativeQuery(sb);
			
			query.setParameter(1, ids);
			
			query.executeUpdate();

			Util.commitAndOpenJoinTransaction();
		}
	}

	public void retornarItensParaFilaAtualizandoEmails(List<ProcessoPushFilaDTO> retornarItensParaFilaAtuandoEmail) {
	
		if (!retornarItensParaFilaAtuandoEmail.isEmpty()) {

			Util.beginAndJoinTransaction();

			for(ProcessoPushFilaDTO pushFila : retornarItensParaFilaAtuandoEmail) {

				String sb = UPDATE_PROCESSO_PUSH_FILA +
							"set in_em_processamento = false, " +
							"ds_lista_emails = :listaEmails " +
							"where in_em_processamento = true " +
							"and id_processo_push_fila = :id";
				
				Query query = getEntityManager().createNativeQuery(sb);

				query.setParameter("id", pushFila.getIdProcessoPushFila());
				query.setParameter("listaEmails", pushFila.getListaEmail());
				
				query.executeUpdate();
			}
			
			Util.commitAndOpenJoinTransaction();
		}		
	}
	
	public List<Integer> gerarListaIdPushFila(List<ProcessoPushFilaDTO> listaDTO) {
				
		List<Integer> listaIds = new ArrayList<>();
		
		if(listaDTO != null) {
			for (ProcessoPushFilaDTO item : listaDTO) {
				listaIds.add(item.getIdProcessoPushFila());
			}
		}
	
		return listaIds;
	}
	
	private List<ProcessoPushFilaDTO> carregarProcessoPushFilaDTO(List<Object[]> dadosProcessos) {
		
		List<ProcessoPushFilaDTO> pushFilaDTOList = new ArrayList<>();
		
		for(Object[] processo : dadosProcessos) {
			
			ProcessoPushFilaDTO pushFilaDTO = new ProcessoPushFilaDTO();
			
			pushFilaDTO.setIdProcessoPushFila((Integer) processo[0]);
			pushFilaDTO.setIdProcesso((Integer) processo[1]);
			pushFilaDTO.setListaEmail((String) processo[2]);
			pushFilaDTO.setListaMovimentacao((String) processo[3]);
			pushFilaDTO.setNrProcesso((String) processo[4]);
			pushFilaDTO.setDtAutuacao((Date) processo[5]);
			pushFilaDTO.setSegredoJustica((Boolean) processo[6]);
			pushFilaDTO.setDtDistribuicao((Date) processo[7]);
			pushFilaDTO.setOrgaoJulgador((String) processo[8]);
			pushFilaDTO.setOrgaoJulgadorColegiado((String) processo[9]);
			pushFilaDTO.setClasseJudicial((String) processo[10]);
			pushFilaDTO.setAssuntoPrincipal((String) processo[11]);
			pushFilaDTO.setNomeAutor((String) processo[12]);
			pushFilaDTO.setQuantidadeAutores((BigInteger) processo[13]);
			pushFilaDTO.setNomeReu((String) processo[14]);
			pushFilaDTO.setQuantidadeReus((BigInteger) processo[15]);
			pushFilaDTO.setDtNascimentoAutor((Date) processo[16]);
			pushFilaDTO.setDtNascimentoReu((Date) processo[17]);

			pushFilaDTOList.add(pushFilaDTO);
		}
		
		return pushFilaDTOList;
	}

	private Date buscarHorarioUltimaExecucaoPush() {

		Date ultimaExecucao = null;

		String sbParamatro = "select vl_variavel from core.tb_parametro param" +
							 " where nm_variavel = 'pje:jobs:push:ultimaExecucao'" +
							 " and in_ativo = true" +
							 " for update of param";

		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		
		try {
			String strUltimaExecucao = (String) getEntityManager().createNativeQuery(sbParamatro).getSingleResult();
			ultimaExecucao = formatter.parse(strUltimaExecucao);
		} 
		catch (Exception e) {
			ultimaExecucao = null;
		}

		return ultimaExecucao;
	}

	private void atualizarHorarioUltimaExecucaoPush(Date horarioAtual) {

		String sbAtualizaParametro = "update core.tb_parametro" +
		" set vl_variavel = :horarioAtual" +
		" where nm_variavel = 'pje:jobs:push:ultimaExecucao'" +
		" and in_ativo = true";

		Query query = getEntityManager().createNativeQuery(sbAtualizaParametro);

		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		String strHorarioAtual = formatter.format(horarioAtual);

		query.setParameter("horarioAtual", strHorarioAtual);

		query.executeUpdate();
	}
}
