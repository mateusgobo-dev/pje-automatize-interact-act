package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.TipoParteConfiguracao;

@Name(TipoParteConfiguracaoDAO.NAME)
public class TipoParteConfiguracaoDAO extends BaseDAO<TipoParteConfiguracao>{

 public static final String NAME = "tipoParteConfiguracaoDAO";
 
 
 @Override
 public Object getId(TipoParteConfiguracao e) {
   return e.getIdTipoParteConfiguracao();
 }
 
 	/**
	 * Método responsável por recuperar as configurações de um tipo parte caso
	 * ela seja padrão ou não
	 * 
	 * @param tipoParte
	 *            parâmetro que se deseja recuperar as configurações
	 * @param padrao
	 *            caso a configuração seja padrão ou não
	 * @return <code>List</code> contendo as configurações de um tipo parte
	 */
 	@SuppressWarnings("unchecked")
	public List<TipoParteConfiguracao> recuperarPorTipoPartePadrao(TipoParte tipoParte, boolean padrao) {
		StringBuilder sb = new StringBuilder("SELECT o FROM TipoParteConfiguracao AS o ");
		sb.append(" WHERE o.tipoParte = :tipoParte ");
		sb.append(" AND o.padrao = :padrao ");
		
		Query query = entityManager.createQuery(sb.toString());
		query.setParameter("tipoParte", tipoParte);
		query.setParameter("padrao", padrao);
		
		List<TipoParteConfiguracao> resultList = query.getResultList();
		return resultList.isEmpty() ? new ArrayList<TipoParteConfiguracao>(0) : resultList;
	}

	/**
	 * de acordo com a {@link TipoParte} verifica se contem
	 * uma {@link TipoParteConfiguracao}
	 * @return
	 */
	public boolean isContemConfiguracao(TipoParte tipoParte) {
		StringBuilder sql = new StringBuilder();
		sql.append("select count(*) from TipoParteConfiguracao o ");
		sql.append("where o.tipoParte = :tipoParte and o.padrao = true");
		Query query = getEntityManager().createQuery(sql.toString());
		query.setParameter("tipoParte", tipoParte);
		return ((Long)query.getSingleResult()) > 0;
	} 


}
