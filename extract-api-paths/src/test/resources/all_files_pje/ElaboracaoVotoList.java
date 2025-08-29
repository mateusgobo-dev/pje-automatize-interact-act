package br.com.jt.pje.list;

import java.util.Map;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.SearchCriteria;
import br.com.infox.DAO.SearchField;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.utils.ItensLegendas;
import br.jus.pje.jt.entidades.PautaSessao;
import br.jus.pje.jt.enums.ClassificacaoTipoSituacaoPautaEnum;


@Name(ElaboracaoVotoList.NAME)
@BypassInterceptors
public class ElaboracaoVotoList extends FiltrosPautaVotacaoAntecipadaList<PautaSessao> {
    private static final long serialVersionUID = 1L;
    public static final String NAME = "elaboracaoVotoList";
    protected static final String DEFAULT_EJBQL = FiltrosPautaVotacaoAntecipadaList.DEFAULT_EJBQL;
    protected static final String DEFAULT_ORDER = "o.dataPedidoSustentacaoOral, o.processoTrf.ano, o.processoTrf.numeroSequencia";
    protected static final String R15 = "o.sessao = #{votoAction.sessao} ";
    protected static final String R16 = "o.sessao = #{secretarioSessaoJulgamentoAction.sessao} ";
    protected static final String R17 = "o.sessao = #{magistradoSessaoJulgamentoAction.sessao} ";
    protected static final String R18 = "o.sessao = #{procuradorSessaoJulgamentoAction.sessao} ";
    protected static final String R19 = "exists (select v from Voto v where " +
        "v.processoTrf = o.processoTrf and " + "v.sessao = o.sessao and " +
        "v.orgaoJulgador = #{orgaoJulgadorAtual} and " +
        "v.marcacaoDivergencia = true)";
    protected static final String R19S = "exists (select v from Voto v where " +
        "v.processoTrf = o.processoTrf and " + "v.sessao = o.sessao and " +
        "v.marcacaoDivergencia = #{true})";
    protected static final String R20 = "exists (select v from Voto v where " +
        "v.processoTrf = o.processoTrf and " + "v.sessao = o.sessao and " +
        "v.orgaoJulgador = #{orgaoJulgadorAtual} and " +
        "v.marcacaoDestaque = true)";
    protected static final String R20S = "exists (select v from Voto v where " +
        "v.processoTrf = o.processoTrf and " + "v.sessao = o.sessao and " +
        "v.marcacaoDestaque = #{true})";
    protected static final String R21 = "exists (select v from Voto v where " +
        "v.processoTrf = o.processoTrf and " + "v.sessao = o.sessao and " +
        "v.orgaoJulgador = #{orgaoJulgadorAtual} and " +
        "v.marcacaoObservacao = true)";
    protected static final String R21S = "exists (select v from Voto v where " +
        "v.processoTrf = o.processoTrf and " + "v.sessao = o.sessao and " +
        "v.marcacaoObservacao = #{true})";
    protected static final String R22 = "o.preferencia = #{true}";
    protected static final String R23 = "o.sustentacaoOral = #{true}";
    protected static final String R24 = "o.tipoSituacaoPauta.classificacao = #{elaboracaoVotoList.tipoClassificacao('J')}";
    protected static final String R25 = "o.tipoSituacaoPauta.classificacao = #{elaboracaoVotoList.tipoClassificacao('D')}";
    protected static final String R26 = "o.tipoSituacaoPauta.classificacao = #{elaboracaoVotoList.tipoClassificacao('R')}";
    protected static final String R27 = "o.tipoSituacaoPauta.classificacao = #{elaboracaoVotoList.tipoClassificacao('P')}";
    protected static final String R28 = "exists (select pd from ProcessoDocumento pd where " +
        "pd.processo.idProcesso = o.processoTrf.idProcessoTrf and " +
        "pd.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoAcordao} " +
        "and pd.ativo = true)";
    protected static final String R29 = "not exists (select pd from ProcessoDocumento pd where " +
        "pd.processo.idProcesso = o.processoTrf.idProcessoTrf and " +
        "pd.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoAcordao} " +
        "and pd.ativo = true)";
    protected static final String R30 = "exists (select pd from ProcessoDocumento pd where " +
        "pd.processo.idProcesso = o.processoTrf.idProcessoTrf and " +
        "pd.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoAcordao} " +
        "and pd.ativo = true" +
        "and 1 = (select count(pdbpa) from ProcessoDocumentoBinPessoaAssinatura pdbpa " +
        "			where pdbpa.processoDocumentoBin.idProcessoDocumentoBin = pd.processoDocumentoBin.idProcessoDocumentoBin))";
    protected static final String R31 = "o.tipoSituacaoPauta.classificacao = 'J' and " +
        "exists (select mp from ProcessoEvento mp where mp.ativo=true and " +
        "mp.dataAtualizacao >= o.sessao.dataSituacaoSessao and " +
        "mp.processo.idProcesso = o.processoTrf.idProcessoTrf and " +
        "exists (select ea.evento.caminhoCompleto from Agrupamento a " +
        "inner join a.eventoAgrupamentoList ea where " +
        "a.idAgrupamento = #{parametroUtil.agrupamentoJulgamento} and " +
        "mp.evento.caminhoCompleto like ea.evento.caminhoCompleto || '%')) ";
    protected static final String R32 = "o.tipoSituacaoPauta.classificacao = 'J' and " +
        "not exists (select mp from ProcessoEvento mp where mp.ativo=true and " +
        "mp.dataAtualizacao >= o.sessao.dataSituacaoSessao and " +
        "mp.processo.idProcesso = o.processoTrf.idProcessoTrf and " +
        "exists (select ea.evento.caminhoCompleto from Agrupamento a " +
        "inner join a.eventoAgrupamentoList ea where " +
        "a.idAgrupamento = #{parametroUtil.agrupamentoJulgamento} and " +
        "mp.evento.caminhoCompleto like ea.evento.caminhoCompleto || '%')) ";
    
