package br.com.infox.cliente.home.icrrefactory;

import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import br.jus.pje.nucleo.entidades.IcrMedidaCautelarDiversa;
import br.jus.pje.nucleo.entidades.MedidaCautelarDiversa;
import br.jus.pje.nucleo.entidades.TipoLocalProibicao;
import br.jus.pje.nucleo.enums.TipoMedidaCautelarDiversaEnum;

@Name("icrMCDManager")
@Scope(ScopeType.EVENT)
public class IcrMedidaCautelarDiversaManager extends InformacaoCriminalRelevanteManager<IcrMedidaCautelarDiversa>{

	public List<TipoMedidaCautelarDiversaEnum> getTipoMedidaCautelarDiversaEnumList(){
		List<TipoMedidaCautelarDiversaEnum> list = new ArrayList<TipoMedidaCautelarDiversaEnum>(0);
		for (TipoMedidaCautelarDiversaEnum tipo : TipoMedidaCautelarDiversaEnum.values()){
			list.add(tipo);
		}
		// return Arrays.asList(TipoMedidaCautelarDiversaEnum.values());
		return list;
	}

	@Override
	protected void prePersist(IcrMedidaCautelarDiversa entity) throws IcrValidationException{
		super.prePersist(entity);
		for (MedidaCautelarDiversa medida : entity.getMedidasCautelaresDiversas()){
			medida.setIcr(entity);
		}
	}

	@SuppressWarnings("unchecked")
	public List<TipoLocalProibicao> getTipoLocalProibicaoList(){
		return getEntityManager().createQuery(
				"from TipoLocalProibicao where inAtivo = true").getResultList();
	}
}
