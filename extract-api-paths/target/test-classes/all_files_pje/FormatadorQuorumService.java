/**
 * 
 */
package br.jus.cnj.pje.nucleo.service;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.util.FlexaoNumeroGenero;
import br.jus.cnj.pje.util.TipoFlexaoNumeroGeneroEnum;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoComposicao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.enums.SexoEnum;

@Name("formatadorQuorumService")
public class FormatadorQuorumService extends BaseService {

	@In
	private ComposicaoJulgamentoService composicaoJulgamentoService;
	
	public String obterTextoQuorumFormatado(SessaoPautaProcessoTrf processoPautado, boolean incluirRelator,	String preTexto, String posTexto, String tratamentoMagistradosTitulares, String tratamentoMagistradosAuxiliares){
		return obterTextoQuorumFormatado(processoPautado.getSessao(), processoPautado, incluirRelator, preTexto, posTexto, tratamentoMagistradosTitulares, tratamentoMagistradosAuxiliares);
	}
	
	public String obterTextoQuorumFormatado(Sessao sessao, String preTexto, String posTexto, String tratamentoMagistradosTitulares, String tratamentoMagistradosAuxiliares){
		return obterTextoQuorumFormatado(sessao, null, true, preTexto, posTexto, tratamentoMagistradosTitulares, tratamentoMagistradosAuxiliares );
	}
	

	public String obterInformacaoRelatoriaFormatada(SessaoPautaProcessoTrf processoPautado, String preTexto, String posTexto, String tratamentoMagistradoTitular, String tratamentoMagistradoAuxiliar, boolean relatorDesignado){
		String informacaoRelatoriaFormatada = "";
		
		SessaoPautaProcessoComposicao participanteRelator = null;
		if (relatorDesignado){
			participanteRelator = obterVencedorDiversoRelatorOriginal(processoPautado);
		}else{
			participanteRelator = obterRelator(processoPautado);
		}
		
		if (participanteRelator != null){
			TipoFlexaoNumeroGeneroEnum tipoFlexao = participanteRelator.getMagistradoPresente().getSexo().equals(SexoEnum.M) ? TipoFlexaoNumeroGeneroEnum.SM : TipoFlexaoNumeroGeneroEnum.SF;
			
			FlexaoNumeroGenero flexaoPreTexto = new FlexaoNumeroGenero(preTexto, true);
			
			String tratamento = participanteRelator.getCargoAtuacao().getAuxiliar() ? tratamentoMagistradoAuxiliar : tratamentoMagistradoTitular; 
			FlexaoNumeroGenero flexaoTratamento = new FlexaoNumeroGenero(tratamento, true);
			
			informacaoRelatoriaFormatada = flexaoPreTexto.getFlexao(tipoFlexao) + flexaoTratamento.getFlexao(tipoFlexao) + 
					participanteRelator.getMagistradoPresente().getNome() + posTexto; 
		}	
		
		return informacaoRelatoriaFormatada;
	}
	
	private SessaoPautaProcessoComposicao obterRelator(SessaoPautaProcessoTrf processoPautado) {
		SessaoPautaProcessoComposicao componenteRelator = null;
		
		for (SessaoPautaProcessoComposicao componente : processoPautado.getSessaoPautaProcessoComposicaoList()){
			if (componente.getOrgaoJulgador().equals(processoPautado.getProcessoTrf().getOrgaoJulgador())){
				componenteRelator = componente;
				break;
			}
			
		}
		
		return componenteRelator;
	}
	
	private SessaoPautaProcessoComposicao obterVencedorDiversoRelatorOriginal(SessaoPautaProcessoTrf processoPautado) {
		SessaoPautaProcessoComposicao vencedorDiverso = null;
		
		if (!processoPautado.getProcessoTrf().getOrgaoJulgador().equals(processoPautado.getOrgaoJulgadorVencedor())){
			for (SessaoPautaProcessoComposicao componente : processoPautado.getSessaoPautaProcessoComposicaoList()){
				if (componente.getOrgaoJulgador().equals(processoPautado.getOrgaoJulgadorVencedor())){
					vencedorDiverso = componente;
					break;
				}
				
			}
		}
				
		return vencedorDiverso;
	}
	
