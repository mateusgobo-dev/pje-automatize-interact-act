package br.com.infox.editor.dao;

import javax.persistence.Query;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.AdvogadoLocalizacaoCabecalho;
import br.jus.pje.nucleo.entidades.Localizacao;

@Name(AdvogadoLocalizacaoCabecalhoDao.NAME)
@AutoCreate
public class AdvogadoLocalizacaoCabecalhoDao {

	public static final String NAME = "advogadoLocalizacaoCabecalhoDao";
	
	public AdvogadoLocalizacaoCabecalho getAdvogadoLocalizacaoCabecalho(Localizacao localizacao){
		StringBuilder sb = new StringBuilder();
		sb.append("select o from AdvogadoLocalizacaoCabecalho o ");
		sb.append("where o.localizacao = :localizacao ");
		
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter("localizacao", localizacao);
		
		return EntityUtil.getSingleResult(q);
	}
}
