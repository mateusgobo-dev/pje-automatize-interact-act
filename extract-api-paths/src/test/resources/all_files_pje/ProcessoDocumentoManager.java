package br.jus.cnj.pje.nucleo.manager;

import static br.com.itx.util.ComponentUtil.getProcessoDocumentoBinManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.bpm.TaskInstance;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.util.Strings;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.editor.manager.ProcessoDocumentoEstruturadoManager;
import br.com.infox.exceptions.NegocioException;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.business.dao.ProcessoDocumentoDAO;
import br.jus.cnj.pje.business.dao.TipoProcessoDocumentoDAO;
import br.jus.cnj.pje.extensao.PontoExtensaoException;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.nucleo.service.TipoProcessoDocumentoPapelService;
import br.jus.cnj.pje.view.PaginatedDataModel;
import br.jus.cnj.pje.vo.ProcessoDocumentoConsultaNaoAssinadoVO;
import br.jus.csjt.pje.business.pdf.GeradorPdfUnificado;
import br.jus.csjt.pje.business.pdf.PdfException;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.pje.mni.entidades.DownloadBinarioArquivo;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoTrfLocal;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumentoPapel;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturado;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.ProcessoTrfApreciadoEnum;
import br.jus.pje.nucleo.enums.TipoOrigemAcaoEnum;
import br.jus.pje.nucleo.util.Crypto;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * @author cristof
 *
 */
@Name(ProcessoDocumentoManager.NAME)
public class ProcessoDocumentoManager extends BaseManager<ProcessoDocumento> {
    
	public static final String NAME = "processoDocumentoManager";
    
    @In
    private ProcessoDocumentoDAO processoDocumentoDAO;

	private br.com.itx.component.Util util = new br.com.itx.component.Util();
	private boolean documentoAssinado = false;
	private boolean justicaTrabalho = false;
	private boolean permiteEditarDocs = false;

	@In(create = true, required = true)
	private transient ProcessoJudicialManager processoJudicialManager;
	private ProcessoTrf processoTrf;

	@In
	private TipoProcessoDocumentoDAO tipoProcessoDocumentoDAO;
	
	@Override
    protected ProcessoDocumentoDAO getDAO() {
        return this.processoDocumentoDAO;
    }
    
    /**
     * Retorna a instância de ProcessoDocumentoManager.
     * 
     * @return ProcessoDocumentoManager
     */
    public static ProcessoDocumentoManager instance() {
    	return ComponentUtil.getComponent(ProcessoDocumentoManager.class);
    }
    
    /**
     * Sobrescrita do metodo persist da BaseManager para centralização das regras que
     * tratam de documentos.
     */
    @Override
	public ProcessoDocumento persist(ProcessoDocumento processoDocumento) throws PJeBusinessException{
    	UsuarioLocalizacao usuarioLocalizacao = Authenticator.getUsuarioLocalizacaoAtual();
        if(usuarioLocalizacao != null) {
        	 if (processoDocumento.getLocalizacao() == null) {
        		 processoDocumento.setLocalizacao(usuarioLocalizacao.getLocalizacaoFisica());
             }

             if (processoDocumento.getPapel() == null) {
            	 processoDocumento.setPapel(usuarioLocalizacao.getPapel());
             }
             
             if ((processoDocumento.getUsuarioInclusao() == null) || (processoDocumento.getNomeUsuarioInclusao() == null)) {
            	 processoDocumento.setUsuarioInclusao(usuarioLocalizacao.getUsuario());
             }
        }
		return getDAO().persist(processoDocumento);
	};

    /*
     * INÍCIO DA INTEGRAÇÃO 1.2.0.M6 -> 1.4.0.M4 *********************************************************
     */

    /**
     * Verifica se existe um tipo de documento informado para o processo passado no parametro.
     *
     * @param processo a se verificar a existencia do documento
     * @param tpd tipo do documento que se deseja saber a existencia
     * @return true se existir ao menos um.
     */
    public boolean existeProcessoDocumentoByTipo(Processo processo,
        TipoProcessoDocumento tpd) {
        boolean ret = false;

        if ((processo != null) && (tpd != null)) {
            if (getDAO().listProcessoDocumentoByTipo(processo, tpd).size() > 0) {
                ret = true;
            }
        }

        return ret;
    }

    /**
     * Verifica se o documento foi criado e precisa ser tratado em atividade específica para: assinatura/ juntada / exclusão
     * @param documento
     * @return
     */
    public boolean verificarDocumentoDeAtividadeEspecifica(ProcessoDocumento documento) {
    	boolean retorno = false;
    	if(EntityUtil.getEntityManager().contains(documento)) {
    		this.processoDocumentoDAO.refresh(documento);
    	}
    	if (documento != null && documento.getIdProcessoDocumento()>0){
    		retorno = this.processoDocumentoDAO.verificaDocumentoDeAtividadeEspecifica(documento.getIdProcessoDocumento());
    	}
    	return retorno;
    }
    
    /**
     * Cria um processo documento binário informando a data de inclusao e a descricao do documento.
     *
     * @param dataInclusao do documento
     * @param modeloDocumento descrição, conteúdo do documento.
     * @return ProcessoDocumentoBin persistido.
     */
    public ProcessoDocumentoBin inserirProcessoDocumentoBin(Date dataInclusao,
        String modeloDocumento) {
        EntityManager em = EntityUtil.getEntityManager();
        ProcessoDocumentoBin pdb = new ProcessoDocumentoBin();
        pdb.setModeloDocumento(modeloDocumento);
        pdb.setDataInclusao(dataInclusao);
        em.persist(pdb);
        em.flush();

        return pdb;
    }

    /**
     * Inclui o processoDocumento informado, porém utiliza o bin <code>pdb</code> informado nos argumentos como documento binário.
     *
     * @param pd ProcessoDocumento a ser cadastrado
     * @param processoTrf processo a ser vinculado o documento.
     * @param pdb processoDocumentoBin a ser vinculado no processoDocumento
     * @param usuarioLocalizacao atual logada.
     * @return ProcessoDocumento persistido.
     */
    public ProcessoDocumento inserirProcessoDocumento(ProcessoDocumento pd,
        ProcessoTrf processoTrf, ProcessoDocumentoBin pdb) throws PJeBusinessException {
        EntityManager em = EntityUtil.getEntityManager();

        pd.setProcessoDocumentoBin(pdb);

        if (pd.getDocumentoSigiloso()) {
            processoTrf.setApreciadoSigilo(ProcessoTrfApreciadoEnum.A);
        }        

        if (pd.getProcessoDocumento() == null) {
            pd.setProcessoDocumento(pd.getTipoProcessoDocumento().getTipoProcessoDocumento());
        }

        if ((pdb.getUsuario() == null) || (pdb.getNomeUsuario() == null)) {
            pdb.setUsuario(Authenticator.getUsuarioLogado());
        }
        
        pd.setProcesso(processoTrf.getProcesso());
        em.merge(processoTrf);

        if (pdb.getIdProcessoDocumentoBin() == 0) {
            em.persist(pdb);
        }

        persist(pd);
        flush();

        return pd;
    }

    public ProcessoDocumento inserirProcessoDocumento(
        ProcessoDocumentoTrfLocal pdTrfLocal) throws PJeBusinessException {
        ProcessoDocumento processoDocumento = pdTrfLocal.getProcessoDocumento();
        ProcessoDocumentoBin pdBin = processoDocumento.getProcessoDocumentoBin();
        ProcessoTrf processoTrf = ComponentUtil.getComponent(ProcessoTrfManager.class).getProcessoTrfByProcesso(processoDocumento.getProcesso());
        inserirProcessoDocumento(processoDocumento, processoTrf, pdBin);
        inserirProcessoDocumentoTrfLocal(processoDocumento, pdTrfLocal);

        return processoDocumento;
    }
    
	/**
 	 * Metodo responsável por retornar uma lista de documentos pendentes de assinatura
 	 * @param ConsultaDocnaoAssinado
 	 * @return PaginatedDataModel<ProcessoDocumento>
 	 */
 	public PaginatedDataModel<ProcessoDocumento> recuperarDocumentosNaoAssinados(ProcessoDocumentoConsultaNaoAssinadoVO consultaDocnaoAssinado) {
 		Boolean verif = ComponentUtil.getComponent(UsuarioLocalizacaoManager.class).isMagistradoAuxiliar();
 		if (verif){
 			return processoDocumentoDAO.recuperarDocumentosNaoAssinadosMagistradoAuxiliar(consultaDocnaoAssinado);			
 		}else{
 			return processoDocumentoDAO.recuperarDocumentosNaoAssinados(consultaDocnaoAssinado);
 		}
	}

    /*
     * TODO INTEGRACAO - Ver da onde o método abaixo é chamado e mudar para chamar a manager da entidade...
     */
    public void inserirProcessoDocumentoTrfLocal(
        ProcessoDocumento processoDocumento,
        ProcessoDocumentoTrfLocal processoDocumentoTrfLocal) {
        processoDocumentoTrfLocal.setProcessoDocumento(processoDocumento);
        processoDocumentoTrfLocal.setIdProcessoDocumentoTrf(processoDocumento.getIdProcessoDocumento());
        EntityUtil.getEntityManager().persist(processoDocumentoTrfLocal);
    }

