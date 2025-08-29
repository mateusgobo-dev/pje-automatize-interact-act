/**
 * IntercomunicacaoBuilderAbstrato.java
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
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.intercomunicacao.v222.beans.DataHora;
import br.jus.cnj.intercomunicacao.v222.beans.Identificador;
import br.jus.cnj.intercomunicacao.v222.beans.TipoComunicacao;
import br.jus.cnj.pje.intercomunicacao.v222.servico.IntercomunicacaoService;
import br.jus.cnj.pje.intercomunicacao.v222.util.ConversorUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;

/**
 * Classe responsável pela infra-estrutura dos conversores usados na
 * intercomunicação. 
 * O conversor em questão é usado como um builder justificando-se na construção 
 * de objetos complexos.
 * 
 * @author Adriano Pamplona
 */
public abstract class IntercomunicacaoConverterAbstrato<O, D> {
	
	/**
	 * Converte o objeto de origem para o objeto de destino.
	 * 
	 * @param objeto
	 *            Objeto de origem.
	 * @return Objeto de destino.
	 */
	public abstract D converter(O objeto);

	/**
	 * Converte uma coleção de objetos para uma outra coleção de objetos do tipo
	 * de destino.
	 * 
	 * @param colecaoObjeto
	 *            Coleção de objetos de origem.
	 * @return Coleção de objetos de destino.
	 */
	public List<D> converterColecao(List<O> colecaoObjeto) {
		Transformer transformer = novoTransformador();
		return aplicarTransformador(colecaoObjeto, transformer);
	}

	/**
	 * Converte um objeto do tipo Date para DataHora.
	 * 
	 * @param data
	 * @return DataHora
	 * @see ConversorUtil#converterParaDataHora(Date)
	 */
	protected DataHora converterParaDataHora(Date data) {
		
		return ConversorUtil.converterParaDataHora(data);
	}
	
	/**
	 * Retorna um novo transformador de objetos que é usado pela conversão de
	 * coleção.
	 * 
	 * @return novo transformador de objeto.
	 */
	protected Transformer novoTransformador() {
		return new Transformer() {
			@SuppressWarnings("unchecked")
			@Override
			public D transform(Object input) {
				return converter((O) input);
			}
		};
	}
	
	/**
	 * Retorna true se o objeto não tem referência, ou se for um array de objetos que pelo menos um objeto não tenha referência.
	 * 
	 * @param objetos Objeto(s)
	 * @return true se o objeto não tiver referência.
	 */
	protected boolean isNull(Object... objetos) {
		boolean res = true;

		if (objetos != null) {
			res = false;
			for (int idx = 0; idx < objetos.length && (res == false); idx++) {
				res = (objetos[idx] == null || objetos[idx].equals(""));
			} 
		}
		return res;
	}

	/**
	 * Retorna true se o objeto tem referência, ou se for um array de objetos que todos os objetos tenham referência.
	 * 
	 * @param objetos Objeto(s)
	 * @return true se o(s) objeto(s) possuem referência.
	 */
	protected boolean isNotNull(Object... objetos) {
		boolean res = false;
		
		if (objetos != null) {
			res = true;
			for (int idx = 0; idx < objetos.length && (res == true); idx++) {
				res = (objetos[idx] != null);
			}
		}
		return res;
	}
	
	/**
	 * Retorna true se a string for vazia, ou se for um array de strings que pelo menos uma esteja vazia.
	 * 
	 * @param strings String(s)
	 * @return true se a string(s) for vazia.
	 */
	protected boolean isVazio(String... strings) {
		boolean res = true;

		if (strings != null) {
			res = false;
			for (int idx = 0; idx < strings.length && (res == false); idx++) {
				res = (strings[idx] == null || strings[idx].trim().equals(""));
			}
		}
		return res;
	}

	/**
	 * Retorna true se a string não for vazia, ou se for um array de strings que pelo menos uma não esteja vazia.
	 * 
	 * @param strings String(s)
	 * @return true se a string(s) não for vazia.
	 */
	protected boolean isNotVazio(String... strings) {
		return !isVazio(strings);
	}
	
