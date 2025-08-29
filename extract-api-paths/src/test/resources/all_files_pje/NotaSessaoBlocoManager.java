package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.Name;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.NotaSessaoBlocoDAO;
import br.jus.pje.nucleo.entidades.BlocoJulgamento;
import br.jus.pje.nucleo.entidades.NotaSessaoBloco;

@Name("notaSessaoBlocoManager")
public class NotaSessaoBlocoManager extends BaseManager<NotaSessaoBloco> {

	@Override
	protected NotaSessaoBlocoDAO getDAO() {
		return ComponentUtil.getNotaSessaoBlocoDAO();
	}

	public List<NotaSessaoBloco> recuperar(BlocoJulgamento bloco) {
		List<NotaSessaoBloco> result = new ArrayList<>();

		if (bloco != null) {
			result = this.getDAO().recuperar(bloco);
		}

		return result;
	}
}
