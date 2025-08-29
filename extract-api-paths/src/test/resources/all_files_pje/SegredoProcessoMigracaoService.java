package br.jus.cnj.pje.nucleo.service;

import static br.com.itx.util.EntityUtil.getEntityManager;

import java.io.Serializable;
import java.util.Date;

import javax.ws.rs.NotFoundException;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;

import br.com.infox.exceptions.NegocioException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoSegredoManager;
import br.jus.pje.nucleo.entidades.ProcessoSegredo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.ProcessoTrfApreciadoEnum;
import br.jus.pje.nucleo.enums.SegredoStatusEnum;

@Name("segredoProcessoService")
@Transactional
public class SegredoProcessoMigracaoService implements Serializable {
	
    private static final long serialVersionUID = 1L;

    @In
    private ProcessoJudicialService processoJudicialService;

    @In
    private UsuarioService usuarioService;
    
    private ProcessoJudicialManager judicialManager = (ProcessoJudicialManager) Component.getInstance(ProcessoJudicialManager.class);

    private static final String PROCESSO_NAO_ENCONTRADO = "Processo não encontrado.";
    private static final String PROCESSO_NAO_INFORMADO = "Processo não Informado.";
    private static final String NIVEL_SIGILO_NAO_INFORMADO = "O Nivel de sigilo do processo nao foi informado.";
    private static final String NIVEL_SIGILO_INVALIDO = "O Nivel de sigilo do processo nao é valido.";
    private static final String PPROCESSO_POSSUI_SIGILO = "Processo possui sigilo informado.";
   

    public void atualizaSegredoProcesso(String numeroProcesso, String nivelSigilo) throws NegocioException {

        try {

            validaProcessoNivelSigilo(numeroProcesso, nivelSigilo);
            final int nivel = Integer.parseInt(nivelSigilo);

            ProcessoTrf processoTrf = buscarProcesso(numeroProcesso);

            if (processoTrf.getNivelAcesso() == nivel) {
            	throw new NegocioException(PPROCESSO_POSSUI_SIGILO);
            }

            if (processoTrf.getNivelAcesso() >= 1 && nivel >= 1) {
                atualizarNivelProcesso(processoTrf, nivel);
                return;
            }

            if (Integer.parseInt(nivelSigilo) > 0) {
            	adicionaSegredoProcesso(processoTrf, nivel);
            } else {
            	removeSegredoProcesso(processoTrf);
            }

        } catch (NegocioException e) {
            throw e;
        } catch (PJeBusinessException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void atualizarNivelProcesso(ProcessoTrf processoTrf, int nivel) {

        try {
        	
            processoTrf.setNivelAcesso(nivel);
        	processoTrf.setApreciadoSigilo(ProcessoTrfApreciadoEnum.S);
            processoTrf.setApreciadoSegredo(ProcessoTrfApreciadoEnum.S);    
            processoTrf.setObservacaoSegredo("Atualizado sigilo sincronizado pelo Eproc, nivel " + nivel);
            judicialManager.persistAndFlush(processoTrf);
            
        } catch (PJeBusinessException e) {
            throw new RuntimeException(e);
        }
    }

    private void adicionaSegredoProcesso(ProcessoTrf processoTrf, int nivelSigilo) throws PJeBusinessException {
        processoTrf.setNivelAcesso(nivelSigilo);
        processoTrf.setSegredoJustica(true);
        processoTrf.setObservacaoSegredo("Adicionado sigilo sincronizado pelo Eproc, nivel " + nivelSigilo);
        processoTrf.setApreciadoSigilo(ProcessoTrfApreciadoEnum.S);
        processoTrf.setApreciadoSigilo(ProcessoTrfApreciadoEnum.S);

        montarProcessoSegredo(processoTrf, SegredoStatusEnum.C, processoTrf.getObservacaoSegredo());
        processoJudicialService.liberarVisualizacaoTodasPartes(processoTrf);
        getEntityManager().merge(processoTrf);
        EntityUtil.flush();
    }

    private void removeSegredoProcesso(ProcessoTrf processoTrf) throws PJeBusinessException {

    	montarProcessoSegredo(processoTrf, SegredoStatusEnum.R, "Removido sigilo sincronizado pelo Eproc");
        getProcessoSegredoManager().removerProcessoSegredoPendente(processoTrf);
        processoJudicialService.removerTodosVisualizadores(processoTrf);
        processoTrf.setSegredoJustica(false);
        processoTrf.setApreciadoSigilo(ProcessoTrfApreciadoEnum.N);
        processoTrf.setApreciadoSegredo(ProcessoTrfApreciadoEnum.N);
        processoTrf.setObservacaoSegredo(null);
        processoTrf.setNivelAcesso(0);
        ComponentUtil.getComponent(ProcessoJudicialManager.class).persistAndFlush(processoTrf);
    }

    private ProcessoTrf buscarProcesso(String numeroProcesso) {

        try {

            return judicialManager.findByNU(numeroProcesso).get(0);

        } catch (Exception e) {
        	throw new NotFoundException(PROCESSO_NAO_ENCONTRADO);
        }
    }

    private void validaProcessoNivelSigilo(String numeroProcesso, String nivelSigilo) {

        try {

            if(numeroProcesso == null) {
                throw new NegocioException(PROCESSO_NAO_INFORMADO);
            }

            if(nivelSigilo == null) {
                throw new NegocioException(NIVEL_SIGILO_NAO_INFORMADO);
            }

            Integer nivel = Integer.parseInt(nivelSigilo);

            if(nivel < 0 || nivel > 5) {
                throw new NegocioException(NIVEL_SIGILO_INVALIDO);
            }

        } catch (Exception e) {
            throw e;
        }
    }

    private ProcessoSegredo montarProcessoSegredo(ProcessoTrf instance, SegredoStatusEnum segredoStatus, String motivo) throws PJeBusinessException {
    	
        try {
        	
        	ProcessoSegredoManager processoSigiloManager = ComponentUtil.getComponent(ProcessoSegredoManager.class);
            UsuarioLogin usuarioLogin = usuarioService.getUsuarioSistema();

            ProcessoSegredo p = new ProcessoSegredo();
            p.setProcessoTrf(instance);
            p.setMotivo(motivo);
            p.setUsuarioLogin(usuarioLogin);
            p.setApreciado(true);
            p.setDtAlteracao(new Date());

            p.setStatus(segredoStatus);

            processoSigiloManager.persistAndFlush(p);
            return p;
        } catch (PJeBusinessException e) {
            throw new RuntimeException(e);
        }
    }

    private ProcessoSegredoManager getProcessoSegredoManager(){
    	ProcessoSegredoManager processoSigiloManager = ComponentUtil.getComponent(ProcessoSegredoManager.NAME);
        return processoSigiloManager;
    }    
}
