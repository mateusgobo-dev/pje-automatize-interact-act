/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.exceptions.NegocioException;
import br.jus.cnj.pje.business.dao.CepDAO;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * @author cristof
 * 
 */
@Name("cepManager")
public class CepManager extends BaseManager<Cep>{

	@In
	private CepDAO cepDAO;

	@Override
	protected CepDAO getDAO(){
		return cepDAO;
	}

	public Cep findByCep(String cep){
		String cepFormatado = cep;
		if (cep.length() == 8){
			cepFormatado = cep.substring(0, 5) + "-" + cep.substring(5);
		}
		return this.cepDAO.findByCodigo(cepFormatado);
	}
	
	/**
	 * Busca o CEP padrão de um município
	 * Algumas cidade possuem um cep único
	 * Será considerado CEP padrão quando os campos: logradouro, bairro e complementos forem nulos.
	 * @param idMunicipio
	 * @return List<Cep>
	 */
	public List<Cep> getCepDefaultByIdMunicipio(int idMunicipio){		
		return this.cepDAO.getCepDefaultByIdMunicipio(idMunicipio);
	}
	
	public static String formatarCep(String cep) {
		String cepFormatado = null;
		
		if (StringUtil.isNotEmpty(cep)) {
			cepFormatado = cep.replaceAll("\\D", "");
			
			if (cepFormatado.length() != 8) {
				throw new NegocioException("CEP Inválido. Informe o número com 8 posições.");
			} else {
				cepFormatado = cepFormatado.substring(0, 5) + "-" + cepFormatado.substring(5);
			}
		}
		
		return cepFormatado;
	}
}