package bo.com.erp360.webapp.data;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.OrdenVenta;
import bo.com.erp360.webapp.model.Producto;
import bo.com.erp360.webapp.model.TipoProducto;
import bo.com.erp360.webapp.model.Usuario;

@Stateless
public class ProductoRepository {

	@Inject
	private EntityManager em;

	@Inject
	private Logger log;

	public Producto findById(int id) {
		return em.find(Producto.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<Producto> findAllOrderedByID() {
		String query = "select em from Producto em where em.estado='AC' or ser.estado='IN' order by em.id desc";
		log.info("Query Producto: " + query);
		return em.createQuery(query).getResultList();
	}

	public Producto findByRazonSocial(String razonSocial) {
		String query = "select em from Producto em where em.razonSocial='"
				+ razonSocial + "'";
		log.info("Query Producto: " + query);
		return (Producto) em.createQuery(query).getSingleResult();
	}

	public Producto findByNIT(String NIT) {
		String query = "select em from Producto em where em.NIT='" + NIT + "'";
		log.info("Query Producto: " + query);
		return (Producto) em.createQuery(query).getSingleResult();
	}

	public List<Producto> findAll() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Producto> criteria = cb.createQuery(Producto.class);
		Root<Producto> company = criteria.from(Producto.class);
		criteria.select(company);
		return em.createQuery(criteria).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Producto> findAllActivas(Empresa empresa) {
		String query = "select em from Producto em  where em.estado='AC' and em.empresa.id="
				+ empresa.getId() + "  order by em.id desc";
		log.info("Query Producto: " + query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Producto> findAllActivasForStructure(Empresa empresa) {
		String query = "select em from Producto em  where em.estado='AC' and em.empresa.id="
				+ empresa.getId() + "  order by em.tipoProducto.id, em.id  asc";
		log.info("Query Producto: " + query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<TipoProducto> findAllActivasTipoProducto(Empresa empresa) {
		String query = "select em from TipoProducto em  where em.state='AC' and em.empresa.id="
				+ empresa.getId()
				+ "  and  em.id in (select pro.tipoProducto.id from Producto pro  where pro.estado='AC' and pro.empresa.id="
				+ empresa.getId() + " ) order by em.id  asc";
		log.info("Query Producto: " + query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Producto> findAllActivasNotList(Empresa empresa,
			OrdenVenta ordenVenta) {
		String query = "select em from Producto em  where em.estado='AC' and em.empresa.id="
				+ empresa.getId()
				+ "  and em.id not in (select ord.producto.id from DetalleOrdenProducto ord where ord.ordenVenta.id="
				+ ordenVenta.getId() + "  ) order by em.id desc";
		log.info("Query Producto: " + query);
		return em.createQuery(query).getResultList();
	}

}
