package br.jus.cnj.pje.nucleo.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.TransactionPropagationType;
import org.jboss.seam.annotations.Transactional;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ProcessoParteDAO;
import br.jus.cnj.pje.nucleo.manager.EnderecoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteRepresentanteManager;
import br.jus.pje.nucleo.dto.AproveitarAdvogadosDTO;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;


@Name(AproveitarAdvogadosService.NAME)
public class AproveitarAdvogadosService extends BaseService implements Serializable{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "aproveitarAdvogadosService";

	public List<AproveitarAdvogadosDTO> procurarAdvogadosProcessoParteByNumeroProcesso(Integer numeroSequencia, 
			Integer digitoVerificador, 
			Integer ano, 
			Integer numeroOrigem, 
			String ramoJustica, 
			String respectivoTribunal ) {
		List<AproveitarAdvogadosDTO> retorno = new ArrayList<AproveitarAdvogadosDTO>(0);
		List<Object[]> objetos = ComponentUtil.getComponent(ProcessoParteDAO.class).procurarAdvogadosProcessoParte(numeroSequencia, digitoVerificador, ano, numeroOrigem, ramoJustica, respectivoTribunal);
		montarResultado(retorno, objetos);
		return retorno;
	}
	
	public List<AproveitarAdvogadosDTO> procurarAdvogadosProcessoParte(Integer idPessoa) {
		List<AproveitarAdvogadosDTO> retorno = new ArrayList<AproveitarAdvogadosDTO>(0);
		List<Object[]> objetos = ComponentUtil.getComponent(ProcessoParteDAO.class).procurarAdvogadosProcessoParte(idPessoa);
		montarResultado(retorno, objetos);
		return retorno;
	}
	
	@Transactional(TransactionPropagationType.REQUIRED)
	public void vincularAdvogadosSelecionados(ProcessoParte processoPartePrincipal, List<AproveitarAdvogadosDTO> listaProcessosParteAdvogadosSelecionados) throws Exception {
		if(processoPartePrincipal == null || (listaProcessosParteAdvogadosSelecionados == null || listaProcessosParteAdvogadosSelecionados.isEmpty())) {
			throw new IllegalArgumentException("Parametros incorretos recebidos para vinculao");
		}
		for (AproveitarAdvogadosDTO appvo : listaProcessosParteAdvogadosSelecionados) {
			Pessoa pessoa = ComponentUtil.getComponent(PessoaManager.class).findById(appvo.getIdPessoa());
			Endereco endereco = null;
			if(!appvo.getIsEnderecoDesconhecido() && appvo.getIdEnderecoCadastrado() != null) {
				endereco = ComponentUtil.getComponent(EnderecoManager.class).findById(appvo.getIdEnderecoCadastrado());
			}
			ComponentUtil.getComponent(ProcessoParteRepresentanteManager.class).inserirRepresentante(processoPartePrincipal, pessoa, ParametroUtil.instance().getTipoParteAdvogado(), processoPartePrincipal.getInParticipacao(), endereco);
		}
	}

	private void montarResultado(List<AproveitarAdvogadosDTO> retorno, List<Object[]> resultado) {
		for (Object[] obj : resultado) {
			retorno.add(new AproveitarAdvogadosDTO(
					String.valueOf(obj[0]), 					//nome usuario
					Integer.parseInt(String.valueOf(obj[1])), 	//idPessoa
					String.valueOf(obj[2]), 	//inParticipacao
					ProcessoParteSituacaoEnum.valueOf(String.valueOf(obj[3])), 					//inSituacao
					String.valueOf(obj[4]),						//nr_oab
					String.valueOf(obj[5]),						//estado_oab
					String.valueOf(obj[6]),						//nr_cpf
					String.valueOf(obj[7]),						//enderecoDesconhecido
					String.valueOf(obj[8]),					//id_endereco_cadastrado
					String.valueOf(obj[9]))); 					//nome da parte representada

		}
	}
}
