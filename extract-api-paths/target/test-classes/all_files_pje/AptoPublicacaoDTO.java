package br.jus.cnj.pje.nucleo.dto;

public class AptoPublicacaoDTO implements Comparable<AptoPublicacaoDTO> {

	private int ordem;
	private int idSessaoPautaProcessoTrf;

	public AptoPublicacaoDTO() {
		super();
	}

	public AptoPublicacaoDTO(int ordem, int idSessaoPautaProcessoTrf) {
		super();
		this.ordem = ordem;
		this.idSessaoPautaProcessoTrf = idSessaoPautaProcessoTrf;
	}

	public int getOrdem() {
		return ordem;
	}

	public void setOrdem(int ordem) {
		this.ordem = ordem;
	}

	public int getIdSessaoPautaProcessoTrf() {
		return idSessaoPautaProcessoTrf;
	}

	public void setIdSessaoPautaProcessoTrf(int idSessaoPautaProcessoTrf) {
		this.idSessaoPautaProcessoTrf = idSessaoPautaProcessoTrf;
	}

	@Override
	public int compareTo(AptoPublicacaoDTO proximo) {
		if (this.ordem < proximo.ordem) {
			return -1;
		}
		if (this.ordem > proximo.ordem) {
			return 1;
		}
		return 0;
	}
}
