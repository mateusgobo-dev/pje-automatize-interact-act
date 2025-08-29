package br.jus.cnj.pje.view;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.utils.Constantes;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.manager.*;
import br.jus.cnj.pje.nucleo.service.ProcessoService;
import br.jus.pje.je.entidades.Eleicao;
import br.jus.pje.nucleo.entidades.*;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.ArrayList;
import java.util.List;

/**
 * Faz o controle do componente de pesquisa de processos paradigma
 * Esta classe é a controladora do componente: pesquisaProcessoParadigma.xhtml
 */
@Name("pesquisaProcessoParadigmaAction")
@Scope(ScopeType.PAGE)
public class PesquisaProcessoParadigmaAction {
	
	@In
	private ProcessoJudicialManager processoJudicialManager;

	@In
	private JurisdicaoManager jurisdicaoManager;

	@In
	private UsuarioLocalizacaoMagistradoServidorManager usuarioLocalizacaoMagistradoServidorManager;	
	
	@In
	private OrgaoJulgadorCargoManager orgaoJulgadorCargoManager;

	private boolean processoEncontrado;
	private boolean usuarioPodeVisualizarDadosProcesso;
	private boolean processoPesquisado;
	private boolean resultadoPesquisaConfirmado;
	private boolean processoValidado;
	private String processoParadigma;
	private ProcessoTrf processoTrfParadigma;
	private Estado estadoJurisdicaoParadigma;
	private Municipio municipioJurisdicaoParadigma;
	private Integer idAreaDireitoParadigma;
	private Jurisdicao jurisdicaoParadigma;
	private Competencia competenciaParadigma;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiadoParadigma;
	private OrgaoJulgador orgaoJulgadorParadigma;
	private OrgaoJulgadorCargo orgaoJulgadorCargoParadigma;
	private Eleicao eleicaoParadigma;
	private boolean processoEproc;
	private String origemSistema = Constantes.ORIGEM_SISTEMA_PJE;
	

	public void pesquisarProcessoParadigma(boolean permiteProcessoSemVisibilidade) {
		
		if (StringUtils.isNotBlank(this.processoParadigma)) {
			this.origemSistema = Constantes.ORIGEM_SISTEMA_PJE;
			this.processoPesquisado = Boolean.TRUE;
			this.processoTrfParadigma = this.processoJudicialManager.pesquisarProcessoPJE(this.processoParadigma, !permiteProcessoSemVisibilidade, true);
			if(processoTrfParadigma == null) {
				processoTrfParadigma = ProcessoService.instance().buscaDCP(this.processoParadigma);
				this.origemSistema = processoTrfParadigma != null ? Constantes.ORIGEM_SISTEMA_DCP : Constantes.ORIGEM_SISTEMA_PJE;
			}
			this.processoEncontrado = this.processoTrfParadigma != null;
			this.usuarioPodeVisualizarDadosProcesso = Boolean.TRUE;
			this.processoEproc = ProcessoService.instance().getNumProc(this.processoParadigma);
			if(this.processoEncontrado) {
				this.usuarioPodeVisualizarDadosProcesso = this.processoJudicialManager.verificarPermissaoVisibilidade(processoTrfParadigma);
			}
		}
	}
	
	public void confirmarResultadoPesquisaProcessoParadigma() {
		if (this.processoTrfParadigma != null) {
			this.processoEncontrado = Boolean.TRUE;
			if (this.getMostraDadosEleicao() && this.processoTrfParadigma.getComplementoJE() != null) {
				this.municipioJurisdicaoParadigma = this.processoTrfParadigma.getComplementoJE().getMunicipioEleicao();
				if (this.municipioJurisdicaoParadigma == null) {
					this.municipioJurisdicaoParadigma = this.processoTrfParadigma.getJurisdicao().getMunicipioSede();
				}
				if (this.municipioJurisdicaoParadigma != null) {
					this.estadoJurisdicaoParadigma = this.municipioJurisdicaoParadigma.getEstado();
				}
				this.eleicaoParadigma = this.processoTrfParadigma.getComplementoJE().getEleicao();
			}
			this.idAreaDireitoParadigma = this.processoTrfParadigma.getIdAreaDireito();
			this.jurisdicaoParadigma = this.processoTrfParadigma.getJurisdicao();
			this.competenciaParadigma = this.processoTrfParadigma.getCompetencia();
			this.orgaoJulgadorColegiadoParadigma = this.processoTrfParadigma.getOrgaoJulgadorColegiado();
			this.orgaoJulgadorParadigma = this.processoTrfParadigma.getOrgaoJulgador();
			this.orgaoJulgadorCargoParadigma = this.processoTrfParadigma.getOrgaoJulgadorCargo();
		} else {
			this.processoEncontrado = Boolean.FALSE;
			this.usuarioPodeVisualizarDadosProcesso = Boolean.TRUE;
			this.processoTrfParadigma = null;
			this.estadoJurisdicaoParadigma = null;
			this.eleicaoParadigma = null;
			this.inicializaVariaveis(); 
		}
		this.resultadoPesquisaConfirmado = Boolean.TRUE;
	}
	
