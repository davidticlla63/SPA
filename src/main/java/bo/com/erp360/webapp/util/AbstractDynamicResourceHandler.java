package bo.com.erp360.webapp.util;

import java.io.FileInputStream;
import java.util.Map;

import javax.faces.application.ResourceHandler;
import javax.faces.context.FacesContext;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * 
 * @author David.Ticlla.Felipe
 *
 */
public abstract class AbstractDynamicResourceHandler {

	private static final StreamedContent EMPTY_STREAMED_CONTENT = new DefaultStreamedContent();

	/**
	 * MOLDE
	 * 
	 * @return StreamedContent
	 * @throws Exception
	 */
	public StreamedContent getStreamedContentImage() throws Exception {
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceHandler handler = context.getApplication().getResourceHandler();

		Map<String, String> params = context.getExternalContext()
				.getRequestParameterMap();
		String param = params.get("param");
		System.out.println(param);
		if (handler.isResourceRequest(context)){
			return buildStreamedContentImage(context,Integer.parseInt(param));
		}else{
			return EMPTY_STREAMED_CONTENT;
		}
//		if (handler.isResourceRequest(context)) {
//			return buildStreamedContentImage(context, Integer.parseInt(param));
//		} else {
//			String url = FacesContext.getCurrentInstance().getExternalContext()
//					.getRealPath("/")
//					+ "resources/paciente_default.png";
//			return new DefaultStreamedContent(new FileInputStream(url),
//					"image/png");
//		}
	}

	protected abstract StreamedContent buildStreamedContentImage(
			FacesContext context, Integer idObject) throws Exception;

}