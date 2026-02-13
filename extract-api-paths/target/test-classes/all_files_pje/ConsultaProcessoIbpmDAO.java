package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.ConsultaProcessoFluxoAbertoTarefaFechadaIbpm;
import br.jus.pje.nucleo.entidades.ConsultaProcessoIbpm;


/**
 * Classe responsável por realizar o acesso ao banco de dados utilizando a entidade
 * ConsultaProcessoIbpm 
 * 
 */
@Name(ConsultaProcessoIbpmDAO.NAME)
public class ConsultaProcessoIbpmDAO extends BaseDAO<ConsultaProcessoIbpm>{

	public static final String NAME = "consultaProcessoIbpmDAO";
	
	@Override
	public Integer getId(ConsultaProcessoIbpm e){
		return e.getIdProcesso();
	}
	
	/**
	 * Método responsável por realizar a listagem na view de processos com erro 
	 * de acordo com os parâmetros informados
	 * 
	 * @param numeroSequencia
	 * @param digitoVerificador
	 * @param ano
	 * @param numeroOrigem
	 * @param segmentoJudiciario
	 * @param respectivoTribunal
	 * @param limiteConsulta
	 * @return uma <b>Lista</b> de ConsultaProcessoLimbo
	 */
	@SuppressWarnings("unchecked")
	public List<ConsultaProcessoFluxoAbertoTarefaFechadaIbpm> processosFluxoAbertoTarefaFechadaIbpm(Integer numeroSequencia, 
			Integer digitoVerificador, Integer ano,	Integer numeroOrigem, String segmentoJudiciario, 
			String respectivoTribunal, Integer limiteConsulta){
		
		String consulta = null;
		List<ConsultaProcessoFluxoAbertoTarefaFechadaIbpm> processos = null; 
		Query query = null; 
		StringBuilder hqlConsultaProcesso = new StringBuilder("SELECT a FROM ConsultaProcessoFluxoAbertoTarefaFechadaIbpm  a WHERE");
		
		if (isParametroValido(numeroSequencia)){
			hqlConsultaProcesso.append(" a.numeroSequencia = :numeroSequencia AND");
		}
		if (isParametroValido(digitoVerificador)){
			hqlConsultaProcesso.append(" a.numeroDigitoVerificador = :digitoVerificador AND");
		}
		if (isParametroValido(ano)){
			hqlConsultaProcesso.append(" a.ano = :ano AND");
		}
		if (isParametroValido(numeroOrigem)){
			hqlConsultaProcesso.append(" a.numeroOrigem = :numeroOrigem AND");
		}
		if(respectivoTribunal != null && !respectivoTribunal.isEmpty()){
				hqlConsultaProcesso.append(" a.numeroOrgaoJustica = :numeroOrgaoJustica AND"); 
		}
		consulta = hqlConsultaProcesso.toString();
		if(consulta.endsWith("WHERE")){
			consulta = StringUtils.chomp(consulta,"WHERE");
		}else{
			consulta = StringUtils.chomp(consulta,"AND");
		}
		
		query = getEntityManager().createQuery(consulta);
		
		if (isParametroValido(numeroSequencia)){
			query.setParameter("numeroSequencia", numeroSequencia);
		}
		if (isParametroValido(digitoVerificador)){
			query.setParameter("digitoVerificador", digitoVerificador);
		}
		if (isParametroValido(ano)){
			query.setParameter("ano", ano);
		}
		if (isParametroValido(numeroOrigem)){
			query.setParameter("numeroOrigem", numeroOrigem);
		}
		if(respectivoTribunal != null && !respectivoTribunal.isEmpty()){
			if(segmentoJudiciario != null){
				query.setParameter("numeroOrgaoJustica", Integer.parseInt(segmentoJudiciario+respectivoTribunal));
			}
			else{
				query.setParameter("numeroOrgaoJustica", Integer.parseInt(respectivoTribunal));
			}
		}
		
		query.setMaxResults(limiteConsulta);
		processos = query.getResultList();
		return processos;
	}

	/**
	 * Verifica se o parametro é valido. Para ser válido é necessário que o parametro seja diferente de nulo e maior que
	 * zero.
	 * @param	parametro
	 * @return	<b>true</b> se for válido, conforme indicado no comentário.
	 */
	private boolean isParametroValido(Integer parametro) {
		return parametro != null && parametro > 0;
	}
}