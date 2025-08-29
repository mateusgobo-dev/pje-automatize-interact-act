package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.TipoModeloDocumento;

@Name(TipoModeloDocumentoDAO.NAME)
public class TipoModeloDocumentoDAO extends BaseDAO<TipoModeloDocumento>{
	
	public static final String NAME = "tipoModeloDocumentoDAO";

	@Override
	public Integer getId(TipoModeloDocumento e){
		return e.getIdTipoModeloDocumento();
	}

	/**
	 * Metodo responsavel por recuperar os tipos de modelos de documento de acordo com o papel atual.
	 * 
	 * @return List<TipoModeloDocumento>
	 */
	@SuppressWarnings("unchecked")
	public List<TipoModeloDocumento> obterTipoModeloDocumentoPorPapelAtual() {		
		StringBuilder sb = new StringBuilder("SELECT DISTINCT tmd FROM TipoModeloDocumento tmd ")
				.append("LEFT JOIN tmd.papeis papeis WHERE tmd.ativo IS TRUE ");
		
		if (!Authenticator.isPermissaoCadastroTodosPapeis()) {
			sb.append("AND papeis.identificador = #{authenticator.getIdentificadorPapelAtual()} ");
		}
		
		sb.append("ORDER BY tmd.tipoModeloDocumento");
		
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		List<TipoModeloDocumento> resultList = q.getResultList();
		
		return CollectionUtilsPje.isNotEmpty(resultList) ? resultList : new ArrayList<TipoModeloDocumento>();
	}
}