    public void atualizarProcessoDocumento(ProcessoDocumento pd) {
        EntityManager em = EntityUtil.getEntityManager();
        UsuarioLocalizacao usuarioLocalizacao = Authenticator.getUsuarioLocalizacaoAtual();
        Date dataAlteracao = new Date();
        pd.setDataAlteracao(dataAlteracao);

        if ((pd.getUsuarioAlteracao() == null) ||
                (pd.getNomeUsuarioAlteracao() == null)) {
            pd.setUsuarioAlteracao(usuarioLocalizacao.getUsuario());
        }

        em.merge(pd.getProcessoDocumentoBin());
        em.merge(pd);
        em.flush();
    }

    /**
     * Assina o documento informado <code>pdb</code>.
     *
     * @param processoDocumentoBin a ser assinado
     * @param assinatura a ser gravada no documento
     * @param certChain certificado
     * @param dataAssinatura data da assinatura do documento
     * @param pessoa que assinou o documento
     * @return ProcessoDocumentoBinPessoaAssinatura
     */
    public ProcessoDocumentoBinPessoaAssinatura inserirAssinaturaNoProcessoDocumentoBin(
        ProcessoDocumentoBin processoDocumentoBin, String assinatura,
        String certChain, Date dataAssinatura, Pessoa pessoa) {
        ProcessoDocumentoBinPessoaAssinatura binPessoaAssinatura = new ProcessoDocumentoBinPessoaAssinatura();
        binPessoaAssinatura.setAssinatura(assinatura);
        binPessoaAssinatura.setCertChain(certChain);
        binPessoaAssinatura.setDataAssinatura(dataAssinatura);
        binPessoaAssinatura.setProcessoDocumentoBin(processoDocumentoBin);

        if (binPessoaAssinatura.getPessoa() == null) {
            binPessoaAssinatura.setPessoa(pessoa);
        }

        processoDocumentoBin.setSignature(assinatura);
        processoDocumentoBin.setCertChain(certChain);

        EntityManager em = EntityUtil.getEntityManager();
        em.merge(processoDocumentoBin);
        em.persist(binPessoaAssinatura);
        em.flush();

        // define o papel do documento como o do responssavel pela assinatura
        for (ProcessoDocumento pd : processoDocumentoBin.getProcessoDocumentoList()) {
            pd.setPapel(Authenticator.getPapelAtual());
            EntityUtil.getEntityManager().merge(pd);
            EntityUtil.getEntityManager().flush();
        }

        return binPessoaAssinatura;
    }

    public boolean checkAssinaturaProcessoDocumentoBin(ProcessoDocumentoBin bin) {
        String hql = "select o from ProcessoDocumentoBinPessoaAssinatura o " +
            "where o.processoDocumentoBin = :bin";
        Query query = EntityUtil.createQuery(hql);
        query.setParameter("bin", bin);

        return EntityUtil.getSingleResult(query) != null;
    }

    public boolean possuiVisibilidadeParaTipoProcessoDocumento(Papel papel,
        int idTipoProcessoDocumento) {
        StringBuilder sqlPes = new StringBuilder();
        sqlPes.append("select o from ");
        sqlPes.append("TipoProcessoDocumentoPapel o ");
        sqlPes.append(
            "where o.tipoProcessoDocumento.idTipoProcessoDocumento = :id and o.papel = :papel");

        Query query = EntityUtil.createQuery(sqlPes.toString());
        query.setParameter("id", idTipoProcessoDocumento);
        query.setParameter("papel", papel);

        Object tipoProcessoDocumento = EntityUtil.getSingleResult(query);

        return tipoProcessoDocumento != null;
    }

    public void excluirDocumento(ProcessoDocumento processoDocumento,
        Usuario usuarioExclusao, String mensagem) throws PJeBusinessException {
        try {
            processoDocumento.setAtivo(false);
            processoDocumento.setDataExclusao(new Date());
            processoDocumento.setUsuarioExclusao(usuarioExclusao);
            processoDocumento.setMotivoExclusao(mensagem);
            processoDocumentoDAO.persist(processoDocumento);
        } catch (Exception e) {
            throw new PJeBusinessException(e.getMessage());
        }
    }

    public boolean isModeloVazio(ProcessoDocumentoBin bin) {
        return (bin == null) || Strings.isEmpty(bin.getModeloDocumento()) ||
        Strings.isEmpty(removeTags(bin.getModeloDocumento()));
    }

    private String removeTags(String modelo) {
        return modelo.replaceAll("\\<.*?\\>", "").replaceAll("\n", "")
                     .replaceAll("\r", "").replaceAll("&nbsp;", "");
    }

    /**
     * Verifica se o documento informado é um documento do ato do magistrado
     *
     * @param processoDocumento
     * @return true se for documento do ato do magistrado
     */
    public boolean isDocumentoAto(ProcessoDocumento processoDocumento) {
        ParametroUtil parametroUtil = ParametroUtil.instance();
        TipoProcessoDocumento tipoPd = processoDocumento.getTipoProcessoDocumento();

        TipoProcessoDocumento atoOrdinatorio = parametroUtil.getTipoProcessoDocumentoAtoOrdinatorio();
        TipoProcessoDocumento despacho = parametroUtil.getTipoProcessoDocumentoDespacho();
        TipoProcessoDocumento decisao = parametroUtil.getTipoProcessoDocumentoDecisao();
        TipoProcessoDocumento sentenca = parametroUtil.getTipoProcessoDocumentoSentenca();
        TipoProcessoDocumento acordao = parametroUtil.getTipoProcessoDocumentoAcordao();
        TipoProcessoDocumento inteiroTeor = parametroUtil.getTipoProcessoDocumentoInteiroTeor();

        return tipoPd.equals(atoOrdinatorio) || tipoPd.equals(despacho) ||
        tipoPd.equals(decisao) || tipoPd.equals(sentenca) ||
        tipoPd.equals(acordao) || tipoPd.equals(inteiroTeor);
    }

    /**
     * Verifica se o documento informado é um documento do expediente
     *
     * @param processoDocumento
     * @return true se for documento do expediente
     */
    public Boolean isDocumentoExpediente(ProcessoDocumento processoDocumento) {
        return processoDocumento.getTipoProcessoDocumento()
                                .equals(ParametroUtil.instance()
                                                     .getTipoProcessoDocumentoExpediente());
    }

    /*
     * FIM INTEGRAÇÃO 1.2.0.M6 -> 1.4.0.M4 ***************************************************************
     */
    public ProcessoDocumento getDocumento(Long jbpmTask) {
        ProcessoDocumento pd = new ProcessoDocumento();
        pd.setAtivo(true);
        pd.setDocumentoSigiloso(false);
        pd.setIdJbpmTask(jbpmTask);
        if(jbpmTask != null && jbpmTask > 0) {
        	pd.setExclusivoAtividadeEspecifica(Boolean.TRUE);
        }
        return pd;
    }
    
    public ProcessoDocumento getDocumento() {
    	Long jbpmTask = null;
        if (TaskInstance.instance() != null) {
            jbpmTask = TaskInstance.instance().getId();
        }
        return this.getDocumento(jbpmTask);
    }
    
    public ProcessoDocumento getDocumento(ProcessoTrf proc, int idDocumento){
    	if(proc == null || idDocumento == 0){
    		return null;
    	}
    	Search s = new Search(ProcessoDocumento.class);
    	s.setMax(1);
    	addCriteria(s,
    			Criteria.not(Criteria.isNull("dataJuntada")),
    			Criteria.equals("idProcessoDocumento", idDocumento),
    			Criteria.equals("processoTrf.idProcessoTrf", proc.getIdProcessoTrf()));
    	List<ProcessoDocumento> ret = list(s);
    	return ret.isEmpty() ? null : ret.get(0);
    }
    
    public List<ProcessoDocumento> recuperaDocumentosJuntados(ProcessoDocumento documentoPrincipal){
    	if(documentoPrincipal == null){
    		return null;
    	}
    	Search s = new Search(ProcessoDocumento.class);
    	addCriteria(s, Criteria.or(
    					Criteria.equals("documentoPrincipal.idProcessoDocumento", documentoPrincipal.getIdProcessoDocumento()),
    					Criteria.equals("idProcessoDocumento", documentoPrincipal.getIdProcessoDocumento()))
    	);
    	addCriteria(s, Criteria.equals("ativo", Boolean.TRUE));
   		addCriteria(s, Criteria.not(Criteria.isNull("dataJuntada")));
    	return list(s);
    }
    
    public List<ProcessoDocumento> recuperaDocumentosJuntados(ProcessoTrf proc, Date dataReferencia){
    	if(proc == null){
    		return null;
    	}
    	Search s = new Search(ProcessoDocumento.class);
    	addCriteria(s, Criteria.equals("processoTrf.idProcessoTrf", proc.getIdProcessoTrf()));
    	addCriteria(s, Criteria.equals("ativo", Boolean.TRUE));
    	if(dataReferencia != null){
    		addCriteria(s, Criteria.greaterOrEquals("dataJuntada", dataReferencia));
    	}else{
    		addCriteria(s, Criteria.not(Criteria.isNull("dataJuntada")));
    	}
    	s.addOrder("dataJuntada", Order.ASC);
    	return list(s);
    }

