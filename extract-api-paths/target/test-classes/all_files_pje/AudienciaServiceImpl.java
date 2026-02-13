package br.jus.cnj.pje.webservice;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.bpm.BusinessProcess;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.pje.manager.ProcessoAudienciaManager;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.jt.pje.manager.SalaManager;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.TempoAudienciaOrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.TipoAudienciaManager;
import br.jus.cnj.pje.nucleo.service.FluxoService;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.webservice.api.IAudienciaService;
import br.jus.cnj.pje.webservice.controller.modalAudienciaLote.dto.AudienciaDTO;
import br.jus.cnj.pje.webservice.controller.modalAudienciaLote.dto.SalaDTO;
import br.jus.cnj.pje.webservice.controller.modalAudienciaLote.dto.TipoAudienciaDTO;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.ProcessoAudiencia;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sala;
import br.jus.pje.nucleo.entidades.TipoAudiencia;
import br.jus.pje.nucleo.enums.EtapaAudienciaEnum;
import br.jus.pje.nucleo.enums.StatusAudienciaEnum;

@Name("audienciaService")
public class AudienciaServiceImpl implements IAudienciaService {
	
	@In
	private TipoAudienciaManager tipoAudienciaManager;
	
	@In
	private SalaManager salaManager;
	
	@In
	private TempoAudienciaOrgaoJulgadorManager tempoAudienciaOrgaoJulgadorManager;
	
	@In
	private ProcessoAudienciaManager processoAudienciaManager;
	
	@In
	private FluxoService fluxoService;
	
	@Override
	public List<TipoAudienciaDTO> obterTiposAudiencia() {
		List<TipoAudienciaDTO> result = new ArrayList<TipoAudienciaDTO>(0);
		
		for (TipoAudiencia tipoAudiencia : tipoAudienciaManager.getTipoAudienciaList()) {
			result.add(new TipoAudienciaDTO(tipoAudiencia.getIdTipoAudiencia(), tipoAudiencia.getTipoAudiencia()));
		}
		return result;
	}

	@Override
	public List<SalaDTO> obterSalasAudiencias(Integer idTipoAudiencia, List<Integer> idsOrgaoJulgador) {
		List<SalaDTO> result = new ArrayList<SalaDTO>(0);
		
		for (Sala sala : salaManager.recuperarSalasAudienciaAtivas(idTipoAudiencia, idsOrgaoJulgador)) {
			result.add(new SalaDTO(sala.getIdSala(), sala.getSala()));
		}
		return result;
	}

	@Override
	public Integer obterTempoAudiencia(Integer idTipoAudiencia, List<Integer> idsOrgaoJulgador) {
		return tempoAudienciaOrgaoJulgadorManager.recuperarAtivo(idTipoAudiencia, idsOrgaoJulgador);
	}
	
    @Override
    @Transactional
	public AudienciaDTO designarAudiencia(Long idTarefa, Long idProcesso, Integer idTipoAudiencia, 
			Integer idSalaAudiencia, Integer duracao, Long dataInicio) throws PJeBusinessException {
    	
    	ProcessoTrf processoTrf = EntityUtil.find(ProcessoTrf.class, Long.valueOf(idProcesso).intValue());
    	TipoAudiencia tipoAudiencia = EntityUtil.find(TipoAudiencia.class, idTipoAudiencia);
    	Sala sala = idSalaAudiencia != null ? EntityUtil.find(Sala.class, idSalaAudiencia) : null;
    	ProcessoAudiencia ultimaAudiencia = obterUltimaAudienciaMarcada(processoTrf, tipoAudiencia);
    	
    	ProcessoAudiencia processoAudiencia = this.processoAudienciaManager.designarAudiencia(processoTrf, 
			ultimaAudiencia, tipoAudiencia, sala, dataInicio != null ? new Date(dataInicio) : null, duracao,EtapaAudienciaEnum.M);
    	
        if (idTarefa != null) {
			if (ParametroUtil.instance().isDesativaSinalizacaoMovimentacaoAudiencia()) {
				this.fluxoService.finalizarTarefa(idTarefa, Boolean.FALSE, null);
			} else {
				String variavel;
				if (ultimaAudiencia != null) {
					variavel = Variaveis.PJE_FLUXO_AUDIENCIA_AGUARDA_REDESIGNACAO;
				} else {
					variavel = Variaveis.PJE_FLUXO_AUDIENCIA_AGUARDA_DESIGNACAO;
				}

				ProcessInstance pi = null;
				BusinessProcess.instance().resumeTask(idTarefa);
				TaskInstance ti = org.jboss.seam.bpm.TaskInstance.instance();
				pi = ti.getProcessInstance();
				pi.getContextInstance().setVariable(Eventos.EVENTO_SINALIZACAO, variavel);
				pi.getContextInstance().setVariable(Variaveis.PJE_FLUXO_AUDIENCIA, processoAudiencia);

				this.fluxoService.finalizarTarefa(idTarefa, Boolean.FALSE, null);

				ProcessoJudicialService pjs = ComponentUtil.getComponent(ProcessoJudicialService.class);
				Map<String, Object> novasVariaveis = new HashMap<String, Object>();
				novasVariaveis.put(Variaveis.PJE_FLUXO_AUDIENCIA, processoAudiencia);
				pjs.sinalizarFluxo(processoTrf, variavel, true, true, true, novasVariaveis);
			}
        }
            
    	return new AudienciaDTO(processoAudiencia.getSalaAudiencia().toString(), processoAudiencia.getDtInicioFormatada());
	}
    
    private ProcessoAudiencia obterUltimaAudienciaMarcada(ProcessoTrf processoTrf, TipoAudiencia tipoAudiencia) {
    	ProcessoAudiencia ultimaAudienciaMarcada = null;
    	for (ProcessoAudiencia audiencia : processoTrf.getProcessoAudienciaList()) {
    		if (StatusAudienciaEnum.M.equals(audiencia.getStatusAudiencia()) 
    				&& tipoAudiencia != null && audiencia.getTipoAudiencia().equals(tipoAudiencia)) {
    			
    			ultimaAudienciaMarcada = audiencia;
    			break;
    		}
    	}
    	return ultimaAudienciaMarcada;
    }

}
