package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Bairro;
import br.jus.pje.nucleo.entidades.Municipio;

@Name(BairroHome.NAME)
public class BairroHome extends AbstractHome<Bairro> {

	private static final long serialVersionUID = -1303250991821453622L;
	public static final String NAME = "bairroHome";
	private int localizacao = Authenticator.getLocalizacaoAtual().getIdLocalizacao();

	@Override
	protected boolean beforePersistOrUpdate() {
		if (verificaBairrosMesmoNomeMunicipio()) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"Já existe um Bairro cadastrado com este nome neste Município.");
			return false;
		}
		return true;
	}
	
	private boolean verificaBairrosMesmoNomeMunicipio() {
		Query query = EntityUtil.getEntityManager().createQuery(
				"select count(o) from Bairro o where lower(o.dsBairro) = :descricao "
						+ "and o.municipio.idMunicipio = :municipio and o.idBairro <> :idBairro");
		query.setParameter("descricao", getInstance().getDsBairro().toLowerCase());
		query.setParameter("municipio", getInstance().getMunicipio().getIdMunicipio());
		query.setParameter("idBairro", getInstance().getIdBairro());
		
		try {
			Long retorno = (Long) query.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	@Factory(value = "municipioBairroItens")
	public List<Municipio> getmunicipioBairroItens() {
		return obterMunicipiosComBairro(localizacao);
	}
	
	/**
	 * Retorna uma lista de Municípios que possui bairros cadastrados no sistema, e que os bairros estejam vinculados à área e à centrais de mandado.
	 * @param 	idlocalizacao
	 * @return	uma lista de municípios cadastrados e que tenham vínculo com os bairros existentes na "tb_bairro", ordenada pelo nome do município.
	 */
	@SuppressWarnings("unchecked")
	private List<Municipio> obterMunicipiosComBairro (Integer idlocalizacao){
		List<Municipio> municipios = new ArrayList<Municipio>();

		StringBuilder hql = new StringBuilder();
		hql.append("SELECT DISTINCT munic FROM Bairro b ");
		hql.append("INNER JOIN b.area area ");
		hql.append("INNER JOIN b.municipio munic ");
		hql.append("INNER JOIN area.centralMandado cm ");
		hql.append("INNER JOIN cm.centralMandadoLocalizacaoList cml ");
		hql.append("WHERE cml.localizacao.idLocalizacao = :paramIdLocalizacao ");
		hql.append("ORDER BY munic.municipio ");
		
		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("paramIdLocalizacao", idlocalizacao);
		municipios = query.getResultList();

		return municipios;
	}
}