    public List<ProcessoDocumento> findByRange(ProcessoTrf processo, int first,
        int length) {
        return this.processoDocumentoDAO.findByRange(processo, first, length);
    }
    
    public List<ProcessoDocumento> findByRange(ProcessoTrf processo, int first,
            int length, boolean b, boolean decrescente, boolean incluirPDF,
            boolean incluirComAssinaturaInvalidada, boolean incluirDocumentoPeticaoInicial) {
            
    	return this.findByRange(processo, first, length, b, decrescente, incluirPDF, incluirComAssinaturaInvalidada, incluirDocumentoPeticaoInicial, false, false, false);
    }

    public List<ProcessoDocumento> findByRange(ProcessoTrf processo, int first,
            int length, boolean b, boolean decrescente, boolean incluirPDF,
            boolean incluirDocumentoPeticaoInicial, boolean soDocumentosJuntados, boolean incluirDocCopiaExpediente) {
            return this.findByRange(processo, first, length, b,
                decrescente, incluirPDF, false, incluirDocumentoPeticaoInicial, soDocumentosJuntados, incluirDocCopiaExpediente, false);
    }
    
    public List<ProcessoDocumento> findByRange(ProcessoTrf processo, int first,
            int length, boolean b, boolean decrescente, boolean incluirPDF,
            boolean incluirComAssinaturaInvalidada, boolean incluirDocumentoPeticaoInicial, 
            boolean soDocumentosJuntados, boolean incluirDocCopiaExpediente, boolean apenasAtosProferidos) {
    	    	
            return this.findByRange(processo, first, length, b,
                decrescente, incluirPDF,  incluirComAssinaturaInvalidada, incluirDocumentoPeticaoInicial, soDocumentosJuntados, incluirDocCopiaExpediente, apenasAtosProferidos, null);
    }
    
    public List<ProcessoDocumento> findByRange(ProcessoTrf processo, int first,
            int length, boolean b, boolean decrescente, boolean incluirPDF,
            boolean incluirComAssinaturaInvalidada, boolean incluirDocumentoPeticaoInicial, 
            boolean soDocumentosJuntados, boolean incluirDocCopiaExpediente, boolean apenasAtosProferidos, TipoOrigemAcaoEnum tipoOrigemAcao) {
    	    	
            return this.processoDocumentoDAO.findByRange(processo, first, length, b,
                decrescente, incluirPDF,  incluirComAssinaturaInvalidada, incluirDocumentoPeticaoInicial, soDocumentosJuntados, incluirDocCopiaExpediente, apenasAtosProferidos, tipoOrigemAcao);
    }
    
    public Integer getCountDocumentos(ProcessoTrf processo, boolean incluirPDF,
    		boolean incluirComAssinaturaInvalidada, boolean incluirDocumentoPeticaoInicial) {
    	return this.getCountDocumentos(processo, incluirPDF, incluirComAssinaturaInvalidada, incluirDocumentoPeticaoInicial, false);
    }
    
    public Integer getCountDocumentos(ProcessoTrf processo, boolean incluirPDF,
            boolean incluirComAssinaturaInvalidada, boolean incluirDocumentoPeticaoInicial, boolean soDocumentosJuntados) {
        return this.getCountDocumentos(processo, incluirPDF, incluirComAssinaturaInvalidada, incluirDocumentoPeticaoInicial, soDocumentosJuntados, false);
    }

    public Integer getCountDocumentos(ProcessoTrf processo, boolean incluirPDF,
            boolean incluirComAssinaturaInvalidada, boolean incluirDocumentoPeticaoInicial, boolean soDocumentosJuntados, boolean apenasAtosProferidos) {
        return this.getCountDocumentos(processo,
                incluirPDF, incluirComAssinaturaInvalidada, incluirDocumentoPeticaoInicial, soDocumentosJuntados, apenasAtosProferidos, null);
    }
    
    public Integer getCountDocumentos(ProcessoTrf processo, boolean incluirPDF,
            boolean incluirComAssinaturaInvalidada, boolean incluirDocumentoPeticaoInicial, boolean soDocumentosJuntados, boolean apenasAtosProferidos, TipoOrigemAcaoEnum tipoOrigemAcao) {
        return this.processoDocumentoDAO.getCountDocumentos(processo,
                incluirPDF, incluirComAssinaturaInvalidada, incluirDocumentoPeticaoInicial, soDocumentosJuntados, apenasAtosProferidos, tipoOrigemAcao);
    }

    /**
     * Verifica se o documento informado está liberado para consulta publica, ou seja, se ele se localiza na {@link ProcessoDocumentoTrfLocal}
     * contendo o atributo liberadoConsultaPublica igual a true.
     *
     * @param processoDocumento Documento que será verificado na {@link ProcessoDocumentoTrfLocal}
     * @return true se o documento está liberado para consulta pública, senão false
     */
    public boolean isLiberadoConsultaPublica(
        ProcessoDocumento processoDocumento) {
        String hql = "SELECT o.liberadoConsultaPublica FROM " +
            ProcessoDocumentoTrfLocal.class.getSimpleName() + " o " +
            " WHERE o.idProcessoDocumentoTrf = :idProcessoDocumentoTrf";
        Query query = EntityUtil.createQuery(hql);
        query.setParameter("idProcessoDocumentoTrf",
            processoDocumento.getIdProcessoDocumento());

        try {
            Boolean liberadoConsultaPublica = EntityUtil.getSingleResult(query);
            return (liberadoConsultaPublica != null) ? liberadoConsultaPublica : false;
        } catch (NoResultException ex) {
            return false;
        }
    }

    public ProcessoDocumento getProcessoDocumento(
        TipoProcessoDocumento tipoProcessoDocumento, Processo processo) {
        return processoDocumentoDAO.getProcessoDocumentoByProcessoTipoProcessoDocumento(tipoProcessoDocumento,
            processo);
    }

    public ProcessoDocumento getUltimoProcessoDocumento(
        Processo processo) {
        return processoDocumentoDAO.getUltimoProcessoDocumento(processo);
       }

    public ProcessoDocumento getUltimoProcessoDocumentoPrincipalAtivo(Processo processo) {
        return processoDocumentoDAO.getUltimoProcessoDocumentoPrincipalAtivo(processo);
    }
    
    public ProcessoDocumento getUltimoProcessoDocumento(
        TipoProcessoDocumento tipoProcessoDocumento, Processo processo) {
        return processoDocumentoDAO.getUltimoProcessoDocumentoByProcessoTipoProcessoDocumento(tipoProcessoDocumento,
            processo);
    }

    public ProcessoDocumento getUltimoProcessoDocumento(
        List<TipoProcessoDocumento> tipos, Processo processo) {
        return processoDocumentoDAO.getUltimoProcessoDocumentoByTiposProcessoDocumento(tipos,
            processo);
    }
    
    public ProcessoDocumento getUltimoProcessoDocumentoNaoAssinado (Processo processo) {
    	return processoDocumentoDAO.getUltimoProcessoDocumentoNaoAssinado(processo);
    }

    public boolean isDocumentoAssinado(ProcessoDocumentoBin procBin) {
        return processoDocumentoDAO.getAssinaturasDocumento(procBin).size() > 0;
    }

    /**
     * Método responsável por realizar a verificação se o documento é uma petição inical 
     * 
     * @param processoDocumento
     * @return true se o documento for uma peticao inicial
     */
    public boolean isDocumentoPeticaoInicial(ProcessoDocumento processoDocumento) {
    	TipoProcessoDocumento tipoPeticaoInicial = processoDocumento.getProcessoTrf().getClasseJudicial().getTipoProcessoDocumentoInicial();
		
		boolean ret = false;
		if(processoDocumento != null 
				&&	processoDocumento.getTipoProcessoDocumento() == tipoPeticaoInicial){
			ret = true;
		}
		
		return ret;
    }

    public List<ProcessoDocumento> getDocumentosPorTipo(ProcessoTrf processoJudicial, Integer... tipos) {
    	if(tipos == null || tipos.length == 0){
    		return Collections.emptyList();
    	}
        Search search = new Search(ProcessoDocumento.class);
        addCriteria(search, 
        		Criteria.equals("processo", processoJudicial.getProcesso()),
        		Criteria.equals("ativo", true),
        		Criteria.in("tipoProcessoDocumento.idTipoProcessoDocumento", tipos)
        		);
		search.addOrder("idProcessoDocumento", Order.ASC);
        return list(search);
    }

