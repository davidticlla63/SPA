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

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Factura;
import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.util.Time;
 
@Stateless
public class FacturaRepository {
	
	@Inject
    private EntityManager em;
	
	@Inject
	private Logger log;

	   public Factura findById(int id) {
	        return em.find(Factura.class, id);
	    }
	    
	    @SuppressWarnings("unchecked")
		public List<Factura> findAllOrderedByID() {
	    	String query = "select em from Factura em where em.estado='V' or ser.estado='IN' order by em.id desc";
	    	log.info("Query Factura: "+query);
	    	return em.createQuery(query).getResultList();
	    }
	    
	    public Factura findByRazonSocial(String razonSocial) {
	    	String query = "select em from Factura em where em.razonSocial='"+razonSocial+"'";
	    	log.info("Query Factura: "+query);
	    	return (Factura) em.createQuery(query).getSingleResult();
	    }
	    
	    public Factura findByNIT(String NIT) {
	    	String query = "select em from Factura em where em.nit='"+NIT+"'";
	    	log.info("Query Factura: "+query);
	    	return (Factura) em.createQuery(query).getSingleResult();
	    }

	    public List<Factura> findAll(){
	    	CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Factura> criteria = cb.createQuery(Factura.class);
			Root<Factura> company = criteria.from(Factura.class);
			criteria.select(company);
			return em.createQuery(criteria).getResultList();
	    }
	    
	    public List<Factura> findAllOrderedByFechaRegistro() {
	        CriteriaBuilder cb = em.getCriteriaBuilder();
	        CriteriaQuery<Factura> criteria = cb.createQuery(Factura.class);
	        Root<Factura> factura = criteria.from(Factura.class);
	        criteria.select(factura).orderBy(cb.desc(factura.get("fechaRegistro")));
	        return em.createQuery(criteria).getResultList();
	    }
	   
	    
	    @SuppressWarnings("unchecked")
		public List<Factura> findAllActivas(String usuario,Empresa empresa) {
	    	String query = "select em from Factura em  where   em.empresa.id="+empresa.getId()+" and (em.usuarioRegistro like '"+usuario+"' or em.usuarioRegistro like '"+usuario+"')  order by em.fechaRegistro desc";
	    	log.info("Query Factura: "+query);
	    	return em.createQuery(query).getResultList();
	    }
	    
	    
	    @SuppressWarnings("unchecked")
		public List<Factura> traerFacturasEntreFechasActivas2(String usuario,Empresa empresa,Date fechaini,Date fechafin) {
	    	String query = "select em from Factura em  where   em.empresa.id="+empresa.getId()+" and (em.usuarioRegistro like '"+usuario+"' or em.usuarioRegistro like '"+usuario+"') and  to_number(to_char(em.fechaRegistro ,'YYYYMMDD'), '999999999')>="
			+ Time.obtenerFormatoYYYYMMDD(fechaini)+" and  to_number(to_char(em.fechaRegistro ,'YYYYMMDD'), '999999999')<="
			+ Time.obtenerFormatoYYYYMMDD(fechafin)+"  order by em.id desc";
	    	log.info("Query Factura: "+query);
	    	return em.createQuery(query).getResultList();
	    }
	    
	    
	    @SuppressWarnings("unchecked")
	  		public List<Factura> traerFacturasEntreFechasActivas(String usuario,Empresa empresa,Date fechaini,Date fechafin) {
	  	    	String query = "select em from Factura em  where   em.empresa.id="+empresa.getId()+" and (em.usuarioRegistro like '"+usuario+"' or em.usuarioRegistro like '"+usuario+"') and  em.fechaRegistro between '"
	  			+ Time.convertSimpleDateToString(fechaini)+"' and  '"+Time.convertSimpleDateToString(fechafin)+"'  order by em.id desc";
	  	    	log.info("Query Factura: "+query);
	  	    	return em.createQuery(query).getResultList();
	  	    }
	    
	    
	    @SuppressWarnings("unchecked")
  		public List<Factura> traerFacturasEntreFechasActivas(String usuario,Empresa empresa,Sucursal sucursal,Date fechaini,Date fechafin) {
  	    	String query = "select em from Factura em  where   em.empresa.id="+empresa.getId()+" and em.sucursal.id="+sucursal.getId()+" and (em.usuarioRegistro like '"+usuario+"' or em.usuarioRegistro like '"+usuario+"') and to_number(to_char(em.fechaRegistro ,'YYYYMMDD'), '99999999')>="
			+ Time.obtenerFormatoYYYYMMDD(fechaini)+" and  to_number(to_char(em.fechaRegistro ,'YYYYMMDD'), '99999999')<="
			+ Time.obtenerFormatoYYYYMMDD(fechafin)+"   order by em.id desc";
  	    	log.info("Query Factura: "+query);
  	    	return em.createQuery(query).getResultList();
  	    }
	  	    
	    
	  /*  and Date between '2011/02/25' and '2011/02/27'*/
	    
