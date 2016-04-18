package bo.com.erp360.webapp.data;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Factura;
import bo.com.erp360.webapp.model.NotaVenta;
import bo.com.erp360.webapp.model.Pais;
import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.util.Time;

@Stateless
public class NotaVentaRepository {

	@Inject
	private EntityManager em;

	public NotaVenta findById(int id) {
		return em.find(NotaVenta.class, id);
	}


	@SuppressWarnings("unchecked")
	public List<NotaVenta> findAllOrderedByID() {
		String query = "select em from NotaVenta em ";// where em.estado='AC' or em.estado='IN' order by em.id desc";
		System.out.println("Query NotaVenta: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<NotaVenta> findAll() {
		String query = "select em from NotaVenta em where (em.estado='AC' or em.estado='IN') order by em.id desc";
		System.out.println("Query NotaVenta: "+query);
		return em.createQuery(query).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<NotaVenta> findAllActivasByEmpresaSucursal(Empresa empresa,Sucursal sucursal) {
		String query = "select em from NotaVenta em where em.estado='AC' and em.empresa.id="+empresa.getId()+" and em.sucursal.id="+sucursal.getId()+" order by em.nombre asc";
		System.out.println("Query NotaVenta: "+query);
		return em.createQuery(query).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public NotaVenta findAllActivasByEmpresaSucursalForFactura(Empresa empresa,Sucursal sucursal,Factura factura) {
		String query = "select em from NotaVenta em where em.estado='AC' and em.empresa.id="+empresa.getId()+" and em.sucursal.id="+sucursal.getId()+" and numeroFactura='"+factura.getNumeroFactura()+"' ";
		System.out.println("Query NotaVenta: "+query);
		return (NotaVenta) em.createQuery(query).getSingleResult();
	}
	@SuppressWarnings("unchecked")
	public List<NotaVenta> findAllActivasByEmpresa(Empresa empresa) {
		String query = "select em from NotaVenta em where em.estado='AC' and em.empresa.id="+empresa.getId()+" order by em.nombre asc";
		System.out.println("Query NotaVenta: "+query);
		return em.createQuery(query).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<NotaVenta> findAllActivasByEmpresaSucursal(Empresa empresa,Sucursal sucursal,String usuario, Date fechaInicio, Date fechaFin) {
		String query = "select em from NotaVenta em where em.estado='AC' and em.empresa.id="+empresa.getId()+" and em.sucursal.id="+sucursal.getId()+"  and (em.usuarioRegistro like '"+usuario+"' or em.usuarioRegistro like '"+usuario+"') and to_number(to_char(em.fechaRegistro ,'YYYYMMDD'), '99999999')>="
			+ Time.obtenerFormatoYYYYMMDD(fechaInicio)+" and  to_number(to_char(em.fechaRegistro ,'YYYYMMDD'), '99999999')<="
			+ Time.obtenerFormatoYYYYMMDD(fechaFin)+"   order by em.id desc";
		System.out.println("Query NotaVenta: "+query);
		return em.createQuery(query).getResultList();
	}




}
