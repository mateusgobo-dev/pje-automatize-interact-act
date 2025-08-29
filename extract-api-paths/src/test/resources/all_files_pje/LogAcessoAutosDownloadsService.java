package br.jus.cnj.pje.nucleo.service;

import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.entity.log.LogException;
import br.com.infox.ibpm.entity.log.LogUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.log.LogAcessoAutos;
import br.jus.pje.nucleo.entidades.log.LogDownloadDocumentos;
import br.com.itx.component.Util;

@Name(LogAcessoAutosDownloadsService.NAME)
@Scope(ScopeType.EVENT)
public class LogAcessoAutosDownloadsService extends BaseService {

    public static final String NAME = "LogAcessoAutosDownloadsService";
    private static final String TIPO_ACESSO_LOG_PADRAO = "WEB";
    private static final String TIPO_ACESSO_LOG_MNI = "MNI";

    private static final LogProvider logger = Logging.getLogProvider(LogAcessoAutosDownloadsService.class);

    private void insereLogDownloadDocumento(LogDownloadDocumentos logDownloadDocumentos) {
        try {
			Runnable task = () -> {
				Lifecycle.beginCall();
         	   
               	Util.beginTransaction();
            	   
                try {	
                    EntityManager entityManagerLog = ComponentUtil.getComponent("entityManagerLog");
        			entityManagerLog.persist(logDownloadDocumentos);
        			entityManagerLog.flush();
        			Util.commitTransction();
                }
                catch (final Exception e) {					 
					Util.rollbackTransaction();
                }
                finally {
                	Lifecycle.endCall();
                }
			};
			
			new Thread(task).start();
        } catch (Exception e) {
			logger.error("Falha ao gerar log de download de documentoss: " + e.getMessage());
		}
    }

    private void insereLogAcessoAutos(LogAcessoAutos logAcessoAutos) {
        try {
			Runnable task = () -> {
				Lifecycle.beginCall();
         	   
               	Util.beginTransaction();
            	   
                try {	
                    EntityManager entityManagerLog = ComponentUtil.getComponent("entityManagerLog");
        			entityManagerLog.persist(logAcessoAutos);
        			entityManagerLog.flush();
        			Util.commitTransction();
                }
                catch (final Exception e) {					 
					Util.rollbackTransaction();
                }
                finally {
                	Lifecycle.endCall();
                }
			};
			
			new Thread(task).start();
        } catch (Exception e) {
			logger.error("Falha ao gerar log de acesso aos autos: " + e.getMessage());
		}
    }

    public void logarDownload(ProcessoDocumento documento, HttpServletRequest request) {
        if (isLogDownloadDocumentosInativo())
            return;
        gravarLogDownloadMNI(documento, request);
    }

    public void logarDownload(ProcessoDocumento documento) {
        this.logarDownload(documento, LogUtil.getRequest());
    }

    public void logarDownloadMNI(ProcessoDocumento documento) {
        if (isLogAcessoMNIInativo())
            return;
        HttpServletRequest request = LogUtil.getRequest();
        gravarLogDownloadMNI(documento, request);
    }

    private void gravarLogDownloadMNI(ProcessoDocumento documento, HttpServletRequest request) {
        try {
            UsuarioLocalizacao loc = null;
            String url = LogUtil.getUrlRequest(request);
            String ip = LogUtil.getIpRequest(request);

            if (Contexts.getSessionContext().get(Authenticator.USUARIO_LOCALIZACAO_ATUAL) instanceof UsuarioLocalizacao) {
                loc = (UsuarioLocalizacao) Contexts.getSessionContext().get(Authenticator.USUARIO_LOCALIZACAO_ATUAL);
            } else {
                return;
            }

            if (loc.getIdUsuarioLocalizacao() != 0) {
                LogDownloadDocumentos logDownloadDocumentos = new LogDownloadDocumentos();
                logDownloadDocumentos.setIdUsuario(Long.valueOf(loc.getUsuario().getIdUsuario()));
                logDownloadDocumentos.setIdUsuarioLocalizacao(Long.valueOf(loc.getIdUsuarioLocalizacao()));
                logDownloadDocumentos.setIdProcesso(Long.valueOf(documento.getProcesso().getIdProcesso()));
                logDownloadDocumentos.setFoiPdfUnificado(false);
                logDownloadDocumentos.setIdProcessoDocumento(Long.valueOf(documento.getIdProcessoDocumento()));
                logDownloadDocumentos.setUrl(url);
                logDownloadDocumentos.setIp(ip);
                insereLogDownloadDocumento(logDownloadDocumentos);
            }
        } catch (LogException e) {
            logger.error(request, e);
        }
    }

