package bo.com.erp360.webapp.data;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.AsientoContable;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.Mayor;
import bo.com.erp360.webapp.model.PlanCuenta;

@Stateless
public class MayorRepository {

	@Inject
	private EntityManager em;

	private Logger log = Logger.getLogger(this.getClass());

	public Mayor findById(int id) {
		return em.find(Mayor.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<Mayor> findAllOrderedByID() {
		String query = "select em from Mayor em ";// where em.estado='AC' or ser.estado='IN' order by em.id desc";
		log.info("Query Mayor: "+query);
		return em.createQuery(query).getResultList();
	}

	/**
	 * Obtner el ultimo registro de la cuenta seleccionada
	 * @param planCuenta
	 * @return
	 */
	public Mayor findNumeroByPlanCuenta(PlanCuenta planCuenta,Gestion gestion) {
		try{
			String query = "select MAX(m.id) from Mayor m, PlanCuenta pc, AsientoContable ac,Empresa em, Gestion ge where pc.empresa.id=em.id and ge.empresa.id=em.id and m.asientoContable.id=ac.id and ac.planCuenta.id="+planCuenta.getId()+" and ge.id="+gestion.getId();
			log.info("Query Mayor: "+query);
			return findById((Integer) em.createQuery(query).getSingleResult());
		}catch(Exception e){
			return null;
		}
	}

	public Mayor findByAsientoContable(AsientoContable asientoContable){
		String query = "select em from Mayor em where em.asientoContable.id="+asientoContable.getId();
		log.info("Query Mayor: "+query);
		return findById((Integer) em.createQuery(query).getSingleResult());

	}

	@SuppressWarnings("unchecked")
	public List<PlanCuenta> findByFecha(Date fechaInicio, Date fechaFin,Empresa empresa,Gestion gestion){
		String query = "select pc from Mayor em,PlanCuenta pc where em.planCuenta.id=pc.id AND em.fecha>=:stDate AND em.fecha<=:edDate group by pc.id ";
		log.info("Query Mayor: "+query);
		return em.createQuery(query).setParameter("stDate", fechaInicio).setParameter("edDate", fechaFin).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<PlanCuenta> findByCuentaInicialAndCuentaFinal(PlanCuenta planCuentaInicial,PlanCuenta planCuentaFinal,Date fechaInicio, Date fechaFin,Empresa empresa,Gestion gestion){
		String query = "select pc from Mayor em,PlanCuenta pc where em.planCuenta.id=pc.id AND em.fecha>=:stDate AND em.fecha<=:edDate group by pc.id ";
		log.info("Query Mayor: "+query);
		return em.createQuery(query).setParameter("stDate", fechaInicio).setParameter("edDate", fechaFin).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Mayor> findByFechaAndPlanCuenta(int start,int maxRows,Date fechaInicio, Date fechaFin,PlanCuenta pc){
		String query = "select em from Mayor em where  em.fecha>=:stDate and em.fecha<=:edDate and em.planCuenta.id="+pc.getId();
		log.info("Query Mayor: "+query);
		return em.createQuery(query).setFirstResult(start).setMaxResults(maxRows).setParameter("stDate", fechaInicio).setParameter("edDate", fechaFin).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Mayor> findByFechaAndPlanCuenta(Date fechaInicio, Date fechaFin,PlanCuenta pc){
		String query = "select em from Mayor em where  em.fecha>=:stDate and em.fecha<=:edDate and em.planCuenta.id="+pc.getId();
		log.info("Query Mayor: "+query);
		return em.createQuery(query).setParameter("stDate", fechaInicio).setParameter("edDate", fechaFin).getResultList();
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Mayor> findByFechas(int start,int maxRows,Date fechaInicio, Date fechaFin,Empresa empresa){
		log.info("Query Mayor: start="+start+", maxRows="+maxRows+", fechaInicio="+fechaInicio+", fechaFin="+fechaFin+""+", empresa="+empresa);
		String query = "select ma from Mayor ma,Empresa em,PlanCuenta pc where ma.planCuenta.id=pc.id and pc.empresa.id=em.id and em.id=:pIdEmpresa and ma.fecha>=:stDate and ma.fecha<=:edDate order by pc.id asc";
		return em.createQuery(query).setFirstResult(start).setMaxResults(maxRows).setParameter("stDate", fechaInicio).setParameter("edDate", fechaFin).setParameter("pIdEmpresa", empresa.getId()).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public double findSaldoAnterior(Date fechaAnterior , PlanCuenta pc){
		String query = "select em from Mayor em where  em.fecha=:stDate and em.planCuenta.id="+ pc.getId()+" ORDER BY em.id DESC";
		log.info("Query Mayor: "+query);
		List<Mayor> listM = em.createQuery(query).setParameter("stDate", fechaAnterior).getResultList();
		return listM.size()>0? listM.get(0).getSaldoNacional():0;
	}
	
	public Long  countTotalRecordByFechas(Date fechaInicial,Date fechaFinal,Empresa empresa) {
       	String query = "SELECT COUNT(ma) from Mayor ma,Empresa em,PlanCuenta pc where ma.planCuenta.id=pc.id and pc.empresa.id=em.id and em.id=:pIdEmpresa and ma.fecha>=:stDate and ma.fecha<=:edDate";
       	log.info("Query fechaInicial="+fechaInicial+" , fechaFinal"+fechaFinal+" Mayor query: "+query);
       	return  (Long) em.createQuery(query).setParameter("stDate", fechaInicial).setParameter("edDate", fechaFinal).setParameter("pIdEmpresa", empresa.getId()).getSingleResult();
     }

}