    /**
     * IMPORTANTE: :el1 referencia a expressão #{usuarioLogado == null?'-1':usuarioLogado.idUsuario} (PJEII-5232)
     */
    // Divergência
    protected static final String R33 = "exists (select a from Anotacao a where " + 
		"a.documento.processoTrf = o.processoTrf and " +
		"a.tipoAnotacao = 'DIVERGENCIA' and " +
		"a.statusAnotacao <> 'R' and " +
		"(a.statusAnotacao = 'L' or  " +
		"a.orgaoJulgador = #{orgaoJulgadorAtual}) and " +
		"a.statusAcolhidoAnotacao is not null)";
    // Divergência com análise pendente
    protected static final String R34 = "exists (select a from Anotacao a where " +
    	"a.documento.processoTrf = o.processoTrf and " +
    	"(a.tipoAnotacao = 'DIVERGENCIA' and " +
    	"a.statusAcolhidoAnotacao is null and statusAnotacao = 'R') or " + 
    	"(a.tipoAnotacao = 'DIVERGENCIA' and  " +
    	"a.statusAcolhidoAnotacao is not null and statusCienciaAnotacao is null and a.orgaoJulgador = #{orgaoJulgadorAtual}))";
    	//"a.statusAcolhidoAnotacao is not null and statusCienciaAnotacao is null and a.orgaoJulgador = :el7))";
    