	public void limparPesquisaProcessoParadigma() {
		this.processoEncontrado = Boolean.FALSE;
		this.usuarioPodeVisualizarDadosProcesso = Boolean.FALSE;
		this.processoPesquisado = Boolean.FALSE;
		this.resultadoPesquisaConfirmado = Boolean.FALSE;
		this.processoValidado = Boolean.FALSE;
		this.processoParadigma = null;
		this.processoTrfParadigma = null;
		this.setJurisdicaoParadigma(null);
	}
	
	public void validarDadosProcesso() {
		this.processoValidado = this.resultadoPesquisaConfirmado && this.processoParadigma != null
				&& this.idAreaDireitoParadigma != null && this.jurisdicaoParadigma != null && this.competenciaParadigma != null
				&& (this.getIsAmbienteColegiado() == false || this.orgaoJulgadorColegiadoParadigma != null)
				&& this.orgaoJulgadorParadigma != null && this.orgaoJulgadorCargoParadigma != null;		
	}

	public List<UsuarioLocalizacaoMagistradoServidor> buscarCargos(){
		List<UsuarioLocalizacaoMagistradoServidor> magistradoServidorList = usuarioLocalizacaoMagistradoServidorManager.obterLocalizacoesMagistrados(orgaoJulgadorParadigma,orgaoJulgadorColegiadoParadigma,true,false, this.getIsAmbienteColegiado() ? true : null);
		return magistradoServidorList;
	}
	
	public List<OrgaoJulgadorCargo> buscarOrgaoJulgadorCargo(){
		return orgaoJulgadorCargoManager.obterCargosJudiciais(orgaoJulgadorParadigma, false, true);
	}
	
	public List<Estado> getEstadosPorJurisdicaoCompetenciaAtiva() {
		List<Estado> result = ComponentUtil.getComponent(EstadoManager.class).recuperarPorJurisdicaoCompetenciaAtiva();
		if (result.size() == 1) {
			this.estadoJurisdicaoParadigma = result.get(0);
		}
		return result;
	}

	public List<Municipio> getMunicipiosPorEstadoComJurisdicaoCompetenciaAtiva() {
		List<Municipio> result = new ArrayList<>();
		if (this.estadoJurisdicaoParadigma != null) {
			result = ComponentUtil.getComponent(MunicipioManager.class)
				.recuperarPorEstadoComJurisdicaoCompetenciaAtiva(this.estadoJurisdicaoParadigma.getIdEstado());
			
			if (result.size() == 1) {
				this.municipioJurisdicaoParadigma = result.get(0);
			}
		}
		return result;
	}

	public List<CompetenciaAreaDireito> getAreasDireito() {
		return ProcessoJudicialManager.instance().recuperarAreasDireito(this.municipioJurisdicaoParadigma);
	}

	public List<Jurisdicao> getJurisdicoesAtivas() {
		List<Jurisdicao> result = new ArrayList<>();

		if (this.idAreaDireitoParadigma != null) {
			if (this.municipioJurisdicaoParadigma != null) {
				result = this.jurisdicaoManager.recuperarJurisdicoes(this.municipioJurisdicaoParadigma);
			} else {
				result = this.jurisdicaoManager.getJurisdicoesAtivas();
			}
		}

		return result;
	}
	
	public void inicializaVariaveis() {
		this.jurisdicaoParadigma = null;
		this.competenciaParadigma = null;
		this.orgaoJulgadorColegiadoParadigma = null;
		this.orgaoJulgadorParadigma = null;
		this.orgaoJulgadorCargoParadigma = null;
	}
	
	public boolean getMostraDadosEleicao() {
		return ComponentUtil.getComponent(ParametroUtil.class).isJusticaEleitoralAndPrimeiroGrau();
	}

	public boolean getIsAmbienteColegiado() {
		return !ComponentUtil.getComponent(ParametroUtil.class).isPrimeiroGrau();
	}
	
	public boolean getProcessoEncontrado() {
		return processoEncontrado;
	}

	public void setProcessoEncontrado(boolean processoEncontrado) {
		this.processoEncontrado = processoEncontrado;
	}

	public boolean getProcessoPesquisado() {
		return processoPesquisado;
	}
	
