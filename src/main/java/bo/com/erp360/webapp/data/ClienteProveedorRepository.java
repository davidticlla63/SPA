package bo.com.erp360.webapp.data;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Cliente;
import bo.com.erp360.webapp.model.ClienteProveedor;
import bo.com.erp360.webapp.model.Proveedor;

@Stateless
public class ClienteProveedorRepository {

	@Inject
	private EntityManager em;

	@Inject
	private Logger log;
	//log.info

	public ClienteProveedor findById(int id) {
		return em.find(ClienteProveedor.class, id);
	}

	public ClienteProveedor findByCliente(Cliente cliente) {
		String query = "select em from ClienteProveedor em where em.estado='AC' and em.cliente="+cliente.getId();
		log.info("Query ClienteProveedor: "+query);
		return (ClienteProveedor) em.createQuery(query).getSingleResult();
	}

	public ClienteProveedor findByProveedor(Proveedor proveedor) {
		try{
			String query = "select em from ClienteProveedor em where em.estado='AC' and em.proveedor="+proveedor.getId();
			log.info("Query ClienteProveedor: "+query);
			return (ClienteProveedor) em.createQuery(query).getSingleResult();
		}catch(javax.persistence.NoResultException e){
			log.info("findByProveedor() NOT FOUND ERROR: "+e.getMessage());
			return new ClienteProveedor();
		}
	}



}
