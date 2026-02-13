package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.cliente.bean.PreCadastroPessoaBean;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteRepresentanteManager;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

@Name("processoParteRepresentanteHome")
public class ProcessoParteRepresentanteHome extends AbstractProcessoParteRepresentanteHome<ProcessoParteRepresentante> {

	private static final long serialVersionUID = 1L;
	private String codInParticipacao;
	private List<ProcessoParte> partes = new ArrayList<ProcessoParte>(0);
	private boolean flgInclusao = true;
	private boolean flgVinculandoRepresentante = false;
	private boolean flgMostrarDadosAdvogado = true;
	
	@In
	private ProcessoParteRepresentanteManager processoParteRepresentanteManager;
	
	@In
	private ProcessoParteManager processoParteManager;

	public static ProcessoParteRepresentanteHome instance() {
		return ComponentUtil.getComponent("processoParteRepresentanteHome");
	}

	/**
	 * [PJEII-3958] - Bernardo Gouvea 
	 * somentePartes - se igual a 'true' Chama o método devolvendo somente as partes se este atributo estiver marcado como verdadeiro.
	 * Exclui os advogados (7), procuradores (9), curadores (3) e terceiros interessados (10).
	 * @return
	 */
    public List<ProcessoParte> getPartesParaRepresentacao(boolean somentePartes) {
    	PessoaHome pessoaHome = (PessoaHome) Component.getInstance("pessoaHome");

    	ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
        TipoParte tipoParte = ProcessoParteHome.instance().getInstance().getTipoParte();
        ProcessoParteParticipacaoEnum tipoParticipacao = obterTipoParticipacao();
		Pessoa pessoa = pessoaHome.getInstance();

        List<ProcessoParte> partes = obterProcessosPartes(somentePartes,
				processoTrf, tipoParte, tipoParticipacao, pessoa);
        
        if(this.getPartes().isEmpty() && !partes.isEmpty() && partes.size() == 1){
            this.getPartes().add(partes.get(0));
        }
        return partes;
    }

    /**
     * Metodo que obtem uma lista de PartesDoProcesso baseado nos parametros podendo restringir por algumas partes especificas.
     * 
     * @param somentePartes True vai restringir os (Advogados, Curadores, Procuradores)
     * @param processoTrf Processo
     * @param tipoParte Tipo da Parte
     * @param tipoParticipacao Tipo de Participacao
     * @param pessoa Pessoa podendo ser null
     * @return Lista de ProcessoParte
     */
	private List<ProcessoParte> obterProcessosPartes(boolean somentePartes, ProcessoTrf processoTrf, 
			TipoParte tipoParte, ProcessoParteParticipacaoEnum tipoParticipacao, Pessoa pessoa) {
		List<ProcessoParte> partes = new ArrayList<ProcessoParte>();

        if (isFlgInclusao()) {
        	partes = processoParteManager.recuperar(somentePartes, processoTrf, tipoParte, tipoParticipacao);
        } else {
        	partes = processoParteManager.recuperar(somentePartes, processoTrf, pessoa, tipoParte, tipoParticipacao);
        }
		return partes;
	}
	
	/**
	 * Recupera as partes vinculadas ao representante.
	 * 
	 * @return Lista de partes representadas
	 */
	private List<ProcessoParte> obterPartesRepresentadas() {
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
		PessoaHome pessoaHome = (PessoaHome) Component.getInstance("pessoaHome");
		Pessoa pessoa = pessoaHome.getInstance();
		
        List<ProcessoParte> representados = new ArrayList<ProcessoParte>();
        
        if(pessoa.getIdPessoa() != null && processoTrf != null){
        	representados = processoParteManager.recuperarRepresentados(pessoa, processoTrf);
        }
		return representados;
	}

	/**
	 * Retorna o tipo de participacao da parte.
	 * 
	 * @return (Ativo ou Passivo ou Todos)
	 */
	private ProcessoParteParticipacaoEnum obterTipoParticipacao() {
		ProcessoParteParticipacaoEnum tipoParticipacao;
		if(codInParticipacao.equals("A")){
        	tipoParticipacao = ProcessoParteParticipacaoEnum.A;
        }else if(codInParticipacao.equals("P")){
        	tipoParticipacao = ProcessoParteParticipacaoEnum.P;
        }else{
        	tipoParticipacao = ProcessoParteParticipacaoEnum.T;
        }
		return tipoParticipacao;
	}


	/**
	 * [PJEII-3958] - Bernardo Gouvea 
	 * Chamada antiga do método para não prejudicar locais em que ja era chamado.
	 * @return
	 */
    public List<ProcessoParte> getPartesParaRepresentacao() {
		return this.getPartesParaRepresentacao(false);
	}

	public ProcessoParteRepresentante criarRepresentacao(ProcessoParte representante, ProcessoParte representado,
			TipoParte tipoRepresentacao) {

		ProcessoParteRepresentante parteRep = new ProcessoParteRepresentante();
		parteRep.setParteRepresentante(representante);
		parteRep.setRepresentante(representante.getPessoa());
		parteRep.setTipoRepresentante(tipoRepresentacao);
		parteRep.setProcessoParte(representado);

		return parteRep;

	}


	public List<ProcessoParteRepresentante> getRepresentacoesExcluidas(List<ProcessoParteRepresentante> participacoesAntes, 
			List<ProcessoParte> partesSelecionadas,	TipoParte tipoParte) {

		List<ProcessoParteRepresentante> excluidas = new ArrayList<ProcessoParteRepresentante>(0);
		boolean podeExcluirRepresentacao;
		for (ProcessoParteRepresentante participacao : participacoesAntes) {
			podeExcluirRepresentacao = true;
			for (ProcessoParte parteSelecionada : partesSelecionadas) {
				if (participacao.getTipoRepresentante().equals(tipoParte)
						&& participacao.getProcessoParte().equals(parteSelecionada)) {
					podeExcluirRepresentacao = false;
					break;
				}
			}
			
			if(podeExcluirRepresentacao){
				excluidas.add(participacao);
			}
		}

		return excluidas;
	}

