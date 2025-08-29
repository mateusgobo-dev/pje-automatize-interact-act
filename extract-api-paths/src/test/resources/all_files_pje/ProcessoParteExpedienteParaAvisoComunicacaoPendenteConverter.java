/**
 * ProcessoParteExpedienteParaAvisoComunicacaoPendenteConverter.java
 * 
 * Data de criação: 23/09/2013
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Transformer;

import br.com.infox.cliente.util.JSONUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.intercomunicacao.v222.beans.AvisoComunicacaoPendente;
import br.jus.cnj.intercomunicacao.v222.beans.CabecalhoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.DataHora;
import br.jus.cnj.intercomunicacao.v222.beans.Endereco;
import br.jus.cnj.intercomunicacao.v222.beans.Identificador;
import br.jus.cnj.intercomunicacao.v222.beans.Parte;
import br.jus.cnj.intercomunicacao.v222.beans.Parametro;
import br.jus.cnj.pje.intercomunicacao.util.constant.MNIParametro;
import br.jus.cnj.pje.intercomunicacao.v222.util.ConversorUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * Conversor de ProcessoParteExpediente para AvisoComunicacaoPendente.
 * 
 * @author Adriano Pamplona
 */
public class ProcessoParteExpedienteParaAvisoComunicacaoPendenteConverter
		extends
		IntercomunicacaoConverterAbstrato<ProcessoParteExpediente, AvisoComunicacaoPendente> {
	
	/**
	 * Converte uma lista de objetos do tipo 'ProcessoParteExpediente' para objetos do tipo 'AvisoComunicacaoPendente'.
	 * A conversão continuará mesmo ocorrendo erro.
	 * 
	 * @param colecaoObjeto Lista de objetos do tipo 'ProcessoParteExpediente'.
	 * @param transformer Transformador.
	 * @return Lista de objetos do tipo 'AvisoComunicacaoPendente'.
	 */
	protected List<AvisoComunicacaoPendente> aplicarTransformador(List<ProcessoParteExpediente> colecaoObjeto, Transformer transformer) {
		List<AvisoComunicacaoPendente> resultado = new ArrayList<AvisoComunicacaoPendente>();
		
		if (isNotVazio(colecaoObjeto) && isNotNull(transformer)) {
			for (Iterator<ProcessoParteExpediente> iterator = colecaoObjeto.iterator(); iterator.hasNext(); ) {
				try {
					ProcessoParteExpediente objeto = iterator.next();
					AvisoComunicacaoPendente objetoConvertido = (AvisoComunicacaoPendente) transformer.transform(objeto);
					
					if (isNotNull(objetoConvertido)) resultado.add(objetoConvertido);
				} catch (Exception e) {
					//se ocorrer erro a conversão continuará.
					e.printStackTrace();
				}  
			}
		}
		return resultado;
	}
	
	@Override
	public AvisoComunicacaoPendente converter(ProcessoParteExpediente expediente) {
		AvisoComunicacaoPendente resultado = new AvisoComunicacaoPendente();

		if (isNotNull(expediente)) {
			resultado.setProcesso(obterCabecalhoProcessual(expediente.getProcessoJudicial()));
			resultado.setDataDisponibilizacao(obterDataDisponibilizacao(expediente));
			try {
				resultado.setDestinatario(obterDestinatario(expediente));
			} catch (PJeBusinessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			resultado.setIdAviso(obterIdAviso(expediente));
			resultado.setTipoComunicacao(obterTipoComunicacao(expediente));
			adicionarInformacoesComplementares(resultado.getProcesso(), expediente);
			
		}
		return resultado;
	}

	/**
	 * @param expediente
	 * @return identificador do aviso.
	 */
	protected Identificador obterIdAviso(ProcessoParteExpediente expediente) {
		Identificador resultado = new Identificador();
		resultado.setValue(String.valueOf(expediente.getIdProcessoParteExpediente()));
		return resultado;
	}

	/**
	 * TODO (adriano.pamplona): verificar.
	 * @param expediente
	 * @return destinatário (pessoaCiencia)
	 * @throws PJeBusinessException 
	 *
	 */
	protected Parte obterDestinatario(ProcessoParteExpediente expediente) throws PJeBusinessException {
		ProcessoTrf processo = expediente.getProcessoJudicial();
		Pessoa pessoaParte = expediente.getPessoaParte();
		PessoaParaParteConverter converter = novoPessoaParaParteConverter(); 
		Parte parte = converter.converter(pessoaParte, processo.getJusticaGratuita());
		parte.getPessoa().getEndereco().addAll(consultarColecaoEndereco(expediente));
		
		return parte;		
	}

	/**
	 * @param expediente
	 * @return data de disponibilização.
	 */
	protected DataHora obterDataDisponibilizacao(ProcessoParteExpediente expediente) {
		Date dataDisponibilizacao = expediente.getDataDisponibilizacao();
		return ConversorUtil.converterParaDataHora(dataDisponibilizacao);
	}

	/**
	 * Converte ProcessoTrf para CabecalhoProcessual.
	 * 
	 * @param processoJudicial
	 * @return CabecalhoProcessual
	 */
	protected CabecalhoProcessual obterCabecalhoProcessual(ProcessoTrf processo) {
		ProcessoTrfParaCabecalhoProcessualConverter converter = new ProcessoTrfParaCabecalhoProcessualConverter();
		return converter.converter(processo);
	}
	
	/**
	 * @return PessoaParaParteConverter
	 */
	protected PessoaParaParteConverter novoPessoaParaParteConverter() {
		return ComponentUtil.getComponent(PessoaParaParteConverter.class);
	}

	/**
	 * @param representante
	 * @return coleção de endereços do ProcessoParteExpediente.
	 */
	protected Collection<Endereco> consultarColecaoEndereco(ProcessoParteExpediente ppe) {
		List<br.jus.pje.nucleo.entidades.Endereco> enderecos = ppe.getEnderecos();
		EnderecoParaIntercomunicacaoEnderecoConverter enderecoParaIntercomunicacaoEnderecoConverter = ComponentUtil.getComponent(EnderecoParaIntercomunicacaoEnderecoConverter.class);

		return enderecoParaIntercomunicacaoEnderecoConverter.converterColecao(enderecos);
	}
	
	/**
	 * Adiciona informações complementares.
	 * 
	 * @param processo
	 * @param expediente
	 */
	private void adicionarInformacoesComplementares(CabecalhoProcessual processo, ProcessoParteExpediente expediente) {
		Map<String, Object> mapa = new HashMap<>();
		mapa.put("id", expediente.getIdProcessoParteExpediente());
		
		Parametro parametro = new Parametro();
		parametro.setNome(MNIParametro.PARAM_EXPEDIENTE);
		parametro.setValor(JSONUtil.converterObjetoParaString(mapa));
		processo.getOutroParametro().add(parametro);
	}
}