    /**
     * Exclui todos os documentos do processo que foram cadastrados sem conteúdo. Utilizado na elaboração do acórdão prévia à sessão de julgamento.
     * 
     * @param processoJudicial
     * @param tipos
     * @throws PJeBusinessException
     */
    public void excluirDocumentosEmBrancoPorTipo(ProcessoTrf processoJudicial, Integer... tipos) throws PJeBusinessException {
    	// Recupera os documentos dos tipos indicados no parâmetro
    	List<ProcessoDocumento> listProcDoc = getDocumentosPorTipo(processoJudicial, tipos);

    	SessaoProcessoDocumentoManager sessaoProcDocManager = ComponentUtil.getSessaoProcessoDocumentoManager();

    	for (ProcessoDocumento processoDocumento : listProcDoc) {
    		boolean isDocumentoEmBranco = processoDocumento.getProcessoDocumentoBin().getModeloDocumento() == null || processoDocumento.getProcessoDocumentoBin().getModeloDocumento().trim().equals("");

    		// Se o documento estiver em branco e não for binário, procede com a exclusão
    		if (isDocumentoEmBranco && !processoDocumento.getProcessoDocumentoBin().getBinario()) {
    			// Remove também o vínculo com a Sessão de Julgamento, já que o documento não possui conteúdo.
    			SessaoProcessoDocumento sessaoProcDoc = sessaoProcDocManager.recuperaPorProcessoDocumento(processoDocumento);
    			sessaoProcDocManager.remove(sessaoProcDoc);

    			remove(processoDocumento);
    		}
    	}
    }
    
    public ProcessoDocumento getDocumentoRecente(ProcessoTrf processo, Integer tipo) throws PJeBusinessException{
    	Search search = new Search(ProcessoDocumento.class);
    	search.setMax(1); 																// Máximo de registros a serem retornados
    	search.addOrder("dataJuntada", Order.DESC);										// Ordem inversa da juntada
    	addCriteria(search, 
    			Criteria.equals("processo.idProcesso", processo.getIdProcessoTrf()), 	// Documentos do processo dado
    			Criteria.not(Criteria.empty("processoDocumentoBin.signatarios")),		// que têm ao menos uma assinatura
    			Criteria.equals("tipoProcessoDocumento.idTipoProcessoDocumento", tipo));// e que é do tipo informado
    	List<ProcessoDocumento> ret = list(search);
    	return ret.isEmpty() ? null : ret.get(0);
    }

    public List<ProcessoDocumento> findByProcessoDocumentoBin(
        ProcessoDocumentoBin procBIN) {
        return processoDocumentoDAO.findByProcessoDocumentoBin(procBIN);
    }

    public Boolean existsByHash(String hash) {
        return null;
    }

    public List<ProcessoDocumento> getDocumentosPorNumero(Integer idProcesso,
        String numeroDocumento) {
        return getDAO().getDocumentosPorNumero(idProcesso, numeroDocumento);
    }

    public ProcessoDocumento getDocumentoPendente(ProcessoTrf processoJudicial, Localizacao localizacaoFisica) {
    	return getDocumentoPendente(processoJudicial, localizacaoFisica, false);
    }

    public ProcessoDocumento getDocumentoPendente(ProcessoTrf processoJudicial, Localizacao localizacaoFisica, boolean abrangerDocumentosDeAtividadeEspecifica) {
    	Search search = new Search(ProcessoDocumento.class);
    	addCriteria(search, 
    			Criteria.equals("processo.idProcesso", processoJudicial.getProcesso().getIdProcesso()),
    			Criteria.equals("localizacao", localizacaoFisica),
                Criteria.equals("ativo", true),
    			Criteria.not(Criteria.in("tipoProcessoDocumento", getTiposDocumentosRestritos().toArray())));
    	
    	// Se o processo já foi distribuído, então deve-se ignorar os documentos já assinados e juntados aos autos
    		addCriteria(search,	
    			Criteria.empty("processoDocumentoBin.signatarios"),
    			Criteria.isNull("dataJuntada"),
    			Criteria.isNull("nomeUsuarioJuntada"),
                Criteria.isNull("documentoPrincipal"));
    	
    	// Ignora documentos de atividades especificas, caso a flag tenha sido marcada para não abranger esses documentos
    	if (!abrangerDocumentosDeAtividadeEspecifica) {
    		List<ProcessoDocumento> documentos = this.processoDocumentoDAO.recuperarDocumentosNaoJuntadosDeAtividadeEspecifica(processoJudicial.getProcesso().getIdProcesso());
    		if (!documentos.isEmpty()) {
    			addCriteria(search, Criteria.not(Criteria.in("idProcessoDocumento", 
    				CollectionUtils.collect(documentos, new BeanToPropertyValueTransformer("idProcessoDocumento")).toArray())));
    		}
    	}
    	search.addOrder("o.dataInclusao", Order.ASC);
    	search.setMax(1);
    	List<ProcessoDocumento> ret = list(search);
    	return ret.isEmpty() ? null : ret.get(0);
    }

    private List<TipoProcessoDocumento> getTiposDocumentosRestritos() {
    	List<TipoProcessoDocumento> tipos = new ArrayList<TipoProcessoDocumento>(0);
    	ParametroUtil parametroUtil = ComponentUtil.getComponent(ParametroUtil.class);
    	tipos.add(parametroUtil.getTipoProcessoDocumentoDespacho());
    	tipos.add(parametroUtil.getTipoProcessoDocumentoSentenca());
    	tipos.add(parametroUtil.getTipoProcessoDocumentoDecisao());
    	if(!parametroUtil.isPrimeiroGrau()) {
        	tipos.add(parametroUtil.getTipoProcessoDocumentoVoto());
        	tipos.add(parametroUtil.getTipoProcessoDocumentoRelatorio());
        	tipos.add(parametroUtil.getTipoProcessoDocumentoEmenta());
        	tipos.add(parametroUtil.getTipoProcessoDocumentoAcordao());
    	}
    	return tipos;
    	
    }
    
    public ProcessoDocumento getUltimoProcessoDocumentoAssinado(
        TipoProcessoDocumento tipoProcessoDocumento, Processo processo) {
        return processoDocumentoDAO.getUltimoProcessoDocumentoAssinadoByProcessoTipoProcessoDocumento(tipoProcessoDocumento,
            processo);
    }

    public ProcessoDocumento getUltimoAcordaoPublicadoDejt(
        ProcessoTrf processoTrf) {
        return processoDocumentoDAO.getUltimoAcordaoPublicadoDejt(processoTrf);
    }

    public boolean isAcordaoPublicado(ProcessoDocumento processoDocumento) {
        if (processoDocumento == null) {
            return false;
        }

        return processoDocumentoDAO.isAcordaoPublicado(processoDocumento);
    }
    
    /**
	 * Método que retorna todos os documentos do processo que foram assinados por um magistrado
	 * @param idProcesso
	 * 			Identificador do processo que deseja trazer os documentos
	 * @return
	 */
	public List<ProcessoDocumento> getDocumentosAssinadosPorMagistradosBy(Integer idProcesso){
		if(idProcesso == null){
			return null;
		}
		return processoDocumentoDAO.getDocumentosAssinadosPorMagistradosBy(idProcesso);
	}
	
	/**
	 * Método que retorna todos os documentos do processo que foram assinados por um advogado ou por um procurador
	 * @param idProcesso
	 * 			Identificador do processo que deseja trazer os documentos
	 * @return
	 */
	public List<ProcessoDocumento> getDocumentosAssinadosPorAdvogadosOuProcuradoresBy(Integer idProcesso){
		if(idProcesso == null){
			return null;
		}
		return processoDocumentoDAO.getDocumentosAssinadosPorAdvogadosOuProcuradoresBy(idProcesso);
	}
	
