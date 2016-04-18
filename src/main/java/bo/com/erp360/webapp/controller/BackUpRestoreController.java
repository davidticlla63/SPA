package bo.com.erp360.webapp.controller;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.event.Event;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.jam.xml.TunnelledException;
import org.primefaces.event.FileUploadEvent;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.model.ConfiguracionDB;
import bo.com.erp360.webapp.service.ConfiguracionDBRegistration;
import bo.com.erp360.webapp.util.CodigoControl7;
import bo.com.erp360.webapp.util.FacesUtil;
import bo.com.erp360.webapp.util.SessionMain;
import bo.com.erp360.webapp.util.Time;

@Named(value = "backUpRestoreController")
@ConversationScoped
public class BackUpRestoreController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5499546254827991832L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEvent;

	private Logger log = Logger.getLogger(this.getClass());

	private ConfiguracionDB configuracionDB;

	private @Inject ConfiguracionDBRegistration configuracionDBRegistration;

	// login
	private @Inject SessionMain sessionMain; // variable del login
	private String nombreUsuario;

	private boolean aspecto = false; // true=nuevo,false=modificar

	@PostConstruct
	public void initBackUpRestore() {
		nombreUsuario = sessionMain.getUsuarioLoggin().getLogin();
		validar();
	}

	public void registrar() {
		if (aspecto) {
			configuracionDB.setEstado("AC");
			configuracionDB.setUsuarioRegistro(nombreUsuario);
			configuracionDB.setFechaRegistro(new Date());
			configuracionDB = configuracionDBRegistration
					.create(configuracionDB);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Datos Registrado!", "" + configuracionDB.getId());
		} else if (!aspecto) {
			configuracionDB.setEstado("AC");
			configuracionDB.setUsuarioRegistro(nombreUsuario);
			configuracionDB.setFechaRegistro(new Date());
			configuracionDB = configuracionDBRegistration
					.update(configuracionDB);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Datos Registrado!", "" + configuracionDB.getId());
		}
	}

	public void validar() {
		List<ConfiguracionDB> lisConfiguracionDBs = configuracionDBRegistration
				.obtenerConfiguracionDB();
		if (lisConfiguracionDBs.size() == 1) {
			configuracionDB = lisConfiguracionDBs.get(0);
			aspecto = false;
		} else {
			configuracionDB = new ConfiguracionDB();
			aspecto = false;
		}
	}

	public boolean backUp() {
		try {
			Process p;
			ProcessBuilder pb;
			String archivo = configuracionDB.getDatabase()
					+ Time.obtenerFormatoYYYYMMDDHHMISS(new Date()) + ".backup";
			String pathDir = configuracionDB.getPath() + "\\" + archivo;
			pb = new ProcessBuilder(configuracionDB.getDirBin()
					+ "\\pg_dump.exe", "-i", "-h", configuracionDB.getHost(),
					"-p", configuracionDB.getPuerto(), "-U",
					configuracionDB.getUser(), "-F", "c", "-b", "-v", "-f",
					pathDir, configuracionDB.getDatabase());
			pb.environment().put("PGPASSWORD", configuracionDB.getPassword());
			pb.redirectErrorStream(true);
			p = pb.start();
			/*p.waitFor();*/
			System.out.println(pathDir);
			System.out
					.println("Termino respaldo " + configuracionDB.toString());
			FacesUtil.infoMessage("Se Realizo la Copia de Seguridad!", pathDir);
			/*p.destroy();*/
			return true;
		} catch (Exception e) {
			FacesUtil.warnMessage("No se pudo hacer el BackUp.!"
					+ e.getStackTrace());
			return false;
		}
	}

	private String lblMessage;

	public boolean restore(FileUploadEvent event) {
		try {
			/*
			 * ServletContext ctx = (ServletContext)
			 * FacesContext.getCurrentInstance
			 * ().getExternalContext().getContext(); String basePath =
			 * ctx.getRealPath("/"); System.out.println("basePath : "+basePath);
			 * String txtPath = basePath + "BackupRestoreMysqldb//";
			 * System.out.println("txtPath : "+txtPath +
			 * event.getFile().getFileName());
			 */
			if (event.getFile().getFileName().equals("")) {
				lblMessage = "Please Select file to restore!";
				System.out.println(lblMessage);
				return false;
			} else {

				ProcessBuilder pb;
				pb = new ProcessBuilder(configuracionDB.getDirBin()
						+ "\\pg_restore.exe", "-i", "-h",
						configuracionDB.getHost(), "-p",
						configuracionDB.getPuerto(), "-U",
						configuracionDB.getUser(), "-d",
						configuracionDB.getDatabase(), "-v",
						configuracionDB.getPath() + "\\"
								+ event.getFile().getFileName());
				pb.environment().put("PGPASSWORD",
						configuracionDB.getPassword());
				pb.redirectErrorStream(true);
				pb.start();
				System.out
				.println("Termino restauracion " + configuracionDB.toString());
				FacesUtil.infoMessage("Termino restauracion",
						configuracionDB.getPath() + "\\"
								+ event.getFile().getFileName());
				return true;
			}

		} catch (Exception e) {
			FacesUtil.warnMessage("Error No se ha podido Restaurar: "
					+ e.getStackTrace());
			return false;
		}
	}

	public ConfiguracionDB getConfiguracionDB() {
		return configuracionDB;
	}

	public void setConfiguracionDB(ConfiguracionDB configuracionDB) {
		this.configuracionDB = configuracionDB;
	}

}
