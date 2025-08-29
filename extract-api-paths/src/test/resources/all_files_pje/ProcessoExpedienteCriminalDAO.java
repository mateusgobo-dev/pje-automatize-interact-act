package br.jus.cnj.pje.business.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import br.jus.pje.nucleo.entidades.ProcessoExpedienteCriminal;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.SituacaoExpedienteCriminalEnum;
import br.jus.pje.nucleo.enums.TipoExpedienteCriminalEnum;

//@Name(ProcessoExpedienteCriminalDAO.NAME)
public abstract class ProcessoExpedienteCriminalDAO<E extends ProcessoExpedienteCriminal> extends BaseDAO<E>{

	//public static final String NAME = "processoExpedienteCriminalDAO";

	@Override
	public Integer getId(E e){
		return e.getId();
	}

	public Integer gerarNumeroExpediente(Integer idProcessoTrf){
		String hql = " select max(o.numero) from ProcessoExpedienteCriminal o " + " where o.processoTrf.idProcessoTrf = ? ";
		Query qry = getEntityManager().createQuery(hql);
		qry.setParameter(1, idProcessoTrf);

		Integer max = (Integer) qry.getSingleResult();
		if (max != null){
			max = max + 1;
		}
		else{
			max = 1;
		}

		return max;
	}

	@SuppressWarnings("unchecked")
	public List<E> recuperarExpedientesNaoAssinados(ProcessoTrf processoTrf){
/*		String hql = " select distinct o from " + getEntityClass().getSimpleName() + " o "
			+ " where o.processoDocumento.processoDocumentoBin.idProcessoDocumentoBin not in( "
			+ " 	select a.processoDocumentoBin.idProcessoDocumentoBin "
			+ "     from ProcessoDocumentoBinPessoaAssinatura a) "
			+ " and o.processoTrf.idProcessoTrf = ? " + " order by o.numero ";*/
		
		String hql = " select distinct o from " + getEntityClass().getSimpleName() + " o "
				+ " where o.situacaoExpedienteCriminal = :situacaoExpedienteCriminal "
				+ " and o.processoTrf.idProcessoTrf = :idProcessoTrf "
				+ " order by o.numero";		
		
		Query qry = getEntityManager().createQuery(hql);
		qry.setParameter("situacaoExpedienteCriminal", SituacaoExpedienteCriminalEnum.PA);
		qry.setParameter("idProcessoTrf", processoTrf.getIdProcessoTrf());
		List<E> resultList = qry.getResultList();
		return resultList;
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoExpedienteCriminal> pesquisarExpedientesCriminais(
			TipoExpedienteCriminalEnum tipoExpedienteCriminal, String numeroProcesso, String nomePessoa,
			String documentoPessoa, Date dtInicio, Date dtTermino, SituacaoExpedienteCriminalEnum situacaoExpediente){

		Map<String, Object> parameters = new HashMap<String, Object>();
		String where = " where 1 = 1 ";

		String nomeClasse = "ProcessoExpedienteCriminal";

		if (tipoExpedienteCriminal != null){
			nomeClasse = tipoExpedienteCriminal.getNomeEntidade();
		}

		String hql = " select o from " + nomeClasse + " o ";

		if (numeroProcesso != null && !numeroProcesso.trim().equals("")){
			where += " and o.processoTrf.processo.numeroProcesso like concat('%',:numeroProcesso,'%') ";
			parameters.put("numeroProcesso", numeroProcesso);
		}

		if (nomePessoa != null && !nomePessoa.trim().equals("")){
			where += " and lower(to_ascii(o.pessoa.nome)) like lower(concat('%', TO_ASCII(:nomePessoa), '%'))";
			parameters.put("nomePessoa", nomePessoa);
		}

		if (documentoPessoa != null && !documentoPessoa.trim().equals("")){
			hql += " inner join o.pessoa.pessoaDocumentoIdentificacaoList d ";
			where += " and d.numeroDocumento = :numeroDocumento ";
			parameters.put("numeroDocumento", documentoPessoa);
		}

		if (dtInicio != null && dtTermino != null){
			where += " and o.dtCriacao between :dtInicio and :dtTermino ";
			parameters.put("dtInicio", dtInicio);
			parameters.put("dtTermino", dtTermino);
		}

		if (situacaoExpediente != null){
			where += " and o.situacaoExpedienteCriminal = :situacaoExpedienteCriminal ";
			parameters.put("situacaoExpedienteCriminal",situacaoExpediente);
		}

		hql += where;
		hql += " order by o.numero ";

		Query qry = getEntityManager().createQuery(hql);
		for (String key : parameters.keySet()){
			qry.setParameter(key, parameters.get(key));
		}

		List<ProcessoExpedienteCriminal> resultList = qry.getResultList();
		return resultList;
	}
}
