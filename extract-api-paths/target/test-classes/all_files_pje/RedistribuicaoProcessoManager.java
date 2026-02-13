package br.jus.cnj.pje.nucleo.manager;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.ProcessoJudicialDAO;
import br.jus.cnj.pje.business.dao.RedistribuicaoProcessoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.je.entidades.Eleicao;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.search.Search;

/**
 * @author Éverton Nogueira Pereira
 *
 */
@Name(RedistribuicaoProcessoManager.NAME)
public class RedistribuicaoProcessoManager implements Serializable{
	public static final String NAME = "redistribuicaoProcessoManager";
	private static final long serialVersionUID = 1L;
	
	@In
	private RedistribuicaoProcessoDAO redistribuicaoProcessoDAO;
	
	@In
	private ProcessoJudicialDAO processoJudicialDAO;
	
	@In
	private ProcessoJudicialManager processoJudicialManager;
	
	private Search search;

	public List<ProcessoTrf> obtemListaComRestricoesBasicas() {
		return redistribuicaoProcessoDAO.obtemListaComRestricoesBasicas();
	}

	public List<ProcessoTrf> consultarProcessos(Integer numeroSequencia, Integer digitoVerificador, Integer ano,
			Integer numeroOrigem, String ramoJustica, String respectivoTribunal, String nomeParte,
			String documentoParte, Estado estadoOAB, String numeroOAB, String letraOAB, OrgaoJulgador orgaoJulgador,
			OrgaoJulgadorColegiado orgaoColegiado, Date dataAutuacaoInicio, Date dataAutuacaoFim, Eleicao eleicao,
			Estado estado, Municipio municipio, String numeroDocumento, Double valorCausaInicial,
			Double valorCausaFinal, String numeroProcessoReferencia, String objetoProcesso, Jurisdicao jurisdicao,
			Evento movimentacaoProcessual) throws NoSuchFieldException, PJeBusinessException {
		
		search = new Search(ProcessoTrf.class);
		search.addCriteria(processoJudicialManager.getCriteriosConsultarProcessos(null, numeroSequencia, 
				digitoVerificador, ano, numeroOrigem, ramoJustica, respectivoTribunal, nomeParte, 
				documentoParte, estadoOAB, numeroOAB, letraOAB, null, null, null, null, null, orgaoJulgador, 
				orgaoColegiado, dataAutuacaoInicio, dataAutuacaoFim, eleicao, estado, municipio, null, 
				valorCausaInicial, valorCausaFinal, numeroProcessoReferencia, objetoProcesso, jurisdicao, 
				movimentacaoProcessual, null,null,null,null));
		
		return processoJudicialDAO.consultarProcessos(search);
	}
	
}