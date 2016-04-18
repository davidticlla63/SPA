package bo.com.erp360.webapp.data;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Cotizacion;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;

@Stateless
public class CotizacionRepository {
	 
	@Inject
    private EntityManager em;

    public Cotizacion findById(int id) {
        return em.find(Cotizacion.class, id);
    }
    
    @SuppressWarnings("unchecked")
	public List<Cotizacion> findAllOrderedByID() {
    	String query = "select em from Cotizacion em ";// where em.estado='AC' or em.estado='IN' order by em.id desc";
    	System.out.println("Query Cotizacion: "+query);
    	return em.createQuery(query).getResultList();
    }
    
    public Integer findNumeroCorrelativo(Empresa empresa, Gestion gestion) {
    	String query = "select em from Cotizacion em  where (em.estado='AC' or em.estado='IN') and em.empresa.id="+empresa.getId()+" and em.gestion.id="+gestion.getId()+" order by em.id desc";
    	System.out.println("Query Cotizacion: "+query);
    	return (em.createQuery(query).getResultList().size())+1;
    }
    
    @SuppressWarnings("unchecked")
	public List<Cotizacion> findAllByEmpresaGestion(Empresa empresa,Gestion gestion) {
    	String query = "select em from Cotizacion em where em.empresa.id="+empresa.getId()+" and em.gestion.id="+gestion.getId();
    	System.out.println("Query Cotizacion: "+query);
    	return em.createQuery(query).getResultList();
    }    
    
    @SuppressWarnings("unchecked")
	public List<Cotizacion> findByNumero(Integer numeroCotizacion) {
    	String query = "select em from Cotizacion em where em.numero="+numeroCotizacion;
    	System.out.println("Query Cotizacion: "+query);
    	return em.createQuery(query).getResultList();
    }  
	
}
