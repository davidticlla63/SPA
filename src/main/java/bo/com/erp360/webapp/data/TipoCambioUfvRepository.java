package bo.com.erp360.webapp.data;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.TipoCambioUfv;

@Stateless
public class TipoCambioUfvRepository {

	@Inject
	private EntityManager em;

	public TipoCambioUfv findById(int id) {
		return em.find(TipoCambioUfv.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<TipoCambioUfv> findAllOrderedByID() {
		String query = "select em from TipoCambioUfv em ";// where em.estado='AC' or ser.estado='IN' order by em.id desc";
		System.out.println("Query TipoCambioUfv: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<TipoCambioUfv> findAllByEmpresa(Empresa empresa){
		try{
			String query = "select em from TipoCambioUfv em  where em.empresa.id="+empresa.getId()+" order by em.id asc";
			System.out.println("Query TipoCambioUfv: "+query);
			return em.createQuery(query).getResultList();
		}catch(Exception e){
			return new ArrayList<TipoCambioUfv>();
		}
	}

	@SuppressWarnings("unchecked")
	public List<TipoCambioUfv> findAllByEmpresaAndGEstionRegistrados(Empresa empresa, Gestion gestion){
		try{			
			Integer year = gestion.getGestion();			
			String query = "select em from TipoCambioUfv em  where em.empresa.id="+empresa.getId()+" and date_part('year', em.fecha)="+year;
			System.out.println("Query TipoCambioUfv: "+query);
			return em.createQuery(query).getResultList();
		}catch(Exception e){
			System.out.println("findAllByEmpresaAndGEstionRegistrados ERROR:"+e.getMessage());
			return null;
		}
	}


}
