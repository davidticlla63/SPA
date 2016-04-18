package bo.com.erp360.webapp.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.TipoCambio;
import bo.com.erp360.webapp.util.Fechas;

@Stateless
public class TipoCambioRepository {

	@Inject
	private EntityManager em;
	
	@Inject
	private Logger log;

	public TipoCambio findById(int id) {
		return em.find(TipoCambio.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<TipoCambio> findAllOrderedByID() {
		String query = "select em from TipoCambio em ";// where em.estado='AC' or em.estado='IN' order by em.id desc";
		log.info("Query TipoCambio: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<TipoCambio> findAllByEmpresa(Empresa empresa){
		String query = "select em from TipoCambio em  where em.empresa.id="+empresa.getId()+" order by em.id asc";
		log.info("Query TipoCambio: "+query);
		return em.createQuery(query).getResultList();
	}
	
	public TipoCambio findTipoCambioDiaAnterior(Empresa empresa, Date date1){
		try{
			Date date = Fechas.restarDiasFecha(date1,1);
			Calendar calendar = Calendar.getInstance();
	        calendar.setTime(date);     
	        
			String year = new SimpleDateFormat("yyyy").format(date);
			Integer month = Integer.parseInt(new SimpleDateFormat("MM").format(date).toString());
			String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
			log.info("findTipoCambioDiaAnterior"+year+month+day);
			String query = "select em from TipoCambio em  where em.empresa.id="+empresa.getId()+" and date_part('month', em.fecha)="+month+" and date_part('year', em.fecha)="+year+" and date_part('day', em.fecha)="+day;
			log.info("Query TipoCambio: "+query);
			return (TipoCambio)em.createQuery(query).getSingleResult();
		}catch(Exception e){
			log.severe("findTipoCambioDiaAnterior ERROR:"+e.getMessage());
			return null;
		}
	}

	public TipoCambio findAllByEmpresaAndFecha(Empresa empresa, Date date){
		try{
			Calendar calendar = Calendar.getInstance();
	        calendar.setTime(date);     
	        
			String year = new SimpleDateFormat("yyyy").format(date);
			Integer month = Integer.parseInt(new SimpleDateFormat("MM").format(date).toString());
			String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
			log.info("findAllByEmpresaAndFecha :"+year+month+day);
			String query = "select em from TipoCambio em  where em.empresa.id="+empresa.getId()+" and date_part('month', em.fecha)="+month+" and date_part('year', em.fecha)="+year+" and date_part('day', em.fecha)="+day;
			log.info("Query TipoCambio: "+query);
			return (TipoCambio)em.createQuery(query).getSingleResult();
		}catch(Exception e){
			log.severe("findAllByEmpresaAndFecha ERROR:"+e.getMessage());
			return null;
		}
	}
	
	public TipoCambio findUltimoRegistroTipoCambio(Empresa empresa ,Gestion gestion){
		try{			
			Integer year = gestion.getGestion();			
			String query = "select MAX(em.id) from TipoCambio em  where em.empresa.id="+empresa.getId()+" and date_part('year', em.fecha)="+year;
			log.info("Query TipoCambio: "+query);
			return findById((Integer)em.createQuery(query).getSingleResult());
		}catch(Exception e){
			log.severe("findUltimoRegistroTipoCambio ERROR:"+e.getMessage());
			return null;
		}
	}
	
	public List<TipoCambio> findUltimoRegistroTipoCambio2(Empresa empresa ,Gestion gestion){
		try{			
			Integer year = gestion.getGestion();			
			String query = "select MAX(em.id) from TipoCambio em  where em.empresa.id="+empresa.getId()+" and date_part('year', em.fecha)="+year;
			log.info("Query TipoCambio: "+query);
			return em.createQuery(query).getResultList();
		}catch(Exception e){
			log.severe("findUltimoRegistroTipoCambio ERROR:"+e.getMessage());
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<TipoCambio> findAllByEmpresaAndGEstionRegistrados(Empresa empresa, Gestion gestion){
		try{			
			Integer year = gestion.getGestion();			
			String query = "select em from TipoCambio em  where em.empresa.id="+empresa.getId()+" and date_part('year', em.fecha)="+year;
			log.info("Query TipoCambio: "+query);
			return em.createQuery(query).getResultList();
		}catch(Exception e){
			log.severe("findAllByEmpresaAndGEstionRegistrados ERROR:"+e.getMessage());
			return null;
		}
	}
}
