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


public enum TipoCaracteristicaFisicaEnum {
	
	ALTURA("Altura") {
		@Override
		public CaracteristicaFisicaEnum[] getCaracteristicasFisicas() {
			CaracteristicaFisicaEnum[] result = new CaracteristicaFisicaEnum[5];			
			result[0] = CaracteristicaFisicaEnum.ALTURA_1_60;
			result[1] = CaracteristicaFisicaEnum.ALTURA_1_70;
			result[2] = CaracteristicaFisicaEnum.ALTURA_1_80;
			result[3] = CaracteristicaFisicaEnum.ALTURA_1_90;
			result[4] = CaracteristicaFisicaEnum.ALTURA_MAIS_DE_1_90;
			return result;
		}
	},
	BARBA("Barba") {
		@Override
		public CaracteristicaFisicaEnum[] getCaracteristicasFisicas() {
			CaracteristicaFisicaEnum[] result = new CaracteristicaFisicaEnum[5];
			result[0] = CaracteristicaFisicaEnum.BARBA_CAVANHAQUE;
			result[1] = CaracteristicaFisicaEnum.BARBA_CHEIA;
			result[2] = CaracteristicaFisicaEnum.BARBA_IMBERBE;
			result[3] = CaracteristicaFisicaEnum.BARBA_RALA;
			result[4] = CaracteristicaFisicaEnum.BARBA_RASPADA;
			return result;
		}
	},
	BIGODE("Bigode") {
		@Override
		public CaracteristicaFisicaEnum[] getCaracteristicasFisicas() {
			CaracteristicaFisicaEnum[] result = new CaracteristicaFisicaEnum[6];
			result[0] = CaracteristicaFisicaEnum.BIGODE_COMPRIDO;
			result[1] = CaracteristicaFisicaEnum.BIGODE_CURTO;
			result[2] = CaracteristicaFisicaEnum.BIGODE_FINO;
			result[3] = CaracteristicaFisicaEnum.BIGODE_GROSSO;
			result[4] = CaracteristicaFisicaEnum.BIGODE_NORMAL;
			result[5] = CaracteristicaFisicaEnum.BIGODE_RASPADO;
			return result;
		}
	},
	BOCA("Boca") {
		@Override
		public CaracteristicaFisicaEnum[] getCaracteristicasFisicas() {
			CaracteristicaFisicaEnum[] result = new CaracteristicaFisicaEnum[3];
			result[0] = CaracteristicaFisicaEnum.BOCA_GRANDE;
			result[1] = CaracteristicaFisicaEnum.BOCA_MEDIA;
			result[2] = CaracteristicaFisicaEnum.BOCA_PEQUENA;
			return result;
		}
	},
	COMPLEIO("Compleio") {
		@Override
		public CaracteristicaFisicaEnum[] getCaracteristicasFisicas() {
			CaracteristicaFisicaEnum[] result = new CaracteristicaFisicaEnum[4];
			result[0] = CaracteristicaFisicaEnum.COMPLEIO_GORDO;
			result[1] = CaracteristicaFisicaEnum.COMPLEIO_MAGRO;
			result[2] = CaracteristicaFisicaEnum.COMPLEIO_RAQUITICO;
			result[3] = CaracteristicaFisicaEnum.COMPLEIO_TRONCUDO;			
			return result;
		}
	},
	TIPO_CABELO("Tipo de Cabelo") {
		@Override
		public CaracteristicaFisicaEnum[] getCaracteristicasFisicas() {
			CaracteristicaFisicaEnum[] result = new CaracteristicaFisicaEnum[5];
			result[0] = CaracteristicaFisicaEnum.TIPO_CABELO_CALVO;
			result[1] = CaracteristicaFisicaEnum.TIPO_CABELO_CRESPOS;
			result[2] = CaracteristicaFisicaEnum.TIPO_CABELO_ENCARACOLADOS;
			result[3] = CaracteristicaFisicaEnum.TIPO_CABELO_LISOS;
			result[4] = CaracteristicaFisicaEnum.TIPO_CABELO_ONDULADOS;
			return result;
		}
	},
	COR_CABELO("Cor do Cabelo") {
		@Override
		public CaracteristicaFisicaEnum[] getCaracteristicasFisicas() {
			CaracteristicaFisicaEnum[] result = new CaracteristicaFisicaEnum[6];
			result[0] = CaracteristicaFisicaEnum.COR_CABELO_BRANCOS;
			result[1] = CaracteristicaFisicaEnum.COR_CABELO_CASTANHOS;
			result[2] = CaracteristicaFisicaEnum.COR_CABELO_GRISALHOS;
			result[3] = CaracteristicaFisicaEnum.COR_CABELO_LOUROS;
			result[4] = CaracteristicaFisicaEnum.COR_CABELO_PRETOS;
			result[5] = CaracteristicaFisicaEnum.COR_CABELO_RUIVOS;
			return result;
		}
	},
	FORMATO_OLHOS("Formato dos Olhos") {
		@Override
		public CaracteristicaFisicaEnum[] getCaracteristicasFisicas() {
			CaracteristicaFisicaEnum[] result = new CaracteristicaFisicaEnum[4];
			result[0] = CaracteristicaFisicaEnum.FORMATO_OLHOS_GRANDES;
			result[1] = CaracteristicaFisicaEnum.FORMATO_OLHOS_ORIENTAIS;
			result[2] = CaracteristicaFisicaEnum.FORMATO_OLHOS_PEQUENOS;
			result[3] = CaracteristicaFisicaEnum.FORMATO_OLHOS_REDONDOS;
			return result;
		}
	},
	COR_OLHOS("Cor dos Olhos") {
		@Override
		public CaracteristicaFisicaEnum[] getCaracteristicasFisicas() {
			CaracteristicaFisicaEnum[] result = new CaracteristicaFisicaEnum[5];
			result[0] = CaracteristicaFisicaEnum.COR_OLHOS_AZUIS;
			result[1] = CaracteristicaFisicaEnum.COR_OLHOS_CASTANHOS;
			result[2] = CaracteristicaFisicaEnum.COR_OLHOS_MISTOS;
			result[3] = CaracteristicaFisicaEnum.COR_OLHOS_PRETOS;
			result[4] = CaracteristicaFisicaEnum.COR_OLHOS_VERDES;			
			return result;
		}
	},
	COR_PELE("Cor da Pele") {
		@Override
		public CaracteristicaFisicaEnum[] getCaracteristicasFisicas() {
			CaracteristicaFisicaEnum[] result = new CaracteristicaFisicaEnum[6];
			result[0] = CaracteristicaFisicaEnum.COR_PELE_AMARELA;
			result[1] = CaracteristicaFisicaEnum.COR_PELE_BRANCA;
			result[2] = CaracteristicaFisicaEnum.COR_PELE_INDIGENA;
			result[3] = CaracteristicaFisicaEnum.COR_PELE_NEGRA;
			result[4] = CaracteristicaFisicaEnum.COR_PELE_OUTRAS;
			result[5] = CaracteristicaFisicaEnum.COR_PELE_PARDA;
			return result;
		}
	},
	LABIOS("Lábios") {
		@Override
		public CaracteristicaFisicaEnum[] getCaracteristicasFisicas() {
			CaracteristicaFisicaEnum[] result = new CaracteristicaFisicaEnum[4];
			result[0] = CaracteristicaFisicaEnum.LABIOS_FINOS;
			result[1] = CaracteristicaFisicaEnum.LABIOS_GROSSOS;
			result[2] = CaracteristicaFisicaEnum.LABIOS_LEPORINOS;
			result[3] = CaracteristicaFisicaEnum.LABIOS_MEDIOS;
			return result;
		}
	},
	NARIZ("Nariz") {
		@Override
		public CaracteristicaFisicaEnum[] getCaracteristicasFisicas() {
			CaracteristicaFisicaEnum[] result = new CaracteristicaFisicaEnum[6];
			result[0] = CaracteristicaFisicaEnum.NARIZ_ACHATADO;
			result[1] = CaracteristicaFisicaEnum.NARIZ_AFILADO;
			result[2] = CaracteristicaFisicaEnum.NARIZ_ARREBITADO;
			result[3] = CaracteristicaFisicaEnum.NARIZ_COMPRIDO;
			result[4] = CaracteristicaFisicaEnum.NARIZ_CURVO_ADUNCO;
			result[5] = CaracteristicaFisicaEnum.NARIZ_PEQUENO;
			return result;
		}
	},
	ORELHA("Orelha") {
		@Override
		public CaracteristicaFisicaEnum[] getCaracteristicasFisicas() {
			CaracteristicaFisicaEnum[] result = new CaracteristicaFisicaEnum[5];
			result[0] = CaracteristicaFisicaEnum.ORELHAS_ABERTAS;
			result[1] = CaracteristicaFisicaEnum.ORELHAS_COLADAS;
			result[2] = CaracteristicaFisicaEnum.ORELHAS_GRANDES;
			result[3] = CaracteristicaFisicaEnum.ORELHAS_MEDIAS;
			result[4] = CaracteristicaFisicaEnum.ORELHAS_PEQUENAS;			
			return result;
		}
	},
	PESCOCO("Pescoço") {
		@Override
		public CaracteristicaFisicaEnum[] getCaracteristicasFisicas() {
			CaracteristicaFisicaEnum[] result = new CaracteristicaFisicaEnum[5];
			result[0] = CaracteristicaFisicaEnum.PESCOCO_COMPRIDO;
			result[1] = CaracteristicaFisicaEnum.PESCOCO_CURTO;
			result[2] = CaracteristicaFisicaEnum.PESCOCO_FINO;
			result[3] = CaracteristicaFisicaEnum.PESCOCO_GROSSO;
			result[4] = CaracteristicaFisicaEnum.PESCOCO_MEDIO;			
			return result;
		}
	},
	ROSTO("Rosto") {
		@Override
		public CaracteristicaFisicaEnum[] getCaracteristicasFisicas() {
			CaracteristicaFisicaEnum[] result = new CaracteristicaFisicaEnum[4];
			result[0] = CaracteristicaFisicaEnum.ROSTO_OVAL;
			result[1] = CaracteristicaFisicaEnum.ROSTO_QUADRADO;
			result[2] = CaracteristicaFisicaEnum.ROSTO_REDONDO;
			result[3] = CaracteristicaFisicaEnum.ROSTO_TRIANGULAR;			
			return result;
		}
	},
	SOBRANCELHA("Sobrancelha") {
		@Override
		public CaracteristicaFisicaEnum[] getCaracteristicasFisicas() {
			CaracteristicaFisicaEnum[] result = new CaracteristicaFisicaEnum[4];
			result[0] = CaracteristicaFisicaEnum.SOBRANCELHAS_FINAS;
			result[1] = CaracteristicaFisicaEnum.SOBRANCELHAS_GROSSAS;
			result[2] = CaracteristicaFisicaEnum.SOBRANCELHAS_SEPARADAS;
			result[3] = CaracteristicaFisicaEnum.SOBRANCELHAS_UNIDAS;			
			return result;
		}
	},
	TESTA("Testa") {
		@Override
		public CaracteristicaFisicaEnum[] getCaracteristicasFisicas() {
			CaracteristicaFisicaEnum[] result = new CaracteristicaFisicaEnum[4];
			result[0] = CaracteristicaFisicaEnum.TESTA_ALTA;
			result[1] = CaracteristicaFisicaEnum.TESTA_COM_ENTRADAS;
			result[2] = CaracteristicaFisicaEnum.SOBRANCELHAS_SEPARADAS;
			result[3] = CaracteristicaFisicaEnum.TESTA_CURTA;
			return result;
		}
	},
	OUTROS("Outros") {
		@Override
		public CaracteristicaFisicaEnum[] getCaracteristicasFisicas() {
			CaracteristicaFisicaEnum[] result = new CaracteristicaFisicaEnum[1];
			result[0] = CaracteristicaFisicaEnum.OUTROS;
			return result;
		}
	};
	
	private String label;

	TipoCaracteristicaFisicaEnum(String label) {
		this.label = label;
	
	}

	public String getLabel() {
		return this.label;
	}
	
	public abstract CaracteristicaFisicaEnum[] getCaracteristicasFisicas();

}
