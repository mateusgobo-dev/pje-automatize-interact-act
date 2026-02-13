package br.com.infox.pje.processor;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.bpm.TaskInstance;
import org.jboss.seam.log.Log;
import org.jbpm.taskmgmt.exe.SwimlaneInstance;
import org.quartz.SchedulerException;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.assignment.LocalizacaoAssignment;
import br.com.infox.ibpm.service.LogService;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.pje.nucleo.entidades.AgendamentoRemessa;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.Lock;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;

/**
 * 
 * @author rodrigo
 * 
 */
@Name(ProcessoIniciarFluxoProcessor.NAME)
@AutoCreate
public class ProcessoIniciarFluxoProcessor implements Serializable{
	@In
	private LogService logService;

	private static final long serialVersionUID = 1L;

	//PJE-JT: Ricardo Scholz : PJEII-1003 - 2012-05-14 Alteracoes feitas pela JT.
	private static final String REMESSA_INSERIR_NO_FLUXO_LOCK_ID = "remessa_inserir_no_fluxo";
	//PJE-JT: Fim.
	
	public final static String NAME = "processoIniciarFluxoProcessor";
	
	@Logger
	private Log log;

	private EntityManager entityManager;

	public ProcessoIniciarFluxoProcessor(){
	}

	public EntityManager getEntityManager(){
		if (entityManager == null){
			entityManager = EntityUtil.getEntityManager();
		}
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager){
		this.entityManager = entityManager;
	}

	public static ProcessoIniciarFluxoProcessor instance(){
		return (ProcessoIniciarFluxoProcessor) Component.getInstance(NAME);
	}

