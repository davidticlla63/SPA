package bo.com.erp360.webapp.data;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Parametro;
import bo.com.erp360.webapp.model.Linea;
import bo.com.erp360.webapp.model.Sucursal;

@Stateless
public class ParametroRepository {

	@Inject
	private EntityManager em;

	public Parametro findById(int id) {
		return em.find(Parametro.class, id);
	}

	public Parametro findByNombre(String nombre) {
		String query = "select em from Parametro em  where em.nombre='"+nombre+"'";
		System.out.println("Query Parametro: "+query);
		return (Parametro) em.createQuery(query).getSingleResult();
	}


	@SuppressWarnings("unchecked")
	public List<Parametro> findAllOrderedByID() {
		String query = "select em from Parametro em ";// where em.estado='AC' or em.estado='IN' order by em.id desc";
		System.out.println("Query Parametro: "+query);
		return em.createQuery(query).getResultList();
	}


	@SuppressWarnings("unchecked")
	public List<Parametro> findAllActivasByEmpresaForSucursal(Empresa empresa,Sucursal  sucursal,String key) {
		String query = "select em from Parametro em where em.estado='AC' and em.empresa.id="+empresa.getId()+" and em.sucursal.id="+sucursal.getId()+" and em.key='"+key+"'  order by em.id desc";
		System.out.println("Query Parametro: "+query);
		return em.createQuery(query).getResultList();
	}
/*
	amy seydou elimane*/
	@SuppressWarnings("unchecked")
	public List<Parametro> findAllActivasByEmpresaForSucursal(Empresa empresa,Sucursal  sucursal) {
		String query = "select em from Parametro em where em.estado='AC' and em.empresa.id="+empresa.getId()+" and em.sucursal.id="+sucursal.getId()+"  order by em.id desc";
		System.out.println("Query Parametro: "+query);
		return em.createQuery(query).getResultList();
	}




}
