package at.splendit.simonykees.core.exception.runtime;

public class ITypeNotFoundRuntimeException extends RuntimeException {
	private static final long serialVersionUID = -4800792235524391680L;

	public ITypeNotFoundRuntimeException() {
        super();
    }


    public ITypeNotFoundRuntimeException(String message) {
        super(message);
    }


    public ITypeNotFoundRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ITypeNotFoundRuntimeException(Throwable cause) {
        super(cause);
    }

    protected ITypeNotFoundRuntimeException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
	

}
