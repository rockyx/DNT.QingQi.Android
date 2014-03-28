package dnt.diag.ecu;

public class DiagException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3286385072092277020L;

	public DiagException() {

	}

	public DiagException(String msg) {
		super(msg);
	}
}
