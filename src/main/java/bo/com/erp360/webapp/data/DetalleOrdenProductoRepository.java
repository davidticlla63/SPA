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
import bo.com.erp360.webapp.model.DetalleOrdenProducto;
import bo.com.erp360.webapp.model.OrdenCompra;
import bo.com.erp360.webapp.model.OrdenVenta;
import bo.com.erp360.webapp.model.Usuario;
 
@Stateless
public class DetalleOrdenProductoRepository {
	
	@Inject
    private EntityManager em;
	
	@Inject
	private Logger log;

    public DetalleOrdenProducto findById(int id) {
        return em.find(DetalleOrdenProducto.class, id);
    }
    
      
    
    @SuppressWarnings("unchecked")
	public List<DetalleOrdenProducto> findAllActivasByOrdenVenta(OrdenVenta ordenVenta) {
    	String query = "select em from DetalleOrdenProducto em  where em.estado='AC' and em.ordenVenta.id="+ordenVenta.getId()+"  order by em.id desc";
    	log.info("Query DetalleOrdenProducto: "+query);
    	return em.createQuery(query).getResultList();
    }
    
    
    @SuppressWarnings("unchecked")
 	public List<DetalleOrdenProducto> findAllActivasByOrdenVenta(OrdenVenta ordenVenta, Usuario vendedor) {
     	String query = "select em from DetalleOrdenProducto em  where em.estado='AC' and em.ordenVenta.id="+ordenVenta.getId()+" and em.vendedor.id="+vendedor.getId()+"  order by em.id desc";
     	log.info("Query DetalleOrdenProducto: "+query);
     	return em.createQuery(query).getResultList();
     }
     
  
    
	
}
