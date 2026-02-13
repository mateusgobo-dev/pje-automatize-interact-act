package br.jus.cnj.pje.business.dao;

import java.util.List;
import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.DimensaoAlcada;

@Name(DimensaoAlcadaDAO.NAME)
public class DimensaoAlcadaDAO extends BaseDAO<DimensaoAlcada> {

	public static final String NAME = "dimensaoAlcadaDAO";
	
	@Override
	public Integer getId(DimensaoAlcada da) {
		return da.getIdDimensaoAlcada();
	}
	
	public List<DimensaoAlcada> getDimensoesAptas(){
		return null;
	}

}
