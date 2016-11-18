package bo.com.erp360.webapp.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import bo.com.erp360.webapp.data.EmpresaRepository;
import bo.com.erp360.webapp.data.SucursalRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Sucursal;

/**
 * 
 * @author David.Ticlla.Felipe
 *
 */

@Named(value = "dynamicBackgroundController")
@RequestScoped
public class DynamicBackgroundController extends AbstractDynamicResourceHandler
		implements Serializable {

	private static final long serialVersionUID = -3756873687377670050L;

	// REPOSITORY
	private @Inject SucursalRepository sucursalRepository;
	// OBJECT
	public static StreamedContent streamedContentimageProducto;

	@PostConstruct
	public void init() {

	}

	private StreamedContent imageDefault(Empresa paciente) {
		/*
		 * String url = FacesContext.getCurrentInstance().getExternalContext()
		 * .getRealPath("/"); System.out.println("url = " + url); String path =
		 * ""; if (paciente.getSexo().equals("Masculino")) { path = url +
		 * "resources/paciente_default.png"; } else { path = url +
		 * "resources/usuaria_default.png"; } try { content = new
		 * DefaultStreamedContent(new FileInputStream(path), "image/png");
		 * DynamicResourceController.streamedContentimageProducto = content;
		 * return content; } catch (FileNotFoundException e) {
		 * e.printStackTrace(); return new DefaultStreamedContent(); }
		 */
		return null;
	}

	private byte[] toByteArrayUsingJava(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int reads = is.read();
		while (reads != -1) {
			baos.write(reads);
			reads = is.read();
		}
		return baos.toByteArray();
	}

	public void handleFileUpload(FileUploadEvent event) {
		FacesUtil.infoMessage("Correcto", event.getFile().getFileName()
				+ " fue cargado.");
		try {
			InputStream molde = event.getFile().getInputstream();
			byte[] image = toByteArrayUsingJava(molde);
			FacesUtil.setSessionAttribute("imageBackground", image);
		} catch (Exception e) {
			FacesUtil.errorMessage("No se pudo subir la imagen.");
		}
	}

	public StreamedContent getImageimageProducto() {
		String mimeType = "image/jpg";
		InputStream is = null;
		try {
			byte[] image = (byte[]) FacesUtil
					.getSessionAttribute("imageBackground");
			is = new ByteArrayInputStream(image);
			return new DefaultStreamedContent(new ByteArrayInputStream(
					toByteArrayUsingJava(is)), mimeType);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	protected StreamedContent buildStreamedContentImage(FacesContext context,
			Integer idObject) throws Exception {
		InputStream is = null;
		streamedContentimageProducto = null;
		Sucursal entity = sucursalRepository.findById(idObject);
		try {
			streamedContentimageProducto = getImageimageProducto();
			if (streamedContentimageProducto != null) {
				return streamedContentimageProducto;
			} else {
				if (entity != null) {
					if (idObject != 0 && entity.getLogo() != null) {
						FacesUtil.setSessionAttribute("imageBackground",entity.getLogo());
						is = new ByteArrayInputStream(entity.getLogo());
						streamedContentimageProducto = new DefaultStreamedContent(
								new ByteArrayInputStream(
										toByteArrayUsingJava(is)));
						return streamedContentimageProducto;
					} else {
						if (idObject != 0 && entity.getLogo() == null) {
							streamedContentimageProducto = getImageimageProducto();
							if (streamedContentimageProducto == null) {
								streamedContentimageProducto = new DefaultStreamedContent();
							}
						} else {
							if (idObject == 0) {
								streamedContentimageProducto = getImageimageProducto();
								if (streamedContentimageProducto == null) {
									streamedContentimageProducto = new DefaultStreamedContent();
								}
							} else {
								String path = FacesContext.getCurrentInstance()
										.getExternalContext()
										.getRealPath("/resources/gfx");
								InputStream inputStream = new FileInputStream(
										path + File.separator
												+ "icon_product.png");
								streamedContentimageProducto = new DefaultStreamedContent(
										inputStream, "image/png");
							}
						}
					}

				} else if (idObject == 0) {
					streamedContentimageProducto = getImageimageProducto();
					if (streamedContentimageProducto == null) {
						streamedContentimageProducto = new DefaultStreamedContent();
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Error obenter Imagen: " + e.getMessage());
		}
		return streamedContentimageProducto;
	}

	public void setStreamedContentimageProducto(
			StreamedContent streamedContentimageProducto) {
		DynamicBackgroundController.streamedContentimageProducto = streamedContentimageProducto;
	}

}