    // Divergência não concluída/liberada
    protected static final String R35 = "exists (select a from Anotacao a where " +
    	"a.documento.processoTrf = o.processoTrf and " +
    	"a.tipoAnotacao = 'DIVERGENCIA' and " +
    	"((a.statusAnotacao = 'N' and a.pessoaCriacao.idUsuario = cast(:el1 as integer)) or " + 
    	"a.statusAnotacao = 'C' and a.orgaoJulgador.idOrgaoJulgador = cast(#{orgaoJulgadorAtual == null?-1:orgaoJulgadorAtual.idOrgaoJulgador} as integer)))";
    // Destaque
    protected static final String R36 = "exists (select a from Anotacao a where " +
    	"a.documento.processoTrf = o.processoTrf and " +
    	"a.destaque = #{true} and a.statusAnotacao = 'L')";
    // Destaque não concluído/liberado
    protected static final String R37 = "exists (select a from Anotacao a where " +
    	"a.documento.processoTrf = o.processoTrf and " +
		"a.destaque = true and  " +
		"((a.statusAnotacao = 'N' and a.pessoaCriacao.idUsuario = cast(:el1 as integer)) or " + 
		"a.statusAnotacao = 'C' and a.orgaoJulgador.idOrgaoJulgador = cast(#{orgaoJulgadorAtual == null?-1:orgaoJulgadorAtual.idOrgaoJulgador} as integer))))";
 // Anotação
    protected static final String R38 = "exists (select a from Anotacao a where " + 
		"a.documento.processoTrf = o.processoTrf and " + 
		"((a.tipoAnotacao = 'ANOTACAO' and a.destaque != true) or a.tipoAnotacao = 'SUGESTAO_DISPOSITIVO' ) and " + 
		"((a.orgaoJulgador = cast(#{orgaoJulgadorAtual == null?-1:orgaoJulgadorAtual.idOrgaoJulgador} as integer) and a.statusAnotacao = 'C' ) or " +
		"a.statusAnotacao = 'L' ))";
    // Anotação não concluída
    protected static final String R39 = "exists (select a from Anotacao a where " +
		"a.documento.processoTrf = o.processoTrf and " +
		"((a.tipoAnotacao = 'ANOTACAO' and a.destaque != true) or a.tipoAnotacao in ( 'VOTO' , 'SUGESTAO_DISPOSITIVO' )) and " +
		"a.statusAnotacao = 'N' and a.pessoaCriacao.idUsuario = cast(:el1 as integer) or " +
		"(a.tipoAnotacao = 'VOTO' and a.statusAnotacao = 'C' and a.orgaoJulgador.idOrgaoJulgador = cast(#{orgaoJulgadorAtual == null?-1:orgaoJulgadorAtual.idOrgaoJulgador} as integer)))";

    public ClassificacaoTipoSituacaoPautaEnum tipoClassificacao(char tipo) {
        switch (tipo) {
        case 'J':
            return ClassificacaoTipoSituacaoPautaEnum.J;

        case 'D':
            return ClassificacaoTipoSituacaoPautaEnum.D;

        case 'R':
            return ClassificacaoTipoSituacaoPautaEnum.R;

        case 'P':
            return ClassificacaoTipoSituacaoPautaEnum.A;

        default:
            return null;
        }
    }

    @Override
    public void addSearchFields() {
        super.addSearchFields();
        addSearchField("sessao", SearchCriteria.igual, R15);
        addSearchField("sessaoSecretario", SearchCriteria.igual, R16);
        addSearchField("sessaoMagistrado", SearchCriteria.igual, R17);
        addSearchField("sessaoProcurador", SearchCriteria.igual, R18);
    }

    private String voto(boolean elaborado, boolean liberado) {
        StringBuilder sb = new StringBuilder();
        
        /*if (sb.indexOf("where") == -1) {
        	sb.append(" where ");
        } else {
        	sb.append(" and ");
        }*/
        
        sb.append(" and ");

        String liberacao = "";

        if (!elaborado) {
            sb.append("(exists(select v from Voto v where ");
            sb.append("o.sessao = v.sessao ");
            sb.append("and o.processoTrf = v.processoTrf ");
            sb.append("and v.orgaoJulgador = ");
            sb.append("o.processoTrf.orgaoJulgador ");
            sb.append("and v.orgaoJulgador != #{orgaoJulgadorAtual} ");
            sb.append("and v.liberacao = true) or ");
            sb.append(
                "o.processoTrf.orgaoJulgador = #{orgaoJulgadorAtual}) and ");
            sb.append("not ");
        } else if (liberado) {
            liberacao = "and v.liberacao = true ";
        } else {
            liberacao = "and v.liberacao = false ";
        }

        sb.append("exists(select v from Voto v where ");
        sb.append("o.sessao = v.sessao ");
        sb.append("and o.processoTrf = v.processoTrf ");
        sb.append("and v.orgaoJulgador = ");

        if (Authenticator.getOrgaoJulgadorAtual() != null) {
            sb.append("#{orgaoJulgadorAtual} ");
        } else {
            sb.append("o.processoTrf.orgaoJulgador ");
        }

        sb.append(liberacao);
        sb.append(") ");

        return sb.toString();
    }

