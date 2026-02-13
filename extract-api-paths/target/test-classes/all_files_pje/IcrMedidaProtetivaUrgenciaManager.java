package br.com.infox.cliente.home.icrrefactory;

import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import br.jus.pje.nucleo.entidades.IcrMedidaProtetivaUrgencia;
import br.jus.pje.nucleo.entidades.TipoLocalProibicao;
import br.jus.pje.nucleo.enums.TipoMedidaProtetivaUrgenciaEnum;

@Name("icrMPUManager")
@Scope(ScopeType.EVENT)
public class IcrMedidaProtetivaUrgenciaManager extends
		InformacaoCriminalRelevanteManager<IcrMedidaProtetivaUrgencia>{

	public List<TipoMedidaProtetivaUrgenciaEnum> getTipoMedidaProtetivaUrgenciaEnumList(){
		List<TipoMedidaProtetivaUrgenciaEnum> tipoMedidaProtetivaUrgenciaList = new ArrayList<TipoMedidaProtetivaUrgenciaEnum>();
		for (TipoMedidaProtetivaUrgenciaEnum tipo : TipoMedidaProtetivaUrgenciaEnum
				.values()){
			tipoMedidaProtetivaUrgenciaList.add(tipo);
		}
		return tipoMedidaProtetivaUrgenciaList;
	}

	@SuppressWarnings("unchecked")
	public List<TipoLocalProibicao> getTipoLocalProibicaoList(){
		return getEntityManager().createQuery(
				"from TipoLocalProibicao where inAtivo = true").getResultList();
	}
}
