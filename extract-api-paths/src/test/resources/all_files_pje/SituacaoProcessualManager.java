/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.SituacaoProcessualDAO;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SituacaoProcessual;
import br.jus.pje.nucleo.entidades.TipoSituacaoProcessual;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Componente de controle negocial da entidade {@link SituacaoProcessual}
 * 
 * @author cristof
 *
 */
@Name("situacaoProcessualManager")
public class SituacaoProcessualManager extends BaseManager<SituacaoProcessual> {
	
	@In
	private SituacaoProcessualDAO situacaoProcessualDAO;

	@Override
	protected SituacaoProcessualDAO getDAO() {
		return situacaoProcessualDAO;
	}
	
	public SituacaoProcessual criarSituacao(ProcessoTrf processo, TipoSituacaoProcessual tipo){
		SituacaoProcessual s = new SituacaoProcessual();
		s.setAtivo(true);
		s.setDataInicial(new Date());
		s.setInstancia(processo.getJurisdicao().getAplicacao().getAplicacaoClasse());
		s.setProcesso(processo);
		s.setTipoSituacaoProcessual(tipo);
		s.setValida(true);
		return s;
	}
	
    /**
     * Recupera situações ainda não encerradas e válidas no processo dado.
     * 
     * @param processo o processo em relação ao qual se pretende recuperar as situações
     * @return as situações
     */
    public List<SituacaoProcessual> recuperaSituacoesAtuais(ProcessoTrf processo){
    	return recuperaSituacoes(processo, new Date(), true, true);
    }
    
    /**
     * Recupera situações válidas e ativas do processo dado cuja data inicial é anterior à data de referência e cuja data final é nula ou posterior à data de referência.
     * 
     * @param processo o processo em relação ao qual se pretende recuperar as situações
     * @param dataReferencia a data de referência
     * @return as situações
     */
    public List<SituacaoProcessual> recuperaSituacoes(ProcessoTrf processo, Date dataReferencia){
    	return recuperaSituacoes(processo, dataReferencia, true, false);
    }
    
    /**
     * Recupera as situações do processo dado independentemente do período de vigência da situação, incluindo as inválidas a depender da chave de consulta limitarValidas.
     * 
     * @param processo o processo em relação ao qual se pretende recuperar as situações
     * @param limitarValidas marca indicativa de que se pretende recuperar apenas as situções válidas
     * @return as situações
     */
    public List<SituacaoProcessual> recuperaSituacoes(ProcessoTrf processo, boolean limitarValidas){
    	return recuperaSituacoes(processo, null, limitarValidas, false);
    }
    
    /**
     * Recupera todas as situações válidas de um dado processo, independentemente do período de vigência dessas situações.
     * 
     * @param processo o processo em relação ao qual se pretende recuperar as situações
     * @return as situações do processo
     */
    public List<SituacaoProcessual> recuperaSituacoes(ProcessoTrf processo){
    	return recuperaSituacoes(processo, null, true, false);
    }
    
    /**
     * Recupera as situações do processo dado.
     * 
     * @param processo o processo cujas situações se pretende recuperar
     * @param dataReferencia a data de referência para recuperação da informação; se definida, somente as situações vigentes nessa data serão recuperadas
     * @param apenasValidas marca indicativa de que se pretende recuperar apenas as situações válidas
     * @param apenasAtivas marca indicativa de que se pretende recuperar apenas as situações ativas 
     * @return a lista de situações
     */
    private List<SituacaoProcessual> recuperaSituacoes(ProcessoTrf processo,  Date dataReferencia, boolean apenasValidas, boolean apenasAtivas){
    	Search s = new Search(SituacaoProcessual.class);
    	addCriteria(s,
    			Criteria.equals("processo", processo));
    	if(dataReferencia != null){
    		addCriteria(s, 
        			Criteria.or(
        					Criteria.isNull("dataInicial"),
        					Criteria.lessOrEquals("dataInicial", dataReferencia)),
        			Criteria.or(
        					Criteria.isNull("dataFinal"),
        					Criteria.greaterOrEquals("dataFinal", dataReferencia)));
    	}
    	if(apenasValidas){
    		addCriteria(s, Criteria.equals("valida", true));
    	}
    	if(apenasAtivas){
    		addCriteria(s, Criteria.equals("ativo", true));
    	}
    	s.addOrder("o.id", Order.DESC);
    	return list(s);
    }

}