	/**
	 * Metodo utilizado para verificar qual expediente esta com a data ciencia parte extrapolada
	 * 
	 * @param inicio
	 * @param cron
	 * @return
	 * @throws SchedulerException
	 */
	@Asynchronous
	@Transactional
	public QuartzTriggerHandle inserirProcessosNoFluxo(@Expiration Date inicio, @IntervalCron String cron){

		// PJEII-4881  Tratamento de excecao para evitar que a aplicação nao inicie.
		try {
			inserirProcessos();
		} catch (Exception exception) {
			logService.enviarLogPorEmail(log, exception, this.getClass(), "inserirProcessosNoFluxo");
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private void inserirProcessos(){
		/*
		 * PJE-JT: Ricardo Scholz : PJEII-1003 - 2012-05-09 Alteracoes feitas pela JT.
		 * Utilização de lock para garantir o isolamento da transação quando houver 
		 * múltiplas threads executando o job de inserção de processos no fluxo.
		 * Para mais informações, consulte os comentários na issue PJEII-1003.
		 */
		EntityManager em = EntityUtil.getEntityManager();
		
		Lock lock = em.find(Lock.class, REMESSA_INSERIR_NO_FLUXO_LOCK_ID);
		em.lock(lock, LockModeType.READ);
		
		String hql = "select o from AgendamentoRemessa o where o.processado = false";
		Query query = em.createQuery(hql);
		List<AgendamentoRemessa> resultList = query.getResultList();
		
		if (resultList.size() > 0){
			processarAgendamentos(resultList);
		}
		em.flush();
		/*
		 * PJE-JT: Fim.
		 */
	}

	private void processarAgendamentos(List<AgendamentoRemessa> resultList){
		long inicio = new Date().getTime();
		log.info("Iniciando processamento dos agendamentos.");
		for (AgendamentoRemessa agendamentoRemessa : resultList){
			try{
				/*
				 * PJE-JT: Ricardo Scholz e Guilheme Bispo : PJEII-486 - 2012-02-29 Alteracoes feitas pela JT. 
				 * Inclusão da chamada a 'setInstance()' de ProcessoTrfHome.
				 * O método 'iniciarProcessoJbpm()' realiza chamada a 'ProcessoTrfHome.possuiOrgaoJulgadorColegiado()'.
				 * Este, utiliza a instância do home, que anteriormente estava nula, ocasionando um NullPointerException. 
				 */
				ProcessoTrfHome.instance().setInstance(agendamentoRemessa.getProcessoTrf());
				/*
				 * PJE-JT: Fim.
				 */
				iniciarProcessoJbpm(agendamentoRemessa.getProcessoTrf().getProcesso(), agendamentoRemessa.getFluxo(),
						null);
				agendamentoRemessa.setProcessado(true);
				getEntityManager().merge(agendamentoRemessa);
				/*
				 * PJE-JT: Dr. Paulo Cristovão e David Vieira: PJEII-542 - 2012-03-09 Alteracoes feitas pela JT. 
				 * Corrigir status do processo para não vir da remessa como distribuído.
				 */
				agendamentoRemessa.getProcessoTrf().setProcessoStatus(ProcessoStatusEnum.E);
				getEntityManager().merge(agendamentoRemessa.getProcessoTrf());
				
				/*
				 * Lançador do movimento de recebimento
				 */
				MovimentoAutomaticoService.preencherMovimento().deCodigo(CodigoMovimentoNacional.COD_MOVIMENTO_DISTRIBUICAO_RECEBIDO)
				.associarAoProcesso(agendamentoRemessa.getProcessoTrf().getProcesso())
				.lancarMovimento();
				
				apagarDocumentosNaoAssinados(agendamentoRemessa);
				
				//PJE-JT: Ricardo Scholz : PJEII-1003 - 2012-05-14 Alteracoes feitas pela JT.
				//Supressão da chamada a 'getEntityManager().flush()' visando evitar a liberação
				//antecipada do lock estabelecido no método 'inserirProcessos()'.
				//PJE-JT: Fim.
			} catch (Exception e){
				log.error("Erro ao processar o agendamento " + agendamentoRemessa + ": " + e.getMessage(), e);
				e.printStackTrace();
			}
		}
		long tempoDecorrido = new Date().getTime() - inicio;
		log.info(MessageFormat.format("Foram processado(s) {0} agrupamento(s). [{1} ms]", resultList.size(),
				tempoDecorrido));
	}

	@SuppressWarnings("unchecked")
	private void apagarDocumentosNaoAssinados(AgendamentoRemessa agendamentoRemessa){
		
		final int INDEX_PROCESSO_DOCUMENTO = 0;
		final int INDEX_PROCESSO_DOCUMENTO_BIN = 1;
		
		/*
		 * PJE-JT: Ricardo Scholz : PJEII-839 - 2012-04-23 Alteracoes feitas pela JT.
		 * Retirada da coluna 'pdb.ds_cert_chain' do select e da condicao 
		 * 'Strings.isEmpty((String) obj[0])', uma vez que o select ja realiza a 
		 * checagem de assinatura e retorna apenas os documentos nao assinados. 
		 * Retirada do 'break', para permitir que todos os documentos nao assinados
		 * sejam excluidos, nao apenas o primeiro da lista.
		 */
		StringBuffer sb = new StringBuffer();
		sb.append("select pd.id_processo_documento, pdb.id_processo_documento_bin from tb_processo_documento pd inner join ");
		sb.append("tb_processo_documento_bin pdb ");
		sb.append("on(pd.id_processo_documento_bin = pdb.id_processo_documento_bin) ");
		sb.append("where pd.id_processo = :idProcesso ");
		sb.append("and not exists (select * from tb_proc_doc_bin_pess_assin pdbpa where pdbpa.id_processo_documento_bin = pdb.id_processo_documento_bin)");

		Query q = getEntityManager().createNativeQuery(sb.toString());
		q.setParameter("idProcesso", agendamentoRemessa.getProcessoTrf().getIdProcessoTrf());
		List<Object[]> objectList = q.getResultList();

		/*
		 * PJE-JT: Ricardo Scholz : PJEII-1884 - 2012-08-08 Alteracoes feitas pela JT.
		 * PJE-JT: Ricardo Scholz : PJEII-2641 - 2012-09-04 Alteracoes feitas pela JT.
		 *
		 * Inclusão de código para remover da tabela 'tb_processo_documento_expediente' as referências
		 * à tabela 'tb_processo_documento' que contivessem documentos não assinados e deletar todas as 
		 * instâncias da tabela 'tb_processo_documento_trf' com os identificadores dos documentos não 
		 * assinados. Isso se faz necessário porque não é possível excluir um documento não assinado, 
		 * mais adiante, se ele estiver sendo referenciado por outra tabela.
		 * 
		 * Abordagens alternativas:
		 * 
		 * 1) [Abordagem proposta na PJEII-2707] A abordagem mais indicada seria reestruturar a rotina de 
		 * distribuição/redistribuição para que funcione mesmo com documentos não assinados, talvez 
		 * removendo apenas as minutas que vieram do primeiro grau (documentos que exigem assinatura, 
		 * mas não foram assinados).
		 * 
		 * 2) [Não aconselhável] Alternativamente, poderia-se utilizar, na constraint de chave estrangeira, 
		 * a opção ON DELETE SET NULL, mas esta poderia acarretar algum efeito colateral em outras partes 
		 * do código que estejam tentando deletar documentos.
		 * 	 
		 * 3) [Não aconselhável] Um contorno mais abrangente, porém mais arriscado, para a abordagem atual 
		 * seria realizar a atualização para todas as tabelas que referenciam a 'tb_processo_documento'. 
		 * Para obter a lista de tabelas que têm a chave primária da tabela 'tb_processo_documento' como 
		 * chave estrangeira, pode-se utilizar o select abaixo:
		 * 
		 * select a.table_schema, a.table_name, b.column_name, a.constraint_name
		 * from information_schema.table_constraints a 
		 * inner join information_schema.constraint_column_usage b
		 * on b.constraint_name = a.constraint_name where
		 * a.constraint_type = 'FOREIGN KEY' and
		 * b.column_name = 'id_processo_documento'
		 * order by a.table_schema, a.table_name, a.constraint_name
		 */
		
		if(objectList.size() > 0){
			
			//Objetivando reúso, gera uma lista de id_processo_documento, separados por vírgula e 
			//entre parênteses "(id1, id2, ..., idn)"
			StringBuffer docsIds = new StringBuffer("(");
			StringBuffer binsIds = new StringBuffer("(");
			docsIds.append((Integer) (objectList.get(0))[INDEX_PROCESSO_DOCUMENTO]);
			binsIds.append((Integer) (objectList.get(0))[INDEX_PROCESSO_DOCUMENTO_BIN]);
			for(int i = 1; i < objectList.size(); i++){
				docsIds.append(",");
				binsIds.append(",");
				docsIds.append((Integer) (objectList.get(i))[INDEX_PROCESSO_DOCUMENTO]);
				binsIds.append((Integer) (objectList.get(i))[INDEX_PROCESSO_DOCUMENTO_BIN]);
			}
			docsIds.append(")");
			binsIds.append(")");
			
			//Muda para NULL todas as referências aos documentos excluídos na tabela 'tb_processo_documento_expediente'
			sb = new StringBuffer();
			sb.append("update tb_proc_doc_expediente set id_processo_documento = NULL where id_processo_documento in ");
			sb.append(docsIds.toString());
			q = EntityUtil.createNativeQuery(getEntityManager(), sb, "tb_proc_doc_expediente");
			q.executeUpdate();
			
			//Deleta todas as referências aos documentos excluídos na tabela 'tb_processo_documento_trf'
			sb = new StringBuffer();
			sb.append("delete from tb_processo_documento_trf where id_processo_documento_trf in ");
			sb.append(docsIds.toString());
			q = EntityUtil.createNativeQuery(getEntityManager(), sb, "tb_processo_documento_trf");
			q.executeUpdate();
			
			sb = new StringBuffer();
			sb.append("update tb_processo_trf_conexao set id_processo_documento = NULL where id_processo_documento in ");
			sb.append(docsIds.toString());
			q = EntityUtil.createNativeQuery(getEntityManager(), sb, "tb_processo_trf_conexao");
			q.executeUpdate();
			
			//Deleta todas as instâncias de 'tb_processo_documento' contendo documentos não assinados
			sb = new StringBuffer();
			sb.append("delete from tb_processo_documento where id_processo_documento in ");
			sb.append(docsIds.toString());
			q = EntityUtil.createNativeQuery(getEntityManager(), sb, "tb_processo_documento");
			q.executeUpdate();
			
			//Deleta todas as instâncias de 'tb_processo_documento_bin' contendo binários não assinados
			sb = new StringBuffer();
			sb.append("delete from tb_processo_documento_bin where id_processo_documento_bin in ");
			sb.append(binsIds.toString());
			q = EntityUtil.createNativeQuery(getEntityManager(), sb, "tb_processo_documento_bin");
			q.executeUpdate();
		}
		/*
		 * PJE-JT: Fim.
		 */
	}

	public void iniciarProcessoJbpm(Processo processo, Fluxo fluxo, Map<String, Object> variaveis){
		// Verificar se processo já foi iniciado
		if (processo.getIdJbpm() != null) {
			return;
		}
		
		BusinessProcess.instance().createProcess(fluxo.getFluxo().trim());
		processo.setFluxo(fluxo);
		processo.setIdJbpm(BusinessProcess.instance().getProcessId());
		getEntityManager().merge(processo);
		
		if (variaveis == null){
			variaveis = new java.util.HashMap<String, Object>();
		}
		variaveis.put("processo", processo.getIdProcesso());
		ProcessoHome.instance().setInstance(processo);
		ProcessoHome.instance().iniciarProcessoJbpm(fluxo, variaveis);
		ProcessoHome.instance().update();
		
		SwimlaneInstance swimlaneInstance = TaskInstance.instance().getSwimlaneInstance();
		String actorsExpression = swimlaneInstance.getSwimlane().getPooledActorsExpression();
		Set<String> pooledActors = LocalizacaoAssignment.instance().getPooledActors(actorsExpression);
		String[] actorIds = pooledActors.toArray(new String[pooledActors.size()]);
		swimlaneInstance.setPooledActors(actorIds);
		log.info(MessageFormat.format("O processo {0} foi inserido no fluxo {1}", processo, fluxo));

	}
}
