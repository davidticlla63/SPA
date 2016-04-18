package bo.com.erp360.webapp.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.controller.SucursalController;
import bo.com.erp360.webapp.model.Dosificacion;
import bo.com.erp360.webapp.model.Sucursal;

@Stateless
public class DosificacionRepository {

	@Inject
	private EntityManager em;
	
	Logger log = Logger.getLogger(SucursalController.class);

	public Dosificacion findById(int id) {
		return em.find(Dosificacion.class, id);
	}


	@SuppressWarnings("unchecked")
	public List<Dosificacion> findAllActivasBySucursal(Sucursal sucursal) {
		try{
			String query = "select em from Dosificacion em  where em.estado='AC' and em.sucursal.id="+sucursal.getId()+ " order by em.id desc";
			log.info("Query Dosificacion: "+query);
			return em.createQuery(query).getResultList();
		}catch(Exception e){
			return new ArrayList<>();
		}
	}
	
	public Dosificacion findActivaBySucursal(Sucursal sucursal) {
		try{
			String query = "select em from Dosificacion em  where em.estado='AC' and activo=TRUE and em.sucursal.id="+sucursal.getId()+ " order by em.id desc";
			log.info("Query Dosificacion: "+query);
			return (Dosificacion) em.createQuery(query).getSingleResult();
		}catch(Exception e){
			return null;
		}
	}

}
