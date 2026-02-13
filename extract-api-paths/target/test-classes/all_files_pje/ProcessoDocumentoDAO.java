package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.view.PaginatedDataModel;
import br.jus.cnj.pje.vo.ProcessoDocumentoConsultaNaoAssinadoVO;
import br.jus.je.pje.entity.vo.ProcessoDocumentoVO;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.TipoOrigemAcaoEnum;
import br.jus.pje.nucleo.util.StringUtil;
import java.util.Objects;

/**
 * @author cristof
 *
 */
@Name("processoDocumentoDAO")
public class ProcessoDocumentoDAO extends BaseDAO<ProcessoDocumento> {

    private static final String ID_PROCESSO_DOCUMENTO = "idProcessoDocumento";
    String QUERY_PARAMETER_PROCESSO = "processo";
    String QUERY_PARAMETER_PROCESSO_DOC_BIN = "procDocBin";
    String QUERY_PARAMETER_TIPO_DOC = "descTipoDoc";
    String LIST_PROCESSO_DOCUMENTO_BY_TIPO_QUERY = "select pd from ProcessoDocumento pd " +
        "where pd.processo = :" + QUERY_PARAMETER_PROCESSO + " and " +
        "pd.tipoProcessoDocumento = :" + QUERY_PARAMETER_TIPO_DOC;
    String LIST_ULTIMO_PROCESSO_DOCUMENTO_BY_TIPO_QUERY = "select o from ProcessoDocumento o where o.tipoProcessoDocumento= :" +
        QUERY_PARAMETER_TIPO_DOC + " and " + "o.processo = :" +
        QUERY_PARAMETER_PROCESSO + " and o.ativo=true " +
        "order by o.dataInclusao desc";
    String LIST_ULTIMO_PROCESSO_DOCUMENTO_ASSINADO_BY_TIPO_QUERY = LIST_PROCESSO_DOCUMENTO_BY_TIPO_QUERY +
        " and pd.ativo = true " +
        " and pd.processoDocumentoBin in (select pdba.processoDocumentoBin from ProcessoDocumentoBinPessoaAssinatura pdba" +
        "			  					   where pdba.dataAssinatura = (select MAX(pdba2.dataAssinatura) from ProcessoDocumentoBinPessoaAssinatura pdba2" +
        "										 						 where pdba2.processoDocumentoBin in (select pd2.processoDocumentoBin from ProcessoDocumento pd2" +
        "																									   where pd2.processo = pd.processo" +
        "																										 and pd2.tipoProcessoDocumento = pd.tipoProcessoDocumento" +
        "																										 and pd2.ativo = pd.ativo)))";
    String IS_DOCUMENTO_ASSINADO = "select pdbpa from ProcessoDocumentoBinPessoaAssinatura pdbpa where pdbpa.processoDocumentoBin= :" +
        QUERY_PARAMETER_PROCESSO_DOC_BIN;
    String GET_DOCUMENTO_BY_TIPO_PROCESSO = "select o from ProcessoDocumento o where o.tipoProcessoDocumento= :" +
        QUERY_PARAMETER_TIPO_DOC + " " + "and o.processo =:" +
        QUERY_PARAMETER_PROCESSO + " and o.ativo=true";
    
    public List<ProcessoDocumento> findByRange(ProcessoTrf processo, int first,
        int length) {
        return findByRange(processo, false, first, length);
    }

    @SuppressWarnings("unchecked")
    public List<ProcessoDocumento> findByRange(ProcessoTrf processo,
        boolean incluiSigilosos, int first, int length) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(
            "SELECT d FROM ProcessoDocumento AS d WHERE d.processo.idProcesso = :id");

        if (!incluiSigilosos) {
            queryBuilder.append(" AND d.documentoSigiloso = false");
        }

        Query q = entityManager.createQuery(queryBuilder.toString());
        q.setParameter("id", processo.getProcesso().getIdProcesso());
        q.setFirstResult((first > 0) ? first : 0);
        q.setMaxResults((length > 0) ? length : 10);

        List<ProcessoDocumento> list = q.getResultList();

