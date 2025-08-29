/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.enums;


public enum TipoOrigemAcaoEnum implements PJeEnum {
		/**
		 * Vinculado ao processo como polo ativo
		 */
		PA("Polo ativo"),
		/**
		 * Vinculado ao processo como polo passivo
		 */
		PP("Polo passivo"),
		/**
		 * Vinculado ao processo como outros interessados
		 */
		OU("Outros interessados"),
		/**
		 * Servidor do tribunal ou rotina interna do sistema
		 */
		I("Interno"),
		/**
		 * Outra pessoa não vinculada ao processo e não é do tribunal
		 */
		E("Externo");
		
		private String label;
		
		TipoOrigemAcaoEnum(String label) {
			this.label = label;
		}
		
		@Override
		public String getLabel() {
			return this.label;
		}
}