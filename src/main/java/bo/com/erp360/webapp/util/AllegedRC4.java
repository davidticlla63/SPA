package bo.com.erp360.webapp.util;

public class AllegedRC4 {
	
	public AllegedRC4() {

    }
    public String encriptaSinguion(String mensaje,String llave){
        String resul="";
        resul=this.encripta(mensaje, llave);
        resul=resul.replace("-","");
        return resul;
    }
    public String encripta(String mensaje, String llave){
        int state[]=new int[256];
        int index1=0;
        int index2=0;
        int x=0;
        int y=0;
        int nmen=0;
        String cifrado="";
        for (int i=0;i<256;i++){
            state[i]=i;
        }
        for (int I=0;I<256;I++){
            /*index2=(obtieneAscii(llave.toCharArray()[index1])+state[o]+index2) % 256;*/
            index2=(obtieneAscii(llave.charAt(index1))+state[I]+index2) % 256;
            int aux;
            //intercambiando valores
            aux=state[I];
            state[I]=state[index2];
            state[index2]=aux;
            index1=(index1+1) % llave.length();
        }
        int uno=0;
        int dos=0;
        for(int u=0;u<mensaje.length();u++){
            x=(x+1) % 256;
            y=(state[x]+y) % 256;
            //intercambiando valor
            int aux2;
            aux2=state[x];
            state[x]=state[y];
            state[y]=aux2;
           /* uno=obtieneAscii(mensaje.toCharArray()[u]);*/
            uno=obtieneAscii(mensaje.charAt(u));
            dos=state[(state[x]+state[y]) % 256];
          /*  System.out.println("uno : "+uno+" dos : "+dos);*/
            nmen=uno^dos;
         /*   System.out.println("Nmen : "+nmen);*/
            cifrado=cifrado+"-"+rellenaCero(decimalaHexadecimal(nmen));

        }
        String Resultado;
        Resultado=cifrado.substring(1,cifrado.length());
        return Resultado;
    }
    public int obtieneAscii(char valor){
        return (int) valor;
    }
    public String rellenaCero(String valor){
        if (valor.length()==1){
            valor="0"+valor;
        }
        return valor;
    }
    public String decimalaHexadecimal(int valor){
        return Integer.toHexString(valor).toUpperCase();
    }

    
	public static void main(String[] args) {
		AllegedRC4 allegedRC4 = new AllegedRC4();
		System.out.println(allegedRC4.encripta("d3Ir6", "sesamo"));
		System.out.println(allegedRC4.encripta("piWCp", "Aa1-bb2-Cc3-Dd4")); 
		System.out.println(allegedRC4.encripta("IUKYo", "XBCPY-GKGX4-PGK44-8B632-X9P33")); 
		System.out.println(allegedRC4.encripta("de3fF", "AbCd321")); 
	}
	
}
