package bo.com.erp360.webapp.util;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import bo.com.erp360.webapp.data.EmpresaRepository;
import bo.com.erp360.webapp.data.FormatoHojaRepository;
import bo.com.erp360.webapp.data.GestionRepository;
import bo.com.erp360.webapp.data.PermisoRepository;
import bo.com.erp360.webapp.data.SucursalRepository;
import bo.com.erp360.webapp.data.TemplatesRepository;
import bo.com.erp360.webapp.data.UsuarioRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.FormatoHoja;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.model.Templates;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.model.security.PermisoV1;
import bo.com.erp360.webapp.model.security.Rol;
import bo.com.erp360.webapp.service.FormatoHojaRegistration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class SessionMain, datos persistente durante la session del usuario
 * @author David.Ticlla.Felipe
 *
 */

@Named
@SessionScoped
public class SessionMain implements Serializable {

	private static final long serialVersionUID = -645068727337928781L;
	private @Inject FacesContext facesContext;
	private Logger log = Logger.getLogger(this.getClass());

	//Repository
	private @Inject PermisoRepository permmisoRepository;
	private @Inject UsuarioRepository usuarioRepository;
	private @Inject EmpresaRepository empresaRepository;
	private @Inject GestionRepository gestionRepository;
	
	private @Inject SucursalRepository sucursalRepository;
	
	private @Inject TemplatesRepository templatesRepository;

	//Object
	private Usuario usuarioLoggin;
	private Empresa empresaLoggin;
	private Gestion gestionLoggin;
	
	private  Sucursal sucursalLoggin;

	private StreamedContent fotoPerfil;

	//list Permisos del usuario
	private List<PermisoV1> listPermiso;

	private EDatosEmpresa datosEmpresa;
	
	private Templates templates;



	@PostConstruct
	public void initSessionMain(){
		log.info("----- initSessionMain() --------");
		listPermiso = new ArrayList<PermisoV1>();
		usuarioLoggin = null;
		empresaLoggin = null;
		gestionLoggin = null;
		fotoPerfil = null;
		datosEmpresaDesarrolladora();
	}
	
	
	private void datosEmpresaDesarrolladora(){
		datosEmpresa= new EDatosEmpresa();
		datosEmpresa.setAutor("Ing. Ticlla Felipe David Noe");
		datosEmpresa.setDireccion("Calle Campero # 257");
		datosEmpresa.setEmail("davidticllafe@hotmail.com");
		datosEmpresa.setTelefono("736-75290");
		datosEmpresa.setNombreProducto("SISTEMA ERP-360");
	}
	

	public Usuario validarUsuario(String username,String password){
		if(usuarioLoggin == null){
			setUsuarioLoggin(usuarioRepository.findByLogin(username, password));
		}
		return getUsuarioLoggin();
	}

	public Usuario validarUsuarioV2(String username,String password){
		//if(usuarioLoggin == null){
		return	usuarioRepository.findByLogin(username, password);
		//}
		//return getUsuarioLoggin();
	}

	/**
	 * Verifica si la pagina tiene permiso de acceso
	 * @param pagina
	 * @return boolean
	 */
	public boolean tienePermisoPagina(String pagina){
		if( pagina.equals("index.xhtml") || pagina.equals("index_.xhtml") || pagina.equals("profile.xhtml")  || pagina.equals("dashboard.xhtml") || pagina.equals("certificacion.xhtml")|| pagina.equals("administradorDB.xhtml")){
			return true;//excepciones
		}
		for(PermisoV1 p: listPermiso){
			String path = p.getDetallePagina().getPagina().getPath();
			String path2 = p.getDetallePagina().getPagina().getPath2();
			path2 = path2!=null?path2:"";
			if(path.equals(pagina) || path2.equals(pagina)){
				return true;
			}
		}
		return false;
	}

	/**
	 * cargar foto del usuario
	 */
	public void setImageUserSession() {
		try{
			log.info("----- setImageUserSession() --------");
			if(getUsuarioLoggin().getPesoFoto() == 0){
				this.usuarioLoggin.setFotoPerfil(toByteArrayUsingJava(getImageDefaul("avatar.jpg").getStream()));
				this.usuarioLoggin.setPesoFoto(32);
			}
		}catch(Exception e){
			log.info("setImageUserSession() - Error: "+e.getMessage());
		}
	}

	private StreamedContent getImageDefaul(String file) {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		InputStream stream = classLoader
				.getResourceAsStream(file);
		return new DefaultStreamedContent(stream, "image/jpeg");
	}