	public boolean isUsuarioPodeVisualizarDadosProcesso() {
		return usuarioPodeVisualizarDadosProcesso;
	}

	public void setUsuarioPodeVisualizarDadosProcesso(boolean usuarioPodeVisualizarDadosProcesso) {
		this.usuarioPodeVisualizarDadosProcesso = usuarioPodeVisualizarDadosProcesso;
	}

	public void setProcessoPesquisado(boolean processoPesquisado) {
		this.processoPesquisado = processoPesquisado;
	}
	
	public boolean getResultadoPesquisaConfirmado() {
		return resultadoPesquisaConfirmado;
	}

	public void setResultadoPesquisaConfirmado(boolean resultadoPesquisaConfirmado) {
		this.resultadoPesquisaConfirmado = resultadoPesquisaConfirmado;
	}

	public boolean getProcessoValidado() {
		return processoValidado;
	}

	public void setProcessoValidado(boolean processoValidado) {
		this.processoValidado = processoValidado;
	}
	
	public String getProcessoParadigma() {
		return processoParadigma;
	}

	public void setProcessoParadigma(String processoParadigma) {
		this.processoParadigma = processoParadigma;
	}

	public ProcessoTrf getProcessoTrfParadigma() {
		return processoTrfParadigma;
	}

	public void setProcessoTrfParadigma(ProcessoTrf processoTrfParadigma) {
		this.processoTrfParadigma = processoTrfParadigma;
	}

	public Estado getEstadoJurisdicaoParadigma() {
		return estadoJurisdicaoParadigma;
	}

	public void setEstadoJurisdicaoParadigma(Estado estadoJurisdicaoParadigma) {
		this.estadoJurisdicaoParadigma = estadoJurisdicaoParadigma;
	}

	public Municipio getMunicipioJurisdicaoParadigma() {
		return municipioJurisdicaoParadigma;
	}

	public void setMunicipioJurisdicaoParadigma(Municipio municipioJurisdicaoParadigma) {
		this.municipioJurisdicaoParadigma = municipioJurisdicaoParadigma;
	}

	public Integer getIdAreaDireitoParadigma() {
		return idAreaDireitoParadigma;
	}

	public void setIdAreaDireitoParadigma(Integer idAreaDireitoParadigma) {
		this.idAreaDireitoParadigma = idAreaDireitoParadigma;
	}

	public Jurisdicao getJurisdicaoParadigma() {
		return jurisdicaoParadigma;
	}

	public void setJurisdicaoParadigma(Jurisdicao jurisdicaoParadigma) {
		this.jurisdicaoParadigma = jurisdicaoParadigma;
		setCompetenciaParadigma(null);
	}

	public Competencia getCompetenciaParadigma() {
		return competenciaParadigma;
	}

	public void setCompetenciaParadigma(Competencia competenciaParadigma) {
		this.competenciaParadigma = competenciaParadigma;
		setOrgaoJulgadorColegiadoParadigma(null);
		setOrgaoJulgadorParadigma(null);
	}

	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiadoParadigma() {
		return orgaoJulgadorColegiadoParadigma;
	}

	public void setOrgaoJulgadorColegiadoParadigma(OrgaoJulgadorColegiado orgaoJulgadorColegiadoParadigma) {
		this.orgaoJulgadorColegiadoParadigma = orgaoJulgadorColegiadoParadigma;
		setOrgaoJulgadorParadigma(null);
	}

	public OrgaoJulgador getOrgaoJulgadorParadigma() {
		return orgaoJulgadorParadigma;
	}

	public void setOrgaoJulgadorParadigma(OrgaoJulgador orgaoJulgadorParadigma) {
		this.orgaoJulgadorParadigma = orgaoJulgadorParadigma;
	}

	public Eleicao getEleicaoParadigma() {
		return eleicaoParadigma;
	}

	public void setEleicaoParadigma(Eleicao eleicaoParadigma) {
		this.eleicaoParadigma = eleicaoParadigma;
	}


	public OrgaoJulgadorCargo getOrgaoJulgadorCargoParadigma() {
		return orgaoJulgadorCargoParadigma;
	}

	public void setOrgaoJulgadorCargoParadigma(OrgaoJulgadorCargo orgaoJulgadorCargoParadigma) {
		this.orgaoJulgadorCargoParadigma = orgaoJulgadorCargoParadigma;
	}

	public boolean getProcessoEproc() {
		return processoEproc;
	}

	public void setProcessoEproc(boolean processoEproc) {
		this.processoEproc = processoEproc;
	}

	public String getOrigemSistema() {
		return origemSistema;
	}

	public void setOrigemSistema(String origemSistema) {
		this.origemSistema = origemSistema;
	}
}
