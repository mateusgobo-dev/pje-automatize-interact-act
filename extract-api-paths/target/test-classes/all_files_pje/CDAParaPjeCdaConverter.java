/**
 * CDAParaPjeCdaConverter.java
 * 
 * Data de criação: 21/08/2020
 */
package br.jus.cnj.pje.intercomunicacao.v223.converter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jboss.seam.annotations.Name;
import org.json.JSONObject;

import br.com.infox.cliente.util.JSONUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.intercomunicacao.v223.beans.DocumentoIdentificacao;
import br.jus.cnj.intercomunicacao.v223.beans.ModalidadeDocumentoIdentificador;
import br.jus.cnj.intercomunicacao.v223.beans.Parametro;
import br.jus.cnj.intercomunicacao.v223.beans.TipoQualificacaoPessoa;
import br.jus.cnj.intercomunicacao.v223.cda.CDA;
import br.jus.cnj.intercomunicacao.v223.cda.ModalidadeTipoDevedor;
import br.jus.cnj.intercomunicacao.v223.cda.ModalidadeTipoValorCDA;
import br.jus.cnj.pje.intercomunicacao.v223.util.ConversorUtil;
import br.jus.cnj.pje.intercomunicacao.v223.util.MNIParametroUtil;
import br.jus.pje.nucleo.entidades.Cda;
import br.jus.pje.nucleo.entidades.Debito;
import br.jus.pje.nucleo.entidades.DevedorCda;
import br.jus.pje.nucleo.entidades.DevedorDocIdentificacao;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.enums.EnumTipoDevedor;
import br.jus.pje.nucleo.enums.EnumTipoValorCda;

/**
 * Conversor de CDA para Cda.
 * 
 * @author Adriano Pamplona
 */
