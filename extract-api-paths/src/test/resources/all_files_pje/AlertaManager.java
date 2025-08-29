/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.exception.AlertaInativacaoInvalidaException;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.business.dao.AlertaDAO;
import br.jus.pje.nucleo.entidades.Alerta;
import br.jus.pje.nucleo.entidades.ProcessoAlerta;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.CriticidadeAlertaEnum;

/**
 * Componente de controle negocial da entidade {@link Alerta}.
 * 
 * @author cristof
 *
 */
@Name("alertaManager")
public class AlertaManager extends BaseManager<Alerta> {
	
	private static final long serialVersionUID = 1L;
	@In
	private AlertaDAO alertaDAO;
	@In
	private ProcessoAlertaManager processoAlertaManager;

	@Override
	protected AlertaDAO getDAO() {
		return alertaDAO;
	}
	
	public Alerta create(String texto, CriticidadeAlertaEnum criticidade, ProcessoTrf processo){
		Alerta ret = new Alerta();
		ret.setAlerta(texto);
		ret.setAtivo(true);
		ret.setDataAlerta(new Date());
		ret.setInCriticidade(criticidade);
		ret.setOrgaoJulgador(processo.getOrgaoJulgador());
		ret.setOrgaoJulgadorColegiado(processo.getOrgaoJulgadorColegiado());
		ret.setLocalizacao(processo.getOrgaoJulgador().getLocalizacao());
		return ret;
	}
	
	/**
	 * [PJEII-18551] - Restringe por órgão do julgador do usuário logado a lista de processos que a rotina 
	 * "Processos -> Outras ações -> Incluir alerta" exibe.
	 * 
	 * @param idAlerta - Id do alerta
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-18551
	 * @return lista de processos do órgão julgador do usuário logado cadastrados em determidado alerta
	 */
	public List<ProcessoTrf> obterListaProcessoTrfPorLocalizacao(Integer idAlerta) {
		List<ProcessoTrf> listaDeProcessos = new ArrayList<>(0);
		List<ProcessoAlerta> processoAlertaList = processoAlertaManager.consultaProcessosAlertasAtivos(idAlerta);
		
		for (ProcessoAlerta processoAlerta : processoAlertaList) {
			if (Authenticator.isPapelAdministrador() || isProcessoDaLocalizacaoDoUsuario(processoAlerta)) {
				listaDeProcessos.add(processoAlerta.getProcessoTrf());
			}
		}
		return listaDeProcessos;
	}
	
	/**
	 * Este método retorna o ID do órgão julgador atual.
	 * Caso o usuário não possua um órgão julgador atual, será retornado o valor ZERO.
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-18551
	 * @return idOrgaoJulgadorAtual - Id do órgão julgador atual
	 */
	public Integer getIdOrgaoJulgadorAtual(){
		Integer idOrgaoJulgadorAtual = Authenticator.getIdOrgaoJulgadorAtual();
		return idOrgaoJulgadorAtual == null ? 0 : idOrgaoJulgadorAtual;
	}

	public void configurarStatusDoAlerta(Alerta alerta, boolean status) throws AlertaInativacaoInvalidaException{
		alerta.setAtivo(status);	
	}
	
	
	/**
	 * Método que recupera um alerta pelo seu texto e criticidade.
	 * Existe um indice único no banco por esses campos, este método permite recuperar o alerta por este 
	 * indice para posteriormente cadastrar processos para este alerta 
	 * @param textoAlerta Texto do alerta
	 * @param criticidade Criticidade do alerta
	 * @return Alerta
	 */
	public Alerta findByTextoECriticidade(String textoAlerta, CriticidadeAlertaEnum criticidade) {
		return getDAO().findByTextoECriticidade(textoAlerta, criticidade);
	}


	/**
	 * Este mtodo retorna 'true' se o processo relacionado ao processo alerta pertence  localizao do usurio logado.
	 * 
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-18551
	 * @param processoAlerta - Objeto da classe ProcessoAlerta
	 * @return Retorna 'true' se o processo relacionado ao processo alerta pertence  localizao do usurio logado.
	 */
	public boolean isProcessoDaLocalizacaoDoUsuario(ProcessoAlerta processoAlerta) {
		return processoAlerta != null && processoAlerta.getProcessoTrf().getOrgaoJulgador().getLocalizacao() != null && processoAlerta.getAlerta().getLocalizacao() != null &&
				(getIdLocalizacaoAtual().equals(processoAlerta.getProcessoTrf().getOrgaoJulgador().getLocalizacao().getIdLocalizacao()) ||
				 getIdLocalizacaoAtual().equals(processoAlerta.getAlerta().getLocalizacao().getIdLocalizacao()));
	}
	
	/**
	 * Este mtodo retorna o ID da localizao atual.
	 * Caso o usurio no possua um rgo julgador atual, ser retornado o valor ZERO.
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-18551
	 * @return idOrgaoJulgadorAtual - Id do rgo julgador atual
	 */
	public Integer getIdLocalizacaoAtual(){
		Integer idLocalizacaoAtual = Authenticator.getIdLocalizacaoAtual();
		return idLocalizacaoAtual == null ? 0 : idLocalizacaoAtual;
	}
}