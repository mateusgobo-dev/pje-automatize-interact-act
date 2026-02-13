package br.jus.cnj.pje.nucleo.service;

import org.jboss.seam.annotations.Name;
import br.com.infox.pje.manager.PessoaProcuradoriaEntidadeManager;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteEndereco;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

@Name(ProcessoParteService.NAME)
public class ProcessoParteService extends BaseService {
	
	public static final String NAME = "processoParteService";
	
	public void incluirPessoaComoParte(ProcessoTrf processoTrf, Pessoa pessoa, ProcessoParteParticipacaoEnum polo, Boolean partePrincipal, TipoParte tipoParte, Boolean visualizaSigilo) {
		boolean pessoaVinculado = false;
		
		//Verifica se a pessoa já não está vinculada ao processo no mesmo polo e com mesmo tipo de participação
		for (ProcessoParte processoParte : processoTrf.getListaPartePoloObj(true, false, polo)) {
			if(processoParte.getTipoParte().equals(tipoParte) 
					&& processoParte.getPessoa().equals(pessoa)){
				pessoaVinculado = true;
				break;
			}
		}
		
		if (!pessoaVinculado) {
			ProcessoParte processoParte = new ProcessoParte();
			
			processoParte.setProcessoTrf(processoTrf);
			processoParte.setInParticipacao(polo);
			processoParte.setPessoa(pessoa);
			processoParte.setTipoParte(tipoParte);
			processoParte.setPartePrincipal(partePrincipal);
			
			PessoaProcuradoriaEntidadeManager pessoaProcuradoriaEntidadeManager = ComponentUtil.getComponent(PessoaProcuradoriaEntidadeManager.class);
			processoParte.setProcuradoria(pessoaProcuradoriaEntidadeManager.getProcuradoriaPadraoPessoa(pessoa));
			
			if (pessoa.getEnderecoList() != null && !pessoa.getEnderecoList().isEmpty()) {
				processoParte.setIsEnderecoDesconhecido(false);
				ProcessoParteEndereco ppe = new ProcessoParteEndereco();
				ppe.setEndereco(pessoa.getEnderecoList().get(0));
				ppe.setProcessoParte(processoParte);
				processoParte.getProcessoParteEnderecoList().add(ppe);
			} else {
				processoParte.setIsEnderecoDesconhecido(true);
			}
			
			EntityUtil.getEntityManager().persist(processoParte);
			processoTrf.getProcessoParteList().add(processoParte);
			
			//Permite que a parte visualize processo sigiloso
			if (visualizaSigilo) {
				try {
					ComponentUtil.getComponent(ProcessoJudicialService.class).acrescentaVisualizador(processoTrf, pessoa, processoParte.getProcuradoria());
				} catch (PJeBusinessException e) {
					e.printStackTrace();
				}
			}
		}		
	}
}
