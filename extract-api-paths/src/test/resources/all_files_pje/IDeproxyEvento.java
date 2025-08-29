package br.com.itx.util;

/**
 * PJE-JT: David Vieira: [[PJE-1010] Interface que será implementada no projeto PJE pelo EventoHome para fazer deproxy da classe Evento.
 * 
 * Foi preciso fazer essa inversao de controle porque o FI-Core nao enxerga a classe Evento do FI-BPM.
 * 
 * Utilizado no AnnotationUtil desta forma: ((IDeproxyEvento) ComponentUtil.getComponent("EventoHome")).deproxy(object)
 */
public interface IDeproxyEvento{

	Object deproxy(Object object);

}