    private String votoNaoElaboradoOuNaoLiberado() {
        StringBuilder sb = new StringBuilder();
        
        /*if (sb.indexOf("where") == -1) {
        	sb.append(" where ");
        } else {
        	sb.append(" and ");
        }*/
        
        sb.append(" and ");
        
        sb.append("(not exists(select v from Voto v where ");
        sb.append("o.sessao = v.sessao ");
        sb.append("and o.processoTrf = v.processoTrf ");
        sb.append("and v.orgaoJulgador = ");
        sb.append("o.processoTrf.orgaoJulgador) ");

        sb.append("or ");

        sb.append("exists(select v from Voto v where ");
        sb.append("o.sessao = v.sessao ");
        sb.append("and o.processoTrf = v.processoTrf ");
        sb.append("and v.orgaoJulgador = ");
        sb.append("o.processoTrf.orgaoJulgador ");
        sb.append("and v.liberacao = false) ");
        sb.append(") ");

        if (Authenticator.getOrgaoJulgadorAtual() != null) {
            sb.append(
                "and o.processoTrf.orgaoJulgador != #{orgaoJulgadorAtual} ");
        }

        return sb.toString();
    }

    public void addSearchFields(Map<String, Boolean> mapLegenda) {
        StringBuilder sb = new StringBuilder(getDefaultEjbql());
        
        /**
         * Filtros referentes às novas legendas (fernando.junior - 17/01/2013)
         */
        if (mapLegenda.get(ItensLegendas.SIGLAS_LEGENDAS_VOTO[0])) {
            addSearchField("divergencia", SearchCriteria.igual, R33);
        }
        
        if (mapLegenda.get(ItensLegendas.SIGLAS_LEGENDAS_VOTO[10])) {
        	addSearchField("divergenciaPendente", SearchCriteria.igual, R34);
        }
        
        if (mapLegenda.get(ItensLegendas.SIGLAS_LEGENDAS_VOTO[11])) {
        	addSearchField("divergenciaNaoConcluida", SearchCriteria.igual, R35);
        }

        if (mapLegenda.get(ItensLegendas.SIGLAS_LEGENDAS_VOTO[1])) {
            addSearchField("destaque", SearchCriteria.igual, R36);
        }
        
        if (mapLegenda.get(ItensLegendas.SIGLAS_LEGENDAS_VOTO[12])) {
        	addSearchField("destaqueNaoConcluido", SearchCriteria.igual, R37);
        }

        if (mapLegenda.get(ItensLegendas.SIGLAS_LEGENDAS_VOTO[13])) {
            addSearchField("anotacao", SearchCriteria.igual, R38);
        }
        
        if (mapLegenda.get(ItensLegendas.SIGLAS_LEGENDAS_VOTO[14])) {
        	addSearchField("anotacaoNaoConcluida", SearchCriteria.igual, R39);
        }
		/** end Filtros novas legendas **/        
        
        if (mapLegenda.get(ItensLegendas.SIGLAS_LEGENDAS_VOTO[3])) {
            addSearchField("preferencia", SearchCriteria.igual, R22);
        }

        if (mapLegenda.get(ItensLegendas.SIGLAS_LEGENDAS_VOTO[4])) {
            addSearchField("sustentacaoOral", SearchCriteria.igual, R23);
        }

        if (mapLegenda.get(ItensLegendas.SIGLAS_LEGENDAS_VOTO[5])) {
            sb.append(voto(false, false));
        } else if (mapLegenda.get(ItensLegendas.SIGLAS_LEGENDAS_VOTO[6])) {
            sb.append(voto(true, false));
        } else if (mapLegenda.get(ItensLegendas.SIGLAS_LEGENDAS_VOTO[7])) {
            sb.append(voto(true, true));
        } else if (mapLegenda.get(ItensLegendas.SIGLAS_LEGENDAS_VOTO[9])) {
            sb.append(votoNaoElaboradoOuNaoLiberado());
        }

        sb.append(" and o.situacaoAnalise != 'A' ");

        setEjbql(sb.toString());
    }

