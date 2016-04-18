package bo.com.erp360.webapp.data;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import bo.com.erp360.webapp.model.AsientoContable;
import bo.com.erp360.webapp.model.Comprobante;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.PlanCuenta;

@Stateless
public class AsientoContableRepository {
	 
	@Inject
    private EntityManager em;
	
	private Logger log = Logger.getLogger(this.getClass());

    public AsientoContable findById(int id) {
        return em.find(AsientoContable.class, id);
    }
    
    @SuppressWarnings("unchecked")
	public List<AsientoContable> findAllOrderedByID() {
    	String query = "select em from AsientoContable em ";// where em.estado='AC' or em.estado='IN' order by em.id desc";
    	log.info("Query AsientoContable: "+query);
    	return em.createQuery(query).getResultList();
    }
    
   	public AsientoContable findByCuenta(PlanCuenta planCuenta, Gestion gestion) {
       	String query = "select ac  from AsientoContable ac, Comprobante c,Gestion g where ac.planCuenta.id = :idPlanCuenta  and c.id = ac.comprobante.id and g.id = c.gestion.id and g.gestion = :idGestion order by c.numero desc LIMIT 1";
       	log.info("Query AsientoContable: "+query);
       	return (AsientoContable) em.createQuery(query).setParameter("idPlanCuenta", planCuenta.getId()).setParameter("idGestion", gestion.getGestion()).getSingleResult();
     }
    
    public List<AsientoContable> findAll(){
    	CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<AsientoContable> criteria = cb.createQuery(AsientoContable.class);
		Root<AsientoContable> asientoContable = criteria.from(AsientoContable.class);
		criteria.select(asientoContable);
		return em.createQuery(criteria).getResultList();
    }
    
	@SuppressWarnings("unchecked")
	public List<AsientoContable>  findByComprobante(Comprobante comprobante) {
       	String query = "select em  from AsientoContable em where em.comprobante.id = :idComprobante order by em.id desc";
       	log.info("Query AsientoContable: "+query);
       	return  em.createQuery(query).setParameter("idComprobante", comprobante.getId()).getResultList();
     }
	
	@SuppressWarnings("unchecked")
	public List<AsientoContable>  findByPeridodo(int start,int maxRows,int periodo,Empresa empresa,Gestion gestion) {
		String query = "SELECT ac FROM AsientoContable ac,Comprobante co,Gestion ge WHERE co.gestion.id = ge.id  AND ac.comprobante.id = co.id AND date_part('month', co.fecha) = "+periodo+" AND ge.id= "+gestion.getId()+" AND ge.empresa.id = "+empresa.getId()+"  order by ac.id asc";
       	log.info("Query start ="+start+",maxRows"+maxRows+", AsientoContable: "+query);
       	return  em.createQuery(query).setFirstResult(start).setMaxResults(maxRows).getResultList();
     }
	
	@SuppressWarnings("unchecked")
	public List<AsientoContable>  findByFechas(int start,int maxRows,Date fechaInicial,Date fechaFinal,Empresa empresa) {
		String query = "SELECT ac FROM AsientoContable ac , Comprobante co, Empresa em WHERE co.empresa.id = em.id AND ac.comprobante.id = co.id AND co.fecha>=:stDate AND co.fecha<=:edDate AND em.id= "+empresa.getId();
       	log.info("Query AsientoContable: "+query);
       	return  em.createQuery(query).setParameter("stDate", fechaInicial).setParameter("edDate", fechaFinal).setFirstResult(start).setMaxResults(maxRows).getResultList();
     }

	public Long countTotalRecordByPeridodo(int periodo,Empresa empresa,Gestion gestion) {
       	String query = "SELECT COUNT(ac) FROM AsientoContable ac,Comprobante co,Gestion ge WHERE co.gestion.id = ge.id  AND ac.comprobante.id = co.id AND date_part('month', co.fecha) = "+periodo+" AND ge.id= "+gestion.getId()+" AND ge.empresa.id = "+empresa.getId();
       	log.info("Query AsientoContable: "+query);
       	return  (Long)em.createQuery(query).getSingleResult();
     }

	public Long  countTotalRecordByFechas(Date fechaInicial,Date fechaFinal,Empresa empresa) {
       	String query = "SELECT COUNT(ac) FROM AsientoContable ac, Comprobante co, Empresa em WHERE co.empresa.id = em.id AND ac.comprobante.id = co.id AND co.fecha>=:stDate AND co.fecha<=:edDate AND em.id= "+empresa.getId();
       	log.info("Query fechaInicial="+fechaInicial+" , fechaFinal"+fechaFinal+" AsientoContable: "+query);
       	return  (Long) em.createQuery(query).setParameter("stDate", fechaInicial).setParameter("edDate", fechaFinal).getSingleResult();
     }
}
