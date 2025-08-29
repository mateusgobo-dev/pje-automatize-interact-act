package br.com.jt.pje.dao;

import java.io.Serializable;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;
import br.com.itx.util.EntityUtil;


@Name(ResultadoSentencaParteDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class ResultadoSentencaParteDAO extends GenericDAO implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String NAME = "resultadoSentencaParteDAO";

  	public Double getSomaValorCondenacaoBy(Integer idProcesso) {
  		StringBuffer sb = new StringBuffer();
		sb.append("select cast(sum(rsp.valorCondenacao) as float) from ResultadoSentencaParte rsp ");
		sb.append("inner join rsp.resultadoSentenca rs ");
		sb.append("where rs.processoTrf.idProcessoTrf = :idProcessoTrf ");
		Query query = EntityUtil.createQuery(sb.toString());
		query.setParameter("idProcessoTrf", idProcesso);
		
		Float soma = EntityUtil.getSingleResult(query);
		return soma != null ? soma.doubleValue() : 0;
  	}
}
