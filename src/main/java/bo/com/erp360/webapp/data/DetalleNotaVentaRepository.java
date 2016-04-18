package bo.com.erp360.webapp.data;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.DetalleNotaVenta;
import bo.com.erp360.webapp.model.NotaVenta;

@Stateless
public class DetalleNotaVentaRepository {

	@Inject
	private EntityManager em;

	public DetalleNotaVenta findById(int id) {
		return em.find(DetalleNotaVenta.class, id);
	}


	@SuppressWarnings("unchecked")
	public List<DetalleNotaVenta> findAllActivasByNotaVenta(NotaVenta notaVenta) {
		String query = "select em from DetalleNotaVenta em where em.estado='AC' and em.notaVenta.id="+notaVenta.getId()+" order by em.id asc";
		System.out.println("Query DetalleNotaVenta: "+query);
		return em.createQuery(query).getResultList();
	}


}
