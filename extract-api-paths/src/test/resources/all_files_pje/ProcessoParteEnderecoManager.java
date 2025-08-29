package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import br.com.infox.cliente.util.ProjetoUtil;
import br.jus.cnj.pje.business.dao.ProcessoParteEnderecoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteEndereco;

@Name(ProcessoParteEnderecoManager.NAME)
public class ProcessoParteEnderecoManager extends BaseManager<ProcessoParteEndereco>{
	
	public static final String NAME = "processoParteEnderecoManager";
	
	@In
	private ProcessoParteEnderecoDAO processoParteEnderecoDAO;

	@In
	private EnderecoManager enderecoManager;
	
	@Override
	protected ProcessoParteEnderecoDAO getDAO() {
		return processoParteEnderecoDAO;
	}

	/**
	 * Cadastra os endereços de processo parte passados por parâmetro, se a coleção de endereços 
	 * estiver nula serão cadastrados todos os endereços da pessoa.
	 * 
	 * @param processoParte Parte.
	 * @param enderecos Endereços que serão cadastrados para a parte.
	 * @return lista de ProcessoParteEndereco.
	 * @throws PJeBusinessException 
	 */
	public List<ProcessoParteEndereco> salvar(ProcessoParte processoParte, List<Endereco> enderecos) throws PJeBusinessException {
		List<ProcessoParteEndereco> resultado = new ArrayList<ProcessoParteEndereco>();
		
		if (processoParte != null && processoParte.getPessoa() != null) {
			Pessoa pessoa = processoParte.getPessoa();
			
			if (ProjetoUtil.isVazio(enderecos)) {
				enderecos = pessoa.getEnderecoList();
			}
			
			for (Endereco endereco : enderecos) {
				endereco = enderecoManager.salvarSeNaoExistir(pessoa, endereco);
				
				ProcessoParteEndereco ppe = salvarSeNaoExistir(processoParte, endereco);
				
				resultado.add(ppe);
			}
		}
		return resultado;
	}

	private ProcessoParteEndereco salvarSeNaoExistir(ProcessoParte processoParte, Endereco endereco) throws PJeBusinessException {
		if (processoParte == null) {
			throw new PJeBusinessException("Tentou salvar relacionamento entre parte e endereco (ProcessoParteEndereco) com parte nula.");
		}
		if (endereco == null) {
			throw new PJeBusinessException("Tentou salvar relacionamento entre parte e endereco (ProcessoParteEndereco) com endereço nulo.");
		}
		ProcessoParteEndereco ppe = getDAO().existeProcessoParteEndereco(processoParte);
		if (ppe == null) {
			ppe = new ProcessoParteEndereco();
			ppe.setProcessoParte(processoParte);
			ppe.setEndereco(endereco);
			ppe = persist(ppe);
		}
		return ppe;
	}

	public void associarEnderecoParteProcesso(ProcessoParte parte, boolean autoridade, Endereco endereco) throws Exception {
 		parte.getProcessoParteEnderecoList().clear();
 		if(parte.getIdProcessoParte() > 0) {
 			processoParteEnderecoDAO.apagarEnderecos(parte);
 		}
		ProcessoParteEndereco aux = new ProcessoParteEndereco();
		aux.setEndereco(endereco);
		aux.setProcessoParte(parte);
		parte.getProcessoParteEnderecoList().add(aux);
	}
}