	public List<TipoParte> getTipoPartes() {
		ProcessoParteParticipacaoEnum p = null;
		if (this.codInParticipacao.equals("A")) {
			p = ProcessoParteParticipacaoEnum.A;
		} else if (this.codInParticipacao.equals("P")) {
			p = ProcessoParteParticipacaoEnum.P;
		} else if (this.codInParticipacao.equals("T")) {
			p = ProcessoParteParticipacaoEnum.T;
		}
		if ((Authenticator.isAdvogado() || Authenticator.isAssistenteAdvogado()) && this.codInParticipacao.equals("P")) {
			return TipoParteClasseJudicialHome.instance().getTipoPartesRepresentantesSemAdvogado(
					p, ProcessoTrfHome.instance().getInstance().getClasseJudicial());
		} else {			
			return TipoParteClasseJudicialHome.instance().getTipoPartesRepresentantes(
					p, ProcessoTrfHome.instance().getInstance().getClasseJudicial());
		}
	}

	public void resetarVinculacaoRepresentante(Boolean flgVinculandoRepresentante, String polo, Boolean flgMostrarDadosAdvogado ) {

		ProcessoParteHome.instance().newInstance();
		ProcessoParteHome.instance().setFlgVinculandoParte(false);
		ProcessoParteHome.instance().getInstance().setTipoParte(null);

		ProcessoTrfHome.instance().getInstance();
		Contexts.removeFromAllContexts("preCadastroPessoaBean");
		newInstance();

		PreCadastroPessoaBean preBean = (PreCadastroPessoaBean) Component.getInstance("preCadastroPessoaBean");
		preBean.resetarBean();
		preBean.setIsPartes(true);

		setCodInParticipacao(polo);
		ProcessoParteHome.instance().getInstance().setInParticipacao(ProcessoParteParticipacaoEnum.valueOf(polo));
		
		setPartes(new ArrayList<ProcessoParte>(0));
		setFlgInclusao(true);
		setFlgVinculandoRepresentante(flgVinculandoRepresentante);
		setFlgMostrarDadosAdvogado(flgMostrarDadosAdvogado);
	}

	public void setPartes(List<ProcessoParte> partes) {
		this.partes = partes;
	}

	public List<ProcessoParte> getPartes() {
		if(CollectionUtilsPje.isEmpty(this.partes)){
			this.partes = obterPartesRepresentadas();
		}
		return this.partes;
	}

	public void setCodInParticipacao(String codInParticipacao) {
		this.codInParticipacao = codInParticipacao;
	}

	public String getCodInParticipacao() {
		return codInParticipacao;
	}

	public void setFlgInclusao(boolean flgInclusao) {
		this.flgInclusao = flgInclusao;
	}

	public boolean isFlgInclusao() {
		return flgInclusao;
	}

	public void setFlgVinculandoRepresentante(boolean flgVinculandoRepresentante) {
		this.flgVinculandoRepresentante = flgVinculandoRepresentante;
	}

	public boolean getFlgVinculandoRepresentante() {
		return flgVinculandoRepresentante;
	}

	public boolean isFlgMostrarDadosAdvogado() {
		return flgMostrarDadosAdvogado;
	}

	public void setFlgMostrarDadosAdvogado(boolean flgMostrarDadosAdvogado) {
		this.flgMostrarDadosAdvogado = flgMostrarDadosAdvogado;
	}
	
	public boolean renderizaTabs(){
//		ProcessoParteHome.instance().getInstance().getTipoParte() 7 é advogado
		if(!isFlgMostrarDadosAdvogado()  && ProcessoParteHome.instance() != null && ProcessoParteHome.instance().getInstance() != null && ProcessoParteHome.instance().getInstance().getTipoParte() != null && ProcessoParteHome.instance().getInstance().getTipoParte().getIdTipoParte() == 7 ){
			return false;
		}
		return true;
	}

	public boolean renderizaTabEndereco(){
		boolean retorno = true;
		GridQuery grid = getComponent("processoParteVinculoPessoaEnderecoGrid");
		
		@SuppressWarnings("unchecked")
		List<Endereco> lista = (List<Endereco>)grid.getResultList();
		
		if(lista!= null && !lista.isEmpty() && lista.size() == 1 ){
			if (grid.getSelectedRow() == null || !lista.contains(grid.getSelectedRow())) {
				grid.setSelectedRow(lista.get(0));
				grid.refresh();
			}
		}
		return retorno;
	}
	
	@SuppressWarnings("unchecked")
	@Observer("setSelectedRows")
	public void selecionaEndereco() {
		GridQuery grid = getComponent("processoParteVinculoPessoaEnderecoGrid");
		if(grid != null && grid.getSelectedRow() == null) {
			// desabilito o evento temporariamente para não haver stackoverflow
			String evento = grid.getBeforeResultEvent();
			grid.setBeforeResultEvent(null);
			List<Endereco> lista = grid.getFullList();
			if(lista!= null && !lista.isEmpty() && lista.size() > 1){
				grid.setSelectedRow(lista.get(0));
				grid.refresh();
			}
			grid.setBeforeResultEvent(evento);
		}
	}

	public ProcessoParteManager getProcessoParteManager() {
		return processoParteManager;
	}
}
