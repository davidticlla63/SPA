package bo.com.erp360.webapp.service;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.model.TipoCambioUfv;
import bo.com.erp360.webapp.model.TipoComprobante;

//The @Stateless annotation eliminates the need for manual transaction demarcation

@Stateless
public class TipoComprobanteRegistration extends DataAccessService<TipoComprobante>{

	@Inject
	private Logger log;

	@Inject
	private EntityManager em;

	@Inject
	private Event<TipoComprobante> tipoComprobanteEventSrc;

	public TipoComprobanteRegistration(){
		super(TipoComprobante.class);
	}

	public TipoComprobante registrarTipoComprobanteEmpresa(TipoComprobante tc){
		//registrar comprobante de ingreso
		try {
			log.info("Registering "+ tc.toString());
			this.em.persist(tc);
			this.em.flush();
			this.em.refresh(tc);
			tipoComprobanteEventSrc.fire(tc);
			log.info("register complet:  "+tc);
			return tc;
		} catch (Exception e) {
			log.info("Registering Error " + e);
			return null;
		} 
	}
}