    public void addSearchFieldsPainel(Map<String, Boolean> mapLegenda,
        char painel) {
        if (painel != 'P') {
            /**
             * Filtros referentes às novas legendas (fernando.junior - 17/01/2013)
             */
            if (mapLegenda.get(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[0])) {
                addSearchField("divergencia", SearchCriteria.igual, R33);
            }
            
            if (mapLegenda.get(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[14])) {
            	addSearchField("divergenciaPendente", SearchCriteria.igual, R34);
            }
            
            if (mapLegenda.get(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[15])) {
            	addSearchField("divergenciaNaoConcluida", SearchCriteria.igual, R35);
            }

            if (mapLegenda.get(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[1])) {
                addSearchField("destaque", SearchCriteria.igual, R36);
            }
            
            if (mapLegenda.get(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[16])) {
            	addSearchField("destaqueNaoConcluido", SearchCriteria.igual, R37);
            }

            if (mapLegenda.get(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[17])) {
                addSearchField("anotacao", SearchCriteria.igual, R38);
            }
            
            if (mapLegenda.get(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[18])) {
            	addSearchField("anotacaoNaoConcluida", SearchCriteria.igual, R39);
            }
    		/** end Filtros novas legendas **/       
        }

        if (mapLegenda.get(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[3])) {
            addSearchField("julgado", SearchCriteria.igual, R24);
        } else if (mapLegenda.get(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[12])) {
            addSearchField("julgadoSemPendencia", SearchCriteria.igual, R31);
        } else if (mapLegenda.get(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[13])) {
            addSearchField("julgadoComPendencia", SearchCriteria.igual, R32);
        } else if (mapLegenda.get(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[4])) {
            addSearchField("deliberado", SearchCriteria.igual, R25);
        } else if (mapLegenda.get(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[5])) {
            addSearchField("retiradoPauta", SearchCriteria.igual, R26);
        } else if (mapLegenda.get(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[6])) {
            addSearchField("pendente", SearchCriteria.igual, R27);
        }

        if (mapLegenda.get(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[7])) {
            addSearchField("sustentacaoOral", SearchCriteria.igual, R23);
        }

        if (mapLegenda.get(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[8])) {
            addSearchField("preferencia", SearchCriteria.igual, R22);
        }

        if (painel == 'M') {
            StringBuilder sb = new StringBuilder(getDefaultEjbql());

            if (mapLegenda.get(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[11])) {
                sb.append(voto(false, false));
            } else if (mapLegenda.get(
                        ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[12])) {
                sb.append(voto(true, false));
            } else if (mapLegenda.get(
                        ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[13])) {
                sb.append(voto(true, true));
            } else if (mapLegenda.get(
                        ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[14])) {
                sb.append(votoNaoElaboradoOuNaoLiberado());
            }

            if (mapLegenda.get(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[9])) {
                addSearchField("julgado", SearchCriteria.igual, R24);
                addSearchField("acordaoAssinado", SearchCriteria.igual, R28);
            }

            if (mapLegenda.get(ItensLegendas.SIGLAS_MAGISTRADO_LEGENDAS[10])) {
                addSearchField("julgado", SearchCriteria.igual, R24);
                addSearchField("acordaoNaoAssinado", SearchCriteria.igual, R29);
            }

            setEjbql(sb.toString());
        } else {
            if (mapLegenda.get(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[9])) {
                addSearchField("julgado", SearchCriteria.igual, R24);
                addSearchField("acordaoAssinado", SearchCriteria.igual, R28);
            }

            if (mapLegenda.get(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[10])) {
                addSearchField("julgado", SearchCriteria.igual, R24);
                addSearchField("acordaoAssinadoMagistrado",
                    SearchCriteria.igual, R30);
            }

            if (mapLegenda.get(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[11])) {
                addSearchField("julgado", SearchCriteria.igual, R24);
                addSearchField("acordaoNaoAssinado", SearchCriteria.igual, R29);
            }
        }
    }

    // PJEII-5414 - Provendo "el1" para superar limitação do JBoss Seam.
    @Override
    protected String getDefaultEjbql() {
    	StringBuilder sb = new StringBuilder(FiltrosPautaVotacaoAntecipadaList.DEFAULT_EJBQL);
		sb.append(" where (1 = 1 or cast(#{usuarioLogado == null?-1:usuarioLogado.idUsuario} as integer) = cast(-9 as integer)) ");
		return sb.toString();
    }

    @Override
    public void setSearchFieldMap(Map<String, SearchField> searchFieldMap) {
        super.setSearchFieldMap(searchFieldMap);
    }

    @Override
    protected String getDefaultOrder() {
        return DEFAULT_ORDER;
    }
}