@Name (CDAParaPjeCdaConverter.NAME)
public class CDAParaPjeCdaConverter extends
		IntercomunicacaoConverterAbstrato<CDA, Cda> {

	public static final String NAME = "cdaParaPjeCdaConverter";
	
	/**
	 * @return Instância de CDAParaPjeCdaConverter.
	 */
	public static CDAParaPjeCdaConverter instance() {
		return ComponentUtil.getComponent(CDAParaPjeCdaConverter.class);
	}

	@Override
	public Cda converter(CDA mniCDA) {
		Cda resultado = null;
		if (isNotNull(mniCDA)) {
			resultado = new Cda();
			resultado.setNumero(mniCDA.getNumero());
			resultado.setNumeroControle(mniCDA.getNumeroControle());
			resultado.setDataApuracao(ConversorUtil.converterParaDate(mniCDA.getDataApuracao()));
			resultado.setDataPrescricao(ConversorUtil.converterParaDate(mniCDA.getDataPrescricao()));
			resultado.setInCreditoTributario(mniCDA.isCreditoTributario());
			resultado.setTipoValorCda(obterTipoValor(mniCDA.getTipoValor()));
			resultado.setValor(obterValor(mniCDA.getValor()));
			resultado.setMoedaValor("Real");
			resultado.getColecaoDevedorCda().addAll(obterColecaoDevedor(mniCDA, resultado));
			resultado.getColecaoDebito().addAll(obterColecaoDebito(mniCDA, resultado));
		}
		return resultado;
	}
	
	public CDA converter(Cda pjeCDA) {
		CDA resultado = null;
		if (isNotNull(pjeCDA)) {
			resultado = new CDA();
			resultado.setNumero(pjeCDA.getNumero());
			resultado.setNumeroControle(pjeCDA.getNumeroControle());
			resultado.setNumeroProcesso(pjeCDA.getProcessoTrf().getNumeroProcesso());
			resultado.setDataApuracao(converterParaDataHora(pjeCDA.getDataApuracao()));
			resultado.setDataPrescricao(converterParaDataHora(pjeCDA.getDataPrescricao()));
			resultado.setCreditoTributario(pjeCDA.getInCreditoTributario());
			resultado.setTipoValor(obterTipoValor(pjeCDA.getTipoValorCda()));
			resultado.setValor(obterValor(pjeCDA.getValor()));
			resultado.getDevedores().addAll(obterColecaoDevedor(pjeCDA, resultado));
			resultado.getDebitos().addAll(obterColecaoDebito(pjeCDA, resultado));
		}
		return resultado;
	}

	

	/**
	 * @param valor
	 * @return Valor do CDA.
	 */
	protected BigDecimal obterValor(String valor) {
		BigDecimal resultado = new BigDecimal(0);
		String valorSemMascara = valor.replaceAll("[.,]", "");
		if (StringUtils.isNotBlank(valorSemMascara)) {
			resultado = new BigDecimal(new BigInteger(valorSemMascara), 2);
		}
		return resultado;
	}
	
	/**
	 * @param valor
	 * @return Valor do CDA.
	 */
	protected String obterValor(BigDecimal valor) {
		NumberFormat formatter = DecimalFormat.getNumberInstance(new Locale("pt", "BR"));
		formatter.setMinimumIntegerDigits(1);
		formatter.setMaximumFractionDigits(2);
		formatter.setMinimumFractionDigits(2);
		return formatter.format(valor);
	}
	
	/**
	 * @param cda
	 * @param pjeCda
	 * @return Coleção dos devedores.
	 */
	protected Set<DevedorCda> obterColecaoDevedor(CDA cda, Cda pjeCda) {
		Set<DevedorCda> resultado = new HashSet<>();
		
		if (isNotNull(cda) && isNotVazio(cda.getDevedores()) ) {
			for (br.jus.cnj.intercomunicacao.v223.cda.Devedor mniDevedor : cda.getDevedores()) {
				DevedorCda pjeDevedor = new DevedorCda();
				pjeDevedor.setCda(pjeCda);
				pjeDevedor.setNome(mniDevedor.getNome());
				pjeDevedor.setTipoDevedor(obterTipoDevedor(mniDevedor.getTipo()));
				
				DocumentoIdentificacao mniIdentificacao = mniDevedor.getDocumentoPrincipal();
				mniIdentificacao.getTipoDocumento();
				
				DevedorDocIdentificacao pjeIdentificacao = new DevedorDocIdentificacao();
				pjeIdentificacao.setDevedorCda(pjeDevedor);
				pjeIdentificacao.setDevedorCda(pjeDevedor);
				pjeIdentificacao.setNomeDevedor(mniIdentificacao.getNome());
				pjeIdentificacao.setNumero(mniIdentificacao.getCodigoDocumento());
				pjeIdentificacao.setDescricaoOrgaoExpedidor(mniIdentificacao.getEmissorDocumento());
				pjeIdentificacao.setCodigoTipo(obterCodigoTipoDocumentoIdentificacao(mniIdentificacao));
				pjeDevedor.getColecaoDevedorDocIdentificacao().add(pjeIdentificacao);
				
				resultado.add(pjeDevedor);
			}
		}
		return resultado;
	}
	
	/**
	 * @param cda
	 * @param mniCDA
	 * @return Coleção dos devedores.
	 */
	protected List<br.jus.cnj.intercomunicacao.v223.cda.Devedor> obterColecaoDevedor(Cda cda, CDA mniCDA) {
		List<br.jus.cnj.intercomunicacao.v223.cda.Devedor> resultado = new ArrayList<>();
		
		if (isNotNull(cda) && isNotVazio(cda.getColecaoDevedorCda())) {
			for (DevedorCda pjeDevedor : cda.getColecaoDevedorCda()) {
				br.jus.cnj.intercomunicacao.v223.cda.Devedor mniDevedor = new br.jus.cnj.intercomunicacao.v223.cda.Devedor();
				mniDevedor.setNome(pjeDevedor.getNome());
				mniDevedor.setTipo(obterTipoDevedor(pjeDevedor.getTipoDevedor()));
				
				List<DevedorDocIdentificacao> identificacoes = pjeDevedor.getColecaoDevedorDocIdentificacao();
				if (identificacoes != null && identificacoes.size() > 0) {
					DevedorDocIdentificacao pjeIdentificacao = identificacoes.iterator().next();
					
					DocumentoIdentificacao mniIdentificacao = new DocumentoIdentificacao();
					mniIdentificacao.setCodigoDocumento(pjeIdentificacao.getNumero());
					mniIdentificacao.setNome(pjeIdentificacao.getNomeDevedor());
					mniIdentificacao.setEmissorDocumento(pjeIdentificacao.getDescricaoOrgaoExpedidor());
					mniIdentificacao.setTipoDocumento(obterCodigoTipoDocumentoIdentificacao(pjeIdentificacao));
					
					mniDevedor.setDocumentoPrincipal(mniIdentificacao);
				}
				
				resultado.add(mniDevedor);
			}
		}
		return resultado;
	}
	
	/**
	 * @param cda
	 * @param pjeCda
	 * @return Coleção dos débitos.
	 */
	protected Set<Debito> obterColecaoDebito(CDA cda, Cda pjeCda) {
		Set<Debito> resultado = new HashSet<>();
		
		if (isNotNull(cda) && isNotVazio(cda.getDebitos()) ) {
			for (br.jus.cnj.intercomunicacao.v223.cda.Debito mniDebito : cda.getDebitos()) {
				Map<String, Object> mapa = MNIParametroUtil.converterParaMap(mniDebito.getParametros());
				String json = JSONUtil.converterObjetoParaString(mapa);
				
				Debito pjeDebito = new Debito();
				pjeDebito.setCda(pjeCda);
				pjeDebito.setCda(pjeCda);
				pjeDebito.setDataExercicio(ConversorUtil.converterParaDate(mniDebito.getDataExercicio()));
				pjeDebito.setCodigoNatureza(mniDebito.getCodigoNatureza());
				pjeDebito.setDescricaoNatureza(mniDebito.getDescricaoNatureza());
				pjeDebito.setDados(json);
				
				resultado.add(pjeDebito);
			}
		}
		return resultado;
	}
	
	private List<br.jus.cnj.intercomunicacao.v223.cda.Debito> obterColecaoDebito(Cda cda,
			CDA mniCDA) {
		List<br.jus.cnj.intercomunicacao.v223.cda.Debito> resultado = new ArrayList<>();
		
		if (isNotNull(cda) && isNotVazio(cda.getColecaoDebito())) {
			for (Debito pjeDebito : cda.getColecaoDebito()) {
				
				br.jus.cnj.intercomunicacao.v223.cda.Debito mniDebito = new br.jus.cnj.intercomunicacao.v223.cda.Debito();
				mniDebito.setDataExercicio(converterParaDataHora(pjeDebito.getDataExercicio()));
				mniDebito.setCodigoNatureza(pjeDebito.getCodigoNatureza());
				mniDebito.setDescricaoNatureza(pjeDebito.getDescricaoNatureza());

				if (pjeDebito.getDados() != null) {
					JSONObject json = new JSONObject(pjeDebito.getDados());
					List<Parametro> parametros = MNIParametroUtil.converterParaParametro(json);
					mniDebito.getParametros().addAll(parametros);
				}
				resultado.add(mniDebito);
			}
		}
		return resultado;
	}

	/**
	 * @param tipo
	 * @return Tipo do devedor.
	 */
	protected EnumTipoDevedor obterTipoDevedor(ModalidadeTipoDevedor tipo) {
		Map<ModalidadeTipoDevedor, EnumTipoDevedor> mapa = new HashMap<>();
		mapa.put(ModalidadeTipoDevedor.PRINCIPAL, EnumTipoDevedor.P);
		mapa.put(ModalidadeTipoDevedor.CORRESPONDENTE, EnumTipoDevedor.C);
		mapa.put(ModalidadeTipoDevedor.SOLIDARIO, EnumTipoDevedor.S);

		return mapa.get(tipo);
	}
	
	/**
	 * @param tipo
	 * @return Tipo do devedor.
	 */
	protected ModalidadeTipoDevedor obterTipoDevedor(EnumTipoDevedor tipo) {
		Map<EnumTipoDevedor, ModalidadeTipoDevedor> mapa = new HashMap<>();
		mapa.put(EnumTipoDevedor.P, ModalidadeTipoDevedor.PRINCIPAL);
		mapa.put(EnumTipoDevedor.C, ModalidadeTipoDevedor.CORRESPONDENTE);
		mapa.put(EnumTipoDevedor.S, ModalidadeTipoDevedor.SOLIDARIO);

		return mapa.get(tipo);
	}

	/**
	 * @param tipoValor
	 * @return Tipo do valor do CDA.
	 */
	protected EnumTipoValorCda obterTipoValor(ModalidadeTipoValorCDA tipoValor) {
		Map<ModalidadeTipoValorCDA, EnumTipoValorCda> mapa = new HashMap<>();
		mapa.put(ModalidadeTipoValorCDA.ORIGINARIO, EnumTipoValorCda.O);
		mapa.put(ModalidadeTipoValorCDA.ATUALIZACAO, EnumTipoValorCda.A);

		return mapa.get(tipoValor);
	}
	
	/**
	 * @param tipoValor
	 * @return Tipo do valor do CDA.
	 */
	protected ModalidadeTipoValorCDA obterTipoValor(EnumTipoValorCda tipoValor) {
		Map<EnumTipoValorCda, ModalidadeTipoValorCDA> mapa = new HashMap<>();
		mapa.put(EnumTipoValorCda.O, ModalidadeTipoValorCDA.ORIGINARIO);
		mapa.put(EnumTipoValorCda.A, ModalidadeTipoValorCDA.ATUALIZACAO);

		return mapa.get(tipoValor);
	}
	
	/**
	 * @param mniIdentificacao
	 * @return Código do tipo do documento de identificação.
	 */
	protected String obterCodigoTipoDocumentoIdentificacao(DocumentoIdentificacao mniIdentificacao) {
		ModalidadeDocumentoIdentificadorParaTipoDocumentoIdentificacaoConverter converter = ComponentUtil.getComponent(ModalidadeDocumentoIdentificadorParaTipoDocumentoIdentificacaoConverter.class);
		if( mniIdentificacao.getCodigoDocumento().length() > 11) {
			return converter.converter(mniIdentificacao.getTipoDocumento(), TipoQualificacaoPessoa.JURIDICA);
		}else {
				return converter.converter(mniIdentificacao.getTipoDocumento());
		}
	}
	
	/**
	 * @param mniIdentificacao
	 * @return Código do tipo do documento de identificação.
	 */
	protected ModalidadeDocumentoIdentificador obterCodigoTipoDocumentoIdentificacao(DevedorDocIdentificacao pjeIdentificacao) {
		TipoDocumentoIdentificacao tipo = new TipoDocumentoIdentificacao();
		tipo.setCodTipo(pjeIdentificacao.getCodigoTipo());
		
		TipoDocumentoIdentificacaoParaModalidadeDocumentoIdentificadorConverter converter = new TipoDocumentoIdentificacaoParaModalidadeDocumentoIdentificadorConverter();
		return converter.converter(tipo);
	}
}
