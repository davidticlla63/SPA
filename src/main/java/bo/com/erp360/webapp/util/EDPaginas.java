package bo.com.erp360.webapp.util;

public class EDPaginas {
	private String nombre;
	private int numero;

	
	
	public EDPaginas(String nombre, int numero) {
		super();
		this.nombre = nombre;
		this.numero = numero;
	}

	public int getNumero() {
		return numero;
	}

	public void setNumero(int numero) {
		this.numero = numero;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

}
