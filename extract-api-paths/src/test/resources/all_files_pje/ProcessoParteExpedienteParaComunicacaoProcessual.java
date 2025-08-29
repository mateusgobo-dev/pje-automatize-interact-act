package br.jus.cnj.pje.intercomunicacao.v222.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.infox.cliente.NumeroProcessoUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.intercomunicacao.v222.beans.ComunicacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.DataHora;
import br.jus.cnj.intercomunicacao.v222.beans.DocumentoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.Endereco;
import br.jus.cnj.intercomunicacao.v222.beans.Parte;
import br.jus.cnj.intercomunicacao.v222.beans.TipoPrazo;
import br.jus.cnj.pje.intercomunicacao.util.constant.MNIParametro;
import br.jus.cnj.pje.intercomunicacao.v222.util.ConversorUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;
import br.jus.pje.nucleo.util.DateUtil;

/**
 * Converte Expedientes Processuais em Comunicação Processual
 * @author rodrigoar
 *
 */
public class ProcessoParteExpedienteParaComunicacaoProcessual extends
	IntercomunicacaoConverterAbstrato<ProcessoParteExpediente, ComunicacaoProcessual>{

	public ComunicacaoProcessual converter(ProcessoParteExpediente objeto) {
		ComunicacaoProcessual comunicacaoProcessual = new ComunicacaoProcessual();
		comunicacaoProcessual.setDestinatario(obterDestinatario(objeto));
		comunicacaoProcessual.setId(novoIdentificador(objeto.getIdProcessoParteExpediente()));
		comunicacaoProcessual.setNivelSigilo(objeto.getProcessoJudicial().getSegredoJustica() ? 1 : 0);
		if(objeto.getDtPrazoLegal() != null){
			DataHora data = ConversorUtil.converterParaDataHora(objeto.getDtPrazoLegal());
			comunicacaoProcessual.setDataReferencia(data);
			comunicacaoProcessual.setPrazo(0);
			comunicacaoProcessual.setTipoPrazo(TipoPrazo.DATA_CERTA);
		} else {
			DataHora data = ConversorUtil.converterParaDataHora(objeto.getDtCienciaParte());
			comunicacaoProcessual.setDataReferencia(data);
			comunicacaoProcessual.setTipoPrazo(obterTipoPrazo(objeto));
			
			if(objeto.getPrazoLegal() != null){
				comunicacaoProcessual.setPrazo(objeto.getPrazoLegal());	
			}
			
			
		}
		comunicacaoProcessual.setProcesso(obterNumeroProcesso(objeto.getProcessoJudicial()));
		comunicacaoProcessual.setTeor(objeto.getProcessoDocumento().getProcessoDocumentoBin().getNomeArquivo());
		comunicacaoProcessual.setTipoComunicacao(obterTipoComunicacao(objeto));
		comunicacaoProcessual.getDocumento().add(obterDocumento(objeto));

		if(objeto.getProcessoExpediente() != null && objeto.getProcessoExpediente().getMeioExpedicaoExpediente() != null){
			ExpedicaoExpedienteEnum meioComunicacao = objeto.getProcessoExpediente().getMeioExpedicaoExpediente();
			comunicacaoProcessual.getParametro().add("meioComunicacao:"+meioComunicacao);
		}
		String dataHora = DateUtil.dateToString(objeto.getDtCienciaParte(), MNIParametro.PARAM_FORMATO_DATA_HORA);
		comunicacaoProcessual.getParametro().add("dataHoraInicioPrazo:"+dataHora);
		
		return comunicacaoProcessual;
	}

	/**
	 * Retorna a coleção de Collection<DocumentoProcessual>.
	 * 
	 * @param objeto ProcessoParteExpediente
	 * @return Collection<DocumentoProcessual>
	 */
	private DocumentoProcessual obterDocumento(ProcessoParteExpediente objeto) {
		DocumentoProcessual resultado = null;
		ProcessoDocumento processoDocumento = objeto.getProcessoDocumento();
		
		if (isNotNull(processoDocumento)) {
			ProcessoDocumentoParaDocumentoConverter processoDocumentoParaDocumentoConverter = novoProcessoDocumentoParaDocumentoConverter();
			List<String> listaIdCarregarBinario = new ArrayList<String>();
			listaIdCarregarBinario.add("*");
			resultado = processoDocumentoParaDocumentoConverter.converter(processoDocumento, listaIdCarregarBinario);
		}
		return resultado;
	}

	/**
	 * Retorna o tipo do prazo do expediente.
	 * 
	 * @param objeto ProcessoParteExpediente
	 * @return TipoPrazo do MNI.
	 */
	private TipoPrazo obterTipoPrazo(ProcessoParteExpediente objeto) {
		Map<TipoPrazoEnum, TipoPrazo> mapa = new HashMap<TipoPrazoEnum, TipoPrazo>();
		mapa.put(TipoPrazoEnum.A, TipoPrazo.ANO);
		mapa.put(TipoPrazoEnum.D, TipoPrazo.DIA);
		mapa.put(TipoPrazoEnum.C, TipoPrazo.DATA_CERTA);
		mapa.put(TipoPrazoEnum.H, TipoPrazo.HOR);
		mapa.put(TipoPrazoEnum.M, TipoPrazo.MES);
		mapa.put(TipoPrazoEnum.N, TipoPrazo.HOR);
		mapa.put(TipoPrazoEnum.S, TipoPrazo.SEMPRAZO);
		
		return mapa.get(objeto.getTipoPrazo());
	}

	/**
	 * Retorna o número do processo judicial sem formatação.
	 * 
	 * @param processoJudicial
	 * @return String.
	 */
	private String obterNumeroProcesso(ProcessoTrf processoJudicial) {
		String resultado = null;
		
		if (isNotNull(processoJudicial)) {
			resultado = processoJudicial.getNumeroProcesso();
			resultado = NumeroProcessoUtil.retiraMascaraNumeroProcesso(resultado);
		}
		
		return resultado;
	}

	/**
	 * Retorna o destinatário do expediente.
	 * 
	 * @param ppe
	 * @return Parte.
	 */
	private Parte obterDestinatario(ProcessoParteExpediente ppe) {
		ProcessoTrf processo = ppe.getProcessoJudicial();
		Pessoa pessoaParte = ppe.getPessoaParte();
		PessoaParaParteConverter converter = novoPessoaParaParteConverter(); 
		Parte parte = converter.converter(pessoaParte, processo.getJusticaGratuita());
		parte.getPessoa().getEndereco().addAll(consultarColecaoEndereco(ppe));
		
		return parte;
	}

	/**
	 * @return ProcessoDocumentoParaDocumentoConverter
	 */
	private ProcessoDocumentoParaDocumentoConverter novoProcessoDocumentoParaDocumentoConverter() {
		return ComponentUtil.getComponent(ProcessoDocumentoParaDocumentoConverter.class);
	}

	/**
	 * @return PessoaParaParteConverter
	 */
	private PessoaParaParteConverter novoPessoaParaParteConverter() {
		return ComponentUtil.getComponent(PessoaParaParteConverter.class);
	}
	
	/**
	 * @param representante
	 * @return coleção de endereços do ProcessoParteExpediente.
	 */
	private Collection<Endereco> consultarColecaoEndereco(ProcessoParteExpediente ppe) {
		List<br.jus.pje.nucleo.entidades.Endereco> enderecos = ppe.getEnderecos();
		EnderecoParaIntercomunicacaoEnderecoConverter enderecoParaIntercomunicacaoEnderecoConverter = ComponentUtil.getComponent(EnderecoParaIntercomunicacaoEnderecoConverter.class);

		return enderecoParaIntercomunicacaoEnderecoConverter.converterColecao(enderecos);
	}
}
