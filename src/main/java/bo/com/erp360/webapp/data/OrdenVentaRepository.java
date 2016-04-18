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

import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.OrdenVenta;
import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.util.Time;
 
@Stateless
public class OrdenVentaRepository {
	
	@Inject
    private EntityManager em;
	
	@Inject
	private Logger log;

    public OrdenVenta findById(int id) {
        return em.find(OrdenVenta.class, id);
    }
    
    @SuppressWarnings("unchecked")
	public List<OrdenVenta> findAllOrderedByID() {
    	String query = "select em from OrdenVenta em where em.estado='AC' or ser.estado='IN' order by em.id desc";
    	log.info("Query OrdenVenta: "+query);
    	return em.createQuery(query).getResultList();
    }
    
    public OrdenVenta findByRazonSocial(String razonSocial) {
    	String query = "select em from OrdenVenta em where em.razonSocial='"+razonSocial+"'";
    	log.info("Query OrdenVenta: "+query);
    	return (OrdenVenta) em.createQuery(query).getSingleResult();
    }
    
    public OrdenVenta findByNIT(String NIT) {
    	String query = "select em from OrdenVenta em where em.nit='"+NIT+"'";
    	log.info("Query OrdenVenta: "+query);
    	return (OrdenVenta) em.createQuery(query).getSingleResult();
    }

    public List<OrdenVenta> findAll(){
    	CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<OrdenVenta> criteria = cb.createQuery(OrdenVenta.class);
		Root<OrdenVenta> company = criteria.from(OrdenVenta.class);
		criteria.select(company);
		return em.createQuery(criteria).getResultList();
    }
   
    
  
    
    
    @SuppressWarnings("unchecked")
	public List<OrdenVenta> findAllActivas(Empresa empresa) {
    	String query = "select em from OrdenVenta em  where em.estado='AC' and em.empresa.id="+empresa.getId()+"  order by em.id desc";
    	log.info("Query OrdenVenta: "+query);
    	return em.createQuery(query).getResultList();
    }
    
    @SuppressWarnings("unchecked")
   	public List<OrdenVenta> findAllActivas(Empresa empresa,Sucursal sucursal,Date fecha) {
       	String query = "select em from OrdenVenta em  where (em.estado='AC' or em.estado='PR' ) and em.empresa.id="+empresa.getId()+" and em.sucursal.id="+sucursal.getId()+"  and  to_number(to_char(em.fechaRegistro ,'YYYYMMDD'), '999999999')="
			+ Time.obtenerFormatoYYYYMMDD(fecha)+"   order by em.id desc";
       	log.info("Query OrdenVenta: "+query);
       	return em.createQuery(query).getResultList();
       }
    
    
    @SuppressWarnings("unchecked")
   	public int findCorrelativoDiaria(Empresa empresa,Sucursal sucursal,Date fecha) {
       	String query = "select max(em.numeroSecuencia)+1 from OrdenVenta em  where (em.estado='AC' or em.estado='PR' ) and em.empresa.id="+empresa.getId()+" and em.sucursal.id="+sucursal.getId()+"  and  to_number(to_char(em.fechaRegistro ,'YYYYMMDD'), '999999999')="
			+ Time.obtenerFormatoYYYYMMDD(fecha);
       	log.info("Query OrdenVenta: "+query);
       Object obj= em.createQuery(query).getSingleResult();
       	if (obj==null) {
			return 1;
		}else{
			return (int) obj;
		}
       }
    
    @SuppressWarnings("unchecked")
   	public List<OrdenVenta> findAllActivasProcesadas(Empresa empresa,Sucursal sucursal,Date fecha) {
       	String query = "select em from OrdenVenta em  where em.estado='PR' and em.empresa.id="+empresa.getId()+" and em.sucursal.id="+sucursal.getId()+"  and  to_number(to_char(em.fechaRegistro ,'YYYYMMDD'), '999999999')="
			+ Time.obtenerFormatoYYYYMMDD(fecha)+"   order by em.id desc";
       	log.info("Query OrdenVenta: "+query);
       	return em.createQuery(query).getResultList();
       }
  
    
	
}