	    @SuppressWarnings("unchecked")
		public List<String> traerGestionesFacturadas(){
	    	try {
	    		System.out.println("Ingreso a traerGestionesFacturadas");
				String query = "select distinct fac.gestion from Factura fac";
				return em.createQuery(query).getResultList();
				
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("Error en traerGestionesFacturadas: "+e.getMessage());
				return null;
			}
	    }
	    
	    
	    @SuppressWarnings("unchecked")
		public int numeroCorrelativo(String usuario,Empresa empresa) {
	    	String query = "select max( em.id ) +1  from Factura em  where em.estado='V' and em.empresa.id="+empresa.getId()+" and (em.usuarioRegistro like '"+usuario+"' or em.usuarioRegistro like '"+usuario+"')";
	    	log.info("Query Factura: "+query);
	    	Object obj=  em.createQuery(query).getSingleResult();
	    	log.info(obj.toString());
	    	if (obj==null)
				return 1;
			else {
				return (int) obj;
			}
	    }
	    
	  
	    
	    
	    @SuppressWarnings("unchecked")
		public List<Factura> findAllActivas(Empresa empresa) {
	    	String query = "select em from Factura em  where em.estado='V' and em.empresa.id="+empresa.getId()+"  order by em.id desc";
	    	log.info("Query Factura: "+query);
	    	return em.createQuery(query).getResultList();
	    }
	    
	    
	    @SuppressWarnings("unchecked")
		public List<Factura> buscarFacturasSucursal(Date fechaIni, Date fechaFin, String estado, int sucursalID) {
	        try {
	        	System.out.println("Ingreso a buscarFacturasSucursal....");
	    	    
	    	    return em.createQuery("select fac FROM Factura fac WHERE fac.fechaFactura>=:stDate and fac.fechaFactura<=:edDate "
	    	    		+ "and fac.estado like '%"+estado+"%' and fac.sucursal.id=:pSucursal order by fac.fechaRegistro desc")
	    	    .setParameter("stDate", fechaIni)
	    	    .setParameter("edDate", fechaFin)
	    	    .setParameter("pSucursal", sucursalID)
	    	    .getResultList();
	    	    
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				System.out.println("Error en buscarFacturasSucursal: "+e.getMessage());
				return null;
			}
	    }
	    
	    public List<Factura> traerComprasPeriodoFiscal(String gestion, String mes){
	    	try {
	    		System.out.println("Ingreso a traerComprasPeriodoFiscal");
				String query = "select comp from Factura comp where comp.gestion='"+gestion+"' and comp.mes='"+mes+"' and comp.estado='V' order by comp.fechaRegistro asc"; 
				return em.createQuery(query).getResultList();
				
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("Error en traerComprasPeriodoFiscal: "+e.getMessage());
				return null;
			}
	    }
	    
	    public List<Factura> traerComprasPeriodoFiscal(String gestion, String mes,String idSucuarsal){
	    	try {
	    		System.out.println("Ingreso a traerComprasPeriodoFiscal");
				/*String query = "select comp from Factura comp where comp.gestion='"+gestion+"' and comp.mes='"+mes+"' and comp.sucursal.id="+idSucuarsal+" and comp.estado='V' order by comp.fechaRegistro asc";*/
	    		String query = "select comp from Factura comp where comp.gestion='"+gestion+"' and comp.mes='"+mes+"' and comp.sucursal.id="+idSucuarsal+" and (comp.estado='V' or comp.estado='A') order by comp.fechaRegistro asc";
				return em.createQuery(query).getResultList();
				
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("Error en traerComprasPeriodoFiscal: "+e.getMessage());
				return null;
			}
	    }
	    
	    public List<Factura> traerComprasPeriodoFiscalValidas(String gestion, String mes,String idSucuarsal){
	    	try {
	    		System.out.println("Ingreso a traerComprasPeriodoFiscalValidas");
				/*String query = "select comp from Factura comp where comp.gestion='"+gestion+"' and comp.mes='"+mes+"' and comp.sucursal.id="+idSucuarsal+" and comp.estado='V' order by comp.fechaRegistro asc";*/
	    		String query = "select comp from Factura comp where comp.gestion='"+gestion+"' and comp.mes='"+mes+"' and comp.sucursal.id="+idSucuarsal+" and comp.estado='V' order by comp.fechaRegistro asc";
				return em.createQuery(query).getResultList();
				
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("Error en traerComprasPeriodoFiscalValidas: "+e.getMessage());
				return null;
			}
	    }
	    
	    public List<Factura> traerComprasPeriodoFiscalAnuladas(String gestion, String mes,String idSucuarsal){
	    	try {
	    		System.out.println("Ingreso a traerComprasPeriodoFiscalAnuladas");
				/*String query = "select comp from Factura comp where comp.gestion='"+gestion+"' and comp.mes='"+mes+"' and comp.sucursal.id="+idSucuarsal+" and comp.estado='V' order by comp.fechaRegistro asc";*/
	    		String query = "select comp from Factura comp where comp.gestion='"+gestion+"' and comp.mes='"+mes+"' and comp.sucursal.id="+idSucuarsal+" and  comp.estado='A' order by comp.fechaRegistro asc";
				return em.createQuery(query).getResultList();
				
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("Error en traerComprasPeriodoFiscalAnuladas: "+e.getMessage());
				return null;
			}
	    }
	    
    
	
}