	/**
	 * Retorna true se a coleção for nula ou vazia.
	 * 
	 * @param colecao
	 * @return true se a coleção for nula ou vazia.
	 */
	protected boolean isVazio(Collection<?> colecao) {
		return (isNull(colecao) || colecao.size() == 0);
	}
	
	/**
	 * Retorna true se a coleção não for vazia.
	 * 
	 * @param colecao
	 * @return true se a coleção não for vazia.
	 */
	protected boolean isNotVazio(Collection<?> colecao) {
		return !isVazio(colecao);
	}
	
	/**
	 * @param valor
	 * @return int
	 * @see Integer#parseInt(String)
	 */
	protected int converterParaInt(String valor) {
		int resultado = 0;
		if (StringUtils.isNotBlank(valor)) {
			resultado = Integer.parseInt(valor);
		}
		return resultado;
	}
	
	/**
	 * Converte o objeto passado por parâmetro para string.
	 * 
	 * @param objeto
	 * @return String do objeto passado por parâmetro.
	 */
	protected String converterParaString(Object objeto) {
		String resultado = null;
		if (isNotNull(objeto)) {
			resultado = String.valueOf(objeto);
		}
		return resultado;
	}
	
	/**
	 * @return Identity do usuário logado.
	 */
	protected Identity obterIdentity() {
		return (Identity) Component.getInstance("org.jboss.seam.security.identity");
	}
	
	/**
	 * @return Pessoa do usuário logado.
	 */
	protected Pessoa obterPessoaLogada() {
		return Authenticator.getPessoaLogada();
	}
	
	/**
	 * @return Logalização do usuário logado.
	 */
	protected UsuarioLocalizacao obterUsuarioLocalizacaoAtual() {
		return Authenticator.getUsuarioLocalizacaoAtual();
	}

	/**
	 * @return Usuário logado.
	 */
	protected Usuario obterUsuarioLogado() {
		Usuario usuarioLogado = Authenticator.getUsuarioLogado();
		if (usuarioLogado == null) {
			usuarioLogado = ParametroUtil.instance().getUsuarioSistema();
		}
		return usuarioLogado;
	}

	/**
	 * Retorna o tipo de comunicação do expediente.
	 * 
	 * @param expediente
	 * @return TipoComunicacao.
	 */
	protected TipoComunicacao obterTipoComunicacao(ProcessoParteExpediente expediente) {
		TipoComunicacao resultado = null;

		if (isNotNull(expediente.getProcessoExpediente())
				&& isNotNull(expediente.getProcessoExpediente().getTipoProcessoDocumento())) {

			TipoProcessoDocumento tipoProcessoDocumento = expediente.getProcessoExpediente().getTipoProcessoDocumento();

			ParametroUtil parametro = ParametroUtil.instance();
			Map<Integer, String> mapa = new HashMap<Integer, String>();
			adicionarIdentificadores(parametro.getListaIdTipoProcessoDocumentoIntimacao(), mapa, "INT");
			adicionarIdentificadores(parametro.getListaIdTipoProcessoDocumentoCitacao(), mapa, "CIT");
			adicionarIdentificadores(parametro.getListaIdTipoProcessoDocumentoNotificacao(), mapa, "NOT");
			adicionarIdentificadores(parametro.getListaIdTipoProcessoDocumentoVistaManifestacao(), mapa, "VIS");
			adicionarIdentificadores(parametro.getListaIdTipoProcessoDocumentoUrgente(), mapa, "URG");
			adicionarIdentificadores(parametro.getListaIdTipoProcessoDocumentoPautaAudienciaOuJulgamento(), mapa,
					"PTA");
			mapa.remove(null);

			String tipo = mapa.get(tipoProcessoDocumento.getIdTipoProcessoDocumento());
			resultado = new TipoComunicacao();
			if (StringUtils.isNotBlank(tipo)) {
				resultado.setValue(tipo);
			} else {
				resultado.setValue("INT");
			}
		}

		return resultado;
	}

	/**
	 * Adiciona a lista de ID's do tipo de processo documento ao mapa.
	 * 
	 * @param idsTipoProcessoDocumento
	 * @param mapa
	 * @param tipoMNI
	 */
	private void adicionarIdentificadores(List<Integer> idsTipoProcessoDocumento, 
			Map<Integer, String> mapa, String tipoMNI) {
		
		if (ProjetoUtil.isNotVazio(idsTipoProcessoDocumento)) {
			for (Integer idTipoProcessoDocumento : idsTipoProcessoDocumento) {
				mapa.put(idTipoProcessoDocumento, tipoMNI);
			}
		}
		
	}
	