	/**
	 * Informa se o documento cujo identificador foi dado pode ser assinado por detentor do papel indicado.
	 * O método indica a possibilidade de assinatura quando:
	 * <li>o identificador é não nulo e superior a 0</li>
	 * <li>o papel é não nulo</li>
	 * <li>o documento com identificador dado existe e ainda não foi juntado</li>
	 * <li>existe algum conteúdo vinculado ao documento</li>
	 * <li>o tipo do documento prevê, entre os papeis que podem assiná-lo, o papel informado</li>  
	 * 
	 * @param idDoc o identificador do documento
	 * @param papel o papel do pretenso signatário
	 * @return true, se o caso preenche os requisitos acima, false, caso contrário
	 */
	public boolean podeAssinar(Integer idDoc, Papel papel){
		if(idDoc == null || idDoc == 0 || papel == null){
			return false;
		}
		try {
			ProcessoDocumento doc = findById(idDoc);
			if(doc == null 
					|| !doc.getAtivo() 
					|| doc.getDataJuntada() != null 
					|| doc.getTipoProcessoDocumento() == null 
					|| doc.getProcessoDocumentoBin() == null
					|| doc.getProcessoDocumentoBin().isBinario() && (doc.getProcessoDocumentoBin().getNumeroDocumentoStorage() == null || doc.getProcessoDocumentoBin().getNumeroDocumentoStorage().isEmpty())
					|| !doc.getProcessoDocumentoBin().isBinario() && (doc.getProcessoDocumentoBin().getModeloDocumento() == null || doc.getProcessoDocumentoBin().getModeloDocumento().isEmpty())){
				return false;
			}
			Search s = new Search(TipoProcessoDocumentoPapel.class);
			addCriteria(s, 
					Criteria.equals("tipoProcessoDocumento.idTipoProcessoDocumento", doc.getTipoProcessoDocumento().getIdTipoProcessoDocumento()),
					Criteria.equals("papel.idPapel", papel.getIdPapel()));
			return count(s).intValue() > 0;
		} catch (PJeBusinessException e) {
			logger.error("Erro ao tentar identificar se o documento pode ser assinado: {0}", e.getLocalizedMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * PJEII-4328 - Utilziado também nos PJEII-4330 e PJEII-4997 
	 * Método que retorna O TipoProcessoDocumentoPapel com a devida permissão de assinatura no tipo de couemnto
	 * @param processoDoc: ProcessoDocumento
	 * @return TipoProcessoDocumentoPapel
	 */
	@SuppressWarnings("unchecked")
	public TipoProcessoDocumentoPapel liberaCertificacao(ProcessoDocumento processoDoc) 
	{
		TipoProcessoDocumentoPapel achou = null;
		if (processoDoc != null && !processoDoc.getAtivo()) 
		{
			return null;
		}
		int id = 0;
		
		if (processoDoc != null &&  processoDoc.getTipoProcessoDocumento() != null)
		{
			id = processoDoc.getTipoProcessoDocumento().getIdTipoProcessoDocumento();
		
	 		if (id != 0) 
	 		{
	 			EntityManager em = ProcessoDocumentoHome.instance().getEntityManager();
	 			
				StringBuilder sqlPes = new StringBuilder();
				sqlPes.append(" select o from ");
				sqlPes.append(" TipoProcessoDocumentoPapel o");
				sqlPes.append(" where o.tipoProcessoDocumento.idTipoProcessoDocumento = :id");
				Query query = em.createQuery(sqlPes.toString());
				query.setParameter("id", id);
				List<TipoProcessoDocumentoPapel> list = query.getResultList();
				UsuarioLocalizacao usuarioLocalizacaoAtual = (UsuarioLocalizacao) Contexts.getSessionContext().get("usuarioLogadoLocalizacaoAtual");
	
				int i = 0;
				while (achou == null && i < list.size()) 
				{
					if (usuarioLocalizacaoAtual.getPapel().getNome().equals(list.get(i).getPapel().getNome())
							&& !list.get(i).getExigibilidade().isSemAssinatura()) 
					{
						achou = list.get(i);
					}
					i++;
				}
			}
		}
		return achou;
	}

	public ProcessoDocumentoBinPessoaAssinatura pessoaDocumentoAssinado(Integer idpdBin) 
	{
		try 
		{
			StringBuilder sb = new StringBuilder();
			sb.append("select o from ProcessoDocumentoBinPessoaAssinatura o ");
			sb.append("where o.processoDocumentoBin.idProcessoDocumentoBin = :idpdBin");
			Query q = ProcessoDocumentoHome.instance().getEntityManager().createQuery(sb.toString()).setParameter("idpdBin", idpdBin);
			ProcessoDocumentoBinPessoaAssinatura retorno = (ProcessoDocumentoBinPessoaAssinatura) q.getSingleResult();
			return retorno;
		} 
		catch (NoResultException no) 
		{
			return null;
		}
	}
	/**
	 * Remove um ProcessoDocumento ou ProcessoDocumentoEstruturado a partir de uma variável de fluxo.
	 * Primeiro recupera o id associado a variável de fluxo passada como parametro
	 * Se não tiver nenhum id associado a variável, o método retorna sem fazer nada. 
	 * Depois testa se existe um ProcessoDocumentoEstruturado associado aquele id,
	 * se tiver remove toda a estrutura associada ao ProcessoDocumentoEstruturado.
	 * Se não tiver, tenta recuperar o ProcessoDocumento associado ao id.
	 * se tiver remove toda a estrutura associada ao ProcessoDocumento.
	 * Se não tiver, não faz nada.
	 * 
	 * @author Antonio Lucas
	 * @param var variável de fluxo associada ao id do texto sendo editado.
	 */
	public void removerDocumentoAPartirDeVariavalDeFluxo(String var) {
	    
		Object idProcessoDocumentoParaRemocao = Contexts.getBusinessProcessContext().get(var);
	  	    
	    if (Objects.nonNull(idProcessoDocumentoParaRemocao) ) {
	      String idProcessoDocumento = idProcessoDocumentoParaRemocao.toString();
	      ProcessoDocumentoEstruturado pde = EntityUtil.find(ProcessoDocumentoEstruturado.class, Integer.valueOf(idProcessoDocumento));
	      if (pde != null) {
	        ComponentUtil.getComponent(ProcessoDocumentoEstruturadoManager.class).removerProcessoDocumentoEstruturadoNaoAssinadoAtravesDeVariavelDoFluxo(var);
	      } else {
	        ProcessoDocumento pd = EntityUtil.find(ProcessoDocumento.class, Integer.valueOf(idProcessoDocumento));
	        if (pd != null) {
	          ComponentUtil.getComponent(ProcessoDocumentoHome.class).excluirDocumentoNaoAssinadoAtravesDeVariavelDeFluxo(var);
	        }
	      }
	    }
	  }

    /**
     * [PJEII-6117]
     * 
     * @param idProcesso Id do Processo
     * @return Lista de ProcessoDocumento que estão assinados e não possuem ProcessoExpediente 
     * associado (São os casos de Intimação, Citação, Comunicação, etc), ou seja, não foram
     * preparados pela atividade de fluxo Preparar Ato de Comunicação.
     * 
     * [PJEII-12221]
     * Adicionado o parametro idPessoaParte, pois a publicação de processo para mera publicidade, agora tem uma parte, que é uma 
     * pessoa cadastrada para esse fim, uma pessoa publica, que representa o DJE. O ID dessa pessoa é armazenado em parâmetro. 
     * É necessário para a correta consulta que o id da pessoa conste na query.
     * 
     */
	public List<ProcessoDocumento> getDocumentosAssinadosSemProcessoExpediente(Integer idProcesso, Integer idPessoaParte){
		if(idProcesso == null || idPessoaParte == null){
			return null;
		}
		return processoDocumentoDAO.getDocumentosAssinadosSemProcessoExpediente(idProcesso, idPessoaParte);
	}	
	
	/**
	 * 
	 * @param idProcesso
	 * @param idPessoaParte
	 * @return
	 * 
	 * [PJEII-12221]
	 * Consulta que retorna os documentos do processo que já foram enviados ao DJE para publicação em algum momento.
	 * O idPessoaParte é o id de um pessoa criada e consta em parâmetros do sistema. É uma pessoa genérica, para representar o DJE.
	 * 
	 */
	public List<ProcessoDocumento> getDocumentosAssinadosJaEnviadosParaPublicacaoDeProcesso(Integer idProcesso, Integer idPessoaParte){
		if(idProcesso == null || idPessoaParte == null){
			return null;
		}
		return processoDocumentoDAO.getDocumentosAssinadosJaEnviadosParaPublicacaoDeProcesso(idProcesso, idPessoaParte);
	}

	public Integer contagemDocumentos(ProcessoTrf processo,boolean incluirBinarios, boolean incluirComAssinaturaInvalida, TipoProcessoDocumento... tipos) throws PJeBusinessException{
		return processoDocumentoDAO.contagemDocumentos(processo, incluirBinarios, incluirComAssinaturaInvalida, tipos);
	}
	
	/**
	 * Recupera o objeto {@link ProcessoDocumento} associado ao objeto {@link ProcessoDocumentoBin}
	 * 
	 * @param processoDocumentoBin {@link ProcessoDocumentoBin}
	 * @return {@link ProcessoDocumento} ou null caso não for encontrado nenhum objeto associado.
	 */
	public ProcessoDocumento getProcessoDocumentoByProcessoDocumentoBin(ProcessoDocumentoBin processoDocumentoBin) {
		Search s = new Search(ProcessoDocumento.class);
		s.setMax(1);
		addCriteria(s, Criteria.equals("processoDocumentoBin", processoDocumentoBin));
		List<ProcessoDocumento> ret = list(s);
		return ret.isEmpty() ? null : ret.get(0);
	}

	/**
	 * Retorna todos os documentos do processo que não possuem assinatura exceto a petição inicial.
	 * 
	 * @param processo
	 * 			Processo que deseja trazer os documentos.
	 * @return documentos não assinados.
	 */
	public List<ProcessoDocumento> getDocumentosNaoAssinadosExcetoPeticao(Processo processo){
		List<ProcessoDocumento> resultado = new ArrayList<ProcessoDocumento>();
		try {
			if (processo != null && processo.getIdProcesso() > 0) {
				ProcessoTrf procTrf = ComponentUtil.getComponent(ProcessoJudicialService.class).findById(processo.getIdProcesso());
				Integer tipoPeticao = procTrf.getClasseJudicial().getTipoProcessoDocumentoInicial().getIdTipoProcessoDocumento();
				resultado = processoDocumentoDAO.getDocumentosNaoAssinadosExcetoTipo(processo, tipoPeticao);
			} 
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}

		return resultado;
	}
	
	/**
	 * Retorna uma lista de {@link ProcessoDocumento} de acordo com as seguintes restrições:
	 * <ul>
	 * 		<li>
	 * 			Usuário com o papel de Administrador obtém uma lista de todos os {@link ProcessoDocumento} 
	 * 			de acordo com o número do processo informado.
	 * 		</li>
	 * 		<li>
	 * 			Usuário com o papel de Procurador obtém uma lista de {@link ProcessoDocumento} 
	 * 			de acordo com o número do processo, o papel "procurador" e a localização informada.
	 *		</li>
	 * 		<li>
	 * 			Usuário com qualquer outro perfil obtém somente uma lista de {@link ProcessoDocumento} 
	 * 			de acordo com o número do processo informado e cujo identificador de quem inclui este {@link ProcessoDocumento} 
	 * 			seja o mesmo do usuário que realiza a pesquisa.
	 *		</li>
	 * </ul>
	 * 
	 * @param numeroProcesso Número do processo.
	 * @param usuario {@link Usuario}.
	 * @param papel {@link Papel}.
	 * @param localizacao {@link Localizacao}.
	 * 
	 * @return {@link List} de {@link ProcessoDocumento}.
	 */
	public  List<ProcessoDocumento> getProcessoDocumento(String numeroProcesso, Usuario usuario, Papel papel, Localizacao localizacao) {
    	Search search = new Search(ProcessoDocumento.class);
    	addCriteria(search, 
    			Criteria.equals("processo.numeroProcesso", numeroProcesso),
    			Criteria.not(Criteria.isNull("dataJuntada")),
    			Criteria.equals("ativo", Boolean.TRUE));
    	
    	if(!Authenticator.isLogouComCertificado()){
    		addCriteria(search, Criteria.equals("documentoSigiloso", Boolean.FALSE));
    	}
    	
    	if (!StringUtils.equalsIgnoreCase(papel.getIdentificador(), "admin")) {
        	if (papel.getIdentificador().equals("procurador")) {
        		addCriteria(search, Criteria.in("usuarioInclusao.idUsuario", getIds(localizacao,"procurador")));
        	} else {
        		addCriteria(search, Criteria.equals("usuarioInclusao.idUsuario", usuario.getIdUsuario()));
        	}    		
    	}
    	
    	search.addOrder("dataJuntada", Order.DESC);
    	search.addOrder("dataInclusao", Order.DESC);
    	
    	return list(search);
	}
	
	
	public String getIdsString(Localizacao localizacao) {
		StringBuilder aux = new StringBuilder();
		int count = 0;
		for (Integer id : getIds(localizacao,"procurador","advogado")) {
			
			if(count > 0){
				aux.append(", ");
			}
			aux.append(id);
			count++;
		}
		return aux.toString();
	}

	/**
	 * Retorna todos os identificadores dos usuários que pertencem a uma determinada localização.
	 * 
	 * @param localizacao {@link Localizacao}
	 * 
	 * @return Array de inteiros que contém os identificadores dos usuários que pertencem a uma determinada localização.
	 */
	//Mantendo comportamento anterior
	public Integer[] getIds(Localizacao localizacao) {
		return getIds(localizacao, "procurador");
	}
	
	/**
	 * Retorna todos os identificadores dos usuários que pertencem a uma determinada localização.
	 * 
	 * @param localizacao {@link Localizacao}
	 * @param identificador 
	 * 
	 * @return Array de inteiros que contém os identificadores dos usuários que pertencem a uma determinada localização.
	 */
	public Integer[] getIds(Localizacao localizacao, String... identificador) {
		Search search = new Search(UsuarioLocalizacao.class);
		addCriteria(search, 
				Criteria.in("papel.identificador", identificador),
				Criteria.equals("localizacaoFisica.idLocalizacao", localizacao.getIdLocalizacao()));
		
		List<UsuarioLocalizacao> list = list(search);
		
		if (list != null && !list.isEmpty()) {
			int count = 0;
			Integer[] ids = new Integer[list.size()];
			
			for (UsuarioLocalizacao usuarioLocalizacao : list) {
				ids[count++] = usuarioLocalizacao.getUsuario().getIdUsuario();
			}
			
			return ids;
		}
		return null;
	}
	
	
	
	/**
	 * Atualiza o processo documento com os dados do DownloadBinarioArquivo
	 * exclui o downloadBinarioArquivo caso a operação seja bem sucedida
	 *
	 * @return true para sucesso
	 */
	@Transactional
	public synchronized boolean atualizarProcessoDocumentoComDownloadBinarioArquivo(DownloadBinarioArquivo arquivoDiferido, int tamanho, Object dados, Date dataJuntada, boolean isHTML, String mimeType){
		int idProcessoDocumentoBin =  arquivoDiferido.getIdProcessoDocumentoBin();
		boolean ret = false;
		Thread t = Thread.currentThread();
		logger.debug("Thread {0} iniciando atualização de documentos. Arquivo: {1}", t.getId(),arquivoDiferido.getId());
		try {
			atualizarDataJuntadaConsolidacao(idProcessoDocumentoBin, dataJuntada);
			if (isHTML){
				ret = ComponentUtil.getComponent(ProcessoDocumentoBinManager.class).atualizarProcessoDocumentoBinHtml(idProcessoDocumentoBin, tamanho, dados, mimeType);
			}else //arquivos binários
			{
				ret = ComponentUtil.getComponent(ProcessoDocumentoBinManager.class).atualizaProcessoDocumentoBinBinarios(idProcessoDocumentoBin, tamanho, dados, mimeType);
				
			}
			if (ret) {
				ComponentUtil.getComponent(DownloadBinarioArquivoManager.class).removeById(arquivoDiferido.getId());
			}
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		}
		logger.info("Thread {0} finalizando a atualização de documentos. Arquivo: {1}", t.getId(),arquivoDiferido.getId());
		return ret;
	}
	
	
	private void atualizarDataJuntadaConsolidacao(int idProcessoDocumentoBin, Date dataJuntada) throws PJeBusinessException{
		Thread t = Thread.currentThread();
		logger.debug("Thread {0} iniciando atualização da data de juntada. Arquivo: {1}", t.getId(),idProcessoDocumentoBin);
		ProcessoDocumentoBin procDocBin = new ProcessoDocumentoBin();
		procDocBin.setIdProcessoDocumentoBin(idProcessoDocumentoBin);
		List<ProcessoDocumento> procDocs = getDAO().findByProcessoDocumentoBin(procDocBin);
		//verifica se há mais de um processoDocumento para o documentoBin
		if (procDocs.size()>1){
			int idPrimeiroDocumento = getIdPrimeiroDocumentoJuntado(procDocs);
			for (ProcessoDocumento processoDocumento : procDocs) {
				
				if (idPrimeiroDocumento==processoDocumento.getIdProcessoDocumento()){
					processoDocumento.setDataJuntada(dataJuntada);
				}else
				{
					processoDocumento.setDataJuntada(processoDocumento.getDataInclusao());
				}
				processoDocumentoDAO.merge(processoDocumento);
			}	
		}else
		{
			ProcessoDocumento processoDocumento = procDocs.get(0);
			processoDocumento.setDataJuntada(dataJuntada);
			processoDocumentoDAO.merge(processoDocumento);
			
		}
		processoDocumentoDAO.flush();

	}
	
	private int getIdPrimeiroDocumentoJuntado (List<ProcessoDocumento> procDocs){
		
		List<Integer> ids = new ArrayList<Integer>();
		for (ProcessoDocumento processoDocumento : procDocs) {
			ids.add(processoDocumento.getIdProcessoDocumento());
		}
		return Collections.min(ids);
		
		
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

	public List<ProcessoDocumento> listDocumentosNaoLidos(
			ProcessoTrf processoJudicial) {
		return processoDocumentoDAO.listDocumentosNaoLidos(processoJudicial);
	}
	
	public List<ProcessoDocumento> getDocumentosNaoLidos(ProcessoTrf processo, TipoProcessoDocumento[] tipos){
    	List<Papel> papeisDocs = ComponentUtil.getComponent(PapelManager.class).getPapeisParaDocumentosNaoLidos(); 
    	Papel papeis[] = papeisDocs.toArray(new Papel[papeisDocs.size()]);
    	return this.processoDocumentoDAO.getDocumentosNaoLidos(processo, papeis, tipos);
	}

    public List<ProcessoDocumento> findAllExceto(ProcessoTrf processoTrf,
			Integer idTipoDocumentoExceto) {
		return processoDocumentoDAO.findAllExceto(processoTrf,idTipoDocumentoExceto);
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
    public List<ProcessoDocumento> getDocumentosVinculados(ProcessoDocumento processoDocumento){
    	return getDocumentosVinculados(processoDocumento, false);
    }
    
    /**
	 * Recupera a lista de documentos ativos anexos pelo componente de upload ordenados pela ordem de inclusão dos mesmos.
	 * 
	 * @param processo
	 * @return lista com os documentos ativos anexos por upload
	 */
    public List<ProcessoDocumento> getListaDocumentosAnexosPorUpload(ProcessoTrf processo){
    	return processoDocumentoDAO.getListaDocumentosAnexosPorUpload(processo);
    }
    
	/**
	 * Método realiza o download do documento para o usuário
	 * 
	 * @param processoDocumento
	 * @throws PJeBusinessException, Exception 
	 */
	public void downloadDocumento(ProcessoDocumento processoDocumento) throws PJeBusinessException, Exception {
		String nome;
		String contentType;
		byte[] dados = null;
		
			if (processoDocumento.getProcessoDocumentoBin().isBinario() ) {
				nome = processoDocumento.getProcessoDocumentoBin().getNomeArquivo();
				contentType = "application/octet-stream";
				dados = ComponentUtil.getComponent(ProcessoDocumentoBinManager.class).getBinaryData(processoDocumento.getProcessoDocumentoBin() );
			}
			else {
				nome = processoDocumento.getProcessoDocumento() + ".html";			
				contentType = "text/html";
				dados = processoDocumento.getProcessoDocumentoBin().getModeloDocumento().getBytes();
			}
			ProjetoUtil.downloadDocumento(nome,	contentType, dados);
	}

	/**
	 * Método responsável por realizar a verificação para exibição ou não do cadeado de assinatura no agrupador
	 * de documentos do processo.
	 * Obs.: Somente para processos "E" Em elaboração ou que não estejam distribuídos e de acordo com as regras
	 * de negócio.
	 * 
	 * @param idProcessoTrf
	 * @param processoDocumento
	 * @return true se exibe o cadeado do assinador 
	 */
	public Boolean exibeCadeadoAssinadorProcessoNaoProtocolado(Integer idProcessoTrf, ProcessoDocumento documento) {
		return isPermiteAssinarPorPapelTipoProcessoDocumento(documento.getTipoProcessoDocumento());
	}
	
	/**
 	 * Metodo que verifica se o papel do usurio logado tem permissao para assinar o documento pelo tipo
 	 * @return boolean
 	 */
 	public boolean isPermiteAssinarPorPapelTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento){
 		return ComponentUtil.getComponent(TipoProcessoDocumentoPapelService.class).verificarExigibilidadeAssina(
 				Authenticator.getPapelAtual(),tipoProcessoDocumento);
	}
	
	/**
	 * Método que verifica as regras do documento PAI para exibição do cadeado 
	 * se o documento passado não está assinado, se não é justiça de trabalho, permite editar os documentos e 
	 * se exibe o cadeado.
	 * 
	 * @param processoDocumento, exibeCadeadoAssinado
	 * @return true se exibe o cadeado
	 */
	public boolean exibeCadeadoDocumentoPai(ProcessoDocumento processoDocumento, boolean exibeCadeadoAssinado){
		boolean retorno = false;
		preencheVariaveisExibicaoCadeado(processoDocumento);
		retorno = (!documentoAssinado && !justicaTrabalho && permiteEditarDocs && exibeCadeadoAssinado
				&& isPermiteAssinarPorPapelTipoProcessoDocumento(processoDocumento.getTipoProcessoDocumento()));
		return retorno;
	}
	
	/**
	 * Método que verifica as regras do documento FILHO para exibição do cadeado 
	 * se o documento passado não está assinado, se não é justiça de trabalho, 
	 * permite editar os documentos, se exibe o cadeado e se o documento pai está assinado.
	 * 
	 * @param processoDoc
	 * @param exibeCadeadoAssinado 
	 * @return true se exibe o cadeado
	 */
	public boolean exibeCadeadoDocumentoFilho(ProcessoDocumento processoDoc, boolean exibeCadeadoAssinado){
		boolean retorno = false;
		preencheVariaveisExibicaoCadeado(processoDoc);
		boolean docPaiAssinado = ComponentUtil.getComponent(DocumentoJudicialService.class).temAssinatura(processoDoc.getDocumentoPrincipal());
		retorno = (!documentoAssinado && !justicaTrabalho && permiteEditarDocs && exibeCadeadoAssinado && docPaiAssinado
				&& isPermiteAssinarPorPapelTipoProcessoDocumento(processoDoc.getTipoProcessoDocumento()));
		return retorno;
	}
	
	private void preencheVariaveisExibicaoCadeado(ProcessoDocumento processoDocumento) {
		this.documentoAssinado = ComponentUtil.getComponent(DocumentoJudicialService.class).temAssinatura(processoDocumento);
		this.justicaTrabalho = ParametroJtUtil.instance().justicaTrabalho();
		this.permiteEditarDocs = 
				(!util.getUrlRequest().contains("detalheProcessoPrevento") 
			 && (!util.getUrlRequest().contains("AcessoTerceiros")) ? true : false);
	}
	
	/**
	 * Método que verifica as regras do documento FILHO de processos protocolados para exibição do cadeado 
	 * se o documento pai está assinado mediante o documento passado, se o documento passado também não está assinado e 
	 * se o documento pai não está assiando para exibição do cadeado.
	 * 
	 * @param processoDoc
	 * @return true se exibe o cadeado
	 */
	public boolean exibeCadeadoDocumentoPaiFilhoProcessoProtocolado(
			ProcessoDocumento processoDoc) {
		boolean retorno = isPermiteAssinarPorPapelTipoProcessoDocumento(processoDoc.getTipoProcessoDocumento());
		if (processoDoc.isDocumentoPai()){
			if (processoDoc.getExclusivoAtividadeEspecifica()){
				retorno = false;
			}
		} else {
			if (processoDoc.getDocumentoPrincipal().getExclusivoAtividadeEspecifica()){
				retorno = false;
			}
		}

		if (retorno){
			boolean docAssinado = ComponentUtil.getComponent(DocumentoJudicialService.class).temAssinatura(processoDoc);
			boolean docPaiAssinado = false;
			if(processoDoc.getDocumentoPrincipal() != null){
				docPaiAssinado = ComponentUtil.getComponent(DocumentoJudicialService.class).temAssinatura(processoDoc.getDocumentoPrincipal());
				retorno = (!docAssinado && docPaiAssinado);
			}
			else{
				retorno = !docAssinado;
			}
		}
		return retorno;
	}

    public ProcessoDocumento recuperaDocumentoNaoAssinadoPorTarefa(Integer idProcesso, Long idTaskInstance){
        return processoDocumentoDAO.recuperaDocumentoNaoAssinadoPorTarefa(idProcesso, idTaskInstance);
    }
    
    /**
	 * Retorna true se o documento possuir tipo de documento com fluxo configurado.
	 * 
	 * @param documento ProcessoDocumento
	 * @return booleano.
	 */
	public boolean isDocumentoComFluxoConfigurado(ProcessoDocumento documento) {
		
		return (documento != null && 
				documento.getIdProcessoDocumento() > 0 && 
				documento.getTipoProcessoDocumento() != null && 
				documento.getTipoProcessoDocumento().getFluxo() != null);
	}
	
	/**
	 * metodo responsavel por retornar a contagem de documentos anexos do documento principal passado em parametro.
	 * regra: conta todos os ProcessosDocumentos onde
     * -> o documento principal seja o passado em parametro.
     * -> o processoTrf seja igual ao do documento principal.
     * -> a data de juntada nao seja nula (somente documentos assinados)
	 * @param procDocPrincipal
	 * @return
	 */
	public int contagemDocumentosAnexos(ProcessoDocumento procDocPrincipal) {
		return processoDocumentoDAO.contagemDocumentosAnexos(procDocPrincipal);
	}
	
	
	
	/**
	 * metodo responsavel por retornar o processo documento pelo numero da ordem.
	 * regra: conta todos os ProcessosDocumentos onde
     * -> o documento principal seja o passado em parametro.
     * -> o processoTrf seja igual ao do documento principal.
	 * @param processoDocumento
	 * @param idNumeroOrdem
	 * @return
	 */
	public ProcessoDocumento getAnexoByNumeroOrdem(ProcessoDocumento processoDocumento, Integer idNumeroOrdem) {
		return processoDocumentoDAO.getAnexoByNumeroOrdem(processoDocumento, idNumeroOrdem);
	}
	
	/**
	 * Lista os documentos principais do processo cujos tipos sejam conforme os parâmetros ou, quando vinculados, cujo tipo do documento principal seja conforme os parâmetros
	 * @param processo
	 * @param tipos
     * @return List<ProcessoDocumento>
	 */
	public List<ProcessoDocumento> listarDocumentosPrincipais(ProcessoTrf processo, Integer... tipos) {
		return processoDocumentoDAO.listarDocumentosPrincipais(processo, tipos);
	}
	
	public List<ProcessoDocumento> getDocumentosVinculados(ProcessoDocumento processoDocumento,
			Boolean filtrarDocumentosSigilosos) {
		if (filtrarDocumentosSigilosos) {
			return ComponentUtil.getProcessoTrfHome().consultarDocumentoJuntadoEhCiente(
					processoDocumento.getProcessoTrf(), null, processoDocumento.getIdProcessoDocumento());
		} else {
			return processoDocumentoDAO.getDocumentosVinculados(processoDocumento);
		}
	}
	
	/**
	 * Método responsável por verificar se existe um documento de um processo a
	 * partir do número do documento na instância de origem especificado
	 * 
	 * @param idProcesso
	 *            Código identificador do processo
	 * @param identificador
	 * 			  Código do documento
	 * @return Boolean - True se existe o documento, False se não existe
	 */
	public Boolean existeDocumentoPorIdentificadorInstanciaOrigem(Integer idProcesso, String identificador) {
		ProcessoDocumento pd = buscaDocumentoPorIdentificadorInstanciaOrigem(idProcesso, identificador);
		return (pd != null);
	}

	/**
	 * Retorna o documento proveniente de outra instância e o processo atual.
	 * 
	 * @param idProcesso Identificador do processo.
	 * @param identificador Identificador do documento originário de outra instância.
	 * @return ProcessoDocumento.
	 */
	private ProcessoDocumento buscaDocumentoPorIdentificadorInstanciaOrigem(Integer idProcesso, String identificador) {
		if (identificador == null)
			return null;
        ProcessoDocumento pd = null;
        try {
            pd = getDAO().recuperarDocumentoPorIdentificadorInstanciaOrigem(idProcesso, identificador);
        } catch (Exception e) {
            throw new NegocioException(
                    "Identificador " + identificador + " não é válido para recuperar um processo documento");
        }
        return pd;
	}

	public ProcessoDocumento registrarProcessoDocumento(String conteudo, String descricao, TipoProcessoDocumento tipo, ProcessoTrf processo) throws PJeBusinessException {
		Date dataInclusao = new Date();
		ProcessoDocumentoBin pdb = getProcessoDocumentoBinManager().inserirProcessoDocumentoBin(dataInclusao, conteudo);
		ProcessoDocumento pd = new ProcessoDocumento();
		pd.setProcessoDocumento(descricao);
		pd.setDataInclusao(dataInclusao);
		pd.setProcessoDocumentoBin(pdb);
		pd.setTipoProcessoDocumento(tipo);
		pd.setProcesso(processo.getProcesso());
		pd.setLocalizacao(Authenticator.getLocalizacaoFisicaAtual());
		pd.setPapel(Authenticator.getUsuarioLocalizacaoAtual().getPapel());
		pd.setNomeUsuarioInclusao(Authenticator.getUsuarioLogado().getNome());
		this.persist(pd);
		return ComponentUtil.getDocumentoJudicialService().finalizaDocumento(pd, processo, null, false, false);
	}

	
	public boolean trasladarDocumentos(ProcessoTrf processoDestino, List<ProcessoDocumento> listaDocumentos,
			boolean atualizaDataJuntada) throws PJeBusinessException {
		return copiarDocumentosTraslado(processoDestino, listaDocumentos, atualizaDataJuntada);
	}

	private boolean copiarDocumentosTraslado(ProcessoTrf processoDestino, List<ProcessoDocumento> listaDocumentos,
			boolean atualizaDataJuntada) throws PJeBusinessException {
		EntityManager em = EntityUtil.getEntityManager();
		Date novaDataJuntada = atualizaDataJuntada ? new Date() : null;
		int idPeticaoInicial = ParametroUtil.instance().getTipoProcessoDocumentoPeticaoInicial().getIdTipoProcessoDocumento();
		String idPeticaoInicialTrasladada = ParametroUtil.instance().getIdPeticaoInicialTrasladada();
		for (ProcessoDocumento documento : listaDocumentos) {
			Set<ProcessoDocumento> documentosVinculados = documento.getDocumentosVinculados();
			ProcessoDocumentoBin pdb = getProcessoDocumentoBinManager().findById(documento.getProcessoDocumentoBin().getIdProcessoDocumentoBin());
			em.detach(documento);
			documento.setIdProcessoDocumento(0);
			documento.setProcesso(processoDestino.getProcesso());
			documento.setProcessoTrf(processoDestino);
			documento.setProcessoDocumentoBin(pdb);
			if (!verificarPeticaoInicialTrasladada(atualizaDataJuntada, novaDataJuntada, idPeticaoInicial,
					idPeticaoInicialTrasladada, documento)) {
				return false;
			}
			if (atualizaDataJuntada && novaDataJuntada != null) {
				documento.setDataJuntada(novaDataJuntada);
			}
			em.persist(documento);
			copiarDocumentosVinculadosTraslado(processoDestino, atualizaDataJuntada, em, novaDataJuntada, documento,
					documentosVinculados);
		}
		lancarMovimentacaoTraslado(processoDestino);
		em.flush();
		return true;
	}

	private boolean verificarPeticaoInicialTrasladada(boolean atualizaDataJuntada, Date novaDataJuntada,
			int idPeticaoInicial, String idPeticaoInicialTrasladada, ProcessoDocumento documento)
			throws PJeBusinessException {
		if (documento.getTipoProcessoDocumento().getIdTipoProcessoDocumento() == idPeticaoInicial) {
			if (idPeticaoInicialTrasladada != null) {
				TipoProcessoDocumento tipoProcessoDocumentoPeticaoInicial = ((TipoProcessoDocumentoManager) ComponentUtil
						.getComponent(TipoProcessoDocumentoManager.class))
								.findByCodigoTipoProcessoDocumento(idPeticaoInicialTrasladada);
				documento.setTipoProcessoDocumento(tipoProcessoDocumentoPeticaoInicial);
			} else {
				FacesMessages.instance().add(Severity.ERROR,
						"Parmetro pje:documento:traslado:idPeticaoInicialTrasladada no cadastrado");
				return false;
			}
		}
		return true;
	}

	private void lancarMovimentacaoTraslado(ProcessoTrf processoDestino) {
		String codigoMovimento = ParametroUtil.instance().getIdMovimentoTraslado();
		if (codigoMovimento != null) {
			MovimentoAutomaticoService.preencherMovimento().deCodigo(codigoMovimento)
					.associarAoProcesso(processoDestino).lancarMovimento();
		}
	}

	private void copiarDocumentosVinculadosTraslado(ProcessoTrf processoDestino, boolean atualizaDataJuntada,
			EntityManager em, Date novaDataJuntada, ProcessoDocumento documento,
			Set<ProcessoDocumento> documentosVinculados) throws PJeBusinessException {
		for (ProcessoDocumento documentoVinculado : documentosVinculados) {
			em.detach(documentoVinculado);
			ProcessoDocumentoBin pdb = getProcessoDocumentoBinManager().findById(documentoVinculado.getProcessoDocumentoBin().getIdProcessoDocumentoBin());
			documentoVinculado.setDocumentoPrincipal(documento);
			documentoVinculado.setIdProcessoDocumento(0);
			documentoVinculado.setProcesso(processoDestino.getProcesso());
			documentoVinculado.setProcessoTrf(processoDestino);
			documentoVinculado.setProcessoDocumentoBin(pdb);
			if (atualizaDataJuntada && novaDataJuntada != null) {
				documentoVinculado.setDataJuntada(novaDataJuntada);
			}
			em.persist(documentoVinculado);
		}
	}

	public void vincularDocumentos(ProcessoDocumento documentoPrincipal, List<ProcessoDocumento> documentosVincular) throws PJeBusinessException {
		if (documentoPrincipal != null) {
			for (ProcessoDocumento pd: documentosVincular) {
				if (!pd.equals(documentoPrincipal)) {
					pd.setDocumentoPrincipal(documentoPrincipal);
					this.merge(pd);
				}
			}
		}
		
	}

	public ProcessoDocumento getUltimoAtoProferido(Integer idProcesso) {
		return processoDocumentoDAO.getUltimoAtoProferido(idProcesso);
	}
	
	/** 
	 * Pesquisa os documentos nã assinados produzidos por usuários internos.
	 *
	 * @param processo
	 * @return Lista de documentos não assinados produzidos por usuários internos.
	 * @author Guilherme Bispo
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> getDocumentosNaoAssinadosUsuarioInterno(int idProcesso) throws PJeBusinessException {

		StringBuffer sb = new StringBuffer();
		sb.append("select pd from ProcessoDocumento pd ");
		sb.append("inner join pd.processoDocumentoBin pdb ");
		sb.append("where not exists (select 1 from ProcessoDocumentoBinPessoaAssinatura pdba ");
		sb.append(" where pdba.processoDocumentoBin.idProcessoDocumentoBin = pdb.idProcessoDocumentoBin)");
		sb.append(" and pd.ativo=true and pd.processo.idProcesso = :idProcesso ");
		sb.append("order by pd.dataInclusao desc ");
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter("idProcesso", idProcesso);

		List<ProcessoDocumento> documentoNaoAssinadoList = (List<ProcessoDocumento>) q.getResultList();
		List<ProcessoDocumento> retornoList = new ArrayList<ProcessoDocumento>();

		boolean usuarioInterno;

		for (ProcessoDocumento documento : documentoNaoAssinadoList) {

			usuarioInterno = !ProcessoDocumentoHome.isUsuarioExterno(documento);

			if (usuarioInterno) {
				retornoList.add(documento);
			}
		}

		return retornoList;
	}
	
	public byte[] gerarDocumentoComAssinatura(ProcessoDocumento processoDocumento, Boolean comPaginaDetalhes, 
			List<Element> elementosInicioDocumento,
			List<Element> elementosFinalDocumento) throws PontoExtensaoException {
		List<ProcessoDocumento> documentos = new ArrayList<>();
		documentos.add(processoDocumento);
		
		GeradorPdfUnificado geradorPdf = new GeradorPdfUnificado();
		geradorPdf.setResurcePath(new Util().getUrlProject());
				
		try {
			byte[] result = null;

			ByteArrayOutputStream byteArray = null;
			try {
				byteArray = new ByteArrayOutputStream();
			
				geradorPdf.gerarPdfUnificadoDetalhes(processoDocumento.getProcessoTrf(), documentos, comPaginaDetalhes, elementosInicioDocumento, elementosFinalDocumento, byteArray);
				
				result = byteArray.toByteArray();
			} finally {
				if (byteArray != null) {
					try {
						byteArray.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return result;
		} catch (PdfException e) {
			throw new PontoExtensaoException(e.getCause());
		}
	}
}
