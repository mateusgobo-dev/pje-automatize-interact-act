/**
 * IntercomunicacaoTest.java
 * 
 * Data: 28/10/2013
 */
package br.jus.cnj.pje.intercomunicacao.v222.servico;

import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import br.jus.cnj.intercomunicacao.v222.beans.ManifestacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.RequisicaoConsultaAvisosPendentes;
import br.jus.cnj.intercomunicacao.v222.beans.RequisicaoConsultaProcesso;
import br.jus.cnj.intercomunicacao.v222.beans.RequisicaoConsultarTeorComunicacao;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaConsultaAvisosPendentes;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaConsultaProcesso;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaConsultarTeorComunicacao;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaManifestacaoProcessual;
import br.jus.cnj.pje.webservice.util.RestUtil;

/**
 * Classe de teste da interface do MNI Rest (Intercomunicacao).
 * 
 * @author adriano.pamplona
 */
@FixMethodOrder (MethodSorters.NAME_ASCENDING)
@SuppressWarnings("all")
public class IntercomunicacaoRestTest extends IntercomunicacaoTest {
	protected static final String URL_API = "http://localhost:8080/pje/seam/resource/rest/pje-legacy/api/v1/servico-intercomunicacao-2.2.2";
	protected static final String URL_PATH_ENTREGAR_MANIFESTACAO_PROCESSUAL = "/entregar-manifestacao-processual";
	protected static final String URL_PATH_CONSULTAR_PROCESSO = "/consultar-processo";
	protected static final String URL_PATH_CONSULTAR_AVISOS_PENDENTES = "/consultar-avisos-pendentes";
	protected static final String URL_PATH_CONSULTAR_TEOR_COMUNICACAO = "/consultar-teor-comunicacao";
	
	@Override
	protected RespostaManifestacaoProcessual mniEntregarManifestacaoProcessual(ManifestacaoProcessual parametro) {
		return RestUtil.post(URL_API+URL_PATH_ENTREGAR_MANIFESTACAO_PROCESSUAL, parametro, RespostaManifestacaoProcessual.class);
	}
	
	@Override
	protected RespostaConsultaProcesso mniConsultarProcesso(RequisicaoConsultaProcesso parametro) {
		return RestUtil.post(URL_API+URL_PATH_CONSULTAR_PROCESSO, parametro, RespostaConsultaProcesso.class);
	}
	
	@Override
	protected RespostaConsultaAvisosPendentes mniConsultarAvisosPendentes(RequisicaoConsultaAvisosPendentes parametro) {
		return RestUtil.post(URL_API+URL_PATH_CONSULTAR_AVISOS_PENDENTES, parametro, RespostaConsultaAvisosPendentes.class);
	}
	
	@Override
	protected RespostaConsultarTeorComunicacao mniConsultarTeorComunicacao(RequisicaoConsultarTeorComunicacao parametro) {
		return RestUtil.post(URL_API+URL_PATH_CONSULTAR_TEOR_COMUNICACAO, parametro, RespostaConsultarTeorComunicacao.class);
	}
}