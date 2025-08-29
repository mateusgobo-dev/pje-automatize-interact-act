/**
 * OrgaoJulgadorParaIntercomunicacaoOrgaoJulgadorConverter.java
 * 
 * Data de criação: 23/09/2013
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;

/**
 * Conversor de OrgaoJulgador para OrgaoJulgador da intercomunicação.
 * 
 * @author Adriano Pamplona
 */
public class OrgaoJulgadorParaIntercomunicacaoOrgaoJulgadorConverter
		extends
		IntercomunicacaoConverterAbstrato<OrgaoJulgador, br.jus.cnj.intercomunicacao.v222.beans.OrgaoJulgador> {

	private static final int STF = 100;
	private static final int CNJ = 200;
	private static final int STJ = 300;
	private static final int CJF = 490;
	private static final int CSJT = 590;

	private static final Map<Integer, String> tipoInstanciaMap = new HashMap<Integer, String>();

	static {
		tipoInstanciaMap.put(STF, "EXT");
		tipoInstanciaMap.put(STJ, "ESP");
		tipoInstanciaMap.put(CNJ, "ADM");
		tipoInstanciaMap.put(CJF, "ADM");
		tipoInstanciaMap.put(CSJT, "ADM");
	}

	@In
	OrgaoJulgadorManager orgaoJulgadorManager = (OrgaoJulgadorManager) Component
			.getInstance("orgaoJulgadorManager");

	@Override
	public br.jus.cnj.intercomunicacao.v222.beans.OrgaoJulgador converter(
			OrgaoJulgador orgaoJulgador) {
		throw new UnsupportedOperationException(
				"Usar converter(OrgaoJulgador orgaoJulgador, Integer numeroOrgaoJustica)");
	}

	public br.jus.cnj.intercomunicacao.v222.beans.OrgaoJulgador converter(
			OrgaoJulgador orgaoJulgador, Integer numeroOrgaoJustica) {
		br.jus.cnj.intercomunicacao.v222.beans.OrgaoJulgador resultado = null;
		
		try {
			orgaoJulgador = orgaoJulgadorManager.findById(orgaoJulgador.getIdOrgaoJulgador());
			if (isNotNull(orgaoJulgador)) {
				resultado = new br.jus.cnj.intercomunicacao.v222.beans.OrgaoJulgador();
				resultado.setCodigoMunicipioIBGE(obterCodigoMunicipioIBGE(orgaoJulgador));
				resultado.setCodigoOrgao(converterParaString(orgaoJulgador
						.getIdOrgaoJulgador()));
				resultado.setInstancia(obterInstancia(numeroOrgaoJustica));
				resultado.setNomeOrgao(orgaoJulgador.getOrgaoJulgador());
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}

		return resultado;
	}

	/**
	 * @param orgaoJulgador
	 * @return Código IBGE do município
	 */
	private Integer obterCodigoMunicipioIBGE(OrgaoJulgador orgaoJulgador) {
		Integer resultado = null;
		if (!isServicoConsultarAvisosPendentes()) {
			try {
				orgaoJulgador = orgaoJulgadorManager.findById(orgaoJulgador
						.getIdOrgaoJulgador());
			} catch (PJeBusinessException e) {
				e.printStackTrace();
			}
		}

		if (orgaoJulgador != null
				&& orgaoJulgador.getLocalizacao() != null
				&& orgaoJulgador.getLocalizacao().getEndereco() != null
				&& orgaoJulgador.getLocalizacao().getEndereco().getCep() != null
				&& orgaoJulgador.getLocalizacao().getEndereco().getCep()
						.getMunicipio() != null) {

			Localizacao localizacao = orgaoJulgador.getLocalizacao();
			Endereco endereco = localizacao.getEndereco();
			Cep cep = endereco.getCep();
			Municipio municipio = cep.getMunicipio();
			String codigoIbge = municipio.getCodigoIbge();
			resultado = (NumberUtils.isNumber(codigoIbge) ? Integer
					.parseInt(codigoIbge) : 0);
		}
		return (resultado == null ? 0 : resultado);
	}

	private String obterInstancia(Integer numeroOrgaoJustica) {
		String instancia = tipoInstanciaMap.get(numeroOrgaoJustica);

		if (instancia == null) {
			String numeroOrgaoJusticaString = ParametroUtil
					.getParametro("numeroOrgaoJustica");

			if (numeroOrgaoJusticaString != null) {

				boolean inicial = numeroOrgaoJustica.equals(Integer
						.parseInt(numeroOrgaoJusticaString));

				instancia = inicial ? "ORI" : "REV";
			}

		}

		return instancia;
	}

}
