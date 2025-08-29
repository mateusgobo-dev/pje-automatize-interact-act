
/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Transactional;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.LogHistoricoMovimentacaoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.LogHistoricoMovimentacao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.MotivoMovimentacaoEnum;

/**
 * PJEII-18813 Registra movimentações das Caixas pelos Procuradores
 * 
 * @author Carlos Lisboa
 * @since 1.7.1
 *
 */
@Name(LogHistoricoMovimentacaoManager.NAME)
public class LogHistoricoMovimentacaoManager extends BaseManager<LogHistoricoMovimentacao> {
	
	@In
	private LogHistoricoMovimentacaoDAO logHistoricoMovimentacaoDAO;
	
	public static final String NAME = "logHistoricoMovimentacaoManager";
	
	@Observer(Eventos.HISTORICO_MOVIMENTACAO_CAIXA_PROCURADORIA)
	@Transactional
	public void registrarHistoricoMovimentacao(List<ProcessoTrf> listProcessoTrf, Calendar dataCorrente, CaixaAdvogadoProcurador caixa, MotivoMovimentacaoEnum motivoMovimentacao, Usuario usuarioMovimentacao) {
		for (ProcessoTrf processoTrf : listProcessoTrf) {
			persistLogHistoricoMovimentacao(
				processoTrf, 
				null, 
				dataCorrente, 
				caixa, 
				motivoMovimentacao, 
				usuarioMovimentacao);
		}
	}

	@Observer(Eventos.HISTORICO_MOVIMENTACAO_CAIXA_PROCURADORIA_EXPEDIENTE)
	@Transactional
	public void registrarHistoricoMovimentacaoExpediente(List<ProcessoParteExpediente> listPpe, Calendar dataCorrente, CaixaAdvogadoProcurador caixa, MotivoMovimentacaoEnum motivoMovimentacao, Usuario usuarioMovimentacao) {
		for (ProcessoParteExpediente ppe : listPpe) {
			persistLogHistoricoMovimentacao(
					ppe.getProcessoJudicial(), 
					ppe, 
					dataCorrente, 
					caixa, 
					motivoMovimentacao, 
					usuarioMovimentacao);
		}
	}
	
	/**
	 * Cria um objeto do tipo LogHistoricoMovimentacao e persite no banco de dados.
	 * 
	 * @param processo ProcessoTrf.
	 * @param ppe ProcessoParteExpediente.
	 * @param dataCorrente Data atual.
	 * @param caixa CaixaAdvogadoProcurador.
	 * @param motivoMovimentacao MotivoMovimentacaoEnum.
	 * @param usuarioMovimentacao UsuarioLogin
	 */
	protected void persistLogHistoricoMovimentacao(ProcessoTrf processo, ProcessoParteExpediente ppe, 
			Calendar dataCorrente, CaixaAdvogadoProcurador caixa, 
			MotivoMovimentacaoEnum motivoMovimentacao, UsuarioLogin usuarioMovimentacao) {
		StringBuilder textoMovimentacao = new StringBuilder();
		textoMovimentacao.append(motivoMovimentacao.getLabel());
		if(usuarioMovimentacao!=null){
			textoMovimentacao.append(" por "+usuarioMovimentacao.getNome());
		}
		SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		textoMovimentacao.append(" em "+formato.format(dataCorrente.getTime()));
		if (caixa != null) {
			if(motivoMovimentacao.equals(motivoMovimentacao.E) || motivoMovimentacao.equals(motivoMovimentacao.A)){
				textoMovimentacao.append(" da caixa: "+caixa.getNomeCaixaAdvogadoProcurador());
			} else {
				textoMovimentacao.append(" para caixa: "+caixa.getNomeCaixaAdvogadoProcurador());
			}
		} else {
			logger.warn("O objeto do tipo CaixaAdvogadoProcurador não está nulo na execução do método "
					+ "LogHistoricoMovimentacaoManager.persistLogHistoricoMovimentacao.");
		}
		
		LogHistoricoMovimentacao log = new LogHistoricoMovimentacao();
		log.setProcessoTrf(processo);
		log.setProcessoParteExpediente(ppe);
		log.setDataLog(dataCorrente.getTime());
		if (caixa != null) {
			log.setCaixa(caixa);
			log.setNomeCaixa(caixa.getNomeCaixaAdvogadoProcurador());
		}
		log.setMotivoMovimentacao(motivoMovimentacao);
		log.setUsuario(usuarioMovimentacao);
		log.setTextoMovimentacao(textoMovimentacao.toString());
		
		try {
			persistAndFlush(log);
		} catch (PJeBusinessException e) {
			logger.error(e);
		}
	}
	
	@Override
	protected BaseDAO<LogHistoricoMovimentacao> getDAO() {
		return logHistoricoMovimentacaoDAO;
	}

	/**
	 * metodo responsavel por recuperar todos os logs de movimentacao da pessoa passada em parametro.
	 * @param pessoa
	 * @return
	 * @throws Exception 
	 */
	public List<LogHistoricoMovimentacao> recuperarLogs(Pessoa pessoa) throws Exception {
		return logHistoricoMovimentacaoDAO.recuperarLogs(pessoa);
	}

	public LogHistoricoMovimentacao recuperarPorId(Integer id) {
		return logHistoricoMovimentacaoDAO.find(id);
	}
}