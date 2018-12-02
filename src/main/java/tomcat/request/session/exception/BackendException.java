package tomcat.request.session.exception;

/** author: Ranjith Manickam @ 3 Dec' 2018 */
public class BackendException extends RuntimeException {

    private static final String ERROR_MESSAGE = "For some reason, we could process your request. Please contact system administrator for more details.";

    public BackendException() {
        super(ERROR_MESSAGE, null, false, false);
    }
}
