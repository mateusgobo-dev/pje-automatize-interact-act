package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.Name;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ConsultaProcessoAdiadoVistaDAO;
import br.jus.pje.nucleo.dto.FiltroProcessoSessaoDTO;
import br.jus.pje.nucleo.entidades.ConsultaProcessoAdiadoVista;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name("consultaProcessoAdiadoVistaManager")
public class ConsultaProcessoAdiadoVistaManager extends BaseManager<ConsultaProcessoAdiadoVista>{

	@Override
	protected ConsultaProcessoAdiadoVistaDAO getDAO() {
		return ComponentUtil.getConsultaProcessoAdiadoVistaDAO();
	}


	public List<ProcessoTrf> pesquisarAdiados(FiltroProcessoSessaoDTO filtro) {
		return getDAO().pesquisarAdiados(filtro);
	}

	public List<ProcessoTrf> pesquisarVista(FiltroProcessoSessaoDTO filtro) {
		return getDAO().pesquisarVista(filtro);
	}
}
