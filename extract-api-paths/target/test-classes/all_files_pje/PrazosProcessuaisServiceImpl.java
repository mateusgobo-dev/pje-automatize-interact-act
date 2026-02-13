/**
 *
 */
package br.jus.cnj.pje.servicos;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.intercomunicacao.v222.beans.TipoPrazo;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.servicos.prazos.CalculadorPrazoFactory;
import br.jus.cnj.pje.servicos.prazos.Calendario;
import br.jus.cnj.pje.servicos.prazos.Feriado;
import br.jus.cnj.pje.servicos.prazos.GerenciadorCache;
import br.jus.pje.nucleo.entidades.CalendarioEvento;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.enums.CategoriaPrazoEnum;
import br.jus.pje.nucleo.enums.ContagemPrazoEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;
import br.jus.pje.servicos.prazos.ICalculadorPrazo;

/**
 * @author cristof
 *
 */
@Name(value = "prazosProcessuaisService")
public class PrazosProcessuaisServiceImpl implements PrazosProcessuaisService {

	private static final int DIAS_DE_GRACA = 10;

	@In
	private OrgaoJulgadorManager orgaoJulgadorManager;

	/**
	 * Define se esta habilidato o cache para o calculador de prazo
	 * Nos casos em que os calculos de prazos serao calculados em lote o desenvolvedor podera habilitar a 
	 * utilizacao do cache para que o processamento seja mais eficiente. 
	 */
	private boolean habilitadoCacheCalculadorPrazo;

	/**
	 * Armazena o gerenciador de cache dos calculadores de prazo que serao reutilizados quando o cache estiver habilitado.
	 */
	private GerenciadorCache cache = new GerenciadorCache();

	private static final String RECUPERA_FERIADOS_ORGAO = "SELECT "
			+ "	NEW CalendarioEvento(c.dtDia, c.dtMes, c.dtAno, c.dtDiaFinal, c.dtMesFinal, c.dtAnoFinal, c.inJudiciario, c.inFeriado, c.inSuspendePrazo, c.indisponibilidadeSistema ) " 
			+ "	FROM CalendarioEvento c "
			+ "	WHERE c.ativo = true " + "		AND (c.inJudiciario = true OR c.inFeriado = true OR c.inSuspendePrazo = true OR c.indisponibilidadeSistema = true)	"
			+ "		AND (c.inAbrangencia ='N' "
			+ "			OR (c.inAbrangencia = 'E' AND c.estado = :estadoOrgao) "
			+ "			OR (c.inAbrangencia = 'C' AND c.municipio = :municipioOrgao) "
			+ "			OR (c.inAbrangencia = 'O' AND c.orgaoJulgador = :orgao))";

	@SuppressWarnings("unused")
	private static final String RECUPERA_FERIADO_EXATO = "SELECT c FROM CalendarioEvento c "
			+ "WHERE (c.inJudiciario = true OR c.inFeriado = true OR c.inSuspendePrazo = true) "
			+ "	AND (c.inAbrangencia ='N' "
			+ "		OR (c.inAbrangencia = 'E' AND c.estado = :estadoOrgao) "
			+ "		OR (c.inAbrangencia = 'C' AND c.municipio = :municipioOrgao) "
			+ "		OR (c.inAbrangencia = 'O' AND c.orgaoJulgador = :orgao)) " + "	AND c.dtDia = :dia "
			+ "	AND c.dtMes = :mes " + "	AND (c.dtAno = :ano OR c.dtAno IS NULL)";

	@SuppressWarnings("unused")
	private static final String RECUPERA_FERIADOS_INTERVALOS = "SELECT c FROM CalendarioEvento c "
			+ "WHERE (c.inJudiciario = true OR c.inFeriado = true OR c.inSuspendePrazo = true) "
			+ "	AND (c.inAbrangencia ='N' "
			+ "		OR (c.inAbrangencia = 'E' AND c.estado = :estadoOrgao) "
			+ "		OR (c.inAbrangencia = 'M' AND c.municipio = :municipioOrgao) "
			+ "		OR (c.inAbrangencia = 'O' AND c.orgaoJulgador = :orgao)) " + "	AND (c.dtDia";

