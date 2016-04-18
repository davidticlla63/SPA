package bo.com.erp360.webapp.service;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Factura;
import bo.com.erp360.webapp.model.Sucursal;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class FacturaRegistration extends DataAccessService<Factura> {
	@Inject
    private EntityManager em ;//= emf.createEntityManager();
	public FacturaRegistration() {
		super(Factura.class);
	}
	
	 
	    
	 
	public void anularFactura(Factura factura) throws Exception {
		log.info("Ingreso --> AnularFactura " + factura.getNumeroFactura()
				+ " NIT: " + factura.getNitCi() + " Cliente: "
				+ factura.getNombreFactura());
		String nit= factura.getNitCi();
		factura.setEstado("A");
		factura.setNombreFactura("ANULADA");
		factura.setNitCi("0");
		factura.setTotalLiteral("ANULADA");
		factura.setTotalFacturado(0);
		factura.setTotalEfectivo(0);
		factura.setTotalEfectivo(0);
		factura.setTotalPagar(0);
		factura.setCambio(0);

		// libro venta
		factura.setImporteICE(0);
		factura.setImporteExportaciones(0);
		factura.setImporteVentasGrabadasTasaCero(0);
		factura.setImporteSubTotal(0);
		factura.setImporteDescuentosBonificaciones(0);
		factura.setImporteBaseDebitoFiscal(0);
		factura.setDebitoFiscal(0);

		Sucursal sucursal = em.find(Sucursal.class, factura.getSucursal()
				.getId());

		// actualizar codigo de respuesta rapida
		String codigoRespuestaRapida = nit + "|"
				+ factura.getNumeroFactura() + "|"
				+ factura.getNumeroAutorizacion() + "|"
				+ formatearFecha(factura.getFechaFactura()) + "|"
				+ round(factura.getTotalFacturado()) + "|"
				+ round(factura.getImporteBaseDebitoFiscal()) + "|"
				+ factura.getCodigoControl() + "|" + factura.getNitCi()
				+ "|0|0|0|0";

		factura.setCodigoRespuestaRapida(codigoRespuestaRapida);

		// actualizar codigo de control

		em.merge(factura);
	
	}
	
	
	   public String formatearFecha(Date date){
	    	try {
	    		String DATE_FORMAT = "dd/MM/yyyy";
				SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
	    		return sdf.format(date);
	    		
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("Error en formatearFecha: "+e.getMessage());
				return null;
			}
		}
	    
	    public static String round(double value) {
		    try {
		    	DecimalFormat df = new DecimalFormat("####0.00");
		    	return df.format(value);
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("Error en round: "+e.getMessage());
				return "0.00";
			}
		}

}
