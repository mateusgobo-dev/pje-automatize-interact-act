package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.jus.pje.nucleo.enums.SimNaoEnum;
import br.jus.pje.nucleo.enums.SituacaoProcessoSessaoEnum;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.util.ParametroUtil;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;

@Name(ProcessoJulgadoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ProcessoJulgadoList extends EntityList<SessaoPautaProcessoTrf> {

    private static final long serialVersionUID = 1L;

	public static final String NAME = "processoJulgadoList";

	private static final String DEFAULT_ORDER = "o.numeroOrdem";

    private static final String DEFAULT_EJBQL = "SELECT o FROM SessaoPautaProcessoTrf o " +
                                            "JOIN o.consultaProcessoTrf cons " +
                                            "WHERE o.dataExclusaoProcessoTrf IS NULL " +
											"AND not exists (" +
											"SELECT 1 FROM ProcessoBloco processoBloco " +
											"WHERE processoBloco.bloco.sessao = o.sessao " +
											"AND processoBloco.processoTrf = o.processoTrf ) " +
                                            "AND (o.situacaoJulgamento = 'JG'";

    private static final String QUERY_CERTIDAO = "SELECT spd.idSessaoProcessoDocumento " +
                                             "FROM SessaoProcessoDocumento spd " +
                                             "INNER JOIN spd.processoDocumento pd " +
                                             "INNER JOIN pd.processoDocumentoBin pdb ";

    private static final String WHERE_CERTIDAO = "WHERE spd.sessao.id = o.sessao.idSessao " +
                                             "AND pd.processo.idProcesso = o.processoTrf.idProcessoTrf " +
                                             "AND pd.tipoProcessoDocumento.idTipoProcessoDocumento = " +
                                             "#{parametroUtil.tipoProcessoDocumentoCertidaoJulgamento != null ? parametroUtil.tipoProcessoDocumentoCertidaoJulgamento.idTipoProcessoDocumento : parametroUtil.tipoProcessoDocumentoCertidao.idTipoProcessoDocumento} " +
    										 "AND pd.ativo = true";

	private static final String R1 = "o.sessao.idSessao = #{sessaoHome.instance.idSessao} ";
	private static final String R2 = "o.processoTrf.orgaoJulgador = #{processoJulgadoList.orgaoJulgador}";

    private StringBuilder defaultEjbql;
	private OrgaoJulgador orgaoJulgador;
	private SimNaoEnum possuiCertidao;
	private SimNaoEnum certidaoAssinada;
	private SituacaoProcessoSessaoEnum situacaoProcessoSessao;

    @Override
    public List<SessaoPautaProcessoTrf> list() {
        recarregarEjbql();
        return super.list();
    }

    @Override
    public List<SessaoPautaProcessoTrf> list(int maxResult) {
        recarregarEjbql();
        return super.list(maxResult);
    }

    /**
     * Responsável por limpar os objetos utilizados como filtro da pesquisa.
     */
    public void limparFiltro() {
        newInstance();
        orgaoJulgador = null;
        possuiCertidao = null;
        certidaoAssinada = null;
        situacaoProcessoSessao = null;
    }

    @Override
	protected void addSearchFields() {
		addSearchField("sessao", SearchCriteria.igual, R1);
		addSearchField("processoTrf", SearchCriteria.igual);
		addSearchField("numeroOrdem", SearchCriteria.igual);
		addSearchField("processoTrf.orgaoJulgador", SearchCriteria.igual, R2);
	}

	@Override
	protected String getDefaultEjbql() {

        defaultEjbql = new StringBuilder(DEFAULT_EJBQL);
        
        if (situacaoProcessoSessao == null){
	        final String listaSituacaoJulgamento = ParametroUtil.instance().getListaSituacaoJulgamento();
			if(StringUtils.isNotEmpty(listaSituacaoJulgamento)){
				final Map<String, String> querysMap = new HashMap<String, String>(3);
				// AD - Adiado
				querysMap.put("AD", " or (o.adiadoVista = 'AD' and o.retiradaJulgamento = false)");
				// RJ - Retirado de julgamento
				querysMap.put("RJ", " or (o.adiadoVista = 'AD' and o.retiradaJulgamento = true)");
				// PV - Pedido de vista
				querysMap.put("PV", " or (o.adiadoVista = 'PV' and o.situacaoJulgamento = 'NJ')");
				
				for (Map.Entry<String, String> entry : querysMap.entrySet()) {
					if (listaSituacaoJulgamento.toUpperCase().contains(entry.getKey())) {
	                    defaultEjbql.append(entry.getValue());
					}
				}
			}
        }

        defaultEjbql.append(")");

		return defaultEjbql.toString();
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		return map;
	}

    /**
     * Recarrega o EJBQL para reavaliar o filtro de emissao do certificado.
     */
    private void recarregarEjbql() {
        StringBuilder query = new StringBuilder(defaultEjbql);

        if (possuiCertidao != null) {
            gerarQueryPossuiCertidao(query);
        }

        if (certidaoAssinada != null) {
            gerarQueryCertidaoAssinada(query);
        }
        
        if (situacaoProcessoSessao !=null){
        	gerarQuerySituacaoProcessoSessao(query);
        }

        setEjbql(query.toString());
    }
    
    /**
     * Gera a query necessária para atender ao filtro "Situação do Processo na Sessão".
     * 
     * @param query
     */
    private void gerarQuerySituacaoProcessoSessao(StringBuilder query) {
    	if (situacaoProcessoSessao.equals(SituacaoProcessoSessaoEnum.JG)){
    		query.append(" and (o.situacaoJulgamento = 'JG')");
    	}
    	if (situacaoProcessoSessao.equals(SituacaoProcessoSessaoEnum.AD)){
    		query.append(" and (o.adiadoVista = 'AD' and o.retiradaJulgamento = false)");
    	}
    	if (situacaoProcessoSessao.equals(SituacaoProcessoSessaoEnum.RJ)){
    		query.append(" and (o.adiadoVista = 'AD' and o.retiradaJulgamento = true)");
    	}
    	if (situacaoProcessoSessao.equals(SituacaoProcessoSessaoEnum.PV)){
    		query.append(" and (o.adiadoVista = 'PV' and o.situacaoJulgamento = 'NJ')");
    	}
	}

	/**
     * Gera a query necessária para atender ao filtro "Possui certificado".
     * 
     * @param query
     */
    private void gerarQueryPossuiCertidao(final StringBuilder query) {
        query.append(obterCondicaoValorSelecionado(possuiCertidao));

        query.append(" ( ");
        query.append(QUERY_CERTIDAO);
        query.append(WHERE_CERTIDAO);
        query.append(" ) ");
    }

    /**
     * Gera a query necessária para atender ao filtro "Certidão assinada".
     * 
     * @param query
     */
    private void gerarQueryCertidaoAssinada(final StringBuilder query) {
        query.append(obterCondicaoValorSelecionado(certidaoAssinada));

        query.append(" ( ");
        query.append(QUERY_CERTIDAO);
        query.append("inner join pdb.signatarios assinaturas ");
        query.append(WHERE_CERTIDAO);
        query.append(" ) ");
    }

    /**
     * Determina de acordo com o ENUM se deve existir o registro ou não na condição da query.
     * 
     * @param opcaoSelecionada
     * 
     * @return "and exists" para SimNaoEnum.S, caso contrário "and not exists".
     */
    private String obterCondicaoValorSelecionado(SimNaoEnum opcaoSelecionada) {
        return SimNaoEnum.S.equals(opcaoSelecionada) ? " and exists " : " and not exists ";
    }

    public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public SimNaoEnum getCertidaoAssinada() {
		return certidaoAssinada;
	}

	public void setCertidaoAssinada(SimNaoEnum certidaoAssinada) {
		this.certidaoAssinada = certidaoAssinada;
	}

	public SimNaoEnum getPossuiCertidao() {
		return possuiCertidao;
	}

	public void setPossuiCertidao(SimNaoEnum possuiCertidao) {
		this.possuiCertidao = possuiCertidao;
	}

	public SituacaoProcessoSessaoEnum getSituacaoProcessoSessao() {
		return situacaoProcessoSessao;
	}

	public void setSituacaoProcessoSessao(SituacaoProcessoSessaoEnum situacaoProcessoSessao) {
		this.situacaoProcessoSessao = situacaoProcessoSessao;
	}
	
}