    public void logarDownloadPdfUnificado(ProcessoDocumento primeiroDocumento, ProcessoDocumento ultimoDocumento) {
        if(isLogDownloadDocumentosInativo())
            return;

		try {
            UsuarioLocalizacao loc = null;
            String url = LogUtil.getUrlRequest();
            String ip = LogUtil.getIpRequest();        
        
            if (Contexts.getSessionContext().get(Authenticator.USUARIO_LOCALIZACAO_ATUAL) instanceof UsuarioLocalizacao) {
                loc = (UsuarioLocalizacao) Contexts.getSessionContext().get(Authenticator.USUARIO_LOCALIZACAO_ATUAL);
            } else {
                return;
            }
            
            if (loc.getIdUsuarioLocalizacao() != 0) {
                LogDownloadDocumentos logDownloadDocumentos = new LogDownloadDocumentos();
                logDownloadDocumentos.setIdUsuario(Long.valueOf(loc.getUsuario().getIdUsuario()));
                logDownloadDocumentos.setIdUsuarioLocalizacao(Long.valueOf(loc.getIdUsuarioLocalizacao()));
                logDownloadDocumentos.setIdProcesso(Long.valueOf(primeiroDocumento.getProcesso().getIdProcesso()));
                logDownloadDocumentos.setFoiPdfUnificado(true);
                logDownloadDocumentos.setPrimeiroIdProcessoDocumento(Long.valueOf(primeiroDocumento.getIdProcessoDocumento()));
                logDownloadDocumentos.setUltimoIdProcessoDocumento(Long.valueOf(ultimoDocumento.getIdProcessoDocumento()));
                logDownloadDocumentos.setUrl(url);
                logDownloadDocumentos.setIp(ip);
                insereLogDownloadDocumento(logDownloadDocumentos);
            }
	    } catch (LogException e) {
            logger.error(primeiroDocumento, e);
        }
    }

    public void logarAcessoAosAutos(ProcessoTrf processo) {
        if(isLogAcessoAutosInativo())
            return;

        logarAcessoProcessoTipoVariado(processo, TIPO_ACESSO_LOG_PADRAO);
    }

    public void logarAcessoProcessoTipoVariado(ProcessoTrf processo, String tipoAcesso) {
        if(isLogAcessoAutosInativo())
            return;

        guardarLogAcessoTipoVariado(processo, tipoAcesso);
    }

    public void logarAcessoProcessoMNI(ProcessoTrf processo) {
        if(isLogAcessoMNIInativo())
            return;

        guardarLogAcessoTipoVariado(processo, TIPO_ACESSO_LOG_MNI);
    }

    private void guardarLogAcessoTipoVariado(ProcessoTrf processo, String tipoAcesso) {
        try {
            UsuarioLocalizacao loc = null;
            String ip = LogUtil.getIpRequest();

            if (Contexts.getSessionContext().get(Authenticator.USUARIO_LOCALIZACAO_ATUAL) instanceof UsuarioLocalizacao) {
                loc = (UsuarioLocalizacao) Contexts.getSessionContext().get(Authenticator.USUARIO_LOCALIZACAO_ATUAL);
            } else {
                return;
            }

            if (loc.getIdUsuarioLocalizacao() != 0) {
                LogAcessoAutos logAcessoAutos = new LogAcessoAutos();
                logAcessoAutos.setIdUsuario(Long.valueOf(loc.getUsuario().getIdUsuario()));
                logAcessoAutos.setIdUsuarioLocalizacao(Long.valueOf(loc.getIdUsuarioLocalizacao()));
                logAcessoAutos.setIdProcesso(Long.valueOf(processo.getIdProcessoTrf()));
                logAcessoAutos.setIp(ip);
                logAcessoAutos.setSigiloso(processo.getSegredoJustica());
                logAcessoAutos.setNivelAcesso(processo.getNivelAcesso());
                logAcessoAutos.setTipoAcesso(tipoAcesso);
                insereLogAcessoAutos(logAcessoAutos);
            }
        } catch (LogException e) {
            logger.error(processo, e);
        }
    }

    private boolean isLogAcessoAutosInativo() {
        return Boolean.FALSE.equals(ParametroUtil.instance().isLogAcessoAutosAtivo());
    }

    private boolean isLogDownloadDocumentosInativo(){
        return Boolean.FALSE.equals(ParametroUtil.instance().isLogDownloadDocumentosAtivo());
    }

    private boolean isLogAcessoMNIInativo(){
        return Boolean.FALSE.equals(ParametroUtil.instance().isLogAcessoMNIAtivo());
    }
}
