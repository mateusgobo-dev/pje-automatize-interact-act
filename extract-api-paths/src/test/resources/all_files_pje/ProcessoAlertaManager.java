/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ProcessoAlertaDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.Alerta;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoAlerta;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.CriticidadeAlertaEnum;

/**
 * Componente de tratamento negocial de alertas processuais.
 * 
 * @author flavioreis
 *
 */
@Name("processoAlertaManager")
public class ProcessoAlertaManager extends BaseManager<ProcessoAlerta> {
	
	private static final long serialVersionUID = 1L;
	@In
	private ProcessoAlertaDAO processoAlertaDAO;
	
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.BaseManager#getDAO()
	 */
	@Override
	protected ProcessoAlertaDAO getDAO() {
		return processoAlertaDAO;
	}
	
	/**
	 * Indica se um determinado processo judicial tem pelo menos um
	 * alerta, de qualquer criticidade, ativo.
	 * 
	 * @param processoJudicial o processo a respeito do qual se pretende
	 * recuperar a informação
	 * @return true, se houver pelo menos um alerta ativo no processo
	 */
	public boolean possuiAlertasAtivos(ProcessoTrf processoJudicial) throws PJeBusinessException {
		return processoAlertaDAO.possuiAlertasAtivos(processoJudicial);
	}
	
	/**
	 * Retorna os ProcessoAlerta's ativos vinculados a um alerta.
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-18551
	 * @param idAlerta - id do alerta
	 * @return Lista de ProcessoAlerta de um alerta
	 */
	public List<ProcessoAlerta> consultaProcessosAlertasAtivos(Integer idAlerta){
		return processoAlertaDAO.consultaProcessosAlertasAtivos(idAlerta);
	}
	
	public boolean existemProcessosAlertasAtivos(Integer idAlerta) {
		return processoAlertaDAO.existemProcessosAlertasAtivos(idAlerta);
	}
	
	public List<ProcessoAlerta> getAlertasProcesso(ProcessoTrf processoJudicial) {
		List<ProcessoAlerta> list = new ArrayList<ProcessoAlerta>(0);
		list = getDAO().getAlertasProcesso(processoJudicial.getIdProcessoTrf());
		return list;
	}
	
	/**
	 * Verifica se o OJ do usuario é o mesmo OJ do processo ou se o usuario nao possuir OJ configurado
	 * verifica se o OJC do usuário é o mesmo OJC do processo.  
	 * <br /><br />
	 * Legendas: <br />
	 * <b>OJ</b> - Órgão Julgador <br />
	 * <b>OJC</b> - Órgão Julgador Colegiado <br />
	 * 
	 * @param processoTrf
	 * @return true se exibi os alertas do processo
	 */
	public boolean exibirAlertasProcesso(ProcessoTrf processoTrf){
		boolean retorno = false;
		
		if(!Authenticator.isUsuarioInterno()){
			return retorno;
		}

		OrgaoJulgador orgaoJulgadorUsuarioLogado = Authenticator.getOrgaoJulgadorAtual();
		OrgaoJulgadorColegiado orgaoJulgadorColegiadoUsuarioLogado = Authenticator.getOrgaoJulgadorColegiadoAtual();
		OrgaoJulgador orgaoJulgadorProcesso = processoTrf.getOrgaoJulgador();
		OrgaoJulgadorColegiado orgaoJulgadorColegiadoProcesso = processoTrf.getOrgaoJulgadorColegiado();
		
		if((orgaoJulgadorUsuarioLogado != null && orgaoJulgadorColegiadoUsuarioLogado == null) && orgaoJulgadorUsuarioLogado == orgaoJulgadorProcesso){
				retorno = true;
			
		}else if((orgaoJulgadorUsuarioLogado != null && orgaoJulgadorColegiadoUsuarioLogado != null) && orgaoJulgadorUsuarioLogado == orgaoJulgadorProcesso){
			retorno = true;
			
		}else if((orgaoJulgadorUsuarioLogado == null && orgaoJulgadorColegiadoUsuarioLogado != null) && orgaoJulgadorColegiadoProcesso == orgaoJulgadorColegiadoUsuarioLogado){
				retorno = true;
		}else if(orgaoJulgadorUsuarioLogado == null && orgaoJulgadorColegiadoUsuarioLogado == null){
			retorno = getAlertasProcesso(processoTrf).stream().anyMatch(alerta -> ComponentUtil.getComponent(AlertaManager.class).isProcessoDaLocalizacaoDoUsuario(alerta));
		}
		return retorno;
	}
	
	public void inativarAlertaProcesso(ProcessoAlerta processoAlerta) {
		getDAO().inativarAlertaProcesso(processoAlerta.getIdProcessoAlerta());
	}
	
	public void inativarTodosAlertasProcesso(ProcessoTrf processoJudicial) {
		getDAO().inativarTodosAlertasProcesso(processoJudicial.getIdProcessoTrf());
	}
	
	public void incluirAlertaAtivo(ProcessoTrf processo, String textoAlerta, CriticidadeAlertaEnum criticidade){
		try{
			
			AlertaManager alertaManager = ComponentUtil.getComponent(AlertaManager.class); 
			Alerta alerta = alertaManager.create(textoAlerta, criticidade, processo);
			
			alerta = alertaManager.persist(alerta);
			
			ProcessoAlerta alertaProcessual;
			alertaProcessual = new ProcessoAlerta();
			alertaProcessual.setAlerta(alerta);
			alertaProcessual.setAtivo(true);
			alertaProcessual.setProcessoTrf(processo);
			this.persist(alertaProcessual);
		}catch (Exception e){
			String msg = String.format("Erro ao registrar alerta.\n"
					+ "Mensagem: [%s]\n"
					+ "Erro: [%s].", textoAlerta, e.getLocalizedMessage());
			logger.error(msg);
		}
	}
}	