	@SuppressWarnings("unused")
	private static final String RECUPERA_FERIADOS = "SELECT c FROM CalendarioEvento c "
			+ "WHERE (c.inJudiciario = true OR c.inFeriado = true OR c.inSuspendePrazo = true)"
			+ "	AND  ((c.dtDia BETWEEN :diaInicial AND :diaFinal ) "
			+ "	OR (c.dtDiaFinal IS NOT NULL AND c.dtDiaFinal BETWEEN :diaInicial AND :diaFinal) "
			+ "	and c.dtMes = :mes "
			+ "	and  (c.dtAno = :ano or c.dtAno is null) "
			+ "	and (c.inAbrangencia in ('N','E','M','O')	or c.orgaoJulgador = :orgao))";

	@SuppressWarnings("unused")
	private static final String EH_FERIADO = "SELECT c FROM CalendarioEvento c "
			+ "WHERE (c.inJudiciario = true OR c.inFeriado = true OR c.inSuspendePrazo = true) "
			+ "	AND c.dtDia = :dia "
			+ "	AND c.dtMes = :mes "
			+ "	AND (c.dtAno = :ano OR c.dtAno IS NULL) "
			+ "	AND(c.inAbrangencia = 'N' "
			+ "		OR (c.inAbrangencia = 'E' AND c.estado = :estadoOrgao) OR"
			+ "		OR (c.inAbrangencia = 'M' AND c.municipio = :municipioOrgao) "
			+ "		OR (c.inAbrangencia = 'O' AND c.orgaoJulgador = :orgao))";

	public PrazosProcessuaisServiceImpl() {
	}

