/**
 * 
 */
package br.jus.cnj.pje.servicos;

import java.util.Date;
import java.util.Map;

import br.jus.cnj.pje.servicos.prazos.Calendario;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.enums.CategoriaPrazoEnum;
import br.jus.pje.nucleo.enums.ContagemPrazoEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;

/**
 * Interface destinada a enumerar as funções de serviço de contagem de prazos
 * processuais. O serviço deverá obter do sistema as seguintes informações:
 * 
 * @author cristof
 * 
 */
public interface PrazosProcessuaisService {

	/**
	 * Identifica se um dado dia é considerado útil para o órgão julgador paradigma.
	 * 
	 * @param data a data paradigma a ser avaliada.
	 * @param orgao o órgão julgador a respeito do qual se pretende pesquisar
	 * @return true, se o dia for útil.
	 * 
	 * @author desconhecido
	 */
	public boolean ehDiaUtilJudicial(Date data, OrgaoJulgador orgao);

	/**
	 * Recupera o calendario do órgão julgador dado com uma lista de eventos, ou seja, recupera os eventos que 
	 * pertencem ao próprio órgão, ao seu município sede (municipais), à unidade federativa em que
	 * está o município (estaduais ou distritais) e a todo o país (nacionais).
	 * 
	 * @param orgao o órgão em relação ao qual se pretende os eventos do calendario.
	 * @return o calendario com uma lista dos eventos afetantes
	 */
	public Calendario obtemCalendario(OrgaoJulgador orgao);

	/**
	 * Recupera um mapa de calendario dos órgãos julgadores ativos do sistema.
	 * 
	 * @return Um mapa de calendarios utilizando o identificador do orgao julgador como chave para recuperar o seu respectivo calendario.
	 */
	public Map<Integer,Calendario> obtemMapaCalendarios();

	/**
	 * Função destinada a identificar a data de intimação ficta decorrente de
	 * intimação eletrônica. Seu cálculo é baseado nas regras da Lei n.º
	 * 11.419/2006, art. 5.º, com os seguintes esclarecimentos:
	 * <ul>
	 * <li>a data da disponibilização NÃO é incluída na contagem do prazo de
	 * graça de 10 dias</li>
	 * <li>a data de início de contagem do prazo é o dia seguinte ao da
	 * dispobilização, independentemente de esse dia ser ou não dia útil ou dia
	 * em que houve suspensão da contagem de prazos</li>
	 * <li>se o 10º dia da contagem a partir da data da disponibilização for dia
	 * não útil, a data considerada como data de intimação será o primeiro dia
	 * útil subseqüente</li>
	 * <ul>
	 * 
	 * @param dataDisponibilizacao
	 *            Data em que houve a disponibilizaçã do ato de comunicação
	 * @param calendario
	 *            O calendario com a lista de eventos aplicáveis ao órgão vinculado ao processo
	 *            judicial em que houve a prática do ato de comunicação.
	 * @param categoriaPrazo Identifica a categoria do prazo conforme: {@link CategoriaPrazoEnum}
	 * @return Data da intimação considerada.
	 * 
	 */
	public Date obtemDataIntimacaoComunicacaoEletronica(Date dataDisponibilizacao, Calendario calendario, 
			CategoriaPrazoEnum categoriaPrazo, ContagemPrazoEnum contagemPrazo);

	/**
	 * Função destinada a identificar a data de intimação ficta decorrente de
	 * intimação eletrônica. Seu cálculo é baseado nas regras da Lei n.º
	 * 11.419/2006, art. 5.º, com os seguintes esclarecimentos:
	 * <ul>
	 * <li>a data da disponibilização NÃO é incluída na contagem do prazo de
	 * graça</li>
	 * <li>o prazo de graça deve ser definido por quem chama a função</li>
	 * <li>a data de início de contagem do prazo é o dia seguinte ao da
	 * dispobilização, independentemente de esse dia ser ou não dia útil ou dia
	 * em que houve suspensão da contagem de prazos</li>
	 * <li>se o último dia da contagem a partir da data da disponibilização for dia
	 * não útil, a data considerada como data de intimação será o primeiro dia
	 * útil subsequente</li>
	 * <ul>
	 * 
	 * @param dataDisponibilizacao
	 *            Data em que houve a disponibilizaçã do ato de comunicação
	 * @param calendario
	 *            O calendario com a lista de eventos aplicáveis ao órgão vinculado ao processo
	 *            judicial em que houve a prática do ato de comunicação.
	 * @param diasDeGraca
	 *            Número de dias de graça.
	 * @param categoriaPrazo Identifica a categoria do prazo conforme: {@link CategoriaPrazoEnum}
	 * @return Data da intimação considerada.
	 * 
	 */
	public Date obtemDataIntimacaoComunicacaoEletronica(Date dataDisponibilizacao, Calendario calendario, int diasDeGraca, 
			CategoriaPrazoEnum categoriaPrazo, ContagemPrazoEnum contagemPrazo);

