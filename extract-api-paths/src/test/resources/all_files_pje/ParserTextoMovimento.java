package br.jus.csjt.pje.business.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.jus.pje.nucleo.entidades.lancadormovimento.AplicacaoComplemento;
import br.jus.pje.nucleo.entidades.lancadormovimento.AplicacaoMovimento;

/**
 * Classe responsável por transformar o texto parametrizado do movimento em texto final externo e interno, 
 * através do preenchimento dos complementos.  
 * 
 * @author David Vieira
 *
 */
public class ParserTextoMovimento{

	private static final String COMPLEMENTO_NAO_PREENCHIDO_REPLACE = "#Não preenchido#";
	private static final String COMPLEMENTO_OCULTO_REPLACE = "#Oculto#";
	private final AplicacaoMovimento aplicacaoMovimento;
	private List<PlaceHolder> placeHolders = new ArrayList<PlaceHolder>();

	private static final Pattern patternPlaceHolders = Pattern.compile("("
		+ LancadorMovimentosService.SEPARADOR_INICIO.replace("{", "\\{")
		+ "(.*?)"
		+ LancadorMovimentosService.SEPARADOR_FIM.replace("}", "\\}")
		+ ")+"); // regex = (#\\{(.*?)\\})+

	public ParserTextoMovimento(AplicacaoMovimento aplicacaoMovimento){
		this.aplicacaoMovimento = aplicacaoMovimento;
		// percorrer textoParametrizado em busca de PlaceHolders
		Matcher matcher = patternPlaceHolders.matcher(this.aplicacaoMovimento.getTextoParametrizado());
		while (matcher.find()){

			String nomeComplemento = matcher.group(2); // nome do complemento
			AplicacaoComplemento aplicacaoComplemento = getAplicacaoComplemento(nomeComplemento);
			if (aplicacaoComplemento != null){
				placeHolders.add(new PlaceHolder(matcher.start(), matcher.end(), aplicacaoComplemento));
			}
		}
	}

	private AplicacaoComplemento getAplicacaoComplemento(String nomeComplemento){
		for (AplicacaoComplemento aplicacaoComplemento : this.aplicacaoMovimento.getAplicacaoComplementoList()){
			if (aplicacaoComplemento.getTipoComplemento().getNome().trim().equalsIgnoreCase(nomeComplemento.trim())){
				return aplicacaoComplemento;
			}
		}
		return null;
	}

	public AplicacaoComplemento preencherProximoComplementoVazio(String texto){
		for (PlaceHolder placeHolder : placeHolders){
			if (placeHolder.isVazio()) {
				placeHolder.textoPreenchimento = texto;
				return placeHolder.aplicacaoComplemento;
			}
		}
		return null;
	}

	public AplicacaoComplemento preencherProximoComplementoDeNome(String nomeDoComplemento, String texto){
		for (PlaceHolder placeHolder : placeHolders){
			if (placeHolder.isVazio() && placeHolder.aplicacaoComplemento.getTipoComplemento().getNome().trim().equalsIgnoreCase(nomeDoComplemento.trim())) {
				placeHolder.textoPreenchimento = texto;
				return placeHolder.aplicacaoComplemento;
			}
		}
		return null;
	}

	public AplicacaoComplemento preencherProximoComplementoDeCodigo(String codigoDoComplemento, String texto){
		for (PlaceHolder placeHolder : placeHolders){
			if (placeHolder.isVazio() && placeHolder.aplicacaoComplemento.getTipoComplemento().getCodigo().trim().equalsIgnoreCase(codigoDoComplemento.trim())) {
				placeHolder.textoPreenchimento = texto;
				return placeHolder.aplicacaoComplemento;
			}
		}
		return null;
	}

	public String getTextoFinalInterno(){
		return getTextoFinal(false);
	}

	public String getTextoFinalExterno(){
		return getTextoFinal(true);
	}
	
	private String getTextoFinal(boolean externo) {
		// preencher da direita para esquerda, para nao perder os start e ends dos placeHolders
		List<PlaceHolder> listaReversaPlaceholders = new ArrayList(placeHolders);
		Collections.reverse(listaReversaPlaceholders);
		StringBuffer retorno = new StringBuffer(this.aplicacaoMovimento.getTextoParametrizado());
		for (PlaceHolder placeHolder : listaReversaPlaceholders){
			if (externo && !placeHolder.aplicacaoComplemento.getVisibilidadeExterna()) {
				retorno.replace(placeHolder.start, placeHolder.end, COMPLEMENTO_OCULTO_REPLACE);
			} else {
				if (placeHolder.isVazio()) {
					retorno.replace(placeHolder.start, placeHolder.end, COMPLEMENTO_NAO_PREENCHIDO_REPLACE);
				} else {
					retorno.replace(placeHolder.start, placeHolder.end, placeHolder.textoPreenchimento);
				}
			}
		}
		return retorno.toString();
	}

}

class PlaceHolder{

	protected final int start;
	protected final int end;
	protected final AplicacaoComplemento aplicacaoComplemento;
	protected String textoPreenchimento = "";

	public PlaceHolder(int start, int end, AplicacaoComplemento aplicacaoComplemento){
		this.start = start;
		this.end = end;
		this.aplicacaoComplemento = aplicacaoComplemento;
	}
	
	public boolean isVazio() {
		return textoPreenchimento.equals("");
	}

}