	private static byte[] toByteArrayUsingJava(InputStream is) throws IOException{ 
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int reads = is.read();
		while(reads != -1){
			baos.write(reads); reads = is.read(); 
		}
		return baos.toByteArray();
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
			log.error("setAttributeSession() ERROR: "+e.getMessage());
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

	//----------------------------------------

	public void cargarPermisos(Rol rol){
		listPermiso = permmisoRepository.findByRol(rol);
	}

	public List<PermisoV1> getListPermiso() {
		return listPermiso;
	}

	public void setListPermiso(List<PermisoV1> listPermiso) {
		this.listPermiso = listPermiso;
	}

	public Usuario getUsuarioLoggin() {
		return usuarioLoggin;
	}

	public void setUsuarioLoggin(Usuario usuarioLoggin) {
		this.usuarioLoggin = usuarioLoggin;
	}

	public Empresa getEmpresaLoggin() {
		if(empresaLoggin == null){
			String empresa= "";
			try{
				HttpSession request = (HttpSession) facesContext.getExternalContext().getSession(false);
				empresa = request.getAttribute("empresa")!=null?request.getAttribute("empresa").toString():"";
				if(! empresa.isEmpty()){
					empresaLoggin =  empresaRepository.findByRazonSocial(empresa);
					log.info("getEmpresaLoggin() -> empresaLoggin : "+empresaLoggin.getRazonSocial());
				}
			}catch(Exception e){
				empresaLoggin =  null;
				log.error("getEmpresaLoggin() ERROR: "+e.getMessage());
			}
		}
		return empresaLoggin;
	}

	public void setEmpresaLoggin(Empresa empresaLoggin) {
		this.empresaLoggin = empresaLoggin;
	}

	public Gestion getGestionLoggin() {
		if(gestionLoggin == null ){
			Integer gestion = -1;
			try{
				HttpSession request1 = (HttpSession) facesContext.getExternalContext().getSession(false);
				gestion = request1.getAttribute("gestion")!=null?Integer.parseInt( request1.getAttribute("gestion").toString() ):-1;
				if(gestion!= -1){
					gestionLoggin = gestionRepository.findByGestionEmpresa(gestion,getEmpresaLoggin());
					log.info("getGestionLoggin() -> gestionLoggin : "+gestionLoggin.getGestion());
				}
			}catch(Exception e){
				gestionLoggin =  null;
				log.error("getGestionLoggin() ERROR: "+e.getMessage());
			}
		}
		return gestionLoggin;
	}

	public void setGestionLoggin(Gestion gestionLoggin) {
		this.gestionLoggin = gestionLoggin;
	}

	public StreamedContent getFotoPerfil() {
		log.info("----- getFotoPerfil() --------");
		if(fotoPerfil == null){
			log.info("----- fotoPerfil = null --------");
			String mimeType = "image/jpg";
			InputStream is = null;
			try{
				is= new ByteArrayInputStream(getUsuarioLoggin().getFotoPerfil());
				fotoPerfil = new DefaultStreamedContent(new ByteArrayInputStream(toByteArrayUsingJava(is)), mimeType);
			}catch(Exception e){
				log.error("getImageUserSession() -> error : "+e.getMessage());
				return null;
			}
		}
		return fotoPerfil;
	}

	public void setFotoPerfil(StreamedContent fotoPerfil) {
		this.fotoPerfil = fotoPerfil;
	}
	

	public EDatosEmpresa getDatosEmpresa() {
		return datosEmpresa;
	}


	public void setDatosEmpresa(EDatosEmpresa datosEmpresa) {
		this.datosEmpresa = datosEmpresa;
	}


	public Sucursal getSucursalLoggin() {
		if(sucursalLoggin== null){
			String sucursal= "";
			try{
				HttpSession request = (HttpSession) facesContext.getExternalContext().getSession(false);
				sucursal = request.getAttribute("sucursal")!=null?request.getAttribute("sucursal").toString():"";
				if(! sucursal.isEmpty()){
					sucursalLoggin =  sucursalRepository.findByNombre(sucursal);
					log.info("getSucursalLoggin() -> sucursalLoggin : "+sucursalLoggin.getNombre());
				}
			}catch(Exception e){
				sucursalLoggin =  null;
				log.error("getSucursalLoggin() ERROR: "+e.getMessage());
			}
		}
		return sucursalLoggin;
	}


	public void setSucursalLoggin(Sucursal sucursalLoggin) {
		this.sucursalLoggin = sucursalLoggin;
	}


	public Templates getTemplates() {
		
		if(templates == null){
			String empresa= "";
			try{
				HttpSession request = (HttpSession) facesContext.getExternalContext().getSession(false);
				empresa = request.getAttribute("templates")!=null?request.getAttribute("templates").toString():"";
				if(! empresa.isEmpty()){
					templates =  templatesRepository.findActivos();
					log.info("getTemplates() -> getTemplates : "+empresaLoggin.getRazonSocial());
				}
			}catch(Exception e){
				templates =  null;
				log.error("getTemplates() ERROR: "+e.getMessage());
			}
		}
		return templates;
	}


	public void setTemplates(Templates templates) {
		this.templates = templates;
	}


	


}