	/*
	 * PJE-JT: Ricardo Scholz : PJEII-1668 PJEII-2009 - 2012-08-06 Alteracoes feitas pela JT.
	 * Modificação da assinatura do método para receber um 'Integer' ao invés do 'int'.
	 */
	/**
	 * Recupera a data final de um prazo processual dado, a partir de uma data.
	 * 
	 * @param inicio a data inicial do prazo
	 * @param prazo o número de unidades de tempo do tipoPrazo que serão contadas.
	 * @param tipoPrazo o tipo de prazo a ser contabilizado (anos, meses, dias, horas, minutos)
	 * @param calendario o calendario com a lista de eventos que podem, potencialmente, afetar a contagem.
	 * @param categoriaPrazo A categoria do prazo processual conforme: {@link CategoriaPrazoEnum}	
	 * @return a data final do prazo, contato a partir do início
	 */
	public Date calculaPrazoProcessual(Date inicio, Integer prazo, TipoPrazoEnum tipoPrazo, Calendario calendario, 
			CategoriaPrazoEnum categoriaPrazo, ContagemPrazoEnum contagemPrazo);
	/*
	 * PJE-JT: Fim.
	 */

	/**
	 * Função destinada a identificar o dia útil seguinte ao apresentado,
	 * podendo ser o próprio dia apresentado caso ele seja útil e seja atribuído
	 * o valor verdadeiro (true) ao parâmetro incluiDiaInicial.
	 * 
	 * @param data
	 *            Data paradigma a partir da qual será buscado o dia útil
	 * @param calendario
	 *            O calendario do orgao julgador com a lista de eventos aplicáveis ao órgão vinculado ao processo
	 *            judicial em que se procura buscar o dia útil.
	 * @param incluiDiaInicial
	 *            Indicativo se a função deverá retornar a própria data dada
	 *            caso seja um dia útil
	 * @return Data útil seguinte à data dada, ou a própria data, se ela for
	 *         útil e o parâmetro incluiDiaInicial for true
	 */
	public Date obtemDiaUtilSeguinte(Date data, Calendario calendario, boolean incluiDiaInicial);
	
	/**
	 * Função destinada a identificar o dia útil seguinte ao apresentado em um dado órgão,
	 * podendo ser o próprio dia apresentado caso ele seja útil e seja atribuído
	 * o valor verdadeiro (true) ao parâmetro incluiDiaInicial.
	 * 
	 * @param data
	 *            Data paradigma a partir da qual será buscado o dia útil
	 * @param orgao
	 *            O órgão julgador em relação ao qual se pretende identificar o dia útil seguinte.
	 * @param incluiDiaInicial
	 *            Indicativo se a função deverá retornar a própria data dada
	 *            caso seja um dia útil
	 * @return Data útil seguinte à data dada, ou a própria data, se ela for
	 *         útil e o parâmetro incluiDiaInicial for true
	 */
	public Date obtemDiaUtilSeguinte(Date data, OrgaoJulgador orgao, boolean incluiDiaInicial);

	/**
	 * Recupera a data final de um prazo processual dado, a partir de uma data.
	 * O cálculo será feito utilizando os feriados do órgão julgador do usuário autenticado.
	 * @param inicio A data de inicio
	 * @param prazo O prazo
	 * @param tipoPrazo O tipo de prazo conforme: {@link TipoPrazoEnum}
	 * @param calendario O calendario com a lista de eventos que podem, potencialmente, afetar a contagem.
	 * @param categoriaPrazo A categoria do prazo processual conforme: {@link CategoriaPrazoEnum}
	 * @return
	 */
	public Date calculaPrazoProcessualTempestividade(Date inicio, Integer prazo, TipoPrazoEnum tipoPrazo, Calendario calendario, 
			CategoriaPrazoEnum categoriaPrazo, ContagemPrazoEnum contagemPrazo);

	/**
	 * Habilita o cache para o calculador de prazo
	 * Nos casos em que os calculos de prazos serao calculados em lote o desenvolvedor podera habilitar a 
	 * utilizacao do cache para que o processamento seja mais eficiente. 
	 */
	public void habilitarCacheCalculadorPrazo();

	/**
	 * Apos o processamento em lote o desenvolvedor devera desabilitar o cache
	 */
	public void desabilitarCacheCalculadorPrazo();
}
