package bo.com.erp360.webapp.data;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import bo.com.erp360.webapp.model.DetalleOrdenProducto;
import bo.com.erp360.webapp.model.DetalleOrdenServicio;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.OrdenCompra;
import bo.com.erp360.webapp.model.OrdenVenta;
import bo.com.erp360.webapp.model.Producto;
import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.util.Time;

@Stateless
public class DetalleOrdenServicioRepository {

	@Inject
	private EntityManager em;

	@Inject
	private Logger log;

	public DetalleOrdenServicio findById(int id) {
		return em.find(DetalleOrdenServicio.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<DetalleOrdenServicio> findAllActivasByOrdenVenta(
			OrdenVenta ordenVenta) {
		String query = "select em from DetalleOrdenServicio em  where em.estado='AC' and em.ordenVenta.id="
				+ ordenVenta.getId() + "  order by em.id desc";
		log.info("Query DetalleOrdenServicio: " + query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<DetalleOrdenServicio> findAllActivasForDate(Empresa empresa,
			Sucursal sucursal, Usuario usuario, Date fecha) {
		String query = "select em from DetalleOrdenServicio em  where em.estado='AC' and em.ordenVenta.empresa.id="
				+ empresa.getId()
				+ " and em.ordenVenta.sucursal.id="
				+ sucursal.getId()
				+ "  and em.vendedor.id="
				+ usuario.getId()
				+ " and  to_number(to_char(em.fechaRegistro ,'YYYYMMDD'), '999999999')="
				+ Time.obtenerFormatoYYYYMMDD(fecha) + "  order by em.id desc";
		log.info("Query DetalleOrdenServicio: " + query);
		return em.createQuery(query).getResultList();
	}

}
