package br.jus.cnj.pje.view;

/**
 * @author: Éverton Nogueira
 * @Description: Enum responsável por mapear as funcionalidades reconhecidas pelo componente filtroDinamico.
 */
public enum FiltroDinamicoFuncionalidade {
	REDISTRIBUICAO_PROCESSOS("redistribuicaoProcessos");
	
	private String nomeFuncionalidade;
	
	private FiltroDinamicoFuncionalidade(String nomeFuncionalidade) {
		this.nomeFuncionalidade = nomeFuncionalidade;
	}
	public String getNomeFuncionalidade() {
		return nomeFuncionalidade;
	}
}