	/**
	 * @return Instância da classe.
	 */
	public static PrazosProcessuaisServiceImpl instance() {
		return ComponentUtil.getComponent(PrazosProcessuaisServiceImpl.class);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.servicos.PrazosProcessuaisService#obtemCalendario(br.jus.pje.nucleo.entidades.OrgaoJulgador)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Calendario obtemCalendario(OrgaoJulgador orgaoJulgador) {
		
		Query q = EntityUtil.getEntityManager().createQuery(RECUPERA_FERIADOS_ORGAO);
		
		if (orgaoJulgador.getJurisdicao().getMunicipioList().size() == 0)
			return null;

		Municipio m = orgaoJulgador.getJurisdicao().getMunicipioSede();
		
		if (m == null) {
			m = orgaoJulgador.getJurisdicao().getMunicipioList().get(0).getMunicipio();
		}
		
		q.setParameter("estadoOrgao", m.getEstado());
		q.setParameter("municipioOrgao", m);
		q.setParameter("orgao", orgaoJulgador);
		
		List<CalendarioEvento> eventos = q.getResultList();
		
		return new Calendario(orgaoJulgador, eventos);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.servicos.PrazosProcessuaisService#obtemMapaCalendarios()
	 */
	@Override
	public Map<Integer,Calendario> obtemMapaCalendarios() {
		try{
			Search search = new Search(OrgaoJulgador.class);
			search.addCriteria(Criteria.equals("ativo", true));
			List<OrgaoJulgador> orgaos = orgaoJulgadorManager.list(search);
			Map<Integer,Calendario> ret = new HashMap<Integer,Calendario>(orgaos.size());
			for(OrgaoJulgador o: orgaos){
				ret.put(o.getIdOrgaoJulgador(), obtemCalendario(o));
			}
			return ret;
		}
		catch (Exception e){
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.servicos.PrazosProcessuaisService#obtemDataIntimacaoComunicacaoEletronica(java.util.Date, java.util.List)
	 */
	@Override
	public Date obtemDataIntimacaoComunicacaoEletronica(Date dataDisponibilizacao, Calendario calendario, 
			CategoriaPrazoEnum categoriaPrazo, ContagemPrazoEnum contagemPrazo) {
		Integer prazoDefinido = ParametroUtil.getPrazoParametro(ParametroUtil.getTipoPrazoParametro(Parametros.PJE_TIPO_PRAZO_DE_GRACA, TipoPrazoEnum.D), Parametros.PJE_QTD_PRAZO_DE_GRACA, DIAS_DE_GRACA);
		return obtemDataIntimacaoComunicacaoEletronica(dataDisponibilizacao, calendario, prazoDefinido,
				categoriaPrazo, contagemPrazo);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.servicos.PrazosProcessuaisService#obtemDataIntimacaoComunicacaoEletronica(java.util.Date, java.util.List, int)
	 */
	@Override
	public Date obtemDataIntimacaoComunicacaoEletronica(Date dataDisponibilizacao, Calendario calendario, int diasDeGraca, 
			CategoriaPrazoEnum categoriaPrazo, ContagemPrazoEnum contagemPrazo) {
		return obtemDataFinalPrazo(dataDisponibilizacao, diasDeGraca, TipoPrazoEnum.D, calendario, false, categoriaPrazo, contagemPrazo); 
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.servicos.PrazosProcessuaisService#obtemDiaUtilSeguinte(java.util.Date, java.util.List, boolean)
	 */
	@Override
	public Date obtemDiaUtilSeguinte(Date data, Calendario calendario, boolean incluiDiaInicial) {
		
		Calendar dataFinal = Calendario.converter(data);
		
		if (!incluiDiaInicial) {
			dataFinal.add(Calendar.DAY_OF_YEAR, 1);
		}

		if (calendario.isDiaNaoUtilOuHouveSuspensaoPrazo(dataFinal)) {
			dataFinal = calendario.obtemProximoDiaSemCairEmDiaNaoUtilOuQueHouveSuspensaoPrazo(dataFinal);
						}
		
		return dataFinal.getTime();
					}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.servicos.PrazosProcessuaisService#obtemDiaUtilSeguinte(java.util.Date, br.jus.pje.nucleo.entidades.OrgaoJulgador, boolean)
	 */
	@Override
	public Date obtemDiaUtilSeguinte(Date data, OrgaoJulgador orgaoJulgador, boolean incluiDiaInicial) {
		Calendario calendario = obtemCalendario(orgaoJulgador);
		return obtemDiaUtilSeguinte(data, calendario, incluiDiaInicial);
	}

	/**
	 * Obtém a data ou momento final de um prazo processual dado.
	 *
	 * O prazo pode ser dado em anos, meses, dias, horas e minutos. Quando o
	 * prazo for em anos e meses, não são consideradas suspensões havidas no
	 * interior do prazo dado, ou seja, se os dias inicial e final forem úteis,
	 * o fato de haver uma suspensão no interior do prazo não altera o termo
	 * final. Quando o prazo for dado em dias, as suspensões prorrogam o prazo
	 * em tantos dias quanto forem os dias suspensos do prazo dado. Quando o
	 * prazo for dado em horas ou minutos, a função retornará:
	 * <ul>
	 * <li>a hora e minuto correspondente à soma da hora e minutos dados com o
	 * prazo</li>
	 * <li>a hora e minuto correspondente à soma do prazo com as 06h00 da manhã
	 * do primeiro dia útil seguinte, caso a data de intimação de referência
	 * seja dada como hora e minutos zero</li>
	 * </ul>
	 *
	 * @param dataIntimacao
	 *            A data da concretização da intimação, independentemente de ser
	 *            ou não dia útil.
	 * @param prazo
	 *            O prazo, na unidade indicada em tipoPrazo
	 * @param tipoPrazo
	 *            O tipo de prazo, conforme o enum {@link TipoPrazo}
	 * @param feriados
	 *            A lista de {@link Feriado} que deve ser utilizada para o
	 *            cálculo do prazo.
	 * @param material
	 *            Indicação se a contagem deve ser feita considerando o dia da
	 *            intimação como fazendo parte do prazo
	 * @param categoriaPrazo 
	 * 			  A categoria do prazo, conforme o enum {@link CategoriaPrazoEnum}
	 * @return A data final, sendo marcado o horário final para 23h59m59s quando
	 *         o prazo não for em horas ou minutos.
	 */
	private Date obtemDataFinalPrazo(Date dataIntimacao, int prazo, TipoPrazoEnum tipoPrazo, Calendario calendario, 
			boolean material, CategoriaPrazoEnum categoriaPrazo, ContagemPrazoEnum contagemPrazo) {
		ICalculadorPrazo calculadorPrazo = obtemCalculadorPrazo(categoriaPrazo, calendario);
		return tipoPrazo.calcularPrazo(calculadorPrazo, dataIntimacao, prazo, contagemPrazo);		
	}

	/**
	 * Recupera o calculador de prazo pela lista de feriados quando ela for uma instancia de ListaFeriadoOrgaoJulgador
	 * @param feriados Lista de feriados do orgao julgador
	 * @param categoriaPrazo A categoria de prazo 
	 * @return O calculador de prazo em dias uteis
	 */
	private ICalculadorPrazo obtemCalculadorPrazo(CategoriaPrazoEnum categoriaPrazo, Calendario calendario) {
		ICalculadorPrazo calculador;
		if (isHabilitadoCacheCalculadorPrazo()) {
			calculador = this.cache.obtemCalculadorPrazo(categoriaPrazo, calendario);
		}
		else {
			calculador = CalculadorPrazoFactory.novoCalculadorPrazo(categoriaPrazo, calendario);
		}
		return calculador;
	}

	/*
	 * PJE-JT: Ricardo Scholz : PJEII-1668 PJEII-2009 - 2012-08-06 Alteracoes feitas pela JT.
	 * Modificação da assinatura do método para receber um 'Integer' ao invés do 'int'.
	 * Modificação da estrutura condicional para considerar 'prazo == null', além das
	 * condições anteriormente consideradas.
	 */
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.servicos.PrazosProcessuaisService#calculaPrazoProcessual(java.util.Date, int, br.jus.pje.nucleo.enums.TipoPrazoEnum, java.util.List)
	 */
	@Override
	public Date calculaPrazoProcessual(Date inicio, Integer prazo, TipoPrazoEnum tipoPrazo, Calendario calendario, 
			CategoriaPrazoEnum categoriaPrazo, ContagemPrazoEnum contagemPrazo) {
		if (prazo == null || prazo == 0 || tipoPrazo.equals(TipoPrazoEnum.S)) {
			return null;
		}
		return obtemDataFinalPrazo(inicio, prazo, tipoPrazo, calendario, false, categoriaPrazo, contagemPrazo); 
	}

	@Override
	public Date calculaPrazoProcessualTempestividade(Date inicio, Integer prazo, TipoPrazoEnum tipoPrazo, Calendario calendario, 
			CategoriaPrazoEnum categoriaPrazo, ContagemPrazoEnum contagemPrazo) {
		if (prazo == null || prazo == 0 || tipoPrazo.equals(TipoPrazoEnum.S)) {
			return null;
		}
		calendario = this.obtemCalendario(Authenticator.getOrgaoJulgadorAtual());
		return obtemDataFinalPrazo(inicio, prazo, tipoPrazo, calendario, false, categoriaPrazo, contagemPrazo);
	}
	/*
	 * PJE-JT: Fim.
	 */

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.servicos.PrazosProcessuaisService#ehDiaUtilJudicial(java.util.Date, br.jus.pje.nucleo.entidades.OrgaoJulgador)
	 */
	@Override
	public boolean ehDiaUtilJudicial(Date data, OrgaoJulgador orgao) {
		Calendario calendario = obtemCalendario(orgao);
		Date dt = obtemDiaUtilSeguinte(data, calendario, true);
		return DateUtil.getDataSemHora(dt).equals(DateUtil.getDataSemHora(data));
	}

	public boolean isHabilitadoCacheCalculadorPrazo() {
		return habilitadoCacheCalculadorPrazo;
	}
	
	public void habilitarCacheCalculadorPrazo() {
		this.habilitadoCacheCalculadorPrazo = true;
		this.cache.limpar();			
}
	public void desabilitarCacheCalculadorPrazo() {
		this.habilitadoCacheCalculadorPrazo = false;
		this.cache.limpar();
	}
}