	private String obterTextoQuorumFormatado(Sessao sessao, SessaoPautaProcessoTrf processoPautado, boolean incluirRelator,
			String preTexto, String posTexto, String tratamentoMagistradosTitulares, String tratamentoMagistradosAuxiliares){

		FlexaoNumeroGenero flexoesTitulares  = new FlexaoNumeroGenero(tratamentoMagistradosTitulares);
		FlexaoNumeroGenero flexoesAuxiliares = new FlexaoNumeroGenero(tratamentoMagistradosAuxiliares);
		
		return obterTextoQuorumFormatado(sessao, processoPautado, incluirRelator, preTexto, posTexto, flexoesTitulares, flexoesAuxiliares);
	}
	
	private String obterTextoQuorumFormatado(Sessao sessao, SessaoPautaProcessoTrf processoPautado, boolean incluirRelator,
			String preTexto, String posTexto, FlexaoNumeroGenero flexoesTitulares, FlexaoNumeroGenero flexoesAuxiliares){
		
		List<PessoaMagistrado> magistradosTitulares  = composicaoJulgamentoService.obterQuorumMagistradosPresentes(sessao, processoPautado, incluirRelator, false);
		List<PessoaMagistrado> magistradosAuxiliares = composicaoJulgamentoService.obterQuorumMagistradosPresentes(sessao, processoPautado, incluirRelator, true);
		
		String textoQuorumFormatadoTitulares = obterQuorumParcialFormatado(magistradosTitulares, flexoesTitulares);
		String textoQuorumFormatadoAuxiliares = obterQuorumParcialFormatado(magistradosAuxiliares, flexoesAuxiliares);
		
		String textoConexao = "";
		if (!textoQuorumFormatadoTitulares.isEmpty() && !textoQuorumFormatadoAuxiliares.isEmpty()){
			textoConexao = " e ";
		}

		String textoQuorumFormatado = preTexto+" "+textoQuorumFormatadoTitulares + textoConexao + textoQuorumFormatadoAuxiliares + posTexto;
		
		return textoQuorumFormatado;		
	}
	
	private String obterQuorumParcialFormatado(List<PessoaMagistrado> magistrados, FlexaoNumeroGenero flexaoNumeroGenero){
		
		String quorumParcialFormatado = "";
			
		if (!magistrados.isEmpty()){
			TipoFlexaoNumeroGeneroEnum tipoFlexaoMagistrados = obterTipoFlexaoNumereroGenero(magistrados);
			String textoTratamentoMagistrados = flexaoNumeroGenero.getFlexao(tipoFlexaoMagistrados);
			quorumParcialFormatado = textoTratamentoMagistrados +" "+obterTextoParticipantes(magistrados);
		}
		
		return quorumParcialFormatado;
	}
	
	private String obterTextoParticipantes(List<PessoaMagistrado> magistrados){
		String participantes = "";
		for (int i=0; i < magistrados.size(); i++){
			PessoaMagistrado magistrado = magistrados.get(i);
			
			String conector;				
			if (i==0){
				conector = "";
			}
			else if (i==magistrados.size() -1){
				conector = " e ";
			}
			else {
				conector = ", ";
			}
			
			participantes += conector + magistrado.getNome();
		}
		
		return participantes;
	}
	
	private TipoFlexaoNumeroGeneroEnum obterTipoFlexaoNumereroGenero(List<PessoaMagistrado> magistrados) {
		Boolean singular = magistrados.size() < 2;
		Boolean masculino = false;
		
		for (PessoaMagistrado magistrado : magistrados) {
			if (magistrado.getSexo().equals(SexoEnum.M)){
				masculino = true;
				break;
			}
		}
		
		return TipoFlexaoNumeroGeneroEnum.getByCaracteristica(singular, masculino);
	}
	
}
