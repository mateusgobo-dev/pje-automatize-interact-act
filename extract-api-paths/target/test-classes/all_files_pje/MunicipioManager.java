package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.MunicipioDAO;
import br.jus.pje.nucleo.dto.MunicipioDTO;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Municipio;

@Name(MunicipioManager.NAME)
public class MunicipioManager extends BaseManager<Municipio> {
	
	public static final String NAME = "municipioManager"; 
	
	@In
	private MunicipioDAO municipioDAO;

	@Override
	protected BaseDAO<Municipio> getDAO() {
		return municipioDAO;
	}
	
	public List<Municipio> findByUf(String uf){
		return municipioDAO.findByUf(uf);
	}
	
	public Municipio findByUfAndDescricao(String uf,String descricao){
		return municipioDAO.findByUfAndDescricao(uf, descricao);
	}
	
	/**
	 * Recupera o objeto {@link Municipio} que representa o município de nascimento do usuário.
	 * 
	 * @param id Identificador do usuário no sistema.
	 * @return {@link Municipio} que representa o município de nascimento do usuário.
	 */
	public Municipio getMunicipioByIdPessoa(Object id) {
		return municipioDAO.getMunicipioByIdPessoa(id);
	}
	
	public Municipio getMunicipioByCodigoIBGE(String codigoIbge) {
		return municipioDAO.getMunicipioByCodigoIBGE(codigoIbge);
	}
	
	public List<Municipio> findAllByIdEstado(Integer idEstado){
		return this.municipioDAO.findAllByIdEstado(idEstado);
	}
	
	public List<MunicipioDTO> findAllByIdEstadoDTO(Integer idEstado){
		return this.municipioDAO.findAllByIdEstadoDTO(idEstado);
	}

	public List<Municipio> recuperarPorJurisdicao(Integer idJurisdicao) {
		return this.municipioDAO.recuperarPorJurisdicao(idJurisdicao);
	}
	
	/**
	 * De acordo com a string do autoComplete recupera a lista de munc?pios. 
	 * 
	 * @param texto String texto do autoComplete
	 * @param idEstado Integer id do estado selecionado na combo.
	 * @return List<Municipio> lista com os munic?pios.
	 */
	public List<Municipio> filtrarMunicipios(String texto, Integer idEstado) {
		return municipioDAO.filtrarMunicipios(texto, idEstado);
	}
	
	public List<Municipio> recuperarPorEstadoComJurisdicao(Estado estado) {
		List<Municipio> result = new ArrayList<>();
		if (estado != null) {
			result = this.municipioDAO.recuperarPorEstadoComJurisdicao(estado.getIdEstado());
		}
		return result;
	}

	public List<Municipio> recuperarPorEstadoComJurisdicaoCompetenciaAtiva(Integer idEstado) {
		return this.municipioDAO.recuperarPorEstadoComJurisdicaoCompetenciaAtiva(idEstado);
	}

}
