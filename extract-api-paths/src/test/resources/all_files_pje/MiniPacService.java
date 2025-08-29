package br.jus.cnj.pje.nucleo.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ProjetoUtil;
import br.jus.cnj.pje.entidades.vo.MiniPacVO;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.view.AtividadesLoteAction;
import br.jus.cnj.pje.view.fluxo.MiniPacAction;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Classe service para processar os dados do frame miniPAC
 * 
 * Ver {@link MiniPacAction} e {@link AtividadesLoteAction}
 *
 */
@Name("miniPacService")
public class MiniPacService extends BaseService {
    
    @In
    private TramitacaoProcessualService tramitacaoProcessualService;
    
    @In
    private AtoComunicacaoService atoComunicacaoService;    
    
    /**
     * Cria o(s) ato(s) de comunicação ({@link ProcessoExpediente}) indicado(s) nos objetos gravados em variável de contexto do processo ({@link ProcessoTrf}) informado.
     * O que ocorre:
     * As variáveis de contexto {@link Variaveis#PJE_FLUXO_MINI_PAC_LIST_VO} e {@link Variaveis#PJE_FLUXO_MINI_PAC_DOCS_VINCULADOS} são lidas;
     * A criação dos expedientes é realizada com base nas informações contidas nas variáveis. 
     * Ver {@link AtoComunicacaoService#criarAtosComunicacao(ProcessoTrf, ProcessoDocumento, Map, List)}
     * 
     * Ao final, as variáveis utilizadas pelo frame miniPAC são removidas do contexto.
     * @param processo
     * @param pd
     */
    public void processarMiniPac(ProcessoTrf processo, ProcessoDocumento pd, Boolean isProcDocExistente){
    	processarMiniPac(processo, pd, isProcDocExistente, false);
    }

    /**
     * Cria o(s) ato(s) de comunicação ({@link ProcessoExpediente}) indicado(s) nos objetos gravados em variável de contexto do processo ({@link ProcessoTrf}) informado.
     * O que ocorre:
     * As variáveis de contexto {@link Variaveis#PJE_FLUXO_MINI_PAC_LIST_VO} e {@link Variaveis#PJE_FLUXO_MINI_PAC_DOCS_VINCULADOS} são lidas;
     * A criação dos expedientes é realizada com base nas informações contidas nas variáveis. 
     * Ver {@link AtoComunicacaoService#criarAtosComunicacao(ProcessoTrf, ProcessoDocumento, Map, List)}
     * 
     * Ao final, as variáveis utilizadas pelo frame miniPAC são removidas do contexto.
     * @param processo
     * @param pd
	 * @param isReenvioCitacaoExpirada -> indica se é reenvio de citação expirada (citação enviada ao Domicílio sem ciência). Este reenvio é feito automaticamente através do fluxo TCI e não é enviado ao Domicílio Eletrônico. 
     */
    @SuppressWarnings("unchecked")
	public void processarMiniPac(ProcessoTrf processo, ProcessoDocumento pd, Boolean isProcDocExistente,
			boolean isReenvioCitacaoExpirada) {
        List<MiniPacVO> destinatariosList = (List<MiniPacVO>) tramitacaoProcessualService.recuperaVariavel(Variaveis.PJE_FLUXO_MINI_PAC_LIST_VO);                 
        if (ProjetoUtil.isNotVazio(destinatariosList)){
            List<ProcessoDocumento> documentosVinculadosList = (List<ProcessoDocumento>) tramitacaoProcessualService.recuperaVariavel(Variaveis.PJE_FLUXO_MINI_PAC_DOCS_VINCULADOS);            
            Map<ExpedicaoExpedienteEnum, List<MiniPacVO>> miniPacVOMap = recuperarMiniPacVOMap(destinatariosList);
            
            Collection<ProcessoExpediente> expedientesIds = atoComunicacaoService.criarAtosComunicacao(processo, pd, miniPacVOMap, documentosVinculadosList, isProcDocExistente, null, isReenvioCitacaoExpirada);
            tramitacaoProcessualService.gravaVariavel(Variaveis.VARIAVEL_FLUXO_PETICAO_INCIDENTAL, pd.getIdProcessoDocumento());                
            tramitacaoProcessualService.gravaVariavel(Variaveis.VARIAVEL_EXPEDIENTE, StringUtil.concatList(CollectionUtils.collect(expedientesIds, new BeanToPropertyValueTransformer("idProcessoExpediente")), ","));
        }
        apagarVariaveisMiniPacContexto();        
    }

