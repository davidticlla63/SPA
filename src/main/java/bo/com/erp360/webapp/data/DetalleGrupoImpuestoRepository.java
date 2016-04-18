package bo.com.erp360.webapp.data;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.DetalleGrupoImpuesto;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.GrupoImpuesto;

@Stateless
public class DetalleGrupoImpuestoRepository {

	@Inject
	private EntityManager em;
	
	@Inject
    private Logger log;
	//log.info

	public DetalleGrupoImpuesto findById(int id) {
		return em.find(DetalleGrupoImpuesto.class, id);
	}

	public DetalleGrupoImpuesto findByEmpresa(Empresa empresa) {
		try{
			String query = "select em from DetalleGrupoImpuesto em  where (em.estado='AC' or em.estado='IN') and em.empresa.id="+empresa.getId()+ " order by em.id desc";
			log.info("Query DetalleGrupoImpuesto: "+query);
			return (DetalleGrupoImpuesto) em.createQuery(query).getSingleResult();
		}catch(Exception e){
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<DetalleGrupoImpuesto> findAllByGrupoIpuesto(GrupoImpuesto grupoImpuesto) {
		try{
			String query = "select em from DetalleGrupoImpuesto em  where em.grupoImpuesto.id="+grupoImpuesto.getId()+ " order by em.id desc";
			log.info("Query DetalleGrupoImpuesto: "+query);
			return em.createQuery(query).getResultList();
		}catch(Exception e){
			return new ArrayList<DetalleGrupoImpuesto>();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<DetalleGrupoImpuesto> findActivosByEmpresa(Empresa empresa) {
		try{
			String query = "select em from DetalleGrupoImpuesto em, GrupoImpuesto gi  where em.grupoImpuesto.id=gi.id and gi.estado='AC' and gi.empresa.id="+empresa.getId()+ " order by em.id desc";
			log.info("Query DetalleGrupoImpuesto: "+query);
			return em.createQuery(query).getResultList();
		}catch(Exception e){
			return new ArrayList<DetalleGrupoImpuesto>();
		}
	}

}
