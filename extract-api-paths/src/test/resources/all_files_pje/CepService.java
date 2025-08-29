/**
 * 
 */
package br.jus.cnj.pje.nucleo.service;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.CepDAO;
import br.jus.pje.nucleo.entidades.Cep;

/**
 * @author cristof
 * 
 */
@Name("cepService")
@Transactional
public class CepService {

	@In(create = true)
	private CepDAO cepDAO;

	/**
	 * @return Instância de CepService.
	 */
	public static CepService instance(){
		return ComponentUtil.getComponent(CepService.class);
	}
	
	public Cep findByCodigo(String codigo) {
		String codigoFormatado = formataCep(codigo);
		return cepDAO.findByCodigo(codigoFormatado);
	}
	
	public List<Cep> findByNumero(String numero) {
		String codigoFormatado = formataCep(numero);
		return cepDAO.findByNumero(codigoFormatado);
	}

	private String formataCep(String numero) {
		String codigoFormatado = null;
		if (numero.length() == 8) {
			codigoFormatado = acrescentaMascaraCep(numero);
		} else if (numero.length() == 9) {
			codigoFormatado = numero;
		} else {
			throw new IllegalArgumentException(
					"Não é permitido consultar o CEP por parte do código postal nessa função.");
		}
		return codigoFormatado;
	}

	public Cep findById(int id) {
		return cepDAO.findById(id);
	}

	public List<Cep> findByBegin(String codigo) {
		String codigoFormatado = acrescentaMascaraCep(codigo);
		if (codigoFormatado.length() < 4) {
			throw new IllegalArgumentException(
					"Não é permitido consultar o CEP com número de caracteres inferiores a 3.");
		}
		return cepDAO.findByBegin(codigoFormatado);
	}

	public static String acrescentaMascaraCep(String codigo) {
		String codigoFormatado = codigo.replace("-", "");
		if (codigoFormatado.length() < 5) {
			return codigoFormatado;
		} else if (codigoFormatado.length() <= 8) {
			codigoFormatado = codigoFormatado.substring(0, 5) + "-" + codigoFormatado.substring(5);
			return codigoFormatado;
		} else {
			throw new IllegalArgumentException("Código de CEP inválido");
		}
	}
	
	public static String removeMascaraCep(String codigo){
		if (codigo != null) {
			return codigo.replaceAll("[^0-9]*", "");
		}
		return codigo;
	}

}
