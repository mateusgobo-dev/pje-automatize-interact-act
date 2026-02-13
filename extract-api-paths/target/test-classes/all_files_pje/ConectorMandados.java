package br.jus.cnj.pje.extensao;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.home.ProcessoExpedienteCentralMandadoHome;
import br.com.infox.cliente.home.ProcessoExpedienteHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.manager.GenericManager;
import br.com.itx.component.Util;
import br.jus.cnj.pje.extensao.PontoExtensaoException;
import br.jus.cnj.pje.view.fluxo.ComunicacaoProcessualAction;
import br.jus.pje.nucleo.entidades.CentralMandado;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoExpedienteCentralMandado;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.enums.ProcessoExpedienteCentralMandadoStatusEnum;

@Name(ConectorMandados.NAME)
@Scope(ScopeType.CONVERSATION)
public class ConectorMandados implements Serializable{

	private static final long serialVersionUID = 7779834046421013677L;

	public static final String NAME = "conectorMandados";

	private CentralMandado centralMandado;

	@In(create = true, required = false)
	private ComunicacaoProcessualAction comunicacaoProcessualAction;

	@In(create = true, required = false)
	private transient ProcessoExpedienteHome processoExpedienteHome;

	@In(create = true, required = false)
	private transient ProcessoExpedienteCentralMandadoHome processoExpedienteCentralMandadoHome;

	@In(create = true, required = false)
	private transient GenericManager genericManager;

	@In(create = true, required = false)
	private transient Util util;

	/**
	 * Método responsável por encaminhar expedientes para a central de mandados com lançamento de movimentos.
	 * @author Ronny Paterson (ronny.silva@trt8.jus.br) / David Vieira (davidv@trt7.jus.br)
	 * @since 1.4.2
	 * @see 
	 * @category PJE-JT	 
	 */
	public void encaminharExpedientesMovimentacaoMandados(ProcessoExpediente processoExpediente) throws PontoExtensaoException{

		if (haProcessoExpedienteCentralMandado(processoExpediente)){
			return;
		}
		if (ParametroUtil.instance().isGerarUmMandadoPorEndereco()) {
			List<ProcessoParteExpediente> partesExpediente = processoExpediente.getProcessoParteExpedienteList();
			
			for (ProcessoParteExpediente parte : partesExpediente) {
			
				List<Endereco> enderecosExpediente = parte.getEnderecos();
			
				for (Endereco endereco : enderecosExpediente) {
					ProcessoExpedienteCentralMandado processoExpedienteCentralMandado = new ProcessoExpedienteCentralMandado();
					processoExpedienteCentralMandado.setProcessoExpediente(processoExpediente);
					List<CentralMandado> centraisMandado = getCentraisMandado();
					if (centraisMandado.size() == 1){
						centralMandado = centraisMandado.get(0);
					}
					processoExpedienteCentralMandado.setCentralMandado(getCentralMandado());
					processoExpedienteCentralMandado.setUrgencia(processoExpediente.getUrgencia());
					processoExpedienteCentralMandado.setStatusExpedienteCentral(ProcessoExpedienteCentralMandadoStatusEnum.A);
					processoExpedienteCentralMandado.setEnderecoParteExpedienteUnico(endereco);
					processoExpedienteCentralMandado.setParteExpedienteUnica(parte);
					genericManager.persist(processoExpedienteCentralMandado);
				}
			}
		} else {
			ProcessoExpedienteCentralMandado processoExpedienteCentralMandado = new ProcessoExpedienteCentralMandado();
			processoExpedienteCentralMandado.setProcessoExpediente(processoExpediente);
			List<CentralMandado> centraisMandado = getCentraisMandado();
			if (centraisMandado.size() == 1){
				centralMandado = centraisMandado.get(0);
			}
			processoExpedienteCentralMandado.setCentralMandado(getCentralMandado());
			processoExpedienteCentralMandado.setUrgencia(processoExpediente.getUrgencia());
			processoExpedienteCentralMandado.setStatusExpedienteCentral(ProcessoExpedienteCentralMandadoStatusEnum.A);
			genericManager.persist(processoExpedienteCentralMandado);			
		}

	}

	public boolean haProcessoExpedienteCentralMandado(){
		List<ProcessoExpediente> expedientesMandados = getExpedientesMandados();
		if (expedientesMandados != null && !expedientesMandados.isEmpty() && processoExpedienteCentralMandadoHome.haProcessoExpedienteCentralMandado(expedientesMandados.get(0))){
			return true;
		}
		return false;
	}

	public boolean haProcessoExpedienteCentralMandado(ProcessoExpediente processoExpediente){
		return processoExpedienteCentralMandadoHome.haProcessoExpedienteCentralMandado(processoExpediente);
	}

	public List<ProcessoExpediente> getExpedientesMandados(){
		return comunicacaoProcessualAction.getExpedientesMandados();
	}

	public List<CentralMandado> getCentraisMandado(){
		List<CentralMandado> buscaCentralMandado = processoExpedienteHome.buscaCentralMandado();
		if (buscaCentralMandado.size() <= 0){
			return processoExpedienteHome.buscaTodasCentraisMandados();
		}
		return buscaCentralMandado;
	}

	public boolean haVariasCentraisMandado(){
		return getCentraisMandado().size() != 1;
	}

	public CentralMandado getCentralMandado(){
		return centralMandado;
	}

	public void setCentralMandado(CentralMandado centralMandado){
		this.centralMandado = centralMandado;
	}

}
