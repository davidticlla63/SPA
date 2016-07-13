package bo.com.erp360.webapp.util;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Date;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Dosificacion;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Factura;
import bo.com.erp360.webapp.model.NitCliente;

public class UtilidadesFacturacion implements Serializable {

	/**
	 * 
	 */

	private static final long serialVersionUID = -2579990640927344648L;

	public static Factura calcularDatosFacturacion(Factura newFactura,
			Dosificacion dosificacion, NitCliente nitCliente) throws Exception {
		System.out.println("Ingreso a calcularDatosFacturacion");
		newFactura.setFechaLimiteEmision(dosificacion.getFechaLimiteEmision()); // Fecha
																				// de
																				// Emision
		newFactura.setTotalLiteral(obtenerMontoLiteral(newFactura
				.getTotalFacturado()));
		// tipo de cliente
		if (nitCliente.getCliente().getTipo().equals("NATURAL")) { // NAURAL
																	// o
																	// JURIDICO
			newFactura.setNitCi(nitCliente.getCliente().getCi());// CI
																	// del
																	// Comprador
		} else {
			newFactura.setNitCi(nitCliente.getNit());// NIT del
														// Comprador
		}
		newFactura.setConcepto("Venta: " + newFactura.getNumeroFactura());
		newFactura.setFechaRegistro(new Date());
		newFactura.setNumeroFactura("" + dosificacion.getNumeroSecuencia());
		newFactura.setNitCi(nitCliente.getNit());
		newFactura.setNombreFactura(nitCliente.getCliente().getNombre());
		newFactura.setCodigoRespuestaRapida(armarCadenaQR(newFactura,
				newFactura.getEmpresa()));
		newFactura.setId(null);

		newFactura.setTipoCambio(6.96);// cambiar

		// LIBRO DE VENTA
		newFactura.setImporteICE(0);
		newFactura.setImporteExportaciones(0);
		newFactura.setImporteVentasGrabadasTasaCero(0);
		newFactura.setImporteSubTotal(newFactura.getTotalFacturado()
				- newFactura.getImporteICE()
				- newFactura.getImporteExportaciones()
				- newFactura.getImporteVentasGrabadasTasaCero());
		newFactura.setImporteDescuentosBonificaciones(0);
		newFactura.setImporteBaseDebitoFiscal(newFactura.getImporteSubTotal()
				- newFactura.getImporteDescuentosBonificaciones());
		if (newFactura.getSucursal().isCreditoFiscal()) {
			newFactura
					.setDebitoFiscal(newFactura.getImporteBaseDebitoFiscal() * 0.13);
			newFactura.setCreditoFiscal("");
		} else {
			newFactura.setImporteBaseDebitoFiscal(0);
			newFactura.setDebitoFiscal(0);
			newFactura.setCreditoFiscal("Sin Derecho a Credito Fiscal");
		}
		newFactura.setGestion(Time.obtenerFormatoYYYY(new Date()));
		newFactura.setMes(Time.obtenerFormatoMM(newFactura.getFechaRegistro()));
		System.out.println("finalizo  calcularDatosFacturacion");
		return newFactura;

	}

	public static String obtenerMontoLiteral(double totalFactura) {
		System.out.println("Total Entero Factura >>>>> " + totalFactura);
		NumerosToLetras convert = new NumerosToLetras();
		String totalLiteral;
		try {
			totalLiteral = convert.convertNumberToLetter(totalFactura);
			return totalLiteral;
		} catch (Exception e) {
			System.err.println("Error en obtenerMontoLiteral: "
					+ e.getMessage());
			return "Error Literal";
		}
	}

	public static String armarCadenaQR(Factura factura, Empresa empresaLogin) {
		String cadenaQR = "";
		try {
			cadenaQR = new String();

			// NIT emisor
			cadenaQR = cadenaQR.concat(empresaLogin.getNit());
			cadenaQR = cadenaQR.concat("|");

			// Numero de Factura
			cadenaQR = cadenaQR.concat(factura.getNumeroFactura());
			cadenaQR = cadenaQR.concat("|");

			// Numero de Autorizacion
			cadenaQR = cadenaQR.concat(factura.getNumeroAutorizacion());
			cadenaQR = cadenaQR.concat("|");

			// Fecha de Emision
			cadenaQR = cadenaQR.concat(Time.convertSimpleDateToString(factura
					.getFechaFactura()));
			cadenaQR = cadenaQR.concat("|");

			// Total Bs
			cadenaQR = cadenaQR.concat(String.valueOf(factura
					.getTotalFacturado()));
			cadenaQR = cadenaQR.concat("|");

			// Importe Base para el Credito Fiscal
			cadenaQR = cadenaQR.concat(String.valueOf(factura
					.getImporteBaseDebitoFiscal()));
			cadenaQR = cadenaQR.concat("|");

			// Codigo de Control
			cadenaQR = cadenaQR.concat(factura.getCodigoControl());
			cadenaQR = cadenaQR.concat("|");

			// NIT / CI del Comprador
			cadenaQR = cadenaQR.concat(factura.getNitCi());
			cadenaQR = cadenaQR.concat("|");

			// Importe ICE/IEHD/TASAS [cuando corresponda]
			cadenaQR = cadenaQR.concat("0");
			cadenaQR = cadenaQR.concat("|");

			// Importe por ventas no Gravadas o Gravadas a Tasa Cero [cuando
			// corresponda]
			cadenaQR = cadenaQR.concat("0");
			cadenaQR = cadenaQR.concat("|");

			// Importe no Sujeto a Credito Fiscal [cuando corresponda]
			cadenaQR = cadenaQR.concat("0");
			cadenaQR = cadenaQR.concat("|");

			// Descuentos Bonificaciones y Rebajas Obtenidas [cuando
			// corresponda]
			cadenaQR = cadenaQR.concat("0");

			return cadenaQR;

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error en armarCadenaQR: " + e.getMessage());
			return cadenaQR;
		}
	}

	public static String urlFacturaServlet(Factura newFactura,
			Dosificacion dosificacion, String urlLogo, String tamano)
			throws Exception {
		String url = "pIdFactura="
				+ newFactura.getId()
				+ "&pEmpresa="
				+ newFactura.getEmpresa().getRazonSocial()
				+ "&pCiudad="
				+ newFactura.getEmpresa().getCiudad()
				+ "&pPais=BOLIVIA&pLogo="
				+ urlLogo
				+ "&pNit="
				+ newFactura.getEmpresa().getNit()
				+ "&pQr="
				+ newFactura.getCodigoRespuestaRapida()
				+ "&pLeyenda="
				+ URLEncoder.encode(dosificacion.getLeyendaInferior2(),
						"ISO-8859-1") + "&pInpresion="
				+ newFactura.isImpresion() + "&pTamano=" + tamano;
		return url;
	}

}
