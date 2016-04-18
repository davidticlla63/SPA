package bo.com.erp360.webapp.service;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import bo.com.erp360.webapp.data.EmpresaRepository;
import bo.com.erp360.webapp.data.GestionRepository;
import bo.com.erp360.webapp.data.SucursalRepository;
import bo.com.erp360.webapp.data.UsuarioRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.model.Usuario;

public class EstadoUsuarioLogin implements Serializable{

	private static final long serialVersionUID = 5793623941703126286L;
	private FacesContext facesContext;

	public EstadoUsuarioLogin(FacesContext facesContext){
		this.facesContext = facesContext;
	}
	
	public Sucursal getSucursalSession(EmpresaRepository empresaRepository,SucursalRepository sucursalRepository){
		String sucursal ="";
		System.out.println("Ingreso a getSucursalSession");
		try{
			HttpSession request1 = (HttpSession) facesContext.getExternalContext().getSession(false);
			sucursal = request1.getAttribute("sucursal")!=null?request1.getAttribute("sucursal").toString():"";
			System.out.println("SUCURSAL : "+sucursal);
			if(!sucursal.isEmpty()){
				HttpSession request2 = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
				Empresa empresa = empresaRepository.findByRazonSocial(request2.getAttribute("empresa").toString());
				return sucursalRepository.findBySucursalEmpresa(sucursal,empresa);
			}
			return null;
		}catch(Exception e){
			Logger.getLogger(getClass().getName()).log(
					Level.SEVERE, "getSucursalSession()...", e);
			return null;
		}
	}

	public Empresa getEmpresaSession(EmpresaRepository empresaRepository){
		String empresa= "";
		try{
			HttpSession request = (HttpSession) facesContext.getExternalContext().getSession(false);
			empresa = request.getAttribute("empresa")!=null?request.getAttribute("empresa").toString():"";
			if(! empresa.isEmpty()){
				return empresaRepository.findByRazonSocial(empresa);
			}
			return null;
		}catch(Exception e){
			Logger.getLogger(getClass().getName()).log(
					Level.SEVERE, "getEmpresaSession()...", e);
			return null;
		}
	}

	public Gestion getGestionSession(EmpresaRepository empresaRepository,GestionRepository gestionRepository){
		Integer gestion = -1;
		try{
			HttpSession request1 = (HttpSession) facesContext.getExternalContext().getSession(false);
			gestion = request1.getAttribute("gestion")!=null?Integer.parseInt( request1.getAttribute("gestion").toString() ):-1;
			if(gestion!= -1){
				HttpSession request2 = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
				Empresa empresa = empresaRepository.findByRazonSocial(request2.getAttribute("empresa").toString());
				return gestionRepository.findByGestionEmpresa(gestion,empresa);
			}
			return null;
		}catch(Exception e){
			Logger.getLogger(getClass().getName()).log(
					Level.SEVERE, "getGestionSession()...", e);
			return null;
		}
	}

	public String getNombreUsuarioSession(){
		HttpServletRequest request1 = (HttpServletRequest) facesContext
				.getExternalContext().getRequest();
		return request1.getUserPrincipal() == null ? "null" : request1.getUserPrincipal().getName();
	}

	public Usuario getUsuarioSession(UsuarioRepository usuarioRepository){
		try{
			return usuarioRepository.findByLogin(getNombreUsuarioSession());
		}catch(Exception e){
			Logger.getLogger(getClass().getName()).log(
					Level.SEVERE, "getUsuarioSession()...", e);
			return null;
		}
	}

	public String getParameterRequest(String name){
		HttpServletRequest request = (HttpServletRequest) facesContext
				.getExternalContext().getRequest();
		return request.getParameter(name);
	}

	public void setAttributeSession(String key,String value){
		try{
			HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			session.setAttribute(key, value);
		}catch(Exception e){
			Logger.getLogger(getClass().getName()).log(
					Level.SEVERE, "setAttributeSession()...", e);
		}		
	}

	public String getAttributeSession(String key){
		try {
			HttpSession request = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			return request.getAttribute(key)!=null ? (String) request.getAttribute(key):null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean removeAttributeSession(String key){
		try {
			HttpSession request = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			request.removeAttribute(key);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
