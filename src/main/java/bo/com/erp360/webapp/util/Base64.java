package bo.com.erp360.webapp.util;

public class Base64 {
	
	 private String resultado;
	    private long va;
	    public Base64(long valor) {
	        va=valor;
	    }

	    /**
	     * @return the resultado
	     */
	    public String getResultado() {
	        long cociente=1;
	        long resto=0;
	        String resul="";
	        while (cociente>0){
	            cociente=va/64;
	            resto=va%64;
	            Diccionario dic=new Diccionario();
	            resul=dic.getValor((int)resto)+resul;
	            va=cociente;
	        }
	        resultado=resul;
	        return resultado;
	    }
	    
	    public String getResultado(int valor) {
	    	va=valor;
	        long cociente=1;
	        long resto=0;
	        String resul="";
	        while (cociente>0){
	            cociente=va/64;
	            resto=va%64;
	            Diccionario dic=new Diccionario();
	            resul=dic.getValor((int)resto)+resul;
	            va=cociente;
	        }
	        resultado=resul;
	        return resultado;
	    }


	
	    
	    
		public static void main(String[] args) {
			Base64 base64= new Base64(1);
			System.out.println(base64.getResultado(934598591));
			System.out.println(base64.getResultado(434376710));
			System.out.println(base64.getResultado(204986118));
		}
}
