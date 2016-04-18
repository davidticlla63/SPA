package bo.com.erp360.webapp.data;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.DetalleFactura;
import bo.com.erp360.webapp.model.SubDetalleFactura;

@Stateless
public class SubDetalleFacturaRepository {

	@Inject
	private EntityManager em;

	public SubDetalleFactura findById(int id) {
		return em.find(SubDetalleFactura.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<SubDetalleFactura> findAllActivasByNotaVenta(DetalleFactura detalleFactura) {
		String query = "select em from SubDetalleFactura em where em.estado='AC' and em.detalleFactura.id="+detalleFactura.getId()+" order by em.id asc";
		System.out.println("Query SubDetalleFactura: "+query);
		return em.createQuery(query).getResultList();
	}



}