        return list;
    }

    public List<ProcessoDocumento> findByRange(ProcessoTrf processo, int first,
        int length, boolean decrescente, boolean incluirPDF,
        boolean incluirComAssinaturaInvalidada, boolean incluirDocumentoPeticaoInicial) {
    	return findByRange(processo, first, length,
                decrescente, incluirPDF, incluirComAssinaturaInvalidada, incluirDocumentoPeticaoInicial, false, false);
    }

	public List<ProcessoDocumento> findByRange(ProcessoTrf processo, int first,
        int length, boolean decrescente, boolean incluirPDF,
        boolean incluirComAssinaturaInvalidada,
        boolean incluirDocumentoPeticaoInicial, boolean soDocumentosJuntados, boolean incluirDocCopiaExpediente) {
    	
    	return this.findByRange(processo, first, length, false, decrescente, incluirPDF, incluirComAssinaturaInvalidada, incluirDocumentoPeticaoInicial, soDocumentosJuntados, incluirDocCopiaExpediente, false);
    }

	public List<ProcessoDocumento> findByRange(ProcessoTrf processo, int first,
        int length, boolean b, boolean decrescente, boolean incluirPDF,
        boolean incluirComAssinaturaInvalidada,
        boolean incluirDocumentoPeticaoInicial, boolean soDocumentosJuntados, boolean incluirDocCopiaExpediente, boolean apenasAtosProferidos) {
		return this.findByRange(processo, first, length, b, decrescente, incluirPDF, incluirComAssinaturaInvalidada, incluirDocumentoPeticaoInicial, soDocumentosJuntados, incluirDocCopiaExpediente, apenasAtosProferidos, null);
	}
	
    @SuppressWarnings("unchecked")
	public List<ProcessoDocumento> findByRange(ProcessoTrf processo, int first,
        int length, boolean b, boolean decrescente, boolean incluirPDF,
        boolean incluirComAssinaturaInvalidada,
        boolean incluirDocumentoPeticaoInicial, boolean soDocumentosJuntados, boolean incluirDocCopiaExpediente, boolean apenasAtosProferidos, TipoOrigemAcaoEnum tipoOrigemAcao) {
    	
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(
            "SELECT d FROM ProcessoDocumento AS d LEFT JOIN d.documentoPrincipal AS dp WHERE d.processo.idProcesso = :id");
        queryBuilder.append(
            " AND d.documentoSigiloso = false AND d.ativo = true ");

        if (soDocumentosJuntados) {
            queryBuilder.append(
                " AND d.dataJuntada IS NOT NULL ");
        }
        
        if (!incluirPDF) {
            queryBuilder.append(
                " AND trim(cast(d.processoDocumentoBin.modeloDocumento as string)) != '' ");
        }

        if (!incluirComAssinaturaInvalidada) {
            queryBuilder.append(" AND d.processoDocumentoBin.valido = true ");
        }

        if(!incluirDocumentoPeticaoInicial &&
    		processo.getClasseJudicial() != null &&
    		processo.getClasseJudicial().getTipoProcessoDocumentoInicial() != null &&
    		processo.getClasseJudicial().getTipoProcessoDocumentoInicial().getIdTipoProcessoDocumento() != null) {
        	
        	Integer idTipoProcessoDocumentoPeticaoInicial = processo.getClasseJudicial().getTipoProcessoDocumentoInicial().getIdTipoProcessoDocumento();
          	queryBuilder.append(" AND d.tipoProcessoDocumento.idTipoProcessoDocumento != " + idTipoProcessoDocumentoPeticaoInicial);
        }

        if(apenasAtosProferidos) {
            queryBuilder.append(
                    " AND d.tipoProcessoDocumento.documentoAtoProferido = true ");
        }

        if(!incluirDocCopiaExpediente) {
            queryBuilder.append(" and not exists  ")
	            .append(" ( ")
	            .append(" select 1 from ProcessoExpediente processoExpediente ")
	            .append(" where processoExpediente.processoDocumento = d ")
	            .append(" and processoExpediente.documentoExistente = true ")
	            .append(" and processoExpediente.processoDocumentoVinculadoExpediente IS NOT NULL ")
	            .append(" ) ");
        }
        
        if (tipoOrigemAcao != null) {
			queryBuilder.append(" AND d.inTipoOrigemJuntada = :tipoOrigemJuntada");
		}

		String orderBy = " ORDER BY d.dataJuntada @tipoOrdenacao, "
				+ "  (CASE "
				+ "     WHEN dp.idProcessoDocumento IS NOT NULL THEN dp.dataInclusao  "
				+ "     ELSE d.dataInclusao     "
				+ "  END) @tipoOrdenacao,       "
				+ "  (CASE                      "
				+ "     WHEN d.numeroOrdem IS NULL THEN 0 "
				+ "     ELSE d.numeroOrdem      "
				+ "  END) @tipoOrdenacao,                 "
				+ "  d.dataInclusao @tipoOrdenacao,       "
				+ "  d.idProcessoDocumento @tipoOrdenacao ";
		
		if (decrescente) {
			queryBuilder.append(orderBy.replace("@tipoOrdenacao", "DESC"));
		} else {
			queryBuilder.append(orderBy.replace("@tipoOrdenacao", "ASC"));
		}

        Query q = entityManager.createQuery(queryBuilder.toString());
        q.setParameter("id", processo.getProcesso().getIdProcesso());
        if (tipoOrigemAcao != null) {
        	q.setParameter("tipoOrigemJuntada", tipoOrigemAcao);
        }
        q.setFirstResult((first > 0) ? first : 0);
        q.setMaxResults((length > 0) ? length : 10);

        List<ProcessoDocumento> list = q.getResultList();

        return list;
    }

    public Integer getCountDocumentos(ProcessoTrf processo, boolean incluirPDF,
        boolean incluirComAssinaturaInvalidada, boolean incluirDocumentoPeticaoInicial) {
    	return getCountDocumentos(processo,
                incluirPDF, incluirComAssinaturaInvalidada, incluirDocumentoPeticaoInicial, false, false);
    }
    
	public Integer getCountDocumentos(ProcessoTrf processo, boolean incluirPDF,
	        boolean incluirComAssinaturaInvalidada, boolean incluirDocumentoPeticaoInicial, boolean soDocumentosJuntados) {
		return this.getCountDocumentos(processo, incluirPDF, incluirComAssinaturaInvalidada, incluirDocumentoPeticaoInicial, soDocumentosJuntados, false);
		
	}
    
	public Integer getCountDocumentos(ProcessoTrf processo, boolean incluirPDF,
	        boolean incluirComAssinaturaInvalidada, boolean incluirDocumentoPeticaoInicial, boolean soDocumentosJuntados, boolean apenasAtosProferidos) {
		return this.getCountDocumentos(processo, incluirPDF, incluirComAssinaturaInvalidada, incluirDocumentoPeticaoInicial, soDocumentosJuntados, apenasAtosProferidos);
	}
	
    @SuppressWarnings("unchecked")
	public Integer getCountDocumentos(ProcessoTrf processo, boolean incluirPDF,
        boolean incluirComAssinaturaInvalidada, boolean incluirDocumentoPeticaoInicial, boolean soDocumentosJuntados, boolean apenasAtosProferidos, TipoOrigemAcaoEnum tipoOrigemAcao) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(
            "SELECT count(*) FROM ProcessoDocumento AS d WHERE d.processo.idProcesso = :id");
        queryBuilder.append(
            " AND d.documentoSigiloso = false AND d.ativo = true ");

        if (soDocumentosJuntados) {
            queryBuilder.append(
                    " AND d.dataJuntada IS NOT NULL ");
        }

        if (!incluirPDF) {
            queryBuilder.append(
                " AND trim(cast(d.processoDocumentoBin.modeloDocumento as string)) != ''");
        }

        if (!incluirComAssinaturaInvalidada) {
            queryBuilder.append(" AND d.processoDocumentoBin.valido = true ");
        }
        
        if(!incluirDocumentoPeticaoInicial && 
    		processo.getClasseJudicial() != null &&
    		processo.getClasseJudicial().getTipoProcessoDocumentoInicial() != null &&
    		processo.getClasseJudicial().getTipoProcessoDocumentoInicial().getIdTipoProcessoDocumento() != null) {
        	
        	Integer idTipoProcessoDocumentoPeticaoInicial = processo.getClasseJudicial().getTipoProcessoDocumentoInicial().getIdTipoProcessoDocumento();
          	queryBuilder.append(" AND d.tipoProcessoDocumento.idTipoProcessoDocumento != " + idTipoProcessoDocumentoPeticaoInicial);
        }
        
        if(apenasAtosProferidos) {
            queryBuilder.append(
                    " AND d.tipoProcessoDocumento.documentoAtoProferido = true ");
        }
        
        if (tipoOrigemAcao != null) {
			queryBuilder.append(" AND d.inTipoOrigemJuntada = :tipoOrigemJuntada");
		}

        Query q = entityManager.createQuery(queryBuilder.toString());
        q.setParameter("id", processo.getProcesso().getIdProcesso());
        if (tipoOrigemAcao != null) {
			q.setParameter("tipoOrigemJuntada", tipoOrigemAcao);
		}

        List<Long> list = q.getResultList();

        return list.get(0).intValue();
    }

    /*
     * INÍCIO DA INTEGRAÇÃO 1.2.0.M6 -> 1.4.0.M4 *********************************************************
     */

    /**
     * Obtem os processoDocumentos de um determinado processo através de um tipo de documento.
     *
     * @param proc Processo que se deseja obter os documentos.
     * @param tpd Tipo do Documento que deseja se consultar do <cod>proc</code>
     * @return Lista de Processos Documento
     */
    @SuppressWarnings("unchecked")
    public List<ProcessoDocumento> listProcessoDocumentoByTipo(Processo proc,
        TipoProcessoDocumento tpd) {
        return getEntityManager()
                   .createQuery("select pd from ProcessoDocumento pd " +
            "where pd.processo = :processo and " +
            "pd.tipoProcessoDocumento = :descTipoDoc")
                   .setParameter("processo", proc)
                   .setParameter("descTipoDoc", tpd).getResultList();
    }
    
    /**
	 * Obtém os ProcessoDocumentos de um determinado processo a partir de um
	 * tipo de documento.
	 * 
	 * @param idProcesso
	 * 		Id do processo
	 * 
	 * @param idTipoProcessoDocumento
	 * 		Id do tipo de documento
	 * 
	 * @return
	 */
    @SuppressWarnings("unchecked")
    public List<ProcessoDocumento> listProcessoDocumentoByTipo(int idProcesso,
        int idTipoProcessoDocumento) {
        return getEntityManager()
                   .createQuery("select pd from ProcessoDocumento pd " +
								"where pd.processo.idProcesso = :idProcesso and " +
								"pd.tipoProcessoDocumento.idTipoProcessoDocumento = :idTipoProcessoDocumento " +
							    " order by pd.idProcessoDocumento desc")
                   .setParameter("idProcesso", idProcesso)
                   .setParameter("idTipoProcessoDocumento", idTipoProcessoDocumento).getResultList();
    }

    /*
     * FIM INTEGRAÇÃO 1.2.0.M6 -> 1.4.0.M4 ***************************************************************
     */
    @Override
    public Integer getId(ProcessoDocumento e) {
        return e.getIdProcessoDocumento();
    }

    public ProcessoDocumento getUltimoProcessoDocumentoByProcessoTipoProcessoDocumento(
        TipoProcessoDocumento tipoProcessoDocumento, Processo processo) {
        Query q = getEntityManager()
                      .createQuery(LIST_ULTIMO_PROCESSO_DOCUMENTO_BY_TIPO_QUERY);
        q.setParameter(QUERY_PARAMETER_TIPO_DOC, tipoProcessoDocumento);
        q.setParameter(QUERY_PARAMETER_PROCESSO, processo);

        q.setMaxResults(1);
        ProcessoDocumento processoDocumento = EntityUtil.getSingleResult(q);

        return processoDocumento;
    }

    public ProcessoDocumento getUltimoProcessoDocumentoAssinadoByProcessoTipoProcessoDocumento(
        TipoProcessoDocumento tipoProcessoDocumento, Processo processo) {
        Query q = getEntityManager()
                      .createQuery(LIST_ULTIMO_PROCESSO_DOCUMENTO_ASSINADO_BY_TIPO_QUERY);
        q.setParameter(QUERY_PARAMETER_TIPO_DOC, tipoProcessoDocumento);
        q.setParameter(QUERY_PARAMETER_PROCESSO, processo);

        ProcessoDocumento processoDocumento = EntityUtil.getSingleResult(q);

        return processoDocumento;
    }

    public ProcessoDocumento getUltimoProcessoDocumentoByTiposProcessoDocumento(
        List<TipoProcessoDocumento> tipos, Processo processo) {
        
    	StringBuilder query = new StringBuilder("select o from ProcessoDocumento o ")
        	.append("where o.tipoProcessoDocumento in (:")
        	.append(QUERY_PARAMETER_TIPO_DOC)
        	.append(") and o.ativo = true and o.processoDocumentoBin.valido = true and o.processo =:")
        	.append(QUERY_PARAMETER_PROCESSO)
        	.append(" and o not in (select e.processoDocumento from ProcessoExpediente e where e.processoDocumento = o and e.documentoExistente = true)")
        	.append(" order by o.dataJuntada DESC, o.dataInclusao DESC");
        Query q = getEntityManager().createQuery(query.toString());
        q.setParameter(QUERY_PARAMETER_TIPO_DOC, tipos);
        q.setParameter(QUERY_PARAMETER_PROCESSO, processo);
        q.setMaxResults(1);

        ProcessoDocumento processoDocumento = EntityUtil.getSingleResult(q);

        return processoDocumento;
    }

    public ProcessoDocumento getUltimoProcessoDocumentoNaoAssinado(Processo processo) {
            Query q = getEntityManager().createQuery(
            		"select pd from ProcessoDocumento pd where pd.processo = :" + QUERY_PARAMETER_PROCESSO +
                        		  " and pd.processoDocumentoBin.valido = false and pd.ativo = true " +
                        		  " and (pd.idJbpmTask is not null and pd.idJbpmTask > 0) order by pd.dataInclusao DESC"
            		);
            
            q.setParameter(QUERY_PARAMETER_PROCESSO, processo);

            ProcessoDocumento processoDocumento = EntityUtil.getSingleResult(q);

            return processoDocumento;
        }
    
    public ProcessoDocumento getUltimoProcessoDocumento(Processo processo) {
        Query q = getEntityManager().createQuery(
        		"select pd from ProcessoDocumento pd where pd.processo = :" + QUERY_PARAMETER_PROCESSO +                   		 
                    		  " order by pd.dataInclusao DESC"
        		);
        
        q.setParameter(QUERY_PARAMETER_PROCESSO, processo);

        ProcessoDocumento processoDocumento = EntityUtil.getSingleResult(q);

        return processoDocumento;
    }

    public ProcessoDocumento getUltimoProcessoDocumentoPrincipalAtivo(Processo processo) {
        StringBuilder jpql = new StringBuilder();
        jpql.append(" select pd from ProcessoDocumento pd ");
        jpql.append(" where pd.processo = :").append(QUERY_PARAMETER_PROCESSO);
        jpql.append(" and pd.documentoPrincipal is null ");
        jpql.append(" and pd.dataJuntada is not null ");
        jpql.append(" and pd.ativo = true ");
        jpql.append(" order by pd.dataInclusao DESC ");
        Query q = getEntityManager().createQuery(jpql.toString());
        q.setParameter(QUERY_PARAMETER_PROCESSO, processo);
        ProcessoDocumento processoDocumento = EntityUtil.getSingleResult(q);
        return processoDocumento;
    }
    
    @SuppressWarnings("unchecked")
    public List<ProcessoDocumentoBinPessoaAssinatura> getAssinaturasDocumento(
        ProcessoDocumentoBin procBin) {
        Query q = getEntityManager().createQuery(IS_DOCUMENTO_ASSINADO);
        q.setParameter(QUERY_PARAMETER_PROCESSO_DOC_BIN, procBin);
        return q.getResultList();
    }

    public ProcessoDocumento getProcessoDocumentoByProcessoTipoProcessoDocumento(
        TipoProcessoDocumento tipoProcessoDocumento, Processo processo) {
        Query q = getEntityManager().createQuery(GET_DOCUMENTO_BY_TIPO_PROCESSO);
        q.setParameter(QUERY_PARAMETER_TIPO_DOC, tipoProcessoDocumento);
        q.setParameter(QUERY_PARAMETER_PROCESSO, processo);

        ProcessoDocumento processoDocumento = EntityUtil.getSingleResult(q);

        return processoDocumento;
    }

    @SuppressWarnings("unchecked")
    public List<ProcessoDocumento> getDocumentosPorTipo(
        ProcessoTrf processoJudicial, Integer... tipos) {
        String query = "SELECT d FROM ProcessoDocumento AS d " +
            "	WHERE d.processo = :proc " +
            "	AND d.tipoProcessoDocumento.idTipoProcessoDocumento IN (:tipos) ";
        Query q = entityManager.createQuery(query);
        q.setParameter("proc", processoJudicial.getProcesso());
        q.setParameter("tipos", Util.isEmpty(Arrays.asList(tipos))?null:Arrays.asList(tipos));

        return q.getResultList();
    }

    public ProcessoDocumento getUltimoDocumentoPorTipoNaoJulgado(Integer idProcesso, Integer idTipoProcessoDocumento) {
        ProcessoDocumento retorno = null;
        StringBuilder sbSessao = new StringBuilder("SELECT pd.idProcessoDocumento FROM SessaoProcessoDocumento spd ");
        sbSessao.append(" JOIN spd.processoDocumento pd ");
        sbSessao.append(" JOIN spd.sessao s ");
        sbSessao.append(" JOIN s.sessaoPautaProcessoTrfList sppt ");
        sbSessao.append(" WHERE sppt.dataExclusaoProcessoTrf = null and sppt.situacaoJulgamento = 'JG' AND sppt.processoTrf.idProcessoTrf = :idProcessoTrf ");
        sbSessao.append(" AND pd.tipoProcessoDocumento.idTipoProcessoDocumento = :idTipoProcessoDocumento ");
        sbSessao.append(" AND pd.idProcessoDocumento is not null AND (s.dataRealizacaoSessao is not null OR sppt.julgamentoFinalizado = false ) ");

        StringBuilder sb = new StringBuilder("SELECT d FROM ProcessoDocumento AS d ");
        sb.append(" WHERE d.processo.idProcesso = :idProcessoTrf ");
        sb.append(" AND d.tipoProcessoDocumento.idTipoProcessoDocumento = :idTipoProcessoDocumento ");
        sb.append(" AND d.idProcessoDocumento NOT IN (");
        sb.append(sbSessao);
        sb.append(") ORDER BY d.idProcessoDocumento DESC ");
        
        Query q = entityManager.createQuery(sb.toString());
        q.setMaxResults(1);
        q.setParameter("idTipoProcessoDocumento", idTipoProcessoDocumento);
        q.setParameter("idProcessoTrf", idProcesso);
        
        try {
            retorno = (ProcessoDocumento) q.getSingleResult();
        } catch (NoResultException e) {
            
        }
        
        return retorno;
    }

	@SuppressWarnings("unchecked")
    public List<ProcessoDocumento> findByProcessoDocumentoBin(
        ProcessoDocumentoBin procBIN) {
        String queryString = "select o from ProcessoDocumento o where " +
            "o.processoDocumentoBin = :procBIN";
        Query query = getEntityManager().createQuery(queryString);
        query.setParameter("procBIN", procBIN);

        return query.getResultList();
    }
	
	@SuppressWarnings("unchecked")
	public ProcessoDocumento findByProcessoDocumento(ProcessoDocumento proc) {
		String queryString = "select o from ProcessoDocumento o where " + "o.idProcessoDocumento = :proc";
		Query query = getEntityManager().createQuery(queryString);
		query.setParameter("proc", proc.getIdProcessoDocumento());

		return (ProcessoDocumento) query.getSingleResult();
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> getDocumentosPorOrdem(Integer idProcesso, Integer numeroOrdem) {
        String query = "SELECT d FROM ProcessoDocumento AS d " +
            "	WHERE d.processo.idProcesso = :idProcesso " +
            "	AND d.numeroOrdem = :numeroOrdem " +
            "	ORDER BY d.numeroOrdem ";
        Query q = entityManager.createQuery(query);
        q.setParameter("idProcesso", idProcesso);
        q.setParameter("numeroOrdem", numeroOrdem);
        List<ProcessoDocumento> q2 = q.getResultList();

        return q.getResultList();
    }

	@SuppressWarnings("unchecked")
    public List<ProcessoDocumento> getDocumentosPorNumero(Integer idProcesso,
        String numeroDocumento) {
        String query = "SELECT d FROM ProcessoDocumento AS d " +
            "	WHERE d.processo.idProcesso = :idProcesso " +
            "	AND d.numeroDocumento = :numeroDocumento) ";
        Query q = entityManager.createQuery(query);
        q.setParameter("idProcesso", idProcesso);
        q.setParameter("numeroDocumento", numeroDocumento);

        return q.getResultList();
    }

    public ProcessoDocumento getDocumentoPendente(
        ProcessoTrf processoJudicial, Papel papel) {
        String query = "SELECT d FROM ProcessoDocumento AS d " +
            "	WHERE d.processo.idProcesso = :idProcesso " +
            "	AND d.papel = :papel " +
            "	AND d.processoDocumentoBin.binario = false " +
            "	AND d.processoDocumentoBin.signatarios IS EMPTY  " +
            "	ORDER BY d.dataInclusao";
        Query q = entityManager.createQuery(query);
        q.setParameter("idProcesso", processoJudicial.getIdProcessoTrf());
        q.setParameter("papel", papel);
        q.setMaxResults(1);

        try {
            return (ProcessoDocumento) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
	 * Método responsável por lista os documentos pendentes de
	 * leitura/apreciação de um processo
	 * 
	 * @param processoJudicial
	 *            {@link ProcessoTrf} a ser pesquisado os documentos
	 * 
	 * @return {@link List} de {@link ProcessoDocumento} não lidos/apreciados
	 */
    @SuppressWarnings("unchecked")
	public List<ProcessoDocumento> listDocumentosNaoLidos(
			ProcessoTrf processoJudicial) {
    	
		String query = "SELECT d FROM ProcessoDocumento AS d "
				+ "	WHERE d.processo.idProcesso = :idProcesso "
				+ "	AND d.ativo IS true "
				+ " AND d.dataJuntada IS NOT NULL "
				+ " AND d.dataInclusao >= d.processoTrf.dataDistribuicao "
				+ " AND d.lido IS false "
				+ "	AND NOT EXISTS (SELECT pdl.processoDocumento FROM ProcessoDocumentoLido AS pdl WHERE pdl.processoDocumento.idProcessoDocumento = d.idProcessoDocumento) ";

		Query q = entityManager.createQuery(query);
		q.setParameter("idProcesso", processoJudicial.getIdProcessoTrf());

		return q.getResultList();    	
    }
    
    @SuppressWarnings("unchecked")
    public List<ProcessoDocumento> listProcessoDocumentoMagistrado(
        ProcessoTrf processoTrf) {
        String query = ProcessoDocumentoHome.instance()
                                            .getConsultaDocumentosProcesso();
        query += "  and o.processoDocumento.processo.idProcesso = #{processoHome.instance.idProcesso}";

        Query q = getEntityManager().createQuery(query);

        return q.getResultList();
    }

    /**
     *  [PJEII-4112] Antonio Lucas: Só trás os documentos assinados
     *  Usado para exportar os documentos do processo direto do editor estruturado
     */
    @SuppressWarnings("unchecked")
    public List<ProcessoDocumento> listProcessoDocumentoAssinadosMagistrado(
        ProcessoTrf processoTrf) {
        String query = ProcessoDocumentoHome.instance()
                                            .getConsultaDocumentosProcesso();
        query += "  and o.processoDocumento.processo.idProcesso = #{processoHome.instance.idProcesso} ";
        query += "  and o.processoDocumentoBin.dataAssinatura is not null ";

        Query q = getEntityManager().createQuery(query);

        return q.getResultList();
    }

    @SuppressWarnings("unchecked")
    public ProcessoDocumento getUltimoAcordaoPublicadoDejt(
        ProcessoTrf processoTrf) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("	pe.id_processo_documento ");
        sql.append("FROM ");
        sql.append("	tb_processo_expediente pe, ");
        sql.append("	tb_jt_mtra_diario_eletronico mde ");
        sql.append(
            "WHERE pe.id_processo_expediente = mde.id_processo_expediente ");
        sql.append(
            "  AND pe.id_tipo_processo_documento = :idTipoProcessoDocumento ");
        sql.append("  AND pe.id_processo_trf = :idProcessoTrf ");
        sql.append(
            "  AND mde.dt_envio = (SELECT MAX(m.dt_envio) FROM tb_jt_mtra_diario_eletronico m ");
        sql.append(
            "					   WHERE m.id_processo_expediente = mde.id_processo_expediente) order by 1");

        Query query = getEntityManager().createNativeQuery(sql.toString());
        query.setParameter("idTipoProcessoDocumento",
            ParametroUtil.instance().getTipoProcessoDocumentoAcordaoDEJT()
                         .getIdTipoProcessoDocumento());
        query.setParameter("idProcessoTrf", processoTrf.getIdProcessoTrf());

        List<Object> resultList = query.getResultList();
        Integer idProcessoDocumento = 0;

        if ((resultList != null) && (resultList.size() > 0)) {
            idProcessoDocumento = (Integer) resultList.get(resultList.size() -
                    1);
        }

        ProcessoDocumento processoDocumento = find(idProcessoDocumento);

        return processoDocumento;
    }

    @SuppressWarnings("unchecked")
    public boolean isAcordaoPublicado(ProcessoDocumento processoDocumento) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("	pe.id_processo_documento ");
        sql.append("FROM ");
        sql.append("	tb_processo_expediente pe, ");
        sql.append("	tb_jt_mtra_diario_eletronico mde, ");
        sql.append("    tb_processo_documento pd ");
        sql.append(
            "WHERE pe.id_processo_expediente = mde.id_processo_expediente ");
        sql.append("  AND pe.id_processo_documento = pd.id_processo_documento ");
        sql.append(
            "  AND pe.id_tipo_processo_documento = :idTipoProcessoDocumento ");
        sql.append("  AND pe.id_processo_trf = :idProcessoTrf ");
        sql.append(
            "  AND pd.id_processo_documento_bin = :idProcessoDocumentoBin ");

        Query query = getEntityManager().createNativeQuery(sql.toString());
        query.setParameter("idTipoProcessoDocumento",
            ParametroUtil.instance().getTipoProcessoDocumentoAcordaoDEJT()
                         .getIdTipoProcessoDocumento());
        query.setParameter("idProcessoTrf",
            processoDocumento.getProcesso().getIdProcesso());
        query.setParameter("idProcessoDocumentoBin",
            processoDocumento.getProcessoDocumentoBin()
                             .getIdProcessoDocumentoBin());

        List<Object> resultList = query.getResultList();
        Integer idProcessoDocumento = 0;

        if ((resultList != null) && (resultList.size() > 0)) {
            idProcessoDocumento = (Integer) resultList.get(0);
        }

        processoDocumento = find(idProcessoDocumento);

        return processoDocumento != null;
    }
    
    /**
	 * Obtém todos os documentos do processo que são do tipo Sentença
	 * @param processo
	 * @return
	 */
	public List<ProcessoDocumento> listarSentencas(ProcessoTrf processo) {
        TipoProcessoDocumento sentenca = ParametroUtil.instance()
                                                      .getTipoProcessoDocumentoSentenca();

        return getDocumentosPorTipo(processo,
            sentenca.getIdTipoProcessoDocumento());
	}
	
	/**
	 * Obtém o último documento de um processo, de um determinado tipo, e que
	 * tenha sido assinado após uma certa data.
	 * 
	 * @param processo Processo de qual os documentos serão retornados
	 * @param tipo Tipo do documento
	 * @param apos O documento deve ter sido assinados após esta data
	 * @return
	 */
    public ProcessoDocumento getUltimoDocumento(ProcessoTrf processo,
        TipoProcessoDocumento tipo, Date apos) {
        StringBuilder query = new StringBuilder(
                "select pd from ProcessoDocumento pd ");
		query.append(" where pd.processoDocumentoBin.dataAssinatura = ");
        query.append(
            " (select max(pd2.processoDocumentoBin.dataAssinatura) from ProcessoDocumento pd2 where pd2.processo = :processo ");
		query.append(" and pd2.tipoProcessoDocumento = :tipo ");
		
		if(apos != null) {
            query.append(
                " and pd2.processoDocumentoBin.dataAssinatura >= :data ");
		}
		
		query.append(" ) ");
		query.append(" and pd.tipoProcessoDocumento = :tipo ");
		query.append(" and pd.processo = :processo ");
		
		Query q = getEntityManager().createQuery(query.toString());
		q.setParameter("processo", processo.getProcesso());
		q.setParameter("tipo", tipo);
		
		if(apos != null) {
			q.setParameter("data", apos);
		}
		
		return (ProcessoDocumento) EntityUtil.getSingleResult(q);
	}
	
	/**
	 * Obtém a última sentença, baseado em data, do processo.
	 * @param processo
	 * @return 
	 */
	public ProcessoDocumento getUltimaSentenca(ProcessoTrf processo) {
        TipoProcessoDocumento sentenca = ParametroUtil.instance()
                                                      .getTipoProcessoDocumentoSentenca();

		return getUltimoDocumento(processo, sentenca, null);
	}
	
	/**
	 * Obtém todos os documentos de um processo que são de determinados tipos
	 * e que foram assinados após uma certa data.
	 * 
	 * @param processoJudicial Processo de qual os documentos serão retornados
	 * @param dataAssinatura Os documentos devem ter sido assinados após esta data
	 * @param tipos tipos de documentos
	 * @return Documentos que atendam as restrições
	 */
	@SuppressWarnings("unchecked")
    public List<ProcessoDocumento> getDocumentosPorTipoAssinadosApos(
        ProcessoTrf processoJudicial, Date dataAssinatura, Integer... tipos) {
		String query = "SELECT pd FROM ProcessoDocumento AS pd " +
			"	WHERE pd.processo = :proc " +
			"	AND pd.tipoProcessoDocumento.idTipoProcessoDocumento IN (:tipos) " +
			"	AND pd.processoDocumentoBin.dataAssinatura >= :dataAssinatura ";
		Query q = entityManager.createQuery(query);
		q.setParameter("proc", processoJudicial.getProcesso());
        List<Integer> tiposList = Arrays.asList(tipos);
		q.setParameter("tipos", Util.isEmpty(tiposList)?null:tiposList);
		q.setParameter("dataAssinatura", dataAssinatura);
		return q.getResultList();
	}
	
	/**
	 * Método que retorna todos os documentos do processo que foram assinados por um magistrado
	 * @param idProcesso
	 * 			Identificador do processo que deseja trazer os documentos
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> getDocumentosAssinadosPorMagistradosBy(Integer idProcesso){
		StringBuffer sb = new StringBuffer();
		sb.append("select pd from ProcessoDocumento pd ");
		sb.append("inner join pd.processoDocumentoBin pdb ");
		sb.append("where exists (select 1 from ProcessoDocumentoBinPessoaAssinatura pdba ");
		sb.append("				  where pdba.processoDocumentoBin.idProcessoDocumentoBin = pdb.idProcessoDocumentoBin ");
		sb.append("					and exists (select 1 from PessoaMagistrado pm ");
		sb.append("		          				 where pm.idUsuario = pdba.pessoa.idUsuario) ");
		sb.append("		        ) ");
		sb.append("  and pd.processo.idProcesso = :idProcesso ");
		sb.append("order by pd.dataInclusao desc ");
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idProcesso", idProcesso);
		
		List<ProcessoDocumento> documentos = q.getResultList();
		return documentos;
	}
	
	/**
	 * Método que retorna todos os documentos do processo que foram assinados por um advogado ou por um procurador
	 * @param idProcesso
	 * 			Identificador do processo que deseja trazer os documentos
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> getDocumentosAssinadosPorAdvogadosOuProcuradoresBy(Integer idProcesso){
		StringBuffer sb = new StringBuffer();
		sb.append("select pd from ProcessoDocumento pd ");
		sb.append("inner join pd.processoDocumentoBin pdb ");
		sb.append("where exists (select 1 from ProcessoDocumentoBinPessoaAssinatura pdba ");
		sb.append("				  where pdba.processoDocumentoBin.idProcessoDocumentoBin = pdb.idProcessoDocumentoBin ");
		sb.append("	 				and (exists (select 1 from PessoaAdvogado pa ");
		sb.append("		           		  		  where pa.idUsuario = pd.usuarioInclusao.idUsuario) ");
		sb.append("	 	  					or ");
		sb.append("	 	  				 exists (select 1 from PessoaProcurador pp ");
		sb.append("		           				  where pp.idUsuario = pd.usuarioInclusao.idUsuario) ");
		sb.append("		           		) ");
		sb.append("		        ) ");
		sb.append("  and pd.processo.idProcesso = :idProcesso ");
		sb.append("order by pd.dataInclusao desc ");
		
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter("idProcesso", idProcesso);
		
		List<ProcessoDocumento> documentos = q.getResultList();
		return documentos;
	}
    

    /**
     * [PJEII-6117]
     * 
     * @param idProcesso Id do Processo
     * @param idPessoaParte 
     * @return Lista de ProcessoDocumento que estão assinados e não possuem ProcessoExpediente 
     * associado (São os casos de Intimação, Citação, Comunicação, etc), ou seja, não foram
     * preparados pela atividade de fluxo Preparar Ato de Comunicação.
     * Nesta consulta também são listados os que tem ProcessoExpediente, mas não tem partes 
     * associadas, ou seja, são os ProcessoExpediente's criados em publicações que não são
     * as mesmas das que foram citadas no parágrafo anterior.
     * 
     * [PJEII-12221]
     * Alteração do método. Adicionado o parâmtero idPessoParte. 
     * Agora todos os expedientes criados e enviados para o DJE terão ProcessoExpediente e ProcessoParteExpediente, até 
     * mesmo a simples publicação de processo, que antes não tinha parte. Esta consulta é chamada na tela de publicação de processo.
     * Como negocialmente não há parte na simples publicação ao DJE, a parte é uma pessoa criada para este fim, uma pessoa pública.
     * O id dessa pessoa pública fica armazenado em um parâmetro de sistema.
     * É necessário informar este id na query HQL para a correta lógica da tela, senão não seria possível a re-publicação pois 
     * o expediente sairia da query. Desta forma o id da pessoa é passado como parâmetro na consulta.
     * 
     * A consulta lista todos os documentos do processo que estão assinados, que ainda não foram enviados ao DJE, ou que já foram 
     * enviados e tem o idPessoaParte como parte do ProcessoarteExpediente.
     * 
     */
	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> getDocumentosAssinadosSemProcessoExpediente(Integer idProcesso, Integer idPessoaParte){
		StringBuilder sb = new StringBuilder();
		sb.append("select pd from ProcessoDocumento pd ");
		sb.append("inner join pd.processoDocumentoBin pdb ");
		sb.append("where exists (select 1 from ProcessoDocumentoBinPessoaAssinatura pdba ");
		sb.append("				  where pdba.processoDocumentoBin.idProcessoDocumentoBin = pdb.idProcessoDocumentoBin ");
		sb.append("		        ) ");
 		sb.append("and not exists (select 1 from ProcessoExpediente pe1 ");
 		sb.append("				  where pe1.processoDocumento.idProcessoDocumento = pd.idProcessoDocumento ");
 		sb.append("				    and pe1.documentoExistente = true ");
 		sb.append(" ) ");		
		sb.append("and (not exists (select 1 from ProcessoDocumentoExpediente pde ");
		sb.append("				  where pde.processoDocumento.idProcessoDocumento = pd.idProcessoDocumento ");
		sb.append("		            ) ");
		sb.append("   or ( exists (select 1 from ProcessoDocumentoExpediente pde, ProcessoExpediente pe, ProcessoParteExpediente ppe ");
		sb.append("                where pde.processoDocumento.idProcessoDocumento = pd.idProcessoDocumento ");
		sb.append("                and pde.processoExpediente.idProcessoExpediente = pe.idProcessoExpediente ");
		sb.append("                and pe.idProcessoExpediente = ppe.processoExpediente.idProcessoExpediente ");
		sb.append("                and ppe.pessoaParte.idUsuario = :idPessoaParte ");
		sb.append("                ) ) ");
		
		
		sb.append("	   ) ");
		sb.append("  and pd.processo.idProcesso = :idProcesso ");
		sb.append("order by pd.dataInclusao desc ");
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idProcesso", idProcesso);
		q.setParameter("idPessoaParte", idPessoaParte);
		
		List<ProcessoDocumento> documentos = q.getResultList();
		return documentos;
	}
	
	/**
	 * 
	 * @param idProcesso
	 * @param idPessoaParte
	 * @return
	 * 
	 * [PJEII-12221] 
	 * A consulta retorna todos os documentos do processo que já foram, em algum momento, 
	 * enviados ao DJE como simples publicação, ou seja, documentos que estão assinados, 
	 * e que possuem ProcessoExpediente e ProcessoParteExpediente sendo o id da pessoa da parte igual ao parâmetro informado.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> getDocumentosAssinadosJaEnviadosParaPublicacaoDeProcesso(Integer idProcesso, Integer idPessoaParte){
		StringBuilder sb = new StringBuilder();
		sb.append("select pd from ProcessoDocumento pd ");
		sb.append("inner join pd.processoDocumentoBin pdb ");
		sb.append("where exists (select 1 from ProcessoDocumentoBinPessoaAssinatura pdba ");
		sb.append("				  where pdba.processoDocumentoBin.idProcessoDocumentoBin = pdb.idProcessoDocumentoBin ");
		sb.append("		        ) ");
		sb.append("and            ");
		sb.append("    exists (select 1 from ProcessoDocumentoExpediente pde, ProcessoExpediente pe, ProcessoParteExpediente ppe ");
		sb.append("                  where pde.processoDocumento.idProcessoDocumento = pd.idProcessoDocumento ");
		sb.append("                  and pde.processoExpediente.idProcessoExpediente = pe.idProcessoExpediente ");
		sb.append("                  and pe.idProcessoExpediente = ppe.processoExpediente.idProcessoExpediente ");
		sb.append("                  and ppe.pessoaParte.idUsuario = :idPessoaParte ");
		sb.append("                  )  ");
		sb.append("  and pd.processo.idProcesso = :idProcesso ");
		sb.append("order by pd.dataInclusao desc ");
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idProcesso", idProcesso);
		q.setParameter("idPessoaParte", idPessoaParte);
		
		List<ProcessoDocumento> documentos = q.getResultList();
		return documentos;
	}
	

	public Integer contagemDocumentos(ProcessoTrf processo, boolean incluirBinarios, boolean incluirComAssinaturaInvalida, TipoProcessoDocumento... tipos) {
        StringBuilder query = new StringBuilder(
        		"SELECT COUNT(DISTINCT(d.idProcessoDocumento)) " +
        		"		FROM ProcessoDocumento AS d " +
        		"			JOIN d.processoDocumentoBin AS doc " +
        		"			JOIN doc.signatarios AS sigs " +
        		"	WHERE d.processo.idProcesso = :id " +
        		"		AND d.ativo = true ");
        if(!incluirBinarios){
        	query.append("	AND (doc.modeloDocumento IS NULL OR doc.modeloDocumento = '') ");
        }
        Query q = null;
        if(tipos != null && tipos.length > 0){
        	query.append("	AND doc.tipoProcessoDocumento IN (:tipos) ");
        	q = entityManager.createQuery(query.toString());
        	q.setParameter("tipos", Arrays.asList(tipos));
        }else{
        	q = entityManager.createQuery(query.toString());
        }
        q.setParameter("id", processo.getProcesso().getIdProcesso());
        
        Number cont = (Number) q.getSingleResult();
        return cont.intValue();
	}
	
	/**
	 * Retorna todos os documentos do processo que não possuem assinatura exceto 
	 * o tipo passado por parâmetro.
	 * 
	 * @param processo
	 * 			Processo que deseja trazer os documentos.
	 * @param tipo
	 * 			Tipo que será excluído da consulta.
	 * @return documentos não assinados.
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> getDocumentosNaoAssinadosExcetoTipo(Processo processo, Integer tipo){
		StringBuilder hql = new StringBuilder();
		hql.append("select pd from ProcessoDocumento pd ");
		hql.append("	left join pd.processoDocumentoBin pdb ");
		hql.append("	left join fetch pd.tipoProcessoDocumento tpd ");
		hql.append("where ");
		hql.append("	not exists ( ");
		hql.append("		select 1 from ProcessoDocumentoBinPessoaAssinatura pdba ");
		hql.append("		where pdba.processoDocumentoBin.idProcessoDocumentoBin = pdb.idProcessoDocumentoBin ");
		hql.append("	) and ");
		hql.append("	pd.ativo=true and ");
		hql.append("	pd.processo = :processo and ");
		hql.append("	tpd.idTipoProcessoDocumento <> :tipo ");
		hql.append("order by pd.numeroOrdem ");
		
		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("processo", processo);
		query.setParameter("tipo", tipo);
		
		return query.getResultList();
	}


	@SuppressWarnings("unchecked")
	/**
	 * Retorna a lista de documentos não lidos do processo indicado,
	 * podendo filtrar por tipos de documentos e pelo papel de quem os juntou. 
	 * @param processo - Processo cujos documentos não lidos serão selecionados
	 * @param papeis - Lista de papéis que terão seus documentos juntados considerados nesta pesquisa. Se informar null, todos são considerados.
	 * @param tipos - Lista de tipos de documentos a serem considerados nesta pesquisa. Se informar null, todos são considerados. 
	 * @return Lista de ProcessoDocumento não lidos do processo.
	 */
	public List<ProcessoDocumento> getDocumentosNaoLidos(ProcessoTrf processo, Papel[] papeis, TipoProcessoDocumento[] tipos) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT pd ");
		query.append("FROM ");
		query.append("	ProcessoDocumento pd ");
		query.append("	JOIN pd.processoDocumentoBin bin ");
		query.append("	JOIN bin.signatarios sigs ");
		query.append("WHERE");
		query.append("	pd.lido = false ");
		query.append("	and pd.processoTrf.idProcessoTrf = :id ");
		query.append("	and pd.ativo = true ");
		query.append("	and pd.dataJuntada is not null ");		
		query.append("	and pd.dataInclusao > pd.processoTrf.dataDistribuicao ");
		query.append("	and pd.dataJuntada > pd.processoTrf.dataAutuacao ");
		query.append("	and bin.binario = false ");		
		if (tipos != null && tipos.length > 0){
			query.append("	and pd.tipoProcessoDocumento in (:tipos) ");
		}
		if (papeis != null && papeis.length > 0){
			query.append(" and pd.papel in (:papeis) ");
		}
		query.append("ORDER BY pd.dataInclusao ");
		
		Query q = entityManager.createQuery(query.toString());
		if (tipos != null && tipos.length > 0){
			q.setParameter("tipos", Arrays.asList(tipos));
		}
		if (papeis != null && papeis.length > 0){
			q.setParameter("papeis", Arrays.asList(papeis));
		}
		q.setParameter("id", processo.getIdProcessoTrf());		
		return q.getResultList();
	}
	
	public Boolean isHaDocumentosNaoLidosNoProcesso(ProcessoTrf processo, Papel[] papeis, TipoProcessoDocumento[] tipos){
		if(Authenticator.isUsuarioExterno()){
			return Boolean.FALSE;
		} else {
			return getDocumentosNaoLidos(processo, papeis, tipos).size() > 0;
		}
	}

	/** 
	 * Recupera o documento da instancia de origem passado pela remessa
	 * @param idProcesso Identificador do processo.
	 * @param identificador utilizado na instancia de origem
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public ProcessoDocumento recuperarDocumentoPorIdentificadorInstanciaOrigem(Integer idProcesso, String identificador) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT d FROM ProcessoDocumento AS d ");
		query.append("WHERE ");
		query.append("d.idInstanciaOrigem = :identificador AND ");
		query.append("d.processo.idProcesso = :idProcesso ");
		
        Query q = entityManager.createQuery(query.toString());
        q.setParameter("identificador", identificador);
        q.setParameter("idProcesso", idProcesso);
        
        //corrige o problema que ocorre quando um processo já foi remetido mais de uma vez, e seus documentos foram duplicados
        List<ProcessoDocumento> result = q.getResultList();
        if (!result.isEmpty())
        	return result.get(0);
        return null;
    }
    /**
     * @author t317549 - Antonio Francisco Osorio Jr / TJDFT
     * [ISSUE-18603] Lista todos documentos relacionados a um processo, exceto para determinado tipo que estejam ativos.
     * @param processoTrf
     * @param idTipoDocumentoExceto
     * @return
     */
	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> findAllExceto(ProcessoTrf processoTrf,
			Integer idTipoDocumentoExceto) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT d FROM ProcessoDocumento AS d ");
		sql.append( "	WHERE d.processo.idProcesso = :idProcesso ");
		sql.append("	AND d.tipoProcessoDocumento.idTipoProcessoDocumento <> :tipoProcessoDocumento  AND d.ativo = :ativo ");
	    Query q = entityManager.createQuery(sql.toString());
	    q.setParameter("idProcesso", processoTrf.getIdProcessoTrf());
	    q.setParameter("tipoProcessoDocumento", idTipoDocumentoExceto);
	    q.setParameter("ativo",true);
	    List<ProcessoDocumento> resultado = q.getResultList();
		return resultado;
	}
	/**
	 * Método responsável por realizar a verificação se no processo existe um 
	 * documento de "PETIÇÃO INICIAL" assinada.
	 * 
	 * @param processoTrf
	 * @return true se existe uma petição inicial assinada no processo
	 */
	@SuppressWarnings("unchecked")
	public boolean isHaDocumentoPeticaoInicialAssinadaNoProcesso(ProcessoTrf processoTrf){
		Integer tipoPeticaoInicial = processoTrf.getClasseJudicial().getTipoProcessoDocumentoInicial().getIdTipoProcessoDocumento();
		boolean temPeticaoAssinada = false;
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT d FROM ProcessoDocumento AS d ");
		sql.append( "	WHERE d.processo.idProcesso = :idProcesso ");
		sql.append("	AND d.tipoProcessoDocumento.idTipoProcessoDocumento = :tipoProcessoDocumento  AND d.ativo = :ativo ");
	    Query q = entityManager.createQuery(sql.toString());
	    q.setParameter("idProcesso", processoTrf.getIdProcessoTrf());
	    q.setParameter("tipoProcessoDocumento",tipoPeticaoInicial);
	    q.setParameter("ativo",true);
	    List<ProcessoDocumento> resultado = q.getResultList();
	    DocumentoJudicialService documentoJudicialService = ComponentUtil.getDocumentoJudicialService();
	    for(ProcessoDocumento documento : resultado){
		    if(documento.getTipoProcessoDocumento().getIdTipoProcessoDocumento() == tipoPeticaoInicial){
		    	temPeticaoAssinada = documentoJudicialService.temAssinatura(documento);
		    }
	    }
	    
		return temPeticaoAssinada;
	}
	/**
	 * Retorna a lista de processo documento vinculado.
	 * 
	 * 
	 * @param ProcessoDocumento
	 * @author Eduardo Paulo
	 * @since 10/06/2015
	 * @return Uma lista de ProcessoDocumento
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> getDocumentosVinculados(ProcessoDocumento processoDocumento){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT d FROM ProcessoDocumento AS d ");
		sql.append( "	WHERE d.documentoPrincipal = :documentoPrincipal ");
		sql.append("    ORDER BY d.numeroOrdem ");
	    Query q = entityManager.createQuery(sql.toString());
	    q.setParameter("documentoPrincipal", processoDocumento);
	    List<ProcessoDocumento> resultado = q.getResultList();
		return resultado;
	}
	
	
	/**
	 * Recupera a lista de documentos ativos anexos pelo componente de upload ordenados pela ordem de inclusão dos mesmos.
	 * 
	 * @param processo
	 * @return lista com os documentos ativos anexos por upload
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> getListaDocumentosAnexosPorUpload(ProcessoTrf processo){
		ArrayList<String> listaMimeTypes = new ArrayList<String>(
				Arrays.asList(ComponentUtil.getMimeUtilChecker().getDefaultMimes().split(",")));
		
		ArrayList<Integer> listaTipoProcessoDocumentoIndesejados = new ArrayList<Integer>(2);
		listaTipoProcessoDocumentoIndesejados.add(Integer.parseInt(ParametroUtil.getParametro(Parametros.ID_TIPO_PROCESSO_DOCUMENTO_PETICAO_INICIAL)));
		listaTipoProcessoDocumentoIndesejados.add(ParametroUtil.instance().getIdTipoDocumentoProtocoloDistribuicao());
		
		StringBuilder query = new StringBuilder("SELECT d FROM ProcessoDocumento AS d  ");
		query.append("inner join d.processoDocumentoBin as pdb ");
		query.append("WHERE d.processo = :processo ");
		query.append("AND d.ativo = :ativo ");
		query.append("AND d.numeroOrdem is not null ");
		query.append("AND d.tipoProcessoDocumento.idTipoProcessoDocumento not in (:idsTipoProcessoDocumentoIndesejados) ");
		query.append("AND pdb.extensao in (:listaExtensao) ");
		query.append("ORDER BY d.numeroOrdem ");
		
		Query q = entityManager.createQuery(query.toString());
		q.setParameter("processo", processo);
		q.setParameter("ativo", Boolean.TRUE);
		q.setParameter("idsTipoProcessoDocumentoIndesejados", listaTipoProcessoDocumentoIndesejados);
		q.setParameter("listaExtensao", listaMimeTypes);
		
		return q.getResultList();
	}
	
	/**
	 * Método responsável por recuperar todos os documentos não juntados que foram criados em alguma tarefa de fluxo.
	 * 
	 * @return Lista de documentos não assinados que foram criados em alguma tarefa de fluxo.
	 */
	public List<ProcessoDocumento> recuperarDocumentosNaoJuntadosDeAtividadeEspecifica(Integer idProcesso) {
		return this.recuperarDocumentosNaoJuntadoDeAtividadeEspecifica(idProcesso, null);
	}
	
	/**
	 * Método responsável por recuperar todos os documentos não juntados criados: 
	 * - para serem tratados em tarefa especifica, por exemplo: criados via fluxo, documentos de diligência
	 * estes documentos não podem ser listados nos editores convencionais de atividades que não sejam as que o criaram
	 * @param idProcesso
	 * @param idDocumento
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<ProcessoDocumento> recuperarDocumentosNaoJuntadoDeAtividadeEspecifica(Integer idProcesso, Integer idDocumento) {
		if(idProcesso == null && idDocumento == null) {
			return null;
		}
		
		StringBuilder jpql = new StringBuilder("SELECT p FROM ProcessoDocumento AS p ")
				.append(" WHERE p.exclusivoAtividadeEspecifica = TRUE")
				.append(" AND p.dataJuntada IS NULL ");
		if(idProcesso != null) {
			jpql.append(" AND p.processo.idProcesso = :idProcesso");
		}
		
		if(idDocumento != null) {
			jpql.append(" AND p.idProcessoDocumento = :idDocumento");
		}

		Query query = entityManager.createQuery(jpql.toString());
		if(idProcesso != null) {
			query.setParameter("idProcesso",idProcesso);
		}
		if(idDocumento != null) {
			query.setParameter("idDocumento",idDocumento);
		}
		return query.getResultList();
	}
	
	public boolean verificaDocumentoDeAtividadeEspecifica(Integer idDocumento) {
		List<ProcessoDocumento> listaDocumento = this.recuperarDocumentosNaoJuntadoDeAtividadeEspecifica(null, idDocumento);
		
		return CollectionUtilsPje.isNotEmpty(listaDocumento);
	}
	
	/**
 	 * Recupera os documentos pendente de assinatura paginados
 	 * @param ProcessoDocumentoConsultaNaoAssinadoVO
 	 * @return PaginatedDataModel
 	 */
 	public PaginatedDataModel<ProcessoDocumento> recuperarDocumentosNaoAssinados(ProcessoDocumentoConsultaNaoAssinadoVO consultaDocnaoAssinado) {
 		HashMap<String, Object> parameters = new HashMap<String, Object>();
 		
 		StringBuffer jpql = new StringBuffer("")
 				.append(" SELECT processoDocumento FROM ProcessoDocumento processoDocumento " )
 				.append(" INNER JOIN processoDocumento.processoDocumentoBin bin ")
 				.append(" WHERE processoDocumento.idJbpmTask is null ")
 				.append(" AND processoDocumento.processoTrf.inOutraInstancia = false ")
 				.append(" AND processoDocumento.dataJuntada is null ")
 				.append(" AND processoDocumento.ativo = true ")
 				.append(" AND processoDocumento.dataExclusao is null ")
 				.append(" and not exists(select a from ProcessoDocumentoBinPessoaAssinatura a where a.processoDocumentoBin = bin) ")
				.append(" and (processoDocumento.documentoPrincipal is null or (processoDocumento.documentoPrincipal is not null and ")
				.append(" exists (select pd from ProcessoDocumento pd where pd = processoDocumento.documentoPrincipal and pd.dataJuntada is not null))) ");  
 		
		if (consultaDocnaoAssinado!=null && consultaDocnaoAssinado.isDocumentosCriadosApenasPeloUsuario()){
			jpql.append(" and processoDocumento.usuarioInclusao.idUsuario = :idUsuario ");
			jpql.append(" and exists (select tpdp from TipoProcessoDocumentoPapel tpdp ")
				.append(" where tpdp.tipoProcessoDocumento = processoDocumento.tipoProcessoDocumento ")
				.append(" and tpdp.papel.idPapel = :idPapelAtual) ");
			parameters.put("idUsuario", Authenticator.getUsuarioLogado().getIdUsuario());
			parameters.put("idPapelAtual", Authenticator.getIdPapelAtual());
			
		}else{
			jpql.append(" and (processoDocumento.localizacao.idLocalizacao = :idLocalizacao ")
				.append(" or processoDocumento.usuarioInclusao.idUsuario = :idUsuario )");
			parameters.put("idLocalizacao", Authenticator.getIdLocalizacaoFisicaAtual());
			parameters.put("idUsuario", Authenticator.getUsuarioLogado().getIdUsuario());
		}
 			
 		if (consultaDocnaoAssinado!=null){
 			if (StringUtil.isNotEmpty(consultaDocnaoAssinado.getNomeParte())){
 				jpql.append(" and exists ( ")
 					.append(" SELECT 1 ")
 					.append(" FROM ProcessoParte pp ")
 					.append(" JOIN pp.pessoa p ")
 					.append(" WHERE pp.processoTrf = processoDocumento.processoTrf ")
 					.append(" AND lower(to_ascii(p.nome)) LIKE to_ascii(:nomeParte)) ");
 				parameters.put("nomeParte", String.format("%%%s%%", consultaDocnaoAssinado.getNomeParte().toLowerCase()));
 			}
 			if (consultaDocnaoAssinado.getAssuntoTrf()!=null && consultaDocnaoAssinado.getAssuntoTrf().getIdAssuntoTrf() > 0){
 				jpql.append(" and exists ( ")
 					.append(" SELECT 1 ")
 					.append(" FROM ProcessoAssunto pa ")
 					.append(" WHERE pa.processoTrf = processoDocumento.processoTrf ")
 					.append(" AND pa.assuntoTrf.idAssuntoTrf = :idAssuntoTrf ) ");
 				parameters.put("idAssuntoTrf", consultaDocnaoAssinado.getAssuntoTrf().getIdAssuntoTrf());
 			}
 			if (StringUtil.isNotEmpty(consultaDocnaoAssinado.getCpf())){
 				jpql.append(" and exists ( ")
 					.append(" SELECT 1 ")
 					.append(" FROM ProcessoParte pp ")
 					.append(" JOIN pp.pessoa p ")
 					.append(" JOIN p.pessoaDocumentoIdentificacaoList pdi ")
 					.append(" WHERE pp.processoTrf = processoDocumento.processoTrf ")
 					.append(" AND pdi.numeroDocumento = :cpf ) ");
 				parameters.put("cpf", consultaDocnaoAssinado.getCpf());
 			}
 			if (StringUtil.isNotEmpty(consultaDocnaoAssinado.getCnpj())){
 				jpql.append(" and exists ( ")
 					.append(" SELECT 1 ")
 					.append(" FROM ProcessoParte pp ")
 					.append(" JOIN pp.pessoa p ")
 					.append(" JOIN p.pessoaDocumentoIdentificacaoList pdi ")
 					.append(" WHERE pp.processoTrf = processoDocumento.processoTrf ")
 					.append(" AND pdi.numeroDocumento = :cnpj )");
 				parameters.put("cnpj", consultaDocnaoAssinado.getCnpj());
 			}
			if (consultaDocnaoAssinado.getClasseJudicial() != null
					&& consultaDocnaoAssinado.getClasseJudicial().getIdClasseJudicial() > 0) {
 				jpql.append(" AND processoDocumento.processoTrf.classeJudicial IS NOT NULL ")
 					.append(" AND processoDocumento.processoTrf.classeJudicial.idClasseJudicial = :idClasseJudicial");
 				parameters.put("idClasseJudicial", consultaDocnaoAssinado.getClasseJudicial().getIdClasseJudicial());
 			}
			if (consultaDocnaoAssinado.getTipoProcessoDocumento() != null
					&& consultaDocnaoAssinado.getTipoProcessoDocumento().getIdTipoProcessoDocumento() > 0) {
 				jpql.append(" AND processoDocumento.tipoProcessoDocumento.idTipoProcessoDocumento = :idTipoProcessoDocumento ");
 				parameters.put("idTipoProcessoDocumento", consultaDocnaoAssinado.getTipoProcessoDocumento().getIdTipoProcessoDocumento());
 			}
 			if (StringUtil.isNotEmpty(consultaDocnaoAssinado.getNumeroProcesso())){
 				jpql.append(" AND processoDocumento.processoTrf.processo.numeroProcesso IS NOT NULL ")
 					.append(" AND processoDocumento.processoTrf.processo.numeroProcesso LIKE :numeroProcesso ");
 				parameters.put("numeroProcesso", String.format("%%%s%%", consultaDocnaoAssinado.getNumeroProcesso()));
 			}
 			if (consultaDocnaoAssinado.getInseridoInicio()!=null){
 				jpql.append(" AND cast(processoDocumento.dataInclusao as date) >= :inseridoInicio");
 				parameters.put("inseridoInicio", consultaDocnaoAssinado.getInseridoInicio());
 			}
 			if (consultaDocnaoAssinado.getInseridoFim()!=null){
 				jpql.append(" AND cast(processoDocumento.dataInclusao as date) <= :inseridoFim ");
 				parameters.put("inseridoFim", consultaDocnaoAssinado.getInseridoFim());
 			}
 			jpql.append(" order by processoDocumento.dataInclusao asc ");
 		}
 		return new PaginatedDataModel<ProcessoDocumento>(ProcessoDocumento.class, jpql, parameters,"idProcessoDocumento");
 	}

 	/**
 	 * Recupera os documentos pendente de assinatura paginados para o magistrado auxiliar
 	 * @param ConsultaDocnaoAssinado
 	 * @return List<ProcessoDocumento>
 	 */
 	public PaginatedDataModel<ProcessoDocumento> recuperarDocumentosNaoAssinadosMagistradoAuxiliar(ProcessoDocumentoConsultaNaoAssinadoVO consultaDocnaoAssinado){
 		HashMap<String, Object> parameters = new HashMap<String, Object>();
 		StringBuffer jpql = new StringBuffer("")
 				.append("select processoDocumento from ProcessoDocumento processoDocumento ")
 				.append(" where processoDocumento.ativo = true and processoDocumento.dataExclusao is null ")
 				.append(" and processoDocumento.processoTrf.inOutraInstancia = false ")
 				.append(" and (processoDocumento.documentoPrincipal is null or (processoDocumento.documentoPrincipal is not null ")   
				.append(" and exists (select pd from ProcessoDocumento pd where pd = processoDocumento.documentoPrincipal and pd.dataJuntada is not null))) ")
				.append(" and not exists(select a from ProcessoDocumentoBinPessoaAssinatura a where a.processoDocumentoBin = processoDocumento.processoDocumentoBin)");
 		
 			if (consultaDocnaoAssinado!=null && consultaDocnaoAssinado.isDocumentosCriadosApenasPeloUsuario()){
				jpql.append(" and processoDocumento.usuarioInclusao.idUsuario = :idUsuario ")
					.append(" and processoDocumento.processo in (select ptu.processoTrf.processo from ProcessoTrfUsuarioLocalizacaoMagistradoServidor ptu ")
	 				.append(" 	where ptu.processoTrf.processo.idProcesso = processoDocumento.processo.idProcesso and  ")
	 				.append(" 	ptu.usuarioLocalizacaoMagistradoServidor.usuarioLocalizacao.usuario.idUsuario = :idUsuario )")
	 				.append(" and exists (select tpdp from TipoProcessoDocumentoPapel tpdp ")
					.append(" where tpdp.tipoProcessoDocumento = processoDocumento.tipoProcessoDocumento ")
					.append(" and tpdp.papel.idPapel = :idPapelAtual) ");
 				parameters.put("idUsuario", Authenticator.getUsuarioLogado().getIdUsuario());
 				parameters.put("idPapelAtual", Authenticator.getIdPapelAtual());
			}else{
				jpql.append(" and (processoDocumento.localizacao.idLocalizacao = :idLocalizacao ")
					.append(" or processoDocumento.usuarioInclusao.idUsuario = :idUsuario )");
				parameters.put("idLocalizacao", Authenticator.getIdLocalizacaoFisicaAtual());
				parameters.put("idUsuario", Authenticator.getUsuarioLogado().getIdUsuario());
			}
 			
 			if (Authenticator.isVisualizaSigiloso()){
 				jpql.append(" and (processoDocumento.documentoSigiloso = false or processoDocumento.documentoSigiloso = true)");
 			}else{
 				jpql.append(" and processoDocumento.documentoSigiloso = false ");
 			}
 		
 			if (consultaDocnaoAssinado!=null){
 	 			if (StringUtil.isNotEmpty(consultaDocnaoAssinado.getNomeParte())){
 	 				jpql.append(" and exists ( ")
 	 					.append(" SELECT 1 ")
 	 					.append(" FROM ProcessoParte pp ")
 	 					.append(" JOIN pp.pessoa p ")
 	 					.append(" WHERE pp.processoTrf = processoDocumento.processoTrf ")
 	 					.append(" AND lower(to_ascii(p.nome)) LIKE to_ascii(:nomeParte)) ");
 	 				parameters.put("nomeParte", String.format("%%%s%%", consultaDocnaoAssinado.getNomeParte().toLowerCase()));
 	 			}
 	 			if (consultaDocnaoAssinado.getAssuntoTrf()!=null && consultaDocnaoAssinado.getAssuntoTrf().getIdAssuntoTrf() > 0){
 	 				jpql.append(" and exists ( ")
 	 					.append(" SELECT 1 ")
 	 					.append(" FROM ProcessoAssunto pa ")
 	 					.append(" WHERE pa.processoTrf = processoDocumento.processoTrf ")
 	 					.append(" AND pa.assuntoTrf.idAssuntoTrf = :idAssuntoTrf ) ");
 	 				parameters.put("idAssuntoTrf", consultaDocnaoAssinado.getAssuntoTrf().getIdAssuntoTrf());
 	 			}
 	 			if (StringUtil.isNotEmpty(consultaDocnaoAssinado.getCpf())){
 	 				jpql.append(" and exists ( ")
 	 					.append(" SELECT 1 ")
 	 					.append(" FROM ProcessoParte pp ")
 	 					.append(" JOIN pp.pessoa p ")
 	 					.append(" JOIN p.pessoaDocumentoIdentificacaoList pdi ")
 	 					.append(" WHERE pp.processoTrf = processoDocumento.processoTrf ")
 	 					.append(" AND pdi.numeroDocumento = :cpf ) ");
 	 				parameters.put("cpf", consultaDocnaoAssinado.getCpf());
 	 			}
 	 			if (StringUtil.isNotEmpty(consultaDocnaoAssinado.getCnpj())){
 	 				jpql.append(" and exists ( ")
 	 					.append(" SELECT 1 ")
 	 					.append(" FROM ProcessoParte pp ")
 	 					.append(" JOIN pp.pessoa p ")
 	 					.append(" JOIN p.pessoaDocumentoIdentificacaoList pdi ")
 	 					.append(" WHERE pp.processoTrf = processoDocumento.processoTrf ")
 	 					.append(" AND pdi.numeroDocumento = :cnpj )");
 	 				parameters.put("cnpj", consultaDocnaoAssinado.getCnpj());
 	 			}
 	 			if (consultaDocnaoAssinado.getClasseJudicial()!=null && consultaDocnaoAssinado.getClasseJudicial().getIdClasseJudicial() > 0){	
 	 				jpql.append(" AND processoDocumento.processoTrf.classeJudicial IS NOT NULL ")
 	 					.append(" AND processoDocumento.processoTrf.classeJudicial.idClasseJudicial = :idClasseJudicial");
 	 				parameters.put("idClasseJudicial", consultaDocnaoAssinado.getClasseJudicial().getIdClasseJudicial());
 	 			}
 	 			if (consultaDocnaoAssinado.getTipoProcessoDocumento()!=null && consultaDocnaoAssinado.getTipoProcessoDocumento().getIdTipoProcessoDocumento() > 0){
 	 				jpql.append(" AND processoDocumento.tipoProcessoDocumento.idTipoProcessoDocumento = :idTipoProcessoDocumento ");
 	 				parameters.put("idTipoProcessoDocumento", consultaDocnaoAssinado.getTipoProcessoDocumento().getIdTipoProcessoDocumento());
 	 			}
 	 			if (StringUtil.isNotEmpty(consultaDocnaoAssinado.getNumeroProcesso())){
 	 				jpql.append(" AND processoDocumento.processoTrf.processo.numeroProcesso IS NOT NULL ")
 	 					.append(" AND processoDocumento.processoTrf.processo.numeroProcesso LIKE :numeroProcesso ");
 	 				parameters.put("numeroProcesso", String.format("%%%s%%", consultaDocnaoAssinado.getNumeroProcesso()));
 	 			}
 	 			if (consultaDocnaoAssinado.getInseridoInicio()!=null){
 	 				jpql.append(" AND cast(processoDocumento.dataInclusao as date) >= :inseridoInicio");
 	 				parameters.put("inseridoInicio", consultaDocnaoAssinado.getInseridoInicio());
 	 			}
 	 			if (consultaDocnaoAssinado.getInseridoFim()!=null){
 	 				jpql.append(" AND cast(processoDocumento.dataInclusao as date) <= :inseridoFim ");
 	 				parameters.put("inseridoFim", consultaDocnaoAssinado.getInseridoFim());
 	 			}
 			}
 			jpql.append(" order by processoDocumento.dataInclusao asc "); 	 		
 	 		return new PaginatedDataModel<ProcessoDocumento>(ProcessoDocumento.class, jpql, parameters,"idProcessoDocumento");
 	}
	
    @SuppressWarnings("unchecked")
	public ProcessoDocumento recuperaDocumentoNaoAssinadoPorTarefa(Integer idProcesso, Long idTaskInstance) {
        if(idProcesso == null || idTaskInstance == null){
            return null;
        }
        String hql = "select o from ProcessoDocumento o where o.ativo=true and o.processo.idProcesso = :idProcesso " +
                     "and o.idJbpmTask = :idTaskInstance and o.dataJuntada is null and not exists " +
                     "(select 1 from ProcessoDocumentoBinPessoaAssinatura pdbpa where pdbpa.processoDocumentoBin = o.processoDocumentoBin)";
        Query q = entityManager.createQuery(hql);
        q.setParameter("idProcesso",idProcesso);
        q.setParameter("idTaskInstance",idTaskInstance);
        List<ProcessoDocumento> res = q.getResultList();
        return res != null && res.size() > 0 ? res.get(0): null;
    }

    /**
     * metodo responsavel por retornar a contagem de documentos anexos do documento principal passado em parametro.
     * regra: conta todos os ProcessosDocumentos onde
     * -> o documento principal seja o passado em parametro.
     * -> o processoTrf seja igual ao do documento principal.
     * -> a data de juntada nao seja nula (somente documentos assinados)
     * @param processoDocumentoPrincipal
     * @return
     */
	public int contagemDocumentosAnexos(ProcessoDocumento pdPrincipal) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT COUNT(o.idProcessoDocumento) ");
		sb.append("FROM ProcessoDocumento AS o ");
		sb.append("WHERE o.documentoPrincipal = :documentoPrincipal ");
		sb.append("AND o.processoTrf = :processo ");
		sb.append("AND o.dataJuntada IS NOT NULL");
 
        Query q = entityManager.createQuery(sb.toString());
        q.setParameter("documentoPrincipal", pdPrincipal);
        q.setParameter("processo", pdPrincipal.getProcessoTrf());
        Long result = ((Long) q.getSingleResult());
        
        return result.intValue();
	}
	
	public ProcessoDocumento getAnexoByNumeroOrdem(ProcessoDocumento processoDocumento, Integer idNumeroOrdem) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o ");
		sb.append("FROM ProcessoDocumento AS o ");
		sb.append("WHERE o.documentoPrincipal = :documentoPrincipal ");
		//sb.append("AND o.processoTrf = :processo ");
		sb.append("AND o.numeroOrdem = :numeroOrdem ");
        Query q = entityManager.createQuery(sb.toString());
        q.setParameter("documentoPrincipal", processoDocumento.getDocumentoPrincipal());
        //q.setParameter("processo", processoDocumento.getProcessoTrf());
        q.setParameter("numeroOrdem", idNumeroOrdem);
        ProcessoDocumento result = (ProcessoDocumento) q.getSingleResult();
        return result;
	}
	
	/**
	 * Lista os documentos principais do processo cujos tipos sejam conforme os parâmetros ou, quando vinculados, cujo tipo do documento principal seja conforme os parâmetros
	 * @param processo
	 * @param tipos
     * @return List<ProcessoDocumento>
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> listarDocumentosPrincipais(ProcessoTrf processo, Integer... tipos) {
        String query = "SELECT d FROM ProcessoDocumento AS d " +
            " WHERE d.processoTrf = :proc " +
            " AND d.tipoProcessoDocumento.idTipoProcessoDocumento IN (:tipos) " +
            " AND d.ativo = true AND d.dataJuntada IS NOT NULL AND d.dataExclusao IS NULL " +
            " UNION ALL " +
            " SELECT dPrincipal FROM ProcessoDocumento AS dPrincipal " +
            " WHERE dPrincipal.processoTrf = :proc " +
            " AND dPrincipal.documentoPrincipal.tipoProcessoDocumento.idTipoProcessoDocumento IN (:tipos) " +
            " AND dPrincipal.ativo = true AND dPrincipal.dataJuntada IS NOT NULL AND dPrincipal.dataExclusao IS NULL " +
            " ORDER BY d.dataJuntada DESC, d.dataInclusao DESC ";
            
        Query q = entityManager.createQuery(query);
        q.setParameter("proc", processo);
        q.setParameter("tipos", Util.isEmpty(Arrays.asList(tipos))?null:Arrays.asList(tipos));

        return q.getResultList();
	}	

   /**
     * Recupera todos documentos do processo por um mapa de parametros
     * @param idProcesso (opcional)
     * @param processoDocumentoVO
     * @return PaginatedDataModel<ProcessoDocumento>
     */
    public PaginatedDataModel<ProcessoDocumento> recuperarDocumentosParametros(Integer idProcesso, ProcessoDocumentoVO processoDocumentoVO) {
        HashMap<String, Object> parametros = new HashMap<String, Object>();
        StringBuffer jpql = new StringBuffer("");
        jpql.append("SELECT d FROM ProcessoDocumento as d ");
        jpql.append(" WHERE d.idProcessoDocumento = d.idProcessoDocumento  ");
        if (idProcesso!=null){
            jpql.append(" AND d.processo.idProcesso = :idProcesso ");
            parametros.put("idProcesso", idProcesso);
        }
        if (processoDocumentoVO!=null){
            if (processoDocumentoVO.getAtivo()==null){
                jpql.append(" AND d.ativo is true ");
            }else{
                jpql.append(" AND d.ativo is :ativo ");
                parametros.put("ativo", processoDocumentoVO.getAtivo());
            }
            if (processoDocumentoVO.getIdProcDoc()!=null && processoDocumentoVO.getIdProcDoc() > 0){
                jpql.append(" AND d.idProcessoDocumento = :idProcessoDocumento ");
                parametros.put("idProcessoDocumento", processoDocumentoVO.getIdProcDoc());
            }
            if (processoDocumentoVO.getTipoProcessoDocumento() != null
                    && processoDocumentoVO.getTipoProcessoDocumento().getIdTipoProcessoDocumento() > 0) {
                jpql.append(" AND d.tipoProcessoDocumento.idTipoProcessoDocumento = :idTipoProcessoDocumento ");
                parametros.put("idTipoProcessoDocumento",
                        processoDocumentoVO.getTipoProcessoDocumento().getIdTipoProcessoDocumento());
            }
            if (processoDocumentoVO.getProcessoDocumento()!=null && !"".equals(processoDocumentoVO.getProcessoDocumento())){
                jpql.append(" AND lower(to_ascii(d.processoDocumento)) LIKE to_ascii(:processoDocumento) ");
                parametros.put("processoDocumento", String.format("%%%s%%", processoDocumentoVO.getProcessoDocumento().toLowerCase()));
            }
            if (processoDocumentoVO.getRecursoInterno()!=null){
                jpql.append(" AND d.tipoProcessoDocumento.recursoInterno is :recursoInterno ");
                parametros.put("recursoInterno", processoDocumentoVO.getRecursoInterno());
            }
        }
        jpql.append(" ORDER BY d.idProcessoDocumento DESC");
        return new PaginatedDataModel<ProcessoDocumento>(ProcessoDocumento.class, jpql, parametros,"idProcessoDocumento");
    }
    
    public ProcessoDocumento getUltimoAtoProferido(Integer idProcesso) {
    	StringBuilder query = new StringBuilder();
    	query.append("select o from ProcessoDocumento o ");
    	query.append("where o.processo.idProcesso = :idProcesso ");
    	query.append(  "and o.ativo = true ");
    	query.append(  "and o.processoDocumentoBin.valido = true ");
    	query.append(  "and o.tipoProcessoDocumento.documentoAtoProferido = true ");
    	query.append("order by o.dataJuntada DESC, o.dataInclusao DESC ");
        Query q = getEntityManager().createQuery(query.toString());
        q.setParameter("idProcesso", idProcesso);
        q.setMaxResults(1);
        return EntityUtil.getSingleResult(q);
    }

    /**
     * Método responsável por verificar se o ProcessoDocumento especificado está juntado ao processo
     * @param idProcessoDocumento Código identificador do ProcessoDocumento
     * @return Retorna "true" caso o documento esteja juntado ao processo e "false" caso contrrio
     */
    public boolean isDocumentoJuntado(int idProcessoDocumento) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT pd.dt_juntada FROM tb_processo_documento pd WHERE pd.id_processo_documento = :idProcessoDocumento");
        sql.append(" AND pd.dt_exclusao IS NULL");
        Query query = EntityUtil.createNativeQuery(getEntityManager(), sql.toString(), "tb_processo_documento");
        query.setParameter(ID_PROCESSO_DOCUMENTO, idProcessoDocumento);
        return !Objects.isNull(EntityUtil.getSingleResult(query));
    }	
}