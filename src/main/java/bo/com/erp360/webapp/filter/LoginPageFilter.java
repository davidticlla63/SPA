package bo.com.erp360.webapp.filter;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import bo.com.erp360.webapp.util.SessionMain;

public class LoginPageFilter implements Filter{

	private Logger log = Logger.getLogger(this.getClass());
	
	@Inject
	private SessionMain sessionMain;

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,   FilterChain filterChain) throws IOException, ServletException{
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		String page = obtenerPartePagina(request.getRequestURI().toString());
		log.info(" doFilter("+page+")");
		if(request.getUserPrincipal() != null){ 
			if(sessionMain.tienePermisoPagina(page)){
				log.info(" page="+page+"------ tiene permiso ----");
				//response.sendRedirect(request.getContextPath()+"/pages/"+page);
				filterChain.doFilter(servletRequest, servletResponse);
			}else{
				log.info(" page="+page+"------ no tiene permiso ----");
				response.sendRedirect(request.getContextPath()+"/error403.xhtml");
			}
		} else{
			filterChain.doFilter(servletRequest, servletResponse);
		}
	}
	
	private String obtenerPartePagina(String uri){
		int length = uri.length() - 1;
		for(int i=length; i>0 ; i--){
			String letra = String.valueOf(uri.charAt(i));
			if(letra.equals("/")){
				return uri.substring(i+1, length+1);
			}
		}
		return null;
	}

	@Override
	public void destroy(){
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException{
	}
}