	/**
	 * Derivação de Map para suportar múltiplos valores para a mesma chave
	 * @author rodrigoar
	 *
	 */
	class MultipleEntryMap<K,E, M> extends HashMap<K, E>{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 7325459392821921242L;
		Map<K, Object[]> entryMap = new HashMap<K, Object[]>();
		
		public void putMultiple(K arg0, Object... arg1) {
			entryMap.put(arg0, arg1);
		}
		
		@SuppressWarnings("unchecked")
		public E get(K key, M entryKey) {
			E entry = super.get(key);
			
			if(entry == null){
				Object[] items = entryMap.get(key);
				
				for(int i = 0; i < items.length; i++){
					Object item = items[i];
					if(item.equals(entryKey)){
						entry = (E) items[i + 1];
					}
				}
			}
			
			return entry;
		}
	}

	/**
	 * Converte uma lista de objetos do tipo 'O' para objetos do tipo 'D'.
	 * 
	 * @param colecaoObjeto Lista de objetos do tipo 'O'.
	 * @param transformer Transformador.
	 * @return Lista de objetos do tipo 'D'.
	 */
	@SuppressWarnings("unchecked")
	protected List<D> aplicarTransformador(List<O> colecaoObjeto, Transformer transformer) {
		List<D> resultado = new ArrayList<D>();
		
		if (isNotVazio(colecaoObjeto) && isNotNull(transformer)) {
			for (Iterator<O> iterator = colecaoObjeto.iterator(); iterator.hasNext(); ) {
				O objeto = iterator.next();
				D objetoConvertido = (D) transformer.transform(objeto);
				
				if (isNotNull(objetoConvertido)) resultado.add(objetoConvertido);
			}
		}
		return resultado;
	}
	
	/**
	 * Retorna novo objeto Identificador
	 * @param id
	 * @return Identificador
	 */
	protected Identificador novoIdentificador(Number id) {
		Identificador resultado = null;
		
		if (isNotNull(id)) {
			resultado = new Identificador();
			resultado.setValue(String.valueOf(id));
		}
		return resultado;
	}
	
	/**
	 * @return IntercomunicacaoService
	 */
	protected IntercomunicacaoService getIntercomunicacaoService() {
		return ComponentUtil.getComponent(IntercomunicacaoService.class);
	}
	
	/**
	 * @return True se o serviço chamado foi o "Consultar avisos pendentes".
	 */
	public Boolean isServicoConsultarAvisosPendentes() {
		return getIntercomunicacaoService().isServicoConsultarAvisosPendentes();
	}
	
	/**
	 * @return True se o serviço chamado foi o "Consultar processo".
	 */
	public Boolean isServicoConsultarProcesso() {
		return getIntercomunicacaoService().isServicoConsultarProcesso();
	}
	
	/**
	 * @return True se o serviço chamado foi o "Consultar teor de comunicação".
	 */
	public Boolean isServicoConsultarTeorComunicacao() {
		return getIntercomunicacaoService().isServicoConsultarTeorComunicacao();
	}
	
	/**
	 * @return True se o serviço chamado foi o "Entregar manifestação processual".
	 */
	public Boolean isServicoEntregarManifestacaoProcessual() {
		return getIntercomunicacaoService().isServicoEntregarManifestacaoProcessual();
	}
	
	/**
	 * Método responsável por verificar se o horário atual permite atendimento em plantão judiciário
	 * @return true se o horário atual permite atendimento em plantão judiciário
	 */
	public Boolean isHorarioAtendimentoPlantao() {
		return getIntercomunicacaoService().isAtendimentoPlantaoPermitido();
	}

	public Boolean isNumeroValidoPreenchido(String valor){
		Boolean resultado = Boolean.TRUE;
		try{
			Integer.parseInt(valor);
		}catch (Exception e) {
			resultado = Boolean.FALSE;
		}
		
		return resultado;
	}

}
