package br.com.infox.ibpm.util;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

import br.com.itx.util.EntityUtil;

@Name(CarregarParametrosAplicacao.NAME)
@Scope(ScopeType.APPLICATION)
@Install()
@Startup(depends = {MigracaoBaseDados.NAME,"org.jboss.seam.navigation.pages"})
@BypassInterceptors
public class CarregarParametrosAplicacao implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "carregarParametrosAplicacao";

	@Create
	public void init() {
		StringBuilder sql = new StringBuilder("select trim(nm_variavel), vl_variavel  from tb_parametro where in_ativo = true");
		Query query = EntityUtil.getEntityManager().createNativeQuery(sql.toString());
		
		List<Object[]> rs = query.getResultList();
		
		if(rs != null) {
			for (Object[] r: rs) {
				Contexts.getApplicationContext().set((String)r[0], r[1]);
			}
		}
	}

}