    /**
     * Cria uma objeto destinatário do frame miniPAC ({@link MiniPacVO} a partir da parte ({@link ProcessoParte} indicada.
     * 
     * @param processoParte parte a ser utilizada para geração do {@link MiniPacVO}
     * @param intimacaoPessoal se o ato de comunicação para essa parte é pessoal ou não. Se for pessoal, o endereço a ser utilizado será o da parte e não do seu representante. 
     * @param prazo prazo, em dias, a ser utilizado no(s) ato(s) de comunicação da parte indicada.
     * @return {@link MiniPacVO} baseado nos dados passados por parâmetro
     */
    public MiniPacVO carregaMiniPacVO(ProcessoParte processoParte, Boolean intimacaoPessoal, String prazo) {
		MiniPacVO miniPacVO = new MiniPacVO(processoParte);
		miniPacVO.setPessoal(intimacaoPessoal);
        
        Endereco endereco = atoComunicacaoService.getMelhorEnderecoParaComunicacao(processoParte, miniPacVO.isPessoal());

		if (endereco != null && !miniPacVO.getEnderecos().contains(endereco)) {
			miniPacVO.getEnderecos().clear();
			miniPacVO.getEnderecos().add(endereco);
		}

		miniPacVO.setAtivo(Boolean.TRUE);
		miniPacVO.setPrazo(Integer.parseInt(prazo));
		miniPacVO.setMeios(new TreeSet<>());

		return miniPacVO;
    }

    /**
     * Apaga do contexto as variáveis utilizadas pelo frame miniPAC
     */
    public void apagarVariaveisMiniPacContexto() {
        tramitacaoProcessualService.apagaVariavel(Variaveis.PJE_FLUXO_MINI_PAC_LIST_VO);
        tramitacaoProcessualService.apagaVariavel(Variaveis.PJE_FLUXO_MINI_PAC_DOCS_VINCULADOS);
        tramitacaoProcessualService.apagaVariavel(Variaveis.PJE_FLUXO_MINI_PAC_DOC_PRINCIPAL);
        tramitacaoProcessualService.apagaVariavel(Variaveis.PJE_FLUXO_MINI_PAC_DOCUMENTO_EXISTE);
        tramitacaoProcessualService.apagaVariavel(Variaveis.PJE_FLUXO_MINI_PAC_PRAZO_GERAL);
    }
    
    /**
     * Método auxiliar para transformar uma lista de objetos destinatários {@link MiniPacVO} em um
     * mapa com os dados agrupados por meio de comunicação {@link ExpedicaoExpedienteEnum}.
     * Esse mapa será utilizado na criação efetiva dos expedientes ({@link AtoComunicacaoService#criarAtosComunicacao(ProcessoTrf, ProcessoDocumento, Map, List)}.
     * 
     * @param miniPacVOList lista de destinatários 
     * @return mapa de destinatários agrupados por meio de comunicação
     */
    @SuppressWarnings("unchecked")
    private Map<ExpedicaoExpedienteEnum, List<MiniPacVO>> recuperarMiniPacVOMap(List<MiniPacVO> miniPacVOList) {
        Map<ExpedicaoExpedienteEnum, List<MiniPacVO>> miniPacVOMap = new HashMap<ExpedicaoExpedienteEnum, List<MiniPacVO>>(miniPacVOList.size());

        Set<ExpedicaoExpedienteEnum> meiosIndividuaisSet = new HashSet<ExpedicaoExpedienteEnum>(ExpedicaoExpedienteEnum.values().length);
	    for (MiniPacVO miniPacVO : miniPacVOList) {
	        meiosIndividuaisSet.addAll(miniPacVO.getMeios());
	    }	    
        List<MiniPacVO> listaInterna;
        for (final ExpedicaoExpedienteEnum meio : meiosIndividuaisSet) {
            listaInterna = (List<MiniPacVO>) CollectionUtilsPje.select(miniPacVOList, getPredicateMeio((ExpedicaoExpedienteEnum) meio));
            miniPacVOMap.put(meio, listaInterna);
        }
        return miniPacVOMap;
    }

    private Predicate getPredicateMeio(final ExpedicaoExpedienteEnum meio) {
        return new Predicate() {
            @Override
            public boolean evaluate(Object objeto) {
                return ((MiniPacVO) objeto).getMeios().contains(meio);
            }
        };
    }

}
